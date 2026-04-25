package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.ReservacionRequestDTO;
import org.example.services.ReservacionService;

import java.util.Map;

/**
 * Controller que gestiona las reservaciones de usuarios autenticados.
 * Requiere sesion activa; el usuarioId se obtiene del contexto inyectado por el middleware JWT.
 */
public class ReservacionController {

    private final ReservacionService reservacionService;

    /**
     * Crea una instancia de ReservacionController con sus dependencias inyectadas.
     */
    public ReservacionController(ReservacionService reservacionService) {
        this.reservacionService = reservacionService;
    }

    /**
     * Registra las rutas de reservaciones en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Crea una nueva reservacion en nombre del usuario autenticado
        app.post("/reservaciones", this::handleCrearReservacion);

        // Retorna todas las reservaciones registradas por el usuario autenticado
        app.get("/reservaciones", this::handleObtenerReservaciones);
    }

    void handleCrearReservacion(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        ReservacionRequestDTO request = ctx.bodyAsClass(ReservacionRequestDTO.class);
        try {
            var resultado = reservacionService.crearReservacion(request, usuarioId,
                    ctx.ip(), ctx.userAgent());
            ctx.status(201).json(resultado);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            ctx.status(500).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleObtenerReservaciones(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        ctx.status(200).json(reservacionService.obtenerReservaciones(usuarioId));
    }
}
