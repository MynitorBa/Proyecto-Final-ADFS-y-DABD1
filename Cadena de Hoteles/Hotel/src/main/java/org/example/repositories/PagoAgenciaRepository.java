package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.PagoResponseDTO;

import java.util.List;

public class PagoAgenciaRepository {

    // Verifica que la reservación pertenece a la agencia y está pendiente
    public Object[] obtenerReservacionParaPago(int reservacionId, int agenciaId) {
        String sql = "SELECT r.ID, r.No_Reservacion, r.Total, er.Estado, r.EstadoID " +
                "FROM Reservacion r " +
                "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "JOIN Usuario u ON r.Usuario_ID = u.ID " +
                "JOIN Agencia a ON a.USUARIOWEBIS_ID = u.ID " +
                "WHERE r.ID = ? AND a.ID = ?";

        List<Object[]> result = DatabaseManager.executeQuery(sql, rs -> new Object[]{
                rs.getInt("ID"),
                rs.getString("No_Reservacion"),
                rs.getDouble("Total"),
                rs.getString("Estado"),
                rs.getInt("EstadoID")
        }, reservacionId, agenciaId);

        return result.isEmpty() ? null : result.get(0);
    }

    // Confirmar reservación: estado 2, quitar expiración
    public void confirmarReservacion(int reservacionId) {
        String sql = "UPDATE Reservacion " +
                "SET EstadoID = 2, Fecha_Expiracion = NULL " +
                "WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, reservacionId);
    }

    // Crear factura
    public int crearFactura(int reservacionId, String nit, String codigoPostal, double total) {
        String sql = "INSERT INTO Factura (ReservacionID, Fecha, NIT, Codigo_Postal, Total) " +
                "VALUES (?, SYSDATE, ?, ?, ?)";
        return DatabaseManager.executeInsertReturnId(sql, "ID",
                reservacionId, nit, codigoPostal, total);
    }

    // Obtener factura para la respuesta
    public PagoResponseDTO obtenerFactura(int facturaId) {
        String sql = "SELECT f.ID, f.Fecha, f.NIT, f.Codigo_Postal, f.Total, " +
                "r.No_Reservacion, er.Estado " +
                "FROM Factura f " +
                "JOIN Reservacion r   ON f.ReservacionID = r.ID " +
                "JOIN EstadoReserva er ON r.EstadoID     = er.ID " +
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
}