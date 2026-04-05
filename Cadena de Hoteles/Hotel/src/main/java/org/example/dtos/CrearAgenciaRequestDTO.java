package org.example.dtos;

/**
 * DTO con los datos necesarios para crear una nueva agencia.
 * El porcentaje de descuento siempre inicia en 0% y solo el administrador puede modificarlo.
 */
public class CrearAgenciaRequestDTO {

    private String nombre;
    private String correo;

    /**
     * Retorna el nombre comercial de la nueva agencia.
     * @return nombre de la agencia.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna el correo electronico de la nueva agencia.
     * @return correo de la agencia.
     */
    public String getCorreo() { return correo; }

    /**
     * Asigna el nombre comercial de la nueva agencia.
     * @param nombre nombre de la agencia.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna el correo electronico de la nueva agencia.
     * @param correo correo de la agencia.
     */
    public void setCorreo(String correo) { this.correo = correo; }
}