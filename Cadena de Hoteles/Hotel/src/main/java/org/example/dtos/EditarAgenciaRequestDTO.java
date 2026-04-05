package org.example.dtos;

/**
 * DTO con los datos editables de una agencia desde el panel de administracion.
 * Permite modificar nombre, correo, porcentaje de descuento y estado.
 */
public class EditarAgenciaRequestDTO {

    private String nombre;
    private String correo;
    private double porcentajeDescuento;
    private int    estadoId;

    /**
     * Retorna el nombre comercial actualizado de la agencia.
     * @return nombre de la agencia.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna el correo electronico actualizado de la agencia.
     * @return correo de la agencia.
     */
    public String getCorreo() { return correo; }

    /**
     * Retorna el porcentaje de descuento a aplicar en reservaciones de la agencia.
     * @return porcentaje de descuento.
     */
    public double getPorcentajeDescuento() { return porcentajeDescuento; }

    /**
     * Retorna el ID del nuevo estado de la agencia.
     * @return ID del estado.
     */
    public int getEstadoId() { return estadoId; }

    /**
     * Asigna el nombre comercial actualizado de la agencia.
     * @param nombre nombre de la agencia.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna el correo electronico actualizado de la agencia.
     * @param correo correo de la agencia.
     */
    public void setCorreo(String correo) { this.correo = correo; }

    /**
     * Asigna el porcentaje de descuento a aplicar en reservaciones de la agencia.
     * @param porcentajeDescuento porcentaje de descuento.
     */
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    /**
     * Asigna el ID del nuevo estado de la agencia.
     * @param estadoId ID del estado.
     */
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }
}