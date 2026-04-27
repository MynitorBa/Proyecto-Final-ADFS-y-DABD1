package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.helpers.MetricasPdfHelper;
import org.example.services.MetricasHotelService;

import java.util.List;
import java.util.Map;

/**
 * Controller para el modulo de metricas del panel de administracion.
 * Todos los endpoints requieren rol administrador (rolId = 2).
 *
 * GET  /admin/metricas/resumen           - KPI, reservaciones por dia, canal split
 * GET  /admin/metricas/negocio           - embudo, top hoteles, cancelaciones, tendencia, heatmap
 * POST /admin/metricas/listado           - registro paginado de busquedas
 * POST /admin/metricas/exportar-archivo  - descarga Excel (.xlsx) o ZIP de CSVs
 * POST /admin/metricas/exportar-correo   - envia reporte por correo con adjunto
 */
public class MetricasHotelController {

    private final MetricasHotelService service;

    public MetricasHotelController(MetricasHotelService service) {
        this.service = service;
    }

    public void registerRoutes(Javalin app) {
        app.get("/admin/metricas/resumen",          this::handleResumen);
        app.get("/admin/metricas/negocio",           this::handleNegocio);
        app.post("/admin/metricas/listado",          this::handleListado);
        app.post("/admin/metricas/exportar-archivo", this::handleExportarArchivo);
        app.post("/admin/metricas/exportar-correo",  this::handleExportarCorreo);
        app.post("/admin/metricas/exportar-pdf",     this::handleExportarPdf);
    }

    // GET /admin/metricas/resumen?fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD
    void handleResumen(Context ctx) {
        if (!esAdmin(ctx)) return;
        String desde = ctx.queryParam("fechaDesde");
        String hasta = ctx.queryParam("fechaHasta");
        ctx.json(service.obtenerResumen(desde, hasta));
    }

    // GET /admin/metricas/negocio?fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD
    void handleNegocio(Context ctx) {
        if (!esAdmin(ctx)) return;
        String desde = ctx.queryParam("fechaDesde");
        String hasta = ctx.queryParam("fechaHasta");
        ctx.json(service.obtenerNegocio(desde, hasta));
    }

    // POST /admin/metricas/listado
    // Body: { fechaDesde, fechaHasta, tipo, usuario, pagina, tamanoPagina }
    void handleListado(Context ctx) {
        if (!esAdmin(ctx)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> body = ctx.bodyAsClass(Map.class);

        String desde       = str(body.get("fechaDesde"));
        String hasta       = str(body.get("fechaHasta"));
        String tipo        = str(body.get("tipo"));
        String usuario     = str(body.get("usuario"));
        int    pagina      = intVal(body.get("pagina"), 1);
        int    tamano      = intVal(body.get("tamanoPagina"), 25);

        ctx.json(service.obtenerListado(desde, hasta, tipo, usuario, pagina, tamano));
    }

    // POST /admin/metricas/exportar-archivo
    // Body: { fechaDesde, fechaHasta, tipo, usuario, secciones, formato }
    // formato: "excel" | "csv"
    void handleExportarArchivo(Context ctx) {
        if (!esAdmin(ctx)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> body = ctx.bodyAsClass(Map.class);

        String desde   = str(body.get("fechaDesde"));
        String hasta   = str(body.get("fechaHasta"));
        String tipo    = str(body.get("tipo"));
        String usuario = str(body.get("usuario"));
        String formato = str(body.get("formato"));

        @SuppressWarnings("unchecked")
        Map<String, Boolean> secciones = body.get("secciones") instanceof Map
                ? (Map<String, Boolean>) body.get("secciones")
                : Map.of();

        try {
            if ("csv".equalsIgnoreCase(formato)) {
                byte[] zip = service.exportarCsv(desde, hasta, tipo, usuario, secciones);
                ctx.contentType("application/zip");
                ctx.header("Content-Disposition",
                        "attachment; filename=\"metricas_miku_" + desde + "_" + hasta + ".zip\"");
                ctx.result(zip);
            } else {
                byte[] xlsx = service.exportarExcel(desde, hasta, tipo, usuario, secciones);
                ctx.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ctx.header("Content-Disposition",
                        "attachment; filename=\"metricas_miku_" + desde + "_" + hasta + ".xlsx\"");
                ctx.result(xlsx);
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("mensaje", "Error generando archivo: " + e.getMessage()));
        }
    }

    // POST /admin/metricas/exportar-correo
    // Body: { fechaDesde, fechaHasta, tipo, usuario, secciones, correos: [...], formato }
    void handleExportarCorreo(Context ctx) {
        if (!esAdmin(ctx)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> body = ctx.bodyAsClass(Map.class);

        String desde   = str(body.get("fechaDesde"));
        String hasta   = str(body.get("fechaHasta"));
        String tipo    = str(body.get("tipo"));
        String usuario = str(body.get("usuario"));
        String formato = str(body.get("formato"));

        @SuppressWarnings("unchecked")
        Map<String, Boolean> secciones = body.get("secciones") instanceof Map
                ? (Map<String, Boolean>) body.get("secciones")
                : Map.of();

        @SuppressWarnings("unchecked")
        List<String> correos = body.get("correos") instanceof List
                ? (List<String>) body.get("correos")
                : List.of();

        List<String> validos = correos.stream()
                .filter(c -> c != null && c.contains("@"))
                .map(String::trim)
                .filter(c -> !c.isBlank())
                .toList();

        if (validos.isEmpty()) {
            ctx.status(400).json(Map.of("mensaje", "Debes indicar al menos un correo valido"));
            return;
        }

        try {
            service.exportarCorreo(validos, desde, hasta, tipo, usuario, secciones, formato);
            ctx.json(Map.of("mensaje", "Reporte enviado a " + validos.size()
                    + " destinatario" + (validos.size() > 1 ? "s" : "")));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("mensaje", "Error enviando correo: " + e.getMessage()));
        }
    }

    // POST /admin/metricas/exportar-pdf
    // Body: { fechaDesde, fechaHasta, tipo, usuario, secciones }
    void handleExportarPdf(Context ctx) {
        if (!esAdmin(ctx)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> body = ctx.bodyAsClass(Map.class);

        String desde   = str(body.get("fechaDesde"));
        String hasta   = str(body.get("fechaHasta"));
        String tipo    = str(body.get("tipo"));
        String usuario = str(body.get("usuario"));

        @SuppressWarnings("unchecked")
        Map<String, Boolean> secciones = body.get("secciones") instanceof Map
                ? (Map<String, Boolean>) body.get("secciones")
                : Map.of();

        try {
            byte[] pdf = service.exportarPdf(desde, hasta, tipo, usuario, secciones);
            ctx.contentType("application/pdf");
            ctx.header("Content-Disposition",
                    "attachment; filename=\"metricas_miku_" + desde + "_" + hasta + ".pdf\"");
            ctx.result(pdf);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("mensaje", "Error generando PDF: " + e.getMessage()));
        }
    }

    // ─── UTILIDADES ──────────────────────────────────────────────────────────

    private boolean esAdmin(Context ctx) {
        Object rolId = ctx.attribute("rolId");
        if (!(rolId instanceof Integer) || (Integer) rolId != 2) {
            ctx.status(403).json(Map.of("mensaje", "Acceso denegado"));
            return false;
        }
        return true;
    }

    private String str(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private int intVal(Object o, int def) {
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); }
        catch (Exception e) { return def; }
    }
}
