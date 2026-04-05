package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos completos de una habitacion para el panel de administracion.
 * Incluye campos propios de la habitacion y campos resueltos mediante join con TipoHabitacion.
 */
public class HabitacionAdminDTO {

    private int           id;
    private int           hotelId;
    private int           tipoHabitacionId;
    private String        tipoHabitacion;
    private String        numeroHabitacion;
    private String        tipoCama;
    private double        precioPorPersona;
    private double        precioPorNoche;
    private int           capacidadMaxima;
    private double        metrosCuadrados;
    private String        descripcion;
    private int           estadoId;
    private String        estado;
    private List<Integer> imagenesIds;

    /**
     * Retorna el identificador unico de la habitacion.
     * @return ID de la habitacion.
     */
    public int getId() { return id; }

    /**
     * Retorna el ID del hotel al que pertenece la habitacion.
     * @return ID del hotel.
     */
    public int getHotelId() { return hotelId; }

    /**
     * Retorna el ID del tipo de habitacion en el catalogo.
     * @return ID del tipo de habitacion.
     */
    public int getTipoHabitacionId() { return tipoHabitacionId; }

    /**
     * Retorna el nombre del tipo de habitacion resuelto desde TipoHabitacion.
     * @return nombre del tipo de habitacion.
     */
    public String getTipoHabitacion() { return tipoHabitacion; }

    /**
     * Retorna el numero o identificador de la habitacion dentro del hotel.
     * @return numero de la habitacion.
     */
    public String getNumeroHabitacion() { return numeroHabitacion; }

    /**
     * Retorna el tipo de cama de la habitacion resuelto desde TipoHabitacion.
     * @return tipo de cama.
     */
    public String getTipoCama() { return tipoCama; }

    /**
     * Retorna el precio adicional por persona extra sobre la capacidad base.
     * @return precio por persona adicional.
     */
    public double getPrecioPorPersona() { return precioPorPersona; }

    /**
     * Retorna el precio base por noche de la habitacion.
     * @return precio por noche.
     */
    public double getPrecioPorNoche() { return precioPorNoche; }

    /**
     * Retorna la capacidad maxima de personas admitidas en la habitacion.
     * @return capacidad maxima.
     */
    public int getCapacidadMaxima() { return capacidadMaxima; }

    /**
     * Retorna la superficie de la habitacion en metros cuadrados.
     * @return metros cuadrados de la habitacion.
     */
    public double getMetrosCuadrados() { return metrosCuadrados; }

    /**
     * Retorna la descripcion detallada de la habitacion.
     * @return descripcion de la habitacion.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Retorna el ID del estado actual de la habitacion.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Retorna el nombre del estado actual de la habitacion.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Retorna los IDs de imagenes asociadas a la habitacion.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> getImagenesIds() { return imagenesIds; }

    /**
     * Asigna el identificador unico de la habitacion.
     * @param id ID de la habitacion.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el ID del hotel al que pertenece la habitacion.
     * @param hotelId ID del hotel.
     */
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    /**
     * Asigna el ID del tipo de habitacion en el catalogo.
     * @param tipoHabitacionId ID del tipo de habitacion.
     */
    public void setTipoHabitacionId(int tipoHabitacionId) { this.tipoHabitacionId = tipoHabitacionId; }

    /**
     * Asigna el nombre del tipo de habitacion resuelto desde TipoHabitacion.
     * @param tipoHabitacion nombre del tipo de habitacion.
     */
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    /**
     * Asigna el numero o identificador de la habitacion dentro del hotel.
     * @param numeroHabitacion numero de la habitacion.
     */
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }

    /**
     * Asigna el tipo de cama de la habitacion resuelto desde TipoHabitacion.
     * @param tipoCama tipo de cama.
     */
    public void setTipoCama(String tipoCama) { this.tipoCama = tipoCama; }

    /**
     * Asigna el precio adicional por persona extra sobre la capacidad base.
     * @param precioPorPersona precio por persona adicional.
     */
    public void setPrecioPorPersona(double precioPorPersona) { this.precioPorPersona = precioPorPersona; }

    /**
     * Asigna el precio base por noche de la habitacion.
     * @param precioPorNoche precio por noche.
     */
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    /**
     * Asigna la capacidad maxima de personas admitidas en la habitacion.
     * @param capacidadMaxima capacidad maxima.
     */
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    /**
     * Asigna la superficie de la habitacion en metros cuadrados.
     * @param metrosCuadrados metros cuadrados de la habitacion.
     */
    public void setMetrosCuadrados(double metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    /**
     * Asigna la descripcion detallada de la habitacion.
     * @param descripcion descripcion de la habitacion.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Asigna el ID del estado actual de la habitacion.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }

    /**
     * Asigna el nombre del estado actual de la habitacion.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Asigna los IDs de imagenes asociadas a la habitacion.
     * @param imagenesIds lista de IDs de imagenes.
     */
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }
}