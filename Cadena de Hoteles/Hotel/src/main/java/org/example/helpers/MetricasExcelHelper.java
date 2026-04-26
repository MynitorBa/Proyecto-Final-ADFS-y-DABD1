package org.example.helpers;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Helper para generar exportaciones de metricas en formato Excel (.xlsx)
 * y ZIP de CSVs usando Apache POI.
 */
public class MetricasExcelHelper {

    // ─── EXCEL ───────────────────────────────────────────────────────────────

    /**
     * Genera un archivo Excel con las secciones de metricas incluidas.
     *
     * @param data      mapa con los datos de cada seccion.
     * @param secciones mapa de boolean indicando que secciones incluir.
     * @param desde     fecha inicio del reporte (YYYY-MM-DD).
     * @param hasta     fecha fin del reporte (YYYY-MM-DD).
     * @return bytes del archivo .xlsx.
     */
    @SuppressWarnings("unchecked")
    public static byte[] generarExcel(Map<String, Object> data,
                                      Map<String, Boolean> secciones,
                                      String desde, String hasta) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            CellStyle headerStyle = crearEstiloEncabezado(wb);
            CellStyle numStyle    = crearEstiloNumero(wb);

            // Hoja 1: Portada / Resumen
            Sheet portada = wb.createSheet("Resumen");
            Row r0 = portada.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("Métricas Miku Inn — " + desde + " al " + hasta);
            c0.setCellStyle(crearEstiloTitulo(wb));
            portada.setColumnWidth(0, 60 * 256);

            // Hoja: KPI
            if (isTrue(secciones, "kpi") && data.containsKey("ingresosKpi")) {
                Map<String, Object> kpi = (Map<String, Object>) data.get("ingresosKpi");
                Sheet sh = wb.createSheet("KPI Ingresos");
                escribirFila(sh, 0, headerStyle, "Métrica", "Valor");
                int fila = 1;
                fila = escribirKpiRow(sh, fila, numStyle, "Ingresos Totales (Q)", num(kpi.get("ingresosTotales")));
                fila = escribirKpiRow(sh, fila, numStyle, "Ingresos Directo (Q)", num(kpi.get("ingresosDirecto")));
                fila = escribirKpiRow(sh, fila, numStyle, "Ingresos Agencia (Q)", num(kpi.get("ingresosAgencia")));
                fila = escribirKpiRow(sh, fila, numStyle, "Ticket Promedio (Q)",  num(kpi.get("ticketPromedio")));
                escribirKpiRow(sh, fila, numStyle, "Total Reservaciones",  num(kpi.get("totalReservaciones")));
                autosize(sh, 2);
            }

            // Hoja: Reservaciones por Día
            if (isTrue(secciones, "reservacionesDiarias") && data.containsKey("reservacionesPorDia")) {
                List<Map<String, Object>> dias = (List<Map<String, Object>>) data.get("reservacionesPorDia");
                Sheet sh = wb.createSheet("Reservaciones por Día");
                escribirFila(sh, 0, headerStyle, "Fecha", "Total");
                int fila = 1;
                for (Map<String, Object> d : dias) {
                    Row row = sh.createRow(fila++);
                    row.createCell(0).setCellValue(str(d.get("fecha")));
                    row.createCell(1).setCellValue(((Number) d.get("total")).intValue());
                }
                autosize(sh, 2);
            }

            // Hoja: Canal
            if (isTrue(secciones, "canal") && data.containsKey("canalSplit")) {
                List<Map<String, Object>> canales = (List<Map<String, Object>>) data.get("canalSplit");
                Sheet sh = wb.createSheet("Canal");
                escribirFila(sh, 0, headerStyle, "Canal", "Total");
                int fila = 1;
                for (Map<String, Object> c : canales) {
                    Row row = sh.createRow(fila++);
                    row.createCell(0).setCellValue(str(c.get("canal")));
                    row.createCell(1).setCellValue(((Number) c.get("total")).intValue());
                }
                autosize(sh, 2);
            }

