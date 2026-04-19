package org.example.dtos;

/**
 * DTO que encapsula el resultado de intentar notificar a una agencia externa
 * cuando el administrador cancela una reservacion que le pertenece.
 *
 * Los campos reflejan exactamente lo que devuelve AgenciaNotificadorExternoService,
 * y son los que el controller serializa como parte de la respuesta al admin.
 */
public class ResultadoNotificacionDTO {

    /** true si la reservacion pertenece a una agencia (usuario webservice). */
    private boolean esReservaDeAgencia;

    /** Nombre comercial de la agencia duena de la reservacion. */
    private String nombreAgencia = "";

    /** true si se llego a enviar la peticion HTTP (independientemente del status). */
    private boolean enviado;

    /** Codigo HTTP devuelto por el sistema externo de la agencia (0 si no se envio). */
    private int httpStatus;

    /** Cuerpo de la respuesta HTTP del sistema externo de la agencia. */
    private String respuestaAgencia = "";

    /**
     * Mensaje de error tecnico si algo fallo antes o durante el envio
     * (p.ej. URL no configurada, token ausente, excepcion de red).
     * null cuando no hubo error.
     */
    private String error;

    // -------------------------------------------------------------------------
    // Getters y setters
    // -------------------------------------------------------------------------

    public boolean isEsReservaDeAgencia() { return esReservaDeAgencia; }
    public void setEsReservaDeAgencia(boolean esReservaDeAgencia) {
        this.esReservaDeAgencia = esReservaDeAgencia;
    }

    public String getNombreAgencia() { return nombreAgencia; }
    public void setNombreAgencia(String nombreAgencia) {
        this.nombreAgencia = nombreAgencia;
    }

    public boolean isEnviado() { return enviado; }
    public void setEnviado(boolean enviado) { this.enviado = enviado; }

    public int getHttpStatus() { return httpStatus; }
    public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }

    public String getRespuestaAgencia() { return respuestaAgencia; }
    public void setRespuestaAgencia(String respuestaAgencia) {
        this.respuestaAgencia = respuestaAgencia;
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}