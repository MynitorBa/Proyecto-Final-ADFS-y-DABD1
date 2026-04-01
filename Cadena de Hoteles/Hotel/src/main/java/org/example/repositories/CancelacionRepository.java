package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class CancelacionRepository {

    // obtenemos la reservacion
    public Object[] obtenerReservacionParaCancelar(int reservacionId, int usuarioId) {
        String sql = "SELECT r.ID, r.EstadoID, er.Estado " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "WHERE r.ID = ? AND r.Usuario_ID = ?";

        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getInt("EstadoID"),
                rs.getString("Estado")
        }, reservacionId, usuarioId);

        return result.isEmpty() ? null : result.get(0);
    }

    //obtenemos la fecha más reciente de las habitaciones
    public java.sql.Date obtenerFechaCheckInMasReciente(int reservacionId) {
        String sql = "SELECT MIN(FechaCheckIn) AS FechaMin " +
                "FROM DetallesReservacion WHERE ReservacionID = ?";

        List<java.sql.Date> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getDate("FechaMin"), reservacionId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    // Ahora recibe el motivo de cancelación
    public void cancelarReservacion(int reservacionId, String motivoCancelacion) {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = 4, Fecha_Cancelacion = SYSDATE, Motivo_Cancelacion = ? " +
                "WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, motivoCancelacion, reservacionId);
    }

    public Object[] obtenerReservacionAgenciaParaCancelar(int reservacionId, int agenciaId) {
        String sql = "SELECT r.ID, r.EstadoID, er.Estado " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "JOIN Agencia a ON a.usuariowebis_id = r.Usuario_ID " +  // ← join por usuario web
                "WHERE r.ID = ? AND a.ID = ?";

        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getInt("EstadoID"),
                rs.getString("Estado")
        }, reservacionId, agenciaId);

        return result.isEmpty() ? null : result.get(0);
    }
}