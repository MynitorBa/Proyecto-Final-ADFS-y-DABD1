package org.example.controllers;

import io.javalin.Javalin;
import org.example.services.AdminBusquedaService;

import java.util.Map;

/**
 * Controller para el modulo de reportes de busquedas del panel de administracion.
 * Expone endpoints para listar busquedas con filtros, obtener un resumen estadistico
 * y exportar el reporte por correo. Solo accesible para usuarios con rol de administrador.
 */
public class AdminBusquedaController {

    private final AdminBusquedaService service;

    /**
     * Crea una instancia de AdminBusquedaController con sus dependencias inyectadas.
     */
    public AdminBusquedaController(AdminBusquedaService service) {
        this.service = service;
    }

    /**
     * Registra las rutas del modulo de reportes de busquedas en la aplicacion Javalin.
     *
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // GET /admin/reportes/busquedas
        // Devuelve { busquedas: [...], total: N }
        // Query params opcionales: destino, usuarioAgencia, tipo (web|rest|todos),
        // fechaDesde (YYYY-MM-DD), fechaHasta (YYYY-MM-DD), pagina (default 1), porPagina (default 25)
        app.get("/admin/reportes/busquedas", ctx -> {

            // Solo admins (rolId=2) pueden acceder
            int rolId = ctx.attribute("rolId");
            if (rolId != 2) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado"));
                return;
            }

            String destino        = ctx.queryParam("destino");
            String usuarioAgencia = ctx.queryParam("usuarioAgencia");
            String tipo           = ctx.queryParamAsClass("tipo", String.class).getOrDefault("todos");
            String fechaDesde     = ctx.queryParam("fechaDesde");
            String fechaHasta     = ctx.queryParam("fechaHasta");

            int pagina    = ctx.queryParamAsClass("pagina",    Integer.class).getOrDefault(1);
            int porPagina = ctx.queryParamAsClass("porPagina", Integer.class).getOrDefault(25);

            // Sanitizar limites de paginacion
            if (pagina < 1)      pagina    = 1;
            if (porPagina < 1)   porPagina = 25;
            if (porPagina > 100) porPagina = 100;

            ctx.json(service.listar(destino, usuarioAgencia, tipo, fechaDesde, fechaHasta, pagina, porPagina));
        });

        // GET /admin/reportes/busquedas/resumen
        // Devuelve { totalWeb, totalRest, porDia: [...], topDestinos: [...] }
        app.get("/admin/reportes/busquedas/resumen", ctx -> {

            int rolId = ctx.attribute("rolId");
            if (rolId != 2) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado"));
                return;
            }

            ctx.json(service.resumen());
        });

        // POST /admin/reportes/busquedas/exportar
        // Body: { email: "...", filtros: { destino, usuarioAgencia, tipo, fechaDesde, fechaHasta } }
        // Envia el reporte HTML por correo al email indicado
        app.post("/admin/reportes/busquedas/exportar", ctx -> {

            int rolId = ctx.attribute("rolId");
            if (rolId != 2) {
                ctx.status(403).json(Map.of("mensaje", "Acceso denegado"));
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String email = body.get("email") != null ? body.get("email").toString().trim() : "";
            if (email.isBlank() || !email.contains("@")) {
                ctx.status(400).json(Map.of("mensaje", "Correo electronico invalido"));
                return;
            }

            // Extraer filtros del body, usando un mapa vacio si no vienen
            @SuppressWarnings("unchecked")
            Map<String, Object> filtros = body.get("filtros") instanceof Map
                    ? (Map<String, Object>) body.get("filtros")
                    : Map.of();

            String destino        = filtros.get("destino")        != null ? filtros.get("destino").toString()        : null;
            String usuarioAgencia = filtros.get("usuarioAgencia") != null ? filtros.get("usuarioAgencia").toString() : null;
            String tipo           = filtros.get("tipo")           != null ? filtros.get("tipo").toString()           : "todos";
            String fechaDesde     = filtros.get("fechaDesde")     != null ? filtros.get("fechaDesde").toString()     : null;
            String fechaHasta     = filtros.get("fechaHasta")     != null ? filtros.get("fechaHasta").toString()     : null;

            service.exportar(email, destino, usuarioAgencia, tipo, fechaDesde, fechaHasta);

            ctx.json(Map.of("mensaje", "Reporte enviado correctamente a " + email));
        });
    }
}