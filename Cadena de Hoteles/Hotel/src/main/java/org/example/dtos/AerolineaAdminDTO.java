package org.example.dtos;

/**
 * DTO que representa los datos de una aerolinea aliada para el panel de administracion.
 * Incluye el username del usuario webservice propietario para facilitar la gestion.
 * No expone el TokenHASH de autenticacion ya que ese se gestiona internamente.
 */
public class AerolineaAdminDTO {

    private int    id;
    private String nombre;
    private int    usuarioWebis;
    private String usuarioUsername;
    private double porcentajeDescuento;
    private int    estadoId;
    private String estado;
    private String url;
    private String urlParaUsuario;

    /**
     * Retorna el identificador unico de la aerolinea.
     * @return ID de la aerolinea.
     */
    public int getId() { return id; }

    /**
     * Retorna el nombre comercial de la aerolinea.
     * @return nombre de la aerolinea.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna el ID del usuario webservice propietario de la aerolinea.
     * @return ID del usuario webservice.
     */
    public int getUsuarioWebis() { return usuarioWebis; }

    /**
     * Retorna el username del usuario webservice propietario.
     * @return username del usuario webservice.
     */
    public String getUsuarioUsername() { return usuarioUsername; }

    /**
     * Retorna el porcentaje de descuento asignado.
     * @return porcentaje de descuento.
     */
    public double getPorcentajeDescuento() { return porcentajeDescuento; }

    /**
     * Retorna el ID del estado actual de la aerolinea.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Retorna el nombre del estado actual de la aerolinea.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Retorna la URL del sistema externo de la aerolinea.
     * @return URL del sistema externo.
     */
    public String getUrl() { return url; }

    /**
     * Retorna la URL de redireccion para el usuario final.
     * @return URL para el usuario final.
     */
    public String getUrlParaUsuario() { return urlParaUsuario; }

    /**
     * Asigna el identificador unico de la aerolinea.
     * @param id ID de la aerolinea.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el nombre comercial de la aerolinea.
     * @param nombre nombre de la aerolinea.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna el ID del usuario webservice propietario.
     * @param usuarioWebis ID del usuario webservice.
     */
    public void setUsuarioWebis(int usuarioWebis) { this.usuarioWebis = usuarioWebis; }

    /**
     * Asigna el username del usuario webservice propietario.
     * @param usuarioUsername username del usuario webservice.
     */
    public void setUsuarioUsername(String usuarioUsername) { this.usuarioUsername = usuarioUsername; }

    /**
     * Asigna el porcentaje de descuento.
     * @param porcentajeDescuento porcentaje de descuento.
     */
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    /**
     * Asigna el ID del estado actual.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }

    /**
     * Asigna el nombre del estado actual.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Asigna la URL del sistema externo.
     * @param url URL del sistema externo.
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Asigna la URL de redireccion para el usuario final.
     * @param urlParaUsuario URL para el usuario final.
     */
    public void setUrlParaUsuario(String urlParaUsuario) { this.urlParaUsuario = urlParaUsuario; }
}