package org.example.services;

import org.example.dtos.SesionDTO;
import org.example.repositories.SesionRepository;

/**
 * Service para la gestion de la sesion activa del usuario.
 * Construye el estado de sesion a partir de los datos del token JWT
 * ya validado por el middleware.
 */
public class SesionService {

    private final SesionRepository sesionRepository = new SesionRepository();

    /**
     * Construye el DTO de sesion para un usuario autenticado.
     * Los datos del usuario vienen del token ya validado por el middleware,
     * y se complementa con el nombre del rol consultado en base de datos.
     *
     * @param usuarioId ID del usuario autenticado.
     * @param username  nombre de usuario extraido del token.
     * @param rolId     ID del rol del usuario.
     * @return DTO con los datos de sesion y autenticado en true.
     */
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

    /**
     * Retorna un DTO de sesion vacia para cuando no hay usuario autenticado.
     * Util para que el frontend determine si debe mostrar contenido protegido.
     *
     * @return DTO con autenticado en false y sin datos de usuario.
     */
    public SesionDTO sinSesion() {
        SesionDTO dto = new SesionDTO();
        dto.setAutenticado(false);
        return dto;
    }
}