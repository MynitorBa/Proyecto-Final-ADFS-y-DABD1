package org.example.dtos;

public class EditarHabitacionRequestDTO {

    private int    tipoHabitacionId;
    private String numeroHabitacion;
    private String descripcion;
    private int    estadoId;

    public int    getTipoHabitacionId() { return tipoHabitacionId; }
    public String getNumeroHabitacion() { return numeroHabitacion; }
    public String getDescripcion()      { return descripcion; }
    public int    getEstadoId()         { return estadoId; }

    public void setTipoHabitacionId(int tipoHabitacionId)       { this.tipoHabitacionId = tipoHabitacionId; }
    public void setNumeroHabitacion(String numeroHabitacion)    { this.numeroHabitacion = numeroHabitacion; }
    public void setDescripcion(String descripcion)              { this.descripcion = descripcion; }
    public void setEstadoId(int estadoId)                       { this.estadoId = estadoId; }
}