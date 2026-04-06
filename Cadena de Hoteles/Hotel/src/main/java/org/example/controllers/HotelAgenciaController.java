package org.example.controllers;

import io.javalin.Javalin;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.HotelAgenciaService;

/**
 * Controller que expone el endpoint de consulta de hoteles para agencias externas.
 * Requiere autenticacion mediante el header X-Agencia-Token.
 */
public class HotelAgenciaController {

    private final HotelAgenciaService service;

    /**
     * Crea una instancia de HotelAgenciaController con sus dependencias inyectadas.
     */
    public HotelAgenciaController(HotelAgenciaService service) {
        this.service = service;
    }

    /**
     * Registra la ruta de hoteles para agencias en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registrarRutas(Javalin app) {

        // Retorna el catalogo de hoteles disponibles para la agencia autenticada
        app.get("/api/hoteles-agencia", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;

            var hoteles = service.obtenerHotelesParaAgencia();
            ctx.json(hoteles);
        });
    }
}