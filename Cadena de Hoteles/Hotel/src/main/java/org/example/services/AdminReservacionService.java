package org.example.services;

import org.example.dtos.ResultadoNotificacionDTO;
import org.example.helpers.EmailHelper;
import org.example.repositories.AdminReservacionRepository;
import org.example.repositories.LogReservacionRepository;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service para la gestion de reservaciones desde el panel de administracion.
 * Permite listar todas las reservaciones y cancelarlas con:
 * <ul>
 *   <li>Validacion de estado (solo Pendiente=1 o Confirmada=2).</li>
 *   <li>Notificacion al sistema externo de la agencia ANTES de cancelar en BD.</li>
 *   <li>Correo de cancelacion al usuario dueno de la reservacion (best-effort).</li>
 * </ul>
 *
 * ORDEN CRITICO de cancelarReservacion:
 *   1. Validar estado en nuestra BD.
 *   2. Leer datos del usuario (mientras la fila todavia esta activa).
 *   3. POST al sistema externo de la agencia  <-- PRIMERO que cualquier UPDATE.
 *   4. UPDATE en nuestra BD (EstadoID = 4).
 *   5. Correo al usuario (best-effort).
 */
public class AdminReservacionService {

    private static final Logger LOG = Logger.getLogger(AdminReservacionService.class.getName());

    private final AdminReservacionRepository      repo;
    private final AgenciaNotificadorExternoService notificadorAgencia;
    private final LogReservacionRepository         logRepo;

    /**
     * Crea una instancia de AdminReservacionService con sus dependencias inyectadas.
     *
     * @param repo               repository de reservaciones del administrador.
     * @param notificadorAgencia service que notifica al sistema externo de la agencia.
     * @param logRepo            repository para registrar eventos de auditoria.
     */
    public AdminReservacionService(AdminReservacionRepository      repo,
                                   AgenciaNotificadorExternoService notificadorAgencia,
                                   LogReservacionRepository         logRepo) {
        this.repo               = repo;
        this.notificadorAgencia = notificadorAgencia;
        this.logRepo            = logRepo;
    }

    /**
     * Retorna todas las reservaciones registradas en el sistema.
     * @return lista de mapas con los datos de cada reservacion.
     */
    public List<Map<String, Object>> listarTodas() {
        return repo.listarTodas();
    }

    /**
     * Retorna las N reservaciones mas recientes (version ligera para el dashboard).
     * @param n cantidad maxima de filas.
     * @return lista de mapas con datos resumidos.
     */
    public List<Map<String, Object>> listarRecientes(int n) {
        return repo.listarRecientes(n);
    }

    /**
     * Cancela una reservacion siguiendo este orden estricto:
     * <ol>
     *   <li>Valida que exista y que su estado sea Pendiente (1) o Confirmada (2).</li>
     *   <li>Lee los datos del usuario para el correo (fila aun activa).</li>
     *   <li>Llama al endpoint externo de la agencia — SIN haber tocado la BD todavia.</li>
     *   <li>Actualiza el estado a Cancelada (4) en nuestra BD.</li>
     *   <li>Envia el correo HTML al usuario (best-effort).</li>
     * </ol>
     *
     * @param reservacionId ID de la reservacion a cancelar.
     * @param motivo        razon de la cancelacion ingresada por el administrador.
     * @return {@link ResultadoNotificacionDTO} con el resultado de la notificacion a la agencia.
     * @throws IllegalArgumentException si la reservacion no existe o su estado no permite cancelacion.
     */
    public ResultadoNotificacionDTO cancelarReservacion(int reservacionId, String motivo,
                                                        String ip, String userAgent) {

        // ------------------------------------------------------------------
        // PASO 1: Validar existencia y estado — solo lectura, ningun UPDATE aun
        // ------------------------------------------------------------------
        Object[] datos = repo.obtenerReservacion(reservacionId);
        if (datos == null) {
            logRepo.registrar(LogReservacionRepository.TIPO_CANCELACION_ADMIN_FALLIDA,
                    reservacionId, null, null, null, null, false, ip, userAgent,
                    "Reservacion #" + reservacionId + " no encontrada");
            throw new IllegalArgumentException("Reservacion #" + reservacionId + " no encontrada");
        }

        int    estadoId = (int)    datos[1];
        String estado   = (String) datos[2];

        if (estadoId != 1 && estadoId != 2) {
            logRepo.registrar(LogReservacionRepository.TIPO_CANCELACION_ADMIN_FALLIDA,
                    reservacionId, null, null, null, null, false, ip, userAgent,
                    "Cancelacion rechazada: estado actual es \"" + estado + "\"");
            throw new IllegalArgumentException(
                    "No se puede cancelar: estado actual es \"" + estado + "\""
            );
        }

        // ------------------------------------------------------------------
        // PASO 2: Leer datos del usuario para el correo (fila aun activa)
        // ------------------------------------------------------------------
        Object[] datosUsuario = repo.obtenerDatosUsuarioPorReservacion(reservacionId);

        // ------------------------------------------------------------------
        // PASO 3: Notificar a la agencia ANTES de modificar nuestra BD.
        //         El detalle en el sistema externo sigue activo en este punto.
        // ------------------------------------------------------------------
        ResultadoNotificacionDTO resultadoAgencia =
                notificadorAgencia.notificarCancelacion(reservacionId, motivo.trim());

        // ------------------------------------------------------------------
        // PASO 4: Ahora si, cancelar en nuestra BD (EstadoID = 4)
        // ------------------------------------------------------------------
        repo.cancelarReservacion(reservacionId, motivo);

        // Log de auditoria: cancelacion admin exitosa
        String noResLog = (datosUsuario != null) ? (String) datosUsuario[2] : null;
        Double totalLog  = (datosUsuario != null) ? (Double) datosUsuario[3] : null;
        logRepo.registrar(LogReservacionRepository.TIPO_CANCELACION_ADMIN_EXITOSA,
                reservacionId, null, null, noResLog, totalLog, true, ip, userAgent,
                "Cancelada por admin. Motivo: " + motivo.trim());

        // ------------------------------------------------------------------
        // PASO 5: Correo al usuario (best-effort: un error aqui no aborta nada)
        // ------------------------------------------------------------------
        if (datosUsuario != null) {
            try {
                String correo        = (String) datosUsuario[0];
                String nombreUsuario = (String) datosUsuario[1];
                String noReservacion = (String) datosUsuario[2];
                double total         = (Double) datosUsuario[3];

                EmailHelper.enviar(
                        correo,
                        "Blink Hotels – Reservacion " + noReservacion + " Cancelada",
                        construirCorreoCancelacion(nombreUsuario, noReservacion, total, motivo.trim())
                );
            } catch (Exception ex) {
                LOG.log(Level.WARNING,
                        "Error al enviar correo de cancelacion. ReservacionId=" + reservacionId, ex);
            }
        }

        return resultadoAgencia;
    }

