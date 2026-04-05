package org.example.dtos;

/**
 * DTO con los datos de facturacion necesarios para procesar el pago de una agencia.
 */
public class PagoAgenciaRequestDTO {

    private String nit;
    private String codigoPostal;

    /**
     * Retorna el NIT de la agencia para la facturacion.
     * @return NIT de la agencia.
     */
    public String getNit() { return nit; }

    /**
     * Asigna el NIT de la agencia para la facturacion.
     * @param nit NIT de la agencia.
     */
    public void setNit(String nit) { this.nit = nit; }

    /**
     * Retorna el codigo postal de la agencia para la facturacion.
     * @return codigo postal de la agencia.
     */
    public String getCodigoPostal() { return codigoPostal; }

    /**
     * Asigna el codigo postal de la agencia para la facturacion.
     * @param codigoPostal codigo postal de la agencia.
     */
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
}