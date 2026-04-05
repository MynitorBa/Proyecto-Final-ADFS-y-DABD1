package org.example.dtos;

/**
 * DTO que representa los datos de una agencia para transferencia entre capas.
 * Incluye el nombre del estado resuelto mediante join con la tabla ESTADO.
 */
public class AgenciaDTO {

    private int    id;
    private String nombre;
    private String correo;
    private int    usuarioWebisId;
    private double porcentajeDescuento;
    private int    estadoId;
    private String estado;

    /**
     * Retorna el identificador unico de la agencia.
     * @return ID de la agencia.
     */
    public int getId() { return id; }

    /**
     * Retorna el nombre comercial de la agencia.
     * @return nombre de la agencia.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna el correo electronico de la agencia.
     * @return correo de la agencia.
     */
    public String getCorreo() { return correo; }

    /**
     * Retorna el ID del usuario webservice asociado a la agencia.
     * @return ID del usuario webservice.
     */
    public int getUsuarioWebisId() { return usuarioWebisId; }

    /**
     * Retorna el porcentaje de descuento aplicado a las reservaciones de la agencia.
     * @return porcentaje de descuento.
     */
    public double getPorcentajeDescuento() { return porcentajeDescuento; }

    /**
     * Retorna el ID del estado actual de la agencia.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Retorna el nombre del estado actual resuelto desde la tabla ESTADO.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Asigna el identificador unico de la agencia.
     * @param id ID de la agencia.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el nombre comercial de la agencia.
     * @param nombre nombre de la agencia.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna el correo electronico de la agencia.
     * @param correo correo de la agencia.
     */
    public void setCorreo(String correo) { this.correo = correo; }

    /**
     * Asigna el ID del usuario webservice asociado a la agencia.
     * @param usuarioWebisId ID del usuario webservice.
     */
    public void setUsuarioWebisId(int usuarioWebisId) { this.usuarioWebisId = usuarioWebisId; }

    /**
     * Asigna el porcentaje de descuento aplicado a las reservaciones de la agencia.
     * @param porcentajeDescuento porcentaje de descuento.
     */
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    /**
     * Asigna el ID del estado actual de la agencia.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }

    /**
     * Asigna el nombre del estado actual resuelto desde la tabla ESTADO.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }
}