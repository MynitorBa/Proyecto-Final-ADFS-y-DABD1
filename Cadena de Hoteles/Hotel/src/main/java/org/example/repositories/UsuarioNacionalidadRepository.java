package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class UsuarioNacionalidadRepository {

    public void asignarNacionalidades(int usuarioId, List<Integer> nacionalidadIds) {
        String sql = "INSERT INTO UsuarioNacionalidad (Usuario_ID, Nacionalidad_ID) VALUES (?, ?)";

        for (int nacionalidadId : nacionalidadIds) {
            DatabaseManager.executeUpdate(sql, usuarioId, nacionalidadId);
        }
    }
}