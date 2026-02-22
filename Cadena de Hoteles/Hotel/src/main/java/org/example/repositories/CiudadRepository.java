package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class CiudadRepository {

    public int buscarOCrearPorNombre(String nombre, int paisId) {

        // 1. Buscar si ya existe (case-insensitive)
        List<Integer> resultado = DatabaseManager.executeQuery(
                "SELECT ID FROM Ciudad WHERE LOWER(Nombre) = LOWER(?) AND Pais_ID = ?",
                rs -> rs.getInt("ID"),
                nombre, paisId
        );

        if (!resultado.isEmpty()) {
            return resultado.get(0);
        }

        // 2. Intentar insertar; si hay colisión de unique (ORA-00001)
        //    volver a buscar (race condition entre hilos o constraint case-sensitive)
        try {
            return DatabaseManager.executeInsertReturnId(
                    "INSERT INTO Ciudad (Nombre, Pais_ID) VALUES (?, ?)",
                    "ID", nombre, paisId
            );
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("ORA-00001")) {
                // La ciudad ya existe (insertada por otro hilo o constraint distinto)
                List<Integer> retry = DatabaseManager.executeQuery(
                        "SELECT ID FROM Ciudad WHERE LOWER(Nombre) = LOWER(?) AND Pais_ID = ?",
                        rs -> rs.getInt("ID"),
                        nombre, paisId
                );
                if (!retry.isEmpty()) return retry.get(0);

                // Si la constraint es solo sobre Nombre (sin Pais_ID), buscar sin filtro de país
                retry = DatabaseManager.executeQuery(
                        "SELECT ID FROM Ciudad WHERE LOWER(Nombre) = LOWER(?)",
                        rs -> rs.getInt("ID"),
                        nombre
                );
                if (!retry.isEmpty()) return retry.get(0);
            }
            throw e; // Otro tipo de error, propagar
        }
    }
}