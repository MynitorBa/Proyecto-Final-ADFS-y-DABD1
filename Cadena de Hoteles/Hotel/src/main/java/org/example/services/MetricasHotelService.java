package org.example.services;

import org.example.helpers.EmailHelper;
import org.example.helpers.MetricasExcelHelper;
import org.example.helpers.MetricasPdfHelper;
import org.example.repositories.AdminBusquedaRepository;
import org.example.repositories.MetricasHotelRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service para el modulo de metricas del panel de administracion.
 * Orquesta MetricasHotelRepository (KPIs, graficas) y AdminBusquedaRepository
 * (listado de busquedas paginado y exportacion completa).
 */
public class MetricasHotelService {

    private final MetricasHotelRepository metRepo;
    private final AdminBusquedaRepository busRepo;

    public MetricasHotelService(MetricasHotelRepository metRepo,
                                AdminBusquedaRepository busRepo) {
        this.metRepo = metRepo;
        this.busRepo = busRepo;
    }

    // ─── PANEL 1: RESUMEN ────────────────────────────────────────────────────

    /**
     * Retorna el resumen para el panel 1: KPI de ingresos, reservaciones por dia
     * y division de canal Directo vs Agencia.
     */
    public Map<String, Object> obtenerResumen(String fechaDesde, String fechaHasta) {
        String desde = normDate(fechaDesde, "-30d");
        String hasta = normDate(fechaHasta, "hoy");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ingresosKpi",         metRepo.obtenerIngresosKpi(desde, hasta));
        result.put("reservacionesPorDia", metRepo.reservacionesPorDia(desde, hasta));
        result.put("canalSplit",          metRepo.canalSplit(desde, hasta));
        result.put("busquedasPorCanal",   metRepo.busquedasPorCanal(desde, hasta));
        return result;
    }

    // ─── PANEL 2: NEGOCIO ────────────────────────────────────────────────────

    /**
     * Retorna los datos del panel 2: embudo de conversion, top hoteles,
     * cancelaciones por tipo, tendencia de ingresos y heatmap.
     */
    public Map<String, Object> obtenerNegocio(String fechaDesde, String fechaHasta) {
        String desde = normDate(fechaDesde, "-30d");
        String hasta = normDate(fechaHasta, "hoy");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("embudo",            metRepo.embudo(desde, hasta));
        result.put("topHoteles",        metRepo.topHoteles(desde, hasta));
        result.put("cancelaciones",     metRepo.cancelacionesPorTipo(desde, hasta));
        result.put("ingresosTendencia", metRepo.ingresosTendencia(desde, hasta));
        result.put("heatmap",           metRepo.heatmap(desde, hasta));
        return result;
    }

    // ─── PANEL 3: LISTADO PAGINADO ───────────────────────────────────────────

    /**
     * Retorna una pagina de busquedas con los filtros aplicados.
     * tipo: "web" | "rest" | null/vacio = todos.
     */
    public Map<String, Object> obtenerListado(
            String fechaDesde, String fechaHasta,
            String tipo, String usuario,
            int pagina, int tamanoPagina) {

        String desde = normDate(fechaDesde, "-30d");
        String hasta = normDate(fechaHasta, "hoy");

        Integer tipoBusquedaId = parseTipo(tipo);
        Date    sqlDesde       = toSqlDate(desde);
        Date    sqlHasta       = toSqlDate(hasta);

        if (pagina < 1)        pagina      = 1;
        if (tamanoPagina < 1)  tamanoPagina = 25;
        if (tamanoPagina > 100) tamanoPagina = 100;

        int offset = (pagina - 1) * tamanoPagina;

        List<Map<String, Object>> registros = busRepo.listar(
                null, usuario, tipoBusquedaId,
                sqlDesde, sqlHasta, offset, tamanoPagina);

        int totalRegistros = busRepo.contar(
                null, usuario, tipoBusquedaId, sqlDesde, sqlHasta);

        int totalPaginas = (int) Math.ceil((double) totalRegistros / tamanoPagina);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("registros",       registros);
        result.put("totalRegistros",  totalRegistros);
        result.put("paginaActual",    pagina);
        result.put("totalPaginas",    Math.max(totalPaginas, 1));
        return result;
    }

