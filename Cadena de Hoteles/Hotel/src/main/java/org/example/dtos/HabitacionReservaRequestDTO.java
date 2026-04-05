package org.example.dtos;

/**
 * DTO con los datos de una habitacion dentro de una solicitud de reservacion.
 * Las fechas deben enviarse en formato YYYY-MM-DD.
 */
public class HabitacionReservaRequestDTO {

    private int    habitacionId;
    private int    cantidadPersonas;
    private String fechaCheckIn;
    private String fechaCheckOut;

    /**
     * Retorna el ID de la habitacion a reservar.
     * @return ID de la habitacion.
     */
    public int getHabitacionId() { return habitacionId; }

    /**
     * Asigna el ID de la habitacion a reservar.
     * @param habitacionId ID de la habitacion.
     */
    public void setHabitacionId(int habitacionId) { this.habitacionId = habitacionId; }

    /**
     * Retorna el numero de personas que ocuparan la habitacion.
     * @return cantidad de personas.
     */
    public int getCantidadPersonas() { return cantidadPersonas; }

    /**
     * Asigna el numero de personas que ocuparan la habitacion.
     * @param cantidadPersonas cantidad de personas.
     */
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }

    /**
     * Retorna la fecha de entrada en formato YYYY-MM-DD.
     * @return fecha de check-in.
     */
    public String getFechaCheckIn() { return fechaCheckIn; }

    /**
     * Asigna la fecha de entrada en formato YYYY-MM-DD.
     * @param fechaCheckIn fecha de check-in.
     */
    public void setFechaCheckIn(String fechaCheckIn) { this.fechaCheckIn = fechaCheckIn; }

    /**
     * Retorna la fecha de salida en formato YYYY-MM-DD.
     * @return fecha de check-out.
     */
    public String getFechaCheckOut() { return fechaCheckOut; }

    /**
     * Asigna la fecha de salida en formato YYYY-MM-DD.
     * @param fechaCheckOut fecha de check-out.
     */
    public void setFechaCheckOut(String fechaCheckOut) { this.fechaCheckOut = fechaCheckOut; }
}