            // Hoja: Embudo
            if (isTrue(secciones, "embudo") && data.containsKey("embudo")) {
                Map<String, Object> emb = (Map<String, Object>) data.get("embudo");
                Sheet sh = wb.createSheet("Embudo Conversión");
                escribirFila(sh, 0, headerStyle, "Estado", "Total");
                int fila = 1;
                for (Map.Entry<String, Object> e : emb.entrySet()) {
                    Row row = sh.createRow(fila++);
                    row.createCell(0).setCellValue(e.getKey());
                    row.createCell(1).setCellValue(((Number) e.getValue()).intValue());
                }
                autosize(sh, 2);
            }

            // Hoja: Top Hoteles
            if (isTrue(secciones, "hoteles") && data.containsKey("topHoteles")) {
                List<Map<String, Object>> hoteles = (List<Map<String, Object>>) data.get("topHoteles");
                Sheet sh = wb.createSheet("Top Hoteles");
                escribirFila(sh, 0, headerStyle, "#", "Hotel", "Reservaciones", "Ingresos (Q)");
                int fila = 1;
                int rank = 1;
                for (Map<String, Object> h : hoteles) {
                    Row row = sh.createRow(fila++);
                    row.createCell(0).setCellValue(rank++);
                    row.createCell(1).setCellValue(str(h.get("hotel")));
                    row.createCell(2).setCellValue(((Number) h.get("totalReservaciones")).intValue());
                    Cell ingrCell = row.createCell(3);
                    ingrCell.setCellValue(num(h.get("ingresosTotales")));
                    ingrCell.setCellStyle(numStyle);
                }
                autosize(sh, 4);
            }

            // Hoja: Cancelaciones
            if (isTrue(secciones, "cancelaciones") && data.containsKey("cancelaciones")) {
                List<Map<String, Object>> canc = (List<Map<String, Object>>) data.get("cancelaciones");
                Sheet sh = wb.createSheet("Cancelaciones");
                escribirFila(sh, 0, headerStyle, "Tipo", "Total");
                int fila = 1;
                for (Map<String, Object> c : canc) {
                    Row row = sh.createRow(fila++);
                    row.createCell(0).setCellValue(str(c.get("tipo")));
                    row.createCell(1).setCellValue(((Number) c.get("total")).intValue());
                }
                autosize(sh, 2);
            }

            // Hoja: Tendencia Ingresos
            if (isTrue(secciones, "tendencia") && data.containsKey("ingresosTendencia")) {
                List<Map<String, Object>> tend = (List<Map<String, Object>>) data.get("ingresosTendencia");
                Sheet sh = wb.createSheet("Tendencia Ingresos");
                escribirFila(sh, 0, headerStyle, "Mes", "Canal", "Revenue (Q)");
                int fila = 1;
                for (Map<String, Object> t : tend) {
                    Row row = sh.createRow(fila++);
                    row.createCell(0).setCellValue(str(t.get("mes")));
                    row.createCell(1).setCellValue(str(t.get("canal")));
                    Cell rev = row.createCell(2);
                    rev.setCellValue(num(t.get("revenue")));
                    rev.setCellStyle(numStyle);
                }
                autosize(sh, 3);
            }

            // Hoja: Heatmap
            if (isTrue(secciones, "heatmap") && data.containsKey("heatmap")) {
                List<Map<String, Object>> heat = (List<Map<String, Object>>) data.get("heatmap");
                Sheet sh = wb.createSheet("Heatmap Búsquedas");
                escribirFila(sh, 0, headerStyle, "Día Semana", "Hora", "Búsquedas");
                int fila = 1;
                for (Map<String, Object> h : heat) {
                    Row row = sh.createRow(fila++);
                    row.createCell(0).setCellValue(((Number) h.get("diaSemana")).intValue());
                    row.createCell(1).setCellValue(((Number) h.get("hora")).intValue());
                    row.createCell(2).setCellValue(((Number) h.get("total")).intValue());
                }
                autosize(sh, 3);
            }