    // ─── EXPORTAR ARCHIVO ────────────────────────────────────────────────────

    /**
     * Genera un archivo Excel (.xlsx) con las secciones indicadas.
     */
    public byte[] exportarExcel(String fechaDesde, String fechaHasta,
                                String tipo, String usuario,
                                Map<String, Boolean> secciones) {

        String desde = normDate(fechaDesde, "-30d");
        String hasta = normDate(fechaHasta, "hoy");
        Map<String, Object> data = recopilarDatos(desde, hasta, tipo, usuario, secciones);
        return MetricasExcelHelper.generarExcel(data, secciones, desde, hasta);
    }

    /**
     * Genera un ZIP con un CSV por cada seccion incluida.
     */
    public byte[] exportarCsv(String fechaDesde, String fechaHasta,
                              String tipo, String usuario,
                              Map<String, Boolean> secciones) {

        String desde = normDate(fechaDesde, "-30d");
        String hasta = normDate(fechaHasta, "hoy");
        Map<String, Object> data = recopilarDatos(desde, hasta, tipo, usuario, secciones);
        return MetricasExcelHelper.generarCsvZip(data, secciones, desde, hasta);
    }

    /**
     * Genera un archivo PDF con las secciones indicadas.
     */
    public byte[] exportarPdf(String fechaDesde, String fechaHasta,
                               String tipo, String usuario,
                               Map<String, Boolean> secciones) {
        String desde = normDate(fechaDesde, "-30d");
        String hasta = normDate(fechaHasta, "hoy");
        Map<String, Object> data = recopilarDatos(desde, hasta, tipo, usuario, secciones);
        return MetricasPdfHelper.generarPdf(data, secciones, desde, hasta);
    }

    // ─── EXPORTAR POR CORREO ─────────────────────────────────────────────────

    /**
     * Envia el reporte por correo a los destinatarios indicados.
     * formato: "excel" | "csv".
     */
    public void exportarCorreo(List<String> correos, String fechaDesde, String fechaHasta,
                               String tipo, String usuario,
                               Map<String, Boolean> secciones, String formato) {

        String desde = normDate(fechaDesde, "-30d");
        String hasta = normDate(fechaHasta, "hoy");
        Map<String, Object> data = recopilarDatos(desde, hasta, tipo, usuario, secciones);

        byte[] adjunto;
        String nombreArchivo;
        String contentType;

        if ("csv".equalsIgnoreCase(formato)) {
            adjunto      = MetricasExcelHelper.generarCsvZip(data, secciones, desde, hasta);
            nombreArchivo = String.format("metricas_miku_%s_%s.zip", desde, hasta);
            contentType  = "application/zip";
        } else {
            adjunto      = MetricasExcelHelper.generarExcel(data, secciones, desde, hasta);
            nombreArchivo = String.format("metricas_miku_%s_%s.xlsx", desde, hasta);
            contentType  = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }

        String asunto  = "Métricas Miku Inn — " + desde + " al " + hasta;
        String cuerpo  = generarHtmlCorreo(data, desde, hasta, secciones);

        for (String correo : correos) {
            EmailHelper.enviarConAdjunto(correo, asunto, cuerpo,
                    adjunto, nombreArchivo, contentType);
        }
    }

