package org.example;

import io.javalin.Javalin;
import org.example.config.ServerConfig;
import org.example.controllers.AgenciaController;         // ← añadir
import org.example.controllers.AuthController;
import org.example.controllers.BusquedaController;
import org.example.controllers.CancelacionController;
import org.example.controllers.ComentarioController;
import org.example.controllers.DownsController;
import org.example.controllers.HotelController;
import org.example.controllers.ImagenController;
import org.example.controllers.PagoController;
import org.example.controllers.ReservacionController;
import org.example.controllers.SesionController;
import org.example.controllers.UsuarioController;
import org.example.data.DatabaseTest;
import org.example.helpers.AuthMiddleware;
import org.example.services.ExpiracionService;
import org.example.controllers.PdfReservacionController;

import java.util.Map;

public class Main {

    public static void main(String[] args) {

        DatabaseTest.testConnection();

        // Hilo de expiración
        ExpiracionService expiracionService = new ExpiracionService();
        expiracionService.iniciar();

        Javalin app = ServerConfig.createServer();

        // ── Manejador global de excepciones ───────────────────────────────────
        app.exception(Exception.class, (e, ctx) -> {
            String msg = e.getMessage() != null ? e.getMessage() : "Error interno del servidor";
            if (msg.contains("ORA-00001")) {
                msg = "Registro duplicado: ya existe un valor con esa restricción única.";
            } else if (msg.contains("ORA-")) {
                msg = msg.split("\n")[0].trim();
            }
            ctx.status(500).json(Map.of("mensaje", msg));
        });

        // ── Middleware ────────────────────────────────────────────────────────
        AuthMiddleware.registrar(app);

        // ── Controllers ───────────────────────────────────────────────────────
        new AuthController().registerRoutes(app);
        new SesionController().registerRoutes(app);
        new UsuarioController().registerRoutes(app);
        new BusquedaController().registerRoutes(app);
        new ReservacionController().registerRoutes(app);
        new PagoController().registerRoutes(app);
        new ComentarioController().registerRoutes(app);
        new DownsController().registerRoutes(app);
        new CancelacionController().registerRoutes(app);
        new ImagenController().registerRoutes(app);
        new HotelController().registerRoutes(app);         // ← añadir
        new AgenciaController().registerRoutes(app);       // ← añadir
        new PdfReservacionController().registerRoutes(app);

        // ── Rutas base ────────────────────────────────────────────────────────
        app.get("/", ctx -> ctx.json("OK"));
        app.get("/health", ctx -> ctx.json("OK"));

        // ── Apagar hilo al cerrar ─────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(expiracionService::detener));
    }
}