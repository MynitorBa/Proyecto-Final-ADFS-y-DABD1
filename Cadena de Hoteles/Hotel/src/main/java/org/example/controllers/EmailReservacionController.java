package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.helpers.EmailHelper;
import org.example.services.EmailReservacionService;

import java.util.Map;

/**
 * Controller que gestiona el envio de correos electronicos relacionados con reservaciones,
 * formulario de contacto y suscripciones al boletin informativo.
 */
public class EmailReservacionController {

    // Correo destino para notificaciones administrativas
    private static final String ADMIN_EMAIL = "distribuidorapine@gmail.com";

    private final EmailReservacionService emailService;

    /**
     * Crea una instancia de EmailReservacionController con sus dependencias inyectadas.
     */
    public EmailReservacionController(EmailReservacionService emailService) {
        this.emailService = emailService;
    }

    /**
     * Registra todas las rutas de correo en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Envia el correo de confirmacion de una reservacion al usuario dueno de la misma
        // Solo accesible para rol 1 (Administrador) y rol 2 (Usuario registrado)
        app.get("/reservaciones/{id}/correo", this::handleEnviarCorreoReservacion);

        // Recibe el formulario de contacto publico y notifica al administrador por correo
        app.post("/contacto", this::handleContacto);

        // Registra una suscripcion al boletin y notifica al administrador con el correo del suscriptor
        app.post("/newsletter", this::handleNewsletter);
    }

    void handleEnviarCorreoReservacion(Context ctx) {
        int usuarioId     = ctx.attribute("usuarioId");
        int rolId         = ctx.attribute("rolId");
        int reservacionId = Integer.parseInt(ctx.pathParam("id"));

        // Verifica que el rol tenga permiso para solicitar el envio del correo
        if (rolId != 1 && rolId != 2) {
            ctx.status(403).json(Map.of("mensaje", "Acceso denegado"));
            return;
        }

        try {
            emailService.enviarCorreoReservacion(reservacionId, usuarioId);
            ctx.status(200).json(Map.of("mensaje", "Correo enviado correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            ctx.status(500).json(Map.of("mensaje", "Error al enviar el correo: " + e.getMessage()));
        }
    }

    void handleContacto(Context ctx) {
        Map<String, String> body = ctx.bodyAsClass(Map.class);

        // Extrae y limpia los campos del formulario
        String nombre  = body.getOrDefault("nombre", "").trim();
        String correo  = body.getOrDefault("correo", "").trim();
        String asunto  = body.getOrDefault("asunto", "").trim();
        String mensaje = body.getOrDefault("mensaje", "").trim();

        // Valida que los campos obligatorios no esten vacios
        if (nombre.isEmpty() || correo.isEmpty() || mensaje.isEmpty()) {
            ctx.status(400).json(Map.of("mensaje", "Nombre, correo y mensaje son obligatorios"));
            return;
        }

        try {
            String html = construirCorreoContacto(nombre, correo, asunto, mensaje);
            EmailHelper.enviar(
                    ADMIN_EMAIL,
                    "\uD83D\uDCE9 Nuevo mensaje de contacto \u2014 " + (asunto.isEmpty() ? "Sin asunto" : asunto),
                    html
            );
            ctx.status(200).json(Map.of("mensaje", "Mensaje enviado correctamente"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("mensaje", "Error al enviar el mensaje: " + e.getMessage()));
        }
    }

    void handleNewsletter(Context ctx) {
        Map<String, String> body = ctx.bodyAsClass(Map.class);
        String correo = body.getOrDefault("correo", "").trim();

        // Valida que el correo tenga formato valido antes de procesar la suscripcion
        if (correo.isEmpty() || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            ctx.status(400).json(Map.of("mensaje", "Correo inv\u00E1lido"));
            return;
        }

        try {
            String html = construirCorreoNewsletter(correo);
            EmailHelper.enviar(
                    ADMIN_EMAIL,
                    "\uD83D\uDCEC Nueva suscripci\u00F3n al bolet\u00EDn \u2014 " + correo,
                    html
            );
            ctx.status(200).json(Map.of("mensaje", "Suscripci\u00F3n registrada correctamente"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("mensaje", "Error al registrar suscripci\u00F3n: " + e.getMessage()));
        }
    }

    /**
     * Construye el cuerpo HTML del correo de contacto que se envia al administrador.
     * @param nombre  nombre del remitente del formulario.
     * @param correo  correo electronico del remitente.
     * @param asunto  asunto indicado en el formulario, puede estar vacio.
     * @param mensaje contenido del mensaje enviado por el usuario.
     * @return string con el HTML completo listo para enviar.
     */
    private String construirCorreoContacto(String nombre, String correo, String asunto, String mensaje) {
        return "<!DOCTYPE html>"
                + "<html lang=\"es\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head>"
                + "<body style=\"margin:0;padding:0;background-color:#0f172a;font-family:'Segoe UI',Roboto,Arial,sans-serif;\">"
                + "<div style=\"max-width:600px;margin:0 auto;padding:20px 12px;\">"
                + "<div style=\"background:linear-gradient(135deg,#1e293b 0%,#334155 100%);border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.1);box-shadow:0 20px 60px rgba(0,0,0,0.4);\">"
                + "<div style=\"background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:24px 16px;text-align:center;\">"
                + "<h1 style=\"margin:0;font-size:20px;color:#ffffff;font-weight:700;\">\uD83D\uDCE9 Nuevo Mensaje de Contacto</h1>"
                + "<p style=\"margin:6px 0 0;font-size:13px;color:rgba(255,255,255,0.8);\">Recibido desde el formulario de Miku Inn</p>"
                + "</div>"
                + "<div style=\"padding:20px 16px;\">"
                + "<div style=\"background:rgba(15,23,42,0.6);border:1px solid rgba(102,126,234,0.3);border-radius:12px;padding:16px;margin-bottom:16px;\">"
                + "<table style=\"width:100%;border-collapse:collapse;table-layout:fixed;\">"
                + "<tr><td style=\"padding:8px 0;font-size:13px;color:#64748b;width:80px;vertical-align:top;\">Nombre</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#f1f5f9;font-weight:600;word-break:break-word;\">" + esc(nombre) + "</td></tr>"
                + "<tr><td style=\"padding:8px 0;font-size:13px;color:#64748b;vertical-align:top;\">Correo</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#667eea;font-weight:600;word-break:break-all;\"><a href=\"mailto:" + esc(correo) + "\" style=\"color:#667eea;text-decoration:none;\">" + esc(correo) + "</a></td></tr>"
                + "<tr><td style=\"padding:8px 0;font-size:13px;color:#64748b;vertical-align:top;\">Asunto</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#f1f5f9;word-break:break-word;\">" + (asunto.isEmpty() ? "<em style=\"color:#64748b;\">Sin asunto</em>" : esc(asunto)) + "</td></tr>"
                + "</table></div>"
                + "<div style=\"background:rgba(15,23,42,0.6);border:1px solid rgba(255,255,255,0.08);border-radius:12px;padding:16px;\">"
                + "<h3 style=\"margin:0 0 10px;font-size:13px;color:#667eea;font-weight:700;text-transform:uppercase;letter-spacing:1px;\">Mensaje</h3>"
                + "<p style=\"margin:0;font-size:14px;color:#e2e8f0;line-height:1.7;white-space:pre-wrap;word-break:break-word;\">" + esc(mensaje) + "</p>"
                + "</div>"
                + "<div style=\"text-align:center;margin-top:16px;\">"
                + "<a href=\"mailto:" + esc(correo) + "\" style=\"display:inline-block;padding:12px 24px;background:linear-gradient(135deg,#667eea,#764ba2);color:#ffffff;text-decoration:none;border-radius:10px;font-size:14px;font-weight:700;\">Responder a " + esc(nombre) + " \u2192</a>"
                + "</div></div>"
                + "<div style=\"padding:12px 16px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;\">"
                + "<p style=\"margin:0;font-size:11px;color:#475569;\">Correo generado autom\u00E1ticamente por Miku Inn.</p>"
                + "</div>"
                + "</div></div></body></html>";
    }

