package org.example.repositories;

import org.example.data.DatabaseManager;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class ReservacionRepository {

    // ------------------------Obtener precios de una habitación ------------------------------

    public double[] obtenerPrecios(int habitacionId) {
        String sql = "SELECT Precio_por_Noche, Precio_por_Persona FROM Habitacion WHERE ID = ?";
        List<double[]> result = DatabaseManager.executeQuery(sql, rs -> new double[]{
                rs.getDouble("Precio_por_Noche"),
                rs.getDouble("Precio_por_Persona")
        }, habitacionId);

        if (result.isEmpty()) throw new RuntimeException("Habitación no encontrada: " + habitacionId);
        return result.get(0);
    }

    // --------------------------------- Verificar traslape de fechas para una habitación -------

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

    //-------------------------------Crear reservación --------------------------------------------------------

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

    // -------------------------------- Insertar detalle ---------------------------------------------

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

    // --------------------------- Obtener reservación para la respuesta --------------------------------

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
                rs.getTimestamp("Fecha_Creacion").toString(),
                rs.getTimestamp("Fecha_Expiracion").toString(),
                rs.getString("Estado")
        }, reservacionId);

        return result.isEmpty() ? null : result.get(0);
    }

    // -------------------------- Expirar reservaciones vencidas --------------------------------------

    public int expirarReservacionesVencidas() {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'expirada') " +
                "WHERE EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'pendiente') " +
                "AND Fecha_Expiracion < SYSDATE";

        return DatabaseManager.executeUpdate(sql);
    }
}