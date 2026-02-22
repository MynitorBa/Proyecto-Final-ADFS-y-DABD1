package org.example.dtos;

public class CrearHotelRequestDTO {

    private String nombre;
    private String direccion;
    private String descripcion;
    private double rating;
    private int    estadoId;
    private String ciudad;      // nombre de la ciudad (se busca o crea)
    private String paisNombre;  // nombre del país   (se busca o crea)

    public String getNombre()      { return nombre; }
    public String getDireccion()   { return direccion; }
    public String getDescripcion() { return descripcion; }
    public double getRating()      { return rating; }
    public int    getEstadoId()    { return estadoId; }
    public String getCiudad()      { return ciudad; }
    public String getPaisNombre()  { return paisNombre; }

    public void setNombre(String nombre)           { this.nombre = nombre; }
    public void setDireccion(String direccion)     { this.direccion = direccion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setRating(double rating)           { this.rating = rating; }
    public void setEstadoId(int estadoId)          { this.estadoId = estadoId; }
    public void setCiudad(String ciudad)           { this.ciudad = ciudad; }
    public void setPaisNombre(String paisNombre)   { this.paisNombre = paisNombre; }
}