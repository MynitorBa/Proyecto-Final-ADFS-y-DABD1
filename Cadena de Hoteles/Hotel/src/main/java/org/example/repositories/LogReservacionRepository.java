package org.example.repositories;

import org.example.data.DatabaseManager;

/**
 * Repository para el registro de eventos de reservacion en la tabla LogReservacion.
 */
public class LogReservacionRepository {

    public static final int TIPO_RESERVACION_EXITOSA         = 1;
    public static final int TIPO_RESERVACION_FALLIDA         = 2;
    public static final int TIPO_RESERVACION_ERROR_INTERNO   = 3;
    public static final int TIPO_AGENCIA_EXITOSA             = 4;
    public static final int TIPO_AGENCIA_FALLIDA             = 5;
    public static final int TIPO_AGENCIA_ERROR               = 6;
    public static final int TIPO_AGENCIA_EXPIRADA            = 7;

    public static final int TIPO_PAGO_EXITOSO         = 8;
    public static final int TIPO_PAGO_FALLIDO         = 9;
    public static final int TIPO_PAGO_ERROR_INTERNO   = 10;
    public static final int TIPO_PAGO_AGENCIA_EXITOSO = 11;
    public static final int TIPO_PAGO_AGENCIA_FALLIDO = 12;
    public static final int TIPO_PAGO_AGENCIA_ERROR   = 13;

    public static final int TIPO_CANCELACION_EXITOSA         = 14;
    public static final int TIPO_CANCELACION_FALLIDA         = 15;
    public static final int TIPO_CANCELACION_AGENCIA_EXITOSA = 16;
    public static final int TIPO_CANCELACION_AGENCIA_FALLIDA = 17;

    public static final int TIPO_RESERVACION_EXPIRADA_AUTO = 18;

    /**
     * Registra un evento de reservacion en la tabla LogReservacion.
     * Si el INSERT falla, imprime el error en consola sin interrumpir el flujo principal.
     *
     * @param tipoEventoId  ID del tipo de evento (usar constantes TIPO_* de esta clase).
     * @param reservacionId ID de la reservacion creada, null si la creacion fallo.
     * @param usuarioId     ID del usuario autenticado, null si es reservacion de agencia.
     * @param agenciaId     ID de la agencia, null si es reservacion de usuario comun.
     * @param noReservacion codigo MIKU-XXXXXXXX, null si la creacion fallo.
     * @param total         total calculado de la reservacion, null si fallo antes del calculo.
     * @param exitoso       true si el evento fue exitoso.
     * @param ip            IP del cliente.
     * @param userAgent     User-Agent del cliente.
     * @param mensaje       detalle adicional del evento, puede ser null.
     */
    public void registrar(int tipoEventoId, Integer reservacionId, Integer usuarioId,
                          Integer agenciaId, String noReservacion, Double total,
                          boolean exitoso, String ip, String userAgent, String mensaje) {
        try {
            DatabaseManager.executeUpdate(
                    "INSERT INTO LogReservacion " +
                            "(TipoEventoID, ReservacionID, UsuarioID, AgenciaID, NoReservacion, " +
                            " TotalCalculado, Exitoso, IPOrigen, UserAgent, Mensaje, Fecha) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                    tipoEventoId,
                    reservacionId,
                    usuarioId,
                    agenciaId,
                    noReservacion,
                    total,
                    exitoso ? 1 : 0,
                    ip,
                    userAgent,
                    mensaje
            );
        } catch (Exception e) {
            System.err.println("[LogReservacionRepository] Error al registrar log (tipo="
                    + tipoEventoId + "): " + e.getMessage());
        }
    }
}