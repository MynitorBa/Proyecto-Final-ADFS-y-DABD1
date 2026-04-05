package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos necesarios para crear una nueva reservacion.
 * Contiene la lista de habitaciones con sus fechas y cantidad de personas.
 */
public class ReservacionRequestDTO {

    private List<HabitacionReservaRequestDTO> habitaciones;

    /**
     * Retorna la lista de habitaciones incluidas en la solicitud de reservacion.
     * @return lista de habitaciones a reservar.
     */
    public List<HabitacionReservaRequestDTO> getHabitaciones() { return habitaciones; }

    /**
     * Asigna la lista de habitaciones incluidas en la solicitud de reservacion.
     * @param habitaciones lista de habitaciones a reservar.
     */
    public void setHabitaciones(List<HabitacionReservaRequestDTO> habitaciones) { this.habitaciones = habitaciones; }
}