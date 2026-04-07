package org.example.dtos;

/**
 * DTO con los datos necesarios para que el administrador cree una agencia
 * y la asigne directamente a un usuario webservice existente.
 * El porcentaje de descuento inicia en 0 y los tokens se generan al conectar.
 */
public class CrearAgenciaAdminRequestDTO {

    private String nombre;
    private String correo;
    private String urlAgencia;

    /** ID del usuario webservice al que se vinculara la agencia. */
    private int usuarioWebisId;

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
     * Retorna la URL del sistema externo de la nueva agencia.
     * @return URL de la agencia.
     */
    public String getUrlAgencia() { return urlAgencia; }

    /**
     * Retorna el ID del usuario webservice asignado a la agencia.
     * @return ID del usuario webservice.
     */
    public int getUsuarioWebisId() { return usuarioWebisId; }

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

    /**
     * Asigna la URL del sistema externo de la nueva agencia.
     * @param urlAgencia URL de la agencia.
     */
    public void setUrlAgencia(String urlAgencia) { this.urlAgencia = urlAgencia; }

    /**
     * Asigna el ID del usuario webservice que sera propietario de la agencia.
     * @param usuarioWebisId ID del usuario webservice.
     */
    public void setUsuarioWebisId(int usuarioWebisId) { this.usuarioWebisId = usuarioWebisId; }
}