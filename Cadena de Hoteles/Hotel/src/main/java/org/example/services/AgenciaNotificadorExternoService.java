package org.example.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.DatabaseManager;
import org.example.dtos.ResultadoNotificacionDTO;

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
 * Service que notifica al sistema externo de una agencia cuando el administrador
 * cancela una reservacion que le pertenece (es decir, cuyo Usuario_ID coincide
 * con el UsuarioWebis_ID de alguna agencia).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Busca en BD si la reservacion pertenece a una agencia.</li>
 *   <li>Si no pertenece a ninguna agencia retorna un DTO con esReservaDeAgencia=false.</li>
 *   <li>Valida que la agencia tenga URL_Agencia y Token_HASH_Salida configurados.</li>
 *   <li>Hace POST a {URL_Agencia}/api/proveedores-ext/detalles/{reservacionId}/cancelar
 *       con el header X-Agencia-Token y body {"mensaje": motivo}.</li>
 *   <li>Devuelve el resultado (httpStatus, respuestaAgencia, error) para que el
 *       controller lo incluya en la respuesta al administrador.</li>
 * </ol>
 *
 * <p>Esta clase sigue el patron best-effort: un fallo al notificar a la agencia
 * NO impide que la cancelacion se aplique en nuestra BD (esa decision la toma
 * AdminReservacionService, que llama a este service primero y luego cancela).
 */
public class AgenciaNotificadorExternoService {

    private static final Logger LOG = Logger.getLogger(AgenciaNotificadorExternoService.class.getName());

    private static final int TIMEOUT_SEGUNDOS = 10;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Crea el service con sus propios HttpClient y ObjectMapper.
     * Se puede sobrecargar en tests pasando mocks.
     */
    public AgenciaNotificadorExternoService() {
        this.httpClient   = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructor package-private para tests unitarios con inyeccion de dependencias.
     * Permite sustituir HttpClient y ObjectMapper por mocks sin tocar el constructor publico.
     *
     * @param httpClient   cliente HTTP a utilizar en los tests.
     * @param objectMapper serializador JSON a utilizar en los tests.
     */
    AgenciaNotificadorExternoService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient   = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Intenta notificar al sistema externo de la agencia duena de la reservacion.
     *
     * @param reservacionId ID de la reservacion que fue cancelada.
     * @param motivo        razon de la cancelacion ingresada por el administrador.
     * @return {@link ResultadoNotificacionDTO} con el resultado de la operacion.
     *         Nunca retorna null; si no es reserva de agencia, el DTO tendra
     *         esReservaDeAgencia=false y el resto de campos vacios.
     */
    public ResultadoNotificacionDTO notificarCancelacion(int reservacionId, String motivo) {

        ResultadoNotificacionDTO resultado = new ResultadoNotificacionDTO();

        try {
            // ------------------------------------------------------------------
            // 1. Buscar si la reservacion pertenece a una agencia
            //    La reservacion tiene Usuario_ID; ese usuario puede ser el
            //    UsuarioWebis_ID de alguna agencia.
            // ------------------------------------------------------------------
            final String SQL_AGENCIA =
                    "SELECT a.ID, a.Nombre, " +
                            "       NVL(a.Token_HASH_Salida, '') AS Token_HASH_Salida, " +
                            "       NVL(a.URL_Agencia,       '') AS URL_Agencia " +
                            "FROM   Agencia     a " +
                            "JOIN   Reservacion r ON r.Usuario_ID = a.UsuarioWebis_ID " +
                            "WHERE  r.ID = ?";

            List<String[]> filas = DatabaseManager.executeQuery(
                    SQL_AGENCIA,
                    rs -> new String[]{
                            String.valueOf(rs.getInt("ID")),
                            rs.getString("Nombre"),
                            rs.getString("Token_HASH_Salida"),
                            rs.getString("URL_Agencia")
                    },
                    reservacionId
            );

            if (filas.isEmpty()) {
                // No es reserva de agencia; nada que notificar
                return resultado;
            }

            String[] fila   = filas.get(0);
            String nombre   = fila[1];
            String token    = fila[2];
            String url      = fila[3];

            resultado.setEsReservaDeAgencia(true);
            resultado.setNombreAgencia(nombre);

            // ------------------------------------------------------------------
            // 2. Validar configuracion de la agencia
            // ------------------------------------------------------------------
            if (url == null || url.isBlank()) {
                String err = "Agencia '" + nombre + "' no tiene URL_Agencia configurada.";
                resultado.setError(err);
                LOG.warning("[AgenciaNotificador] " + err);
                return resultado;
            }

            if (token == null || token.isBlank()) {
                String err = "Agencia '" + nombre + "' no tiene Token_HASH_Salida configurado.";
                resultado.setError(err);
                LOG.warning("[AgenciaNotificador] " + err);
                return resultado;
            }

            // ------------------------------------------------------------------
            // 3. Construir y enviar la peticion HTTP
            //    POST {URL}/api/proveedores-ext/detalles/{id}/cancelar
            //    Header:  X-Agencia-Token: {token}
            //    Body:    {"mensaje": "{motivo}"}
            // ------------------------------------------------------------------
            String endpoint = url.stripTrailing().replaceAll("/+$", "")
                    + "/api/proveedores-ext/detalles/" + reservacionId + "/cancelar";

            String bodyJson = objectMapper.writeValueAsString(Map.of("mensaje", motivo));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                    .header("Content-Type", "application/json")
                    .header("X-Agencia-Token", token)
                    .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                    .build();

            LOG.info("[AgenciaNotificador] POST " + endpoint);

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            resultado.setEnviado(true);
            resultado.setHttpStatus(response.statusCode());
            resultado.setRespuestaAgencia(response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                LOG.info("[AgenciaNotificador] HTTP " + response.statusCode()
                        + " — " + response.body());
            } else {
                LOG.warning("[AgenciaNotificador] HTTP " + response.statusCode()
                        + " — " + response.body());
            }

        } catch (Exception ex) {
            resultado.setError(ex.getMessage());
            LOG.log(Level.SEVERE,
                    "[AgenciaNotificador] Error. ReservacionId=" + reservacionId, ex);
        }

        return resultado;
    }
}