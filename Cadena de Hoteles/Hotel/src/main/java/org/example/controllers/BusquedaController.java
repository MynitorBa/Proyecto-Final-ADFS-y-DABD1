package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.BusquedaRequestDTO;
import org.example.services.BusquedaService;

import java.util.Map;

public class BusquedaController {

    private final BusquedaService busquedaService = new BusquedaService();

    public void registerRoutes(Javalin app) {

        // POST /busqueda — pública, cualquier persona puede buscar
        app.post("/busqueda", ctx -> {
            BusquedaRequestDTO request = ctx.bodyAsClass(BusquedaRequestDTO.class);
            try {
                ctx.status(200).json(busquedaService.buscar(request));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}