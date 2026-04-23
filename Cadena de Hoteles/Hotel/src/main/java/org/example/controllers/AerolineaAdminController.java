package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.CrearAerolineaAdminRequestDTO;
import org.example.dtos.EditarAerolineaRequestDTO;
import org.example.services.AerolineaAdminService;

import java.util.Map;

/**
 * Controller que registra las rutas HTTP de aerolineas aliadas para el panel de
 * administracion. Expone endpoints exclusivamente para el rol Administrador (rol 2).
 * Tambien expone el endpoint de usuarios webservice libres, usado al crear entidades.
 */
public class AerolineaAdminController {

    private final AerolineaAdminService aerolineaAdminService;

    /**
     * Crea una instancia de AerolineaAdminController con sus dependencias inyectadas.
     */
    public AerolineaAdminController(AerolineaAdminService aerolineaAdminService) {
        this.aerolineaAdminService = aerolineaAdminService;
    }

    /**
     * Registra todas las rutas de administracion de aerolineas en la aplicacion Javalin.
     * Todas las rutas requieren rol Administrador (rol 2).
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {
        app.get("/admin/aerolineas",          this::handleListar);
        app.post("/admin/aerolineas",         this::handleCrear);
        app.patch("/admin/aerolineas/{id}",   this::handleEditar);
        app.get("/admin/webservice/libres",   this::handleListarLibres);
    }

    // Retorna todas las aerolineas aliadas registradas en el sistema
    void handleListar(Context ctx) {
        if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
        ctx.json(aerolineaAdminService.listarTodas());
    }

    // Crea una nueva aerolinea aliada asignada a un usuario webservice especifico
    void handleCrear(Context ctx) {
        if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
        try {
            ctx.status(201).json(
                    aerolineaAdminService.crear(ctx.bodyAsClass(CrearAerolineaAdminRequestDTO.class))
            );
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // Edita los datos de una aerolinea especifica
    void handleEditar(Context ctx) {
        if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
        try {
            aerolineaAdminService.editar(id(ctx, "id"), ctx.bodyAsClass(EditarAerolineaRequestDTO.class));
            ctx.json(Map.of("mensaje", "Aerolinea actualizada correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // Retorna los usuarios webservice sin entidad asignada, para el selector al crear agencia o aerolinea
    void handleListarLibres(Context ctx) {
        if (!esAdmin(ctx)) { denyAdmin(ctx); return; }
        ctx.json(aerolineaAdminService.listarWebserviceLibres());
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
}
