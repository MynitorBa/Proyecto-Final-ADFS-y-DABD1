package org.example.controllers;

import io.javalin.Javalin;
import org.example.services.ImagenService;

public class ImagenController {

    private final ImagenService imagenService = new ImagenService();

    public void registerRoutes(Javalin app) {

        // GET /imagenes/hotel/{id}
        app.get("/imagenes/hotel/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            byte[] imagen = imagenService.obtenerImagenHotel(id);
            if (imagen == null) { ctx.status(404); return; }
            ctx.contentType("image/jpeg").result(imagen);
        });

        // GET /imagenes/habitacion/{id}
        app.get("/imagenes/habitacion/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            byte[] imagen = imagenService.obtenerImagenHabitacion(id);
            if (imagen == null) { ctx.status(404); return; }
            ctx.contentType("image/jpeg").result(imagen);
        });

        // GET /imagenes/amenidad/{id}
        app.get("/imagenes/amenidad/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            byte[] imagen = imagenService.obtenerImagenAmenidad(id);
            if (imagen == null) { ctx.status(404); return; }
            ctx.contentType("image/jpeg").result(imagen);
        });
    }
}