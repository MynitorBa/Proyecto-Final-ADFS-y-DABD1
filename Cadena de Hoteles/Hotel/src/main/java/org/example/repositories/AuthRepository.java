package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.models.Usuario;

import java.util.List;

/**
 * Repository para la autenticacion de usuarios.
 * Maneja la busqueda de credenciales en la base de datos durante el proceso de login.
 */
public class AuthRepository {

    /**
     * Busca un usuario por su username o correo electronico.
     * El identificador se compara contra ambos campos en una sola consulta.
     * @param identificador username o correo del usuario a buscar.
     * @return instancia de Usuario con sus credenciales, o null si no existe.
     */
    public Usuario buscarPorIdentificador(String identificador) {
        // Busca por username O por correo con una sola query
        String sql = """
                SELECT ID, Username, Correo, Contrasena, Rol_ID
                FROM Usuario
                WHERE Username = ? OR Correo = ?
                """;

        List<Usuario> resultado = DatabaseManager.executeQuery(sql, rs -> {
            Usuario u = new Usuario();
            u.setId(rs.getInt("ID"));
            u.setUsername(rs.getString("Username"));
            u.setCorreo(rs.getString("Correo"));
            u.setContrasena(rs.getString("Contrasena"));
            u.setRolId(rs.getInt("Rol_ID"));
            return u;
        }, identificador, identificador);

        return resultado.isEmpty() ? null : resultado.get(0);
    }
}