package org.example.helpers;

/**
 * Excepcion lanzada cuando las credenciales proporcionadas por el usuario
 * no coinciden con las registradas en el sistema durante el inicio de sesion
 * o el cambio de contrasena.
 */
public class CredencialesInvalidasException extends RuntimeException {

    /**
     * Crea la excepcion con un mensaje fijo indicando credenciales invalidas.
     */
    public CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}