package org.example.helpers;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * Helper para generar reportes de metricas en formato PDF usando iText 7.
 * Produce un documento A4 con encabezado Miku Inn, KPIs y tablas por seccion.
 */
public class MetricasPdfHelper {

    // Misma paleta que el PDF de confirmación de reserva (PdfHelper.java)
    private static final DeviceRgb HDR_DARK  = new DeviceRgb(0x1E, 0x28, 0x3C);
    private static final DeviceRgb HDR_TEXT  = new DeviceRgb(0xE8, 0xED, 0xF5);
    private static final DeviceRgb HDR_LINE  = new DeviceRgb(0x48, 0x60, 0x88);
    private static final DeviceRgb ACCENT    = new DeviceRgb(0x3A, 0x52, 0x7C);
    private static final DeviceRgb BG_ROW    = new DeviceRgb(0xF5, 0xF7, 0xFA);
    private static final DeviceRgb BDR       = new DeviceRgb(0xC8, 0xCE, 0xDA);
    private static final DeviceRgb TEXT_MID  = new DeviceRgb(0x44, 0x50, 0x60);
    private static final DeviceRgb TEXT_SOFT = new DeviceRgb(0x78, 0x84, 0x96);
    private static final DeviceRgb BLANCO    = new DeviceRgb(0xFF, 0xFF, 0xFF);
    private static final DeviceRgb DORADO    = new DeviceRgb(0xD4, 0xAF, 0x37);

