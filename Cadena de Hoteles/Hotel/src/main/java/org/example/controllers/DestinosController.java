package org.example.controllers;

import io.javalin.Javalin;
import org.example.services.DestinosService;

/**
 * Controller que expone el endpoint publico de destinos disponibles.
 * No requiere autenticacion; retorna hoteles activos con sus imagenes.
 */
public class DestinosController {

    private final DestinosService destinosService;

    /**
     * Crea una instancia de DestinosController con sus dependencias inyectadas.
     */
    public DestinosController(DestinosService destinosService) {
        this.destinosService = destinosService;
    }

    /**
     * Registra la ruta de destinos en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // Retorna todos los destinos activos disponibles para busqueda publica
        app.get("/destinos", ctx -> {
            ctx.status(200).json(destinosService.obtenerDestinos());
        });
    }
}