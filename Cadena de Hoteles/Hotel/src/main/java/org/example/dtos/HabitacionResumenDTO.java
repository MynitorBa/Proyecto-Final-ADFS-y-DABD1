package org.example.dtos;

/**
 * DTO con los datos minimos de una habitacion para usarse en listados y resumenes.
 */
public class HabitacionResumenDTO {

    private int    id;
    private String numeroHabitacion;

    /**
     * Retorna el identificador unico de la habitacion.
     * @return ID de la habitacion.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico de la habitacion.
     * @param id ID de la habitacion.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el numero o identificador de la habitacion dentro del hotel.
     * @return numero de la habitacion.
     */
    public String getNumeroHabitacion() { return numeroHabitacion; }

    /**
     * Asigna el numero o identificador de la habitacion dentro del hotel.
     * @param numeroHabitacion numero de la habitacion.
     */
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }
}