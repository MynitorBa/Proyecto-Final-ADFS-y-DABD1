package org.example.dtos;

/**
 * DTO con los datos necesarios para cambiar la contrasena de un usuario autenticado.
 */
public class CambiarContrasenaRequestDTO {

    private String contrasenaActual;
    private String contrasenaNueva;

    /**
     * Retorna la contrasena actual del usuario para verificar su identidad.
     * @return contrasena actual.
     */
    public String getContrasenaActual() { return contrasenaActual; }

    /**
     * Asigna la contrasena actual del usuario para verificar su identidad.
     * @param contrasenaActual contrasena actual.
     */
    public void setContrasenaActual(String contrasenaActual) { this.contrasenaActual = contrasenaActual; }

    /**
     * Retorna la nueva contrasena que reemplazara a la actual.
     * @return nueva contrasena.
     */
    public String getContrasenaNueva() { return contrasenaNueva; }

    /**
     * Asigna la nueva contrasena que reemplazara a la actual.
     * @param contrasenaNueva nueva contrasena.
     */
    public void setContrasenaNueva(String contrasenaNueva) { this.contrasenaNueva = contrasenaNueva; }
}