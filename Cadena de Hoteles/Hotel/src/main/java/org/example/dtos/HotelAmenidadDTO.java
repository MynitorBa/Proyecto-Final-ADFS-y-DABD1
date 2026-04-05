package org.example.dtos;

import java.util.List;

/**
 * DTO que representa una amenidad asignada a un hotel especifico.
 * Incluye la descripcion personalizada y los IDs de imagenes de la tabla ImagenHotelAmenidad.
 */
public class HotelAmenidadDTO {

    private int           id;
    private int           hotelId;
    private int           amenidadId;
    private String        amenidadNombre;
    private String        descripcion;
    private List<Integer> imagenesIds;

    /**
     * Retorna el ID del registro en la tabla HotelAmenidad.
     * @return ID del registro.
     */
    public int getId() { return id; }

    /**
     * Retorna el ID del hotel al que pertenece la amenidad.
     * @return ID del hotel.
     */
    public int getHotelId() { return hotelId; }

    /**
     * Retorna el ID de la amenidad en el catalogo del sistema.
     * @return ID de la amenidad.
     */
    public int getAmenidadId() { return amenidadId; }

    /**
     * Retorna el nombre de la amenidad resuelto desde el catalogo.
     * @return nombre de la amenidad.
     */
    public String getAmenidadNombre() { return amenidadNombre; }

    /**
     * Retorna la descripcion personalizada de la amenidad para este hotel.
     * @return descripcion de la amenidad.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Retorna los IDs de imagenes asociadas a esta amenidad en el hotel.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> getImagenesIds() { return imagenesIds; }

    /**
     * Asigna el ID del registro en la tabla HotelAmenidad.
     * @param id ID del registro.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el ID del hotel al que pertenece la amenidad.
     * @param hotelId ID del hotel.
     */
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    /**
     * Asigna el ID de la amenidad en el catalogo del sistema.
     * @param amenidadId ID de la amenidad.
     */
    public void setAmenidadId(int amenidadId) { this.amenidadId = amenidadId; }

    /**
     * Asigna el nombre de la amenidad resuelto desde el catalogo.
     * @param amenidadNombre nombre de la amenidad.
     */
    public void setAmenidadNombre(String amenidadNombre) { this.amenidadNombre = amenidadNombre; }

    /**
     * Asigna la descripcion personalizada de la amenidad para este hotel.
     * @param descripcion descripcion de la amenidad.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Asigna los IDs de imagenes asociadas a esta amenidad en el hotel.
     * @param imagenesIds lista de IDs de imagenes.
     */
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }
}