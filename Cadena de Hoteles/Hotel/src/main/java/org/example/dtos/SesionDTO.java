package org.example.dtos;

/**
 * DTO con los datos de la sesion activa de un usuario autenticado.
 * Se usa para verificar el estado de autenticacion y el rol del usuario.
 */
public class SesionDTO {

    private int     usuarioId;
    private String  username;
    private int     rolId;
    private String  rol;
    private boolean autenticado;

    /**
     * Retorna el identificador unico del usuario autenticado.
     * @return ID del usuario.
     */
    public int getUsuarioId() { return usuarioId; }

    /**
     * Asigna el identificador unico del usuario autenticado.
     * @param usuarioId ID del usuario.
     */
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    /**
     * Retorna el nombre de usuario de la sesion activa.
     * @return username del usuario.
     */
    public String getUsername() { return username; }

    /**
     * Asigna el nombre de usuario de la sesion activa.
     * @param username username del usuario.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Retorna el ID del rol asignado al usuario autenticado.
     * @return ID del rol.
     */
    public int getRolId() { return rolId; }

    /**
     * Asigna el ID del rol asignado al usuario autenticado.
     * @param rolId ID del rol.
     */
    public void setRolId(int rolId) { this.rolId = rolId; }

    /**
     * Retorna el nombre del rol asignado al usuario autenticado.
     * @return nombre del rol.
     */
    public String getRol() { return rol; }

    /**
     * Asigna el nombre del rol asignado al usuario autenticado.
     * @param rol nombre del rol.
     */
    public void setRol(String rol) { this.rol = rol; }

    /**
     * Retorna si el usuario tiene una sesion activa autenticada.
     * @return true si esta autenticado, false en caso contrario.
     */
    public boolean isAutenticado() { return autenticado; }

    /**
     * Asigna el estado de autenticacion del usuario.
     * @param autenticado true si esta autenticado, false en caso contrario.
     */
    public void setAutenticado(boolean autenticado) { this.autenticado = autenticado; }
}