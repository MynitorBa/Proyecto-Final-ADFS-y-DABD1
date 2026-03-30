package org.example.controllers;

import io.javalin.Javalin;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.HotelAgenciaService;

public class HotelAgenciaController {

    private final HotelAgenciaService service = new HotelAgenciaService();

    public void registrarRutas(Javalin app) {

        // GET /api/hoteles-agencia
        // Protegido: requiere header X-Agencia-Token válido
        app.get("/api/hoteles-agencia", ctx -> {
            if (!AgenciaAuthMiddleware.verificar(ctx)) return;

            var hoteles = service.obtenerHotelesParaAgencia();
            ctx.json(hoteles);
        });
    }
}