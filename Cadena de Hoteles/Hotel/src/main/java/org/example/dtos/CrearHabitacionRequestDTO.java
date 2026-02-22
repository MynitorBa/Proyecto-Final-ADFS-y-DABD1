package org.example.dtos;

public class CrearHabitacionRequestDTO {

    private int    hotelId;
    private int    tipoHabitacionId;
    private int    camaId;
    private double precioPorPersona;
    private double precioPorNoche;
    private int    capacidadMaxima;
    private double metrosCuadrados;
    private String descripcion;
    private int    estadoId;

    public int    getHotelId()           { return hotelId; }
    public int    getTipoHabitacionId()  { return tipoHabitacionId; }
    public int    getCamaId()            { return camaId; }
    public double getPrecioPorPersona()  { return precioPorPersona; }
    public double getPrecioPorNoche()    { return precioPorNoche; }
    public int    getCapacidadMaxima()   { return capacidadMaxima; }
    public double getMetrosCuadrados()   { return metrosCuadrados; }
    public String getDescripcion()       { return descripcion; }
    public int    getEstadoId()          { return estadoId; }

    public void setHotelId(int hotelId)                         { this.hotelId = hotelId; }
    public void setTipoHabitacionId(int tipoHabitacionId)       { this.tipoHabitacionId = tipoHabitacionId; }
    public void setCamaId(int camaId)                           { this.camaId = camaId; }
    public void setPrecioPorPersona(double precioPorPersona)    { this.precioPorPersona = precioPorPersona; }
    public void setPrecioPorNoche(double precioPorNoche)        { this.precioPorNoche = precioPorNoche; }
    public void setCapacidadMaxima(int capacidadMaxima)         { this.capacidadMaxima = capacidadMaxima; }
    public void setMetrosCuadrados(double metrosCuadrados)      { this.metrosCuadrados = metrosCuadrados; }
    public void setDescripcion(String descripcion)              { this.descripcion = descripcion; }
    public void setEstadoId(int estadoId)                       { this.estadoId = estadoId; }
}