package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.ReservacionRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.ReservacionAgenciaService;

import java.util.Map;

/**
 * Controller que gestiona las reservaciones realizadas por agencias externas.
 * Todas las rutas requieren autenticacion mediante el header X-Agencia-Token.
 */
public class ReservacionAgenciaController {

    private final ReservacionAgenciaService reservacionAgenciaService;

    /**
     * Crea una instancia de ReservacionAgenciaController con sus dependencias inyectadas.
     */
    public ReservacionAgenciaController(ReservacionAgenciaService reservacionAgenciaService) {
        this.reservacionAgenciaService = reservacionAgenciaService;
    }

    /**
     * Registra todas las rutas de reservaciones de agencias en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {
        // Crea una nueva reservacion en nombre de la agencia autenticada
        app.post("/agencia/reservaciones", this::handleCrearReservacion);

        // Retorna todas las reservaciones asociadas a la agencia autenticada
        app.get("/agencia/reservaciones", this::handleObtenerReservaciones);

        // Marca una reservacion especifica como expirada si pertenece a la agencia autenticada
        app.post("/agencia/reservaciones/{id}/expirar", this::handleExpirarReservacion);

        // Retorna el detalle completo de una reservacion perteneciente a la agencia autenticada
        app.get("/agencia/reservaciones/{id}", this::handleObtenerDetalleReservacion);
    }

    void handleCrearReservacion(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;
        int agenciaId = ctx.attribute("agenciaId");

        ReservacionRequestDTO request = ctx.bodyAsClass(ReservacionRequestDTO.class);
        try {
            var reservacion = reservacionAgenciaService.crearReservacion(request, agenciaId);
            ctx.status(201).json(reservacion);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            ctx.status(500).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleObtenerReservaciones(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;
        int agenciaId = ctx.attribute("agenciaId");

        ctx.status(200).json(reservacionAgenciaService.obtenerReservaciones(agenciaId));
    }

    void handleExpirarReservacion(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;

        // Extrae el ID de la reservacion desde el path y la agencia del contexto
        int reservacionId = Integer.parseInt(ctx.pathParam("id"));
        int agenciaId     = ctx.attribute("agenciaId");

        try {
            reservacionAgenciaService.expirarReservacion(reservacionId, agenciaId);
            ctx.json(Map.of("mensaje", "Reservacion expirada correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleObtenerDetalleReservacion(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;

        // Extrae la agencia autenticada y el ID de la reservacion desde el path
        int agenciaId     = ctx.attribute("agenciaId");
        int reservacionId = Integer.parseInt(ctx.pathParam("id"));

        try {
            var detalle = reservacionAgenciaService.obtenerDetalleReservacion(reservacionId, agenciaId);
            ctx.status(200).json(detalle);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            ctx.status(500).json(Map.of("mensaje", e.getMessage()));
        }
    }
}
