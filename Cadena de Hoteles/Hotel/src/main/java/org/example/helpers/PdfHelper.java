package org.example.helpers;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.example.dtos.ReservacionDetalleDTO;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PdfHelper {

    private static final Color INK       = new DeviceRgb(0x1A, 0x1A, 0x1A);
    private static final Color TEXT_MID  = new DeviceRgb(0x44, 0x50, 0x60);
    private static final Color TEXT_SOFT = new DeviceRgb(0x78, 0x84, 0x96);
    private static final Color BDR       = new DeviceRgb(0xC8, 0xCE, 0xDA);
    private static final Color BDR_DARK  = new DeviceRgb(0x7A, 0x86, 0x9E);
    private static final Color BG_WHITE  = new DeviceRgb(0xFF, 0xFF, 0xFF);
    private static final Color BG_ROW    = new DeviceRgb(0xF5, 0xF7, 0xFA);
    private static final Color BG_LBL    = new DeviceRgb(0xEE, 0xF1, 0xF6);
    private static final Color BG_HI     = new DeviceRgb(0xE4, 0xE9, 0xF4);
    private static final Color ACCENT    = new DeviceRgb(0x3A, 0x52, 0x7C);
    private static final Color ACCENTBG  = new DeviceRgb(0xE2, 0xE8, 0xF4);
    private static final Color HDR_DARK  = new DeviceRgb(0x1E, 0x28, 0x3C);
    private static final Color HDR_MID   = new DeviceRgb(0x2C, 0x3A, 0x52);
    private static final Color HDR_TEXT  = new DeviceRgb(0xE8, 0xED, 0xF5);
    private static final Color HDR_SOFT  = new DeviceRgb(0x78, 0x88, 0xA6);
    private static final Color HDR_LINE  = new DeviceRgb(0x48, 0x60, 0x88);
    private static final Color S_OK      = new DeviceRgb(0x1A, 0x68, 0x3C);
    private static final Color S_ERR     = new DeviceRgb(0x84, 0x18, 0x18);
    private static final Color S_WARN    = new DeviceRgb(0x76, 0x4C, 0x10);

    private static final float HDR_H = 78f;
    private static final float FTR_H = 40f;
    private static final float MH    = 50f;
    private static final float MT    = HDR_H + 26f;
    private static final float MB    = FTR_H + 24f;

    private static final float GAP = 22f;

    // Número de columnas de la tabla de habitaciones (usado en colspans)
    private static final int COLS = 9;

    public static byte[] generarPdfReservacion(List<ReservacionDetalleDTO> detalles, Object[] factura) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));

        ReservacionDetalleDTO p0 = detalles.get(0);
        boolean esF = factura != null;

        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new HeaderRenderer(p0, esF));
        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterRenderer());

        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(MT, MH, MB, MH);

        doc.add(bloque(buildInfoBlock(p0, esF, factura)));
        doc.add(sp(GAP));

        doc.add(bloque(buildTablaConTotales(detalles, p0, esF)));
        doc.add(sp(GAP));

        if (esF) {
            doc.add(bloque(buildDatosFactura(factura, p0)));
            doc.add(sp(GAP));
        }

        doc.add(new Div().setKeepTogether(true).add(bloque(buildCondiciones())));

        doc.close();
        return baos.toByteArray();
    }

    private static Div bloque(Table inner) {
        return new Div()
                .setBorder(new SolidBorder(BDR_DARK, 0.8f))
                .setBackgroundColor(BG_WHITE)
                .add(inner);
    }

    // ══════════════════════════════════════════════════════════════════
    //  HEADER / FOOTER
    // ══════════════════════════════════════════════════════════════════

    static class HeaderRenderer implements IEventHandler {
        private final ReservacionDetalleDTO p0;
        private final boolean esF;
        HeaderRenderer(ReservacionDetalleDTO p0, boolean esF) { this.p0 = p0; this.esF = esF; }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent e = (PdfDocumentEvent) event;
            PdfPage page = e.getPage();
            PdfDocument pdf = e.getDocument();
            float pw = page.getPageSize().getWidth();
            float ph = page.getPageSize().getHeight();

            PdfCanvas cv = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
            cv.saveState();
            cv.setFillColor(HDR_DARK);
            cv.rectangle(0, ph - HDR_H, pw, HDR_H);
            cv.fill();
            cv.setFillColor(HDR_LINE);
            cv.rectangle(0, ph - HDR_H, pw, 3f);
            cv.fill();
            cv.restoreState();

            Rectangle area = new Rectangle(MH, ph - HDR_H + 3, pw - 2 * MH, HDR_H - 6);
            try (Canvas c = new Canvas(cv, area)) {
                Table t = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                        .setWidth(UnitValue.createPercentValue(100));

                Cell L = new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingTop(12);
                L.add(new Paragraph("MIKU INN").setFontSize(22).setBold()
                        .setFontColor(HDR_TEXT).setCharacterSpacing(1.5f).setMarginBottom(5));
                L.add(new Paragraph("Hotel Boutique  ·  Guatemala City  ·  info@mikuinn.com")
                        .setFontSize(7.5f).setFontColor(HDR_SOFT));
                t.addCell(L);

                Cell R = new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingTop(12);
                R.add(new Paragraph(esF ? "FACTURA" : "COMPROBANTE").setFontSize(7.5f).setBold()
                        .setFontColor(HDR_SOFT).setCharacterSpacing(2f)
                        .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(5));
                R.add(new Paragraph(p0.getNoReservacion()).setFontSize(13).setBold()
                        .setFontColor(HDR_TEXT).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(6));
                R.add(new Paragraph("● " + (p0.getEstado() != null ? p0.getEstado().toUpperCase() : "—"))
                        .setFontSize(7.5f).setBold().setFontColor(sColor(p0.getEstado()))
                        .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(3));
                R.add(new Paragraph("Emitido: " + p0.getFechaCreacion()).setFontSize(7f)
                        .setFontColor(HDR_SOFT).setTextAlignment(TextAlignment.RIGHT));
                t.addCell(R);
                c.add(t);
            }
        }
    }

    static class FooterRenderer implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent e = (PdfDocumentEvent) event;
            PdfPage page = e.getPage();
            PdfDocument pdf = e.getDocument();
            float pw = page.getPageSize().getWidth();

            PdfCanvas cv = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
            cv.saveState();
            cv.setFillColor(HDR_LINE);
            cv.rectangle(0, FTR_H - 2, pw, 2f);
            cv.fill();
            cv.setFillColor(HDR_DARK);
            cv.rectangle(0, 0, pw, FTR_H - 2);
            cv.fill();
            cv.restoreState();

            try (Canvas c = new Canvas(cv, new Rectangle(0, 0, pw, FTR_H))) {
                Table ft = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                        .setWidth(UnitValue.createPercentValue(100));
                ft.addCell(new Cell().setBorder(Border.NO_BORDER)
                        .add(new Paragraph("MIKU INN  ·  info@mikuinn.com  ·  +502 4276-8687  ·  Guatemala City, Guatemala")
                                .setFontSize(7f).setFontColor(HDR_SOFT))
                        .setPaddingLeft(MH).setPaddingTop(13));
                ft.addCell(new Cell().setBorder(Border.NO_BORDER)
                        .add(new Paragraph("Comprobante oficial de reservación")
                                .setFontSize(7f).setFontColor(new DeviceRgb(0x55, 0x62, 0x78))
                                .setTextAlignment(TextAlignment.RIGHT))
                        .setTextAlignment(TextAlignment.RIGHT).setPaddingRight(MH).setPaddingTop(13));
                c.add(ft);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  BLOQUE 1 — Info reservación + facturación
    // ══════════════════════════════════════════════════════════════════

    private static Table buildInfoBlock(ReservacionDetalleDTO p, boolean esF, Object[] fac) {
        Table outer = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell cL = new Cell().setBorder(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(BDR, 1f))
                .setPadding(18);
        cL.add(colTitle("DATOS DE LA RESERVACIÓN"));
        cL.add(sp(8));
        Table tL = infoTbl();
        iRow(tL, "Nro. Reservación", p.getNoReservacion(), true);
        iRow(tL, "Hotel",            nn(p.getNombreHotel()), false);
        iRow(tL, "Check-in",         nn(p.getFechaCheckIn()), false);
        iRow(tL, "Check-out",        nn(p.getFechaCheckOut()), false);
        if (p.getFechaCancelacion() != null)  iRow(tL, "Cancelación", p.getFechaCancelacion(), false);
        if (p.getMotivoCancelacion() != null) iRow(tL, "Motivo",      p.getMotivoCancelacion(), false);
        cL.add(tL);
        outer.addCell(cL);

        Cell cR = new Cell().setBorder(Border.NO_BORDER).setPadding(18);
        if (esF && fac != null) {
            cR.add(colTitle("DATOS DE FACTURACIÓN"));
            cR.add(sp(8));
            Table tR = infoTbl();
            iRow(tR, "NIT / RFC",     s(fac[2]), false);
            iRow(tR, "Código Postal", s(fac[3]), false);
            iRow(tR, "Fecha Emisión", s(fac[1]), false);
            cR.add(tR);
        } else {
            cR.add(colTitle("DATOS DE CONTACTO"));
            cR.add(sp(8));
            Table tR = infoTbl();
            iRow(tR, "Email",  "info@mikuinn.com",   false);
            iRow(tR, "Tel.",   "+502 4276-8687",      false);
            iRow(tR, "Ciudad", "Guatemala City, GT",  false);
            cR.add(tR);
        }
        outer.addCell(cR);
        return outer;
    }

    // ══════════════════════════════════════════════════════════════════
    //  BLOQUE 2 — Tabla de habitaciones + filas de total al final
    // ══════════════════════════════════════════════════════════════════

    private static Table buildTablaConTotales(List<ReservacionDetalleDTO> detalles,
                                              ReservacionDetalleDTO p0, boolean esF) {
        Table outer = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(100));

        outer.addCell(titleBar(esF ? "CONCEPTOS Y SERVICIOS" : "DETALLE DE HABITACIONES"));

        Cell tableWrap = new Cell().setBorder(Border.NO_BORDER).setPadding(0);

        // ── 9 columnas: se añadió "Nro. Hab." entre "Habitación" y "Cama" ──
        Table t = new Table(UnitValue.createPercentArray(new float[]{5, 19, 10, 13, 7, 7, 13, 13, 13}))
                .setWidth(UnitValue.createPercentValue(100));

        String[]        hs = {"#", "Habitación", "Nro. Hab.", "Cama", "Noches", "Pers.", "Check-in", "Check-out", "Subtotal"};
        TextAlignment[] ta = {
                TextAlignment.CENTER,   // #
                TextAlignment.LEFT,     // Habitación
                TextAlignment.CENTER,   // Nro. Hab.  ← NUEVO
                TextAlignment.LEFT,     // Cama
                TextAlignment.CENTER,   // Noches
                TextAlignment.CENTER,   // Pers.
                TextAlignment.CENTER,   // Check-in
                TextAlignment.CENTER,   // Check-out
                TextAlignment.RIGHT     // Subtotal
        };
        for (int i = 0; i < hs.length; i++) t.addHeaderCell(thC(hs[i], ta[i]));

        for (int i = 0; i < detalles.size(); i++) {
            ReservacionDetalleDTO d = detalles.get(i);
            Color bg = i % 2 == 0 ? BG_WHITE : BG_ROW;
            t.addCell(tdC(String.valueOf(i + 1),                            TextAlignment.CENTER, bg, false));
            t.addCell(tdC(nn(d.getTipoHabitacion()),                         TextAlignment.LEFT,   bg, false));
            t.addCell(tdC(nn(d.getNumeroHabitacion()),                       TextAlignment.CENTER, bg, false)); // ← NUEVO
            t.addCell(tdC(nn(d.getTipoCama()),                               TextAlignment.LEFT,   bg, false));
            t.addCell(tdC(nights(d.getFechaCheckIn(), d.getFechaCheckOut()), TextAlignment.CENTER, bg, false));
            t.addCell(tdC(String.valueOf(d.getCantidadPersonas()),           TextAlignment.CENTER, bg, false));
            t.addCell(tdC(nn(d.getFechaCheckIn()),                           TextAlignment.CENTER, bg, false));
            t.addCell(tdC(nn(d.getFechaCheckOut()),                          TextAlignment.CENTER, bg, false));
            t.addCell(tdC("$ " + fmt(d.getTotalDetalle()),                   TextAlignment.RIGHT,  bg, true));
        }

        // Separador horizontal — abarca COLS columnas
        t.addCell(new Cell(1, COLS).setHeight(1f).setBackgroundColor(BDR)
                .setBorder(Border.NO_BORDER).setPadding(0));

        // Sub-totales por habitación (solo si hay más de una)
        if (detalles.size() > 1) {
            for (int i = 0; i < detalles.size(); i++) {
                ReservacionDetalleDTO d = detalles.get(i);
                // Celda vacía ocupa las primeras (COLS - 3) columnas
                t.addCell(new Cell(1, COLS - 3).setBorder(Border.NO_BORDER)
                        .setBackgroundColor(BG_WHITE).setPadding(0));
                t.addCell(new Cell(1, 2)
                        .add(new Paragraph("Hab. " + (i + 1) + "  " + nn(d.getTipoHabitacion()))
                                .setFontSize(8).setFontColor(TEXT_MID).setBold())
                        .setBackgroundColor(BG_LBL)
                        .setPaddingTop(8).setPaddingBottom(8).setPaddingLeft(10).setPaddingRight(8)
                        .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                        .setBorderRight(new SolidBorder(BDR, 0.5f))
                        .setBorderBottom(new SolidBorder(BDR, 0.5f)));
                t.addCell(new Cell(1, 1)
                        .add(new Paragraph("$ " + fmt(d.getTotalDetalle()))
                                .setFontSize(8).setFontColor(INK).setBold()
                                .setTextAlignment(TextAlignment.RIGHT))
                        .setBackgroundColor(BG_WHITE)
                        .setPaddingTop(8).setPaddingBottom(8).setPaddingLeft(8).setPaddingRight(10)
                        .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                        .setBorderRight(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(BDR, 0.5f)));
            }
            // Línea divisoria antes del total general
            t.addCell(new Cell(1, COLS - 3).setBorder(Border.NO_BORDER)
                    .setBackgroundColor(BG_WHITE).setPadding(0));
            t.addCell(new Cell(1, 3).setHeight(1f).setBackgroundColor(BDR_DARK)
                    .setBorder(Border.NO_BORDER).setPadding(0));
        }

        // Fila TOTAL RESERVACIÓN — misma lógica de colspan
        t.addCell(new Cell(1, COLS - 3).setBorder(Border.NO_BORDER)
                .setBackgroundColor(BG_WHITE).setPadding(0));
        t.addCell(new Cell(1, 2)
                .add(new Paragraph("TOTAL RESERVACIÓN")
                        .setFontSize(9.5f).setBold().setFontColor(HDR_TEXT))
                .setBackgroundColor(HDR_MID)
                .setPaddingTop(11).setPaddingBottom(11).setPaddingLeft(10).setPaddingRight(8)
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(new DeviceRgb(0x44, 0x54, 0x70), 0.5f))
                .setBorderBottom(Border.NO_BORDER));
        t.addCell(new Cell(1, 1)
                .add(new Paragraph("$ " + fmt(p0.getTotal()))
                        .setFontSize(9.5f).setBold().setFontColor(ACCENT)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(ACCENTBG)
                .setPaddingTop(11).setPaddingBottom(11).setPaddingLeft(8).setPaddingRight(10)
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER));

        tableWrap.add(t);
        outer.addCell(tableWrap);
        return outer;
    }

    // ══════════════════════════════════════════════════════════════════
    //  BLOQUE 3 — Datos fiscales
    // ══════════════════════════════════════════════════════════════════

    private static Table buildDatosFactura(Object[] fac, ReservacionDetalleDTO p0) {
        Table outer = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(100));

        outer.addCell(titleBar("DATOS FISCALES"));

        String[] lbls = {"NIT / RFC", "Código Postal", "Fecha Emisión", "Total Factura"};
        String[] vals = {s(fac[2]), s(fac[3]), s(fac[1]), "$ " + fmt(p0.getTotal())};

        Table grid = new Table(UnitValue.createPercentArray(new float[]{28, 18, 20, 34}))
                .setWidth(UnitValue.createPercentValue(100));

        for (int i = 0; i < lbls.length; i++) {
            boolean last = i == lbls.length - 1;
            grid.addCell(new Cell()
                    .add(new Paragraph(lbls[i]).setFontSize(7.5f).setBold().setFontColor(TEXT_MID))
                    .setBackgroundColor(BG_LBL)
                    .setPaddingTop(10).setPaddingBottom(10).setPaddingLeft(16).setPaddingRight(16)
                    .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(BDR, 0.6f))
                    .setBorderRight(last ? Border.NO_BORDER : new SolidBorder(BDR, 0.6f)));
        }
        for (int i = 0; i < vals.length; i++) {
            boolean last = i == vals.length - 1;
            grid.addCell(new Cell()
                    .add(new Paragraph(vals[i]).setFontSize(last ? 12 : 9).setBold()
                            .setFontColor(last ? ACCENT : INK))
                    .setBackgroundColor(last ? ACCENTBG : BG_WHITE)
                    .setPaddingTop(11).setPaddingBottom(11).setPaddingLeft(16).setPaddingRight(16)
                    .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                    .setBorderBottom(Border.NO_BORDER)
                    .setBorderRight(last ? Border.NO_BORDER : new SolidBorder(BDR, 0.6f)));
        }

        outer.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0).add(grid));
        return outer;
    }

    // ══════════════════════════════════════════════════════════════════
    //  BLOQUE 4 — Términos y condiciones
    // ══════════════════════════════════════════════════════════════════

    private static Table buildCondiciones() {
        Table outer = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(100));

        outer.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BDR, 0.8f))
                .setBackgroundColor(BG_LBL)
                .setPaddingTop(10).setPaddingBottom(10).setPaddingLeft(18).setPaddingRight(18)
                .add(new Paragraph("TÉRMINOS Y CONDICIONES").setFontSize(7.5f).setBold()
                        .setFontColor(TEXT_MID).setCharacterSpacing(0.8f)));

        outer.addCell(new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(BG_WHITE)
                .setPaddingTop(14).setPaddingBottom(14).setPaddingLeft(18).setPaddingRight(18)
                .add(new Paragraph(
                        "1. Esta reservación es válida únicamente para las fechas indicadas.\n" +
                                "2. Check-in estándar: 15:00 hrs  ·  Check-out estándar: 12:00 hrs.\n" +
                                "3. Cancelaciones con menos de 24 hrs de anticipación generan cargo del 100%.\n" +
                                "4. El hotel no se responsabiliza por objetos de valor no declarados en recepción.\n" +
                                "5. Este documento es comprobante oficial de reservación.")
                        .setFontSize(7.5f).setFontColor(TEXT_SOFT)));
        return outer;
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════

    private static Cell titleBar(String txt) {
        return new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(HDR_DARK, 1f))
                .setBackgroundColor(HDR_MID)
                .setPaddingTop(10).setPaddingBottom(10).setPaddingLeft(18).setPaddingRight(18)
                .add(new Paragraph(txt).setFontSize(7.5f).setBold()
                        .setFontColor(HDR_TEXT).setCharacterSpacing(1.2f));
    }

    private static Cell thC(String txt, TextAlignment ta) {
        return new Cell()
                .add(new Paragraph(txt).setFontSize(7.5f).setBold().setFontColor(HDR_TEXT).setTextAlignment(ta))
                .setBackgroundColor(HDR_MID)
                .setPaddingTop(10).setPaddingBottom(10).setPaddingLeft(8).setPaddingRight(8)
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(HDR_DARK, 1f))
                .setBorderRight(new SolidBorder(new DeviceRgb(0x42, 0x52, 0x6E), 0.5f))
                .setTextAlignment(ta);
    }

    private static Cell tdC(String txt, TextAlignment ta, Color bg, boolean bold) {
        Paragraph p = new Paragraph(txt != null ? txt : "—").setFontSize(8).setFontColor(INK).setTextAlignment(ta);
        if (bold) p.setBold();
        return new Cell().add(p).setBackgroundColor(bg)
                .setPaddingTop(9).setPaddingBottom(9).setPaddingLeft(8).setPaddingRight(8)
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BDR, 0.6f))
                .setBorderRight(new SolidBorder(BDR, 0.5f))
                .setTextAlignment(ta);
    }

    private static Paragraph colTitle(String txt) {
        return new Paragraph(txt).setFontSize(7.5f).setBold().setFontColor(ACCENT)
                .setCharacterSpacing(0.8f).setMarginBottom(0);
    }

    private static Table infoTbl() {
        return new Table(UnitValue.createPercentArray(new float[]{38, 62}))
                .setWidth(UnitValue.createPercentValue(100));
    }

    private static void iRow(Table t, String lbl, String val, boolean hi) {
        t.addCell(new Cell()
                .add(new Paragraph(lbl).setFontSize(7.5f).setBold().setFontColor(TEXT_MID))
                .setBackgroundColor(hi ? BG_HI : BG_LBL)
                .setPaddingTop(8).setPaddingBottom(8).setPaddingLeft(10).setPaddingRight(8)
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                .setBorderRight(new SolidBorder(BDR, 0.6f))
                .setBorderBottom(new SolidBorder(BDR, 0.6f)));
        t.addCell(new Cell()
                .add(new Paragraph(val != null ? val : "—").setFontSize(7.5f).setFontColor(INK))
                .setBackgroundColor(BG_WHITE)
                .setPaddingTop(8).setPaddingBottom(8).setPaddingLeft(10).setPaddingRight(10)
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BDR, 0.6f)));
    }

    private static Paragraph sp(float pts) {
        return new Paragraph(" ").setFontSize(pts / 4f).setMargin(0);
    }

    private static Color sColor(String e) {
        if (e == null) return new DeviceRgb(0x88, 0x9A, 0xB4);
        return switch (e.toLowerCase()) {
            case "confirmada"  -> S_OK;
            case "cancelada"   -> S_ERR;
            case "pendiente"   -> S_WARN;
            default            -> new DeviceRgb(0x88, 0x9A, 0xB4);
        };
    }

    private static String nights(String ci, String co) {
        try {
            java.time.LocalDate d1 = java.time.LocalDate.parse(ci.substring(0, 10));
            java.time.LocalDate d2 = java.time.LocalDate.parse(co.substring(0, 10));
            return java.time.temporal.ChronoUnit.DAYS.between(d1, d2) + " n.";
        } catch (Exception ex) { return "—"; }
    }

    private static String fmt(Object o) {
        if (o == null) return "—";
        try {
            double d = Double.parseDouble(o.toString());
            return d == Math.floor(d) ? String.format("%,.0f", d) : String.format("%,.2f", d);
        } catch (Exception e) { return o.toString(); }
    }

    private static String s(Object o)  { return o != null ? o.toString() : "—"; }
    private static String nn(String s) { return s != null && !s.isBlank() ? s : "—"; }
}