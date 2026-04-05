package org.example.controllers;

import io.javalin.Javalin;
import org.example.services.ImagenService;

/**
 * Controller que expone los endpoints publicos de descarga de imagenes.
 * Sirve imagenes de hoteles, habitaciones y amenidades en formato JPEG.
 */
public class ImagenController {

    private final ImagenService imagenService = new ImagenService();

    /**
     * Registra las rutas de imagenes en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Retorna la imagen de un hotel; responde 404 si no existe
        app.get("/imagenes/hotel/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            byte[] imagen = imagenService.obtenerImagenHotel(id);
            if (imagen == null) { ctx.status(404); return; }
            ctx.contentType("image/jpeg").result(imagen);
        });

        // Retorna la imagen de una habitacion; responde 404 si no existe
        app.get("/imagenes/habitacion/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            byte[] imagen = imagenService.obtenerImagenHabitacion(id);
            if (imagen == null) { ctx.status(404); return; }
            ctx.contentType("image/jpeg").result(imagen);
        });

        // Retorna la imagen de una amenidad; responde 404 si no existe
        app.get("/imagenes/amenidad/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            byte[] imagen = imagenService.obtenerImagenAmenidad(id);
            if (imagen == null) { ctx.status(404); return; }
            ctx.contentType("image/jpeg").result(imagen);
        });
    }
}