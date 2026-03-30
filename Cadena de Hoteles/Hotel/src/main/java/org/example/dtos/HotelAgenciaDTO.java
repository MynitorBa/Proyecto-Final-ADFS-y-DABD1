package org.example.dtos;

public class HotelAgenciaDTO {
    private int    id;
    private String nombre;
    private String ciudad;
    private String pais;

    // Getters y Setters
    public int    getId()     { return id; }
    public void   setId(int id) { this.id = id; }

    public String getNombre()        { return nombre; }
    public void   setNombre(String n){ this.nombre = n; }

    public String getCiudad()        { return ciudad; }
    public void   setCiudad(String c){ this.ciudad = c; }

    public String getPais()          { return pais; }
    public void   setPais(String p)  { this.pais = p; }
}