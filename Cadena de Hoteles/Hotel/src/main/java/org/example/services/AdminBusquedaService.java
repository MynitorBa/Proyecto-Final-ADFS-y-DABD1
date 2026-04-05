package org.example.services;

import org.example.helpers.EmailHelper;
import org.example.repositories.AdminBusquedaRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service para el modulo de busquedas del panel de administracion.
 * Maneja listado paginado, resumen para dashboard y exportacion por correo.
 */
public class AdminBusquedaService {

    private final AdminBusquedaRepository repo = new AdminBusquedaRepository();

    /**
     * Retorna una pagina de busquedas aplicando los filtros dados.
     * @param destino          nombre del destino a filtrar, o null para ignorar.
     * @param usuarioAgencia   nombre de usuario o agencia a filtrar, o null para ignorar.
     * @param tipo             "web", "rest" o "todos".
     * @param fechaDesdeStr    fecha de inicio en formato ISO (yyyy-MM-dd), o null.
     * @param fechaHastaStr    fecha de fin en formato ISO (yyyy-MM-dd), o null.
     * @param pagina           numero de pagina (base 1).
     * @param porPagina        cantidad de registros por pagina.
     * @return mapa con "busquedas" (lista) y "total" (int).
     */
    public Map<String, Object> listar(
            String destino, String usuarioAgencia, String tipo,
            String fechaDesdeStr, String fechaHastaStr,
            int pagina, int porPagina) {

        Integer tipoBusquedaId = parseTipo(tipo);
        Date fechaDesde = parseDate(fechaDesdeStr);
        Date fechaHasta = parseDate(fechaHastaStr);

        int offset = (pagina - 1) * porPagina;

        List<Map<String, Object>> busquedas = repo.listar(
                destino, usuarioAgencia, tipoBusquedaId,
                fechaDesde, fechaHasta, offset, porPagina);

        int total = repo.contar(destino, usuarioAgencia, tipoBusquedaId, fechaDesde, fechaHasta);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("busquedas", busquedas);
        result.put("total",     total);
        return result;
    }

    /**
     * Retorna un resumen de busquedas para el dashboard de admin.
     * Incluye totales por tipo, busquedas por dia y top destinos.
     * @return mapa con totalWeb, totalRest, porDia y topDestinos.
     */
    public Map<String, Object> resumen() {
        int totalWeb  = repo.contarPorTipo(1); // TipoBusquedaID=1 -> Usuario/Web
        int totalRest = repo.contarPorTipo(2); // TipoBusquedaID=2 -> Agencia/REST

        List<Map<String, Object>> porDia      = repo.busquedasPorDia();
        List<Map<String, Object>> topDestinos = repo.topDestinos();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalWeb",    totalWeb);
        result.put("totalRest",   totalRest);
        result.put("porDia",      porDia);
        result.put("topDestinos", topDestinos);
        return result;
    }

    /**
     * Genera un reporte HTML con las busquedas filtradas y lo envia por correo.
     * @param email            destinatario del reporte.
     * @param destino          filtro de destino, o null.
     * @param usuarioAgencia   filtro de usuario o agencia, o null.
     * @param tipo             "web", "rest" o "todos".
     * @param fechaDesdeStr    fecha de inicio, o null.
     * @param fechaHastaStr    fecha de fin, o null.
     */
    public void exportar(String email, String destino, String usuarioAgencia,
                         String tipo, String fechaDesdeStr, String fechaHastaStr) {

        Integer tipoBusquedaId = parseTipo(tipo);
        Date fechaDesde = parseDate(fechaDesdeStr);
        Date fechaHasta = parseDate(fechaHastaStr);

        List<Map<String, Object>> datos = repo.exportar(
                destino, usuarioAgencia, tipoBusquedaId, fechaDesde, fechaHasta);

        String html = generarHtmlExport(datos, destino, usuarioAgencia, tipo, fechaDesdeStr, fechaHastaStr);

        EmailHelper.enviar(email,
                "Reporte de Busquedas - Miku Inn",
                html);
    }

    /**
     * Convierte el string de tipo a su ID numerico en base de datos.
     * "web" -> 1, "rest" -> 2, cualquier otro valor -> null (sin filtro).
     */
    private Integer parseTipo(String tipo) {
        if (tipo == null || tipo.equalsIgnoreCase("todos") || tipo.isBlank()) return null;
        if (tipo.equalsIgnoreCase("web"))  return 1;
        if (tipo.equalsIgnoreCase("rest")) return 2;
        return null;
    }

