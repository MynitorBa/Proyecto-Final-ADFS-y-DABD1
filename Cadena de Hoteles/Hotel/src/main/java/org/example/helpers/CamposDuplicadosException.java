package org.example.helpers;

import org.example.dtos.UsuarioValidacionResponseDTO;

/**
 * Excepcion lanzada cuando uno o mas campos unicos ya existen en el sistema
 * durante el registro de un usuario (username, correo o pasaporte duplicados).
 * Incluye el detalle de cuales campos especificamente estan duplicados.
 */
public class CamposDuplicadosException extends RuntimeException {

    private final UsuarioValidacionResponseDTO detalle;

    /**
     * Crea la excepcion con el detalle de los campos duplicados encontrados.
     *
     * @param detalle DTO con flags indicando cuales campos ya existen en el sistema.
     */
    public CamposDuplicadosException(UsuarioValidacionResponseDTO detalle) {
        super("Uno o más campos ya existen");
        this.detalle = detalle;
    }

    /**
     * Retorna el detalle de los campos duplicados que causaron la excepcion.
     *
     * @return DTO con el estado de validacion de username, correo y pasaporte.
     */
    public UsuarioValidacionResponseDTO getDetalle() {
        return detalle;
    }
}