package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.CancelacionRequestDTO;
import org.example.services.CancelacionService;

import java.util.Map;

public class CancelacionAgenciaController {

    private final CancelacionService cancelacionService = new CancelacionService();

    public void registerRoutes(Javalin app) {

        // PATCH /agencia/reservaciones/{id}/cancelar — requiere rol Webservice
        app.patch("/agencia/reservaciones/{id}/cancelar", ctx -> {
            int usuarioId     = ctx.attribute("usuarioId");
            int rolId         = ctx.attribute("rolId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

            if (rolId != 3) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Webservice"));
                return;
            }

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