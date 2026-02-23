package org.example.controllers;

import io.javalin.Javalin;
import org.example.services.EmailReservacionService;

import java.util.Map;

public class EmailReservacionController {

    private final EmailReservacionService emailService = new EmailReservacionService();

    public void registerRoutes(Javalin app) {

        // GET /reservaciones/{id}/correo — envía el correo al usuario dueño de la reservación
        // Solo roles 1 (Admin) y 2 (Usuario registrado)
        app.get("/reservaciones/{id}/correo", ctx -> {
            int usuarioId     = ctx.attribute("usuarioId");
            int rolId         = ctx.attribute("rolId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

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
        });
    }
}