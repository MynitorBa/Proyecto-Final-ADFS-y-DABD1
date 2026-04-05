package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos completos de un hotel para el panel de administracion.
 * Incluye informacion de ubicacion, estado, cantidad de habitaciones e imagenes asociadas.
 */
public class HotelAdminDTO {

    private int           id;
    private String        nombre;
    private String        direccion;
    private String        descripcion;
    private double        rating;
    private int           estadoId;
    private String        estado;
    private String        ciudad;
    private String        pais;
    private int           cantidadHabitaciones;
    private List<Integer> imagenesIds;

    /**
     * Retorna el identificador unico del hotel.
     * @return ID del hotel.
     */
    public int getId() { return id; }

    /**
     * Retorna el nombre del hotel.
     * @return nombre del hotel.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna la direccion fisica del hotel.
     * @return direccion del hotel.
     */
    public String getDireccion() { return direccion; }

    /**
     * Retorna la descripcion general del hotel.
     * @return descripcion del hotel.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Retorna la calificacion promedio del hotel.
     * @return rating del hotel.
     */
    public double getRating() { return rating; }

    /**
     * Retorna el ID del estado actual del hotel.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Retorna el nombre del estado actual del hotel.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Retorna la ciudad donde se ubica el hotel.
     * @return nombre de la ciudad.
     */
    public String getCiudad() { return ciudad; }

    /**
     * Retorna el pais donde se ubica el hotel.
     * @return nombre del pais.
     */
    public String getPais() { return pais; }

    /**
     * Retorna el total de habitaciones registradas en el hotel.
     * @return cantidad de habitaciones.
     */
    public int getCantidadHabitaciones() { return cantidadHabitaciones; }

    /**
     * Retorna los IDs de imagenes asociadas al hotel.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> getImagenesIds() { return imagenesIds; }

    /**
     * Asigna el identificador unico del hotel.
     * @param id ID del hotel.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el nombre del hotel.
     * @param nombre nombre del hotel.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna la direccion fisica del hotel.
     * @param direccion direccion del hotel.
     */
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Asigna la descripcion general del hotel.
     * @param descripcion descripcion del hotel.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Asigna la calificacion promedio del hotel.
     * @param rating calificacion del hotel.
     */
    public void setRating(double rating) { this.rating = rating; }

    /**
     * Asigna el ID del estado actual del hotel.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }

    /**
     * Asigna el nombre del estado actual del hotel.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Asigna la ciudad donde se ubica el hotel.
     * @param ciudad nombre de la ciudad.
     */
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    /**
     * Asigna el pais donde se ubica el hotel.
     * @param pais nombre del pais.
     */
    public void setPais(String pais) { this.pais = pais; }

    /**
     * Asigna el total de habitaciones registradas en el hotel.
     * @param cantidadHabitaciones total de habitaciones.
     */
    public void setCantidadHabitaciones(int cantidadHabitaciones) { this.cantidadHabitaciones = cantidadHabitaciones; }

    /**
     * Asigna los IDs de imagenes asociadas al hotel.
     * @param imagenesIds lista de IDs de imagenes.
     */
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }
}