    @SuppressWarnings("unchecked")
    public static byte[] generarPdf(Map<String, Object> data, Map<String, Boolean> secciones,
                                    String desde, String hasta) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf  = new PdfDocument(writer);
             Document doc     = new Document(pdf, PageSize.A4)) {

            doc.setMargins(40, 40, 40, 40);

            // ── Encabezado ──────────────────────────────────────────────────
            Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .useAllAvailableWidth();
            Cell hCell = new Cell()
                    .setBackgroundColor(HDR_DARK)
                    .setPadding(18)
                    .setBorder(Border.NO_BORDER);
            hCell.add(new Paragraph("MIKU INN")
                    .setFontColor(DORADO)
                    .setBold()
                    .setFontSize(22)
                    .setMarginBottom(2));
            hCell.add(new Paragraph("Reporte de Métricas")
                    .setFontColor(HDR_TEXT)
                    .setFontSize(11)
                    .setMarginBottom(0));
            hCell.add(new Paragraph("Período: " + desde + " al " + hasta)
                    .setFontColor(TEXT_SOFT)
                    .setFontSize(9)
                    .setMarginBottom(0));
            header.addCell(hCell);
            doc.add(header);
            doc.add(new Paragraph(" ").setFontSize(6));

            // ── KPI de Ingresos ──────────────────────────────────────────────
            if (isTrue(secciones, "kpi") && data.containsKey("ingresosKpi")) {
                Map<String, Object> kpi = (Map<String, Object>) data.get("ingresosKpi");
                doc.add(seccionTitulo("KPI de Ingresos"));

                Table kpiTable = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                        .useAllAvailableWidth().setMarginBottom(12);
                kpiTable.addHeaderCell(hdrCell("Métrica")).addHeaderCell(hdrCell("Valor"));
                addKpiRow(kpiTable, "Ingresos Totales", "Q " + fmt(num(kpi.get("ingresosTotales"))));
                addKpiRow(kpiTable, "Ingresos Directo", "Q " + fmt(num(kpi.get("ingresosDirecto"))));
                addKpiRow(kpiTable, "Ingresos Agencia", "Q " + fmt(num(kpi.get("ingresosAgencia"))));
                addKpiRow(kpiTable, "Ticket Promedio",  "Q " + fmt(num(kpi.get("ticketPromedio"))));
                addKpiRow(kpiTable, "Reservaciones Pagadas", String.valueOf(intVal(kpi.get("reservacionesPagadas"))));
                addKpiRow(kpiTable, "Total Reservaciones", String.valueOf(intVal(kpi.get("totalReservaciones"))));
                doc.add(kpiTable);
            }

            // ── Canal Split ──────────────────────────────────────────────────
            if (isTrue(secciones, "canal") && data.containsKey("canalSplit")) {
                List<Map<String, Object>> canales = (List<Map<String, Object>>) data.get("canalSplit");
                doc.add(seccionTitulo("Reservaciones por Canal"));
                Table t = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                        .useAllAvailableWidth().setMarginBottom(12);
                t.addHeaderCell(hdrCell("Canal")).addHeaderCell(hdrCell("Total"));
                boolean alt = false;
                for (Map<String, Object> c : canales) {
                    addRow(t, str(c.get("canal")), String.valueOf(intVal(c.get("total"))), alt);
                    alt = !alt;
                }
                doc.add(t);
            }

            // ── Embudo ───────────────────────────────────────────────────────
            if (isTrue(secciones, "embudo") && data.containsKey("embudo")) {
                Map<String, Object> emb = (Map<String, Object>) data.get("embudo");
                doc.add(seccionTitulo("Embudo de Conversión"));
                Table t = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                        .useAllAvailableWidth().setMarginBottom(12);
                t.addHeaderCell(hdrCell("Estado")).addHeaderCell(hdrCell("Total"));
                String[][] estados = {
                    {"Completadas", str(emb.get("completadas"))},
                    {"Pagadas (Confirmadas)", str(emb.get("pagadas"))},
                    {"Pendientes", str(emb.get("pendientes"))},
                    {"Expiradas", str(emb.get("expiradas"))},
                    {"Canceladas", str(emb.get("canceladas"))}
                };
                boolean alt = false;
                for (String[] row : estados) {
                    addRow(t, row[0], row[1], alt); alt = !alt;
                }
                doc.add(t);
            }

            // ── Top Hoteles ──────────────────────────────────────────────────
            if (isTrue(secciones, "hoteles") && data.containsKey("topHoteles")) {
                List<Map<String, Object>> hoteles = (List<Map<String, Object>>) data.get("topHoteles");
                doc.add(seccionTitulo("Top Hoteles por Ingresos"));
                Table t = new Table(UnitValue.createPercentArray(new float[]{0.5f, 2.5f, 1f, 1.5f}))
                        .useAllAvailableWidth().setMarginBottom(12);
                t.addHeaderCell(hdrCell("#"))
                 .addHeaderCell(hdrCell("Hotel"))
                 .addHeaderCell(hdrCell("Reservaciones"))
                 .addHeaderCell(hdrCell("Ingresos (Q)"));
                int rank = 1; boolean alt = false;
                for (Map<String, Object> h : hoteles) {
                    DeviceRgb bg = alt ? BG_ROW : BLANCO;
                    t.addCell(dataCell(String.valueOf(rank++), bg));
                    t.addCell(dataCell(str(h.get("hotel")), bg));
                    t.addCell(dataCell(String.valueOf(intVal(h.get("totalReservaciones"))), bg));
                    t.addCell(dataCell("Q " + fmt(num(h.get("ingresosTotales"))), bg));
                    alt = !alt;
                }
                doc.add(t);
            }

            // ── Cancelaciones ────────────────────────────────────────────────
            if (isTrue(secciones, "cancelaciones") && data.containsKey("cancelaciones")) {
                List<Map<String, Object>> canc = (List<Map<String, Object>>) data.get("cancelaciones");
                doc.add(seccionTitulo("Cancelaciones por Tipo"));
                Table t = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                        .useAllAvailableWidth().setMarginBottom(12);
                t.addHeaderCell(hdrCell("Tipo")).addHeaderCell(hdrCell("Total"));
                boolean alt = false;
                for (Map<String, Object> c : canc) {
                    addRow(t, str(c.get("tipo")), String.valueOf(intVal(c.get("total"))), alt);
                    alt = !alt;
                }
                doc.add(t);
            }

            // ── Tendencia de Ingresos ────────────────────────────────────────
            if (isTrue(secciones, "tendencia") && data.containsKey("ingresosTendencia")) {
                List<Map<String, Object>> tend = (List<Map<String, Object>>) data.get("ingresosTendencia");
                doc.add(seccionTitulo("Tendencia de Ingresos"));
                Table t = new Table(UnitValue.createPercentArray(new float[]{1.5f, 1.5f, 2f}))
                        .useAllAvailableWidth().setMarginBottom(12);
                t.addHeaderCell(hdrCell("Mes"))
                 .addHeaderCell(hdrCell("Canal"))
                 .addHeaderCell(hdrCell("Revenue (Q)"));
                boolean alt = false;
                for (Map<String, Object> x : tend) {
                    DeviceRgb bg = alt ? BG_ROW : BLANCO;
                    t.addCell(dataCell(str(x.get("mes")), bg));
                    t.addCell(dataCell(str(x.get("canal")), bg));
                    t.addCell(dataCell("Q " + fmt(num(x.get("revenue"))), bg));
                    alt = !alt;
                }
                doc.add(t);
            }

            // ── Reservaciones por Día ────────────────────────────────────────
            if (isTrue(secciones, "reservacionesDiarias") && data.containsKey("reservacionesPorDia")) {
                List<Map<String, Object>> dias = (List<Map<String, Object>>) data.get("reservacionesPorDia");
                doc.add(seccionTitulo("Reservaciones por Día"));
                Table t = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                        .useAllAvailableWidth().setMarginBottom(12);
                t.addHeaderCell(hdrCell("Fecha")).addHeaderCell(hdrCell("Total"));
                boolean alt = false;
                for (Map<String, Object> d : dias) {
                    addRow(t, str(d.get("fecha")), String.valueOf(intVal(d.get("total"))), alt);
                    alt = !alt;
                }
                doc.add(t);
            }

            // ── Pie de página ────────────────────────────────────────────────
            doc.add(new Paragraph(" ").setFontSize(4));
            Table footer = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
            Cell fCell = new Cell()
                    .setBorderTop(new SolidBorder(BDR, 1))
                    .setBorderBottom(Border.NO_BORDER)
                    .setBorderLeft(Border.NO_BORDER)
                    .setBorderRight(Border.NO_BORDER)
                    .setPaddingTop(6);
            fCell.add(new Paragraph("Generado por el Panel de Administración de Miku Inn  ·  " + new java.util.Date())
                    .setFontColor(TEXT_SOFT)
                    .setFontSize(7)
                    .setTextAlignment(TextAlignment.CENTER));
            footer.addCell(fCell);
            doc.add(footer);

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }

    // ─── UTILIDADES ──────────────────────────────────────────────────────────

    private static Paragraph seccionTitulo(String texto) {
        return new Paragraph(texto)
                .setBold()
                .setFontSize(11)
                .setFontColor(ACCENT)
                .setBorderBottom(new SolidBorder(HDR_LINE, 1.5f))
                .setPaddingBottom(3)
                .setMarginBottom(6)
                .setMarginTop(10);
    }

    private static Cell hdrCell(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setBold().setFontColor(HDR_TEXT).setFontSize(9))
                .setBackgroundColor(HDR_DARK)
                .setPadding(6)
                .setBorder(Border.NO_BORDER);
    }

    private static void addKpiRow(Table t, String label, String valor) {
        t.addCell(new Cell().add(new Paragraph(label).setFontSize(9))
                .setPadding(5).setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BDR, 0.5f)));
        t.addCell(new Cell().add(new Paragraph(valor).setFontSize(9).setBold())
                .setPadding(5).setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BDR, 0.5f)));
    }

    private static void addRow(Table t, String col1, String col2, boolean alternate) {
        DeviceRgb bg = alternate ? BG_ROW : BLANCO;
        t.addCell(dataCell(col1, bg));
        t.addCell(dataCell(col2, bg));
    }

    private static Cell dataCell(String texto, DeviceRgb bg) {
        return new Cell()
                .add(new Paragraph(texto != null ? texto : "").setFontSize(8.5f))
                .setBackgroundColor(bg)
                .setPadding(5)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BDR, 0.3f));
    }

    private static boolean isTrue(Map<String, Boolean> m, String key) {
        return m != null && Boolean.TRUE.equals(m.get(key));
    }

    private static double num(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0.0;
    }

    private static int intVal(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    private static String str(Object o) {
        return o != null ? String.valueOf(o) : "";
    }

    private static String fmt(double v) {
        return String.format("%,.2f", v);
    }
}
