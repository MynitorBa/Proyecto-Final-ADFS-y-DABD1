package org.example.dtos;

/**
 * DTO simple para transportar datos básicos de una aerolinea (ID y porcentaje de descuento).
 * Se utiliza durante el handshake para retornar el porcentaje de ganancia configurado.
 */
public class AerolineaDTO {

    private int id;
    private double porcentajeDescuento;

    /**
     * Constructor sin argumentos.
     */
    public AerolineaDTO() {
    }

    /**
     * Constructor con parámetros.
     * @param id ID de la aerolinea.
     * @param porcentajeDescuento porcentaje de descuento de la aerolinea.
     */
    public AerolineaDTO(int id, double porcentajeDescuento) {
        this.id = id;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    /**
     * Retorna el ID de la aerolinea.
     * @return ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Asigna el ID de la aerolinea.
     * @param id ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retorna el porcentaje de descuento de la aerolinea.
     * @return porcentaje de descuento.
     */
    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    /**
     * Asigna el porcentaje de descuento de la aerolinea.
     * @param porcentajeDescuento porcentaje de descuento.
     */
    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }
}
