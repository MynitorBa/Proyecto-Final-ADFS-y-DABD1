package org.example.dtos;

import java.util.List;
import java.util.Map;

/**
 * DTO con los datos completos de un hotel como resultado de una busqueda.
 * Incluye amenidades, tipos de habitacion disponibles, combinaciones numericas
 * y tipos de habitacion agrupados por capacidad para facilitar la seleccion al usuario.
 */
public class HotelResultadoDTO {

    private int                                            id;
    private String                                         nombre;
    private String                                         direccion;
    private String                                         ciudad;
    private String                                         pais;
    private String                                         descripcion;
    private double                                         rating;
    private String                                         estado;
    private List<Integer>                                  imagenesIds;
    private List<AmenidadHotelDTO>                         amenidades;
    private List<TipoHabitacionResultadoDTO>               tiposHabitacion;
    private List<List<Integer>>                            combinacionesNumericas;
    private Map<Integer, List<TipoHabitacionResultadoDTO>> tiposHabitacionPorCapacidad;

    /**
     * Retorna el identificador unico del hotel.
     * @return ID del hotel.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico del hotel.
     * @param id ID del hotel.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el nombre del hotel.
     * @return nombre del hotel.
     */
    public String getNombre() { return nombre; }

    /**
     * Asigna el nombre del hotel.
     * @param nombre nombre del hotel.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Retorna la direccion fisica del hotel.
     * @return direccion del hotel.
     */
    public String getDireccion() { return direccion; }

    /**
     * Asigna la direccion fisica del hotel.
     * @param direccion direccion del hotel.
     */
    public void setDireccion(String direccion) { this.direccion = direccion; }

    /**
     * Retorna la ciudad donde se ubica el hotel.
     * @return nombre de la ciudad.
     */
    public String getCiudad() { return ciudad; }

    /**
     * Asigna la ciudad donde se ubica el hotel.
     * @param ciudad nombre de la ciudad.
     */
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    /**
     * Retorna el pais donde se ubica el hotel.
     * @return nombre del pais.
     */
    public String getPais() { return pais; }

    /**
     * Asigna el pais donde se ubica el hotel.
     * @param pais nombre del pais.
     */
    public void setPais(String pais) { this.pais = pais; }

    /**
     * Retorna la descripcion general del hotel.
     * @return descripcion del hotel.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Asigna la descripcion general del hotel.
     * @param descripcion descripcion del hotel.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Retorna la calificacion promedio del hotel.
     * @return rating del hotel.
     */
    public double getRating() { return rating; }

    /**
     * Asigna la calificacion promedio del hotel.
     * @param rating calificacion del hotel.
     */
    public void setRating(double rating) { this.rating = rating; }

    /**
     * Retorna el estado actual del hotel.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Asigna el estado actual del hotel.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Retorna los IDs de las imagenes asociadas al hotel.
     * @return lista de IDs de imagenes.
     */
    public List<Integer> getImagenesIds() { return imagenesIds; }

    /**
     * Asigna los IDs de las imagenes asociadas al hotel.
     * @param imagenesIds lista de IDs de imagenes.
     */
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }

    /**
     * Retorna las amenidades asignadas al hotel.
     * @return lista de amenidades con sus descripciones e imagenes.
     */
    public List<AmenidadHotelDTO> getAmenidades() { return amenidades; }

    /**
     * Asigna las amenidades del hotel.
     * @param amenidades lista de amenidades con sus descripciones e imagenes.
     */
    public void setAmenidades(List<AmenidadHotelDTO> amenidades) { this.amenidades = amenidades; }

    /**
     * Retorna los tipos de habitacion disponibles en el hotel para la busqueda.
     * @return lista de tipos de habitacion.
     */
    public List<TipoHabitacionResultadoDTO> getTiposHabitacion() { return tiposHabitacion; }

    /**
     * Asigna los tipos de habitacion disponibles en el hotel.
     * @param tiposHabitacion lista de tipos de habitacion.
     */
    public void setTiposHabitacion(List<TipoHabitacionResultadoDTO> tiposHabitacion) { this.tiposHabitacion = tiposHabitacion; }

    /**
     * Retorna las combinaciones numericas de habitaciones que cubren la cantidad de personas solicitada.
     * @return lista de combinaciones, donde cada combinacion es una lista de capacidades.
     */
    public List<List<Integer>> getCombinacionesNumericas() { return combinacionesNumericas; }

    /**
     * Asigna las combinaciones numericas de habitaciones.
     * @param combinacionesNumericas lista de combinaciones de capacidades.
     */
    public void setCombinacionesNumericas(List<List<Integer>> combinacionesNumericas) { this.combinacionesNumericas = combinacionesNumericas; }

    /**
     * Retorna los tipos de habitacion agrupados por capacidad maxima.
     * Util para mostrar al usuario las opciones segun el numero de personas por habitacion.
     * @return mapa donde la clave es la capacidad y el valor es la lista de tipos de habitacion.
     */
    public Map<Integer, List<TipoHabitacionResultadoDTO>> getTiposHabitacionPorCapacidad() { return tiposHabitacionPorCapacidad; }

    /**
     * Asigna los tipos de habitacion agrupados por capacidad maxima.
     * @param tiposHabitacionPorCapacidad mapa de capacidad a lista de tipos de habitacion.
     */
    public void setTiposHabitacionPorCapacidad(Map<Integer, List<TipoHabitacionResultadoDTO>> tiposHabitacionPorCapacidad) { this.tiposHabitacionPorCapacidad = tiposHabitacionPorCapacidad; }
}