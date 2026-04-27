package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ReservacionDetalleDTO;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Repository para el acceso a datos relacionados con reservaciones.
 * Maneja consultas y actualizaciones sobre reservaciones, detalles e imagenes.
 */
public class ReservacionRepository {

    /**
     * Retorna los precios y la capacidad maxima de una habitacion segun su tipo.
     * @param habitacionId ID de la habitacion a consultar.
     * @return arreglo con precio por noche, precio por persona y capacidad maxima.
     * @throws RuntimeException si la habitacion no existe en la base de datos.
     */
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

        if (result.isEmpty()) throw new RuntimeException("Habitación no encontrada: " + habitacionId);
        return result.get(0);
    }

    /**
     * Verifica si existe un traslape de fechas para una habitacion con reservaciones activas.
     * Solo considera reservaciones en estado pendiente o confirmada.
     * @param habitacionId  ID de la habitacion a verificar.
     * @param fechaCheckIn  fecha de entrada de la nueva reservacion.
     * @param fechaCheckOut fecha de salida de la nueva reservacion.
     * @return true si existe al menos un traslape, false en caso contrario.
     */
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

    /**
     * Marca como expiradas todas las reservaciones pendientes de un usuario,
     * exceptuando la reservacion que se acaba de crear o actualizar.
     * @param usuarioId          ID del usuario dueno de las reservaciones.
     * @param reservacionIdExcluir ID de la reservacion que no debe expirar.
     */
    public void expirarPendientesDeUsuario(int usuarioId, int reservacionIdExcluir) {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'expirada'), " +
                "    Fecha_Expiracion = SYSDATE " +
                "WHERE Usuario_ID = ? " +
                "AND ID != ? " +
                "AND EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'pendiente')";
        DatabaseManager.executeUpdate(sql, usuarioId, reservacionIdExcluir);
    }

    /**
     * Inserta una nueva reservacion en la base de datos con estado inicial pendiente.
     * @param noReservacion  codigo unico que identifica la reservacion.
     * @param total          monto total de la reservacion.
     * @param usuarioId      ID del usuario que realiza la reservacion.
     * @param fechaCreacion  fecha y hora en que se creo la reservacion.
     * @param fechaExpiracion fecha y hora en que expira la reservacion si no se confirma.
     * @return ID generado por la base de datos para la nueva reservacion.
     */
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

    /**
     * Inserta el detalle de una habitacion asociado a una reservacion existente.
     * @param reservacionId   ID de la reservacion a la que pertenece el detalle.
     * @param habitacionId    ID de la habitacion reservada.
     * @param fechaCheckIn    fecha de entrada a la habitacion.
     * @param fechaCheckOut   fecha de salida de la habitacion.
     * @param cantidadPersonas numero de personas para esta habitacion.
     * @param total           costo total calculado para este detalle.
     */
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

    /**
     * Retorna los datos principales de una reservacion junto con su estado actual.
     * @param reservacionId ID de la reservacion a consultar.
     * @return arreglo con los campos de la reservacion, o null si no existe.
     */
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

    /**
     * Retorna todas las reservaciones de un usuario con el detalle completo de cada habitacion.
     * Incluye informacion del hotel, tipo de habitacion, cama y fechas de cada detalle.
     * @param usuarioId ID del usuario cuyas reservaciones se desean consultar.
     * @return lista de DTOs con la informacion consolidada de reservaciones y detalles.
     */
    public List<ReservacionDetalleDTO> obtenerReservacionesDeUsuario(int usuarioId) {
        String sql = "SELECT * FROM VW_DETALLE_RESERVACIONES " +
                "WHERE USUARIO_ID = ? " +
                "ORDER BY FECHA_CREACION DESC, ReservacionID, DetalleID";

        return DatabaseManager.executeQuery(sql, rs -> {
            ReservacionDetalleDTO dto = new ReservacionDetalleDTO();
            dto.setId(rs.getInt("ReservacionID"));
            dto.setNoReservacion(rs.getString("NO_RESERVACION"));
            dto.setTotal(rs.getDouble("TOTAL"));
            dto.setEstado(rs.getString("ESTADO"));
            dto.setFechaCreacion(rs.getTimestamp("FECHA_CREACION") != null ? rs.getTimestamp("FECHA_CREACION").toString() : null);
            dto.setFechaExpiracion(rs.getTimestamp("FECHA_EXPIRACION") != null ? rs.getTimestamp("FECHA_EXPIRACION").toString() : null);
            dto.setFechaCancelacion(rs.getDate("FECHA_CANCELACION") != null ? rs.getDate("FECHA_CANCELACION").toString() : null);
            dto.setMotivoCancelacion(rs.getString("MOTIVO_CANCELACION"));
            dto.setDetalleId(rs.getInt("DetalleID"));
            dto.setHabitacionId(rs.getInt("HABITACIONID"));
            dto.setFechaCheckIn(rs.getDate("FECHACHECKIN") != null ? rs.getDate("FECHACHECKIN").toString() : null);
            dto.setFechaCheckOut(rs.getDate("FECHACHECKOUT") != null ? rs.getDate("FECHACHECKOUT").toString() : null);
            dto.setCantidadPersonas(rs.getInt("CANTIDADPERSONAS"));
            dto.setTotalDetalle(rs.getDouble("TotalDetalle"));
            dto.setDescripcionHabitacion(rs.getString("DescripcionHabitacion"));
            dto.setNumeroHabitacion(rs.getString("NUMEROHABITACION"));
            dto.setTipoHabitacion(rs.getString("TipoHabitacion"));
            dto.setTipoCama(rs.getString("TipoCama"));
            dto.setHotelId(rs.getInt("HotelID"));
            dto.setNombreHotel(rs.getString("NombreHotel"));
            return dto;
        }, usuarioId);
    }

    // IDs de imagenes asociadas a hoteles y habitaciones

    /**
     * Retorna los IDs de todas las imagenes registradas para un hotel.
     * @param hotelId ID del hotel del que se quieren obtener las imagenes.
     * @return lista de IDs de imagenes del hotel.
     */
    public List<Integer> obtenerImagenesHotel(int hotelId) {
        String sql = "SELECT ID FROM ImagenHotel WHERE HotelID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelId);
    }

    /**
     * Retorna los IDs de todas las imagenes registradas para una habitacion.
     * @param habitacionId ID de la habitacion de la que se quieren obtener las imagenes.
     * @return lista de IDs de imagenes de la habitacion.
     */
    public List<Integer> obtenerImagenesHabitacion(int habitacionId) {
        String sql = "SELECT ih.ID FROM ImagenHabitacion ih " +
                "JOIN Habitacion h ON ih.TipoHabitacionID = h.TIPOHABITACIONID " +
                "WHERE h.ID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), habitacionId);
    }

    /**
     * Marca como expiradas todas las reservaciones pendientes cuya fecha de expiracion ya paso.
     * @return numero de filas actualizadas en la base de datos.
     */
    public List<Integer> expirarReservacionesVencidas() {
        // Primero obtener los IDs que van a ser expirados
        String sqlSelect = "SELECT ID FROM Reservacion " +
                "WHERE EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'pendiente') " +
                "AND Fecha_Expiracion < SYSDATE";

        List<Integer> ids = DatabaseManager.executeQuery(sqlSelect, rs -> rs.getInt("ID"));

        if (ids.isEmpty()) return ids;

        // Luego expirarlos en bloque
        String sqlUpdate = "UPDATE Reservacion " +
                "SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'expirada') " +
                "WHERE EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'pendiente') " +
                "AND Fecha_Expiracion < SYSDATE";
        DatabaseManager.executeUpdate(sqlUpdate);

        return ids;
    }






    /**
     * Verifica traslape de fechas para una habitacion, excluyendo un detalle especifico.
     * Util al cambiar fechas de un detalle existente para no colisionar con si mismo.
     */
    public boolean existeTraslapeExcluyendoDetalle(int habitacionId, Date checkIn,
                                                   Date checkOut, int detalleIdExcluir) {
        String sql =
                "SELECT COUNT(*) AS total " +
                        "FROM DetallesReservacion dr " +
                        "JOIN Reservacion r ON dr.ReservacionID = r.ID " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                        "WHERE dr.HabitacionID = ? " +
                        "AND dr.ID != ? " +
                        "AND LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada') " +
                        "AND dr.FechaCheckIn  < ? " +
                        "AND dr.FechaCheckOut > ?";

        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("total"),
                habitacionId, detalleIdExcluir, checkOut, checkIn
        );
        return !result.isEmpty() && result.get(0) > 0;
    }

    /**
     * Actualiza las fechas y el total de un detalle de reservacion.
     * Tambien recalcula y actualiza el total general de la reservacion padre.
     */
    public void actualizarFechasDetalle(int detalleId, Date checkIn,
                                        Date checkOut, int personas, double nuevoTotal) {
        String sqlDetalle =
                "UPDATE DetallesReservacion " +
                        "SET FechaCheckIn = ?, FechaCheckOut = ?, CantidadPersonas = ?, Total = ? " +
                        "WHERE ID = ?";
        DatabaseManager.executeUpdate(sqlDetalle, checkIn, checkOut, personas, nuevoTotal, detalleId);

        // Recalcular el total general sumando todos los detalles de la reservacion
        String sqlTotal =
                "UPDATE Reservacion r " +
                        "SET r.Total = (SELECT SUM(d.Total) FROM DetallesReservacion d WHERE d.ReservacionID = r.ID) " +
                        "WHERE r.ID = (SELECT ReservacionID FROM DetallesReservacion WHERE ID = ?)";
        DatabaseManager.executeUpdate(sqlTotal, detalleId);
    }

    /**
     * Retorna el detalle de reservacion por ID, incluyendo el estado de la reservacion padre
     * y el ID del usuario dueno, para validar permisos antes de modificar.
     */
    public Object[] obtenerDetalle(int detalleId) {
        String sql =
                "SELECT dr.ID, dr.ReservacionID, dr.HabitacionID, dr.FechaCheckIn, dr.FechaCheckOut, " +
                        "       dr.CantidadPersonas, dr.Total, r.Usuario_ID, LOWER(TRIM(er.Estado)) AS Estado " +
                        "FROM DetallesReservacion dr " +
                        "JOIN Reservacion r  ON dr.ReservacionID = r.ID " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                        "WHERE dr.ID = ?";

        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getInt("ReservacionID"),
                rs.getInt("HabitacionID"),
                rs.getDate("FechaCheckIn"),
                rs.getDate("FechaCheckOut"),
                rs.getInt("CantidadPersonas"),
                rs.getDouble("Total"),
                rs.getInt("Usuario_ID"),
                rs.getString("Estado")
        }, detalleId);

        return result.isEmpty() ? null : result.get(0);
    }


    /**
     * Actualiza fechas y total de multiples detalles en una sola transaccion.
     * Si cualquier UPDATE falla, se hace rollback de todos.
     */
    public void actualizarFechasDetallesAtomico(List<Object[]> actualizaciones) {

        String sqlDetalle =
                "UPDATE DetallesReservacion " +
                        "SET FechaCheckIn = ?, FechaCheckOut = ?, Total = ? " +
                        "WHERE ID = ?";

        String sqlTotal =
                "UPDATE Reservacion r " +
                        "SET r.Total = (SELECT SUM(d.Total) FROM DetallesReservacion d WHERE d.ReservacionID = r.ID) " +
                        "WHERE r.ID = (SELECT ReservacionID FROM DetallesReservacion WHERE ID = ?)";

        DatabaseManager.executeInTransaction(conn -> {
            for (Object[] params : actualizaciones) {
                // params = { fechaCheckIn(Date), fechaCheckOut(Date), total(double), detalleId(int) }
                try (PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle)) {
                    stmtDetalle.setDate(1,   (java.sql.Date)   params[0]);
                    stmtDetalle.setDate(2,   (java.sql.Date)   params[1]);
                    stmtDetalle.setDouble(3, (Double)          params[2]);
                    stmtDetalle.setInt(4,    (Integer)         params[3]);
                    stmtDetalle.executeUpdate();
                }
                try (PreparedStatement stmtTotal = conn.prepareStatement(sqlTotal)) {
                    stmtTotal.setInt(1, (Integer) params[3]);
                    stmtTotal.executeUpdate();
                }
            }
        });
    }
}