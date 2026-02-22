package org.example.dtos;

public class AgregarAmenidadRequestDTO {
    private int    amenidadId;
    private String descripcion;

    public int    getAmenidadId()  { return amenidadId; }
    public String getDescripcion() { return descripcion; }
    public void   setAmenidadId(int amenidadId)        { this.amenidadId = amenidadId; }
    public void   setDescripcion(String descripcion)   { this.descripcion = descripcion; }
}