    /**
     * Parsea un string ISO a java.sql.Date.
     * Retorna null si el string es nulo, vacio o tiene formato invalido.
     */
    private Date parseDate(String str) {
        if (str == null || str.isBlank()) return null;
        try { return Date.valueOf(LocalDate.parse(str.trim())); }
        catch (Exception e) { return null; }
    }

    /**
     * Construye el HTML del reporte de busquedas para enviar por correo.
     * Incluye estilos inline, tabla de resultados y seccion de filtros aplicados.
     */
    private String generarHtmlExport(List<Map<String, Object>> datos,
                                     String destino, String usuarioAgencia,
                                     String tipo, String desde, String hasta) {

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;font-size:13px;color:#222}")
                .append("h2{color:#1a56db}")
                .append(".filtros{background:#f5f7fa;padding:8px 12px;border-radius:4px;margin-bottom:16px;font-size:12px;color:#555}")
                .append("table{border-collapse:collapse;width:100%}")
                .append("th{background:#1a56db;color:#fff;padding:8px 10px;text-align:left;font-weight:600}")
                .append("td{padding:7px 10px;border-bottom:1px solid #e5e7eb}")
                .append("tr:nth-child(even) td{background:#f9fafb}")
                .append(".badge-web{background:#d1fae5;color:#065f46;padding:2px 7px;border-radius:999px;font-size:11px}")
                .append(".badge-rest{background:#dbeafe;color:#1e40af;padding:2px 7px;border-radius:999px;font-size:11px}")
                .append(".footer{margin-top:20px;font-size:11px;color:#888}")
                .append("</style></head><body>")
                .append("<h2>Reporte de Busquedas - Miku Inn</h2>");

        // Muestra los filtros activos en el encabezado del reporte
        boolean hayFiltro = (destino != null && !destino.isBlank())
                || (usuarioAgencia != null && !usuarioAgencia.isBlank())
                || (tipo != null && !tipo.equalsIgnoreCase("todos") && !tipo.isBlank())
                || (desde != null && !desde.isBlank())
                || (hasta  != null && !hasta.isBlank());

        if (hayFiltro) {
            sb.append("<div class='filtros'><strong>Filtros aplicados:</strong> ");
            if (destino        != null && !destino.isBlank())                              sb.append("Destino: <em>").append(destino).append("</em>  ");
            if (usuarioAgencia != null && !usuarioAgencia.isBlank())                       sb.append("Usuario/Agencia: <em>").append(usuarioAgencia).append("</em>  ");
            if (tipo           != null && !tipo.equalsIgnoreCase("todos") && !tipo.isBlank()) sb.append("Tipo: <em>").append(tipo).append("</em>  ");
            if (desde          != null && !desde.isBlank())                                sb.append("Desde: <em>").append(desde).append("</em>  ");
            if (hasta          != null && !hasta.isBlank())                                sb.append("Hasta: <em>").append(hasta).append("</em>");
            sb.append("</div>");
        }

        sb.append("<p><strong>Total de registros:</strong> ").append(datos.size()).append("</p>");

        if (datos.isEmpty()) {
            sb.append("<p style='color:#888'>No se encontraron busquedas con los filtros aplicados.</p>");
        } else {
            sb.append("<table>")
                    .append("<thead><tr>")
                    .append("<th>ID</th><th>Destino</th><th>Check-in</th><th>Check-out</th>")
                    .append("<th>Personas</th><th>Usuario / Agencia</th><th>Tipo</th>")
                    .append("</tr></thead><tbody>");

            // Fila por cada busqueda registrada
            for (Map<String, Object> row : datos) {
                String tipoVal = String.valueOf(row.getOrDefault("tipo", "Web"));
                boolean esRest = tipoVal.equalsIgnoreCase("REST");
                String badgeClass = esRest ? "badge-rest" : "badge-web";

                sb.append("<tr>")
                        .append("<td>").append(row.get("id")).append("</td>")
                        .append("<td>").append(safe(row.get("destino"))).append("</td>")
                        .append("<td>").append(safe(row.get("checkIn"))).append("</td>")
                        .append("<td>").append(safe(row.get("checkOut"))).append("</td>")
                        .append("<td style='text-align:center'>").append(row.get("personas")).append("</td>")
                        .append("<td>").append(safe(row.get("usuario"))).append("</td>")
                        .append("<td><span class='").append(badgeClass).append("'>").append(tipoVal).append("</span></td>")
                        .append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        sb.append("<div class='footer'>")
                .append("Generado por el Panel de Administracion de Miku Inn · ")
                .append(new java.util.Date())
                .append("</div>")
                .append("</body></html>");

        return sb.toString();
    }

    /**
     * Retorna el valor como String o un guion si es null.
     */
    private String safe(Object val) {
        return val != null ? String.valueOf(val) : "-";
    }
}