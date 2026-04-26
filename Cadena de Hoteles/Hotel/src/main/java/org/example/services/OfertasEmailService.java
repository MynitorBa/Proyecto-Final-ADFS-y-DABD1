package org.example.services;

import org.example.helpers.EmailHelper;
import org.example.repositories.OfertasRepository;
import org.example.repositories.OfertasRepository.HabitacionOferta;
import org.example.repositories.OfertasRepository.UsuarioOferta;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Servicio en segundo plano que envia ofertas de paquetes hoteleros por correo
 * a todos los usuarios que optaron por recibirlas al registrarse.
 * Corre cada 1 hora desde que arranca el servidor y tambien puede invocarse
 * de forma puntual para enviar la oferta inmediatamente al registrarse.
 */
public class OfertasEmailService {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private final OfertasRepository repository;

    // Descuento fijo aplicado visualmente en el correo (15 %)
    private static final double DESCUENTO_PCT = 0.15;

    /**
     * Crea una instancia de OfertasEmailService con sus dependencias inyectadas.
     */
    public OfertasEmailService(OfertasRepository repository) {
        this.repository = repository;
    }

    /**
     * Arranca el hilo programado que envia ofertas cada 1 hora.
     * El primer envio ocurre exactamente 1 hora despues del arranque.
     */
    public void iniciar() {
        scheduler.scheduleAtFixedRate(this::enviarOfertasTodos, 1, 1, TimeUnit.HOURS);
        System.out.println("[OfertasEmailService] Hilo de ofertas iniciado - envia cada 1 hora.");
    }

    /**
     * Detiene el hilo del scheduler al apagar el servidor.
     */
    public void detener() {
        scheduler.shutdown();
        System.out.println("[OfertasEmailService] Hilo de ofertas detenido.");
    }

    /**
     * Envia el correo de ofertas inmediatamente a un usuario recien registrado
     * que haya optado por recibirlas. Se invoca desde UsuarioService tras el registro.
     *
     * @param usuarioId ID del usuario recien registrado.
     */
    public void enviarOfertasAUsuario(int usuarioId) {
        try {
            UsuarioOferta u = repository.obtenerUsuarioPorId(usuarioId);
            if (u == null) return;
            List<HabitacionOferta> habitaciones = repository.obtenerHabitacionesDisponibles();
            if (habitaciones.isEmpty()) return;
            enviarCorreo(u, habitaciones);
        } catch (Exception e) {
            System.err.println("[OfertasEmailService] Error al enviar oferta al usuario "
                    + usuarioId + ": " + e.getMessage());
        }
    }

    /**
     * Ciclo principal del scheduler: obtiene todos los usuarios con preferencias
     * y envia el correo de ofertas a cada uno.
     */
    private void enviarOfertasTodos() {
        try {
            List<UsuarioOferta> usuarios = repository.obtenerUsuariosConPreferencias();
            if (usuarios.isEmpty()) return;

            List<HabitacionOferta> habitaciones = repository.obtenerHabitacionesDisponibles();
            if (habitaciones.isEmpty()) return;

            int enviados = 0;
            for (UsuarioOferta u : usuarios) {
                try {
                    enviarCorreo(u, habitaciones);
                    enviados++;
                } catch (Exception e) {
                    System.err.println("[OfertasEmailService] Fallo al enviar a "
                            + u.correo + ": " + e.getMessage());
                }
            }
            System.out.println("[OfertasEmailService] Ofertas enviadas a " + enviados + " usuario(s).");
        } catch (Exception e) {
            System.err.println("[OfertasEmailService] Error en ciclo de ofertas: " + e.getMessage());
        }
    }

    /**
     * Construye y envia el correo HTML de ofertas a un usuario especifico.
     *
     * @param usuario     datos del usuario destinatario.
     * @param habitaciones lista de habitaciones disponibles para incluir en el correo.
     */
    private void enviarCorreo(UsuarioOferta usuario, List<HabitacionOferta> habitaciones) {
        String asunto = "\uD83C\uDFE8 Ofertas exclusivas para ti en Miku Inn";
        String html   = construirHtml(usuario, habitaciones);
        EmailHelper.enviar(usuario.correo, asunto, html);
    }

