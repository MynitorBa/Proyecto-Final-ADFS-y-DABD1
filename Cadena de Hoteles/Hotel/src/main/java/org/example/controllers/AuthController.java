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
     * Ejemplo: COOKIE_NAME=auth_token_hotel1
     */
    private static final String COOKIE_NAME =
            System.getenv().getOrDefault("COOKIE_NAME", "auth_token");

    private final AuthService authService;

    /**
     * Crea una instancia de AuthController con sus dependencias inyectadas.
     *
     * @param authService servicio que contiene la logica de autenticacion.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registra las rutas de autenticacion en la aplicacion Javalin.
     *
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Autentica al usuario y emite una cookie de sesion si las credenciales son validas
        app.post("/auth/login", this::handleLogin);

        // Invalida la cookie de sesion sobreescribiendola con maxAge 0
        app.post("/auth/logout", this::handleLogout);
    }

    void handleLogin(Context ctx) {
        LoginRequestDTO request = ctx.bodyAsClass(LoginRequestDTO.class);
        try {
            AuthService.LoginResultado resultado = authService.login(request);

            // Configura la cookie con restricciones de seguridad para proteger el token.
            // El nombre dinamico (COOKIE_NAME) evita colisiones entre instancias del hotel.
            Cookie cookie = new Cookie(COOKIE_NAME, resultado.token());
            cookie.setHttpOnly(true);       // JS del cliente no puede leer la cookie
            cookie.setSecure(true);         // solo se envia por HTTPS (false en dev local)
            cookie.setSameSite(SameSite.STRICT); // protege contra CSRF
            cookie.setMaxAge(COOKIE_MAX_AGE);
            cookie.setPath("/");            // disponible en toda la aplicacion
            ctx.cookie(cookie);

            ctx.status(200).json(resultado.respuesta());

        } catch (CredencialesInvalidasException e) {
            ctx.status(401).json(Map.of("mensaje", "Usuario o contrasena incorrectos"));
        }
    }

    void handleLogout(Context ctx) {
        // Reemplaza la cookie existente con una vacia y expirada para forzar
        // su eliminacion en el navegador. Se usa el mismo nombre dinamico
        // para asegurarse de borrar la cookie correcta de esta instancia.
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setSameSite(SameSite.STRICT);
        cookie.setMaxAge(0);    // expira inmediatamente
        cookie.setPath("/");
        ctx.cookie(cookie);

        ctx.status(200).json(Map.of("mensaje", "Sesion cerrada"));
    }
}