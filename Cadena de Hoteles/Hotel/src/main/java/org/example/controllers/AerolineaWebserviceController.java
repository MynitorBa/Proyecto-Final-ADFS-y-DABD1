package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.CrearAerolineaRequestDTO;
import org.example.services.AerolineaWebserviceService;

import java.util.Map;

/**
 * Controller que registra las rutas HTTP relacionadas con aerolineas aliadas
 * para el portal webservice. Expone endpoints exclusivamente para el rol Webservice (rol 3).
 */
public class AerolineaWebserviceController {

    private final AerolineaWebserviceService aerolineaService;

    /**
     * Crea una instancia de AerolineaWebserviceController con sus dependencias inyectadas.
     */
    public AerolineaWebserviceController(AerolineaWebserviceService aerolineaService) {
        this.aerolineaService = aerolineaService;
    }

    /**
     * Registra todas las rutas de aerolineas aliadas para el webservice en la aplicacion Javalin.
     * Todas las rutas requieren rol Webservice (rol 3).
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {
        app.get("/webservice/aerolineas",              this::handleListar);
        app.post("/webservice/aerolineas",             this::handleCrear);
        app.patch("/webservice/aerolineas/{id}/estado", this::handleCambiarEstado);
    }

    // Lista las aerolineas asociadas al usuario autenticado
    void handleListar(Context ctx) {
        if (!esWebservice(ctx)) { deny(ctx); return; }
        ctx.json(aerolineaService.listarPorUsuario(usuarioId(ctx)));
    }

    // Crea una nueva aerolinea aliada para el usuario autenticado
    void handleCrear(Context ctx) {
        if (!esWebservice(ctx)) { deny(ctx); return; }
        try {
            ctx.status(201).json(
                    aerolineaService.crear(usuarioId(ctx), ctx.bodyAsClass(CrearAerolineaRequestDTO.class))
            );
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // Cambia el estado de una aerolinea especifica del usuario autenticado
    void handleCambiarEstado(Context ctx) {
        if (!esWebservice(ctx)) { deny(ctx); return; }
        try {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            int nuevoEstado = Integer.parseInt(body.get("estadoId").toString());
            aerolineaService.cambiarEstado(id(ctx, "id"), usuarioId(ctx), nuevoEstado);
            ctx.json(Map.of("mensaje", "Estado actualizado correctamente"));
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
     * Responde con 403 cuando el acceso requiere rol Webservice y el usuario no lo tiene.
     * @param ctx contexto de la peticion HTTP.
     */
    private void deny(Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Webservice"));
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
