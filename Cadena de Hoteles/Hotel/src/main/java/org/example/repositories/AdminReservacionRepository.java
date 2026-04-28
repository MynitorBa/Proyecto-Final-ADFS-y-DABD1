package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository para la gestion de reservaciones desde el panel de administracion.
 * Maneja la consulta de todas las reservaciones y la logica de cancelacion a nivel de base de datos.
 */
public class AdminReservacionRepository {

    /**
     * Retorna todas las reservaciones registradas en el sistema con sus datos completos.
     * Usa subqueries escalares para garantizar una sola fila por reservacion, evitando
     * duplicados cuando una reservacion tiene habitaciones en distintos hoteles.
     * @return lista de mapas con los datos de cada reservacion, ordenadas por fecha de creacion descendente.
     */
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

        // Mapea cada fila del ResultSet a un LinkedHashMap preservando el orden de las columnas
        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",                   rs.getInt("ID"));
            row.put("noReservacion",         rs.getString("No_Reservacion"));
            row.put("total",                rs.getDouble("Total"));
            row.put("estadoId",             rs.getInt("EstadoID"));
            row.put("estado",               rs.getString("Estado").toLowerCase());
            // Convierte timestamps a String, o null si no tiene valor
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
            // Convierte fechas de check-in y check-out a String, o null si no aplica
            row.put("checkIn",              rs.getDate("CheckIn")  != null ? rs.getDate("CheckIn").toString()  : null);
            row.put("checkOut",             rs.getDate("CheckOut") != null ? rs.getDate("CheckOut").toString() : null);
            row.put("cantidadHabitaciones",  rs.getInt("CantidadHabitaciones"));
            return row;
        });
    }

    /**
     * Retorna las N reservaciones mas recientes. Usa FETCH FIRST para evitar
     * traer toda la tabla cuando solo se necesitan las ultimas filas (dashboard).
     * @param n cantidad maxima de filas a retornar.
     * @return lista de mapas con los datos resumidos de cada reservacion.
     */
    public List<Map<String, Object>> listarRecientes(int n) {
        String sql =
                "SELECT r.ID, r.No_Reservacion, r.Total, " +
                "  er.Estado, " +
                "  u.Username, " +
                "  ( SELECT hot2.Nombre " +
                "    FROM DetallesReservacion dr2 " +
                "    JOIN Habitacion h2   ON dr2.HabitacionID = h2.ID " +
                "    JOIN Hotel      hot2 ON h2.HotelID       = hot2.ID " +
                "    WHERE dr2.ReservacionID = r.ID AND ROWNUM = 1 " +
                "  ) AS HotelNombre " +
                "FROM Reservacion   r " +
                "JOIN EstadoReserva er ON r.EstadoID   = er.ID " +
                "JOIN Usuario       u  ON r.Usuario_ID = u.ID " +
                "ORDER BY r.Fecha_Creacion DESC " +
                "FETCH FIRST ? ROWS ONLY";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",            rs.getInt("ID"));
            row.put("noReservacion", rs.getString("No_Reservacion"));
            row.put("total",         rs.getDouble("Total"));
            row.put("estado",        rs.getString("Estado").toLowerCase());
            row.put("usuario",       rs.getString("Username"));
            row.put("hotel",         rs.getString("HotelNombre"));
            return row;
        }, n);
    }

    /**
     * Busca una reservacion por su ID y retorna sus datos basicos de estado.
     * @param reservacionId ID de la reservacion a buscar.
     * @return arreglo con {ID, EstadoID, Estado} o null si no existe.
     */
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

    /**
     * Retorna los datos del usuario y de la reservacion necesarios para enviar
     * el correo de cancelacion al cliente.
     *
     * @param reservacionId ID de la reservacion cancelada.
     * @return arreglo con {correo, nombreCompleto, noReservacion, total (Double)}
     *         o null si la reservacion no existe.
     */
    public Object[] obtenerDatosUsuarioPorReservacion(int reservacionId) {
        String sql =
                "SELECT u.Correo, " +
                        "       u.Nombre || ' ' || u.Apellido AS NombreCompleto, " +
                        "       r.No_Reservacion, " +
                        "       r.Total " +
                        "FROM   Reservacion r " +
                        "JOIN   Usuario     u ON r.Usuario_ID = u.ID " +
                        "WHERE  r.ID = ?";

        List<Object[]> res = DatabaseManager.executeQuery(
                sql,
                rs -> new Object[]{
                        rs.getString("Correo"),
                        rs.getString("NombreCompleto"),
                        rs.getString("No_Reservacion"),
                        rs.getDouble("Total")
                },
                reservacionId
        );
        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * Actualiza el estado de una reservacion a Cancelada (EstadoID = 4) y registra el motivo.
     * Si el motivo es nulo o vacio, se guarda un texto por defecto.
     * @param reservacionId ID de la reservacion a cancelar.
     * @param motivo        razon de la cancelacion ingresada por el administrador.
     */
    public void cancelarReservacion(int reservacionId, String motivo) {
        DatabaseManager.executeUpdate(
                "UPDATE Reservacion " +
                        "SET EstadoID = 4, Fecha_Cancelacion = SYSDATE, Motivo_Cancelacion = ? " +
                        "WHERE ID = ?",
                motivo != null && !motivo.isBlank() ? motivo : "Cancelada por administrador",
                reservacionId
        );
    }

    /**
     * Retorna los detalles (habitaciones) de una reservacion con su informacion completa.
     * @param reservacionId ID de la reservacion.
     * @return lista de mapas con {detalleId, habitacion, tipo, checkIn, checkOut, total}.
     */
    public List<Map<String, Object>> obtenerDetalles(int reservacionId) {
        String sql =
                "SELECT dr.ID AS DetalleID, h.NUMEROHABITACION AS Habitacion, " +
                "       th.NOMBRE AS Tipo, " +
                "       dr.FechaCheckIn, dr.FechaCheckOut, dr.Total " +
                "FROM DetallesReservacion dr " +
                "JOIN Habitacion h ON dr.HabitacionID = h.ID " +
                "JOIN TipoHabitacion th ON h.TIPOHABITACIONID = th.ID " +
                "WHERE dr.ReservacionID = ? " +
                "ORDER BY dr.ID";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("detalleId",  rs.getInt("DetalleID"));
            row.put("habitacion", rs.getString("Habitacion"));
            row.put("tipo",       rs.getString("Tipo"));
            row.put("checkIn",    rs.getDate("FechaCheckIn")  != null ? rs.getDate("FechaCheckIn").toString()  : null);
            row.put("checkOut",   rs.getDate("FechaCheckOut") != null ? rs.getDate("FechaCheckOut").toString() : null);
            row.put("total",      rs.getDouble("Total"));
            return row;
        }, reservacionId);
    }

    /**
     * Actualiza las fechas de un detalle de reservacion especifico.
     * @param detalleId    ID del detalle a actualizar.
     * @param fechaCheckIn nueva fecha de check-in.
     * @param fechaCheckOut nueva fecha de check-out.
     */
    public void actualizarFechaDetalle(int detalleId,
                                        java.sql.Date fechaCheckIn,
                                        java.sql.Date fechaCheckOut) {
        DatabaseManager.executeUpdate(
                "UPDATE DetallesReservacion " +
                "SET FechaCheckIn = ?, FechaCheckOut = ? " +
                "WHERE ID = ?",
                fechaCheckIn, fechaCheckOut, detalleId
        );
    }
}