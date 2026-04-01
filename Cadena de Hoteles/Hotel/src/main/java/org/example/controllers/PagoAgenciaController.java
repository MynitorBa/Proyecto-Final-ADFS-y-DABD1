package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.PagoAgenciaRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.PagoAgenciaService;

import java.util.Map;

public class PagoAgenciaController {

    private final PagoAgenciaService pagoAgenciaService = new PagoAgenciaService();

    public void registerRoutes(Javalin app) {

        // POST /agencia/reservaciones/{id}/pago
        app.post("/agencia/reservaciones/{id}/pago", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;

            int agenciaId     = ctx.attribute("agenciaId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

            PagoAgenciaRequestDTO request = ctx.bodyAsClass(PagoAgenciaRequestDTO.class);
            try {
                ctx.status(200).json(pagoAgenciaService.procesarPago(reservacionId, agenciaId, request));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}