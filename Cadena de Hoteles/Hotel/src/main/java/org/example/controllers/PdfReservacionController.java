package org.example.controllers;

import io.javalin.Javalin;
import org.example.services.PdfReservacionService;

import java.util.Map;

public class PdfReservacionController {

    private final PdfReservacionService pdfReservacionService = new PdfReservacionService();

    public void registerRoutes(Javalin app) {

        // GET /reservaciones/{id}/pdf — descarga el PDF de una reservación
        app.get("/reservaciones/{id}/pdf", ctx -> {
            int usuarioId     = ctx.attribute("usuarioId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

            try {
                byte[] pdf = pdfReservacionService.generarPdf(reservacionId, usuarioId);
                ctx.contentType("application/pdf")
                        .header("Content-Disposition", "attachment; filename=\"MIKU-" + reservacionId + ".pdf\"")
                        .result(pdf);
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}