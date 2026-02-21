package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.CambiarContrasenaRequestDTO;
import org.example.dtos.CambiarTelefonoRequestDTO;
import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.helpers.CredencialesInvalidasException;
import org.example.services.UsuarioService;

import java.util.Map;

public class UsuarioController {

    private final UsuarioService usuarioService = new UsuarioService();

    public void registerRoutes(Javalin app) {

        // Rutas publicas

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
                        "mensaje",    "Usuario creado exitosamente",
                        "usuarioId",  nuevoId
                ));
            } catch (CamposDuplicadosException e) {
                ctx.status(409).json(Map.of(
                        "mensaje", "No se pudo crear el usuario, algunos campos ya existen",
                        "campos",  e.getDetalle()
                ));
            }
        });










        //Rutas Privadas

        // GET /usuarios/perfil
        app.get("/usuarios/perfil", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            ctx.json(usuarioService.obtenerPerfil(usuarioId));
        });

        // PATCH /usuarios/telefono
        app.patch("/usuarios/telefono", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            CambiarTelefonoRequestDTO request = ctx.bodyAsClass(CambiarTelefonoRequestDTO.class);
            try {
                usuarioService.cambiarTelefono(usuarioId, request.getTelefono());
                ctx.status(200).json(Map.of("mensaje", "Teléfono actualizado correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /usuarios/contrasena
        app.patch("/usuarios/contrasena", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            CambiarContrasenaRequestDTO request = ctx.bodyAsClass(CambiarContrasenaRequestDTO.class);
            try {
                usuarioService.cambiarContrasena(usuarioId, request.getContrasenaActual(), request.getContrasenaNueva());
                ctx.status(200).json(Map.of("mensaje", "Contraseña actualizada correctamente"));
            } catch (CredencialesInvalidasException e) {
                ctx.status(401).json(Map.of("mensaje", "La contraseña actual es incorrecta"));
            }
        });
    }
}