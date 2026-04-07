package org.example.dtos;

/**
 * DTO con los datos necesarios para registrar una nueva aerolinea aliada desde el portal webservice.
 * El porcentaje de descuento siempre inicia en 0% y solo el administrador puede modificarlo.
 * El token de autenticacion se genera automaticamente al establecer la conexion.
 */
public class CrearAerolineaRequestDTO {

    private String nombre;

    /** URL del sistema externo de la aerolinea (endpoint de su API). */
    private String url;

    /** URL de redireccion que se mostrara a los usuarios finales al reservar desde la aerolinea. */
    private String urlParaUsuario;

    /**
     * Retorna el nombre comercial de la nueva aerolinea.
     * @return nombre de la aerolinea.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna la URL del sistema externo de la aerolinea.
     * @return URL del sistema externo.
     */
    public String getUrl() { return url; }

    /**
     * Retorna la URL de redireccion para usuarios finales.
     * @return URL para el usuario final.
     */
    public String getUrlParaUsuario() { return urlParaUsuario; }

    /**
     * Asigna el nombre comercial de la nueva aerolinea.
     * @param nombre nombre de la aerolinea.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna la URL del sistema externo de la aerolinea.
     * @param url URL del sistema externo.
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Asigna la URL de redireccion para usuarios finales.
     * @param urlParaUsuario URL para el usuario final.
     */
    public void setUrlParaUsuario(String urlParaUsuario) { this.urlParaUsuario = urlParaUsuario; }
}