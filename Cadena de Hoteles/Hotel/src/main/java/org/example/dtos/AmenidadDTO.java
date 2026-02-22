package org.example.dtos;

/** Catálogo de amenidades (las 12 del sistema). */
public class AmenidadDTO {
    private int    id;
    private String nombre;

    public AmenidadDTO() {}
    public AmenidadDTO(int id, String nombre) { this.id = id; this.nombre = nombre; }

    public int    getId()     { return id; }
    public String getNombre() { return nombre; }
    public void   setId(int id)            { this.id = id; }
    public void   setNombre(String nombre) { this.nombre = nombre; }
}