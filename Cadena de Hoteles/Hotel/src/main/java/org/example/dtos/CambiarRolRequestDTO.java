package org.example.dtos;

/**
 * DTO con el nuevo rol a asignar a un usuario desde el panel de administracion.
 */
public class CambiarRolRequestDTO {

    private int rolId;

    /**
     * Retorna el ID del rol a asignar al usuario.
     * @return ID del rol.
     */
    public int getRolId() { return rolId; }

    /**
     * Asigna el ID del rol a asignar al usuario.
     * @param rolId ID del rol.
     */
    public void setRolId(int rolId) { this.rolId = rolId; }
}