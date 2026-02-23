package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AgenciaDTO;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AgenciaRepository {

    // ── Mapper reutilizable ───────────────────────────────────────────────────
    private AgenciaDTO mapRow(ResultSet rs) throws SQLException {
        AgenciaDTO dto = new AgenciaDTO();
        dto.setId(rs.getInt("ID"));
        dto.setNombre(rs.getString("Nombre"));
        dto.setCorreo(rs.getString("Correo"));
        dto.setUsuarioWebisId(rs.getInt("UsuarioWebis_ID"));
        dto.setPorcentajeDescuento(rs.getDouble("PorcentajeDescuento"));
        dto.setEstadoId(rs.getInt("EstadoID"));
        dto.setEstado(rs.getString("Estado"));
        return dto;
    }

    // ── Listar TODAS las agencias (admin) ─────────────────────────────────────
    public List<AgenciaDTO> listarTodas() {
        String sql = """
                SELECT a.ID, a.Nombre, a.Correo, a.UsuarioWebis_ID,
                       a.PorcentajeDescuento, a.EstadoID, e.Estado
                FROM   Agencia a
                JOIN   Estado  e ON a.EstadoID = e.ID
                ORDER BY a.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> mapRow(rs));
    }

    // ── Listar agencias de un usuario webservice ───────────────────────────────
    public List<AgenciaDTO> listarPorUsuario(int usuarioId) {
        String sql = """
                SELECT a.ID, a.Nombre, a.Correo, a.UsuarioWebis_ID,
                       a.PorcentajeDescuento, a.EstadoID, e.Estado
                FROM   Agencia a
                JOIN   Estado  e ON a.EstadoID = e.ID
                WHERE  a.UsuarioWebis_ID = ?
                ORDER BY a.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> mapRow(rs), usuarioId);
    }

    // ── Crear agencia ─────────────────────────────────────────────────────────
    public AgenciaDTO crear(int usuarioId, CrearAgenciaRequestDTO req) {
        if (req.getNombre() == null || req.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la agencia es obligatorio");
        if (req.getCorreo() == null || req.getCorreo().isBlank())
            throw new IllegalArgumentException("El correo de la agencia es obligatorio");
        if (req.getPorcentajeDescuento() < 0 || req.getPorcentajeDescuento() > 100)
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");

        String sql = """
                INSERT INTO Agencia (Nombre, Correo, UsuarioWebis_ID, PorcentajeDescuento, EstadoID)
                VALUES (?, ?, ?, ?, 1)
                """;
        int nuevoId = DatabaseManager.executeInsertReturnId(sql, "ID",
                req.getNombre().trim(),
                req.getCorreo().trim(),
                usuarioId,
                req.getPorcentajeDescuento()
        );

        AgenciaDTO dto = new AgenciaDTO();
        dto.setId(nuevoId);
        dto.setNombre(req.getNombre().trim());
        dto.setCorreo(req.getCorreo().trim());
        dto.setUsuarioWebisId(usuarioId);
        dto.setPorcentajeDescuento(req.getPorcentajeDescuento());
        dto.setEstadoId(1);
        dto.setEstado("Activo");
        return dto;
    }

    // ── Editar agencia (admin) ────────────────────────────────────────────────
    public void editar(int agenciaId, EditarAgenciaRequestDTO req) {
        if (req.getNombre() == null || req.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la agencia es obligatorio");
        if (req.getCorreo() == null || req.getCorreo().isBlank())
            throw new IllegalArgumentException("El correo de la agencia es obligatorio");
        if (req.getPorcentajeDescuento() < 0 || req.getPorcentajeDescuento() > 100)
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        if (req.getEstadoId() != 1 && req.getEstadoId() != 2)
            throw new IllegalArgumentException("Estado invalido. Use 1 (Activo) o 2 (Cerrado)");

        DatabaseManager.executeUpdate(
                "UPDATE Agencia SET Nombre = ?, Correo = ?, PorcentajeDescuento = ?, EstadoID = ? WHERE ID = ?",
                req.getNombre().trim(),
                req.getCorreo().trim(),
                req.getPorcentajeDescuento(),
                req.getEstadoId(),
                agenciaId
        );
    }

    // ── Cambiar estado (webservice) ───────────────────────────────────────────
    public void cambiarEstado(int agenciaId, int usuarioId, int nuevoEstadoId) {
        String check = "SELECT COUNT(*) AS C FROM Agencia WHERE ID = ? AND UsuarioWebis_ID = ?";
        List<Integer> res = DatabaseManager.executeQuery(check,
                rs -> rs.getInt("C"), agenciaId, usuarioId);
        if (res.isEmpty() || res.get(0) == 0)
            throw new IllegalArgumentException("Agencia no encontrada o no pertenece a este usuario");

        DatabaseManager.executeUpdate(
                "UPDATE Agencia SET EstadoID = ? WHERE ID = ?",
                nuevoEstadoId, agenciaId
        );
    }

    // ── Eliminar agencia (webservice) ─────────────────────────────────────────
    public void eliminar(int agenciaId, int usuarioId) {
        String check = "SELECT COUNT(*) AS C FROM Agencia WHERE ID = ? AND UsuarioWebis_ID = ?";
        List<Integer> res = DatabaseManager.executeQuery(check,
                rs -> rs.getInt("C"), agenciaId, usuarioId);
        if (res.isEmpty() || res.get(0) == 0)
            throw new IllegalArgumentException("Agencia no encontrada o no pertenece a este usuario");

        DatabaseManager.executeUpdate("DELETE FROM Agencia WHERE ID = ?", agenciaId);
    }
}