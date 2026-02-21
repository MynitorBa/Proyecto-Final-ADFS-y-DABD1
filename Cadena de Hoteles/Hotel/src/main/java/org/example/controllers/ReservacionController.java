package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.ReservacionRequestDTO;
import org.example.services.ReservacionService;

import java.util.Map;

public class ReservacionController {

    private final ReservacionService reservacionService = new ReservacionService();

    public void registerRoutes(Javalin app) {

        // POST /reservaciones — crear reservación
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

        // GET /reservaciones — todas las reservaciones del usuario en sesión
        app.get("/reservaciones", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            ctx.status(200).json(reservacionService.obtenerReservaciones(usuarioId));
        });
    }
}