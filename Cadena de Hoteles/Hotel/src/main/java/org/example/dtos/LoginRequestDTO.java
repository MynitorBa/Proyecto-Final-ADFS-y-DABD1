package org.example.dtos;

/**
 * DTO con los datos necesarios para autenticar a un usuario en el sistema.
 * El identificador puede ser el username o el correo electronico.
 */
public class LoginRequestDTO {

    private String identificador; // puede ser username o correo
    private String contrasena;

    /**
     * Retorna el identificador del usuario, que puede ser su username o correo.
     * @return username o correo del usuario.
     */
    public String getIdentificador() { return identificador; }

    /**
     * Asigna el identificador del usuario, que puede ser su username o correo.
     * @param identificador username o correo del usuario.
     */
    public void setIdentificador(String identificador) { this.identificador = identificador; }

    /**
     * Retorna la contrasena del usuario.
     * @return contrasena del usuario.
     */
    public String getContrasena() { return contrasena; }

    /**
     * Asigna la contrasena del usuario.
     * @param contrasena contrasena del usuario.
     */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}