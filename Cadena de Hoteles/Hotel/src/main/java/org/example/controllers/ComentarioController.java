package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.ComentarioRequestDTO;
import org.example.services.ComentarioService;
import org.example.helpers.AgenciaAuthMiddleware;

import java.util.Map;

/**
 * Controller que gestiona los endpoints relacionados con comentarios de hoteles.
 * Expone rutas publicas, rutas protegidas por sesion de usuario y rutas para agencias externas.
 */
public class ComentarioController {

    private final ComentarioService comentarioService = new ComentarioService();

    /**
     * Registra todas las rutas de comentarios en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Agrega un comentario a un hotel en nombre del usuario autenticado
        app.post("/comentarios", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            ComentarioRequestDTO request = ctx.bodyAsClass(ComentarioRequestDTO.class);
            try {
                ctx.status(201).json(comentarioService.agregarComentario(request, usuarioId));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Retorna todos los comentarios realizados por el usuario autenticado
        app.get("/comentarios/usuario", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            ctx.status(200).json(comentarioService.obtenerComentariosPorUsuario(usuarioId));
        });

        // Retorna los comentarios de un hotel especifico, accesible sin autenticacion
        app.get("/comentarios/hotel/{hotelId}", ctx -> {
            int hotelId = Integer.parseInt(ctx.pathParam("hotelId"));
            ctx.status(200).json(comentarioService.obtenerComentariosPorHotel(hotelId));
        });

        // Retorna los comentarios de un hotel para agencias autenticadas mediante X-Agencia-Token
        app.get("/agencia/comentarios/hotel/{hotelId}", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;
            int hotelId = Integer.parseInt(ctx.pathParam("hotelId"));
            try {
                ctx.status(200).json(comentarioService.obtenerComentariosPorHotel(hotelId));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}