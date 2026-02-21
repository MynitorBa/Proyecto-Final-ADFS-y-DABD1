package org.example;

import io.javalin.Javalin;
import org.example.config.ServerConfig;
import org.example.data.DatabaseTest;

public class Main {

    public static void main(String[] args) {

        DatabaseTest.testConnection();
        Javalin app = ServerConfig.createServer();

        app.get("/", ctx -> {
            ctx.json("Tengo un equipo bien gay");
        });

        app.get("/health", ctx -> {
            ctx.json("OK");
        });
    }
}