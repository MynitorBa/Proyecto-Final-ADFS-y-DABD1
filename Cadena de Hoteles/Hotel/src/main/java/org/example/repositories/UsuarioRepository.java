package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.UsuarioAdminDTO;
import org.example.dtos.UsuarioPerfilResponseDTO;

import java.util.List;

public class UsuarioRepository {

    // ─────────────────────── Validaciones ──────────────────────────────────

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

    // ─────────────────────── Creación ──────────────────────────────────────

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

    // ─────────────────────── Perfil completo ───────────────────────────────

    public UsuarioPerfilResponseDTO obtenerPerfil(int usuarioId) {
        String sql = """
                SELECT u.ID, u.Username, u.Correo, u.Pasaporte, u.Nombre, u.Apellido,
                       u.Telefono, u.Fecha_Nacimiento, u.Rol_ID,
                       c.Nombre AS Ciudad, p.Nombre AS Pais
                FROM   Usuario u
                LEFT JOIN Ciudad c ON u.Ciudad_ID = c.ID
                LEFT JOIN Pais   p ON c.Pais_ID   = p.ID
                WHERE  u.ID = ?
                """;

        List<UsuarioPerfilResponseDTO> result = DatabaseManager.executeQuery(sql, rs -> {
            UsuarioPerfilResponseDTO dto = new UsuarioPerfilResponseDTO();
            dto.setId(rs.getInt("ID"));
            dto.setUsername(rs.getString("Username"));
            dto.setCorreo(rs.getString("Correo"));
            dto.setPasaporte(rs.getString("Pasaporte"));
            dto.setNombre(rs.getString("Nombre"));
            dto.setApellido(rs.getString("Apellido"));
            dto.setTelefono(rs.getString("Telefono"));

            java.sql.Date fecha = rs.getDate("Fecha_Nacimiento");
            dto.setFechaNacimiento(fecha != null ? fecha.toString() : null);

            dto.setRolId(rs.getInt("Rol_ID"));
            dto.setCiudad(rs.getString("Ciudad"));
            dto.setPais(rs.getString("Pais"));
            return dto;
        }, usuarioId);

        return result.isEmpty() ? null : result.get(0);
    }

    // ─────────────────────── Nacionalidades del usuario ────────────────────

    public List<String> obtenerNacionalidades(int usuarioId) {
        String sql = """
                SELECT n.Nombre
                FROM   UsuarioNacionalidad un
                JOIN   Nacionalidad n ON un.Nacionalidad_ID = n.ID
                WHERE  un.Usuario_ID = ?
                """;

        return DatabaseManager.executeQuery(sql, rs -> rs.getString("Nombre"), usuarioId);
    }

    // ─────────────────────── Cambiar teléfono ──────────────────────────────

    public void actualizarTelefono(int usuarioId, String telefono) {
        String sql = "UPDATE Usuario SET Telefono = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, telefono, usuarioId);
    }

    // ─────────────────────── Contraseña ────────────────────────────────────

    public String obtenerContrasena(int usuarioId) {
        String sql = "SELECT Contrasena FROM Usuario WHERE ID = ?";
        List<String> result = DatabaseManager.executeQuery(sql, rs -> rs.getString("Contrasena"), usuarioId);
        return result.isEmpty() ? null : result.get(0);
    }

    public void actualizarContrasena(int usuarioId, String contrasenaHasheada) {
        String sql = "UPDATE Usuario SET Contrasena = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, contrasenaHasheada, usuarioId);
    }

    // ─────────────────────── Admin: listar todos con rol ───────────────────

    public List<UsuarioAdminDTO> listarTodosConRol() {
        String sql = """
                SELECT u.ID, u.Username, u.Nombre, u.Apellido, u.Correo, u.Telefono,
                       u.Fecha_Nacimiento, u.Rol_ID,
                       r.RolNombre,
                       c.Nombre AS Ciudad,
                       p.Nombre AS Pais
                FROM   Usuario u
                JOIN   Rol     r ON u.Rol_ID    = r.ID
                LEFT JOIN Ciudad c ON u.Ciudad_ID = c.ID
                LEFT JOIN Pais   p ON c.Pais_ID   = p.ID
                ORDER BY u.ID
                """;

        return DatabaseManager.executeQuery(sql, rs -> {
            UsuarioAdminDTO dto = new UsuarioAdminDTO();
            dto.setId(rs.getInt("ID"));
            dto.setUsername(rs.getString("Username"));
            dto.setNombre(rs.getString("Nombre"));
            dto.setApellido(rs.getString("Apellido"));
            dto.setCorreo(rs.getString("Correo"));
            dto.setTelefono(rs.getString("Telefono"));

            java.sql.Date fecha = rs.getDate("Fecha_Nacimiento");
            dto.setFechaNacimiento(fecha != null ? fecha.toString() : null);

            dto.setRolId(rs.getInt("Rol_ID"));
            dto.setRolNombre(rs.getString("RolNombre"));
            dto.setCiudad(rs.getString("Ciudad"));
            dto.setPais(rs.getString("Pais"));
            return dto;
        });
    }

    // ─────────────────────── Admin: cambiar rol ────────────────────────────

    public void actualizarRol(int usuarioId, int nuevoRolId) {
        String sql = "UPDATE Usuario SET Rol_ID = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, nuevoRolId, usuarioId);
    }
}