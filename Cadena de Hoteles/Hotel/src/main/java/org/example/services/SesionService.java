package org.example.services;

import org.example.dtos.SesionDTO;
import org.example.repositories.SesionRepository;

public class SesionService {

    private final SesionRepository sesionRepository = new SesionRepository();

    // Sesión activa — datos vienen del token ya validado por el middleware
    public SesionDTO obtenerSesion(int usuarioId, String username, int rolId) {
        String nombreRol = sesionRepository.obtenerNombreRol(rolId);

        SesionDTO dto = new SesionDTO();
        dto.setUsuarioId(usuarioId);
        dto.setUsername(username);
        dto.setRolId(rolId);
        dto.setRol(nombreRol);
        dto.setAutenticado(true);
        return dto;
    }

    // Sin sesión — para cuando el frontend quiere saber si hay alguien logueado
    public SesionDTO sinSesion() {
        SesionDTO dto = new SesionDTO();
        dto.setAutenticado(false);
        return dto;
    }
}