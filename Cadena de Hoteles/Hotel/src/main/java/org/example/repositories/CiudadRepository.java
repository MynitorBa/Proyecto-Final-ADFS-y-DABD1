package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

/**
 * Repository para la gestion de ciudades.
 * Permite buscar o crear ciudades de forma segura ante condiciones de carrera entre hilos.
 */
public class CiudadRepository {

    /**
     * Busca una ciudad por nombre y pais, y la crea si no existe.
     * Si dos hilos intentan insertar la misma ciudad simultaneamente y se produce un conflicto
     * de clave unica (ORA-00001), reintenta la busqueda antes de propagar el error.
     * @param nombre  nombre de la ciudad a buscar o crear.
     * @param paisId  ID del pais al que pertenece la ciudad.
     * @return ID de la ciudad existente o recien creada.
     * @throws RuntimeException si ocurre un error de base de datos distinto a una colision de clave unica.
     */
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

        // 2. Intentar insertar; si hay colision de unique (ORA-00001)
        //    volver a buscar por posible race condition entre hilos
        try {
            return DatabaseManager.executeInsertReturnId(
                    "INSERT INTO Ciudad (Nombre, Pais_ID) VALUES (?, ?)",
                    "ID", nombre, paisId
            );
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("ORA-00001")) {
                // La ciudad ya existe, posiblemente insertada por otro hilo
                List<Integer> retry = DatabaseManager.executeQuery(
                        "SELECT ID FROM Ciudad WHERE LOWER(Nombre) = LOWER(?) AND Pais_ID = ?",
                        rs -> rs.getInt("ID"),
                        nombre, paisId
                );
                if (!retry.isEmpty()) return retry.get(0);

                // Si la constraint no incluye Pais_ID, buscar solo por nombre
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