    /**
     * Construye el HTML completo del correo de ofertas.
     * Muestra hasta 6 tarjetas de habitaciones con precio tachado + precio promo,
     * badge de descuento y detalles del hotel.
     *
     * @param usuario     datos del usuario para personalizar el saludo.
     * @param habitaciones lista de habitaciones a mostrar.
     * @return string con el HTML del correo.
     */
    private String construirHtml(UsuarioOferta usuario, List<HabitacionOferta> habitaciones) {

        // Paleta de colores (mismo estilo que EmailReservacionService)
        String bgBody    = "#f4f6f9";
        String bgCard    = "#ffffff";
        String bgHeader  = "#1e283c";
        String bgAccent  = "#2c3a52";
        String txtHeader = "#e8edf5";
        String txtDark   = "#1a1a1a";
        String txtMid    = "#445060";
        String txtSoft   = "#788496";
        String accent    = "#3a527c";
        String border    = "#d5dae3";
        String promoRed  = "#dc2626";
        String promoGreen = "#16a34a";

        // Limitar a 6 tarjetas para no saturar el correo
        List<HabitacionOferta> seleccion = habitaciones.size() > 6
                ? habitaciones.subList(0, 6)
                : habitaciones;

        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'></head>");
        sb.append("<body style='margin:0;padding:0;background:").append(bgBody)
          .append(";font-family:Arial,Helvetica,sans-serif;'>");

        // Contenedor principal
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background:")
          .append(bgBody).append(";'><tr><td align='center' style='padding:32px 16px;'>");
        sb.append("<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>");

        // Header
        sb.append("<tr><td style='background:").append(bgHeader)
          .append(";padding:28px 32px;border-radius:12px 12px 0 0;'>");
        sb.append("<table width='100%' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td style='color:").append(txtHeader).append(";'>");
        sb.append("<div style='font-size:22px;font-weight:bold;letter-spacing:1.5px;margin-bottom:4px;'>MIKU INN</div>");
        sb.append("<div style='font-size:12px;color:").append(txtSoft)
          .append(";'>Hotel Boutique \u00B7 Guatemala City</div>");
        sb.append("</td>");
        sb.append("<td align='right'>");
        sb.append("<span style='display:inline-block;background:").append(promoRed)
          .append(";color:#fff;font-size:13px;font-weight:bold;padding:6px 16px;border-radius:20px;'>")
          .append((int)(DESCUENTO_PCT * 100)).append("% DESCUENTO</span>");
        sb.append("</td></tr></table></td></tr>");

        // Subheader con saludo
        sb.append("<tr><td style='background:").append(bgAccent)
          .append(";padding:14px 32px;border-bottom:2px solid ").append(accent).append(";'>");
        sb.append("<div style='font-size:14px;color:").append(txtHeader)
          .append(";'>\u00A1Hola, <strong>").append(escHtml(usuario.nombre))
          .append("</strong>! Tenemos paquetes especiales para ti esta semana.</div>");
        sb.append("</td></tr>");

        // Cuerpo
        sb.append("<tr><td style='background:").append(bgCard).append(";padding:28px 32px 8px;'>");

        sb.append("<div style='font-size:15px;color:").append(txtDark)
          .append(";line-height:1.7;margin-bottom:24px;'>")
          .append("Descubre nuestras habitaciones con precios exclusivos. ")
          .append("Reserva ahora y disfruta del ")
          .append("<span style='color:").append(promoRed).append(";font-weight:bold;'>")
          .append((int)(DESCUENTO_PCT * 100)).append("% de descuento</span> ")
          .append("en tu primera noche.")
          .append("</div>");

        // Titulo de seccion
        sb.append("<div style='font-size:11px;font-weight:bold;color:").append(accent)
          .append(";letter-spacing:0.8px;text-transform:uppercase;padding-bottom:6px;")
          .append("border-bottom:2px solid ").append(accent)
          .append(";display:inline-block;margin-bottom:20px;'>")
          .append("Paquetes disponibles</div>");

        // Grid de tarjetas de habitaciones (2 por fila)
        sb.append("<table width='100%' cellpadding='0' cellspacing='0'>");
        for (int i = 0; i < seleccion.size(); i++) {
            if (i % 2 == 0) sb.append("<tr>");
            tarjetaHabitacion(sb, seleccion.get(i), border, txtMid, txtDark, txtSoft,
                              bgAccent, txtHeader, promoRed, promoGreen, accent);
            if (i % 2 == 1 || i == seleccion.size() - 1) {
                // Si la fila tiene solo 1 tarjeta, rellenamos con celda vacia
                if (i % 2 == 0) sb.append("<td width='50%' style='padding:0 0 16px 8px;'></td>");
                sb.append("</tr>");
            }
        }
        sb.append("</table>");

        // Boton CTA
        sb.append("<div style='text-align:center;padding:20px 0 28px;'>");
        sb.append("<a href='http://localhost:5173' style='display:inline-block;padding:14px 44px;")
          .append("background:linear-gradient(135deg,#667eea,#764ba2);")
          .append("color:#fff;text-decoration:none;border-radius:10px;font-size:15px;font-weight:700;")
          .append("box-shadow:0 4px 20px rgba(102,126,234,0.4);'>")
          .append("Ver todos los paquetes \u2192</a>");
        sb.append("</div>");

        // Nota legal
        sb.append("<div style='font-size:12px;color:").append(txtSoft)
          .append(";border-top:1px solid ").append(border)
          .append(";padding:16px 0 24px;line-height:1.6;'>")
          .append("Precios expresados en d\u00F3lares (USD) por noche. El descuento aplica en la primera noche de tu reservaci\u00F3n. ")
          .append("Sujeto a disponibilidad. Para dejar de recibir estas ofertas, actualiza tus preferencias en tu perfil.")
          .append("</div>");

        sb.append("</td></tr>");

        // Footer
        sb.append("<tr><td style='background:").append(bgHeader)
          .append(";padding:20px 32px;border-radius:0 0 12px 12px;text-align:center;'>");
        sb.append("<div style='font-size:11px;color:").append(txtSoft)
          .append(";'>Miku Inn \u00B7 Guatemala City, Guatemala \u00B7 info@mikuinn.com</div>");
        sb.append("</td></tr>");

        sb.append("</table></td></tr></table></body></html>");
        return sb.toString();
    }

