package org.example.dtos;

/**
 * DTO con los datos necesarios para que el administrador cree una aerolinea aliada
 * y la asigne directamente a un usuario webservice existente.
 * El porcentaje de descuento inicia en 0 y el token se genera al conectar.
 */
public class CrearAerolineaAdminRequestDTO {

    private String nombre;
    private String url;
    private String urlParaUsuario;

    /** ID del usuario webservice al que se vinculara la aerolinea. */
    private int usuarioWebisId;

    private String urlHome;

    public String getUrlHome() { return urlHome; }
    public void setUrlHome(String urlHome) { this.urlHome = urlHome; }

    /**
     * Retorna el nombre comercial de la nueva aerolinea.
     * @return nombre de la aerolinea.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna la URL del sistema externo de la nueva aerolinea.
     * @return URL del sistema externo.
     */
    public String getUrl() { return url; }

    /**
     * Retorna la URL de redireccion para el usuario final.
     * @return URL para el usuario final.
     */
    public String getUrlParaUsuario() { return urlParaUsuario; }

    /**
     * Retorna el ID del usuario webservice asignado a la aerolinea.
     * @return ID del usuario webservice.
     */
    public int getUsuarioWebisId() { return usuarioWebisId; }

    /**
     * Asigna el nombre comercial de la nueva aerolinea.
     * @param nombre nombre de la aerolinea.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna la URL del sistema externo de la nueva aerolinea.
     * @param url URL del sistema externo.
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Asigna la URL de redireccion para el usuario final.
     * @param urlParaUsuario URL para el usuario final.
     */
    public void setUrlParaUsuario(String urlParaUsuario) { this.urlParaUsuario = urlParaUsuario; }

    /**
     * Asigna el ID del usuario webservice que sera propietario de la aerolinea.
     * @param usuarioWebisId ID del usuario webservice.
     */
    public void setUsuarioWebisId(int usuarioWebisId) { this.usuarioWebisId = usuarioWebisId; }
}