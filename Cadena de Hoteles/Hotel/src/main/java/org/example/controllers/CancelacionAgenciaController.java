package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.CancelacionRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.CancelacionService;

import java.util.Map;

/**
 * Controller que expone los endpoints de cancelacion de reservaciones para agencias externas.
 * Todas las rutas requieren autenticacion mediante el header X-Agencia-Token.
 */
public class CancelacionAgenciaController {

    private final CancelacionService cancelacionService;

    /**
     * Crea una instancia de CancelacionAgenciaController con sus dependencias inyectadas.
     */
    public CancelacionAgenciaController(CancelacionService cancelacionService) {
        this.cancelacionService = cancelacionService;
    }

    /**
     * Registra las rutas de cancelacion de agencias en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {
        // Consulta si una reservacion puede ser cancelada por la agencia autenticada
        app.get("/agencia/reservaciones/{id}/puede-cancelar", this::handlePuedeCancelar);

        // Ejecuta la cancelacion de una reservacion perteneciente a la agencia autenticada
        app.patch("/agencia/reservaciones/{id}/cancelar", this::handleCancelar);
    }

    void handlePuedeCancelar(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;

        // Extrae la agencia autenticada y el ID de la reservacion desde el path
        int agenciaId     = ctx.attribute("agenciaId");
        int reservacionId = Integer.parseInt(ctx.pathParam("id"));

        try {
            var resultado = cancelacionService.puedeCancelar(reservacionId, agenciaId);
            ctx.status(200).json(resultado);
        } catch (RuntimeException e) {
            ctx.status(500).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleCancelar(Context ctx) {
        if (!AgenciaAuthMiddleware.verificar(ctx)) return;

        // Extrae la agencia autenticada y el ID de la reservacion desde el path
        int agenciaId     = ctx.attribute("agenciaId");
        int reservacionId = Integer.parseInt(ctx.pathParam("id"));

        CancelacionRequestDTO request = ctx.bodyAsClass(CancelacionRequestDTO.class);
        try {
            cancelacionService.cancelarReservacionAgencia(
                    reservacionId, agenciaId, request.getMotivoCancelacion()
            );
            ctx.status(200).json(Map.of("mensaje", "Reservacion cancelada correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }
}
