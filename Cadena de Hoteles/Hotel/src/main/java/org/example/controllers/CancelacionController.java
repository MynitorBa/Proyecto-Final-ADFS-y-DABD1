package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.CancelacionRequestDTO;
import org.example.services.CancelacionService;

import java.util.Map;

public class CancelacionController {

    private final CancelacionService cancelacionService = new CancelacionService();

    public void registerRoutes(Javalin app) {

        // PATCH /reservaciones/{id}/cancelar — requiere sesión activa
        app.patch("/reservaciones/{id}/cancelar", ctx -> {
            int usuarioId     = ctx.attribute("usuarioId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));
            CancelacionRequestDTO request = ctx.bodyAsClass(CancelacionRequestDTO.class);
            try {
                cancelacionService.cancelarReservacion(
                        reservacionId, usuarioId, request.getMotivoCancelacion()
                );
                ctx.status(200).json(Map.of("mensaje", "Reservación cancelada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}