    /**
     * Agrega la tarjeta HTML de una habitacion al StringBuilder del correo.
     * Muestra precio original tachado, precio promo, badge de descuento,
     * tipo de cama, capacidad, metros cuadrados y nombre de hotel + ciudad.
     */
    private void tarjetaHabitacion(StringBuilder sb, HabitacionOferta h,
                                   String border, String txtMid, String txtDark,
                                   String txtSoft, String bgAccent, String txtHeader,
                                   String promoRed, String promoGreen, String accent) {
        double precioPromo    = h.precioPorNoche * (1 - DESCUENTO_PCT);
        String precioOrigFmt  = String.format("%.2f", h.precioPorNoche);
        String precioPromoFmt = String.format("%.2f", precioPromo);

        // Celda del 50% de ancho
        boolean esIzquierda = true; // alternamos padding via orden de llamada
        sb.append("<td width='50%' valign='top' style='padding:0 8px 16px 0;'>");
        sb.append("<div style='border:1px solid ").append(border)
          .append(";border-radius:8px;overflow:hidden;'>");

        // Encabezado de la tarjeta
        sb.append("<div style='background:").append(bgAccent)
          .append(";padding:10px 14px;'>");
        sb.append("<div style='font-size:13px;font-weight:bold;color:").append(txtHeader)
          .append(";white-space:nowrap;overflow:hidden;text-overflow:ellipsis;'>")
          .append(escHtml(h.tipoHabitacion)).append("</div>");
        sb.append("<div style='font-size:11px;color:#94a3b8;margin-top:2px;'>")
          .append(escHtml(h.nombre)).append("</div>");
        sb.append("</div>");

        // Cuerpo de la tarjeta
        sb.append("<div style='padding:12px 14px;background:#fff;'>");

        // Precio con descuento
        sb.append("<div style='margin-bottom:10px;'>");
        sb.append("<span style='font-size:12px;color:").append(txtSoft)
          .append(";text-decoration:line-through;'>$").append(precioOrigFmt).append("</span>");
        sb.append(" <span style='font-size:16px;font-weight:bold;color:").append(promoGreen)
          .append(";'>$").append(precioPromoFmt).append("</span>");
        sb.append(" <span style='font-size:10px;color:#fff;background:").append(promoRed)
          .append(";padding:2px 7px;border-radius:10px;font-weight:bold;'>-")
          .append((int)(DESCUENTO_PCT * 100)).append("%</span>");
        sb.append("</div>");

        // Detalles: tipo de cama, capacidad, m2, ciudad
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='font-size:12px;'>");
        filaDetalle(sb, "\uD83D\uDECF\uFE0F Cama",
                    h.tipoCama != null ? escHtml(h.tipoCama) : "-", txtMid, txtDark);
        filaDetalle(sb, "\uD83D\uDC65 Capacidad",
                    h.capacidadMaxima + " persona" + (h.capacidadMaxima != 1 ? "s" : ""),
                    txtMid, txtDark);
        filaDetalle(sb, "\uD83D\uDCCF Tama\u00F1o",
                    String.format("%.0f m\u00B2", h.metrosCuadrados), txtMid, txtDark);
        filaDetalle(sb, "\uD83D\uDCCD Ciudad",
                    escHtml(h.ciudad) + ", " + escHtml(h.pais), txtMid, txtDark);
        if (h.precioPorPersona > 0) {
            filaDetalle(sb, "\uD83D\uDC64 +Persona",
                        "+$" + String.format("%.2f", h.precioPorPersona) + "/noche", txtMid, txtDark);
        }
        sb.append("</table>");

        sb.append("</div></div></td>");
    }

    /**
     * Agrega una fila de dos celdas (etiqueta / valor) dentro de la tabla de detalles.
     */
    private void filaDetalle(StringBuilder sb, String label, String valor,
                             String labelColor, String valorColor) {
        sb.append("<tr>")
          .append("<td style='padding:3px 0;color:").append(labelColor)
          .append(";'>").append(label).append("</td>")
          .append("<td style='padding:3px 0;color:").append(valorColor)
          .append(";font-weight:600;text-align:right;'>").append(valor).append("</td>")
          .append("</tr>");
    }

    /**
     * Escapa caracteres especiales HTML para evitar inyeccion en el cuerpo del correo.
     */
    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
