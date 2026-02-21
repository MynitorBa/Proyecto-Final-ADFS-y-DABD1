package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.services.UsuarioService;

public class UsuarioController {

    private final UsuarioService usuarioService = new UsuarioService();

    public void registerRoutes(Javalin app) {

        // GET /usuarios/validar
        // Body JSON: { "username": "...", "correo": "...", "pasaporte": "..." }
        app.get("/usuarios/validar", ctx -> {
            UsuarioValidacionRequestDTO request = ctx.bodyAsClass(UsuarioValidacionRequestDTO.class);
            ctx.json(usuarioService.validarDisponibilidad(request));
        });
    }
}