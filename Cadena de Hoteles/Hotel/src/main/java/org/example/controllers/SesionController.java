package org.example.controllers;

import io.javalin.Javalin;
import org.example.helpers.JwtHelper;
import org.example.services.SesionService;
import io.jsonwebtoken.Claims;

public class SesionController {

    private final SesionService sesionService = new SesionService();

    public void registerRoutes(Javalin app) {

        // GET /sesion
        // Esta ruta es pública para que el frontend pueda llamarla sin importar el estado
        app.get("/sesion", ctx -> {
            String token = ctx.cookie("auth_token");

            if (token == null || token.isBlank() || !JwtHelper.esValido(token)) {
                ctx.status(200).json(sesionService.sinSesion());
                return;
            }

            Claims claims = JwtHelper.verificarToken(token);
            int    usuarioId = JwtHelper.getUsuarioId(claims);
            String username  = JwtHelper.getUsername(claims);
            int    rolId     = JwtHelper.getRolId(claims);

            ctx.status(200).json(sesionService.obtenerSesion(usuarioId, username, rolId));
        });
    }
}