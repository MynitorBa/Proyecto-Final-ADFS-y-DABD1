package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos de una reservacion realizada por una agencia externa.
 * Incluye el desglose de habitaciones con sus precios calculados.
 */
public class ReservacionAgenciaResponseDTO {

    private int                                id;
    private String                             noReservacion;
    private double                             total;
    private String                             fechaCreacion;
    private String                             fechaExpiracion;
    private String                             estado;
    private List<HabitacionAgenciaResponseDTO> habitaciones;

    /**
     * Retorna el identificador unico de la reservacion.
     * @return ID de la reservacion.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico de la reservacion.
     * @param id ID de la reservacion.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el numero de reservacion generado por el sistema.
     * @return numero de reservacion.
     */
    public String getNoReservacion() { return noReservacion; }

    /**
     * Asigna el numero de reservacion generado por el sistema.
     * @param noReservacion numero de reservacion.
     */
    public void setNoReservacion(String noReservacion) { this.noReservacion = noReservacion; }

    /**
     * Retorna el monto total de la reservacion.
     * @return total de la reservacion.
     */
    public double getTotal() { return total; }

    /**
     * Asigna el monto total de la reservacion.
     * @param total total de la reservacion.
     */
    public void setTotal(double total) { this.total = total; }

    /**
     * Retorna la fecha en que se creo la reservacion.
     * @return fecha de creacion.
     */
    public String getFechaCreacion() { return fechaCreacion; }

    /**
     * Asigna la fecha en que se creo la reservacion.
     * @param fechaCreacion fecha de creacion.
     */
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    /**
     * Retorna la fecha en que expira la reservacion si no es pagada.
     * @return fecha de expiracion.
     */
    public String getFechaExpiracion() { return fechaExpiracion; }

    /**
     * Asigna la fecha en que expira la reservacion si no es pagada.
     * @param fechaExpiracion fecha de expiracion.
     */
    public void setFechaExpiracion(String fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    /**
     * Retorna el nombre del estado actual de la reservacion.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Asigna el nombre del estado actual de la reservacion.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Retorna el desglose de habitaciones incluidas en la reservacion.
     * @return lista de habitaciones con sus precios calculados.
     */
    public List<HabitacionAgenciaResponseDTO> getHabitaciones() { return habitaciones; }

    /**
     * Asigna el desglose de habitaciones incluidas en la reservacion.
     * @param habitaciones lista de habitaciones con sus precios calculados.
     */
    public void setHabitaciones(List<HabitacionAgenciaResponseDTO> habitaciones) { this.habitaciones = habitaciones; }
}