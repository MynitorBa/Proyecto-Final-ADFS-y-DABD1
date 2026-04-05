package org.example.dtos;

/**
 * DTO con los datos de confirmacion retornados al cliente tras procesar un pago.
 * Incluye los datos de la factura generada y el total cobrado.
 */
public class PagoResponseDTO {

    private int    facturaId;
    private String noReservacion;
    private String estado;
    private String fecha;
    private String nit;
    private String codigoPostal;
    private double total;

    /**
     * Retorna el identificador unico de la factura generada.
     * @return ID de la factura.
     */
    public int getFacturaId() { return facturaId; }

    /**
     * Asigna el identificador unico de la factura generada.
     * @param facturaId ID de la factura.
     */
    public void setFacturaId(int facturaId) { this.facturaId = facturaId; }

    /**
     * Retorna el numero de reservacion asociado al pago.
     * @return numero de reservacion.
     */
    public String getNoReservacion() { return noReservacion; }

    /**
     * Asigna el numero de reservacion asociado al pago.
     * @param noReservacion numero de reservacion.
     */
    public void setNoReservacion(String noReservacion) { this.noReservacion = noReservacion; }

    /**
     * Retorna el estado actual de la reservacion tras el pago.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Asigna el estado actual de la reservacion tras el pago.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Retorna la fecha en que se registro el pago.
     * @return fecha del pago.
     */
    public String getFecha() { return fecha; }

    /**
     * Asigna la fecha en que se registro el pago.
     * @param fecha fecha del pago.
     */
    public void setFecha(String fecha) { this.fecha = fecha; }

    /**
     * Retorna el NIT del cliente usado en la facturacion.
     * @return NIT del cliente.
     */
    public String getNit() { return nit; }

    /**
     * Asigna el NIT del cliente usado en la facturacion.
     * @param nit NIT del cliente.
     */
    public void setNit(String nit) { this.nit = nit; }

    /**
     * Retorna el codigo postal del cliente usado en la facturacion.
     * @return codigo postal del cliente.
     */
    public String getCodigoPostal() { return codigoPostal; }

    /**
     * Asigna el codigo postal del cliente usado en la facturacion.
     * @param codigoPostal codigo postal del cliente.
     */
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    /**
     * Retorna el monto total cobrado en el pago.
     * @return total del pago.
     */
    public double getTotal() { return total; }

    /**
     * Asigna el monto total cobrado en el pago.
     * @param total total del pago.
     */
    public void setTotal(double total) { this.total = total; }
}