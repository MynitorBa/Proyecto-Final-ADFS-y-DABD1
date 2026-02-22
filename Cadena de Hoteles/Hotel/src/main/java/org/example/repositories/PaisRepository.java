package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.CiudadDTO;
import org.example.dtos.PaisDTO;

import java.util.List;

public class PaisRepository {

    // ── Método existente — busca o crea el país ────────────────────────────────
    public int buscarOCrearPorNombre(String nombre) {
        List<Integer> resultado = DatabaseManager.executeQuery(
                "SELECT ID FROM Pais WHERE LOWER(Nombre) = LOWER(?)",
                rs -> rs.getInt("ID"),
                nombre
        );

        if (!resultado.isEmpty()) return resultado.get(0);

        try {
            return DatabaseManager.executeInsertReturnId(
                    "INSERT INTO Pais (Nombre) VALUES (?)", "ID", nombre);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("ORA-00001")) {
                // Otro hilo insertó primero — volver a buscar
                List<Integer> retry = DatabaseManager.executeQuery(
                        "SELECT ID FROM Pais WHERE LOWER(Nombre) = LOWER(?)",
                        rs -> rs.getInt("ID"), nombre);
                if (!retry.isEmpty()) return retry.get(0);
            }
            throw e;
        }
    }

    // ── Para dropdowns del panel admin ────────────────────────────────────────
    public List<PaisDTO> listarPaises() {
        return DatabaseManager.executeQuery(
                "SELECT ID, Nombre FROM Pais ORDER BY Nombre",
                rs -> {
                    PaisDTO d = new PaisDTO();
                    d.setId(rs.getInt("ID"));
                    d.setNombre(rs.getString("Nombre"));
                    return d;
                }
        );
    }

    public List<CiudadDTO> listarCiudades() {
        String sql = """
                SELECT c.ID, c.Nombre, c.Pais_ID, p.Nombre AS PaisNombre
                FROM   Ciudad c JOIN Pais p ON c.Pais_ID = p.ID
                ORDER BY p.Nombre, c.Nombre
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            CiudadDTO d = new CiudadDTO();
            d.setId(rs.getInt("ID"));
            d.setNombre(rs.getString("Nombre"));
            d.setPaisId(rs.getInt("Pais_ID"));
            d.setPaisNombre(rs.getString("PaisNombre"));
            return d;
        });
    }
}