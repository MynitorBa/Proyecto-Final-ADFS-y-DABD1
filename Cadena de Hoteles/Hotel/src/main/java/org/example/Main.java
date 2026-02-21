package org.example;

import io.javalin.Javalin;
import org.example.config.ServerConfig;
import org.example.controllers.AuthController;
import org.example.controllers.UsuarioController;
import org.example.data.DatabaseTest;
import org.example.helpers.AuthMiddleware;

public class Main {

    public static void main(String[] args) {

        DatabaseTest.testConnection();
        Javalin app = ServerConfig.createServer();

        // ------------------------ Middleware --------------------------------------------
        AuthMiddleware.registrar(app);

        // ------------------------------- Controllers ------------------------------------
        new AuthController().registerRoutes(app);
        new UsuarioController().registerRoutes(app);

        // ------------------------------ Rutas base --------------------------------------
        app.get("/", ctx -> ctx.json("OK"));
        app.get("/health", ctx -> ctx.json("OK"));
    }
}