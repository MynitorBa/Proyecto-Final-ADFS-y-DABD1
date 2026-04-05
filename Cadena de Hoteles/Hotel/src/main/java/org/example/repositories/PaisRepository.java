package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.CiudadDTO;
import org.example.dtos.PaisDTO;

import java.util.List;

/**
 * Repository para la gestion de paises y ciudades.
 * Permite buscar o crear paises de forma segura ante condiciones de carrera,
 * y provee listados de paises y ciudades para uso en formularios del panel de administracion.
 */
public class PaisRepository {

    /**
     * Busca un pais por nombre y lo crea si no existe.
     * Si dos hilos intentan insertar el mismo pais simultaneamente y se produce un conflicto
     * de clave unica (ORA-00001), reintenta la busqueda antes de propagar el error.
     * @param nombre nombre del pais a buscar o crear.
     * @return ID del pais existente o recien creado.
     * @throws RuntimeException si ocurre un error de base de datos distinto a una colision de clave unica.
     */
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
                // Otro hilo inserto primero, se reintenta la busqueda
                List<Integer> retry = DatabaseManager.executeQuery(
                        "SELECT ID FROM Pais WHERE LOWER(Nombre) = LOWER(?)",
                        rs -> rs.getInt("ID"), nombre);
                if (!retry.isEmpty()) return retry.get(0);
            }
            throw e;
        }
    }

    /**
     * Retorna todos los paises registrados ordenados alfabeticamente por nombre.
     * Se usa para poblar dropdowns en el panel de administracion.
     * @return lista de PaisDTO con el ID y nombre de cada pais.
     */
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

    /**
     * Retorna todas las ciudades registradas con su pais asociado, ordenadas por pais y nombre de ciudad.
     * Se usa para poblar dropdowns en el panel de administracion.
     * @return lista de CiudadDTO con el ID, nombre, ID de pais y nombre de pais de cada ciudad.
     */
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