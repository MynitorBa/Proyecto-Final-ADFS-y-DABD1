package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.CiudadDTO;
import org.example.dtos.PaisDTO;
import java.util.List;

public class PaisRepository {

    public List<PaisDTO> listarPaises() {
        String sql = "SELECT ID, Nombre FROM Pais ORDER BY Nombre";
        return DatabaseManager.executeQuery(sql, rs -> {
            PaisDTO dto = new PaisDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNombre(rs.getString("Nombre"));
            return dto;
        });
    }

    public List<CiudadDTO> listarCiudades() {
        String sql = """
                SELECT c.ID, c.Nombre, c.Pais_ID, p.Nombre AS PaisNombre
                FROM   Ciudad c
                JOIN   Pais   p ON c.Pais_ID = p.ID
                ORDER BY p.Nombre, c.Nombre
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            CiudadDTO dto = new CiudadDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNombre(rs.getString("Nombre"));
            dto.setPaisId(rs.getInt("Pais_ID"));
            dto.setPaisNombre(rs.getString("PaisNombre"));
            return dto;
        });
    }

    public int buscarOCrearPorNombre(String nombre) {
        String sqlBuscar = "SELECT ID FROM Pais WHERE LOWER(Nombre) = LOWER(?)";
        List<Integer> resultado = DatabaseManager.executeQuery(
                sqlBuscar,
                rs -> rs.getInt("ID"),
                nombre
        );

        if (!resultado.isEmpty()) {
            return resultado.get(0);
        }

        String sqlInsertar = "INSERT INTO Pais (Nombre) VALUES (?)";
        return DatabaseManager.executeInsertReturnId(sqlInsertar, "ID", nombre);
    }
}