package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.PagoRequestDTO;
import org.example.services.PagoService;

import java.util.Map;

public class PagoController {

    private final PagoService pagoService = new PagoService();

    public void registerRoutes(Javalin app) {

        // POST /reservaciones/{id}/pago — requiere sesión activa
        app.post("/reservaciones/{id}/pago", ctx -> {
            int usuarioId     = ctx.attribute("usuarioId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));
            PagoRequestDTO request = ctx.bodyAsClass(PagoRequestDTO.class);

            try {
                ctx.status(200).json(pagoService.procesarPago(reservacionId, usuarioId, request));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}