package org.example.services;

import org.example.dtos.AerolineaWebserviceDTO;
import org.example.dtos.CrearAerolineaRequestDTO;
import org.example.repositories.AerolineaWebserviceRepository;

import java.util.List;

/**
 * Service para la gestion de aerolineas aliadas desde el portal webservice.
 * Cubre operaciones del usuario webservice dueno de la aerolinea:
 * consulta, registro y cambio de estado.
 */
public class AerolineaWebserviceService {

    private final AerolineaWebserviceRepository repo;

    /**
     * Crea una instancia de AerolineaWebserviceService con sus dependencias inyectadas.
     */
    public AerolineaWebserviceService(AerolineaWebserviceRepository repo) {
        this.repo = repo;
    }

    /**
     * Retorna las aerolineas aliadas asociadas a un usuario webservice especifico.
     * @param usuarioId ID del usuario webservice propietario de las aerolineas.
     * @return lista de aerolineas del usuario.
     */
    public List<AerolineaWebserviceDTO> listarPorUsuario(int usuarioId) {
        return repo.listarPorUsuario(usuarioId);
    }

    /**
     * Registra una nueva aerolinea aliada vinculada al usuario dado.
     * @param usuarioId ID del usuario webservice que crea la aerolinea.
     * @param req       datos de la nueva aerolinea.
     * @return DTO con los datos de la aerolinea creada.
     */
    public AerolineaWebserviceDTO crear(int usuarioId, CrearAerolineaRequestDTO req) {
        return repo.crear(usuarioId, req);
    }

    /**
     * Cambia el estado de una aerolinea entre Activo (1) y Cerrado (2).
     * @param aerolineaId   ID de la aerolinea a modificar.
     * @param usuarioId     ID del usuario webservice dueno de la aerolinea.
     * @param nuevoEstadoId nuevo estado: 1 para Activo, 2 para Cerrado.
     * @throws IllegalArgumentException si el estado no es 1 ni 2.
     */
    public void cambiarEstado(int aerolineaId, int usuarioId, int nuevoEstadoId) {
        if (nuevoEstadoId != 1 && nuevoEstadoId != 2)
            throw new IllegalArgumentException("Estado invalido. Use 1 (Activo) o 2 (Cerrado)");
        repo.cambiarEstado(aerolineaId, usuarioId, nuevoEstadoId);
    }
}