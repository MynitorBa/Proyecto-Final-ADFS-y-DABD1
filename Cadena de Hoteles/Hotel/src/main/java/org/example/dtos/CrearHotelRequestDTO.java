package org.example.dtos;

public class CrearHotelRequestDTO {

    private String nombre;
    private String direccion;
    private String descripcion;
    private double rating;
    private int    estadoId;
    private String ciudad;   // nombre de la ciudad
    private int    paisId;   // ID del país (para buscarOCrear ciudad)

    public String getNombre()      { return nombre; }
    public String getDireccion()   { return direccion; }
    public String getDescripcion() { return descripcion; }
    public double getRating()      { return rating; }
    public int    getEstadoId()    { return estadoId; }
    public String getCiudad()      { return ciudad; }
    public int    getPaisId()      { return paisId; }

    public void setNombre(String nombre)           { this.nombre = nombre; }
    public void setDireccion(String direccion)     { this.direccion = direccion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setRating(double rating)           { this.rating = rating; }
    public void setEstadoId(int estadoId)          { this.estadoId = estadoId; }
    public void setCiudad(String ciudad)           { this.ciudad = ciudad; }
    public void setPaisId(int paisId)              { this.paisId = paisId; }
}