package org.example.helpers;

import org.example.dtos.UsuarioValidacionResponseDTO;

public class CamposDuplicadosException extends RuntimeException {

    private final UsuarioValidacionResponseDTO detalle;

    public CamposDuplicadosException(UsuarioValidacionResponseDTO detalle) {
        super("Uno o más campos ya existen");
        this.detalle = detalle;
    }

    public UsuarioValidacionResponseDTO getDetalle() {
        return detalle;
    }
}