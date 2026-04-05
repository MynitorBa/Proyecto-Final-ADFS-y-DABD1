package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.CambiarContrasenaRequestDTO;
import org.example.dtos.CambiarRolRequestDTO;
import org.example.dtos.CambiarTelefonoRequestDTO;
import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.helpers.CredencialesInvalidasException;
import org.example.services.UsuarioService;

import java.util.Map;

/**
 * Controller que gestiona las operaciones sobre usuarios del sistema.
 * Expone rutas publicas de registro y validacion, rutas privadas para el perfil
 * del usuario autenticado, y rutas administrativas exclusivas para rol 2.
 */
public class UsuarioController {

    private final UsuarioService usuarioService = new UsuarioService();

    /**
     * Registra todas las rutas de usuarios en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Verifica si el username o correo proporcionados ya estan en uso
        app.get("/usuarios/validar", ctx -> {
            UsuarioValidacionRequestDTO request = ctx.bodyAsClass(UsuarioValidacionRequestDTO.class);
            ctx.json(usuarioService.validarDisponibilidad(request));
        });

        // Registra un nuevo usuario en el sistema; responde 409 si hay campos duplicados
        app.post("/usuarios/registrar", ctx -> {
            UsuarioValidacionRequestDTO request = ctx.bodyAsClass(UsuarioValidacionRequestDTO.class);
            try {
                int nuevoId = usuarioService.registrarUsuario(request);
                ctx.status(201).json(Map.of(
                        "mensaje",   "Usuario creado exitosamente",
                        "usuarioId", nuevoId
                ));
            } catch (CamposDuplicadosException e) {
                ctx.status(409).json(Map.of(
                        "mensaje", "No se pudo crear el usuario, algunos campos ya existen",
                        "campos",  e.getDetalle()
                ));
            }
        });

        // Retorna el perfil del usuario autenticado
        app.get("/usuarios/perfil", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            ctx.json(usuarioService.obtenerPerfil(usuarioId));
        });

        // Actualiza el numero de telefono del usuario autenticado
        app.patch("/usuarios/telefono", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            CambiarTelefonoRequestDTO request = ctx.bodyAsClass(CambiarTelefonoRequestDTO.class);
            try {
                usuarioService.cambiarTelefono(usuarioId, request.getTelefono());
                ctx.status(200).json(Map.of("mensaje", "Telefono actualizado correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Actualiza la contrasena del usuario autenticado validando la contrasena actual
        app.patch("/usuarios/contrasena", ctx -> {
            int usuarioId = ctx.attribute("usuarioId");
            CambiarContrasenaRequestDTO request = ctx.bodyAsClass(CambiarContrasenaRequestDTO.class);
            try {
                usuarioService.cambiarContrasena(usuarioId, request.getContrasenaActual(), request.getContrasenaNueva());
                ctx.status(200).json(Map.of("mensaje", "Contrasena actualizada correctamente"));
            } catch (CredencialesInvalidasException e) {
                ctx.status(401).json(Map.of("mensaje", "La contrasena actual es incorrecta"));
            }
        });

        // Retorna la lista completa de usuarios con su rol; exclusivo para administradores
        app.get("/admin/usuarios", ctx -> {
            int rolId = ctx.attribute("rolId");

            // Verifica que el usuario tenga rol Administrador antes de continuar
            if (rolId != 2) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
                return;
            }

            ctx.json(usuarioService.listarTodosUsuarios());
        });

        // Cambia el rol de un usuario especifico; exclusivo para administradores
        app.patch("/admin/usuarios/{id}/rol", ctx -> {
            int rolId = ctx.attribute("rolId");

            // Verifica que el usuario tenga rol Administrador antes de continuar
            if (rolId != 2) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
                return;
            }

            // Extrae el ID del usuario a modificar y el nuevo rol del cuerpo de la peticion
            int usuarioId  = Integer.parseInt(ctx.pathParam("id"));
            int nuevoRolId = ctx.bodyAsClass(CambiarRolRequestDTO.class).getRolId();

            try {
                usuarioService.cambiarRol(usuarioId, nuevoRolId);
                ctx.status(200).json(Map.of("mensaje", "Rol actualizado correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}