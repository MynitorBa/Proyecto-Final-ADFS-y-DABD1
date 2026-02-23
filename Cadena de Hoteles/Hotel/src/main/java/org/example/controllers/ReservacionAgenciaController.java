package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.ReservacionRequestDTO;
import org.example.services.ReservacionAgenciaService;

import java.util.Map;

public class ReservacionAgenciaController {

    private final ReservacionAgenciaService reservacionAgenciaService = new ReservacionAgenciaService();

    public void registerRoutes(Javalin app) {

        // POST /agencia/reservaciones — crear reservación con descuento
        app.post("/agencia/reservaciones", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            int rolId     = ctx.attribute("rolId");

            if (rolId != 3) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Webservice"));
                return;
            }

            ReservacionRequestDTO request = ctx.bodyAsClass(ReservacionRequestDTO.class);
            try {
                ctx.status(201).json(reservacionAgenciaService.crearReservacion(request, usuarioId));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // GET /agencia/reservaciones — todas las reservaciones de la agencia
        app.get("/agencia/reservaciones", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            int rolId     = ctx.attribute("rolId");

            if (rolId != 3) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Webservice"));
                return;
            }

            ctx.status(200).json(reservacionAgenciaService.obtenerReservaciones(usuarioId));
        });
    }
}