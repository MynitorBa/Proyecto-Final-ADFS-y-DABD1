package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.BusquedaRequestDTO;
import org.example.services.BusquedaAgenciaService;

import java.util.Map;

public class BusquedaAgenciaController {

    private final BusquedaAgenciaService busquedaAgenciaService = new BusquedaAgenciaService();
/*
    public void registerRoutes(Javalin app) {

        // POST /agencia/busqueda — requiere sesión con rol Webservice
        app.post("/agencia/busqueda", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            int rolId     = ctx.attribute("rolId");

            // Verificar que sea un usuario webservice (rol 3)
            if (rolId != 3) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Webservice"));
                return;
            }

            BusquedaRequestDTO request = ctx.bodyAsClass(BusquedaRequestDTO.class);
            try {
                ctx.status(200).json(busquedaAgenciaService.buscar(request, usuarioId));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }*/
}