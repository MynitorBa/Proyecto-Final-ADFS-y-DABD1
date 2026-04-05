package org.example.dtos;

/**
 * DTO con los datos necesarios para agregar o actualizar una amenidad en un hotel.
 */
public class AgregarAmenidadRequestDTO {

    private int    amenidadId;
    private String descripcion;

    /**
     * Retorna el ID de la amenidad del catalogo a asociar al hotel.
     * @return ID de la amenidad.
     */
    public int getAmenidadId() { return amenidadId; }

    /**
     * Retorna la descripcion personalizada de la amenidad para ese hotel.
     * @return descripcion de la amenidad.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Asigna el ID de la amenidad del catalogo a asociar al hotel.
     * @param amenidadId ID de la amenidad.
     */
    public void setAmenidadId(int amenidadId) { this.amenidadId = amenidadId; }

    /**
     * Asigna la descripcion personalizada de la amenidad para ese hotel.
     * @param descripcion descripcion de la amenidad.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}