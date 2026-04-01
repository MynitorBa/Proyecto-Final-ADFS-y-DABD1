package org.example.dtos;

public class PagoAgenciaRequestDTO {
    private String nit;
    private String codigoPostal;

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
}