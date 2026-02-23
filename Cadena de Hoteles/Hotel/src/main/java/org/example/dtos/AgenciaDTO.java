package org.example.dtos;

public class AgenciaDTO {

    private int    id;
    private String nombre;
    private String correo;
    private int    usuarioWebisId;
    private double porcentajeDescuento;
    private int    estadoId;
    private String estado;   // nombre del estado (join con ESTADO)

    // ── Getters ──────────────────────────────────────────────────────────────

    public int    getId()                  { return id; }
    public String getNombre()              { return nombre; }
    public String getCorreo()              { return correo; }
    public int    getUsuarioWebisId()      { return usuarioWebisId; }
    public double getPorcentajeDescuento() { return porcentajeDescuento; }
    public int    getEstadoId()            { return estadoId; }
    public String getEstado()              { return estado; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                                   { this.id = id; }
    public void setNombre(String nombre)                        { this.nombre = nombre; }
    public void setCorreo(String correo)                        { this.correo = correo; }
    public void setUsuarioWebisId(int usuarioWebisId)           { this.usuarioWebisId = usuarioWebisId; }
    public void setPorcentajeDescuento(double p)                { this.porcentajeDescuento = p; }
    public void setEstadoId(int estadoId)                       { this.estadoId = estadoId; }
    public void setEstado(String estado)                        { this.estado = estado; }
}