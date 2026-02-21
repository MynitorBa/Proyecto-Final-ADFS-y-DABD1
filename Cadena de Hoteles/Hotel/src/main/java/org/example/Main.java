package org.example;

import io.javalin.Javalin;
import org.example.config.ServerConfig;
import org.example.controllers.AuthController;
import org.example.controllers.BusquedaController;
import org.example.controllers.PagoController;
import org.example.controllers.ReservacionController;
import org.example.controllers.SesionController;
import org.example.controllers.UsuarioController;
import org.example.data.DatabaseTest;
import org.example.helpers.AuthMiddleware;
import org.example.services.ExpiracionService;

public class Main {

    public static void main(String[] args) {

        DatabaseTest.testConnection();

        //Hilo de expiración
        ExpiracionService expiracionService = new ExpiracionService();
        expiracionService.iniciar();

        Javalin app = ServerConfig.createServer();

        //Middleware
        AuthMiddleware.registrar(app);

        //Controllers
        new AuthController().registerRoutes(app);
        new SesionController().registerRoutes(app);
        new UsuarioController().registerRoutes(app);
        new BusquedaController().registerRoutes(app);
        new ReservacionController().registerRoutes(app);
        new PagoController().registerRoutes(app);

        //Rutas base
        app.get("/", ctx -> ctx.json("OK"));
        app.get("/health", ctx -> ctx.json("OK"));

        //Apagar hilo al cerrar
        Runtime.getRuntime().addShutdownHook(new Thread(expiracionService::detener));
    }
}