    // ─── PRIVADOS ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> recopilarDatos(String desde, String hasta,
                                               String tipo, String usuario,
                                               Map<String, Boolean> secciones) {
        Map<String, Object> data = new LinkedHashMap<>();

        boolean incKpi    = isTrue(secciones, "kpi");
        boolean incResDia = isTrue(secciones, "reservacionesDiarias");
        boolean incCanal  = isTrue(secciones, "canal");
        boolean incEmb    = isTrue(secciones, "embudo");
        boolean incHot    = isTrue(secciones, "hoteles");
        boolean incCanc   = isTrue(secciones, "cancelaciones");
        boolean incTend   = isTrue(secciones, "tendencia");
        boolean incHeat   = isTrue(secciones, "heatmap");
        boolean incReg    = isTrue(secciones, "registro");

        if (incKpi)    data.put("ingresosKpi",         metRepo.obtenerIngresosKpi(desde, hasta));
        if (incResDia) data.put("reservacionesPorDia", metRepo.reservacionesPorDia(desde, hasta));
        if (incCanal)  data.put("canalSplit",          metRepo.canalSplit(desde, hasta));
        if (incEmb)    data.put("embudo",              metRepo.embudo(desde, hasta));
        if (incHot)    data.put("topHoteles",          metRepo.topHoteles(desde, hasta));
        if (incCanc)   data.put("cancelaciones",       metRepo.cancelacionesPorTipo(desde, hasta));
        if (incTend)   data.put("ingresosTendencia",   metRepo.ingresosTendencia(desde, hasta));
        if (incHeat)   data.put("heatmap",             metRepo.heatmap(desde, hasta));

        if (incReg) {
            Integer tipoBusquedaId = parseTipo(tipo);
            data.put("registro", busRepo.exportar(
                    null, usuario, tipoBusquedaId,
                    toSqlDate(desde), toSqlDate(hasta)));
        }

        return data;
    }

    private boolean isTrue(Map<String, Boolean> map, String key) {
        return map != null && Boolean.TRUE.equals(map.get(key));
    }

    private Integer parseTipo(String tipo) {
        if (tipo == null || tipo.isBlank() || tipo.equalsIgnoreCase("todos")) return null;
        if (tipo.equalsIgnoreCase("web"))  return 1;
        if (tipo.equalsIgnoreCase("rest")) return 2;
        return null;
    }

    private Date toSqlDate(String str) {
        if (str == null || str.isBlank()) return null;
        try { return Date.valueOf(LocalDate.parse(str.trim())); }
        catch (Exception e) { return null; }
    }

    /**
     * Normaliza fechas: si son nulas o vacias usa un valor por defecto.
     * defecto "-30d" = 30 dias atras, "hoy" = hoy.
     */
    private String normDate(String str, String defecto) {
        if (str != null && !str.isBlank()) {
            try { LocalDate.parse(str.trim()); return str.trim(); }
            catch (Exception ignored) {}
        }
        if ("-30d".equals(defecto)) {
            return LocalDate.now().minusDays(29).toString();
        }
        return LocalDate.now().toString();
    }

    @SuppressWarnings("unchecked")
    private String generarHtmlCorreo(Map<String, Object> data,
                                     String desde, String hasta,
                                     Map<String, Boolean> secciones) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
          .append("<style>")
          .append("body{font-family:Arial,sans-serif;font-size:13px;color:#222;max-width:700px;margin:0 auto}")
          .append("h2{color:#1a3a5c;border-bottom:2px solid #D4AF37;padding-bottom:6px}")
          .append("h3{color:#1a3a5c;margin:20px 0 6px}")
          .append(".kpi-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin:12px 0}")
          .append(".kpi-card{background:#f8f9fa;border-radius:6px;padding:12px;border-left:3px solid #D4AF37}")
          .append(".kpi-val{font-size:20px;font-weight:700;color:#1a3a5c}")
          .append(".kpi-lbl{font-size:11px;color:#666;margin-top:3px}")
          .append("table{border-collapse:collapse;width:100%;margin:8px 0}")
          .append("th{background:#1a3a5c;color:#fff;padding:7px 9px;text-align:left;font-size:12px}")
          .append("td{padding:6px 9px;border-bottom:1px solid #e5e7eb;font-size:12px}")
          .append("tr:nth-child(even) td{background:#f9fafb}")
          .append(".footer{margin-top:24px;padding-top:10px;border-top:1px solid #e5e7eb;font-size:11px;color:#888}")
          .append("</style></head><body>")
          .append("<h2>Métricas Miku Inn</h2>")
          .append("<p style='color:#666;font-size:12px'>Período: <strong>").append(desde)
          .append("</strong> al <strong>").append(hasta).append("</strong></p>");

        // KPI
        if (isTrue(secciones, "kpi") && data.containsKey("ingresosKpi")) {
            Map<String, Object> kpi = (Map<String, Object>) data.get("ingresosKpi");
            double total   = num(kpi.get("ingresosTotales"));
            double directo = num(kpi.get("ingresosDirecto"));
            double agencia = num(kpi.get("ingresosAgencia"));
            double ticket  = num(kpi.get("ticketPromedio"));
            int    pagadas = ((Number) kpi.getOrDefault("reservacionesPagadas", 0)).intValue();
            int    totalR  = ((Number) kpi.getOrDefault("totalReservaciones", 0)).intValue();
            sb.append("<h3>KPI de Ingresos</h3>")
              .append("<div class='kpi-grid'>")
              .append(kpiCard("Ingresos Totales", String.format("Q %.2f", total)))
              .append(kpiCard("Directo", String.format("Q %.2f", directo)))
              .append(kpiCard("Agencia", String.format("Q %.2f", agencia)))
              .append(kpiCard("Ticket Promedio", String.format("Q %.2f", ticket)))
              .append(kpiCard("Res. Pagadas", String.valueOf(pagadas)))
              .append(kpiCard("Total Res.", String.valueOf(totalR)))
              .append("</div>");
        }

        // Top Hoteles
        if (isTrue(secciones, "hoteles") && data.containsKey("topHoteles")) {
            List<Map<String, Object>> hoteles = (List<Map<String, Object>>) data.get("topHoteles");
            sb.append("<h3>Top Hoteles por Ingresos</h3>")
              .append("<table><thead><tr><th>#</th><th>Hotel</th><th>Reservaciones</th><th>Ingresos</th></tr></thead><tbody>");
            int i = 1;
            for (Map<String, Object> h : hoteles) {
                sb.append("<tr><td>").append(i++).append("</td>")
                  .append("<td>").append(safe(h.get("hotel"))).append("</td>")
                  .append("<td>").append(h.get("totalReservaciones")).append("</td>")
                  .append("<td>Q ").append(String.format("%.2f", num(h.get("ingresosTotales")))).append("</td></tr>");
            }
            sb.append("</tbody></table>");
        }

        // Cancelaciones
        if (isTrue(secciones, "cancelaciones") && data.containsKey("cancelaciones")) {
            List<Map<String, Object>> canc = (List<Map<String, Object>>) data.get("cancelaciones");
            sb.append("<h3>Cancelaciones por Tipo</h3>")
              .append("<table><thead><tr><th>Tipo</th><th>Total</th></tr></thead><tbody>");
            for (Map<String, Object> c : canc) {
                sb.append("<tr><td>").append(safe(c.get("tipo"))).append("</td>")
                  .append("<td>").append(c.get("total")).append("</td></tr>");
            }
            sb.append("</tbody></table>");
        }

        sb.append("<div class='footer'>Generado por el Panel de Administración de Miku Inn · ")
          .append(new java.util.Date())
          .append("</div></body></html>");

        return sb.toString();
    }

    private String kpiCard(String label, String val) {
        return "<div class='kpi-card'><div class='kpi-val'>" + val
             + "</div><div class='kpi-lbl'>" + label + "</div></div>";
    }

    private double num(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0.0;
    }

    private String safe(Object o) {
        return o != null ? String.valueOf(o) : "-";
    }
}
