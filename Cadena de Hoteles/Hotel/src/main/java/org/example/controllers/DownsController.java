package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.DownRequestDTO;
import org.example.services.DownsService;

import java.util.Map;

public class DownsController {

    private final DownsService downsService = new DownsService();

    public void registerRoutes(Javalin app) {

        // POST /comentarios/{id}/downs — requiere sesión
        app.post("/comentarios/{id}/downs", ctx -> {
            int usuarioId    = ctx.attribute("usuarioId");
            int comentarioId = Integer.parseInt(ctx.pathParam("id"));
            DownRequestDTO request = ctx.bodyAsClass(DownRequestDTO.class);
            try {
                downsService.agregarDown(comentarioId, usuarioId, request.getValor());
                ctx.status(201).json(Map.of("mensaje", "Down agregado correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // DELETE /comentarios/{id}/downs — requiere sesión
        app.delete("/comentarios/{id}/downs", ctx -> {
            int usuarioId    = ctx.attribute("usuarioId");
            int comentarioId = Integer.parseInt(ctx.pathParam("id"));
            try {
                downsService.eliminarDown(comentarioId, usuarioId);
                ctx.status(200).json(Map.of("mensaje", "Down eliminado correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /comentarios/{id}/downs — requiere sesión
        app.patch("/comentarios/{id}/downs", ctx -> {
            int usuarioId    = ctx.attribute("usuarioId");
            int comentarioId = Integer.parseInt(ctx.pathParam("id"));
            DownRequestDTO request = ctx.bodyAsClass(DownRequestDTO.class);
            try {
                downsService.actualizarDown(comentarioId, usuarioId, request.getValor());
                ctx.status(200).json(Map.of("mensaje", "Down actualizado correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}