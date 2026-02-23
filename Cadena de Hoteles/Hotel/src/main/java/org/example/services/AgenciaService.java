package org.example.services;

import org.example.dtos.AgenciaDTO;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;
import org.example.repositories.AgenciaRepository;

import java.util.List;

public class AgenciaService {

    private final AgenciaRepository repo = new AgenciaRepository();

    // ── Webservice ────────────────────────────────────────────────────────────
    public List<AgenciaDTO> listarPorUsuario(int usuarioId) {
        return repo.listarPorUsuario(usuarioId);
    }

    public AgenciaDTO crear(int usuarioId, CrearAgenciaRequestDTO req) {
        return repo.crear(usuarioId, req);
    }

    public void cambiarEstado(int agenciaId, int usuarioId, int nuevoEstadoId) {
        if (nuevoEstadoId != 1 && nuevoEstadoId != 2)
            throw new IllegalArgumentException("Estado invalido. Use 1 (Activo) o 2 (Cerrado)");
        repo.cambiarEstado(agenciaId, usuarioId, nuevoEstadoId);
    }

    public void eliminar(int agenciaId, int usuarioId) {
        repo.eliminar(agenciaId, usuarioId);
    }

    // ── Admin ─────────────────────────────────────────────────────────────────
    public List<AgenciaDTO> listarTodas() {
        return repo.listarTodas();
    }

    public void editar(int agenciaId, EditarAgenciaRequestDTO req) {
        repo.editar(agenciaId, req);
    }
}