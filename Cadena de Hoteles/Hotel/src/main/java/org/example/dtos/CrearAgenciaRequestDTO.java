package org.example.dtos;

public class CrearAgenciaRequestDTO {

    private String nombre;
    private String correo;
    private double porcentajeDescuento;
    // estadoId se fija automáticamente en 1 (Activo) al crear

    public String getNombre()              { return nombre; }
    public String getCorreo()              { return correo; }
    public double getPorcentajeDescuento() { return porcentajeDescuento; }

    public void setNombre(String nombre)   { this.nombre = nombre; }
    public void setCorreo(String correo)   { this.correo = correo; }
    public void setPorcentajeDescuento(double p) { this.porcentajeDescuento = p; }
}