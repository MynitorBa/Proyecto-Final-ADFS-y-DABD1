package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos de una habitacion para mostrar en resultados de busqueda y detalle de hotel.
 * Los IDs de imagenes se usan para consultarlas via GET /imagenes/habitacion/{id}.
 */
public class HabitacionDTO {

    private int           id;
    private String        tipoHabitacion;
    private double        precioPorPersona;
    private double        precioPorNoche;
    private int           capacidadMaxima;
    private String        tipoCama;
    private double        metrosCuadrados;
    private String        descripcion;
    private String        estado;
    private List<Integer> imagenesIds;

    /**
     * Retorna el identificador unico de la habitacion.
     * @return ID de la habitacion.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico de la habitacion.
     * @param id ID de la habitacion.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el nombre del tipo de habitacion.
     * @return tipo de habitacion.
     */
    public String getTipoHabitacion() { return tipoHabitacion; }

    /**
     * Asigna el nombre del tipo de habitacion.
     * @param tipoHabitacion tipo de habitacion.
     */
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    /**
     * Retorna el precio adicional por persona extra sobre la capacidad base.
     * @return precio por persona adicional.
     */
    public double getPrecioPorPersona() { return precioPorPersona; }

    /**
     * Asigna el precio adicional por persona extra sobre la capacidad base.
     * @param precioPorPersona precio por persona adicional.
     */
    public void setPrecioPorPersona(double precioPorPersona) { this.precioPorPersona = precioPorPersona; }

    /**
     * Retorna el precio base por noche de la habitacion.
     * @return precio por noche.
     */
    public double getPrecioPorNoche() { return precioPorNoche; }

    /**
     * Asigna el precio base por noche de la habitacion.
     * @param precioPorNoche precio por noche.
     */
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    /**
     * Retorna la capacidad maxima de personas admitidas en la habitacion.
     * @return capacidad maxima.
     */
    public int getCapacidadMaxima() { return capacidadMaxima; }

    /**
     * Asigna la capacidad maxima de personas admitidas en la habitacion.
     * @param capacidadMaxima capacidad maxima.
     */
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    /**
     * Retorna el tipo de cama disponible en la habitacion.
     * @return tipo de cama.
     */
    public String getTipoCama() { return tipoCama; }

    /**
     * Asigna el tipo de cama disponible en la habitacion.
     * @param tipoCama tipo de cama.
     */
    public void setTipoCama(String tipoCama) { this.tipoCama = tipoCama; }

    /**
     * Retorna la superficie de la habitacion en metros cuadrados.
     * @return metros cuadrados de la habitacion.
     */
    public double getMetrosCuadrados() { return metrosCuadrados; }

    /**
     * Asigna la superficie de la habitacion en metros cuadrados.
     * @param metrosCuadrados metros cuadrados de la habitacion.
     */
    public void setMetrosCuadrados(double metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    /**
     * Retorna la descripcion detallada de la habitacion.
     * @return descripcion de la habitacion.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Asigna la descripcion detallada de la habitacion.
     * @param descripcion descripcion de la habitacion.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Retorna el nombre del estado actual de la habitacion.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Asigna el nombre del estado actual de la habitacion.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Retorna los IDs de imagenes asociadas a la habitacion.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> getImagenesIds() { return imagenesIds; }

    /**
     * Asigna los IDs de imagenes asociadas a la habitacion.
     * @param imagenesIds lista de IDs de imagenes.
     */
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }
}