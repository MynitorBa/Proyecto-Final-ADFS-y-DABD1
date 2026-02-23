package org.example.services;

import org.example.dtos.ReservacionDetalleDTO;
import org.example.helpers.EmailHelper;
import org.example.repositories.PdfReservacionRepository;

import java.util.List;

public class EmailReservacionService {

    private final PdfReservacionRepository repository = new PdfReservacionRepository();

    public void enviarCorreoReservacion(int reservacionId, int usuarioId) {

        // Verificar que la reservación pertenece al usuario
        if (!repository.perteneceAlUsuario(reservacionId, usuarioId)) {
            throw new IllegalArgumentException("Reservación no encontrada");
        }

        // Obtener correo del usuario
        String correoUsuario = repository.obtenerCorreoUsuario(usuarioId);
        if (correoUsuario == null) {
            throw new IllegalArgumentException("No se encontró el correo del usuario");
        }

        List<ReservacionDetalleDTO> detalles = repository.obtenerDetalles(reservacionId);
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("La reservación no tiene detalles");
        }

        Object[] factura = repository.obtenerFactura(reservacionId);
        ReservacionDetalleDTO primera = detalles.get(0);

        String asunto = "Reservación " + primera.getNoReservacion() + " — Detalles";
        String html   = construirHtml(detalles, factura, primera);

        EmailHelper.enviar(correoUsuario, asunto, html);
    }

    private String construirHtml(List<ReservacionDetalleDTO> detalles,
                                 Object[] factura, ReservacionDetalleDTO primera) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>");

        // ----------------------Encabezado---------------------
        sb.append("<h2 style='color:#2c3e50;'>Reservación ").append(primera.getNoReservacion()).append("</h2>");

        // -------------------------Datos generales -----------------------
        sb.append("<h3>Datos de la Reservación</h3>");
        sb.append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse; width:100%;'>");
        sb.append("<tr><td><b>Número</b></td><td>").append(primera.getNoReservacion()).append("</td></tr>");
        sb.append("<tr><td><b>Estado</b></td><td>").append(primera.getEstado()).append("</td></tr>");
        sb.append("<tr><td><b>Fecha creación</b></td><td>").append(primera.getFechaCreacion()).append("</td></tr>");
        sb.append("<tr><td><b>Total</b></td><td>Q ").append(primera.getTotal()).append("</td></tr>");
        if (primera.getFechaCancelacion() != null) {
            sb.append("<tr><td><b>Fecha cancelación</b></td><td>").append(primera.getFechaCancelacion()).append("</td></tr>");
        }
        if (primera.getMotivoCancelacion() != null) {
            sb.append("<tr><td><b>Motivo cancelación</b></td><td>").append(primera.getMotivoCancelacion()).append("</td></tr>");
        }
        sb.append("</table>");

        // ----------------------------- Habitaciones--------------------
        sb.append("<h3>Habitaciones Reservadas</h3>");
        for (ReservacionDetalleDTO detalle : detalles) {
            sb.append("<h4 style='color:#555;'>").append(detalle.getNombreHotel())
                    .append(" — ").append(detalle.getTipoHabitacion()).append("</h4>");
            sb.append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse; width:100%; margin-bottom:12px;'>");
            sb.append("<tr><td><b>Tipo de cama</b></td><td>").append(detalle.getTipoCama()).append("</td></tr>");
            sb.append("<tr><td><b>Check-in</b></td><td>").append(detalle.getFechaCheckIn()).append("</td></tr>");
            sb.append("<tr><td><b>Check-out</b></td><td>").append(detalle.getFechaCheckOut()).append("</td></tr>");
            sb.append("<tr><td><b>Personas</b></td><td>").append(detalle.getCantidadPersonas()).append("</td></tr>");
            sb.append("<tr><td><b>Subtotal</b></td><td>Q ").append(detalle.getTotalDetalle()).append("</td></tr>");
            sb.append("</table>");
        }

        // -----------------------------Factura ---------------------
        if (factura != null) {
            sb.append("<h3>Factura</h3>");
            sb.append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse; width:100%;'>");
            sb.append("<tr><td><b>NIT</b></td><td>").append(factura[2]).append("</td></tr>");
            sb.append("<tr><td><b>Código postal</b></td><td>").append(factura[3]).append("</td></tr>");
            sb.append("<tr><td><b>Fecha</b></td><td>").append(factura[1]).append("</td></tr>");
            sb.append("<tr><td><b>Total facturado</b></td><td>Q ").append(factura[4]).append("</td></tr>");
            sb.append("</table>");
        }

        sb.append("<br><p style='color:#888; font-size:12px;'>Gracias por su reservación.</p>");
        sb.append("</body></html>");

        return sb.toString();
    }
}