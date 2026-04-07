package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AerolineaWebserviceDTO;
import org.example.dtos.CrearAerolineaRequestDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository para la gestion de aerolineas aliadas desde el portal webservice.
 * Cubre operaciones de consulta, creacion y cambio de estado para el usuario webservice dueno.
 * El token de autenticacion no se gestiona aqui; se asigna automaticamente al conectar.
 */
public class AerolineaWebserviceRepository {

    /**
     * Convierte una fila del ResultSet en un objeto AerolineaWebserviceDTO.
     * No depende de la tabla EstadoAliado; el nombre del estado se resuelve
     * localmente a partir del EstadoID para mayor robustez.
     * @param rs fila activa del ResultSet con los campos de la aerolinea.
     * @return instancia de AerolineaWebserviceDTO con los datos mapeados.
     * @throws SQLException si ocurre un error al leer alguna columna del ResultSet.
     */
    private AerolineaWebserviceDTO mapRow(ResultSet rs) throws SQLException {
        AerolineaWebserviceDTO dto = new AerolineaWebserviceDTO();
        dto.setId(rs.getInt("ID"));
        dto.setNombre(rs.getString("NOMBRE"));
        dto.setUsuarioWebis(rs.getInt("USUARIOWEBIS"));
        dto.setPorcentajeDescuento(rs.getDouble("PORCENTAJEDESCUENTO"));
        int estadoId = rs.getInt("ESTADOID");
        dto.setEstadoId(estadoId);
        // Resuelve el nombre del estado localmente sin depender del JOIN con EstadoAliado
        dto.setEstado(estadoId == 1 ? "Activo" : "Cerrado");
        // Mapea las dos URLs de la aerolinea
        dto.setUrl(rs.getString("URL"));
        dto.setUrlParaUsuario(rs.getString("URLPARAUSUARIO"));
        return dto;
    }

    /**
     * Retorna las aerolineas aliadas asociadas a un usuario webservice especifico.
     * La consulta no realiza JOIN con EstadoAliado para evitar dependencia del ID de estado.
     * @param usuarioId ID del usuario webservice propietario de las aerolineas.
     * @return lista de AerolineaWebserviceDTO pertenecientes al usuario.
     */
    public List<AerolineaWebserviceDTO> listarPorUsuario(int usuarioId) {
        // Consulta directa sin JOIN para evitar fallo si EstadoID no existe en EstadoAliado
        String sql = "SELECT ID, Nombre, UsuarioWebis, PorcentajeDescuento, EstadoID, URL, URLParaUsuario " +
                "FROM AerolineaAliado " +
                "WHERE UsuarioWebis = ? " +
                "ORDER BY ID";
        return DatabaseManager.executeQuery(sql, rs -> mapRow(rs), usuarioId);
    }

