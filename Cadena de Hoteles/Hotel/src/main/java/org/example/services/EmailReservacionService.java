package org.example.services;

import org.example.dtos.ReservacionDetalleDTO;
import org.example.helpers.EmailHelper;
import org.example.repositories.PdfReservacionRepository;

import java.util.List;

public class EmailReservacionService {

    private final PdfReservacionRepository repository = new PdfReservacionRepository();

    public void enviarCorreoReservacion(int reservacionId, int usuarioId) {

        if (!repository.perteneceAlUsuario(reservacionId, usuarioId)) {
            throw new IllegalArgumentException("Reservación no encontrada");
        }

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

        String asunto = "Miku Inn — Reservación " + primera.getNoReservacion();
        String html   = construirHtml(detalles, factura, primera);

        EmailHelper.enviar(correoUsuario, asunto, html);
    }

    private String construirHtml(List<ReservacionDetalleDTO> detalles,
                                 Object[] factura, ReservacionDetalleDTO primera) {
        StringBuilder sb = new StringBuilder();

        // Estilos base
        String bgBody     = "#f4f6f9";
        String bgCard     = "#ffffff";
        String bgHeader   = "#1e283c";
        String bgAccent   = "#2c3a52";
        String txtHeader  = "#e8edf5";
        String txtDark    = "#1a1a1a";
        String txtMid     = "#445060";
        String txtSoft    = "#788496";
        String accent     = "#3a527c";
        String border     = "#d5dae3";
        String bgRow      = "#f5f7fa";
        String bgLabel    = "#eef1f6";
        String statusColor = statusHex(primera.getEstado());

        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'></head>");
        sb.append("<body style='margin:0;padding:0;background:").append(bgBody).append(";font-family:Arial,Helvetica,sans-serif;'>");

        // Contenedor principal
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:").append(bgBody).append(";'><tr><td align='center' style='padding:32px 16px;'>");
        sb.append("<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>");

        // ── Header ──
        sb.append("<tr><td style='background:").append(bgHeader).append(";padding:28px 32px;border-radius:12px 12px 0 0;'>");
        sb.append("<table width='100%' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td style='color:").append(txtHeader).append(";'>");
        sb.append("<div style='font-size:22px;font-weight:bold;letter-spacing:1.5px;margin-bottom:4px;'>MIKU INN</div>");
        sb.append("<div style='font-size:12px;color:").append(txtSoft).append(";'>Hotel Boutique · Guatemala City</div>");
        sb.append("</td>");
        sb.append("<td align='right' style='color:").append(txtHeader).append(";'>");
        sb.append("<div style='font-size:11px;color:").append(txtSoft).append(";letter-spacing:1px;margin-bottom:4px;'>RESERVACIÓN</div>");
        sb.append("<div style='font-size:16px;font-weight:bold;'>").append(primera.getNoReservacion()).append("</div>");
        sb.append("</td>");
        sb.append("</tr></table>");
        sb.append("</td></tr>");

        // ── Barra de estado ──
        sb.append("<tr><td style='background:").append(bgAccent).append(";padding:12px 32px;border-bottom:2px solid ").append(accent).append(";'>");
        sb.append("<table width='100%' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td style='font-size:12px;color:").append(txtSoft).append(";'>Estado de tu reservación</td>");
        sb.append("<td align='right'><span style='display:inline-block;padding:4px 14px;border-radius:20px;font-size:11px;font-weight:bold;color:#fff;background:").append(statusColor).append(";'>");
        sb.append(primera.getEstado() != null ? primera.getEstado().toUpperCase() : "—");
        sb.append("</span></td>");
        sb.append("</tr></table>");
        sb.append("</td></tr>");

        // ── Cuerpo blanco ──
        sb.append("<tr><td style='background:").append(bgCard).append(";padding:0;'>");

        // Saludo
        sb.append("<div style='padding:28px 32px 20px;font-size:15px;color:").append(txtDark).append(";line-height:1.6;'>");
        sb.append("A continuación encontrarás el resumen de tu reservación en Miku Inn.");
        sb.append("</div>");

        // ── Datos de la reservación ──
        sb.append(seccionTitulo("Datos de la reservación", accent));
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='margin:0 32px 20px;width:calc(100% - 64px);border:1px solid ").append(border).append(";border-radius:6px;overflow:hidden;'>");
        filaDato(sb, "Nro. Reservación", primera.getNoReservacion(), bgLabel, border, txtMid, txtDark, true);
        filaDato(sb, "Hotel", primera.getNombreHotel(), bgRow, border, txtMid, txtDark, false);
        filaDato(sb, "Fecha creación", primera.getFechaCreacion(), bgLabel, border, txtMid, txtDark, false);
        filaDato(sb, "Total", "$ " + primera.getTotal(), bgRow, border, txtMid, accent, true);
        if (primera.getFechaCancelacion() != null) {
            filaDato(sb, "Cancelación", primera.getFechaCancelacion(), bgLabel, border, txtMid, txtDark, false);
        }
        if (primera.getMotivoCancelacion() != null) {
            filaDato(sb, "Motivo", primera.getMotivoCancelacion(), bgRow, border, txtMid, txtDark, false);
        }
        sb.append("</table>");

        // ── Habitaciones ──
        sb.append(seccionTitulo("Habitaciones reservadas", accent));

        for (int i = 0; i < detalles.size(); i++) {
            ReservacionDetalleDTO d = detalles.get(i);
            sb.append("<div style='margin:0 32px 16px;border:1px solid ").append(border).append(";border-radius:6px;overflow:hidden;'>");

            // Header de habitación
            sb.append("<div style='background:").append(bgAccent).append(";padding:10px 16px;'>");
            sb.append("<span style='font-size:12px;font-weight:bold;color:").append(txtHeader).append(";'>");
            sb.append("Hab. ").append(i + 1).append(" — ").append(d.getTipoHabitacion());
            sb.append("</span>");
            sb.append("<span style='float:right;font-size:12px;font-weight:bold;color:").append(txtHeader).append(";'>")
                    .append(d.getNombreHotel()).append("</span>");
            sb.append("</div>");

            // Datos de habitación
            sb.append("<table width='100%' cellpadding='0' cellspacing='0'>");
            filaDato(sb, "Tipo de cama", d.getTipoCama(), bgLabel, border, txtMid, txtDark, false);
            filaDato(sb, "Check-in", d.getFechaCheckIn(), bgRow, border, txtMid, txtDark, false);
            filaDato(sb, "Check-out", d.getFechaCheckOut(), bgLabel, border, txtMid, txtDark, false);
            filaDato(sb, "Personas", String.valueOf(d.getCantidadPersonas()), bgRow, border, txtMid, txtDark, false);
            filaDato(sb, "Subtotal", "$ " + d.getTotalDetalle(), bgLabel, border, txtMid, accent, true);
            sb.append("</table>");
            sb.append("</div>");
        }

        // ── Total general ──
        sb.append("<div style='margin:8px 32px 24px;background:").append(bgAccent).append(";border-radius:6px;padding:16px 20px;'>");
        sb.append("<table width='100%' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td style='font-size:13px;font-weight:bold;color:").append(txtHeader).append(";'>TOTAL RESERVACIÓN</td>");
        sb.append("<td align='right' style='font-size:18px;font-weight:bold;color:#fff;'>$ ").append(primera.getTotal()).append("</td>");
        sb.append("</tr></table>");
        sb.append("</div>");

        // ── Factura ──
        if (factura != null) {
            sb.append(seccionTitulo("Datos de facturación", accent));
            sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='margin:0 32px 20px;width:calc(100% - 64px);border:1px solid ").append(border).append(";border-radius:6px;overflow:hidden;'>");
            filaDato(sb, "NIT", String.valueOf(factura[2]), bgLabel, border, txtMid, txtDark, false);
            filaDato(sb, "Código postal", String.valueOf(factura[3]), bgRow, border, txtMid, txtDark, false);
            filaDato(sb, "Fecha emisión", String.valueOf(factura[1]), bgLabel, border, txtMid, txtDark, false);
            filaDato(sb, "Total facturado", "$ " + factura[4], bgRow, border, txtMid, accent, true);
            sb.append("</table>");
        }

        // ── Pie informativo ──
        sb.append("<div style='padding:20px 32px 28px;font-size:12px;color:").append(txtSoft).append(";line-height:1.6;border-top:1px solid ").append(border).append(";margin-top:8px;'>");
        sb.append("Check-in: 15:00 hrs · Check-out: 12:00 hrs<br>");
        sb.append("Cancelaciones con menos de 24 hrs de anticipación generan cargo del 100%.<br>");
        sb.append("Si tienes alguna consulta, escríbenos a info@mikuinn.com o llámanos al +502 4276-8687.");
        sb.append("</div>");

        sb.append("</td></tr>"); // fin cuerpo blanco

        // ── Footer ──
        sb.append("<tr><td style='background:").append(bgHeader).append(";padding:20px 32px;border-radius:0 0 12px 12px;text-align:center;'>");
        sb.append("<div style='font-size:11px;color:").append(txtSoft).append(";'>Miku Inn · Guatemala City, Guatemala · info@mikuinn.com</div>");
        sb.append("</td></tr>");

        sb.append("</table>"); // fin tabla 600
        sb.append("</td></tr></table>"); // fin tabla contenedor

        sb.append("</body></html>");

        return sb.toString();
    }

