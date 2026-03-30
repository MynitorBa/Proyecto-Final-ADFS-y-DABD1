package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.BusquedaRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.BusquedaAgenciaService;

import java.util.Map;

public class BusquedaAgenciaController {

    private final BusquedaAgenciaService busquedaAgenciaService = new BusquedaAgenciaService();

    public void registerRoutes(Javalin app) {

        // POST /agencia/busqueda — autenticación por X-Agencia-Token
        app.post("/agencia/busqueda", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;

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