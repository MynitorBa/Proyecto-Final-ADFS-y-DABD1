package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.services.UsuarioService;

import java.util.Map;

public class UsuarioController {

    private final UsuarioService usuarioService = new UsuarioService();

    public void registerRoutes(Javalin app) {

        // GET /usuarios/validar
        app.get("/usuarios/validar", ctx -> {
            UsuarioValidacionRequestDTO request = ctx.bodyAsClass(UsuarioValidacionRequestDTO.class);
            ctx.json(usuarioService.validarDisponibilidad(request));
        });

        // POST /usuarios/registrar
        app.post("/usuarios/registrar", ctx -> {
            UsuarioValidacionRequestDTO request = ctx.bodyAsClass(UsuarioValidacionRequestDTO.class);
            try {
                int nuevoId = usuarioService.registrarUsuario(request);
                ctx.status(201).json(Map.of(
                        "mensaje", "Usuario creado exitosamente",
                        "usuarioId", nuevoId
                ));
            } catch (CamposDuplicadosException e) {
                ctx.status(409).json(Map.of(
                        "mensaje", "No se pudo crear el usuario, algunos campos ya existen",
                        "campos",  e.getDetalle()
                ));
            }
        });
    }
}