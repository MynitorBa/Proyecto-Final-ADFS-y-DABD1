package org.example.dtos;

public class CiudadDTO {
    private int    id;
    private String nombre;
    private int    paisId;
    private String paisNombre;

    public CiudadDTO() {}

    public int    getId()         { return id; }
    public String getNombre()     { return nombre; }
    public int    getPaisId()     { return paisId; }
    public String getPaisNombre() { return paisNombre; }
    public void   setId(int id)               { this.id = id; }
    public void   setNombre(String nombre)    { this.nombre = nombre; }
    public void   setPaisId(int paisId)       { this.paisId = paisId; }
    public void   setPaisNombre(String p)     { this.paisNombre = p; }
}