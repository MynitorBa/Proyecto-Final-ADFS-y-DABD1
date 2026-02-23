package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminReservacionRepository {

    // ════════════════════════════════════════════════════
    //  LISTAR TODAS LAS RESERVACIONES (admin)
    //
    //  Una fila garantizada por reservación.
    //  El hotel, fechas y cantidad de habitaciones se
    //  obtienen con subqueries escalares para evitar que
    //  el GROUP BY duplique filas cuando una reservación
    //  tiene habitaciones de distintos hoteles.
    // ════════════════════════════════════════════════════

    public List<Map<String, Object>> listarTodas() {
        String sql =
                "SELECT " +
                        "  r.ID, r.No_Reservacion, r.Total, r.EstadoID, " +
                        "  er.Estado, " +
                        "  r.Fecha_Creacion, r.Fecha_Expiracion, " +
                        "  r.Fecha_Cancelacion, r.Motivo_Cancelacion, " +
                        "  u.ID       AS UsuarioID, " +
                        "  u.Nombre   AS UsuarioNombre, " +
                        "  u.Apellido AS UsuarioApellido, " +
                        "  u.Username, " +
                        "  u.Correo, " +
                        "  ( SELECT hot2.Nombre " +
                        "    FROM DetallesReservacion dr2 " +
                        "    JOIN Habitacion h2   ON dr2.HabitacionID = h2.ID " +
                        "    JOIN Hotel      hot2 ON h2.HotelID       = hot2.ID " +
                        "    WHERE dr2.ReservacionID = r.ID " +
                        "    AND ROWNUM = 1 " +
                        "  ) AS HotelNombre, " +
                        "  ( SELECT MIN(dr3.FechaCheckIn) " +
                        "    FROM DetallesReservacion dr3 " +
                        "    WHERE dr3.ReservacionID = r.ID " +
                        "  ) AS CheckIn, " +
                        "  ( SELECT MAX(dr4.FechaCheckOut) " +
                        "    FROM DetallesReservacion dr4 " +
                        "    WHERE dr4.ReservacionID = r.ID " +
                        "  ) AS CheckOut, " +
                        "  ( SELECT COUNT(*) " +
                        "    FROM DetallesReservacion dr5 " +
                        "    WHERE dr5.ReservacionID = r.ID " +
                        "  ) AS CantidadHabitaciones " +
                        "FROM Reservacion   r " +
                        "JOIN EstadoReserva er ON r.EstadoID   = er.ID " +
                        "JOIN Usuario       u  ON r.Usuario_ID = u.ID " +
                        "ORDER BY r.Fecha_Creacion DESC";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",                   rs.getInt("ID"));
            row.put("noReservacion",         rs.getString("No_Reservacion"));
            row.put("total",                rs.getDouble("Total"));
            row.put("estadoId",             rs.getInt("EstadoID"));
            row.put("estado",               rs.getString("Estado").toLowerCase());
            row.put("fechaCreacion",         rs.getTimestamp("Fecha_Creacion") != null
                    ? rs.getTimestamp("Fecha_Creacion").toString() : null);
            row.put("fechaExpiracion",       rs.getTimestamp("Fecha_Expiracion") != null
                    ? rs.getTimestamp("Fecha_Expiracion").toString() : null);
            row.put("fechaCancelacion",      rs.getDate("Fecha_Cancelacion") != null
                    ? rs.getDate("Fecha_Cancelacion").toString() : null);
            row.put("motivoCancelacion",     rs.getString("Motivo_Cancelacion"));
            row.put("usuarioId",            rs.getInt("UsuarioID"));
            row.put("usuario",              rs.getString("Username"));
            row.put("nombreCompleto",        rs.getString("UsuarioNombre") + " " + rs.getString("UsuarioApellido"));
            row.put("correo",               rs.getString("Correo"));
            row.put("hotel",                rs.getString("HotelNombre"));
            row.put("checkIn",              rs.getDate("CheckIn")  != null ? rs.getDate("CheckIn").toString()  : null);
            row.put("checkOut",             rs.getDate("CheckOut") != null ? rs.getDate("CheckOut").toString() : null);
            row.put("cantidadHabitaciones",  rs.getInt("CantidadHabitaciones"));
            return row;
        });
    }

    // ════════════════════════════════════════════════════
    //  CANCELAR RESERVACIÓN (admin)
    // ════════════════════════════════════════════════════

    /** Devuelve {id, estadoId, estado} o null si no existe */
    public Object[] obtenerReservacion(int reservacionId) {
        List<Object[]> res = DatabaseManager.executeQuery(
                "SELECT r.ID, r.EstadoID, er.Estado " +
                        "FROM Reservacion r " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                        "WHERE r.ID = ?",
                rs -> new Object[]{
                        rs.getInt("ID"),
                        rs.getInt("EstadoID"),
                        rs.getString("Estado")
                },
                reservacionId
        );
        return res.isEmpty() ? null : res.get(0);
    }

    /** Pone EstadoID = 4 (Cancelada) y guarda el motivo */
    public void cancelarReservacion(int reservacionId, String motivo) {
        DatabaseManager.executeUpdate(
                "UPDATE Reservacion " +
                        "SET EstadoID = 4, Fecha_Cancelacion = SYSDATE, Motivo_Cancelacion = ? " +
                        "WHERE ID = ?",
                motivo != null && !motivo.isBlank() ? motivo : "Cancelada por administrador",
                reservacionId
        );
    }
}