    // -------------------------------------------------------------------------
    // Metodo privado: construye el HTML del correo de cancelacion
    // -------------------------------------------------------------------------

    private static String construirCorreoCancelacion(
            String nombreUsuario,
            String noReservacion,
            double total,
            String motivo) {

        int anio = Year.now().getValue();

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><title>Cancelacion de Reservacion</title></head>
                <body style="margin:0;padding:0;background:#F4F6F8;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background:#F4F6F8;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:8px;
                                    box-shadow:0 4px 20px rgba(0,0,0,.08);overflow:hidden;">

                        <!-- Cabecera -->
                        <tr>
                          <td style="background:#1A3C5E;padding:28px 40px;text-align:center;">
                            <h1 style="margin:0;font-size:22px;font-weight:300;color:#ffffff;
                                       letter-spacing:3px;">
                              BLINK
                              <span style="color:#F0A500;font-weight:700;">HOTELS</span>
                            </h1>
                          </td>
                        </tr>

                        <!-- Cuerpo -->
                        <tr>
                          <td style="padding:32px 40px;">
                            <p style="margin:0 0 8px;font-size:16px;color:#1A3C5E;font-weight:600;">
                              Hola, %s:
                            </p>
                            <p style="margin:0 0 24px;font-size:14px;color:#555;">
                              Te informamos que tu reservacion ha sido cancelada por el equipo de
                              administracion. A continuacion encontraras el resumen:
                            </p>

                            <!-- Resumen -->
                            <table width="100%%" cellpadding="8" cellspacing="0"
                                   style="background:#F4F6F8;border-radius:6px;margin-bottom:24px;">
                              <tr>
                                <td style="font-size:12px;color:#888;width:40%%;">N° Reservacion</td>
                                <td style="font-size:14px;font-weight:700;color:#1A3C5E;
                                           font-family:monospace;">%s</td>
                              </tr>
                              <tr>
                                <td style="font-size:12px;color:#888;">Total</td>
                                <td style="font-size:14px;font-weight:700;">$%.2f</td>
                              </tr>
                            </table>

                            <!-- Motivo -->
                            <div style="background:#FDECEA;border-left:4px solid #C62828;
                                        padding:14px 18px;border-radius:4px;margin-bottom:24px;">
                              <p style="margin:0 0 4px;font-size:12px;color:#C62828;font-weight:700;">
                                Motivo de cancelacion
                              </p>
                              <p style="margin:0;font-size:14px;color:#333;">%s</p>
                            </div>

                            <p style="margin:0;font-size:13px;color:#777;">
                              Si tienes alguna pregunta, no dudes en contactarnos.
                            </p>
                          </td>
                        </tr>

                        <!-- Pie -->
                        <tr>
                          <td style="background:#1A3C5E;padding:16px 40px;text-align:center;">
                            <p style="margin:0;font-size:11px;color:#90A4AE;">
                              &copy; %d Blink Hotels — Todos los derechos reservados
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(nombreUsuario, noReservacion, total, motivo, anio);
    }
}