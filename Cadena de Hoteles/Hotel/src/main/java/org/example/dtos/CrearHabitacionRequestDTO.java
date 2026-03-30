package org.example.dtos;

public class CrearHabitacionRequestDTO {
    private int hotelId;
    private int tipoHabitacionId;
    private String descripcion;
    private int estadoId;

    public int getHotelId()             { return hotelId; }
    public void setHotelId(int v)       { this.hotelId = v; }

    public int getTipoHabitacionId()          { return tipoHabitacionId; }
    public void setTipoHabitacionId(int v)    { this.tipoHabitacionId = v; }

    public String getDescripcion()            { return descripcion; }
    public void setDescripcion(String v)      { this.descripcion = v; }

    public int getEstadoId()            { return estadoId; }
    public void setEstadoId(int v)      { this.estadoId = v; }
}