package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class SesionRepository {

    public String obtenerNombreRol(int rolId) {
        String sql = "SELECT RolNombre FROM Rol WHERE ID = ?";
        List<String> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getString("RolNombre"), rolId
        );
        return result.isEmpty() ? "Desconocido" : result.get(0);
    }
}