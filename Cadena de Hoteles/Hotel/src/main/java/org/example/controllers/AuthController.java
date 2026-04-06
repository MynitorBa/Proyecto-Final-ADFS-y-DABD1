package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import org.example.dtos.LoginRequestDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.services.AuthService;

import java.util.Map;

/**
 * Controller que maneja la autenticacion de usuarios.
 * Expone endpoints para login y logout usando cookies HttpOnly como mecanismo de sesion.
 */
public class AuthController {

    // Duracion de la cookie de sesion: 8 horas expresadas en segundos
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;

    private final AuthService authService;

    /**
     * Crea una instancia de AuthController con sus dependencias inyectadas.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registra las rutas de autenticacion en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Autentica al usuario y emite una cookie de sesion si las credenciales son validas
        app.post("/auth/login", ctx -> {
            LoginRequestDTO request = ctx.bodyAsClass(LoginRequestDTO.class);
            try {
                AuthService.LoginResultado resultado = authService.login(request);

                // Configura la cookie con restricciones de seguridad para proteger el token
                Cookie cookie = new Cookie("auth_token", resultado.token());
                cookie.setHttpOnly(true);   // JS no puede leerla
                cookie.setSecure(true);     // solo HTTPS — ponla en false si estas en desarrollo local
                cookie.setSameSite(SameSite.STRICT);
                cookie.setMaxAge(COOKIE_MAX_AGE);
                cookie.setPath("/");
                ctx.cookie(cookie);

                ctx.status(200).json(resultado.respuesta());

            } catch (CredencialesInvalidasException e) {
                ctx.status(401).json(Map.of("mensaje", "Usuario o contrasena incorrectos"));
            }
        });

        // Invalida la cookie de sesion sobreescribiendola con maxAge 0
        app.post("/auth/logout", ctx -> {
            // Reemplaza la cookie existente con una vacia y expirada para forzar su eliminacion en el cliente
            Cookie cookie = new Cookie("auth_token", "");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setSameSite(SameSite.STRICT);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            ctx.cookie(cookie);

            ctx.status(200).json(Map.of("mensaje", "Sesion cerrada"));
        });
    }
}