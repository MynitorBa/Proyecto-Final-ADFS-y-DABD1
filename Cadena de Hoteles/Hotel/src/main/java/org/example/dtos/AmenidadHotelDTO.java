package org.example.dtos;

import java.util.List;

/**
 * DTO que representa una amenidad asignada a un hotel especifico.
 * Incluye los IDs de sus imagenes para consultarlas via GET /imagenes/amenidad/{id}.
 */
public class AmenidadHotelDTO {

    private int           hotelAmenidadId;
    private int           amenidadId;
    private String        nombre;
    private String        descripcion;
    private List<Integer> imagenesIds;

    /**
     * Retorna el ID de la relacion entre el hotel y la amenidad.
     * @return ID de la relacion hotel-amenidad.
     */
    public int getHotelAmenidadId() { return hotelAmenidadId; }

    /**
     * Asigna el ID de la relacion entre el hotel y la amenidad.
     * @param hotelAmenidadId ID de la relacion hotel-amenidad.
     */
    public void setHotelAmenidadId(int hotelAmenidadId) { this.hotelAmenidadId = hotelAmenidadId; }

    /**
     * Retorna el ID de la amenidad en el catalogo del sistema.
     * @return ID de la amenidad.
     */
    public int getAmenidadId() { return amenidadId; }

    /**
     * Asigna el ID de la amenidad en el catalogo del sistema.
     * @param amenidadId ID de la amenidad.
     */
    public void setAmenidadId(int amenidadId) { this.amenidadId = amenidadId; }

    /**
     * Retorna el nombre de la amenidad.
     * @return nombre de la amenidad.
     */
    public String getNombre() { return nombre; }

    /**
     * Asigna el nombre de la amenidad.
     * @param nombre nombre de la amenidad.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Retorna la descripcion personalizada de la amenidad para este hotel.
     * @return descripcion de la amenidad.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Asigna la descripcion personalizada de la amenidad para este hotel.
     * @param descripcion descripcion de la amenidad.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Retorna los IDs de imagenes asociadas a esta amenidad.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> getImagenesIds() { return imagenesIds; }

    /**
     * Asigna los IDs de imagenes asociadas a esta amenidad.
     * @param imagenesIds lista de IDs de imagenes.
     */
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }
}