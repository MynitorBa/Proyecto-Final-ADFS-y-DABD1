package org.example.dtos;

/**
 * DTO con los criterios de busqueda de habitaciones disponibles.
 * Las fechas deben enviarse en formato YYYY-MM-DD.
 */
public class BusquedaRequestDTO {

    private String pais;
    private String ciudad;
    private String fechaCheckIn;
    private String fechaCheckOut;
    private int    cantidadPersonas;

    /**
     * Retorna el pais destino de la busqueda.
     * @return nombre del pais.
     */
    public String getPais() { return pais; }

    /**
     * Asigna el pais destino de la busqueda.
     * @param pais nombre del pais.
     */
    public void setPais(String pais) { this.pais = pais; }

    /**
     * Retorna la ciudad destino de la busqueda.
     * @return nombre de la ciudad.
     */
    public String getCiudad() { return ciudad; }

    /**
     * Asigna la ciudad destino de la busqueda.
     * @param ciudad nombre de la ciudad.
     */
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    /**
     * Retorna la fecha de entrada en formato YYYY-MM-DD.
     * @return fecha de check-in.
     */
    public String getFechaCheckIn() { return fechaCheckIn; }

    /**
     * Asigna la fecha de entrada en formato YYYY-MM-DD.
     * @param fechaCheckIn fecha de check-in.
     */
    public void setFechaCheckIn(String fechaCheckIn) { this.fechaCheckIn = fechaCheckIn; }

    /**
     * Retorna la fecha de salida en formato YYYY-MM-DD.
     * @return fecha de check-out.
     */
    public String getFechaCheckOut() { return fechaCheckOut; }

    /**
     * Asigna la fecha de salida en formato YYYY-MM-DD.
     * @param fechaCheckOut fecha de check-out.
     */
    public void setFechaCheckOut(String fechaCheckOut) { this.fechaCheckOut = fechaCheckOut; }

    /**
     * Retorna el numero de personas para filtrar habitaciones con capacidad suficiente.
     * @return cantidad de personas.
     */
    public int getCantidadPersonas() { return cantidadPersonas; }

    /**
     * Asigna el numero de personas para filtrar habitaciones con capacidad suficiente.
     * @param cantidadPersonas cantidad de personas.
     */
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }
}