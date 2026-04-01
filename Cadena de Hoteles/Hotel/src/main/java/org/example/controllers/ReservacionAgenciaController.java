package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.ReservacionRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.ReservacionAgenciaService;

import java.util.Map;

public class ReservacionAgenciaController {

    private final ReservacionAgenciaService reservacionAgenciaService = new ReservacionAgenciaService();

    public void registerRoutes(Javalin app) {

        // POST /agencia/reservaciones
        app.post("/agencia/reservaciones", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;
            int agenciaId = ctx.attribute("agenciaId");

            ReservacionRequestDTO request = ctx.bodyAsClass(ReservacionRequestDTO.class);
            try {
                ctx.status(201).json(reservacionAgenciaService.crearReservacion(request, agenciaId));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // GET /agencia/reservaciones
        app.get("/agencia/reservaciones", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;
            int agenciaId = ctx.attribute("agenciaId");

            ctx.status(200).json(reservacionAgenciaService.obtenerReservaciones(agenciaId));
        });

        // POST /agencia/reservaciones/{id}/expirar
        app.post("/agencia/reservaciones/{id}/expirar", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));
            int agenciaId = ctx.attribute("agenciaId");

            try {
                reservacionAgenciaService.expirarReservacion(reservacionId, agenciaId);
                ctx.json(Map.of("mensaje", "Reservación expirada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });


        // GET /agencia/reservaciones/{id}
        app.get("/agencia/reservaciones/{id}", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;
            int agenciaId = ctx.attribute("agenciaId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

            try {
                ctx.status(200).json(reservacionAgenciaService.obtenerDetalleReservacion(reservacionId, agenciaId));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}