package org.example.dtos;

/**
 * DTO con los datos editables de un hotel desde el panel de administracion.
 */
public class EditarHotelRequestDTO {

    private String nombre;
    private String direccion;
    private String descripcion;
    private double rating;
    private int    estadoId;

    /**
     * Retorna el nombre actualizado del hotel.
     * @return nombre del hotel.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna la direccion fisica actualizada del hotel.
     * @return direccion del hotel.
     */
    public String getDireccion() { return direccion; }

    /**
     * Retorna la descripcion actualizada del hotel.
     * @return descripcion del hotel.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Retorna la calificacion actualizada del hotel.
     * @return rating del hotel.
     */
    public double getRating() { return rating; }

    /**
     * Retorna el ID del nuevo estado del hotel.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Asigna el nombre actualizado del hotel.
     * @param nombre nombre del hotel.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna la direccion fisica actualizada del hotel.
     * @param direccion direccion del hotel.
     */
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Asigna la descripcion actualizada del hotel.
     * @param descripcion descripcion del hotel.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Asigna la calificacion actualizada del hotel.
     * @param rating calificacion del hotel.
     */
    public void setRating(double rating) { this.rating = rating; }

    /**
     * Asigna el ID del nuevo estado del hotel.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }
}