    /**
     * Crea una nueva aerolinea aliada vinculada al usuario webservice indicado.
     * Valida que los campos obligatorios esten presentes y que el usuario
     * no tenga ya una aerolinea ni una agencia registrada,
     * ya que solo se permite una entidad por usuario webservice.
     * @param usuarioId ID del usuario webservice que sera propietario de la aerolinea.
     * @param req       datos de la nueva aerolinea (nombre, URL del sistema y URL para usuario).
     * @return AerolineaWebserviceDTO con los datos de la aerolinea recien creada.
     * @throws IllegalArgumentException si algun campo obligatorio esta vacio,
     *                                  o si el usuario ya tiene una aerolinea o una agencia registrada.
     */
    public AerolineaWebserviceDTO crear(int usuarioId, CrearAerolineaRequestDTO req) {
        if (req.getNombre() == null || req.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre de la aerolinea es obligatorio");
        if (req.getUrl() == null || req.getUrl().isBlank())
            throw new IllegalArgumentException("La URL del sistema externo es obligatoria");
        if (req.getUrlParaUsuario() == null || req.getUrlParaUsuario().isBlank())
            throw new IllegalArgumentException("La URL para el usuario final es obligatoria");

        // Verifica que el usuario no tenga ya una aerolinea registrada
        String checkAerolinea = "SELECT COUNT(*) AS C FROM AerolineaAliado WHERE UsuarioWebis = ?";
        List<Integer> existeAerolinea = DatabaseManager.executeQuery(checkAerolinea, rs -> rs.getInt("C"), usuarioId);
        if (!existeAerolinea.isEmpty() && existeAerolinea.get(0) > 0)
            throw new IllegalArgumentException("Ya tienes una aerolinea registrada. Solo se permite una entidad por usuario webservice.");

        // Verifica que el usuario no tenga ya una agencia registrada
        String checkAgencia = "SELECT COUNT(*) AS C FROM Agencia WHERE UsuarioWebis_ID = ?";
        List<Integer> existeAgencia = DatabaseManager.executeQuery(checkAgencia, rs -> rs.getInt("C"), usuarioId);
        if (!existeAgencia.isEmpty() && existeAgencia.get(0) > 0)
            throw new IllegalArgumentException("Ya tienes una agencia registrada. Solo se permite una entidad por usuario webservice.");

        // El descuento inicia en 0; TOKENHASH recibe '1' como valor inicial ya que no
        // puede ser NULL en Oracle; sera reemplazado al establecer la primera conexion.
        // EstadoID se resuelve dinamicamente desde EstadoAliado para no depender de un ID fijo
        String sql = "INSERT INTO AerolineaAliado " +
                "(Nombre, UsuarioWebis, PorcentajeDescuento, EstadoID, URL, URLParaUsuario, TokenHASH) " +
                "VALUES (?, ?, 0, " +
                "(SELECT ID FROM EstadoAliado WHERE LOWER(TRIM(Estado)) = 'activo' AND ROWNUM = 1), " +
                "?, ?, '1')";
        int nuevoId = DatabaseManager.executeInsertReturnId(sql, "ID",
                req.getNombre().trim(),
                usuarioId,
                req.getUrl().trim(),
                req.getUrlParaUsuario().trim()
        );

        // Construye y retorna el DTO con los datos insertados
        AerolineaWebserviceDTO dto = new AerolineaWebserviceDTO();
        dto.setId(nuevoId);
        dto.setNombre(req.getNombre().trim());
        dto.setUsuarioWebis(usuarioId);
        dto.setPorcentajeDescuento(0);
        dto.setEstadoId(1);
        dto.setEstado("Activo");
        dto.setUrl(req.getUrl().trim());
        dto.setUrlParaUsuario(req.getUrlParaUsuario().trim());
        return dto;
    }

    /**
     * Cambia el estado de una aerolinea verificando que pertenezca al usuario webservice indicado.
     * @param aerolineaId  ID de la aerolinea a modificar.
     * @param usuarioId    ID del usuario webservice propietario de la aerolinea.
     * @param nuevoEstadoId nuevo estado a asignar.
     * @throws IllegalArgumentException si la aerolinea no existe o no pertenece al usuario.
     */
    public void cambiarEstado(int aerolineaId, int usuarioId, int nuevoEstadoId) {
        // Verifica que la aerolinea exista y pertenezca al usuario antes de modificar
        String check = "SELECT COUNT(*) AS C FROM AerolineaAliado WHERE ID=? AND UsuarioWebis=?";
        List<Integer> res = DatabaseManager.executeQuery(check,
                rs -> rs.getInt("C"), aerolineaId, usuarioId);
        if (res.isEmpty() || res.get(0) == 0)
            throw new IllegalArgumentException("Aerolinea no encontrada o no pertenece a este usuario");

        DatabaseManager.executeUpdate(
                "UPDATE AerolineaAliado SET EstadoID=? WHERE ID=?",
                nuevoEstadoId, aerolineaId
        );
    }
}