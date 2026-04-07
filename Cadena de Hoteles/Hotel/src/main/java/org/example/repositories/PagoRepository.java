package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.PagoResponseDTO;

import java.util.List;

/**
 * Repository para el procesamiento de pagos de reservaciones de usuarios directos.
 * Maneja la validacion de reservaciones, confirmacion y generacion de facturas.
 */
public class PagoRepository {

    /**
     * Busca una reservacion verificando que pertenezca al usuario indicado.
     * Se usa para validar la reservacion antes de procesar el pago.
     * @param reservacionId ID de la reservacion a consultar.
     * @param usuarioId     ID del usuario propietario de la reservacion.
     * @return arreglo con {ID, No_Reservacion, Total, Estado, EstadoID} o null si no existe o no pertenece al usuario.
     */
    public Object[] obtenerReservacionParaPago(int reservacionId, int usuarioId) {
        String sql = "SELECT r.ID, r.No_Reservacion, r.Total, er.Estado, r.EstadoID " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "WHERE r.ID = ? AND r.Usuario_ID = ?";

        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getString("No_Reservacion"),
                rs.getDouble("Total"),
                rs.getString("Estado"),
                rs.getInt("EstadoID")
        }, reservacionId, usuarioId);

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Confirma una reservacion actualizando su estado a Confirmada (EstadoID = 2) y eliminando la fecha de expiracion.
     * @param reservacionId ID de la reservacion a confirmar.
     */
    public void confirmarReservacion(int reservacionId) {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = 2, Fecha_Expiracion = NULL " +
                "WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, reservacionId);
    }

    /**
     * Crea una factura asociada a una reservacion y retorna el ID generado.
     * @param reservacionId ID de la reservacion a facturar.
     * @param nit           numero de identificacion tributaria del cliente.
     * @param codigoPostal  codigo postal del cliente.
     * @param total         monto total de la factura.
     * @return ID de la factura recien insertada.
     */
    public int crearFactura(int reservacionId, String nit, String codigoPostal, double total) {
        String sql = "INSERT INTO Factura (ReservacionID, Fecha, NIT, Codigo_Postal, Total) " +
                "VALUES (?, SYSDATE, ?, ?, ?)";
        return DatabaseManager.executeInsertReturnId(sql, "ID",
                reservacionId, nit, codigoPostal, total);
    }

    /**
     * Retorna los datos completos de una factura junto con el estado y numero de su reservacion asociada.
     * @param facturaId ID de la factura a consultar.
     * @return PagoResponseDTO con los datos de la factura, o null si no existe.
     */
    public PagoResponseDTO obtenerFactura(int facturaId) {
        String sql = "SELECT f.ID, f.Fecha, f.NIT, f.Codigo_Postal, f.Total, " +
                "r.No_Reservacion, er.Estado " +
                "FROM Factura f " +
                "JOIN Reservacion  r  ON f.ReservacionID = r.ID " +
                "JOIN EstadoReserva er ON r.EstadoID      = er.ID " +
                "WHERE f.ID = ?";

        List<PagoResponseDTO> result = DatabaseManager.executeQuery(sql, rs -> {
            PagoResponseDTO dto = new PagoResponseDTO();
            dto.setFacturaId(rs.getInt("ID"));
            dto.setFecha(rs.getDate("Fecha").toString());
            dto.setNit(rs.getString("NIT"));
            dto.setCodigoPostal(rs.getString("Codigo_Postal"));
            dto.setTotal(rs.getDouble("Total"));
            dto.setNoReservacion(rs.getString("No_Reservacion"));
            dto.setEstado(rs.getString("Estado"));
            return dto;
        }, facturaId);

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Actualiza el total de una reservacion tras aplicar un descuento de alianza.
     * @param reservacionId ID de la reservacion a actualizar.
     * @param nuevoTotal    total ya con el descuento aplicado.
     */
    public void actualizarTotalReservacion(int reservacionId, double nuevoTotal) {
        String sql = "UPDATE Reservacion SET Total = ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, nuevoTotal, reservacionId);
    }

    /**
     * Aplica el mismo factor de descuento proporcionalmente a todos los detalles
     * de una reservacion, para que la suma de detalles coincida con el total de la reservacion.
     * @param reservacionId    ID de la reservacion cuyos detalles se van a actualizar.
     * @param factorDescuento  factor multiplicador (ej. 0.90 para 10% de descuento).
     */
    public void actualizarTotalDetalles(int reservacionId, double factorDescuento) {
        String sql = "UPDATE DetallesReservacion " +
                "SET Total = ROUND(Total * ?, 2) " +
                "WHERE ReservacionID = ?";
        DatabaseManager.executeUpdate(sql, factorDescuento, reservacionId);
    }


}