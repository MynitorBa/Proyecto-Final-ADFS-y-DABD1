package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.BusquedaRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.BusquedaAgenciaService;

import java.util.Map;

/**
 * Controller que expone el endpoint de busqueda para agencias externas.
 * Las peticiones se autentican mediante el header X-Agencia-Token.
 */
public class BusquedaAgenciaController {

    private final BusquedaAgenciaService busquedaAgenciaService;

    /**
     * Crea una instancia de BusquedaAgenciaController con sus dependencias inyectadas.
     */
    public BusquedaAgenciaController(BusquedaAgenciaService busquedaAgenciaService) {
        this.busquedaAgenciaService = busquedaAgenciaService;
    }

    /**
     * Registra la ruta de busqueda de agencias en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // Valida el token de agencia antes de procesar la busqueda
        app.post("/agencia/busqueda", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;

            // Extrae el token del header para identificar la agencia solicitante
            String token = ctx.header("X-Agencia-Token");

            BusquedaRequestDTO request = ctx.bodyAsClass(BusquedaRequestDTO.class);
            try {
                ctx.status(200).json(busquedaAgenciaService.buscarPorToken(request, token));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}