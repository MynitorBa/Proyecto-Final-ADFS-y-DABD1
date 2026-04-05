package org.example.controllers;

import io.javalin.Javalin;
import org.example.services.PdfReservacionService;

import java.util.Map;

/**
 * Controller que gestiona la descarga del comprobante PDF de una reservacion.
 * Requiere sesion activa; el usuarioId se obtiene del contexto inyectado por el middleware JWT.
 */
public class PdfReservacionController {

    private final PdfReservacionService pdfReservacionService = new PdfReservacionService();

    /**
     * Registra la ruta de descarga de PDF en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // Genera y retorna el PDF de una reservacion como archivo adjunto descargable
        app.get("/reservaciones/{id}/pdf", ctx -> {

            // Extrae el usuario de la sesion y el ID de la reservacion desde el path
            int usuarioId     = ctx.attribute("usuarioId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

            try {
                byte[] pdf = pdfReservacionService.generarPdf(reservacionId, usuarioId);

                // Configura la respuesta para forzar la descarga del archivo con nombre unico por reservacion
                ctx.contentType("application/pdf")
                        .header("Content-Disposition", "attachment; filename=\"MIKU-" + reservacionId + ".pdf\"")
                        .result(pdf);
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}