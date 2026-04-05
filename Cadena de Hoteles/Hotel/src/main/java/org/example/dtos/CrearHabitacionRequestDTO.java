package org.example.dtos;

/**
 * DTO con los datos necesarios para crear una nueva habitacion en un hotel.
 */
public class CrearHabitacionRequestDTO {

    private int    hotelId;
    private int    tipoHabitacionId;
    private String descripcion;
    private int    estadoId;

    /**
     * Retorna el ID del hotel al que pertenece la habitacion.
     * @return ID del hotel.
     */
    public int getHotelId() { return hotelId; }

    /**
     * Asigna el ID del hotel al que pertenece la habitacion.
     * @param hotelId ID del hotel.
     */
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    /**
     * Retorna el ID del tipo de habitacion segun el catalogo del sistema.
     * @return ID del tipo de habitacion.
     */
    public int getTipoHabitacionId() { return tipoHabitacionId; }

    /**
     * Asigna el ID del tipo de habitacion segun el catalogo del sistema.
     * @param tipoHabitacionId ID del tipo de habitacion.
     */
    public void setTipoHabitacionId(int tipoHabitacionId) { this.tipoHabitacionId = tipoHabitacionId; }

    /**
     * Retorna la descripcion detallada de la habitacion.
     * @return descripcion de la habitacion.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Asigna la descripcion detallada de la habitacion.
     * @param descripcion descripcion de la habitacion.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Retorna el ID del estado inicial de la habitacion.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Asigna el ID del estado inicial de la habitacion.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }
}