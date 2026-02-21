package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import org.example.dtos.LoginRequestDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.services.AuthService;

import java.util.Map;

public class AuthController {

    private static final int COOKIE_MAX_AGE = 8 * 60 * 60; // 8 horas en segundos

    private final AuthService authService = new AuthService();

    public void registerRoutes(Javalin app) {

        // POST /auth/login
        app.post("/auth/login", ctx -> {
            LoginRequestDTO request = ctx.bodyAsClass(LoginRequestDTO.class);
            try {
                AuthService.LoginResultado resultado = authService.login(request);

                // Setear el token en una HttpOnly Cookie
                Cookie cookie = new Cookie("auth_token", resultado.token());
                cookie.setHttpOnly(true);   // JS no puede leerla
                cookie.setSecure(true);     // solo HTTPS — ponla en false si estás en desarrollo local
                cookie.setSameSite(SameSite.STRICT);
                cookie.setMaxAge(COOKIE_MAX_AGE);
                cookie.setPath("/");
                ctx.cookie(cookie);

                ctx.status(200).json(resultado.respuesta());

            } catch (CredencialesInvalidasException e) {
                ctx.status(401).json(Map.of("mensaje", "Usuario o contraseña incorrectos"));
            }
        });

        // POST /auth/logout
        app.post("/auth/logout", ctx -> {
            // Sobreescribir la cookie con maxAge 0 para eliminarla
            Cookie cookie = new Cookie("auth_token", "");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setSameSite(SameSite.STRICT);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            ctx.cookie(cookie);

            ctx.status(200).json(Map.of("mensaje", "Sesión cerrada"));
        });
    }
}