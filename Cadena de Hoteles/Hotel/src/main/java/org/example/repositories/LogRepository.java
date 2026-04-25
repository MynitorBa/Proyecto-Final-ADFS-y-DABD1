package org.example.repositories;

import org.example.data.DatabaseManager;

/**
 * Repository para el registro de eventos de sesion en la tabla LogSesion.
 * Centraliza todos los INSERT de logs del sistema para mantener
 * un historico de autenticacion y actividad de usuarios.
 */
public class LogRepository {

    // IDs de TipoEventoSesion en la base de datos
    public static final int TIPO_LOGIN_EXITOSO       = 1;
    public static final int TIPO_LOGIN_FALLIDO       = 2;
    public static final int TIPO_LOGIN_ERROR_INTERNO = 3;

    public static final int TIPO_LOGOUT_EXITOSO       = 4;
    public static final int TIPO_LOGOUT_ERROR_INTERNO = 5;

    public static final int TIPO_REGISTRO_EXITOSO          = 6;
    public static final int TIPO_REGISTRO_FALLIDO          = 7;
    public static final int TIPO_REGISTRO_ERROR_INTERNO    = 8;
    public static final int TIPO_CAMBIO_TELEFONO_EXITOSO   = 9;
    public static final int TIPO_CAMBIO_TELEFONO_ERROR     = 10;
    public static final int TIPO_CAMBIO_CONTRASENA_EXITOSO = 11;
    public static final int TIPO_CAMBIO_CONTRASENA_FALLIDO = 12;
    public static final int TIPO_CAMBIO_CONTRASENA_ERROR   = 13;

    /**
     * Registra un evento de sesion en la tabla LogSesion.
     * Si el INSERT falla, imprime el error en consola pero NO lanza excepcion
     * para que el flujo principal del sistema no se vea interrumpido por un log.
     *
     * @param tipoEventoId   ID del tipo de evento (usar constantes TIPO_* de esta clase).
     * @param usuarioId      ID del usuario autenticado, null si el login fallo o es anonimo.
     * @param loginIntentado username o correo que se intento usar en el login, puede ser null.
     * @param exitoso        true si el evento fue exitoso, false en caso contrario.
     * @param ipOrigen       IP del cliente que origino la peticion, puede ser null.
     * @param userAgent      User-Agent del cliente, puede ser null.
     * @param mensaje        mensaje adicional descriptivo del evento, puede ser null.
     */
    public void registrar(int tipoEventoId, Integer usuarioId, String loginIntentado,
                          boolean exitoso, String ipOrigen, String userAgent, String mensaje) {
        try {
            DatabaseManager.executeUpdate(
                    "INSERT INTO LogSesion " +
                            "(TipoEventoID, UsuarioID, LoginIntentado, Exitoso, IPOrigen, UserAgent, Mensaje, Fecha) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                    tipoEventoId,
                    usuarioId,
                    loginIntentado,
                    exitoso ? 1 : 0,
                    ipOrigen,
                    userAgent,
                    mensaje
            );
        } catch (Exception e) {
            System.err.println("[LogRepository] Error al registrar log (tipo=" + tipoEventoId + "): " + e.getMessage());
        }
    }
}