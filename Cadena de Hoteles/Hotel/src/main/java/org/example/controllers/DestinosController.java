package org.example.controllers;

import io.javalin.Javalin;
import org.example.services.DestinosService;

public class DestinosController {

    private final DestinosService destinosService = new DestinosService();

    public void registerRoutes(Javalin app) {

        // GET /destinos — pública, devuelve todos los hoteles activos con imágenes
        app.get("/destinos", ctx -> {
            ctx.status(200).json(destinosService.obtenerDestinos());
        });
    }
}