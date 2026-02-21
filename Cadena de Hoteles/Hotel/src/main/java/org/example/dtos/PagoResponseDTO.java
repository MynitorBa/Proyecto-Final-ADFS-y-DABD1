package org.example.dtos;

public class PagoResponseDTO {
    private int    facturaId;
    private String noReservacion;
    private String estado;
    private String fecha;
    private String nit;
    private String codigoPostal;
    private double total;

    public int getFacturaId() { return facturaId; }
    public void setFacturaId(int facturaId) { this.facturaId = facturaId; }

    public String getNoReservacion() { return noReservacion; }
    public void setNoReservacion(String noReservacion) { this.noReservacion = noReservacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}