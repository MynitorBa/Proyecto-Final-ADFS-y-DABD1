package org.example.dtos;

public class EditarHotelRequestDTO {

    private String nombre;
    private String direccion;
    private String descripcion;
    private double rating;
    private int    estadoId;

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getNombre()      { return nombre; }
    public String getDireccion()   { return direccion; }
    public String getDescripcion() { return descripcion; }
    public double getRating()      { return rating; }
    public int    getEstadoId()    { return estadoId; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setNombre(String nombre)           { this.nombre = nombre; }
    public void setDireccion(String direccion)     { this.direccion = direccion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setRating(double rating)           { this.rating = rating; }
    public void setEstadoId(int estadoId)          { this.estadoId = estadoId; }
}