package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ReservacionDetalleDTO;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import java.util.ArrayList;

/**
 * Repository para la gestion de reservaciones realizadas por agencias de viaje.
 * Cubre la creacion, consulta, validacion de disponibilidad y expiracion de reservaciones,
 * asi como la obtencion de datos de agencia y precios de habitaciones.
 */
public class ReservacionAgenciaRepository {

    /**
     * Retorna el ID del usuario webservice y el porcentaje de descuento de una agencia activa.
     * @param agenciaId ID de la agencia a consultar.
     * @return arreglo con {UsuarioWEBIs_ID, PorcentajeDescuento (entero)} o null si la agencia no existe o no esta activa.
     */
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

    /**
     * Retorna el porcentaje de descuento exacto con decimales de una agencia activa.
     * @param agenciaId ID de la agencia a consultar.
     * @return porcentaje de descuento como double, o 0.0 si la agencia no existe o no esta activa.
     */
    public double obtenerDescuentoAgencia(int agenciaId) {
        String sql = "SELECT PorcentajeDescuento FROM Agencia " +
                "WHERE ID = ? AND EstadoID = " +
                "(SELECT ID FROM EstadoAgencia WHERE LOWER(Estado) = 'activa')";
        List<Double> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getDouble("PorcentajeDescuento"), agenciaId
        );
        return result.isEmpty() ? 0.0 : result.get(0);
    }

    /**
     * Retorna el precio por noche, precio por persona y capacidad maxima de una habitacion.
     * Los valores se retornan en ese orden: precios[0] = precioPorNoche, precios[1] = precioPorPersona,
     * precios[2] = capacidadMaxima.
     * @param habitacionId ID de la habitacion a consultar.
     * @return arreglo de doubles con los tres valores de precio y capacidad.
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
        if (result.isEmpty())
            throw new RuntimeException("Habitacion no encontrada: " + habitacionId);
        return result.get(0);
    }

    /**
     * Verifica si una habitacion tiene reservaciones activas que se traslapen con el rango de fechas indicado.
     * Solo considera reservaciones en estado Pendiente o Confirmada.
     * @param habitacionId  ID de la habitacion a verificar.
     * @param fechaCheckIn  fecha de entrada solicitada.
     * @param fechaCheckOut fecha de salida solicitada.
     * @return true si existe al menos un traslape, false si la habitacion esta disponible.
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
     * Crea una nueva reservacion en estado Pendiente (EstadoID = 1) y retorna el ID generado.
     * @param noReservacion   codigo unico de la reservacion.
     * @param total           monto total de la reservacion.
     * @param usuarioWebisId  ID del usuario webservice asociado a la agencia.
     * @param fechaCreacion   fecha y hora de creacion de la reservacion.
     * @param fechaExpiracion fecha y hora limite para completar el pago.
     * @return ID de la reservacion recien insertada.
     */
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

    /**
     * Verifica si una reservacion pertenece a una agencia especifica y se encuentra en estado Pendiente.
     * @param reservacionId ID de la reservacion a verificar.
     * @param agenciaId     ID de la agencia a validar como propietaria.
     * @return true si la reservacion pertenece a la agencia y esta pendiente, false en caso contrario.
     */
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

    /**
     * Marca una reservacion como Expirada si actualmente se encuentra en estado Pendiente.
     * @param reservacionId ID de la reservacion a expirar.
     */
    public void expirarReservacion(int reservacionId) {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'expirada'), " +
                "    Fecha_Expiracion = SYSDATE " +
                "WHERE ID = ? " +
                "AND EstadoID = (SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'pendiente')";
        DatabaseManager.executeUpdate(sql, reservacionId);
    }

    /**
     * Inserta un detalle de reservacion con los datos de una habitacion y el rango de fechas solicitado.
     * @param reservacionId    ID de la reservacion a la que pertenece el detalle.
     * @param habitacionId     ID de la habitacion reservada.
     * @param fechaCheckIn     fecha de entrada.
     * @param fechaCheckOut    fecha de salida.
     * @param cantidadPersonas numero de personas para este detalle.
     * @param total            costo total de este detalle.
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
     * Retorna los datos resumidos de una reservacion para construir la respuesta al cliente.
     * @param reservacionId ID de la reservacion a consultar.
     * @return arreglo con {ID, No_Reservacion, Total, Fecha_Creacion, Fecha_Expiracion, Estado}
     *         o null si no existe.
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
     * Retorna todas las reservaciones asociadas a una agencia con el detalle completo de habitaciones y hotel.
     * @param agenciaId ID de la agencia de la que se quieren obtener las reservaciones.
     * @return lista de ReservacionDetalleDTO con una entrada por cada habitacion reservada.
     */
    public List<ReservacionDetalleDTO> obtenerReservacionesDeAgencia(int agenciaId) {
        String sql = "SELECT * FROM VW_DETALLE_RESERVACIONES " +
                "WHERE AgenciaID = ? " +
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
        }, agenciaId);
    }

    /**
     * Retorna los IDs de las imagenes asociadas a un hotel.
     * @param hotelId ID del hotel del que se quieren obtener las imagenes.
     * @return lista de IDs de imagenes del hotel.
     */
    public List<Integer> obtenerImagenesHotel(int hotelId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHotel WHERE HotelID = ?",
                rs -> rs.getInt("ID"), hotelId
        );
    }

    /**
     * Retorna los IDs de las imagenes asociadas a una habitacion.
     * @param habitacionId ID de la habitacion de la que se quieren obtener las imagenes.
     * @return lista de IDs de imagenes de la habitacion.
     */
    public List<Integer> obtenerImagenesHabitacion(int habitacionId) {
        return DatabaseManager.executeQuery(
                "SELECT ih.ID FROM ImagenHabitacion ih " +
                        "JOIN Habitacion h ON ih.TipoHabitacionID = h.TIPOHABITACIONID " +
                        "WHERE h.ID = ?",
                rs -> rs.getInt("ID"), habitacionId
        );
    }

    /**
     * Retorna el detalle completo de una reservacion especifica verificando que pertenezca a la agencia indicada.
     * Primero obtiene el ID del usuario webservice de la agencia y luego filtra la reservacion por ese usuario.
     * @param reservacionId ID de la reservacion a consultar.
     * @param agenciaId     ID de la agencia propietaria de la reservacion.
     * @return lista de ReservacionDetalleDTO con una entrada por cada habitacion, o lista vacia si la agencia no existe
     *         o la reservacion no le pertenece.
     */
    public List<ReservacionDetalleDTO> obtenerDetalleReservacionAgencia(
            int reservacionId, int agenciaId) {

        String sql = "SELECT * FROM VW_DETALLE_RESERVACIONES " +
                "WHERE ReservacionID = ? AND AgenciaID = ? " +
                "ORDER BY DetalleID";

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
        }, reservacionId, agenciaId);
    }






    /**
     * Retorna el detalle de un DetallesReservacion por ID, verificando que la reservacion
     * pertenezca a la agencia indicada. Incluye estado y checkout original para validar
     * duracion y regla de 48 horas.
     * @param detalleId ID del detalle a consultar.
     * @param agenciaId ID de la agencia propietaria.
     * @return arreglo con {ID, ReservacionID, HabitacionID, FechaCheckIn, FechaCheckOut,
     *         CantidadPersonas, Total, Estado} o null si no existe o no pertenece.
     */
    public Object[] obtenerDetalleDeAgencia(int detalleId, int agenciaId) {
        String sql =
                "SELECT dr.ID, dr.ReservacionID, dr.HabitacionID, dr.FechaCheckIn, dr.FechaCheckOut, " +
                        "       dr.CantidadPersonas, dr.Total, LOWER(TRIM(er.Estado)) AS Estado " +
                        "FROM DetallesReservacion dr " +
                        "JOIN Reservacion r   ON dr.ReservacionID = r.ID " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                        "JOIN Agencia a        ON r.Usuario_ID = a.UsuarioWEBIs_ID " +
                        "WHERE dr.ID = ? AND a.ID = ?";

        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getInt("ReservacionID"),
                rs.getInt("HabitacionID"),
                rs.getDate("FechaCheckIn"),
                rs.getDate("FechaCheckOut"),
                rs.getInt("CantidadPersonas"),
                rs.getDouble("Total"),
                rs.getString("Estado")
        }, detalleId, agenciaId);

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Verifica traslape de fechas para una habitacion excluyendo un detalle especifico.
     */
    public boolean existeTraslapeExcluyendoDetalle(int habitacionId, Date checkIn,
                                                   Date checkOut, int detalleIdExcluir) {
        String sql =
                "SELECT COUNT(*) AS total " +
                        "FROM DetallesReservacion dr " +
                        "JOIN Reservacion r   ON dr.ReservacionID = r.ID " +
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
     * Actualiza fechas y total de multiples detalles en una sola transaccion atomica.
     * Si cualquier UPDATE falla se hace rollback de todos.
     * Como el cambio es solo de rango (misma duracion), el total no cambia.
     */
    public void actualizarFechasDetallesAtomico(List<Object[]> actualizaciones) {
        String sqlDetalle =
                "UPDATE DetallesReservacion " +
                        "SET FechaCheckIn = ?, FechaCheckOut = ?, Total = ? " +
                        "WHERE ID = ?";

        // El total de la reservacion padre no cambia — misma duracion, mismo precio
        // pero se recalcula igual por consistencia
        String sqlTotal =
                "UPDATE Reservacion r " +
                        "SET r.Total = (SELECT SUM(d.Total) FROM DetallesReservacion d WHERE d.ReservacionID = r.ID) " +
                        "WHERE r.ID = (SELECT ReservacionID FROM DetallesReservacion WHERE ID = ?)";

        DatabaseManager.executeInTransaction(conn -> {
            for (Object[] params : actualizaciones) {
                // params = { fechaCheckIn(Date), fechaCheckOut(Date), total(double), detalleId(int) }
                try (java.sql.PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle)) {
                    stmtDetalle.setDate(1,   (java.sql.Date) params[0]);
                    stmtDetalle.setDate(2,   (java.sql.Date) params[1]);
                    stmtDetalle.setDouble(3, (Double)        params[2]);
                    stmtDetalle.setInt(4,    (Integer)       params[3]);
                    stmtDetalle.executeUpdate();
                }
                try (java.sql.PreparedStatement stmtTotal = conn.prepareStatement(sqlTotal)) {
                    stmtTotal.setInt(1, (Integer) params[3]);
                    stmtTotal.executeUpdate();
                }
            }
        });
    }
}