package org.example.dtos;

/**
 * DTO con los datos necesarios para procesar el pago de una reservacion.
 * Incluye datos de facturacion y datos de tarjeta. Los datos de tarjeta
 * solo se validan durante el proceso y nunca se almacenan en el sistema.
 */
public class PagoRequestDTO {

    // Datos de factura
    private String nit;
    private String codigoPostal;

    // Datos de tarjeta (solo se validan, nunca se guardan)
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaVencimiento; // "MM/YY"
    private String cvv;

    /**
     * Retorna el NIT del cliente para la facturacion.
     * @return NIT del cliente.
     */
    public String getNit() { return nit; }

    /**
     * Asigna el NIT del cliente para la facturacion.
     * @param nit NIT del cliente.
     */
    public void setNit(String nit) { this.nit = nit; }

    /**
     * Retorna el codigo postal del cliente para la facturacion.
     * @return codigo postal del cliente.
     */
    public String getCodigoPostal() { return codigoPostal; }

    /**
     * Asigna el codigo postal del cliente para la facturacion.
     * @param codigoPostal codigo postal del cliente.
     */
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    /**
     * Retorna el numero de tarjeta del cliente.
     * @return numero de tarjeta.
     */
    public String getNumeroTarjeta() { return numeroTarjeta; }

    /**
     * Asigna el numero de tarjeta del cliente.
     * @param numeroTarjeta numero de tarjeta.
     */
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    /**
     * Retorna el nombre del titular de la tarjeta.
     * @return nombre del titular.
     */
    public String getNombreTitular() { return nombreTitular; }

    /**
     * Asigna el nombre del titular de la tarjeta.
     * @param nombreTitular nombre del titular.
     */
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    /**
     * Retorna la fecha de vencimiento de la tarjeta en formato MM/YY.
     * @return fecha de vencimiento.
     */
    public String getFechaVencimiento() { return fechaVencimiento; }

    /**
     * Asigna la fecha de vencimiento de la tarjeta en formato MM/YY.
     * @param fechaVencimiento fecha de vencimiento.
     */
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    /**
     * Retorna el CVV de la tarjeta del cliente.
     * @return CVV de la tarjeta.
     */
    public String getCvv() { return cvv; }

    /**
     * Asigna el CVV de la tarjeta del cliente.
     * @param cvv CVV de la tarjeta.
     */
    public void setCvv(String cvv) { this.cvv = cvv; }
}