package org.example.dtos;

/**
 * DTO con los datos necesarios para crear un nuevo hotel en el sistema.
 * Si la ciudad o el pais indicados no existen, el servicio los crea automaticamente.
 */
public class CrearHotelRequestDTO {

    private String nombre;
    private String direccion;
    private String descripcion;
    private double rating;
    private int    estadoId;
    private String ciudad;
    private String paisNombre;

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
     * Retorna la calificacion inicial del hotel.
     * @return rating del hotel.
     */
    public double getRating() { return rating; }

    /**
     * Retorna el ID del estado inicial del hotel.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Retorna el nombre de la ciudad donde se ubica el hotel.
     * @return nombre de la ciudad.
     */
    public String getCiudad() { return ciudad; }

    /**
     * Retorna el nombre del pais donde se ubica el hotel.
     * @return nombre del pais.
     */
    public String getPaisNombre() { return paisNombre; }

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
     * Asigna la calificacion inicial del hotel.
     * @param rating rating del hotel.
     */
    public void setRating(double rating) { this.rating = rating; }

    /**
     * Asigna el ID del estado inicial del hotel.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }

    /**
     * Asigna el nombre de la ciudad donde se ubica el hotel.
     * @param ciudad nombre de la ciudad.
     */
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    /**
     * Asigna el nombre del pais donde se ubica el hotel.
     * @param paisNombre nombre del pais.
     */
    public void setPaisNombre(String paisNombre) { this.paisNombre = paisNombre; }
}