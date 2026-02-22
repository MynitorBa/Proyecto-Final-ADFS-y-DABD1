package org.example.config;

import io.javalin.Javalin;

public class ServerConfig {

    public static Javalin createServer() {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "7000")
        );

        return Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.allowHost("http://localhost:5173");
                    it.allowCredentials = true;
                });
            });
        }).start(port);
    }
}