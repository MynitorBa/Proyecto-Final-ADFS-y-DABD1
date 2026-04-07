package org.example.services;

import org.example.dtos.AgenciaDTO;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.CrearAgenciaAdminRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;
import org.example.repositories.AgenciaRepository;

import java.util.List;

/**
 * Service para la gestion de agencias de viaje.
 * Cubre operaciones del webservice (usuario dueno) y del panel de administracion.
 */
public class AgenciaService {

    private final AgenciaRepository repo;

    /**
     * Crea una instancia de AgenciaService con sus dependencias inyectadas.
     */
    public AgenciaService(AgenciaRepository repo) {
        this.repo = repo;
    }

    /**
     * Retorna las agencias asociadas a un usuario especifico.
     * @param usuarioId ID del usuario dueno de las agencias.
     * @return lista de agencias del usuario.
     */
    public List<AgenciaDTO> listarPorUsuario(int usuarioId) {
        return repo.listarPorUsuario(usuarioId);
    }

    /**
     * Crea una nueva agencia vinculada al usuario dado (flujo del portal webservice).
     * @param usuarioId ID del usuario que crea la agencia.
     * @param req       datos de la nueva agencia.
     * @return DTO con los datos de la agencia creada.
     */
    public AgenciaDTO crear(int usuarioId, CrearAgenciaRequestDTO req) {
        return repo.crear(usuarioId, req);
    }

    /**
     * Crea una nueva agencia desde el panel de administracion, asignandola al usuario
     * webservice indicado en el request.
     * @param req datos de la nueva agencia incluyendo el ID del usuario webservice.
     * @return DTO con los datos de la agencia creada.
     */
    public AgenciaDTO crearDesdeAdmin(CrearAgenciaAdminRequestDTO req) {
        return repo.crearDesdeAdmin(req);
    }

    /**
     * Cambia el estado de una agencia entre Activo (1) y Cerrado (2).
     * @param agenciaId    ID de la agencia a modificar.
     * @param usuarioId    ID del usuario webservice dueno de la agencia.
     * @param nuevoEstadoId nuevo estado: 1 para Activo, 2 para Cerrado.
     * @throws IllegalArgumentException si el estado no es 1 ni 2.
     */
    public void cambiarEstado(int agenciaId, int usuarioId, int nuevoEstadoId) {
        if (nuevoEstadoId != 1 && nuevoEstadoId != 2)
            throw new IllegalArgumentException("Estado invalido. Use 1 (Activo) o 2 (Cerrado)");
        repo.cambiarEstado(agenciaId, usuarioId, nuevoEstadoId);
    }

    /**
     * Elimina una agencia del usuario dado.
     * @param agenciaId ID de la agencia a eliminar.
     * @param usuarioId ID del usuario webservice dueno de la agencia.
     */
    public void eliminar(int agenciaId, int usuarioId) {
        repo.eliminar(agenciaId, usuarioId);
    }

    /**
     * Retorna todas las agencias registradas en el sistema.
     * Solo disponible para administradores.
     * @return lista completa de agencias.
     */
    public List<AgenciaDTO> listarTodas() {
        return repo.listarTodas();
    }

    /**
     * Edita los datos de una agencia existente.
     * Solo disponible para administradores.
     * @param agenciaId ID de la agencia a editar.
     * @param req       datos actualizados de la agencia.
     */
    public void editar(int agenciaId, EditarAgenciaRequestDTO req) {
        repo.editar(agenciaId, req);
    }
}