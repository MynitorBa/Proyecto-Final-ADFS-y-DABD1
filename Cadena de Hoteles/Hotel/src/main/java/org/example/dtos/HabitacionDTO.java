package org.example.dtos;

import java.util.List;

public class HabitacionDTO {
    private int    id;
    private String tipoHabitacion;
    private double precioPorPersona;
    private double precioPorNoche;
    private int    capacidadMaxima;
    private String tipoCama;
    private double metrosCuadrados;
    private String descripcion;
    private String estado;
    private List<Integer> imagenesIds; // IDs para pedir GET /imagenes/habitacion/{id}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<Integer> getImagenesIds() { return imagenesIds; }
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }
}