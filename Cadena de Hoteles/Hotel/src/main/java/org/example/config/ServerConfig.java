package org.example.config;

import io.javalin.Javalin;
import java.util.Arrays;
import java.util.List;

/**
 * Configuracion del servidor Javalin.
 * Lee el puerto desde la variable de entorno PORT (default 7000)
 * y habilita CORS para el frontend en localhost:5173.
 * Acepta origenes adicionales definidos en CORS_EXTRA_ORIGINS (separados por coma).
 * Ej: CORS_EXTRA_ORIGINS=http://localhost:3001,http://localhost:3002,http://localhost:3003
 */
public class ServerConfig {

    /**
     * Crea y arranca el servidor Javalin con CORS configurado.
     * Permite credenciales desde el frontend propio y desde los frontends
     * de aerolineas aliadas definidos en CORS_EXTRA_ORIGINS.
     * @return instancia de Javalin ya iniciada en el puerto configurado.
     */
    public static Javalin createServer() {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "7000")
        );

        // Lee origenes CORS adicionales separados por coma
        // Ej: CORS_EXTRA_ORIGINS=http://localhost:3001,http://localhost:3002,http://localhost:3003
        String extraOriginsEnv = System.getenv().getOrDefault("CORS_EXTRA_ORIGINS", "");
        List<String> extraOrigins = extraOriginsEnv.isBlank()
                ? List.of()
                : Arrays.asList(extraOriginsEnv.split(","));

        //lee dinamicamente los peurtos del .env relacionados al frontend
        String dynamicFrontPort = System.getenv().getOrDefault("FRONTEND_PORT", "4001");
        String dynamicOrigin = "http://localhost:" + dynamicFrontPort;

        return Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.http.maxRequestSize = 10_485_760L; // 10 MB — soporta imagenes base64 de hasta ~7 MB reales
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.allowHost("http://localhost:5173"); // hotel frontend propio
                    it.allowHost("http://localhost:5174"); // hotel frontend dev

                    it.allowHost(dynamicOrigin); //puertos de frontend

                    for (String origin : extraOrigins) {
                        it.allowHost(origin.trim());       // frontends de aerolineas aliadas
                    }
                    it.allowCredentials = true;
                });
            });
        }).start(port);
    }
}