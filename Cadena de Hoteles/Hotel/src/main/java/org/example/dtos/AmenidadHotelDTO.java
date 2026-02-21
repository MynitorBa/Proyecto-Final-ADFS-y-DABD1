package org.example.dtos;

import java.util.List;

public class AmenidadHotelDTO {
    private int    hotelAmenidadId;
    private int    amenidadId;
    private String nombre;
    private String descripcion;
    private List<Integer> imagenesIds; // IDs para pedir GET /imagenes/amenidad/{id}

    public int getHotelAmenidadId() { return hotelAmenidadId; }
    public void setHotelAmenidadId(int hotelAmenidadId) { this.hotelAmenidadId = hotelAmenidadId; }

    public int getAmenidadId() { return amenidadId; }
    public void setAmenidadId(int amenidadId) { this.amenidadId = amenidadId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<Integer> getImagenesIds() { return imagenesIds; }
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }
}