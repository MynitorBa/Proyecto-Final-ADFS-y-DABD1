package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;
import org.example.services.AgenciaService;

import java.util.Map;

public class AgenciaController {

    private final AgenciaService agenciaService = new AgenciaService();

    public void registerRoutes(Javalin app) {


        // GET /webservice/agencias
        app.get("/webservice/agencias", ctx -> {
            if (!esWebservice(ctx)) { deny(ctx); return; }
            ctx.json(agenciaService.listarPorUsuario(usuarioId(ctx)));
        });

        // POST /webservice/agencias
        app.post("/webservice/agencias", ctx -> {
            if (!esWebservice(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(
                        agenciaService.crear(usuarioId(ctx), ctx.bodyAsClass(CrearAgenciaRequestDTO.class))
                );
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /webservice/agencias/{id}/estado
        app.patch("/webservice/agencias/{id}/estado", ctx -> {
            if (!esWebservice(ctx)) { deny(ctx); return; }
            try {
                Map<?, ?> body = ctx.bodyAsClass(Map.class);
                int nuevoEstado = Integer.parseInt(body.get("estadoId").toString());
                agenciaService.cambiarEstado(id(ctx, "id"), usuarioId(ctx), nuevoEstado);
                ctx.json(Map.of("mensaje", "Estado actualizado correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // DELETE /webservice/agencias/{id}
        app.delete("/webservice/agencias/{id}", ctx -> {
            if (!esWebservice(ctx)) { deny(ctx); return; }
            try {
                agenciaService.eliminar(id(ctx, "id"), usuarioId(ctx));
                ctx.json(Map.of("mensaje", "Agencia eliminada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // ════════════════════════════════════════════════════
        //  ADMIN — rutas accesibles solo por rol 2
        // ════════════════════════════════════════════════════

        // GET /admin/agencias  →  todas las agencias
        app.get("/admin/agencias", ctx -> {
            if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
            ctx.json(agenciaService.listarTodas());
        });

        // PATCH /admin/agencias/{id}  →  editar agencia
        app.patch("/admin/agencias/{id}", ctx -> {
            if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
            try {
                agenciaService.editar(id(ctx, "id"), ctx.bodyAsClass(EditarAgenciaRequestDTO.class));
                ctx.json(Map.of("mensaje", "Agencia actualizada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean esWebservice(Context ctx) {
        Integer rolId = ctx.attribute("rolId");
        return rolId != null && rolId == 3;
    }

    private boolean esAdmin(Context ctx) {
        Integer rolId = ctx.attribute("rolId");
        return rolId != null && rolId == 2;
    }

    private void deny(Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Webservice"));
    }

    private void denyAdmin(Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
    }

    private int id(Context ctx, String param) {
        return Integer.parseInt(ctx.pathParam(param));
    }

    private int usuarioId(Context ctx) {
        Integer uid = ctx.attribute("usuarioId");
        if (uid == null) throw new IllegalStateException("Usuario no autenticado");
        return uid;
    }
}