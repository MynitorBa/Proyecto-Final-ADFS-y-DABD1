package org.example.dtos;

/**
 * DTO que extiende HabitacionDTO con los precios ajustados por el descuento
 * negociado para la agencia solicitante.
 */
public class HabitacionAgenciaDTO extends HabitacionDTO {

    private double porcentajeDescuento;
    private double precioPorNocheConDescuento;
    private double precioPorPersonaConDescuento;

    /**
     * Retorna el porcentaje de descuento aplicado a los precios de la habitacion.
     * @return porcentaje de descuento.
     */
    public double getPorcentajeDescuento() { return porcentajeDescuento; }

    /**
     * Asigna el porcentaje de descuento aplicado a los precios de la habitacion.
     * @param porcentajeDescuento porcentaje de descuento.
     */
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    /**
     * Retorna el precio por noche luego de aplicar el descuento de la agencia.
     * @return precio por noche con descuento.
     */
    public double getPrecioPorNocheConDescuento() { return precioPorNocheConDescuento; }

    /**
     * Asigna el precio por noche luego de aplicar el descuento de la agencia.
     * @param precioPorNocheConDescuento precio por noche con descuento.
     */
    public void setPrecioPorNocheConDescuento(double precioPorNocheConDescuento) { this.precioPorNocheConDescuento = precioPorNocheConDescuento; }

    /**
     * Retorna el precio por persona luego de aplicar el descuento de la agencia.
     * @return precio por persona con descuento.
     */
    public double getPrecioPorPersonaConDescuento() { return precioPorPersonaConDescuento; }

    /**
     * Asigna el precio por persona luego de aplicar el descuento de la agencia.
     * @param precioPorPersonaConDescuento precio por persona con descuento.
     */
    public void setPrecioPorPersonaConDescuento(double precioPorPersonaConDescuento) { this.precioPorPersonaConDescuento = precioPorPersonaConDescuento; }
}