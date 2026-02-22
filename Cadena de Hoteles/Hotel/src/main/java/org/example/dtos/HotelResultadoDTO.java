package org.example.dtos;

import java.util.List;
import java.util.Map;

public class HotelResultadoDTO {
    private int    id;
    private String nombre;
    private String direccion;
    private String ciudad;
    private String pais;
    private String descripcion;
    private double rating;
    private String estado;
    private List<Integer>          imagenesIds;
    private List<AmenidadHotelDTO> amenidades;
    private List<HabitacionDTO>    habitaciones;

    // Combinaciones numéricas: ej. [[1,3],[2,2],[1,1,2]]
    private List<List<Integer>>            combinacionesNumericas;
    // Habitaciones agrupadas por capacidad: {"1": [...], "2": [...], "3": [...]}
    private Map<Integer, List<HabitacionDTO>> habitacionesPorCapacidad;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<Integer> getImagenesIds() { return imagenesIds; }
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }

    public List<AmenidadHotelDTO> getAmenidades() { return amenidades; }
    public void setAmenidades(List<AmenidadHotelDTO> amenidades) { this.amenidades = amenidades; }

    public List<HabitacionDTO> getHabitaciones() { return habitaciones; }
    public void setHabitaciones(List<HabitacionDTO> habitaciones) { this.habitaciones = habitaciones; }

    public List<List<Integer>> getCombinacionesNumericas() { return combinacionesNumericas; }
    public void setCombinacionesNumericas(List<List<Integer>> combinacionesNumericas) { this.combinacionesNumericas = combinacionesNumericas; }

    public Map<Integer, List<HabitacionDTO>> getHabitacionesPorCapacidad() { return habitacionesPorCapacidad; }
    public void setHabitacionesPorCapacidad(Map<Integer, List<HabitacionDTO>> habitacionesPorCapacidad) { this.habitacionesPorCapacidad = habitacionesPorCapacidad; }
}