package org.example.dtos;

/**
 * DTO que representa un usuario webservice disponible para ser asignado a una entidad.
 * Un usuario se considera "libre" cuando no tiene ni agencia ni aerolinea vinculadas.
 */
public class UsuarioWebserviceLibreDTO {

    private int    id;
    private String username;

    /**
     * Retorna el ID del usuario webservice.
     * @return ID del usuario.
     */
    public int getId() { return id; }

    /**
     * Retorna el nombre de usuario.
     * @return username del usuario.
     */
    public String getUsername() { return username; }

    /**
     * Asigna el ID del usuario webservice.
     * @param id ID del usuario.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el nombre de usuario.
     * @param username username del usuario.
     */
    public void setUsername(String username) { this.username = username; }
}