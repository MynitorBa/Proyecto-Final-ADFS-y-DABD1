package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AgenciaDTO;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;
import org.example.dtos.AgenciaIdentidad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository para la gestion de agencias de viaje.
 * Cubre operaciones de consulta, creacion, edicion, cambio de estado y eliminacion,
 * tanto para el panel de administracion como para usuarios webservice.
 */
public class AgenciaRepository {

    /**
     * Convierte una fila del ResultSet en un objeto AgenciaDTO.
     * @param rs fila activa del ResultSet con los campos de la agencia.
     * @return instancia de AgenciaDTO con los datos mapeados.
     * @throws SQLException si ocurre un error al leer alguna columna del ResultSet.
     */
    private AgenciaDTO mapRow(ResultSet rs) throws SQLException {
        AgenciaDTO dto = new AgenciaDTO();
        dto.setId(rs.getInt("ID"));
        dto.setNombre(rs.getString("Nombre"));
        dto.setCorreo(rs.getString("Correo"));
        dto.setUsuarioWebisId(rs.getInt("UsuarioWebis_ID"));
        dto.setPorcentajeDescuento(rs.getDouble("PorcentajeDescuento"));
        dto.setEstadoId(rs.getInt("EstadoID"));
        dto.setEstado(rs.getString("Estado"));
        return dto;
    }

