package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ReservacionDetalleDTO;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class ReservacionAgenciaRepository {

    // Obtener usuarioWebisId y descuento de la agencia
    public int[] obtenerDatosAgencia(int agenciaId) {
        String sql = "SELECT UsuarioWEBIs_ID, PorcentajeDescuento FROM Agencia " +
                "WHERE ID = ? AND EstadoID = " +
                "(SELECT ID FROM EstadoAgencia WHERE LOWER(Estado) = 'activa')";
        List<int[]> result = DatabaseManager.executeQuery(sql, rs -> new int[]{
                rs.getInt("UsuarioWEBIs_ID"),
                (int) rs.getDouble("PorcentajeDescuento")
        }, agenciaId);
        return result.isEmpty() ? null : result.get(0);
    }

    // Obtener descuento exacto (con decimales) de la agencia
    public double obtenerDescuentoAgencia(int agenciaId) {
        String sql = "SELECT PorcentajeDescuento FROM Agencia " +
                "WHERE ID = ? AND EstadoID = " +
                "(SELECT ID FROM EstadoAgencia WHERE LOWER(Estado) = 'activa')";
        List<Double> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getDouble("PorcentajeDescuento"), agenciaId
        );
        return result.isEmpty() ? 0.0 : result.get(0);
    }

    // Obtener precios Y capacidad de habitacion
    // precios[0] = precioPorNoche, precios[1] = precioPorPersona, precios[2] = capacidadMaxima
    public double[] obtenerPrecios(int habitacionId) {
        String sql = "SELECT t.PRECIONOCHE, t.PRECIOPERSONA, t.CAPACIDADMAXIMA " +
                "FROM Habitacion h " +
                "JOIN TipoHabitacion t ON h.TIPOHABITACIONID = t.ID " +
                "WHERE h.ID = ?";
        List<double[]> result = DatabaseManager.executeQuery(sql, rs -> new double[]{
                rs.getDouble("PRECIONOCHE"),
                rs.getDouble("PRECIOPERSONA"),
                rs.getDouble("CAPACIDADMAXIMA")
        }, habitacionId);
        if (result.isEmpty())
            throw new RuntimeException("Habitación no encontrada: " + habitacionId);
        return result.get(0);
    }

    // Verificar traslape
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

    // Crear reservacion
    public int crearReservacion(String noReservacion, double total, int usuarioWebisId,
                                Timestamp fechaCreacion, Timestamp fechaExpiracion) {
        String sql = "INSERT INTO Reservacion " +
                "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                "VALUES (?, ?, 1, ?, ?, ?)";
        return DatabaseManager.executeInsertReturnId(
                sql, "ID",
                noReservacion, total, usuarioWebisId, fechaCreacion, fechaExpiracion
        );
    }

    // Verificar que la reservación pertenece a la agencia y está pendiente
    public boolean perteneceAAgenciaYEstaPendiente(int reservacionId, int agenciaId) {
        String sql = "SELECT COUNT(*) AS total " +
                "FROM Reservacion r " +
                "JOIN Agencia a ON r.Usuario_ID = a.UsuarioWEBIs_ID " +
                "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "WHERE r.ID = ? " +
                "AND a.ID = ? " +
                "AND LOWER(TRIM(er.Estado)) = 'pendiente'";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("total"),
                reservacionId, agenciaId
        );
        return !result.isEmpty() && result.get(0) > 0;
    }

    // Expirar una reservacion especifica
    public void expirarReservacion(int reservacionId) {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'expirada'), " +
                "    Fecha_Expiracion = SYSDATE " +
                "WHERE ID = ? " +
                "AND EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'pendiente')";
        DatabaseManager.executeUpdate(sql, reservacionId);
    }

    // Insertar detalle
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

    // Obtener reservacion para respuesta
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

    // Obtener reservaciones de una agencia
    public List<ReservacionDetalleDTO> obtenerReservacionesDeAgencia(int agenciaId) {
        String sql = "SELECT r.ID, r.No_Reservacion, r.Total, " +
                "r.Fecha_Creacion, r.Fecha_Expiracion, r.Fecha_Cancelacion, r.Motivo_Cancelacion, " +
                "er.Estado, " +
                "dr.ID AS DetalleID, dr.HabitacionID, dr.FechaCheckIn, dr.FechaCheckOut, " +
                "dr.CantidadPersonas, dr.Total AS TotalDetalle, " +
                "h.Descripcion AS DescripcionHabitacion, h.NUMEROHABITACION, " +
                "t.NOMBRE AS TipoHabitacion, c.TIPO_DE_CLASE AS TipoCama, " +
                "hot.ID AS HotelID, hot.Nombre AS NombreHotel " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva       er  ON r.EstadoID         = er.ID " +
                "JOIN DetallesReservacion dr  ON dr.ReservacionID   = r.ID " +
                "JOIN Habitacion          h   ON dr.HabitacionID    = h.ID " +
                "JOIN TipoHabitacion      t   ON h.TIPOHABITACIONID = t.ID " +
                "JOIN Cama                c   ON t.TIPOCAMAID       = c.ID " +
                "JOIN Hotel               hot ON h.HOTELID          = hot.ID " +
                "JOIN Agencia             a   ON r.Usuario_ID       = a.UsuarioWEBIs_ID " +
                "WHERE a.ID = ? " +
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
            dto.setNumeroHabitacion(rs.getString("NUMEROHABITACION"));
            dto.setTipoHabitacion(rs.getString("TipoHabitacion"));
            dto.setTipoCama(rs.getString("TipoCama"));
            dto.setHotelId(rs.getInt("HotelID"));
            dto.setNombreHotel(rs.getString("NombreHotel"));
            return dto;
        }, agenciaId);
    }

    // IDs de imagenes
    public List<Integer> obtenerImagenesHotel(int hotelId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHotel WHERE HotelID = ?",
                rs -> rs.getInt("ID"), hotelId
        );
    }

    public List<Integer> obtenerImagenesHabitacion(int habitacionId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHabitacion WHERE HabitacionID = ?",
                rs -> rs.getInt("ID"), habitacionId
        );
    }

    public List<ReservacionDetalleDTO> obtenerDetalleReservacionAgencia(int reservacionId, int agenciaId) {
        String sql = "SELECT r.ID, r.No_Reservacion, r.Total, " +
                "r.Fecha_Creacion, r.Fecha_Expiracion, r.Fecha_Cancelacion, r.Motivo_Cancelacion, " +
                "er.Estado, " +
                "dr.ID AS DetalleID, dr.HabitacionID, dr.FechaCheckIn, dr.FechaCheckOut, " +
                "dr.CantidadPersonas, dr.Total AS TotalDetalle, " +
                "h.Descripcion AS DescripcionHabitacion, " +
                "h.NUMEROHABITACION, " +
                "t.NOMBRE AS TipoHabitacion, " +
                "c.TIPO_DE_CLASE AS TipoCama, " +
                "hot.ID AS HotelID, " +
                "hot.Nombre AS NombreHotel " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva       er  ON r.EstadoID           = er.ID " +
                "JOIN DetallesReservacion dr  ON dr.ReservacionID     = r.ID " +
                "JOIN Habitacion          h   ON dr.HabitacionID      = h.ID " +
                "JOIN TipoHabitacion      t   ON h.TIPOHABITACIONID   = t.ID " +
                "JOIN Cama                c   ON t.TIPOCAMAID         = c.ID " +
                "JOIN Hotel               hot ON h.HOTELID            = hot.ID " +
                "WHERE r.ID = ? " +
                "AND r.Agencia_ID = ? " +
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
            dto.setNumeroHabitacion(rs.getString("NUMEROHABITACION"));
            dto.setTipoHabitacion(rs.getString("TipoHabitacion"));
            dto.setTipoCama(rs.getString("TipoCama"));
            dto.setHotelId(rs.getInt("HotelID"));
            dto.setNombreHotel(rs.getString("NombreHotel"));
            return dto;
        }, reservacionId, agenciaId);
    }
}