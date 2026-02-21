package org.example.dtos;

import java.util.List;

public class ReservacionRequestDTO {
    private List<HabitacionReservaRequestDTO> habitaciones;

    public List<HabitacionReservaRequestDTO> getHabitaciones() { return habitaciones; }
    public void setHabitaciones(List<HabitacionReservaRequestDTO> habitaciones) { this.habitaciones = habitaciones; }
}