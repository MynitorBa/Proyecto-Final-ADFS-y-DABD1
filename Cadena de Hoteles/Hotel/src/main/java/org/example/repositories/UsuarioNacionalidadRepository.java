package org.example.repositories;

import org.example.data.DataAccessException;
import org.example.data.DatabaseManager;

import java.util.List;

/**
 * Repository para gestionar la relacion entre usuarios y sus nacionalidades.
 */
public class UsuarioNacionalidadRepository {

    /**
     * Inserta en la base de datos las nacionalidades asociadas a un usuario.
     * Si la combinacion ya existe (ORA-00001), la ignora y continua.
     * @param usuarioId       ID del usuario al que se le asignan las nacionalidades.
     * @param nacionalidadIds lista de IDs de nacionalidades a asociar.
     */
    public void asignarNacionalidades(int usuarioId, List<Integer> nacionalidadIds) {
        String sql = "INSERT INTO UsuarioNacionalidad (Usuario_ID, Nacionalidad_ID) VALUES (?, ?)";

        for (int nacionalidadId : nacionalidadIds) {
            try {
                DatabaseManager.executeUpdate(sql, usuarioId, nacionalidadId);
            } catch (DataAccessException e) {
                Throwable cause = e.getCause();
                if (cause instanceof java.sql.SQLException sqlEx && sqlEx.getErrorCode() == 1) {
                    // Combinacion ya existe, se ignora
                } else {
                    throw e;
                }
            }
        }
    }
}