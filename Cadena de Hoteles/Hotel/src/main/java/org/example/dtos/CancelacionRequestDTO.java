package org.example.dtos;

/**
 * DTO con el motivo de cancelacion de una reservacion.
 */
public class CancelacionRequestDTO {

    private String motivoCancelacion;

    /**
     * Retorna el motivo por el que se cancela la reservacion.
     * @return motivo de cancelacion.
     */
    public String getMotivoCancelacion() { return motivoCancelacion; }

    /**
     * Asigna el motivo por el que se cancela la reservacion.
     * @param motivoCancelacion motivo de cancelacion.
     */
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
}