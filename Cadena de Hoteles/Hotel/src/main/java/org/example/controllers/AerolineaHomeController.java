package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.services.AerolineaAdminService;

/**
 * Controller publico que expone el listado de aerolineas activas con su URL Home
 * para uso en secciones de recomendaciones del frontend. Sin restriccion de rol.
 */
public class AerolineaHomeController {

    private final AerolineaAdminService aerolineaAdminService;

    public AerolineaHomeController(AerolineaAdminService aerolineaAdminService) {
        this.aerolineaAdminService = aerolineaAdminService;
    }

    /**
     * Registra la ruta publica de recomendaciones de aerolineas.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {
        app.get("/aerolineas/home", this::handleListarHome);
    }

    // Retorna nombre y URL Home de todas las aerolineas activas
    void handleListarHome(Context ctx) {
        ctx.json(aerolineaAdminService.listarHome());
    }
}