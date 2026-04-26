package org.example.repositories;

import org.example.data.DataAccessException;
import org.example.data.DatabaseManager;
import org.example.dtos.UsuarioAdminDTO;
import org.example.dtos.UsuarioPerfilResponseDTO;
import org.example.dtos.UsuarioValidacionResponseDTO;
import org.example.helpers.CamposDuplicadosException;

import java.util.List;

/**
 * Repository para el acceso a datos de usuarios.
 * Cubre validaciones de unicidad, creacion, consulta de perfil,
 * gestion de contrasenas y operaciones administrativas de rol.
 */
public class UsuarioRepository {

    // Validaciones de unicidad antes de registrar un usuario

    /**
     * Verifica si ya existe un usuario con el username dado.
     * @param username nombre de usuario a verificar.
     * @return true si el username ya esta en uso, false en caso contrario.
     */
    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Username = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), username);
        return !result.isEmpty() && result.get(0) > 0;
    }

    /** Verifica si el username está en uso por un usuario diferente al indicado. */
    public boolean existeUsernameExceptoId(String username, int excluirId) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Username = ? AND ID != ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), username, excluirId);
        return !result.isEmpty() && result.get(0) > 0;
    }

    /** Verifica si el correo está en uso por un usuario diferente al indicado. */
    public boolean existeCorreoExceptoId(String correo, int excluirId) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Correo = ? AND ID != ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), correo, excluirId);
        return !result.isEmpty() && result.get(0) > 0;
    }

    /** Verifica si el pasaporte está en uso por un usuario diferente al indicado. */
    public boolean existePasaporteExceptoId(String pasaporte, int excluirId) {
        if (pasaporte == null || pasaporte.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Pasaporte = ? AND ID != ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), pasaporte, excluirId);
        return !result.isEmpty() && result.get(0) > 0;
    }

    /**
     * Verifica si ya existe un usuario registrado con el correo dado.
     * @param correo correo electronico a verificar.
     * @return true si el correo ya esta en uso, false en caso contrario.
     */
    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Correo = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), correo);
        return !result.isEmpty() && result.get(0) > 0;
    }

    /**
     * Verifica si ya existe un usuario registrado con el numero de pasaporte dado.
     * Retorna false directamente si el pasaporte es nulo o esta vacio.
     * @param pasaporte numero de pasaporte a verificar.
     * @return true si el pasaporte ya esta en uso, false en caso contrario.
     */
    public boolean existePasaporte(String pasaporte) {
        if (pasaporte == null || pasaporte.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM Usuario WHERE Pasaporte = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), pasaporte);
        return !result.isEmpty() && result.get(0) > 0;
    }

    // Creacion de nuevos usuarios

    /**
     * Inserta un nuevo usuario en la base de datos con rol de cliente por defecto.
     * Si Oracle lanza ORA-00001 (restriccion unica violada) por una race condition,
     * re-verifica que campos estan duplicados y lanza {@link CamposDuplicadosException}.
     * @param correo              correo electronico del usuario.
     * @param contrasenaHasheada  contrasena ya procesada con hash.
     * @param pasaporte           numero de pasaporte del usuario, puede ser nulo.
     * @param username            nombre de usuario unico.
     * @param nombre              nombre de pila del usuario.
     * @param apellido            apellido del usuario.
     * @param telefono            numero de telefono de contacto.
     * @param fechaNacimiento     fecha de nacimiento del usuario.
     * @param ciudadId            ID de la ciudad de residencia del usuario.
     * @return ID generado por la base de datos para el nuevo usuario.
     * @throws CamposDuplicadosException si correo, pasaporte o username ya existen.
     */
    public int crearUsuario(
            String correo,
            String contrasenaHasheada,
            String pasaporte,
            String username,
            String nombre,
            String apellido,
            String telefono,
            java.sql.Date fechaNacimiento,
            int ciudadId,
            String preferenciasOferta
    ) {
        String sql = """
                INSERT INTO Usuario
                    (Correo, Contrasena, Pasaporte, Username, Nombre, Apellido,
                     Rol_ID, Telefono, Fecha_Nacimiento, Ciudad_ID, Preferencias_Oferta)
                VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?)
                """;

        try {
            return DatabaseManager.executeInsertReturnId(
                    sql, "ID",
                    correo, contrasenaHasheada, pasaporte, username,
                    nombre, apellido, telefono, fechaNacimiento, ciudadId, preferenciasOferta
            );
        } catch (DataAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.sql.SQLException sqlEx && sqlEx.getErrorCode() == 1) {
                UsuarioValidacionResponseDTO campos = new UsuarioValidacionResponseDTO(
                        existeUsername(username),
                        existeCorreo(correo),
                        existePasaporte(pasaporte)
                );
                throw new CamposDuplicadosException(campos);
            }
            throw e;
        }
    }

    // Consulta del perfil completo del usuario

    /**
     * Retorna el perfil completo de un usuario incluyendo su ciudad y pais.
     * @param usuarioId ID del usuario a consultar.
     * @return DTO con los datos del perfil, o null si el usuario no existe.
     */
    public UsuarioPerfilResponseDTO obtenerPerfil(int usuarioId) {
        String sql = """
                SELECT u.ID, u.Username, u.Correo, u.Pasaporte, u.Nombre, u.Apellido,
                       u.Telefono, u.Fecha_Nacimiento, u.Rol_ID,
                       u.Preferencias_Oferta,
                       c.Nombre AS Ciudad, p.Nombre AS Pais
                FROM   Usuario u
                LEFT JOIN Ciudad c ON u.Ciudad_ID = c.ID
                LEFT JOIN Pais   p ON c.Pais_ID   = p.ID
                WHERE  u.ID = ?
                """;

        // Mapea la fila del resultado al DTO de perfil, manejando fecha nula
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
            dto.setPreferenciasOferta(rs.getString("Preferencias_Oferta"));
            return dto;
        }, usuarioId);

        return result.isEmpty() ? null : result.get(0);
    }

    // Nacionalidades asociadas al usuario

    /**
     * Retorna los nombres de las nacionalidades registradas para un usuario.
     * @param usuarioId ID del usuario a consultar.
     * @return lista de nombres de nacionalidades del usuario.
     */
    public List<String> obtenerNacionalidades(int usuarioId) {
        String sql = """
                SELECT n.Nombre
                FROM   UsuarioNacionalidad un
                JOIN   Nacionalidad n ON un.Nacionalidad_ID = n.ID
                WHERE  un.Usuario_ID = ?
                """;

        return DatabaseManager.executeQuery(sql, rs -> rs.getString("Nombre"), usuarioId);
    }

    // Actualizacion de datos de contacto

    /**
     * Actualiza el numero de telefono de un usuario.
     * @param usuarioId ID del usuario a modificar.
     * @param telefono  nuevo numero de telefono.
     */
    public void actualizarTelefono(int usuarioId, String telefono) {
        String sql = "UPDATE Usuario SET Telefono = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, telefono, usuarioId);
    }

    // Gestion de contrasena

    /**
     * Retorna la contrasena hasheada almacenada para un usuario.
     * @param usuarioId ID del usuario a consultar.
     * @return contrasena hasheada, o null si el usuario no existe.
     */
    public String obtenerContrasena(int usuarioId) {
        String sql = "SELECT Contrasena FROM Usuario WHERE ID = ?";
        List<String> result = DatabaseManager.executeQuery(sql, rs -> rs.getString("Contrasena"), usuarioId);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Actualiza la contrasena de un usuario con el nuevo hash proporcionado.
     * @param usuarioId          ID del usuario a modificar.
     * @param contrasenaHasheada nueva contrasena ya procesada con hash.
     */
    public void actualizarContrasena(int usuarioId, String contrasenaHasheada) {
        String sql = "UPDATE Usuario SET Contrasena = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, contrasenaHasheada, usuarioId);
    }

    // Operaciones administrativas

    /**
     * Retorna todos los usuarios del sistema con su rol, ciudad y pais asignados.
     * @return lista de DTOs con la informacion administrativa de cada usuario.
     */
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

        // Mapea cada fila al DTO administrativo, manejando fecha nula
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

    /**
     * Actualiza el rol asignado a un usuario.
     * @param usuarioId  ID del usuario a modificar.
     * @param nuevoRolId ID del nuevo rol a asignar.
     */
    public void actualizarRol(int usuarioId, int nuevoRolId) {
        String sql = "UPDATE Usuario SET Rol_ID = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, nuevoRolId, usuarioId);
    }

    // ── Actualizaciones de perfil editables ────────────────────────────────────

    /** Actualiza nombre, apellido y fecha de nacimiento del usuario. */
    public void actualizarDatosPersonales(int usuarioId, String nombre, String apellido, java.sql.Date fechaNacimiento) {
        String sql = "UPDATE Usuario SET Nombre = ?, Apellido = ?, Fecha_Nacimiento = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, nombre, apellido, fechaNacimiento, usuarioId);
    }

    /** Actualiza el username del usuario. */
    public void actualizarUsername(int usuarioId, String username) {
        String sql = "UPDATE Usuario SET Username = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, username, usuarioId);
    }

    /** Actualiza el correo del usuario. */
    public void actualizarCorreo(int usuarioId, String correo) {
        String sql = "UPDATE Usuario SET Correo = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, correo, usuarioId);
    }

    /** Actualiza el pasaporte del usuario. */
    public void actualizarPasaporte(int usuarioId, String pasaporte) {
        String sql = "UPDATE Usuario SET Pasaporte = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, pasaporte, usuarioId);
    }

    /** Actualiza la ciudad del usuario. */
    public void actualizarCiudad(int usuarioId, int ciudadId) {
        String sql = "UPDATE Usuario SET Ciudad_ID = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, ciudadId, usuarioId);
    }

    /** Actualiza las preferencias de ofertas del usuario (JSON string o null). */
    public void actualizarPreferencias(int usuarioId, String preferenciasOferta) {
        String sql = "UPDATE Usuario SET Preferencias_Oferta = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, preferenciasOferta, usuarioId);
    }
}