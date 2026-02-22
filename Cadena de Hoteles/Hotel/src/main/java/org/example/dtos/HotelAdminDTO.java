package org.example.dtos;

import java.util.List;

public class HotelAdminDTO {

    private int          id;
    private String       nombre;
    private String       direccion;
    private String       descripcion;
    private double       rating;
    private int          estadoId;
    private String       estado;
    private String       ciudad;
    private String       pais;
    private int          cantidadHabitaciones;
    private List<Integer> imagenesIds;

    // ── Getters ──────────────────────────────────────────────────────────────

    public int           getId()                   { return id; }
    public String        getNombre()               { return nombre; }
    public String        getDireccion()            { return direccion; }
    public String        getDescripcion()          { return descripcion; }
    public double        getRating()               { return rating; }
    public int           getEstadoId()             { return estadoId; }
    public String        getEstado()               { return estado; }
    public String        getCiudad()               { return ciudad; }
    public String        getPais()                 { return pais; }
    public int           getCantidadHabitaciones() { return cantidadHabitaciones; }
    public List<Integer> getImagenesIds()          { return imagenesIds; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                                   { this.id = id; }
    public void setNombre(String nombre)                        { this.nombre = nombre; }
    public void setDireccion(String direccion)                  { this.direccion = direccion; }
    public void setDescripcion(String descripcion)              { this.descripcion = descripcion; }
    public void setRating(double rating)                        { this.rating = rating; }
    public void setEstadoId(int estadoId)                       { this.estadoId = estadoId; }
    public void setEstado(String estado)                        { this.estado = estado; }
    public void setCiudad(String ciudad)                        { this.ciudad = ciudad; }
    public void setPais(String pais)                            { this.pais = pais; }
    public void setCantidadHabitaciones(int c)                  { this.cantidadHabitaciones = c; }
    public void setImagenesIds(List<Integer> imagenesIds)       { this.imagenesIds = imagenesIds; }
}