package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.CancelacionRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.CancelacionService;

import java.util.Map;

public class CancelacionAgenciaController {

    private final CancelacionService cancelacionService = new CancelacionService();

    public void registerRoutes(Javalin app) {

        // GET /agencia/reservaciones/{id}/puede-cancelar
        app.get("/agencia/reservaciones/{id}/puede-cancelar", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;
            int agenciaId     = ctx.attribute("agenciaId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

            try {
                var resultado = cancelacionService.puedeCancelar(reservacionId, agenciaId);
                ctx.status(200).json(resultado);
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /agencia/reservaciones/{id}/cancelar
        app.patch("/agencia/reservaciones/{id}/cancelar", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;
            int agenciaId     = ctx.attribute("agenciaId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

            CancelacionRequestDTO request = ctx.bodyAsClass(CancelacionRequestDTO.class);
            try {
                cancelacionService.cancelarReservacionAgencia(
                        reservacionId, agenciaId, request.getMotivoCancelacion()
                );
                ctx.status(200).json(Map.of("mensaje", "Reservación cancelada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}