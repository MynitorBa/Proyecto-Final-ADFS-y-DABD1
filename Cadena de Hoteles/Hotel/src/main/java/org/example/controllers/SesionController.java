package org.example.controllers;

import io.javalin.Javalin;
import org.example.helpers.JwtHelper;
import org.example.services.SesionService;
import io.jsonwebtoken.Claims;

/**
 * Controller que expone el endpoint de consulta del estado de sesion actual.
 * Es publico para que el frontend pueda verificar la sesion sin importar si hay token o no.
 */
public class SesionController {

    private final SesionService sesionService = new SesionService();

    /**
     * Registra la ruta de sesion en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // Retorna el estado de sesion del usuario; responde sin sesion si el token es invalido o no existe
        app.get("/sesion", ctx -> {
            String token = ctx.cookie("auth_token");

            // Si no hay token o es invalido, retorna un estado sin sesion sin lanzar error
            if (token == null || token.isBlank() || !JwtHelper.esValido(token)) {
                ctx.status(200).json(sesionService.sinSesion());
                return;
            }

            // Extrae los datos del usuario desde el token JWT para construir la respuesta de sesion
            Claims claims    = JwtHelper.verificarToken(token);
            int    usuarioId = JwtHelper.getUsuarioId(claims);
            String username  = JwtHelper.getUsername(claims);
            int    rolId     = JwtHelper.getRolId(claims);

            ctx.status(200).json(sesionService.obtenerSesion(usuarioId, username, rolId));
        });
    }
}