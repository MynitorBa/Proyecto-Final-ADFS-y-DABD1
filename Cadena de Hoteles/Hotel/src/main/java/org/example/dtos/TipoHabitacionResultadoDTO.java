package org.example.dtos;

import java.util.List;

public class TipoHabitacionResultadoDTO {

    private int tipoHabitacionId;
    private String tipoHabitacion;
    private double precioPorPersona;
    private double precioPorNoche;
    private int capacidadMaxima;
    private String tipoCama;
    private double metrosCuadrados;
    private List<Integer> imagenesIds;

    // Habitaciones físicas disponibles de este tipo
    private List<HabitacionResumenDTO> habitacionesDisponibles;

    // Getters y Setters
    public int getTipoHabitacionId() { return tipoHabitacionId; }
    public void setTipoHabitacionId(int tipoHabitacionId) { this.tipoHabitacionId = tipoHabitacionId; }

    public String getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    public double getPrecioPorPersona() { return precioPorPersona; }
    public void setPrecioPorPersona(double precioPorPersona) { this.precioPorPersona = precioPorPersona; }

    public double getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public String getTipoCama() { return tipoCama; }
    public void setTipoCama(String tipoCama) { this.tipoCama = tipoCama; }

    public double getMetrosCuadrados() { return metrosCuadrados; }
    public void setMetrosCuadrados(double metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    public List<Integer> getImagenesIds() { return imagenesIds; }
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }

    public List<HabitacionResumenDTO> getHabitacionesDisponibles() { return habitacionesDisponibles; }
    public void setHabitacionesDisponibles(List<HabitacionResumenDTO> habitacionesDisponibles) {
        this.habitacionesDisponibles = habitacionesDisponibles;
    }
}