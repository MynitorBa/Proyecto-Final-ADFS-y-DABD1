package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ComentarioResponseDTO;

import java.util.List;

public class ComentarioRepository {
    public boolean existeComentarioConResena(int usuarioId, int hotelId) {
        String sql = "SELECT COUNT(*) AS total FROM Comentario " +
                "WHERE Usuario_ID = ? AND HotelID = ? AND Resena IS NOT NULL";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("total"), usuarioId, hotelId
        );
        return !result.isEmpty() && result.get(0) > 0;
    }

    public int crearComentario(int usuarioId, int hotelId, Integer comentarioPadreId,
                               Integer resena, String contenido) {
        String sql = "INSERT INTO Comentario " +
                "(Usuario_ID, HotelID, ComentarioPadreID, Resena, Contenido, Fecha, Downs) " +
                "VALUES (?, ?, ?, ?, ?, SYSDATE, 0)";

        return DatabaseManager.executeInsertReturnId(sql, "ID",
                usuarioId, hotelId, comentarioPadreId, resena, contenido);
    }

    public void actualizarRatingHotel(int hotelId) {
        String sql = "UPDATE Hotel " +
                "SET Rating = (" +
                "  SELECT AVG(Resena) FROM Comentario " +
                "  WHERE HotelID = ? AND Resena IS NOT NULL" +
                ") WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, hotelId, hotelId);
    }

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
            dto.setComentarioPadreId(rs.getObject("ComentarioPadreID") != null ? rs.getInt("ComentarioPadreID") : null);
            dto.setResena(rs.getObject("Resena") != null ? rs.getInt("Resena") : null);
            dto.setContenido(rs.getString("Contenido"));
            dto.setFecha(rs.getDate("Fecha").toString());
            dto.setDowns(rs.getInt("Downs"));
            return dto;
        }, comentarioId);

        return result.isEmpty() ? null : result.get(0);
    }

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
            dto.setComentarioPadreId(rs.getObject("ComentarioPadreID") != null ? rs.getInt("ComentarioPadreID") : null);
            dto.setResena(rs.getObject("Resena") != null ? rs.getInt("Resena") : null);
            dto.setContenido(rs.getString("Contenido"));
            dto.setFecha(rs.getDate("Fecha").toString());
            dto.setDowns(rs.getInt("Downs"));
            return dto;
        }, hotelId);
    }
}