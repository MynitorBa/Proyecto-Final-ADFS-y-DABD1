package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ReservacionDetalleDTO;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class ReservacionRepository {

    // -------------------- Obtener precios de una habitación ---------------------------------

    public double[] obtenerPrecios(int habitacionId) {
        String sql = "SELECT Precio_por_Noche, Precio_por_Persona FROM Habitacion WHERE ID = ?";
        List<double[]> result = DatabaseManager.executeQuery(sql, rs -> new double[]{
                rs.getDouble("Precio_por_Noche"),
                rs.getDouble("Precio_por_Persona")
        }, habitacionId);

        if (result.isEmpty()) throw new RuntimeException("Habitación no encontrada: " + habitacionId);
        return result.get(0);
    }

    // -----------------------Verificar traslape ------------------------

    public boolean existeTraslape(int habitacionId, Date fechaCheckIn, Date fechaCheckOut) {
        String sql = "SELECT COUNT(*) AS total " +
                "FROM DetallesReservacion dr " +
                "JOIN Reservacion r ON dr.ReservacionID = r.ID " +
                "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "WHERE dr.HabitacionID = ? " +
                "AND LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada') " +
                "AND dr.FechaCheckIn  < ? " +
                "AND dr.FechaCheckOut > ?";

        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("total"),
                habitacionId, fechaCheckOut, fechaCheckIn
        );
        return !result.isEmpty() && result.get(0) > 0;
    }

    // ------------------- Expirar otras reservaciones pendientes del mismo usuario -----------------------

    public void expirarPendientesDeUsuario(int usuarioId, int reservacionIdExcluir) {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'expirada'), " +
                "    Fecha_Expiracion = SYSDATE " +
                "WHERE Usuario_ID = ? " +
                "AND ID != ? " +
                "AND EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'pendiente')";

        DatabaseManager.executeUpdate(sql, usuarioId, reservacionIdExcluir);
    }

    //----------------------------Crear reservación -----------------------------------

    public int crearReservacion(String noReservacion, double total, int usuarioId,
                                Timestamp fechaCreacion, Timestamp fechaExpiracion) {
        String sql = "INSERT INTO Reservacion " +
                "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                "VALUES (?, ?, 1, ?, ?, ?)";

        return DatabaseManager.executeInsertReturnId(
                sql, "ID",
                noReservacion, total, usuarioId, fechaCreacion, fechaExpiracion
        );
    }

    // ---------------------------------- Insertar detalle---------------------------------
    public void crearDetalle(int reservacionId, int habitacionId,
                             Date fechaCheckIn, Date fechaCheckOut,
                             int cantidadPersonas, double total) {
        String sql = "INSERT INTO DetallesReservacion " +
                "(ReservacionID, HabitacionID, FechaCheckIn, FechaCheckOut, CantidadPersonas, Total) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        DatabaseManager.executeUpdate(sql,
                reservacionId, habitacionId, fechaCheckIn, fechaCheckOut, cantidadPersonas, total
        );
    }

    // -------------------obtener reservación para la respuesta------------------------------------

    public Object[] obtenerReservacion(int reservacionId) {
        String sql = "SELECT r.ID, r.No_Reservacion, r.Total, r.Fecha_Creacion, r.Fecha_Expiracion, " +
                "e.Estado " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva e ON r.EstadoID = e.ID " +
                "WHERE r.ID = ?";

        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getString("No_Reservacion"),
                rs.getDouble("Total"),
                rs.getTimestamp("Fecha_Creacion") != null ? rs.getTimestamp("Fecha_Creacion").toString() : null,
                rs.getTimestamp("Fecha_Expiracion") != null ? rs.getTimestamp("Fecha_Expiracion").toString() : null,
                rs.getString("Estado")
        }, reservacionId);

        return result.isEmpty() ? null : result.get(0);
    }

    // ----------------------Obtener todas las reservaciones de un usuario---------------------------

    public List<ReservacionDetalleDTO> obtenerReservacionesDeUsuario(int usuarioId) {
        String sql = "SELECT r.ID, r.No_Reservacion, r.Total, " +
                "r.Fecha_Creacion, r.Fecha_Expiracion, r.Fecha_Cancelacion, r.Motivo_Cancelacion, " +
                "er.Estado, " +
                "dr.ID AS DetalleID, dr.HabitacionID, dr.FechaCheckIn, dr.FechaCheckOut, " +
                "dr.CantidadPersonas, dr.Total AS TotalDetalle, " +
                "h.Descripcion AS DescripcionHabitacion, " +
                "t.nombre AS TipoHabitacion, " +
                "c.Tipo_de_clase AS TipoCama, " +
                "hot.Nombre AS NombreHotel " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva      er  ON r.EstadoID          = er.ID " +
                "JOIN DetallesReservacion dr  ON dr.ReservacionID   = r.ID " +
                "JOIN Habitacion          h   ON dr.HabitacionID    = h.ID " +
                "JOIN TipoHabitacion      t   ON h.TipoHabitacionID = t.ID " +
                "JOIN Cama                c   ON h.CamaID           = c.ID " +
                "JOIN Hotel               hot ON h.HotelID          = hot.ID " +
                "WHERE r.Usuario_ID = ? " +
                "ORDER BY r.Fecha_Creacion DESC, r.ID, dr.ID";

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
            dto.setNombreHotel(rs.getString("NombreHotel"));
            return dto;
        }, usuarioId);
    }

    // --------------- Expirar reservaciones vencidas- ----------------------

    public int expirarReservacionesVencidas() {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'expirada') " +
                "WHERE EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'pendiente') " +
                "AND Fecha_Expiracion < SYSDATE";

        return DatabaseManager.executeUpdate(sql);
    }
}