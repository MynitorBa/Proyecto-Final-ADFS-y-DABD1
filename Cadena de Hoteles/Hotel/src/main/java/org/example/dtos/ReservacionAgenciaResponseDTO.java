package org.example.dtos;

import java.util.List;

public class ReservacionAgenciaResponseDTO {
    private int id;
    private String noReservacion;
    private double total;
    private String fechaCreacion;
    private String fechaExpiracion;
    private String estado;
    private List<HabitacionAgenciaResponseDTO> habitaciones;

    public int getId()                                          { return id; }
    public void setId(int v)                                   { this.id = v; }
    public String getNoReservacion()                           { return noReservacion; }
    public void setNoReservacion(String v)                     { this.noReservacion = v; }
    public double getTotal()                                    { return total; }
    public void setTotal(double v)                             { this.total = v; }
    public String getFechaCreacion()                           { return fechaCreacion; }
    public void setFechaCreacion(String v)                     { this.fechaCreacion = v; }
    public String getFechaExpiracion()                         { return fechaExpiracion; }
    public void setFechaExpiracion(String v)                   { this.fechaExpiracion = v; }
    public String getEstado()                                   { return estado; }
    public void setEstado(String v)                            { this.estado = v; }
    public List<HabitacionAgenciaResponseDTO> getHabitaciones(){ return habitaciones; }
    public void setHabitaciones(List<HabitacionAgenciaResponseDTO> v){ this.habitaciones = v; }
}