package org.example;

import io.javalin.Javalin;
import org.example.config.ServerConfig;
import org.example.controllers.AgenciaController;
import org.example.controllers.HotelAgenciaController;
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
import org.example.controllers.DestinosController;
import org.example.controllers.AdminBusquedaController;
import org.example.controllers.EmailReservacionController;
import org.example.controllers.CancelacionAgenciaController;
import org.example.controllers.BusquedaAgenciaController;
import org.example.controllers.ReservacionAgenciaController;
import org.example.controllers.PagoAgenciaController;

import java.util.Map;

/**
 * Punto de entrada de la aplicacion.
 * Arranca el servidor, registra middleware, controllers y el hilo de expiracion.
 */
public class Main {

    public static void main(String[] args) {

        // Verifica conexion a Oracle al iniciar
        DatabaseTest.testConnection();

        // Hilo de expiracion de reservaciones pendientes
        ExpiracionService expiracionService = new ExpiracionService();
        expiracionService.iniciar();

        Javalin app = ServerConfig.createServer();

        // Manejador global de excepciones
        // Filtra errores de Oracle para devolver mensajes legibles al cliente
        app.exception(Exception.class, (e, ctx) -> {
            String msg = e.getMessage() != null ? e.getMessage() : "Error interno del servidor";
            if (msg.contains("ORA-00001")) {
                msg = "Registro duplicado: ya existe un valor con esa restriccion unica.";
            } else if (msg.contains("ORA-")) {
                msg = msg.split("\n")[0].trim();
            }
            ctx.status(500).json(Map.of("mensaje", msg));
        });

        // Middleware de autenticacion JWT
        AuthMiddleware.registrar(app);

        // Controllers generales
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
        new HotelController().registerRoutes(app);
        new AgenciaController().registerRoutes(app);
        new PdfReservacionController().registerRoutes(app);
        new HotelAgenciaController().registrarRutas(app);

        // Controllers de agencias
        new CancelacionAgenciaController().registerRoutes(app);
        new ReservacionAgenciaController().registerRoutes(app);
        new BusquedaAgenciaController().registerRoutes(app);
        new PagoAgenciaController().registerRoutes(app);

        // Controllers adicionales
        new DestinosController().registerRoutes(app);
        new EmailReservacionController().registerRouteds(app);
        new AdminBusquedaController().registerRoutes(app);

        // Rutas base de salud del servidor
        app.get("/", ctx -> ctx.json("OK"));
        app.get("/health", ctx -> ctx.json("OK"));

        // Detiene el hilo de expiracion cuando la JVM se apaga
        Runtime.getRuntime().addShutdownHook(new Thread(expiracionService::detener));
    }
}