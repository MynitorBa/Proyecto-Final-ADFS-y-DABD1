package org.example.dtos;

public class PagoRequestDTO {
    // Datos de factura
    private String nit;
    private String codigoPostal;

    // Datos de tarjeta (solo se validan, nunca se guardan)
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaVencimiento; // "MM/YY"
    private String cvv;

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}