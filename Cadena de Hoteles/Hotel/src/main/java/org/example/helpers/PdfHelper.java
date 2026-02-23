package org.example.helpers;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.example.dtos.ReservacionDetalleDTO;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PdfHelper {

    // ── Miku Inn brand palette ─────────────────────────────────────────
    private static final Color PRIMARY      = new DeviceRgb(0x66, 0x7E, 0xEA); // #667eea
    private static final Color SECONDARY    = new DeviceRgb(0x76, 0x4B, 0xA2); // #764ba2
    private static final Color DARK         = new DeviceRgb(0x0F, 0x17, 0x2A); // #0f172a
    private static final Color MEDIUM       = new DeviceRgb(0x1E, 0x29, 0x3B); // #1e293b
    private static final Color MUTED        = new DeviceRgb(0x64, 0x74, 0x8B); // #64748b
    private static final Color BG_LIGHT     = new DeviceRgb(0xEE, 0xF1, 0xF8); // #eef1f8
    private static final Color CARD         = new DeviceRgb(0xF7, 0xF9, 0xFC); // #f7f9fc
    private static final Color BORDER_COLOR = new DeviceRgb(0xDD, 0xE3, 0xF0); // #dde3f0
    private static final Color WHITE        = new DeviceRgb(0xFF, 0xFF, 0xFF);
    private static final Color SUCCESS      = new DeviceRgb(0x10, 0xB9, 0x81); // #10b981

    public static byte[] generarPdfReservacion(List<ReservacionDetalleDTO> detalles, Object[] factura) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter   writer   = new PdfWriter(baos);
        PdfDocument pdf      = new PdfDocument(writer);
        Document    document = new Document(pdf, PageSize.A4);
        document.setMargins(0, 0, 36, 0);

        ReservacionDetalleDTO primera = detalles.get(0);

        // ══════════════════════════════════════════════════════════════
        // HEADER — gradiente simulado con banda de color primario
        // ══════════════════════════════════════════════════════════════
        Table header = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell headerCell = new Cell()
                .setBackgroundColor(PRIMARY)
                .setPadding(32)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        headerCell.add(new Paragraph("MIKU INN")
                .setFontSize(28)
                .setBold()
                .setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        headerCell.add(new Paragraph("Comprobante de Reservación")
                .setFontSize(11)
                .setFontColor(new DeviceRgb(0xCC, 0xD6, 0xF8))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0));

        header.addCell(headerCell);
        document.add(header);

        // Banda secundaria con el número de reservación
        Table subHeader = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell subCell = new Cell()
                .setBackgroundColor(SECONDARY)
                .setPaddingTop(10).setPaddingBottom(10)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        subCell.add(new Paragraph("Reservación  " + primera.getNoReservacion())
                .setFontSize(13)
                .setBold()
                .setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0));

        subHeader.addCell(subCell);
        document.add(subHeader);

        // Espacio interior con padding
        document.setLeftMargin(36);
        document.setRightMargin(36);

        spacer(document, 18);

        // ══════════════════════════════════════════════════════════════
        // SECCIÓN: Datos de la Reservación
        // ══════════════════════════════════════════════════════════════
        seccionTitulo(document, "📋  Datos de la Reservación", PRIMARY);

        Table tablaGeneral = crearTabla();

        agregarFila(tablaGeneral, "Número de Reservación", primera.getNoReservacion(), true);
        agregarFilaEstado(tablaGeneral, "Estado", primera.getEstado());
        agregarFila(tablaGeneral, "Fecha de Creación", primera.getFechaCreacion(), false);
        agregarFilaTotal(tablaGeneral, "Total", "Q " + primera.getTotal());

        if (primera.getFechaCancelacion() != null) {
            agregarFila(tablaGeneral, "Fecha de Cancelación", primera.getFechaCancelacion(), false);
        }
        if (primera.getMotivoCancelacion() != null) {
            agregarFila(tablaGeneral, "Motivo de Cancelación", primera.getMotivoCancelacion(), false);
        }

        document.add(tablaGeneral);
        spacer(document, 20);

        // ══════════════════════════════════════════════════════════════
        // SECCIÓN: Habitaciones Reservadas
        // ══════════════════════════════════════════════════════════════
        seccionTitulo(document, "🛏  Habitaciones Reservadas", PRIMARY);

        for (int idx = 0; idx < detalles.size(); idx++) {
            ReservacionDetalleDTO detalle = detalles.get(idx);

            // Sub-encabezado de cada habitación
            Table habHeader = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(0);

            Cell habTitleCell = new Cell()
                    .setBackgroundColor(BG_LIGHT)
                    .setPadding(10)
                    .setBorder(new SolidBorder(BORDER_COLOR, 1))
                    .setBorderBottom(Border.NO_BORDER);

            habTitleCell.add(new Paragraph(detalle.getNombreHotel() + "  —  " + detalle.getTipoHabitacion())
                    .setFontSize(11)
                    .setBold()
                    .setFontColor(MEDIUM)
                    .setMarginBottom(0));

            habHeader.addCell(habTitleCell);
            document.add(habHeader);

            Table tablaHab = crearTabla();
            agregarFila(tablaHab, "Tipo de cama",  detalle.getTipoCama(),                         false);
            agregarFila(tablaHab, "Check-in",       detalle.getFechaCheckIn(),                     false);
            agregarFila(tablaHab, "Check-out",      detalle.getFechaCheckOut(),                    false);
            agregarFila(tablaHab, "Personas",        String.valueOf(detalle.getCantidadPersonas()), false);
            agregarFilaTotal(tablaHab, "Subtotal", "Q " + detalle.getTotalDetalle());

            document.add(tablaHab);

            if (idx < detalles.size() - 1) spacer(document, 10);
        }

        spacer(document, 20);

        // ══════════════════════════════════════════════════════════════
        // SECCIÓN: Factura
        // ══════════════════════════════════════════════════════════════
        if (factura != null) {
            seccionTitulo(document, "🧾  Factura", SECONDARY);

            Table tablaFactura = crearTabla();
            agregarFila(tablaFactura, "NIT",           (String) factura[2], false);
            agregarFila(tablaFactura, "Código postal",  (String) factura[3], false);
            agregarFila(tablaFactura, "Fecha",          (String) factura[1], false);
            agregarFilaTotal(tablaFactura, "Total Factura", "Q " + factura[4]);

            document.add(tablaFactura);
            spacer(document, 20);
        }

        // ══════════════════════════════════════════════════════════════
        // FOOTER
        // ══════════════════════════════════════════════════════════════
        Table footer = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10);

        Cell footerCell = new Cell()
                .setBackgroundColor(DARK)
                .setPadding(16)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        footerCell.add(new Paragraph("Miku Inn  ·  info@mikuinn.com  ·  +502 4276-8687  ·  Guatemala City, Guatemala")
                .setFontSize(9)
                .setFontColor(new DeviceRgb(0x94, 0xA3, 0xB8))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        footerCell.add(new Paragraph("Gracias por elegir Miku Inn. ¡Esperamos verte pronto!")
                .setFontSize(9)
                .setFontColor(new DeviceRgb(0x64, 0x74, 0x8B))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0));

        footer.addCell(footerCell);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    // ── Helpers de diseño ─────────────────────────────────────────────

    /** Título de sección con barra de color lateral */
    private static void seccionTitulo(Document doc, String texto, Color color) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{3, 97}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(6);

        // Barra lateral de color
        t.addCell(new Cell()
                .setBackgroundColor(color)
                .setBorder(Border.NO_BORDER)
                .setPadding(0));

        t.addCell(new Cell()
                .setBackgroundColor(BG_LIGHT)
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(12).setPaddingTop(9).setPaddingBottom(9)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph(texto)
                        .setFontSize(12)
                        .setBold()
                        .setFontColor(DARK)
                        .setMarginBottom(0)));

        doc.add(t);
    }

    /** Tabla base con bordes sutiles */
    private static Table crearTabla() {
        return new Table(UnitValue.createPercentArray(new float[]{38, 62}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(0)
                .setBorder(new SolidBorder(BORDER_COLOR, 1));
    }

    /** Fila estándar */
    private static void agregarFila(Table tabla, String etiqueta, String valor, boolean highlight) {
        Color bgLabel = highlight ? new DeviceRgb(0xE8, 0xED, 0xF8) : CARD;

        tabla.addCell(new Cell()
                .add(new Paragraph(etiqueta).setFontSize(9).setBold().setFontColor(MEDIUM))
                .setBackgroundColor(bgLabel)
                .setPadding(8)
                .setBorderRight(new SolidBorder(BORDER_COLOR, 1))
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER));

        tabla.addCell(new Cell()
                .add(new Paragraph(valor != null ? valor : "-").setFontSize(9).setFontColor(MEDIUM))
                .setBackgroundColor(WHITE)
                .setPadding(8)
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setBorderTop(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER));
    }

    /** Fila para estado con badge de color */
    private static void agregarFilaEstado(Table tabla, String etiqueta, String valor) {
        tabla.addCell(new Cell()
                .add(new Paragraph(etiqueta).setFontSize(9).setBold().setFontColor(MEDIUM))
                .setBackgroundColor(CARD)
                .setPadding(8)
                .setBorderRight(new SolidBorder(BORDER_COLOR, 1))
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER));

        String v = valor != null ? valor : "-";
        Color estadoColor = v.equalsIgnoreCase("Confirmada") ? SUCCESS
                : v.equalsIgnoreCase("Cancelada")  ? new DeviceRgb(0xEF,0x44,0x44)
                : v.equalsIgnoreCase("Completada") ? PRIMARY
                : MUTED;

        tabla.addCell(new Cell()
                .add(new Paragraph(v).setFontSize(9).setBold().setFontColor(estadoColor))
                .setBackgroundColor(WHITE)
                .setPadding(8)
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setBorderTop(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER));
    }

    /** Fila de total con color destacado */
    private static void agregarFilaTotal(Table tabla, String etiqueta, String valor) {
        tabla.addCell(new Cell()
                .add(new Paragraph(etiqueta).setFontSize(9).setBold().setFontColor(WHITE))
                .setBackgroundColor(PRIMARY)
                .setPadding(9)
                .setBorderRight(new SolidBorder(SECONDARY, 1))
                .setBorderBottom(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER));

        tabla.addCell(new Cell()
                .add(new Paragraph(valor != null ? valor : "-")
                        .setFontSize(11).setBold().setFontColor(PRIMARY))
                .setBackgroundColor(BG_LIGHT)
                .setPadding(9)
                .setBorderBottom(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER));
    }

    /** Espacio en blanco */
    private static void spacer(Document doc, float size) {
        doc.add(new Paragraph(" ").setFontSize(size / 3f).setMarginBottom(0).setMarginTop(0));
    }
}