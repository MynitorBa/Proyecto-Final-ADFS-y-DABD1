package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class NacionalidadRepository {

    public int buscarOCrearPorNombre(String nombre) {
        // 1. Buscar si ya existe
        String sqlBuscar = "SELECT ID FROM Nacionalidad WHERE LOWER(Nombre) = LOWER(?)";
        List<Integer> resultado = DatabaseManager.executeQuery(
                sqlBuscar,
                rs -> rs.getInt("ID"),
                nombre
        );

        if (!resultado.isEmpty()) {
            return resultado.get(0);
        }

        // 2. No existe, la creamos y retornamos el ID generado directamente
        String sqlInsertar = "INSERT INTO Nacionalidad (Nombre) VALUES (?)";
        return DatabaseManager.executeInsertReturnId(sqlInsertar, "ID", nombre);
    }
}