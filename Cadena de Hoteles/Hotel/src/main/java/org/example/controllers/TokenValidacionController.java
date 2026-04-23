package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.services.TokenValidacionService;

import java.util.Map;

/**
 * Controller que expone el endpoint de validacion de tokens de alianza.
 * Requiere sesion activa del usuario; el token de alianza se recibe como
 * query parameter en la URL.
 */
public class TokenValidacionController {

    private final TokenValidacionService tokenValidacionService;

    /**
     * Crea una instancia de TokenValidacionController con sus dependencias inyectadas.
     */
    public TokenValidacionController(TokenValidacionService tokenValidacionService) {
        this.tokenValidacionService = tokenValidacionService;
    }

    /**
     * Registra la ruta de validacion de tokens en la aplicacion Javalin.
     *
     * <p>Endpoint: GET /alianza/validar?token=uuid</p>
     * <p>Requiere JWT activo en el header Authorization.</p>
     * <p>Respuesta exitosa 200: ciudad, pais, porcentaje de descuento y fecha de expiracion.</p>
     * <p>Respuesta 400: si el token no existe, ya fue usado o expiro.</p>
     *
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // El usuarioId lo inyecta el AuthMiddleware, confirma que el usuario tiene sesion activa
        app.get("/alianza/validar", this::handleValidar);
    }

    void handleValidar(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");

        String token = ctx.queryParam("token");
        if (token == null || token.isBlank()) {
            ctx.status(400).json(Map.of("mensaje", "Token requerido"));
            return;
        }

        try {
            var resultado = tokenValidacionService.validar(token);
            ctx.status(200).json(resultado);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }
}
