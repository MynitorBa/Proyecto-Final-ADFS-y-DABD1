package org.example.dtos;

/**
 * DTO con los datos editables de una habitacion desde el panel de administracion.
 */
public class EditarHabitacionRequestDTO {

    private int    tipoHabitacionId;
    private String numeroHabitacion;
    private String descripcion;
    private int    estadoId;

    /**
     * Retorna el ID del tipo de habitacion segun el catalogo del sistema.
     * @return ID del tipo de habitacion.
     */
    public int getTipoHabitacionId() { return tipoHabitacionId; }

    /**
     * Retorna el numero o identificador de la habitacion dentro del hotel.
     * @return numero de la habitacion.
     */
    public String getNumeroHabitacion() { return numeroHabitacion; }

    /**
     * Retorna la descripcion actualizada de la habitacion.
     * @return descripcion de la habitacion.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Retorna el ID del nuevo estado de la habitacion.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Asigna el ID del tipo de habitacion segun el catalogo del sistema.
     * @param tipoHabitacionId ID del tipo de habitacion.
     */
    public void setTipoHabitacionId(int tipoHabitacionId) { this.tipoHabitacionId = tipoHabitacionId; }

    /**
     * Asigna el numero o identificador de la habitacion dentro del hotel.
     * @param numeroHabitacion numero de la habitacion.
     */
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }

    /**
     * Asigna la descripcion actualizada de la habitacion.
     * @param descripcion descripcion de la habitacion.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Asigna el ID del nuevo estado de la habitacion.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }
}