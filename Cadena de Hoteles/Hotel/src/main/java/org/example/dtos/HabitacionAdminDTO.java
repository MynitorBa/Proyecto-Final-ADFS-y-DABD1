package org.example.dtos;

import java.util.List;

public class HabitacionAdminDTO {

    private int          id;
    private int          hotelId;
    private int          tipoHabitacionId;
    private String       tipoHabitacion;
    private int          camaId;
    private String       tipoCama;
    private double       precioPorPersona;
    private double       precioPorNoche;
    private int          capacidadMaxima;
    private double       metrosCuadrados;
    private String       descripcion;
    private int          estadoId;
    private String       estado;
    private List<Integer> imagenesIds;

    // ── Getters ──────────────────────────────────────────────────────────────

    public int           getId()               { return id; }
    public int           getHotelId()          { return hotelId; }
    public int           getTipoHabitacionId() { return tipoHabitacionId; }
    public String        getTipoHabitacion()   { return tipoHabitacion; }
    public int           getCamaId()           { return camaId; }
    public String        getTipoCama()         { return tipoCama; }
    public double        getPrecioPorPersona() { return precioPorPersona; }
    public double        getPrecioPorNoche()   { return precioPorNoche; }
    public int           getCapacidadMaxima()  { return capacidadMaxima; }
    public double        getMetrosCuadrados()  { return metrosCuadrados; }
    public String        getDescripcion()      { return descripcion; }
    public int           getEstadoId()         { return estadoId; }
    public String        getEstado()           { return estado; }
    public List<Integer> getImagenesIds()      { return imagenesIds; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                                   { this.id = id; }
    public void setHotelId(int hotelId)                         { this.hotelId = hotelId; }
    public void setTipoHabitacionId(int tipoHabitacionId)       { this.tipoHabitacionId = tipoHabitacionId; }
    public void setTipoHabitacion(String tipoHabitacion)        { this.tipoHabitacion = tipoHabitacion; }
    public void setCamaId(int camaId)                           { this.camaId = camaId; }
    public void setTipoCama(String tipoCama)                    { this.tipoCama = tipoCama; }
    public void setPrecioPorPersona(double precioPorPersona)    { this.precioPorPersona = precioPorPersona; }
    public void setPrecioPorNoche(double precioPorNoche)        { this.precioPorNoche = precioPorNoche; }
    public void setCapacidadMaxima(int capacidadMaxima)         { this.capacidadMaxima = capacidadMaxima; }
    public void setMetrosCuadrados(double metrosCuadrados)      { this.metrosCuadrados = metrosCuadrados; }
    public void setDescripcion(String descripcion)              { this.descripcion = descripcion; }
    public void setEstadoId(int estadoId)                       { this.estadoId = estadoId; }
    public void setEstado(String estado)                        { this.estado = estado; }
    public void setImagenesIds(List<Integer> imagenesIds)       { this.imagenesIds = imagenesIds; }
}