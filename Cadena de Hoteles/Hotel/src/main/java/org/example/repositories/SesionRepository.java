package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

/**
 * Repository para el acceso a datos relacionados con sesiones de usuario.
 * Actualmente provee consultas sobre roles del sistema.
 */
public class SesionRepository {

    /**
     * Retorna el nombre del rol correspondiente a un ID dado.
     * Si el rol no existe, devuelve el valor por defecto "Desconocido".
     * @param rolId ID del rol a consultar.
     * @return nombre del rol, o "Desconocido" si no se encuentra.
     */
    public String obtenerNombreRol(int rolId) {
        String sql = "SELECT RolNombre FROM Rol WHERE ID = ?";
        List<String> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getString("RolNombre"), rolId
        );
        return result.isEmpty() ? "Desconocido" : result.get(0);
    }
}