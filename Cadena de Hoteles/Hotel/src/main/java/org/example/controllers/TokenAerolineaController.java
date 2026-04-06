package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.TokenAerolineaRequestDTO;
import org.example.helpers.AerolineaAuthMiddleware;
import org.example.services.TokenAerolineaService;

import java.util.Map;

/**
 * Controller que expone el endpoint de generacion de tokens de alianza.
 * Solo aerolineas autenticadas mediante X-Aerolinea-Token pueden acceder.
 */
public class TokenAerolineaController {

    private final TokenAerolineaService tokenService;

    /**
     * Crea una instancia de TokenAerolineaController con sus dependencias inyectadas.
     */
    public TokenAerolineaController(TokenAerolineaService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Registra la ruta de generacion de tokens en la aplicacion Javalin.
     *
     * <p>Endpoint: POST /aerolinea/token</p>
     * <p>Header requerido: X-Aerolinea-Token</p>
     * <p>Body esperado: { "ciudad": "Paris", "pais": "Francia" }</p>
     * <p>Respuesta exitosa 201: token generado, URL de redireccion y fecha de expiracion.</p>
     *
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        app.post("/aerolinea/token", ctx -> {
            if (!AerolineaAuthMiddleware.verificar(ctx)) return;

            String tokenHash = ctx.header("X-Aerolinea-Token");

            TokenAerolineaRequestDTO request = ctx.bodyAsClass(TokenAerolineaRequestDTO.class);
            try {
                ctx.status(201).json(tokenService.generarToken(request, tokenHash));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}