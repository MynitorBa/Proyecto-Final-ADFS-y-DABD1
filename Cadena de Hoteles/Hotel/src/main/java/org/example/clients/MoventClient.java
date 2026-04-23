package org.example.clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cliente HTTP best-effort para notificar a Movent cuando un hotel de Miku cierra
 * y cancela reservaciones que pertenecian a agencias de viaje.
 *
 * <p>NOTA DE ARQUITECTURA: La notificacion per-reservacion se realiza via
 * {@code AgenciaNotificadorExternoService}, que usa el endpoint existente
 * {@code POST {URL_Agencia}/api/proveedores-ext/detalles/{id}/cancelar}
 * con autenticacion por token de agencia. Este cliente es un complemento
 * para enviar un aviso de resumen cuando se desee un endpoint receptor de
 * notificaciones de cierre masivo en Movent.
 *
 * <p>La URL del webhook se configura via variable de entorno
 * {@code MOVENT_WEBHOOK_URL}. Si no esta definida, se usa el default de desarrollo.
 *
 * <p>Todos los metodos son best-effort: un fallo en el envio solo genera
 * un log de advertencia y NO propaga la excepcion al flujo principal.
 */
public class MoventClient {

    private static final Logger LOG = Logger.getLogger(MoventClient.class.getName());

    private static final String MOVENT_URL = System.getenv().getOrDefault(
            "MOVENT_WEBHOOK_URL",
            "http://localhost:8080/api/webhooks/proveedor/hotel-cerrado"
    );

    private static final int CONNECT_TIMEOUT = 3;
    private static final int REQUEST_TIMEOUT = 5;

    /**
     * Notifica a Movent que un hotel cerro y se cancelaron reservaciones
     * que originalmente venian via agencia de viaje.
     * Best-effort: si falla, log error pero NO propaga excepcion.
     *
     * @param hotelNombre   nombre del hotel que fue cerrado.
     * @param reservasAgencia lista de reservas afectadas (de agencias, no usuarios directos).
     *                       Cada mapa debe tener: noReservacion, correo, total.
     */
    public static void notificarHotelCerrado(
            String hotelNombre,
            List<Map<String, Object>> reservasAgencia
    ) {
        if (reservasAgencia == null || reservasAgencia.isEmpty()) {
            LOG.info("[MoventClient] Sin reservas de agencia afectadas — webhook omitido.");
            return;
        }

        try {
            String body = construirJson(hotelNombre, reservasAgencia);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MOVENT_URL))
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT))
                    .header("Content-Type", "application/json")
                    .header("X-Miku-Webhook", "hotel-cerrado")
                    .header("X-Miku-Token", System.getenv().getOrDefault("MOVENT_WEBHOOK_TOKEN", ""))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                LOG.info("[MoventClient] Webhook enviado correctamente. " +
                         reservasAgencia.size() + " reserva(s) notificadas. Hotel: " + hotelNombre);
            } else {
                LOG.warning("[MoventClient] Webhook respondio " + response.statusCode() +
                            " para hotel '" + hotelNombre + "': " + response.body());
            }

        } catch (Exception ex) {
            LOG.log(Level.WARNING,
                    "[MoventClient] Error enviando webhook de cierre de hotel '" + hotelNombre + "'", ex);
        }
    }

    /**
     * Notifica a Movent que una habitacion cerro y se cancelaron reservaciones
     * que originalmente venian via agencia de viaje.
     * Best-effort: si falla, log error pero NO propaga excepcion.
     *
     * @param habitacionId    ID de la habitacion que fue cerrada.
     * @param reservasAgencia lista de reservas afectadas (solo las de agencia).
     *                        Cada mapa debe tener: noReservacion, correo, total.
     */
    public static void notificarHabitacionCerrada(
            int habitacionId,
            List<Map<String, Object>> reservasAgencia
    ) {
        if (reservasAgencia == null || reservasAgencia.isEmpty()) {
            LOG.info("[MoventClient] Sin reservas de agencia afectadas por habitacion " + habitacionId + " — webhook omitido.");
            return;
        }

        String webhookUrl = System.getenv().getOrDefault(
                "MOVENT_WEBHOOK_URL",
                "http://localhost:8080/api/webhooks/proveedor/hotel-cerrado"
        );

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{")
              .append("\"tipo\":\"habitacion_cerrada\",")
              .append("\"proveedor\":\"MikuInn\",")
              .append("\"habitacionId\":").append(habitacionId).append(",")
              .append("\"reservas\":[");

            for (int i = 0; i < reservasAgencia.size(); i++) {
                Map<String, Object> r = reservasAgencia.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                  .append("\"noReservacion\":\"").append(escape(str(r.get("noReservacion")))).append("\",")
                  .append("\"correo\":\"").append(escape(str(r.get("correo")))).append("\",")
                  .append("\"total\":").append(r.get("total"))
                  .append("}");
            }

            sb.append("]}");
            String body = sb.toString();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT))
                    .header("Content-Type", "application/json")
                    .header("X-Miku-Webhook", "habitacion-cerrada")
                    .header("X-Miku-Token", System.getenv().getOrDefault("MOVENT_WEBHOOK_TOKEN", ""))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                LOG.info("[MoventClient] Webhook habitacion_cerrada enviado. " +
                         reservasAgencia.size() + " reserva(s). HabitacionID=" + habitacionId);
            } else {
                LOG.warning("[MoventClient] Webhook habitacion_cerrada respondio " + response.statusCode() +
                            " para habitacion " + habitacionId + ": " + response.body());
            }

        } catch (Exception ex) {
            LOG.log(Level.WARNING,
                    "[MoventClient] Error enviando webhook de cierre de habitacion " + habitacionId, ex);
        }
    }

    // -------------------------------------------------------------------------
    // Construccion manual del JSON (sin dependencia de Jackson en el cliente)
    // -------------------------------------------------------------------------

    private static String construirJson(String hotelNombre,
                                        List<Map<String, Object>> reservas) {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"tipo\":\"hotel_cerrado\",")
          .append("\"proveedor\":\"MikuInn\",")
          .append("\"hotelNombre\":\"").append(escape(hotelNombre)).append("\",")
          .append("\"reservaciones\":[");

        for (int i = 0; i < reservas.size(); i++) {
            Map<String, Object> r = reservas.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"noReservacion\":\"").append(escape(str(r.get("noReservacion")))).append("\",")
              .append("\"correo\":\"").append(escape(str(r.get("correo")))).append("\",")
              .append("\"total\":").append(r.get("total"))
              .append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
