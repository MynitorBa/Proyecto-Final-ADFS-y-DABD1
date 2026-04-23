package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.BusquedaRequestDTO;
import org.example.helpers.AerolineaAuthMiddleware;
import org.example.services.BusquedaAerolineaService;

import java.util.Map;

/**
 * Controller que expone el endpoint de busqueda para aerolineas aliadas.
 * Las peticiones se autentican mediante el header X-Aerolinea-Token.
 */
public class BusquedaAerolineaController {

    private final BusquedaAerolineaService busquedaAerolineaService;

    public BusquedaAerolineaController(BusquedaAerolineaService busquedaAerolineaService) {
        this.busquedaAerolineaService = busquedaAerolineaService;
    }

    /**
     * Registra la ruta de busqueda de aerolineas en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {
        app.post("/aerolinea/busqueda", this::handleBuscar);
    }

    void handleBuscar(Context ctx) {
        if (!AerolineaAuthMiddleware.verificar(ctx)) return;

        String token = ctx.header("X-Aerolinea-Token");

        BusquedaRequestDTO request = ctx.bodyAsClass(BusquedaRequestDTO.class);
        try {
            ctx.status(200).json(busquedaAerolineaService.buscar(request, token));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }
}
