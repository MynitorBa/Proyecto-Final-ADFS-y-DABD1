package org.example.dtos;

/**
 * DTO que representa la identidad basica de una agencia externa.
 * Se usa para identificar la agencia durante el proceso de handshake.
 */
public class AgenciaIdentidad {

    private int    id;
    private String nombre;
    private String urlAgencia;

    /**
     * Retorna el identificador unico de la agencia.
     * @return ID de la agencia.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico de la agencia.
     * @param id ID de la agencia.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el nombre comercial de la agencia.
     * @return nombre de la agencia.
     */
    public String getNombre() { return nombre; }

    /**
     * Asigna el nombre comercial de la agencia.
     * @param nombre nombre de la agencia.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Retorna la URL base del sistema de la agencia externa.
     * @return URL de la agencia.
     */
    public String getUrlAgencia() { return urlAgencia; }

    /**
     * Asigna la URL base del sistema de la agencia externa.
     * @param urlAgencia URL de la agencia.
     */
    public void setUrlAgencia(String urlAgencia) { this.urlAgencia = urlAgencia; }
}