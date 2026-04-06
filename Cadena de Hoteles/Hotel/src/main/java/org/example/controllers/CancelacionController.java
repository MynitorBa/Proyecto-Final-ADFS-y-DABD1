package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.CancelacionRequestDTO;
import org.example.services.CancelacionService;

import java.util.Map;

/**
 * Controller que expone el endpoint de cancelacion de reservaciones para usuarios autenticados.
 * Requiere sesion activa; el usuarioId se obtiene del contexto inyectado por el middleware JWT.
 */
public class CancelacionController {

    private final CancelacionService cancelacionService;

    /**
     * Crea una instancia de CancelacionController con sus dependencias inyectadas.
     */
    public CancelacionController(CancelacionService cancelacionService) {
        this.cancelacionService = cancelacionService;
    }

    /**
     * Registra la ruta de cancelacion en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // Cancela una reservacion validando que pertenezca al usuario autenticado
        app.patch("/reservaciones/{id}/cancelar", ctx -> {

            // Extrae el usuario de la sesion y el ID de la reservacion desde el path
            int usuarioId     = ctx.attribute("usuarioId");
            int reservacionId = Integer.parseInt(ctx.pathParam("id"));
            CancelacionRequestDTO request = ctx.bodyAsClass(CancelacionRequestDTO.class);
            try {
                cancelacionService.cancelarReservacion(
                        reservacionId, usuarioId, request.getMotivoCancelacion()
                );
                ctx.status(200).json(Map.of("mensaje", "Reservacion cancelada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}