package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

/**
 * Repository para la gestion de nacionalidades.
 * Permite buscar o crear una nacionalidad de forma idempotente por nombre.
 */
public class NacionalidadRepository {

    /**
     * Busca una nacionalidad por nombre y la crea si no existe.
     * La comparacion del nombre es case-insensitive.
     * @param nombre nombre de la nacionalidad a buscar o crear.
     * @return ID de la nacionalidad existente o recien creada.
     */
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

        // 2. No existe, se crea y se retorna el ID generado
        String sqlInsertar = "INSERT INTO Nacionalidad (Nombre) VALUES (?)";
        return DatabaseManager.executeInsertReturnId(sqlInsertar, "ID", nombre);
    }
}