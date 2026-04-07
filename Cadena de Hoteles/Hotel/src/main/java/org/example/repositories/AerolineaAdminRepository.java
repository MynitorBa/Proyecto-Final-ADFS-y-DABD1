package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AerolineaAdminDTO;
import org.example.dtos.CrearAerolineaAdminRequestDTO;
import org.example.dtos.EditarAerolineaRequestDTO;
import org.example.dtos.UsuarioWebserviceLibreDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository para la gestion de aerolineas aliadas desde el panel de administracion.
 * Cubre operaciones de listado, creacion y edicion para el rol Administrador (rol 2).
 * Tambien expone la consulta de usuarios webservice libres (sin entidad asignada).
 */
public class AerolineaAdminRepository {

    /**
     * Convierte una fila del ResultSet en un AerolineaAdminDTO.
     * Resuelve el nombre del estado localmente sin depender del JOIN con EstadoAliado.
     * @param rs fila activa del ResultSet.
     * @return instancia de AerolineaAdminDTO con los datos mapeados.
     * @throws SQLException si ocurre un error al leer alguna columna.
     */
    private AerolineaAdminDTO mapRow(ResultSet rs) throws SQLException {
        AerolineaAdminDTO dto = new AerolineaAdminDTO();
        dto.setId(rs.getInt("ID"));
        dto.setNombre(rs.getString("NOMBRE"));
        dto.setUsuarioWebis(rs.getInt("USUARIOWEBIS"));
        // Username puede ser null si el usuario fue eliminado
        dto.setUsuarioUsername(rs.getString("USERNAME"));
        dto.setPorcentajeDescuento(rs.getDouble("PORCENTAJEDESCUENTO"));
        int estadoId = rs.getInt("ESTADOID");
        dto.setEstadoId(estadoId);
        // Resuelve el nombre del estado localmente sin depender de EstadoAliado
        dto.setEstado(estadoId == 1 ? "Activo" : "Inactivo");
        dto.setUrl(rs.getString("URL"));
        dto.setUrlParaUsuario(rs.getString("URLPARAUSUARIO"));
        return dto;
    }

    /**
     * Retorna todas las aerolineas aliadas registradas en el sistema,
     * incluyendo el username del usuario webservice propietario.
     * @return lista completa de aerolineas con datos de su usuario webservice.
     */
    public List<AerolineaAdminDTO> listarTodas() {
        // LEFT JOIN con Usuario para obtener username; LEFT JOIN por si el usuario fue eliminado
        String sql = "SELECT a.ID, a.Nombre, a.UsuarioWebis, " +
                "NVL(u.Username, '—') AS Username, " +
                "a.PorcentajeDescuento, a.EstadoID, a.URL, a.URLParaUsuario " +
                "FROM AerolineaAliado a " +
                "LEFT JOIN Usuario u ON a.UsuarioWebis = u.ID " +
                "ORDER BY a.ID";
        return DatabaseManager.executeQuery(sql, rs -> mapRow(rs));
    }

