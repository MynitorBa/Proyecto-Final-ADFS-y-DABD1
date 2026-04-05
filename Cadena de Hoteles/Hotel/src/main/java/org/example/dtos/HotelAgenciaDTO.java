package org.example.dtos;

/**
 * DTO con los datos basicos de un hotel para mostrar en el catalogo de agencias externas.
 */
public class HotelAgenciaDTO {

    private int    id;
    private String nombre;
    private String ciudad;
    private String pais;

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
}