    /**
     * Retorna todas las agencias registradas en el sistema, ordenadas por ID.
     * @return lista de AgenciaDTO con todas las agencias.
     */
    public List<AgenciaDTO> listarTodas() {
        String sql = """
                SELECT a.ID, a.Nombre, a.Correo, a.UsuarioWebis_ID,
                       a.PorcentajeDescuento, a.EstadoID, e.Estado
                FROM   Agencia      a
                JOIN   EstadoAgencia e ON a.EstadoID = e.ID
                ORDER BY a.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> mapRow(rs));
    }

    /**
     * Retorna las agencias asociadas a un usuario webservice especifico.
     * @param usuarioId ID del usuario webservice propietario de las agencias.
     * @return lista de AgenciaDTO pertenecientes al usuario.
     */
    public List<AgenciaDTO> listarPorUsuario(int usuarioId) {
        String sql = """
                SELECT a.ID, a.Nombre, a.Correo, a.UsuarioWebis_ID,
                       a.PorcentajeDescuento, a.EstadoID, e.Estado
                FROM   Agencia      a
                JOIN   EstadoAgencia e ON a.EstadoID = e.ID
                WHERE  a.UsuarioWebis_ID = ?
                ORDER BY a.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> mapRow(rs), usuarioId);
    }

    /**
     * Crea una nueva agencia vinculada al usuario webservice indicado.
     * Valida que los campos obligatorios esten presentes y que el usuario
     * no tenga ya una agencia registrada, ya que solo se permite una por usuario.
     * @param usuarioId ID del usuario webservice que sera propietario de la agencia.
     * @param req       datos de la nueva agencia (nombre y correo).
     * @return AgenciaDTO con los datos de la agencia recien creada.
     * @throws IllegalArgumentException si el nombre o correo estan vacios, o si el usuario ya tiene una agencia.
     */
    public AgenciaDTO crear(int usuarioId, CrearAgenciaRequestDTO req) {
        if (req.getNombre() == null || req.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la agencia es obligatorio");
        if (req.getCorreo() == null || req.getCorreo().isBlank())
            throw new IllegalArgumentException("El correo de la agencia es obligatorio");

        // Verifica que el usuario no tenga ya una agencia registrada
        String checkSql = "SELECT COUNT(*) AS C FROM Agencia WHERE UsuarioWebis_ID = ?";
        List<Integer> existe = DatabaseManager.executeQuery(checkSql, rs -> rs.getInt("C"), usuarioId);
        if (!existe.isEmpty() && existe.get(0) > 0)
            throw new IllegalArgumentException("Ya tienes una agencia registrada. Solo se permite una por usuario webservice.");

        // EstadoID=1 equivale a Activa; el descuento siempre inicia en 0
        String sql = """
                INSERT INTO Agencia (Nombre, Correo, UsuarioWebis_ID, PorcentajeDescuento, EstadoID)
                VALUES (?, ?, ?, 0, 1)
                """;
        int nuevoId = DatabaseManager.executeInsertReturnId(sql, "ID",
                req.getNombre().trim(),
                req.getCorreo().trim(),
                usuarioId
        );

        // Construye y retorna el DTO con los datos insertados
        AgenciaDTO dto = new AgenciaDTO();
        dto.setId(nuevoId);
        dto.setNombre(req.getNombre().trim());
        dto.setCorreo(req.getCorreo().trim());
        dto.setUsuarioWebisId(usuarioId);
        dto.setPorcentajeDescuento(0);
        dto.setEstadoId(1);
        dto.setEstado("Activa");
        return dto;
    }

    /**
     * Actualiza los datos de una agencia existente desde el panel de administracion.
     * Valida nombre, correo, porcentaje de descuento y estado antes de aplicar los cambios.
     * @param agenciaId ID de la agencia a editar.
     * @param req       datos actualizados de la agencia.
     * @throws IllegalArgumentException si algun campo es invalido o el estado no corresponde a Activa o Inactiva.
     */
    public void editar(int agenciaId, EditarAgenciaRequestDTO req) {
        if (req.getNombre() == null || req.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la agencia es obligatorio");
        if (req.getCorreo() == null || req.getCorreo().isBlank())
            throw new IllegalArgumentException("El correo de la agencia es obligatorio");
        if (req.getPorcentajeDescuento() < 0 || req.getPorcentajeDescuento() > 100)
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        // EstadoAgencia: 1=Activa, 2=Inactiva
        if (req.getEstadoId() != 1 && req.getEstadoId() != 2)
            throw new IllegalArgumentException("Estado invalido. Use 1 (Activa) o 2 (Inactiva)");

        DatabaseManager.executeUpdate(
                "UPDATE Agencia SET Nombre=?, Correo=?, PorcentajeDescuento=?, EstadoID=? WHERE ID=?",
                req.getNombre().trim(),
                req.getCorreo().trim(),
                req.getPorcentajeDescuento(),
                req.getEstadoId(),
                agenciaId
        );
    }

    /**
     * Cambia el estado de una agencia verificando que pertenezca al usuario webservice indicado.
     * @param agenciaId    ID de la agencia a modificar.
     * @param usuarioId    ID del usuario webservice propietario de la agencia.
     * @param nuevoEstadoId nuevo estado a asignar.
     * @throws IllegalArgumentException si la agencia no existe o no pertenece al usuario.
     */
    public void cambiarEstado(int agenciaId, int usuarioId, int nuevoEstadoId) {
        // Verifica que la agencia exista y pertenezca al usuario antes de modificar
        String check = "SELECT COUNT(*) AS C FROM Agencia WHERE ID=? AND UsuarioWebis_ID=?";
        List<Integer> res = DatabaseManager.executeQuery(check,
                rs -> rs.getInt("C"), agenciaId, usuarioId);
        if (res.isEmpty() || res.get(0) == 0)
            throw new IllegalArgumentException("Agencia no encontrada o no pertenece a este usuario");

        DatabaseManager.executeUpdate(
                "UPDATE Agencia SET EstadoID=? WHERE ID=?",
                nuevoEstadoId, agenciaId
        );
    }

    /**
     * Elimina una agencia verificando que pertenezca al usuario webservice indicado.
     * @param agenciaId ID de la agencia a eliminar.
     * @param usuarioId ID del usuario webservice propietario de la agencia.
     * @throws IllegalArgumentException si la agencia no existe o no pertenece al usuario.
     */
    public void eliminar(int agenciaId, int usuarioId) {
        // Verifica que la agencia exista y pertenezca al usuario antes de eliminar
        String check = "SELECT COUNT(*) AS C FROM Agencia WHERE ID=? AND UsuarioWebis_ID=?";
        List<Integer> res = DatabaseManager.executeQuery(check,
                rs -> rs.getInt("C"), agenciaId, usuarioId);
        if (res.isEmpty() || res.get(0) == 0)
            throw new IllegalArgumentException("Agencia no encontrada o no pertenece a este usuario");

        DatabaseManager.executeUpdate("DELETE FROM Agencia WHERE ID=?", agenciaId);
    }

    /**
     * Busca el ID de una agencia a partir de su URL registrada.
     * @param urlAgencia URL unica asociada a la agencia.
     * @return ID de la agencia, o null si no se encuentra ninguna con esa URL.
     */
    public Integer obtenerAgenciaIdPorURL(String urlAgencia) {
        String sql = "SELECT ID FROM Agencia WHERE URL_Agencia = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), urlAgencia);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Guarda los tokens de entrada y salida asociados a una agencia.
     * @param agenciaId    ID de la agencia a actualizar.
     * @param tokenEntrada hash del token de entrada.
     * @param tokenSalida  hash del token de salida.
     * @return true si se actualizo al menos un registro, false si no se encontro la agencia.
     */
    public boolean guardarTokens(int agenciaId, String tokenEntrada, String tokenSalida) {
        String sql = "UPDATE Agencia SET Token_HASH_Entrada = ?, Token_HASH_Salida = ? WHERE ID = ?";
        return DatabaseManager.executeUpdate(sql, tokenEntrada, tokenSalida, agenciaId) > 0;
    }

    /**
     * Busca una agencia por su token de entrada y retorna su informacion de identidad.
     * @param token hash del token de entrada a buscar.
     * @return AgenciaIdentidad con los datos basicos de la agencia, o null si no existe.
     */
    public AgenciaIdentidad obtenerAgenciaPorToken(String token) {
        String sql = "SELECT ID, Nombre, URL_Agencia FROM Agencia WHERE Token_HASH_Entrada = ?";
        List<AgenciaIdentidad> result = DatabaseManager.executeQuery(sql, rs -> {
            AgenciaIdentidad a = new AgenciaIdentidad();
            a.setId(rs.getInt("ID"));
            a.setNombre(rs.getString("Nombre"));
            a.setUrlAgencia(rs.getString("URL_Agencia"));
            return a;
        }, token);
        return result.isEmpty() ? null : result.get(0);
    }
}