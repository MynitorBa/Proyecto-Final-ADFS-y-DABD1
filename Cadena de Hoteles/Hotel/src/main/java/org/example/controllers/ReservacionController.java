package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.ReservacionRequestDTO;
import org.example.services.ReservacionService;

import java.util.Map;

public class ReservacionController {

    private final ReservacionService reservacionService = new ReservacionService();

    public void registerRoutes(Javalin app) {

        // POST /reservaciones — requiere sesión activa
        app.post("/reservaciones", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            ReservacionRequestDTO request = ctx.bodyAsClass(ReservacionRequestDTO.class);
            try {
                ctx.status(201).json(reservacionService.crearReservacion(request, usuarioId));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}