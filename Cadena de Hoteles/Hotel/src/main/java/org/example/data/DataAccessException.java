package org.example.data;

/**
 * Excepcion personalizada para errores de acceso a datos.
 * Envuelve cualquier excepcion de base de datos en una RuntimeException.
 */
public class DataAccessException extends RuntimeException {

    /**
     * Crea una nueva excepcion de acceso a datos.
     * @param message descripcion del error ocurrido.
     * @param cause   excepcion original que causo el fallo.
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}