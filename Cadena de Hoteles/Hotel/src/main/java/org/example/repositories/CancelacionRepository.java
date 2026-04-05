package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

/**
 * Repository para la cancelacion de reservaciones, tanto de usuarios directos como de agencias.
 * Maneja la consulta de estado previo a la cancelacion y la actualizacion del registro.
 */
public class CancelacionRepository {

    /**
     * Busca una reservacion verificando que pertenezca al usuario indicado, para validar antes de cancelar.
     * @param reservacionId ID de la reservacion a consultar.
     * @param usuarioId     ID del usuario propietario de la reservacion.
     * @return arreglo con {ID, EstadoID, Estado} o null si no existe o no pertenece al usuario.
     */
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

    /**
     * Retorna la fecha de check-in mas proxima entre todas las habitaciones de una reservacion.
     * Se usa para validar si la cancelacion esta dentro del plazo permitido.
     * @param reservacionId ID de la reservacion de la que se quiere obtener la fecha.
     * @return fecha de check-in mas reciente, o null si no se encuentran detalles.
     */
    public java.sql.Date obtenerFechaCheckInMasReciente(int reservacionId) {
        String sql = "SELECT MIN(FechaCheckIn) AS FechaMin " +
                "FROM DetallesReservacion WHERE ReservacionID = ?";

        List<java.sql.Date> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getDate("FechaMin"), reservacionId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Actualiza el estado de una reservacion a Cancelada (EstadoID = 4) y registra el motivo y la fecha.
     * @param reservacionId     ID de la reservacion a cancelar.
     * @param motivoCancelacion razon de la cancelacion ingresada por el usuario.
     */
    public void cancelarReservacion(int reservacionId, String motivoCancelacion) {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = 4, Fecha_Cancelacion = SYSDATE, Motivo_Cancelacion = ? " +
                "WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, motivoCancelacion, reservacionId);
    }

    /**
     * Busca una reservacion verificando que pertenezca a una agencia especifica, para validar antes de cancelar.
     * La relacion se resuelve a traves del usuario webservice vinculado a la agencia.
     * @param reservacionId ID de la reservacion a consultar.
     * @param agenciaId     ID de la agencia propietaria de la reservacion.
     * @return arreglo con {ID, EstadoID, Estado} o null si no existe o no pertenece a la agencia.
     */
    public Object[] obtenerReservacionAgenciaParaCancelar(int reservacionId, int agenciaId) {
        String sql = "SELECT r.ID, r.EstadoID, er.Estado " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "JOIN Agencia a ON a.usuariowebis_id = r.Usuario_ID " +
                "WHERE r.ID = ? AND a.ID = ?";

        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getInt("EstadoID"),
                rs.getString("Estado")
        }, reservacionId, agenciaId);

        return result.isEmpty() ? null : result.get(0);
    }
}