    /**
     * Crea una nueva aerolinea aliada asignada al usuario webservice indicado.
     * Valida que los campos obligatorios esten presentes y que el usuario no tenga
     * ya una agencia o aerolinea registrada, ya que solo se permite una entidad por usuario.
     * @param req datos de la nueva aerolinea incluyendo el ID del usuario webservice.
     * @return AerolineaAdminDTO con los datos de la aerolinea recien creada.
     * @throws IllegalArgumentException si algun campo es invalido o el usuario ya tiene una entidad.
     */
    public AerolineaAdminDTO crear(CrearAerolineaAdminRequestDTO req) {
        if (req.getNombre() == null || req.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la aerolinea es obligatorio");
        if (req.getUrl() == null || req.getUrl().isBlank())
            throw new IllegalArgumentException("La URL del sistema externo es obligatoria");
        if (req.getUrlParaUsuario() == null || req.getUrlParaUsuario().isBlank())
            throw new IllegalArgumentException("La URL para el usuario final es obligatoria");
        if (req.getUsuarioWebisId() <= 0)
            throw new IllegalArgumentException("Debe seleccionar un usuario webservice valido");

        // Verifica que el usuario no tenga ya una aerolinea registrada
        String checkAerolinea = "SELECT COUNT(*) AS C FROM AerolineaAliado WHERE UsuarioWebis = ?";
        List<Integer> existeAe = DatabaseManager.executeQuery(checkAerolinea, rs -> rs.getInt("C"), req.getUsuarioWebisId());
        if (!existeAe.isEmpty() && existeAe.get(0) > 0)
            throw new IllegalArgumentException("El usuario ya tiene una aerolinea registrada. Solo se permite una entidad por usuario webservice.");

        // Verifica que el usuario no tenga ya una agencia registrada
        String checkAgencia = "SELECT COUNT(*) AS C FROM Agencia WHERE UsuarioWebis_ID = ?";
        List<Integer> existeAg = DatabaseManager.executeQuery(checkAgencia, rs -> rs.getInt("C"), req.getUsuarioWebisId());
        if (!existeAg.isEmpty() && existeAg.get(0) > 0)
            throw new IllegalArgumentException("El usuario ya tiene una agencia registrada. Solo se permite una entidad por usuario webservice.");

        // El descuento inicia en 0; TOKENHASH recibe '1' como valor inicial ya que no puede ser NULL;
        // EstadoID se resuelve dinamicamente desde EstadoAliado para no depender de un ID fijo
        String sql = "INSERT INTO AerolineaAliado " +
                "(Nombre, UsuarioWebis, PorcentajeDescuento, EstadoID, URL, URLParaUsuario, TokenHASH) " +
                "VALUES (?, ?, 0, " +
                "(SELECT ID FROM EstadoAliado WHERE LOWER(TRIM(Estado)) = 'activo' AND ROWNUM = 1), " +
                "?, ?, '1')";
        int nuevoId = DatabaseManager.executeInsertReturnId(sql, "ID",
                req.getNombre().trim(),
                req.getUsuarioWebisId(),
                req.getUrl().trim(),
                req.getUrlParaUsuario().trim()
        );

        // Obtiene el username del usuario asignado para incluirlo en el DTO de respuesta
        String userSql = "SELECT Username FROM Usuario WHERE ID = ?";
        List<String> usernames = DatabaseManager.executeQuery(userSql, rs -> rs.getString("USERNAME"), req.getUsuarioWebisId());
        String username = usernames.isEmpty() ? "—" : usernames.get(0);

        // Construye y retorna el DTO con los datos insertados
        AerolineaAdminDTO dto = new AerolineaAdminDTO();
        dto.setId(nuevoId);
        dto.setNombre(req.getNombre().trim());
        dto.setUsuarioWebis(req.getUsuarioWebisId());
        dto.setUsuarioUsername(username);
        dto.setPorcentajeDescuento(0);
        dto.setEstadoId(1);
        dto.setEstado("Activo");
        dto.setUrl(req.getUrl().trim());
        dto.setUrlParaUsuario(req.getUrlParaUsuario().trim());
        return dto;
    }

    /**
     * Actualiza los datos de una aerolinea existente desde el panel de administracion.
     * Valida nombre, URLs, porcentaje de descuento y estado antes de aplicar los cambios.
     * @param aerolineaId ID de la aerolinea a editar.
     * @param req         datos actualizados de la aerolinea.
     * @throws IllegalArgumentException si algun campo es invalido.
     */
    public void editar(int aerolineaId, EditarAerolineaRequestDTO req) {
        if (req.getNombre() == null || req.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la aerolinea es obligatorio");
        if (req.getUrl() == null || req.getUrl().isBlank())
            throw new IllegalArgumentException("La URL del sistema externo es obligatoria");
        if (req.getUrlParaUsuario() == null || req.getUrlParaUsuario().isBlank())
            throw new IllegalArgumentException("La URL para el usuario final es obligatoria");
        if (req.getPorcentajeDescuento() < 0 || req.getPorcentajeDescuento() > 100)
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        // EstadoAliado: 1=Activo, 2=Inactivo
        if (req.getEstadoId() != 1 && req.getEstadoId() != 2)
            throw new IllegalArgumentException("Estado invalido. Use 1 (Activo) o 2 (Inactivo)");

        DatabaseManager.executeUpdate(
                "UPDATE AerolineaAliado SET Nombre=?, URL=?, URLParaUsuario=?, PorcentajeDescuento=?, EstadoID=? WHERE ID=?",
                req.getNombre().trim(),
                req.getUrl().trim(),
                req.getUrlParaUsuario().trim(),
                req.getPorcentajeDescuento(),
                req.getEstadoId(),
                aerolineaId
        );
    }

    /**
     * Retorna los usuarios con rol Webservice (rol 3) que no tienen ninguna entidad
     * registrada (ni agencia ni aerolinea). Se usan para poblar el selector al crear
     * una entidad desde el panel de administracion.
     * @return lista de UsuarioWebserviceLibreDTO disponibles para asignacion.
     */
    public List<UsuarioWebserviceLibreDTO> listarWebserviceLibres() {
        // Excluye usuarios que ya tienen agencia o aerolinea asignada
        String sql = "SELECT u.ID, u.Username " +
                "FROM Usuario u " +
                "WHERE u.Rol_ID = 3 " +
                "AND u.ID NOT IN (SELECT UsuarioWebis_ID FROM Agencia WHERE UsuarioWebis_ID IS NOT NULL) " +
                "AND u.ID NOT IN (SELECT UsuarioWebis FROM AerolineaAliado WHERE UsuarioWebis IS NOT NULL) " +
                "ORDER BY u.Username";
        return DatabaseManager.executeQuery(sql, rs -> {
            UsuarioWebserviceLibreDTO dto = new UsuarioWebserviceLibreDTO();
            dto.setId(rs.getInt("ID"));
            dto.setUsername(rs.getString("USERNAME"));
            return dto;
        });
    }
}