package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class UsuarioRepository {

    //--------------------------------Validaciones -----------------------------------------------------------

    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Username = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), username);
        return !result.isEmpty() && result.get(0) > 0;
    }

    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Correo = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), correo);
        return !result.isEmpty() && result.get(0) > 0;
    }

    public boolean existePasaporte(String pasaporte) {
        if (pasaporte == null || pasaporte.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Pasaporte = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), pasaporte);
        return !result.isEmpty() && result.get(0) > 0;
    }

    //--------------------------------Crecion -----------------------------------------------------------
    public int crearUsuario(
            String correo,
            String contrasenaHasheada,
            String pasaporte,
            String username,
            String nombre,
            String apellido,
            String telefono,
            java.sql.Date fechaNacimiento,
            int ciudadId
    ) {
        String sql = """
                INSERT INTO Usuario
                    (Correo, Contrasena, Pasaporte, Username, Nombre, Apellido,
                     Rol_ID, Telefono, Fecha_Nacimiento, Ciudad_ID)
                VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?, ?)
                """;

        return DatabaseManager.executeInsertReturnId(
                sql, "ID",
                correo, contrasenaHasheada, pasaporte, username,
                nombre, apellido, telefono, fechaNacimiento, ciudadId
        );
    }
}