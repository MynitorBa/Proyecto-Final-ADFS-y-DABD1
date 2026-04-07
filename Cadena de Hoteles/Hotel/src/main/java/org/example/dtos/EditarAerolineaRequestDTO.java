package org.example.dtos;

/**
 * DTO con los datos editables de una aerolinea aliada desde el panel de administracion.
 * Permite modificar nombre, URLs, porcentaje de descuento y estado.
 */
public class EditarAerolineaRequestDTO {

    private String nombre;
    private String url;
    private String urlParaUsuario;
    private double porcentajeDescuento;
    private int    estadoId;

    /**
     * Retorna el nombre comercial actualizado de la aerolinea.
     * @return nombre de la aerolinea.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna la URL del sistema externo actualizada.
     * @return URL del sistema externo.
     */
    public String getUrl() { return url; }

    /**
     * Retorna la URL de redireccion para el usuario final actualizada.
     * @return URL para el usuario final.
     */
    public String getUrlParaUsuario() { return urlParaUsuario; }

    /**
     * Retorna el porcentaje de descuento actualizado.
     * @return porcentaje de descuento.
     */
    public double getPorcentajeDescuento() { return porcentajeDescuento; }

    /**
     * Retorna el ID del nuevo estado de la aerolinea.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Asigna el nombre comercial actualizado de la aerolinea.
     * @param nombre nombre de la aerolinea.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna la URL del sistema externo actualizada.
     * @param url URL del sistema externo.
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * Asigna la URL de redireccion para el usuario final actualizada.
     * @param urlParaUsuario URL para el usuario final.
     */
    public void setUrlParaUsuario(String urlParaUsuario) { this.urlParaUsuario = urlParaUsuario; }

    /**
     * Asigna el porcentaje de descuento actualizado.
     * @param porcentajeDescuento porcentaje de descuento.
     */
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    /**
     * Asigna el ID del nuevo estado de la aerolinea.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }
}