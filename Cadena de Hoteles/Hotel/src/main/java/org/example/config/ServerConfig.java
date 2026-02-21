package org.example.config;

import io.javalin.Javalin;

public class ServerConfig {

    public static Javalin createServer() {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "7000")
        );

        return Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(port);
    }
}