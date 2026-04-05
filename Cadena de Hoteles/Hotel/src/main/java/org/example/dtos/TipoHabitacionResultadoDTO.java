package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos de un tipo de habitacion como resultado de una busqueda.
 * Incluye las habitaciones fisicas disponibles de ese tipo para el rango de fechas solicitado.
 */
public class TipoHabitacionResultadoDTO {

    private int               tipoHabitacionId;
    private String            tipoHabitacion;
    private double            precioPorPersona;
    private double            precioPorNoche;
    private int               capacidadMaxima;
    private String            tipoCama;
    private double            metrosCuadrados;
    private List<Integer>     imagenesIds;

    // Habitaciones fisicas disponibles de este tipo
    private List<HabitacionResumenDTO> habitacionesDisponibles;

    /**
     * Retorna el ID del tipo de habitacion en el catalogo.
     * @return ID del tipo de habitacion.
     */
    public int getTipoHabitacionId() { return tipoHabitacionId; }

    /**
     * Asigna el ID del tipo de habitacion en el catalogo.
     * @param tipoHabitacionId ID del tipo de habitacion.
     */
    public void setTipoHabitacionId(int tipoHabitacionId) { this.tipoHabitacionId = tipoHabitacionId; }

    /**
     * Retorna el nombre del tipo de habitacion.
     * @return nombre del tipo de habitacion.
     */
    public String getTipoHabitacion() { return tipoHabitacion; }

    /**
     * Asigna el nombre del tipo de habitacion.
     * @param tipoHabitacion nombre del tipo de habitacion.
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
     * Retorna el precio base por noche del tipo de habitacion.
     * @return precio por noche.
     */
    public double getPrecioPorNoche() { return precioPorNoche; }

    /**
     * Asigna el precio base por noche del tipo de habitacion.
     * @param precioPorNoche precio por noche.
     */
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    /**
     * Retorna la capacidad maxima de personas admitidas en este tipo de habitacion.
     * @return capacidad maxima.
     */
    public int getCapacidadMaxima() { return capacidadMaxima; }

    /**
     * Asigna la capacidad maxima de personas admitidas en este tipo de habitacion.
     * @param capacidadMaxima capacidad maxima.
     */
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    /**
     * Retorna el tipo de cama disponible en este tipo de habitacion.
     * @return tipo de cama.
     */
    public String getTipoCama() { return tipoCama; }

    /**
     * Asigna el tipo de cama disponible en este tipo de habitacion.
     * @param tipoCama tipo de cama.
     */
    public void setTipoCama(String tipoCama) { this.tipoCama = tipoCama; }

    /**
     * Retorna la superficie en metros cuadrados de este tipo de habitacion.
     * @return metros cuadrados.
     */
    public double getMetrosCuadrados() { return metrosCuadrados; }

    /**
     * Asigna la superficie en metros cuadrados de este tipo de habitacion.
     * @param metrosCuadrados metros cuadrados.
     */
    public void setMetrosCuadrados(double metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    /**
     * Retorna los IDs de imagenes asociadas a este tipo de habitacion.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> getImagenesIds() { return imagenesIds; }

    /**
     * Asigna los IDs de imagenes asociadas a este tipo de habitacion.
     * @param imagenesIds lista de IDs de imagenes.
     */
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }

    /**
     * Retorna las habitaciones fisicas disponibles de este tipo para el rango de fechas solicitado.
     * @return lista de habitaciones disponibles.
     */
    public List<HabitacionResumenDTO> getHabitacionesDisponibles() { return habitacionesDisponibles; }

    /**
     * Asigna las habitaciones fisicas disponibles de este tipo para el rango de fechas solicitado.
     * @param habitacionesDisponibles lista de habitaciones disponibles.
     */
    public void setHabitacionesDisponibles(List<HabitacionResumenDTO> habitacionesDisponibles) {
        this.habitacionesDisponibles = habitacionesDisponibles;
    }
}