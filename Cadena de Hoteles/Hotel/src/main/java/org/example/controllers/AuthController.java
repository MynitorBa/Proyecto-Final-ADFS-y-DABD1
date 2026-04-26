package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import org.example.dtos.LoginRequestDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.services.AuthService;

import java.util.Map;

/**
 * Controller que maneja la autenticacion de usuarios.
 * Expone endpoints para login y logout usando cookies HttpOnly como mecanismo de sesion.
 *
 * El nombre de la cookie se lee de la variable de entorno COOKIE_NAME para permitir
 * que multiples instancias del servidor corran en paralelo sin que sus cookies
 * se sobrepongan en el navegador del cliente.
 */
public class AuthController {

    /** Duracion de la cookie de sesion: 8 horas expresadas en segundos. */
    private static final int COOKIE_MAX_AGE = 8 * 60 * 60;

    /**
     * Nombre de la cookie de sesion leido desde la variable de entorno COOKIE_NAME.
     * Si la variable no esta definida, se usa "auth_token" como valor por defecto.
     */
    private static final String COOKIE_NAME =
            System.getenv().getOrDefault("COOKIE_NAME", "auth_token");

    private final AuthService authService;

    /**
     * Crea una instancia de AuthController con sus dependencias inyectadas.
     * @param authService servicio que contiene la logica de autenticacion.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registra las rutas de autenticacion en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {
        app.post("/auth/login",  this::handleLogin);
        app.post("/auth/logout", this::handleLogout);
    }

    void handleLogin(Context ctx) {
        LoginRequestDTO request = ctx.bodyAsClass(LoginRequestDTO.class);

        // Extrae IP y UserAgent del contexto para pasarlos al log
        String ip        = ctx.ip();
        String userAgent = ctx.userAgent();

        try {
            AuthService.LoginResultado resultado = authService.login(request, ip, userAgent);

            Cookie cookie = new Cookie(COOKIE_NAME, resultado.token());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setSameSite(SameSite.NONE);
            cookie.setMaxAge(COOKIE_MAX_AGE);
            cookie.setPath("/");
            ctx.cookie(cookie);

            ctx.status(200).json(resultado.respuesta());

        } catch (CredencialesInvalidasException e) {
            ctx.status(401).json(Map.of("mensaje", "Usuario o contrasena incorrectos"));
        } catch (RuntimeException e) {
            ctx.status(500).json(Map.of("mensaje", "Error interno, intenta de nuevo"));
        }
    }

    void handleLogout(Context ctx) {
        // Leer el token ANTES de borrar la cookie
        String token     = ctx.cookie(COOKIE_NAME);
        String ip        = ctx.ip();
        String userAgent = ctx.userAgent();

        authService.logout(token, ip, userAgent);

        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setSameSite(SameSite.STRICT);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        ctx.cookie(cookie);

        ctx.status(200).json(Map.of("mensaje", "Sesion cerrada"));
    }
}