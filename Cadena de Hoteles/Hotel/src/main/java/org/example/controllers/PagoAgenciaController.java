package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.PagoAgenciaRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.PagoAgenciaService;

import java.util.Map;

/**
 * Controller que gestiona el procesamiento de pagos de reservaciones realizadas por agencias externas.
 * Requiere autenticacion mediante el header X-Agencia-Token.
 */
public class PagoAgenciaController {

    private final PagoAgenciaService pagoAgenciaService;

    /**
     * Crea una instancia de PagoAgenciaController con sus dependencias inyectadas.
     */
    public PagoAgenciaController(PagoAgenciaService pagoAgenciaService) {
        this.pagoAgenciaService = pagoAgenciaService;
    }

    /**
     * Registra la ruta de pago de agencias en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // Procesa el pago de una reservacion asociada a la agencia autenticada
        app.post("/agencia/reservaciones/{id}/pago", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;

            // Extrae la agencia autenticada y el ID de la reservacion desde el path
            int agenciaId     = ctx.attribute("agenciaId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));

            PagoAgenciaRequestDTO request = ctx.bodyAsClass(PagoAgenciaRequestDTO.class);
            try {
                ctx.status(200).json(pagoAgenciaService.procesarPago(reservacionId, agenciaId, request));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            } catch (RuntimeException e) {
                ctx.status(500).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}