package org.example.dtos;

public class EditarAgenciaRequestDTO {

    private String nombre;
    private String correo;
    private double porcentajeDescuento;
    private int    estadoId;

    public String getNombre()              { return nombre; }
    public String getCorreo()              { return correo; }
    public double getPorcentajeDescuento() { return porcentajeDescuento; }
    public int    getEstadoId()            { return estadoId; }

    public void setNombre(String nombre)         { this.nombre = nombre; }
    public void setCorreo(String correo)         { this.correo = correo; }
    public void setPorcentajeDescuento(double p) { this.porcentajeDescuento = p; }
    public void setEstadoId(int estadoId)        { this.estadoId = estadoId; }
}






