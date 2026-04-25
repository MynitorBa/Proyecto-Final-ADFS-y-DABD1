package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.ImagenService;

/**
 * Controller que expone los endpoints de descarga de imagenes para agencias externas.
 * Todas las rutas requieren autenticacion mediante el header X-Agencia-Token.
 * Sirve imagenes de hoteles, habitaciones y amenidades en formato JPEG.
 */
public class ImagenAgenciaController {

    private final ImagenService imagenService;

    /**
     * Crea una instancia de ImagenAgenciaController con sus dependencias inyectadas.
     */
    public ImagenAgenciaController(ImagenService imagenService) {
        this.imagenService = imagenService;
    }

    /**
     * Registra las rutas de imagenes para agencias en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Retorna la imagen de un hotel; requiere token de agencia valido
        app.get("/agencia/imagenes/hotel/{id}", this::handleObtenerImagenHotel);

        // Retorna la imagen de una habitacion; requiere token de agencia valido
        app.get("/agencia/imagenes/habitacion/{id}", this::handleObtenerImagenHabitacion);

        // Retorna la imagen de una amenidad; requiere token de agencia valido
        app.get("/agencia/imagenes/amenidad/{id}", this::handleObtenerImagenAmenidad);
    }

    void handleObtenerImagenHotel(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;
        int id = Integer.parseInt(ctx.pathParam("id"));
        byte[] imagen = imagenService.obtenerImagenHotel(id);
        if (imagen == null) { ctx.status(404); return; }
        ctx.contentType("image/jpeg").result(imagen);
    }

    void handleObtenerImagenHabitacion(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;
        int id = Integer.parseInt(ctx.pathParam("id"));
        byte[] imagen = imagenService.obtenerImagenHabitacion(id);
        if (imagen == null) { ctx.status(404); return; }
        ctx.contentType("image/jpeg").result(imagen);
    }

    void handleObtenerImagenAmenidad(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;
        int id = Integer.parseInt(ctx.pathParam("id"));
        byte[] imagen = imagenService.obtenerImagenAmenidad(id);
        if (imagen == null) { ctx.status(404); return; }
        ctx.contentType("image/jpeg").result(imagen);
    }
}