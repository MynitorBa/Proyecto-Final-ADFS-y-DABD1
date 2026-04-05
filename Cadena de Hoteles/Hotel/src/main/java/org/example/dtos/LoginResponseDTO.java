package org.example.dtos;

/**
 * DTO con los datos retornados al cliente tras una autenticacion exitosa.
 * Incluye el mensaje de confirmacion, el username y el rol del usuario autenticado.
 */
public class LoginResponseDTO {

    private String mensaje;
    private String username;
    private int    rolId;

    /**
     * Constructor que inicializa la respuesta de login con todos sus datos.
     * @param mensaje  mensaje de confirmacion de la autenticacion.
     * @param username nombre de usuario autenticado.
     * @param rolId    ID del rol asignado al usuario.
     */
    public LoginResponseDTO(String mensaje, String username, int rolId) {
        this.mensaje   = mensaje;
        this.username  = username;
        this.rolId     = rolId;
    }

    /**
     * Retorna el mensaje de confirmacion de la autenticacion.
     * @return mensaje de confirmacion.
     */
    public String getMensaje() { return mensaje; }

    /**
     * Retorna el nombre de usuario autenticado.
     * @return username del usuario.
     */
    public String getUsername() { return username; }

    /**
     * Retorna el ID del rol asignado al usuario autenticado.
     * @return ID del rol.
     */
    public int getRolId() { return rolId; }
}