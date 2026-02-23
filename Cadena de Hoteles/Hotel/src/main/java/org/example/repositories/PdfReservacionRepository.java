package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ReservacionDetalleDTO;

import java.util.List;

public class PdfReservacionRepository {

    // -------------------------------- Obtener correo del usuario ----------------

    public String obtenerCorreoUsuario(int usuarioId) {
        String sql = "SELECT Correo FROM Usuario WHERE ID = ?";
        List<String> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getString("Correo"), usuarioId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    //-------------------- Verificar que la reservación pertenece al usuario -------------------

    public boolean perteneceAlUsuario(int reservacionId, int usuarioId) {
        String sql = "SELECT COUNT(*) AS total FROM Reservacion " +
                "WHERE ID = ? AND Usuario_ID = ?";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("total"), reservacionId, usuarioId
        );
        return !result.isEmpty() && result.get(0) > 0;
    }

    // ------------------------ Obtener datos completos de la reservación -------------------------------

    public List<ReservacionDetalleDTO> obtenerDetalles(int reservacionId) {
        String sql = "SELECT r.ID, r.No_Reservacion, r.Total, " +
                "r.Fecha_Creacion, r.Fecha_Expiracion, r.Fecha_Cancelacion, r.Motivo_Cancelacion, " +
                "er.Estado, " +
                "dr.ID AS DetalleID, dr.HabitacionID, dr.FechaCheckIn, dr.FechaCheckOut, " +
                "dr.CantidadPersonas, dr.Total AS TotalDetalle, " +
                "h.Descripcion AS DescripcionHabitacion, " +
                "t.nombre AS TipoHabitacion, " +
                "c.Tipo_de_clase AS TipoCama, " +
                "hot.ID AS HotelID, hot.Nombre AS NombreHotel " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva      er  ON r.EstadoID          = er.ID " +
                "JOIN DetallesReservacion dr  ON dr.ReservacionID   = r.ID " +
                "JOIN Habitacion          h   ON dr.HabitacionID    = h.ID " +
                "JOIN TipoHabitacion      t   ON h.TipoHabitacionID = t.ID " +
                "JOIN Cama                c   ON h.CamaID           = c.ID " +
                "JOIN Hotel               hot ON h.HotelID          = hot.ID " +
                "WHERE r.ID = ? " +
                "ORDER BY dr.ID";

        return DatabaseManager.executeQuery(sql, rs -> {
            ReservacionDetalleDTO dto = new ReservacionDetalleDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNoReservacion(rs.getString("No_Reservacion"));
            dto.setTotal(rs.getDouble("Total"));
            dto.setEstado(rs.getString("Estado"));
            dto.setFechaCreacion(rs.getTimestamp("Fecha_Creacion") != null ? rs.getTimestamp("Fecha_Creacion").toString() : null);
            dto.setFechaExpiracion(rs.getTimestamp("Fecha_Expiracion") != null ? rs.getTimestamp("Fecha_Expiracion").toString() : null);
            dto.setFechaCancelacion(rs.getDate("Fecha_Cancelacion") != null ? rs.getDate("Fecha_Cancelacion").toString() : null);
            dto.setMotivoCancelacion(rs.getString("Motivo_Cancelacion"));
            dto.setDetalleId(rs.getInt("DetalleID"));
            dto.setHabitacionId(rs.getInt("HabitacionID"));
            dto.setFechaCheckIn(rs.getDate("FechaCheckIn").toString());
            dto.setFechaCheckOut(rs.getDate("FechaCheckOut").toString());
            dto.setCantidadPersonas(rs.getInt("CantidadPersonas"));
            dto.setTotalDetalle(rs.getDouble("TotalDetalle"));
            dto.setDescripcionHabitacion(rs.getString("DescripcionHabitacion"));
            dto.setTipoHabitacion(rs.getString("TipoHabitacion"));
            dto.setTipoCama(rs.getString("TipoCama"));
            dto.setHotelId(rs.getInt("HotelID"));
            dto.setNombreHotel(rs.getString("NombreHotel"));
            return dto;
        }, reservacionId);
    }

    // -----------------------Obtener factura si existe ----------------------------------------------

    public Object[] obtenerFactura(int reservacionId) {
        String sql = "SELECT ID, Fecha, NIT, Codigo_Postal, Total " +
                "FROM Factura WHERE ReservacionID = ?";
        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getDate("Fecha") != null ? rs.getDate("Fecha").toString() : null,
                rs.getString("NIT"),
                rs.getString("Codigo_Postal"),
                rs.getDouble("Total")
        }, reservacionId);
        return result.isEmpty() ? null : result.get(0);
    }
}