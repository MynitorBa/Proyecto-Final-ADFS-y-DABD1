package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.PagoRequestDTO;
import org.example.services.PagoService;

import java.util.Map;

/**
 * Controller que gestiona el procesamiento de pagos de reservaciones para usuarios autenticados.
 * Requiere sesion activa; el usuarioId se obtiene del contexto inyectado por el middleware JWT.
 */
public class PagoController {

    private final PagoService pagoService;

    /**
     * Crea una instancia de PagoController con sus dependencias inyectadas.
     */
    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /**
     * Registra la ruta de pago en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // Procesa el pago de una reservacion perteneciente al usuario autenticado
        app.post("/reservaciones/{id}/pago", ctx -> {

            // Extrae el usuario de la sesion y el ID de la reservacion desde el path
            int usuarioId     = ctx.attribute("usuarioId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));
            PagoRequestDTO request = ctx.bodyAsClass(PagoRequestDTO.class);

            try {
                ctx.status(200).json(pagoService.procesarPago(reservacionId, usuarioId, request));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}