package org.example.dtos;

import java.util.List;

/** Una amenidad asignada a un hotel concreto, con su descripción e imágenes. */
public class HotelAmenidadDTO {
    private int          id;            // ID en HotelAmenidad
    private int          hotelId;
    private int          amenidadId;
    private String       amenidadNombre;
    private String       descripcion;
    private List<Integer> imagenesIds;  // IDs de ImagenHotelAmenidad

    public int           getId()             { return id; }
    public int           getHotelId()        { return hotelId; }
    public int           getAmenidadId()     { return amenidadId; }
    public String        getAmenidadNombre() { return amenidadNombre; }
    public String        getDescripcion()    { return descripcion; }
    public List<Integer> getImagenesIds()    { return imagenesIds; }

    public void setId(int id)                          { this.id = id; }
    public void setHotelId(int hotelId)                { this.hotelId = hotelId; }
    public void setAmenidadId(int amenidadId)          { this.amenidadId = amenidadId; }
    public void setAmenidadNombre(String n)            { this.amenidadNombre = n; }
    public void setDescripcion(String descripcion)     { this.descripcion = descripcion; }
    public void setImagenesIds(List<Integer> ids)      { this.imagenesIds = ids; }
}