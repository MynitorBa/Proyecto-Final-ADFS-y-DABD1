package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class CiudadRepository {

    public int buscarOCrearPorNombre(String nombre, int paisId) {
        // 1. Buscar si ya existe la ciudad en ese país
        String sqlBuscar = "SELECT ID FROM Ciudad WHERE LOWER(Nombre) = LOWER(?) AND Pais_ID = ?";
        List<Integer> resultado = DatabaseManager.executeQuery(
                sqlBuscar,
                rs -> rs.getInt("ID"),
                nombre, paisId
        );

        if (!resultado.isEmpty()) {
            return resultado.get(0);
        }

        // 2. No existe, la creamos y retornamos el ID generado directamente
        String sqlInsertar = "INSERT INTO Ciudad (Nombre, Pais_ID) VALUES (?, ?)";
        return DatabaseManager.executeInsertReturnId(sqlInsertar, "ID", nombre, paisId);
    }
}