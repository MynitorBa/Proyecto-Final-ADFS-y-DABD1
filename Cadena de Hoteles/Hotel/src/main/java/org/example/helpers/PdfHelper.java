package org.example.helpers;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.example.dtos.ReservacionDetalleDTO;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PdfHelper {

    public static byte[] generarPdfReservacion(List<ReservacionDetalleDTO> detalles, Object[] factura) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer   = new PdfWriter(baos);
        PdfDocument pdf    = new PdfDocument(writer);
        Document document  = new Document(pdf);

        ReservacionDetalleDTO primera = detalles.get(0);

        // ------------------------Título-----------------------------------
        document.add(new Paragraph("Reservación " + primera.getNoReservacion())
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(" "));

        // --Datos generales----
        document.add(new Paragraph("Datos de la Reservación")
                .setFontSize(14).setBold());

        Table tablaGeneral = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));

        agregarFila(tablaGeneral, "Número",        primera.getNoReservacion());
        agregarFila(tablaGeneral, "Estado",         primera.getEstado());
        agregarFila(tablaGeneral, "Fecha creación", primera.getFechaCreacion());
        agregarFila(tablaGeneral, "Total",          "Q " + primera.getTotal());

        if (primera.getFechaCancelacion() != null) {
            agregarFila(tablaGeneral, "Fecha cancelación", primera.getFechaCancelacion());
        }
        if (primera.getMotivoCancelacion() != null) {
            agregarFila(tablaGeneral, "Motivo cancelación", primera.getMotivoCancelacion());
        }

        document.add(tablaGeneral);
        document.add(new Paragraph(" "));

        // -------------------Detalle de habitaciones -------------------------------------
        document.add(new Paragraph("Habitaciones Reservadas")
                .setFontSize(14).setBold());

        for (ReservacionDetalleDTO detalle : detalles) {
            document.add(new Paragraph(detalle.getNombreHotel() + " — " + detalle.getTipoHabitacion())
                    .setFontSize(11).setBold().setFontColor(ColorConstants.DARK_GRAY));

            Table tablaHab = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100));

            agregarFila(tablaHab, "Tipo de cama",    detalle.getTipoCama());
            agregarFila(tablaHab, "Check-in",         detalle.getFechaCheckIn());
            agregarFila(tablaHab, "Check-out",        detalle.getFechaCheckOut());
            agregarFila(tablaHab, "Personas",         String.valueOf(detalle.getCantidadPersonas()));
            agregarFila(tablaHab, "Subtotal",         "Q " + detalle.getTotalDetalle());

            document.add(tablaHab);
            document.add(new Paragraph(" "));
        }

        //-- Factura (si existe) --------------------------------------
        if (factura != null) {
            document.add(new Paragraph("Factura")
                    .setFontSize(14).setBold());

            Table tablaFactura = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100));

            agregarFila(tablaFactura, "NIT",          (String) factura[2]);
            agregarFila(tablaFactura, "Código postal", (String) factura[3]);
            agregarFila(tablaFactura, "Fecha",         (String) factura[1]);
            agregarFila(tablaFactura, "Total factura", "Q " + factura[4]);

            document.add(tablaFactura);
        }

        document.close();
        return baos.toByteArray();
    }

    // -----------------------------------Helper para filas de tabla ---------------------------------

    private static void agregarFila(Table tabla, String etiqueta, String valor) {
        tabla.addCell(new Cell().add(new Paragraph(etiqueta).setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        tabla.addCell(new Cell().add(new Paragraph(valor != null ? valor : "-")));
    }
}