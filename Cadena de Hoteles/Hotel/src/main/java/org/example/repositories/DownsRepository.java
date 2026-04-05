package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.DownResponseDTO;

import java.util.List;

/**
 * Repository para la gestion de downs (votos negativos) sobre comentarios de hoteles.
 * Permite consultar, registrar, eliminar y contabilizar downs por usuario y comentario.
 */
public class DownsRepository {

    /**
     * Retorna todos los downs registrados por un usuario, ordenados por fecha descendente.
     * @param usuarioId ID del usuario del que se quieren obtener los downs.
     * @return lista de DownResponseDTO con los datos de cada down del usuario.
     */
    public List<DownResponseDTO> obtenerDownsDeUsuario(int usuarioId) {
        String sql = "SELECT d.ID, d.Comentario_ID, d.Valor, d.Fecha, " +
                "c.HotelID, c.Contenido " +
                "FROM Downs d " +
                "JOIN Comentario c ON d.Comentario_ID = c.ID " +
                "WHERE d.Usuario_ID = ? " +
                "ORDER BY d.Fecha DESC";
        return DatabaseManager.executeQuery(sql, rs -> {
            DownResponseDTO dto = new DownResponseDTO();
            dto.setId(rs.getInt("ID"));
            dto.setComentarioId(rs.getInt("Comentario_ID"));
            dto.setValor(rs.getInt("Valor"));
            dto.setFecha(rs.getDate("Fecha").toString());
            dto.setHotelId(rs.getInt("HotelID"));
            dto.setContenidoComentario(rs.getString("Contenido"));
            return dto;
        }, usuarioId);
    }

    /**
     * Retorna los downs de un usuario filtrados por un hotel especifico, ordenados por fecha descendente.
     * @param usuarioId ID del usuario del que se quieren obtener los downs.
     * @param hotelId   ID del hotel por el que se filtra.
     * @return lista de DownResponseDTO con los downs del usuario en ese hotel.
     */
    public List<DownResponseDTO> obtenerDownsDeUsuarioPorHotel(int usuarioId, int hotelId) {
        String sql = "SELECT d.ID, d.Comentario_ID, d.Valor, d.Fecha, " +
                "c.HotelID, c.Contenido " +
                "FROM Downs d " +
                "JOIN Comentario c ON d.Comentario_ID = c.ID " +
                "WHERE d.Usuario_ID = ? AND c.HotelID = ? " +
                "ORDER BY d.Fecha DESC";
        return DatabaseManager.executeQuery(sql, rs -> {
            DownResponseDTO dto = new DownResponseDTO();
            dto.setId(rs.getInt("ID"));
            dto.setComentarioId(rs.getInt("Comentario_ID"));
            dto.setValor(rs.getInt("Valor"));
            dto.setFecha(rs.getDate("Fecha").toString());
            dto.setHotelId(rs.getInt("HotelID"));
            dto.setContenidoComentario(rs.getString("Contenido"));
            return dto;
        }, usuarioId, hotelId);
    }

    /**
     * Retorna el valor del down que un usuario tiene registrado sobre un comentario especifico.
     * Se usa para verificar si ya existe un down antes de insertar o eliminar.
     * @param usuarioId    ID del usuario a verificar.
     * @param comentarioId ID del comentario a verificar.
     * @return valor del down existente, o null si el usuario no ha marcado ese comentario.
     */
    public Integer obtenerValorDown(int usuarioId, int comentarioId) {
        String sql = "SELECT Valor FROM Downs WHERE Usuario_ID = ? AND Comentario_ID = ?";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("Valor"), usuarioId, comentarioId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Registra un nuevo down de un usuario sobre un comentario.
     * @param usuarioId    ID del usuario que registra el down.
     * @param comentarioId ID del comentario sobre el que se aplica el down.
     * @param valor        valor del down a registrar.
     */
    public void insertarDown(int usuarioId, int comentarioId, int valor) {
        String sql = "INSERT INTO Downs (Usuario_ID, Comentario_ID, Valor, Fecha) " +
                "VALUES (?, ?, ?, SYSDATE)";
        DatabaseManager.executeUpdate(sql, usuarioId, comentarioId, valor);
    }

    /**
     * Elimina el down que un usuario tiene registrado sobre un comentario.
     * @param usuarioId    ID del usuario cuyo down se eliminara.
     * @param comentarioId ID del comentario del que se quita el down.
     */
    public void eliminarDown(int usuarioId, int comentarioId) {
        String sql = "DELETE FROM Downs WHERE Usuario_ID = ? AND Comentario_ID = ?";
        DatabaseManager.executeUpdate(sql, usuarioId, comentarioId);
    }

    /**
     * Incrementa o decrementa el contador de downs de un comentario segun el delta indicado.
     * Se usa para mantener sincronizado el campo Downs del comentario tras insertar o eliminar un down.
     * @param comentarioId ID del comentario cuyo contador se actualiza.
     * @param delta        valor a sumar al contador (puede ser negativo para restar).
     */
    public void actualizarContadorDown(int comentarioId, int delta) {
        String sql = "UPDATE Comentario SET Downs = Downs + ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, delta, comentarioId);
    }
}