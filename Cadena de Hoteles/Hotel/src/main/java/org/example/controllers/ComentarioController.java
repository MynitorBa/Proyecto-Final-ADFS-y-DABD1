package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.ComentarioRequestDTO;
import org.example.services.ComentarioService;
import org.example.helpers.AgenciaAuthMiddleware;

import java.util.Map;

/**
 * Controller que gestiona los endpoints relacionados con comentarios de hoteles.
 * Expone rutas publicas, rutas protegidas por sesion de usuario y rutas para agencias externas.
 */
public class ComentarioController {

    private final ComentarioService comentarioService;

    /**
     * Crea una instancia de ComentarioController con sus dependencias inyectadas.
     */
    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    /**
     * Registra todas las rutas de comentarios en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Agrega un comentario a un hotel en nombre del usuario autenticado
        app.post("/comentarios", this::handleAgregarComentario);

        // Retorna todos los comentarios realizados por el usuario autenticado
        app.get("/comentarios/usuario", this::handleObtenerPorUsuario);

        // Retorna los comentarios de un hotel especifico, accesible sin autenticacion
        app.get("/comentarios/hotel/{hotelId}", this::handleObtenerPorHotel);

        // Retorna los comentarios de un hotel para agencias autenticadas mediante X-Agencia-Token
        app.get("/agencia/comentarios/hotel/{hotelId}", this::handleObtenerPorHotelAgencia);
    }

    void handleAgregarComentario(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        ComentarioRequestDTO request = ctx.bodyAsClass(ComentarioRequestDTO.class);
        try {
            var comentario = comentarioService.agregarComentario(request, usuarioId);
            ctx.status(201).json(comentario);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleObtenerPorUsuario(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        ctx.status(200).json(comentarioService.obtenerComentariosPorUsuario(usuarioId));
    }

    void handleObtenerPorHotel(Context ctx) {
        int hotelId = Integer.parseInt(ctx.pathParam("hotelId"));
        ctx.status(200).json(comentarioService.obtenerComentariosPorHotel(hotelId));
    }

    void handleObtenerPorHotelAgencia(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;
        int hotelId = Integer.parseInt(ctx.pathParam("hotelId"));
        try {
            ctx.status(200).json(comentarioService.obtenerComentariosPorHotel(hotelId));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }
}
