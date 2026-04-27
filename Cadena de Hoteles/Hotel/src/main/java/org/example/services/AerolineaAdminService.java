package org.example.services;

import org.example.dtos.*;
import org.example.repositories.AerolineaAdminRepository;

import java.util.List;

/**
 * Service para la gestion de aerolineas aliadas desde el panel de administracion.
 * Cubre operaciones de listado, creacion, edicion y consulta de usuarios libres.
 */
public class AerolineaAdminService {

    private final AerolineaAdminRepository repo;

    /**
     * Crea una instancia de AerolineaAdminService con sus dependencias inyectadas.
     */
    public AerolineaAdminService(AerolineaAdminRepository repo) {
        this.repo = repo;
    }

    /**
     * Retorna todas las aerolineas aliadas registradas en el sistema.
     * @return lista completa de aerolineas.
     */
    public List<AerolineaAdminDTO> listarTodas() {
        return repo.listarTodas();
    }

    /**
     * Crea una nueva aerolinea aliada asignada al usuario webservice indicado.
     * @param req datos de la nueva aerolinea incluyendo el ID del usuario webservice.
     * @return DTO con los datos de la aerolinea creada.
     */
    public AerolineaAdminDTO crear(CrearAerolineaAdminRequestDTO req) {
        return repo.crear(req);
    }

    /**
     * Edita los datos de una aerolinea existente.
     * @param aerolineaId ID de la aerolinea a editar.
     * @param req         datos actualizados de la aerolinea.
     */
    public void editar(int aerolineaId, EditarAerolineaRequestDTO req) {
        repo.editar(aerolineaId, req);
    }

    /**
     * Retorna los usuarios webservice disponibles para ser asignados a una entidad.
     * Un usuario se considera libre si no tiene ni agencia ni aerolinea registrada.
     * @return lista de usuarios webservice sin entidad asignada.
     */
    public List<UsuarioWebserviceLibreDTO> listarWebserviceLibres() {
        return repo.listarWebserviceLibres();
    }

    /**
     * Retorna nombre y URLHome de las aerolineas activas para recomendaciones.
     */
    public List<AerolineaHomeDTO> listarHome() {
        return repo.listarHome();
    }
}