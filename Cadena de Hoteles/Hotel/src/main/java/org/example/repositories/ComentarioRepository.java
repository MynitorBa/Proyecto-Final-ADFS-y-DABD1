package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ComentarioResponseDTO;

import java.util.List;

/**
 * Repository para la gestion de comentarios y resenas de hoteles.
 * Cubre la creacion de comentarios, validacion de resenas previas y actualizacion del rating del hotel.
 */
public class ComentarioRepository {

    /**
     * Verifica si un usuario ya tiene un comentario con resena registrado para un hotel especifico.
     * @param usuarioId ID del usuario a verificar.
     * @param hotelId   ID del hotel a verificar.
     * @return true si ya existe un comentario con resena del usuario en ese hotel, false en caso contrario.
     */
    public boolean existeComentarioConResena(int usuarioId, int hotelId) {
        String sql = "SELECT COUNT(*) AS total FROM Comentario " +
                "WHERE Usuario_ID = ? AND HotelID = ? AND Resena IS NOT NULL";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("total"), usuarioId, hotelId
        );
        return !result.isEmpty() && result.get(0) > 0;
    }

    /**
     * Inserta un nuevo comentario en la base de datos y retorna el ID generado.
     * El comentario puede ser una resena principal o una respuesta a otro comentario si se indica el padre.
     * @param usuarioId         ID del usuario que publica el comentario.
     * @param hotelId           ID del hotel al que pertenece el comentario.
     * @param comentarioPadreId ID del comentario padre si es una respuesta, o null si es raiz.
     * @param resena            puntuacion de la resena (1-5), o null si es solo un comentario.
     * @param contenido         texto del comentario.
     * @return ID del comentario recien insertado.
     */
    public int crearComentario(int usuarioId, int hotelId, Integer comentarioPadreId,
                               Integer resena, String contenido) {
        String sql = "INSERT INTO Comentario " +
                "(Usuario_ID, HotelID, ComentarioPadreID, Resena, Contenido, Fecha, Downs) " +
                "VALUES (?, ?, ?, ?, ?, SYSDATE, 0)";

        return DatabaseManager.executeInsertReturnId(sql, "ID",
                usuarioId, hotelId, comentarioPadreId, resena, contenido);
    }

    /**
     * Recalcula y actualiza el rating de un hotel en base al promedio de sus resenas activas.
     * @param hotelId ID del hotel cuyo rating se debe actualizar.
     */
    public void actualizarRatingHotel(int hotelId) {
        String sql = "UPDATE Hotel " +
                "SET Rating = (" +
                "  SELECT AVG(Resena) FROM Comentario " +
                "  WHERE HotelID = ? AND Resena IS NOT NULL" +
                ") WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, hotelId, hotelId);
    }

    /**
     * Retorna un comentario especifico con los datos del usuario que lo publico.
     * @param comentarioId ID del comentario a buscar.
     * @return ComentarioResponseDTO con los datos del comentario, o null si no existe.
     */
    public ComentarioResponseDTO obtenerComentario(int comentarioId) {
        String sql = "SELECT c.ID, c.Usuario_ID, u.Username, c.HotelID, " +
                "c.ComentarioPadreID, c.Resena, c.Contenido, c.Fecha, c.Downs " +
                "FROM Comentario c " +
                "JOIN Usuario u ON c.Usuario_ID = u.ID " +
                "WHERE c.ID = ?";

        List<ComentarioResponseDTO> result = DatabaseManager.executeQuery(sql, rs -> {
            ComentarioResponseDTO dto = new ComentarioResponseDTO();
            dto.setId(rs.getInt("ID"));
            dto.setUsuarioId(rs.getInt("Usuario_ID"));
            dto.setUsername(rs.getString("Username"));
            dto.setHotelId(rs.getInt("HotelID"));
            // ComentarioPadreID y Resena pueden ser null, se verifica antes de leer
            dto.setComentarioPadreId(rs.getObject("ComentarioPadreID") != null ? rs.getInt("ComentarioPadreID") : null);
            dto.setResena(rs.getObject("Resena") != null ? rs.getInt("Resena") : null);
            dto.setContenido(rs.getString("Contenido"));
            dto.setFecha(rs.getDate("Fecha").toString());
            dto.setDowns(rs.getInt("Downs"));
            return dto;
        }, comentarioId);

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Retorna todos los comentarios publicados por un usuario, ordenados por fecha descendente.
     * @param usuarioId ID del usuario del que se quieren obtener los comentarios.
     * @return lista de ComentarioResponseDTO con los comentarios del usuario.
     */
    public List<ComentarioResponseDTO> obtenerComentariosPorUsuario(int usuarioId) {
        String sql = "SELECT c.ID, c.Usuario_ID, u.Username, c.HotelID, " +
                "c.ComentarioPadreID, c.Resena, c.Contenido, c.Fecha, c.Downs " +
                "FROM Comentario c " +
                "JOIN Usuario u ON c.Usuario_ID = u.ID " +
                "WHERE c.Usuario_ID = ? " +
                "ORDER BY c.Fecha DESC";

        return DatabaseManager.executeQuery(sql, rs -> {
            ComentarioResponseDTO dto = new ComentarioResponseDTO();
            dto.setId(rs.getInt("ID"));
            dto.setUsuarioId(rs.getInt("Usuario_ID"));
            dto.setUsername(rs.getString("Username"));
            dto.setHotelId(rs.getInt("HotelID"));
            // ComentarioPadreID y Resena pueden ser null, se verifica antes de leer
            dto.setComentarioPadreId(rs.getObject("ComentarioPadreID") != null ? rs.getInt("ComentarioPadreID") : null);
            dto.setResena(rs.getObject("Resena") != null ? rs.getInt("Resena") : null);
            dto.setContenido(rs.getString("Contenido"));
            dto.setFecha(rs.getDate("Fecha").toString());
            dto.setDowns(rs.getInt("Downs"));
            return dto;
        }, usuarioId);
    }

    /**
     * Retorna todos los comentarios registrados para un hotel, ordenados por fecha descendente.
     * @param hotelId ID del hotel del que se quieren obtener los comentarios.
     * @return lista de ComentarioResponseDTO con los comentarios del hotel.
     */
    public List<ComentarioResponseDTO> obtenerComentariosPorHotel(int hotelId) {
        String sql = "SELECT c.ID, c.Usuario_ID, u.Username, c.HotelID, " +
                "c.ComentarioPadreID, c.Resena, c.Contenido, c.Fecha, c.Downs " +
                "FROM Comentario c " +
                "JOIN Usuario u ON c.Usuario_ID = u.ID " +
                "WHERE c.HotelID = ? " +
                "ORDER BY c.Fecha DESC";

        return DatabaseManager.executeQuery(sql, rs -> {
            ComentarioResponseDTO dto = new ComentarioResponseDTO();
            dto.setId(rs.getInt("ID"));
            dto.setUsuarioId(rs.getInt("Usuario_ID"));
            dto.setUsername(rs.getString("Username"));
            dto.setHotelId(rs.getInt("HotelID"));
            // ComentarioPadreID y Resena pueden ser null, se verifica antes de leer
            dto.setComentarioPadreId(rs.getObject("ComentarioPadreID") != null ? rs.getInt("ComentarioPadreID") : null);
            dto.setResena(rs.getObject("Resena") != null ? rs.getInt("Resena") : null);
            dto.setContenido(rs.getString("Contenido"));
            dto.setFecha(rs.getDate("Fecha").toString());
            dto.setDowns(rs.getInt("Downs"));
            return dto;
        }, hotelId);
    }
}