    /**
     * Construye el cuerpo HTML del correo de notificacion de nueva suscripcion al boletin.
     * @param correo direccion de correo del nuevo suscriptor.
     * @return string con el HTML completo listo para enviar.
     */
    private String construirCorreoNewsletter(String correo) {
        return "<!DOCTYPE html>"
                + "<html lang=\"es\"><head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"margin:0;padding:0;background-color:#0f172a;font-family:'Segoe UI',Roboto,Arial,sans-serif;\">"
                + "<div style=\"max-width:600px;margin:0 auto;padding:40px 20px;\">"
                + "<div style=\"background:linear-gradient(135deg,#1e293b 0%,#334155 100%);border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.1);box-shadow:0 20px 60px rgba(0,0,0,0.4);\">"
                + "<div style=\"background:linear-gradient(135deg,#10b981 0%,#059669 100%);padding:30px;text-align:center;\">"
                + "<h1 style=\"margin:0;font-size:24px;color:#ffffff;font-weight:700;\">\uD83D\uDCEC Nueva Suscripci\u00F3n al Bolet\u00EDn</h1>"
                + "</div>"
                + "<div style=\"padding:30px;text-align:center;\">"
                + "<p style=\"font-size:15px;color:#94a3b8;margin:0 0 20px;\">Un nuevo usuario se ha suscrito al bolet\u00EDn de Miku Inn:</p>"
                + "<div style=\"display:inline-block;background:rgba(15,23,42,0.6);border:1px solid rgba(16,185,129,0.3);border-radius:12px;padding:16px 32px;\">"
                + "<p style=\"margin:0;font-size:18px;color:#10b981;font-weight:700;\">" + esc(correo) + "</p>"
                + "</div>"
                + "<div style=\"margin-top:20px;\">"
                + "<a href=\"mailto:" + esc(correo) + "\" style=\"display:inline-block;padding:10px 28px;background:linear-gradient(135deg,#667eea,#764ba2);color:#ffffff;text-decoration:none;border-radius:8px;font-size:13px;font-weight:600;\">Enviar correo \u2192</a>"
                + "</div></div>"
                + "<div style=\"padding:16px 30px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;\">"
                + "<p style=\"margin:0;font-size:11px;color:#475569;\">Notificaci\u00F3n autom\u00E1tica de Miku Inn.</p>"
                + "</div>"
                + "</div></div></body></html>";
    }

    /**
     * Escapa caracteres especiales HTML para evitar inyecciones en el contenido del correo.
     * @param t texto a escapar.
     * @return texto con los caracteres HTML reemplazados por sus entidades seguras.
     */
    private String esc(String t) {
        if (t == null) return "";
        return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}