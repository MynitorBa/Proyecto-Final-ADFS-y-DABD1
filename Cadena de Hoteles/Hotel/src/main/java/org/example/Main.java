package org.example;

import io.javalin.Javalin;
import org.example.config.ServerConfig;
import org.example.controllers.UsuarioController;
import org.example.data.DatabaseTest;

public class Main {

    public static void main(String[] args) {

        DatabaseTest.testConnection();
        Javalin app = ServerConfig.createServer();

        // Controllers
        new UsuarioController().registerRoutes(app);

        app.get("/", ctx -> ctx.json("OK"));
        app.get("/health", ctx -> ctx.json("OK"));
    }
}