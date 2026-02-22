package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.ComentarioRequestDTO;
import org.example.services.ComentarioService;

import java.util.Map;

public class ComentarioController {

    private final ComentarioService comentarioService = new ComentarioService();

    public void registerRoutes(Javalin app) {

        // POST /comentarios — requiere sesión
        app.post("/comentarios", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            ComentarioRequestDTO request = ctx.bodyAsClass(ComentarioRequestDTO.class);
            try {
                ctx.status(201).json(comentarioService.agregarComentario(request, usuarioId));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // GET /comentarios/usuario — comentarios del usuario en sesión, requiere sesión
        app.get("/comentarios/usuario", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            ctx.status(200).json(comentarioService.obtenerComentariosPorUsuario(usuarioId));
        });

        // GET /comentarios/hotel/{hotelId} — pública
        app.get("/comentarios/hotel/{hotelId}", ctx -> {
            int hotelId = Integer.parseInt(ctx.pathParam("hotelId"));
            ctx.status(200).json(comentarioService.obtenerComentariosPorHotel(hotelId));
        });
    }
}