            // Hoja: Registro Búsquedas
            if (isTrue(secciones, "registro") && data.containsKey("registro")) {
                List<Map<String, Object>> reg = (List<Map<String, Object>>) data.get("registro");
                Sheet sh = wb.createSheet("Registro Búsquedas");
                escribirFila(sh, 0, headerStyle,
                        "ID", "Destino", "Check-in", "Check-out",
                        "Personas", "Usuario/Agencia", "Tipo", "Fecha");
                int fila = 1;
                for (Map<String, Object> b : reg) {
                    Row row = sh.createRow(fila++);
                    row.createCell(0).setCellValue(((Number) b.get("id")).intValue());
                    row.createCell(1).setCellValue(str(b.get("destino")));
                    row.createCell(2).setCellValue(str(b.get("checkIn")));
                    row.createCell(3).setCellValue(str(b.get("checkOut")));
                    row.createCell(4).setCellValue(((Number) b.getOrDefault("personas", 0)).intValue());
                    Object usr = b.get("usuario");
                    row.createCell(5).setCellValue(usr != null ? str(usr) : str(b.get("agencia")));
                    row.createCell(6).setCellValue(str(b.get("tipo")));
                    row.createCell(7).setCellValue(str(b.get("fechaHora")));
                }
                autosize(sh, 8);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel: " + e.getMessage(), e);
        }
    }

    // ─── CSV ZIP ─────────────────────────────────────────────────────────────

    /**
     * Genera un ZIP que contiene un CSV por cada seccion incluida.
     */
    @SuppressWarnings("unchecked")
    public static byte[] generarCsvZip(Map<String, Object> data,
                                       Map<String, Boolean> secciones,
                                       String desde, String hasta) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            if (isTrue(secciones, "kpi") && data.containsKey("ingresosKpi")) {
                Map<String, Object> kpi = (Map<String, Object>) data.get("ingresosKpi");
                StringBuilder csv = new StringBuilder("Metrica,Valor\n");
                csv.append("Ingresos Totales,").append(num(kpi.get("ingresosTotales"))).append("\n");
                csv.append("Ingresos Directo,").append(num(kpi.get("ingresosDirecto"))).append("\n");
                csv.append("Ingresos Agencia,").append(num(kpi.get("ingresosAgencia"))).append("\n");
                csv.append("Ticket Promedio,").append(num(kpi.get("ticketPromedio"))).append("\n");
                csv.append("Total Reservaciones,").append(num(kpi.get("totalReservaciones"))).append("\n");
                agregarCsv(zos, "kpi_ingresos.csv", csv.toString());
            }

            if (isTrue(secciones, "reservacionesDiarias") && data.containsKey("reservacionesPorDia")) {
                List<Map<String, Object>> dias = (List<Map<String, Object>>) data.get("reservacionesPorDia");
                StringBuilder csv = new StringBuilder("Fecha,Total\n");
                for (Map<String, Object> d : dias)
                    csv.append(d.get("fecha")).append(",").append(d.get("total")).append("\n");
                agregarCsv(zos, "reservaciones_por_dia.csv", csv.toString());
            }

            if (isTrue(secciones, "canal") && data.containsKey("canalSplit")) {
                List<Map<String, Object>> c = (List<Map<String, Object>>) data.get("canalSplit");
                StringBuilder csv = new StringBuilder("Canal,Total\n");
                for (Map<String, Object> x : c)
                    csv.append(x.get("canal")).append(",").append(x.get("total")).append("\n");
                agregarCsv(zos, "canal.csv", csv.toString());
            }

            if (isTrue(secciones, "embudo") && data.containsKey("embudo")) {
                Map<String, Object> emb = (Map<String, Object>) data.get("embudo");
                StringBuilder csv = new StringBuilder("Estado,Total\n");
                for (Map.Entry<String, Object> e : emb.entrySet())
                    csv.append(e.getKey()).append(",").append(e.getValue()).append("\n");
                agregarCsv(zos, "embudo.csv", csv.toString());
            }

            if (isTrue(secciones, "hoteles") && data.containsKey("topHoteles")) {
                List<Map<String, Object>> h = (List<Map<String, Object>>) data.get("topHoteles");
                StringBuilder csv = new StringBuilder("Hotel,Reservaciones,Ingresos\n");
                for (Map<String, Object> x : h)
                    csv.append(x.get("hotel")).append(",")
                       .append(x.get("totalReservaciones")).append(",")
                       .append(num(x.get("ingresosTotales"))).append("\n");
                agregarCsv(zos, "top_hoteles.csv", csv.toString());
            }

            if (isTrue(secciones, "cancelaciones") && data.containsKey("cancelaciones")) {
                List<Map<String, Object>> c = (List<Map<String, Object>>) data.get("cancelaciones");
                StringBuilder csv = new StringBuilder("Tipo,Total\n");
                for (Map<String, Object> x : c)
                    csv.append(x.get("tipo")).append(",").append(x.get("total")).append("\n");
                agregarCsv(zos, "cancelaciones.csv", csv.toString());
            }

            if (isTrue(secciones, "tendencia") && data.containsKey("ingresosTendencia")) {
                List<Map<String, Object>> t = (List<Map<String, Object>>) data.get("ingresosTendencia");
                StringBuilder csv = new StringBuilder("Mes,Canal,Revenue\n");
                for (Map<String, Object> x : t)
                    csv.append(x.get("mes")).append(",")
                       .append(x.get("canal")).append(",")
                       .append(num(x.get("revenue"))).append("\n");
                agregarCsv(zos, "tendencia_ingresos.csv", csv.toString());
            }

            if (isTrue(secciones, "heatmap") && data.containsKey("heatmap")) {
                List<Map<String, Object>> h = (List<Map<String, Object>>) data.get("heatmap");
                StringBuilder csv = new StringBuilder("DiaSemana,Hora,Total\n");
                for (Map<String, Object> x : h)
                    csv.append(x.get("diaSemana")).append(",")
                       .append(x.get("hora")).append(",")
                       .append(x.get("total")).append("\n");
                agregarCsv(zos, "heatmap.csv", csv.toString());
            }

            if (isTrue(secciones, "registro") && data.containsKey("registro")) {
                List<Map<String, Object>> reg = (List<Map<String, Object>>) data.get("registro");
                StringBuilder csv = new StringBuilder("ID,Destino,CheckIn,CheckOut,Personas,Usuario,Tipo,Fecha\n");
                for (Map<String, Object> b : reg) {
                    Object usr = b.get("usuario");
                    csv.append(b.get("id")).append(",")
                       .append(esc(b.get("destino"))).append(",")
                       .append(esc(b.get("checkIn"))).append(",")
                       .append(esc(b.get("checkOut"))).append(",")
                       .append(b.getOrDefault("personas", "")).append(",")
                       .append(esc(usr != null ? usr : b.get("agencia"))).append(",")
                       .append(esc(b.get("tipo"))).append(",")
                       .append(esc(b.get("fechaHora"))).append("\n");
                }
                agregarCsv(zos, "registro_busquedas.csv", csv.toString());
            }

            zos.finish();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando CSV ZIP: " + e.getMessage(), e);
        }
    }

    // ─── UTILIDADES ──────────────────────────────────────────────────────────

    private static void agregarCsv(ZipOutputStream zos, String nombre, String contenido) throws Exception {
        zos.putNextEntry(new ZipEntry(nombre));
        zos.write(contenido.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static void escribirFila(Sheet sh, int fila, CellStyle style, String... headers) {
        Row row = sh.createRow(fila);
        for (int i = 0; i < headers.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(style);
        }
    }

    private static int escribirKpiRow(Sheet sh, int fila, CellStyle numStyle, String label, double val) {
        Row row = sh.createRow(fila);
        row.createCell(0).setCellValue(label);
        Cell valCell = row.createCell(1);
        valCell.setCellValue(val);
        valCell.setCellStyle(numStyle);
        return fila + 1;
    }

    private static void autosize(Sheet sh, int cols) {
        for (int i = 0; i < cols; i++) sh.autoSizeColumn(i);
    }

    private static CellStyle crearEstiloEncabezado(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloTitulo(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private static CellStyle crearEstiloNumero(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        return style;
    }

    private static boolean isTrue(Map<String, Boolean> m, String key) {
        return m != null && Boolean.TRUE.equals(m.get(key));
    }

    private static double num(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0.0;
    }

    private static String str(Object o) {
        return o != null ? String.valueOf(o) : "";
    }

    private static String esc(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o);
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
