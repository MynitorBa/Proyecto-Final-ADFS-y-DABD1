package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.CrearAgenciaAdminRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;
import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.services.AgenciaService;
import org.example.services.HandshakeService;

import java.util.Map;

/**
 * Controller que registra las rutas HTTP relacionadas con agencias.
 * Expone endpoints para el rol Webservice (rol 3) y para el rol Administrador (rol 2).
 */
public class AgenciaController {

    private final AgenciaService   agenciaService;
    private final HandshakeService handshakeService;

    /**
     * Crea una instancia de AgenciaController con sus dependencias inyectadas.
     */
    public AgenciaController(AgenciaService agenciaService,
                             HandshakeService handshakeService) {
        this.agenciaService   = agenciaService;
        this.handshakeService = handshakeService;
    }

    /**
     * Registra todas las rutas de agencias en la aplicacion Javalin.
     * Las rutas bajo /webservice requieren rol 3 y las de /admin requieren rol 2.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Lista las agencias asociadas al usuario autenticado
        app.get("/webservice/agencias", this::handleListarWebservice);

        // Crea una nueva agencia para el usuario autenticado (flujo del portal webservice)
        app.post("/webservice/agencias", this::handleCrearWebservice);

        // Cambia el estado de una agencia especifica del usuario autenticado
        app.patch("/webservice/agencias/{id}/estado", this::handleCambiarEstadoWebservice);

        // Elimina una agencia perteneciente al usuario autenticado
        app.delete("/webservice/agencias/{id}", this::handleEliminarWebservice);

        // Procesa el handshake de autenticacion entre sistemas externos y la plataforma
        app.post("/api/agencias/handshake", this::handleHandshake);

        // Retorna todas las agencias registradas en el sistema
        app.get("/admin/agencias", this::handleListarAdmin);

        // Crea una nueva agencia desde el panel de administracion asignandola a un usuario webservice
        app.post("/admin/agencias", this::handleCrearAdmin);

        // Edita los datos de una agencia especifica
        app.patch("/admin/agencias/{id}", this::handleEditarAdmin);
    }

    void handleListarWebservice(Context ctx) {
        if (!esWebservice(ctx)) { deny(ctx); return; }
        ctx.json(agenciaService.listarPorUsuario(usuarioId(ctx)));
    }

    void handleCrearWebservice(Context ctx) {
        if (!esWebservice(ctx)) { deny(ctx); return; }
        try {
            var agencia = agenciaService.crear(usuarioId(ctx), ctx.bodyAsClass(CrearAgenciaRequestDTO.class));
            ctx.status(201).json(agencia);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleCambiarEstadoWebservice(Context ctx) {
        if (!esWebservice(ctx)) { deny(ctx); return; }
        try {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            int nuevoEstado = Integer.parseInt(body.get("estadoId").toString());
            agenciaService.cambiarEstado(id(ctx, "id"), usuarioId(ctx), nuevoEstado);
            ctx.json(Map.of("mensaje", "Estado actualizado correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleEliminarWebservice(Context ctx) {
        if (!esWebservice(ctx)) { deny(ctx); return; }
        try {
            agenciaService.eliminar(id(ctx, "id"), usuarioId(ctx));
            ctx.json(Map.of("mensaje", "Agencia eliminada correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleHandshake(Context ctx) {
        System.out.println("[HANDSHAKE] url_agencia recibida: '" + ctx.body() + "'");
        try {
            HandshakeRequestDTO dto = ctx.bodyAsClass(HandshakeRequestDTO.class);
            System.out.println("[HANDSHAKE] url_agencia: '" + dto.getUrlAgencia() + "'");
            System.out.println("[HANDSHAKE] token_entrada: '" + dto.getTokenEntrada() + "'");

            HandshakeResponseDTO response = handshakeService.procesarHandshake(dto);
            ctx.json(response);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleListarAdmin(Context ctx) {
        if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
        ctx.json(agenciaService.listarTodas());
    }

    void handleCrearAdmin(Context ctx) {
        if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
        try {
            var agencia = agenciaService.crearDesdeAdmin(ctx.bodyAsClass(CrearAgenciaAdminRequestDTO.class));
            ctx.status(201).json(agencia);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleEditarAdmin(Context ctx) {
        if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
        try {
            agenciaService.editar(id(ctx, "id"), ctx.bodyAsClass(EditarAgenciaRequestDTO.class));
            ctx.json(Map.of("mensaje", "Agencia actualizada correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * Verifica si el usuario autenticado tiene rol Webservice (rol 3).
     * @param ctx contexto de la peticion HTTP.
     * @return true si el rolId del contexto es 3, false en caso contrario.
     */
    private boolean esWebservice(Context ctx) {
        Integer rolId = ctx.attribute("rolId");
        return rolId != null && rolId == 3;
    }

    /**
     * Verifica si el usuario autenticado tiene rol Administrador (rol 2).
     * @param ctx contexto de la peticion HTTP.
     * @return true si el rolId del contexto es 2, false en caso contrario.
     */
    private boolean esAdmin(Context ctx) {
        Integer rolId = ctx.attribute("rolId");
        return rolId != null && rolId == 2;
    }

    /**
     * Responde con 403 cuando el acceso requiere rol Webservice y el usuario no lo tiene.
     * @param ctx contexto de la peticion HTTP.
     */
    private void deny(Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Webservice"));
    }

    /**
     * Responde con 403 cuando el acceso requiere rol Administrador y el usuario no lo tiene.
     * @param ctx contexto de la peticion HTTP.
     */
    private void denyAdmin(Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
    }

    /**
     * Extrae y convierte a entero el path parameter indicado.
     * @param ctx   contexto de la peticion HTTP.
     * @param param nombre del path parameter a extraer.
     * @return valor del parametro convertido a int.
     */
    private int id(Context ctx, String param) {
        return Integer.parseInt(ctx.pathParam(param));
    }

    /**
     * Obtiene el ID del usuario autenticado desde los atributos del contexto.
     * @param ctx contexto de la peticion HTTP.
     * @return ID del usuario autenticado.
     * @throws IllegalStateException si el atributo usuarioId no esta presente en el contexto.
     */
    private int usuarioId(Context ctx) {
        Integer uid = ctx.attribute("usuarioId");
        if (uid == null) throw new IllegalStateException("Usuario no autenticado");
        return uid;
    }
}
