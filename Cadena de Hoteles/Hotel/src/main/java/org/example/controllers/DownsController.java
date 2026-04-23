package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.DownRequestDTO;
import org.example.services.DownsService;

import java.util.Map;

/**
 * Controller que gestiona las valoraciones negativas (downs) sobre comentarios.
 * Todas las rutas requieren sesion activa; el usuarioId se obtiene del contexto JWT.
 */
public class DownsController {

    private final DownsService downsService;

    /**
     * Crea una instancia de DownsController con sus dependencias inyectadas.
     */
    public DownsController(DownsService downsService) {
        this.downsService = downsService;
    }

    /**
     * Registra todas las rutas de downs en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Registra un down del usuario autenticado sobre un comentario especifico
        app.post("/comentarios/{id}/downs", this::handleAgregarDown);

        // Elimina el down que el usuario autenticado habia registrado en un comentario
        app.delete("/comentarios/{id}/downs", this::handleEliminarDown);

        // Actualiza el valor del down existente del usuario autenticado en un comentario
        app.patch("/comentarios/{id}/downs", this::handleActualizarDown);

        // Retorna todos los downs registrados por el usuario autenticado
        app.get("/downs", this::handleObtenerDowns);

        // Retorna los downs del usuario autenticado filtrados por hotel
        app.get("/downs/hotel/{hotelId}", this::handleObtenerDownsPorHotel);
    }

    void handleAgregarDown(Context ctx) {
        int usuarioId    = ctx.attribute("usuarioId");
        int comentarioId = Integer.parseInt(ctx.pathParam("id"));
        DownRequestDTO request = ctx.bodyAsClass(DownRequestDTO.class);
        try {
            downsService.agregarDown(comentarioId, usuarioId, request.getValor());
            ctx.status(201).json(Map.of("mensaje", "Down agregado correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleEliminarDown(Context ctx) {
        int usuarioId    = ctx.attribute("usuarioId");
        int comentarioId = Integer.parseInt(ctx.pathParam("id"));
        try {
            downsService.eliminarDown(comentarioId, usuarioId);
            ctx.status(200).json(Map.of("mensaje", "Down eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleActualizarDown(Context ctx) {
        int usuarioId    = ctx.attribute("usuarioId");
        int comentarioId = Integer.parseInt(ctx.pathParam("id"));
        DownRequestDTO request = ctx.bodyAsClass(DownRequestDTO.class);
        try {
            downsService.actualizarDown(comentarioId, usuarioId, request.getValor());
            ctx.status(200).json(Map.of("mensaje", "Down actualizado correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleObtenerDowns(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        ctx.status(200).json(downsService.obtenerDownsDeUsuario(usuarioId));
    }

    void handleObtenerDownsPorHotel(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        int hotelId   = Integer.parseInt(ctx.pathParam("hotelId"));
        ctx.status(200).json(downsService.obtenerDownsDeUsuarioPorHotel(usuarioId, hotelId));
    }
}