    // ── Helpers ──

    private String seccionTitulo(String titulo, String color) {
        return "<div style='padding:0 32px;margin-bottom:12px;'>" +
                "<div style='font-size:11px;font-weight:bold;color:" + color +
                ";letter-spacing:0.8px;text-transform:uppercase;padding-bottom:6px;border-bottom:2px solid " + color + ";display:inline-block;'>" +
                titulo + "</div></div>";
    }

    private void filaDato(StringBuilder sb, String label, String valor,
                          String bgColor, String borderColor, String labelColor,
                          String valorColor, boolean bold) {
        sb.append("<tr>");
        sb.append("<td style='padding:10px 16px;font-size:12px;font-weight:bold;color:").append(labelColor)
                .append(";background:").append(bgColor)
                .append(";border-bottom:1px solid ").append(borderColor)
                .append(";width:40%;'>").append(label).append("</td>");
        sb.append("<td style='padding:10px 16px;font-size:12px;color:").append(valorColor)
                .append(";background:#fff;border-bottom:1px solid ").append(borderColor).append(";");
        if (bold) sb.append("font-weight:bold;");
        sb.append("'>").append(valor != null ? valor : "—").append("</td>");
        sb.append("</tr>");
    }

    private String statusHex(String estado) {
        if (estado == null) return "#788496";
        return switch (estado.toLowerCase()) {
            case "confirmada" -> "#1a683c";
            case "cancelada"  -> "#841818";
            case "pendiente"  -> "#764c10";
            default           -> "#788496";
        };
    }
}