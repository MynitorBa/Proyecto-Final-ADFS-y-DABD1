package org.example.dtos;

/**
 * DTO que representa los datos de una aerolinea aliada para el portal webservice.
 * No expone el TokenHASH de autenticacion ya que ese se gestiona internamente.
 * Incluye el nombre del estado resuelto mediante join con la tabla EstadoAliado.
 */
public class AerolineaWebserviceDTO {

    private int    id;
    private String nombre;
    private int    usuarioWebis;
    private double porcentajeDescuento;
    private int    estadoId;
    private String estado;

    /** URL del sistema externo (endpoint de la API de la aerolinea). */
    private String url;

    /** URL de redireccion que se entrega al usuario final al reservar desde la aerolinea. */
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
     * Retorna el porcentaje de descuento asignado por el administrador.
     * @return porcentaje de descuento.
     */
    public double getPorcentajeDescuento() { return porcentajeDescuento; }

    /**
     * Retorna el ID del estado actual de la aerolinea.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Retorna el nombre del estado actual resuelto desde la tabla EstadoAliado.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

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
     * Asigna el ID del usuario webservice propietario de la aerolinea.
     * @param usuarioWebis ID del usuario webservice.
     */
    public void setUsuarioWebis(int usuarioWebis) { this.usuarioWebis = usuarioWebis; }

    /**
     * Asigna el porcentaje de descuento asignado por el administrador.
     * @param porcentajeDescuento porcentaje de descuento.
     */
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    /**
     * Asigna el ID del estado actual de la aerolinea.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }

    /**
     * Asigna el nombre del estado actual resuelto desde la tabla EstadoAliado.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

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