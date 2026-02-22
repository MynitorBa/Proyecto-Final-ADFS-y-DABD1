package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.BusquedaRequestDTO;
import org.example.services.BusquedaService;
import org.example.helpers.JwtHelper;
import io.jsonwebtoken.Claims;

import java.util.Map;

public class BusquedaController {

    private final BusquedaService busquedaService = new BusquedaService();

    public void registerRoutes(Javalin app) {

        // POST /busqueda — pública, pero si hay sesión guarda el usuarioId
        app.post("/busqueda", ctx -> {
            BusquedaRequestDTO request = ctx.bodyAsClass(BusquedaRequestDTO.class);

            // Intentar leer el token si existe, sin bloquear si no hay
            Integer usuarioId = null;
            String token = ctx.cookie("auth_token");
            if (token != null && !token.isBlank() && JwtHelper.esValido(token)) {
                Claims claims = JwtHelper.verificarToken(token);
                usuarioId = JwtHelper.getUsuarioId(claims);
            }

            try {
                ctx.status(200).json(busquedaService.buscar(request, usuarioId));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}