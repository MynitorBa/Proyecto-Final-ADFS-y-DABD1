package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.DownResponseDTO;

import java.util.List;

public class DownsRepository {

    //Obtener todos los downs de un usuario

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

    // Obtener downs de un usuario filtrados por hotel

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

    // Verificar si el usuario ya tiene un down en este comentario

    public Integer obtenerValorDown(int usuarioId, int comentarioId) {
        String sql = "SELECT Valor FROM Downs WHERE Usuario_ID = ? AND Comentario_ID = ?";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("Valor"), usuarioId, comentarioId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    // Insertar down

    public void insertarDown(int usuarioId, int comentarioId, int valor) {
        String sql = "INSERT INTO Downs (Usuario_ID, Comentario_ID, Valor, Fecha) " +
                "VALUES (?, ?, ?, SYSDATE)";
        DatabaseManager.executeUpdate(sql, usuarioId, comentarioId, valor);
    }

    //  Eliminar down

    public void eliminarDown(int usuarioId, int comentarioId) {
        String sql = "DELETE FROM Downs WHERE Usuario_ID = ? AND Comentario_ID = ?";
        DatabaseManager.executeUpdate(sql, usuarioId, comentarioId);
    }

    //--- Sumar al contador de downs del comentario ---------------------

    public void actualizarContadorDown(int comentarioId, int delta) {
        String sql = "UPDATE Comentario SET Downs = Downs + ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, delta, comentarioId);
    }
}