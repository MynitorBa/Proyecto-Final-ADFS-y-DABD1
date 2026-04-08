package org.example.config;

import io.javalin.Javalin;

/**
 * Configuracion del servidor Javalin.
 * Lee el puerto desde la variable de entorno PORT (default 7000)
 * y habilita CORS para el frontend en localhost:5173.
 */
public class ServerConfig {

    /**
     * Crea y arranca el servidor Javalin con CORS configurado.
     * Permite credenciales desde http://localhost:5173.
     * @return instancia de Javalin ya iniciada en el puerto configurado.
     */
    public static Javalin createServer() {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "7000")
        );

        return Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.allowHost("http://localhost:5174");
                    it.allowCredentials = true;
                });
            });
        }).start(port);
    }
}