package org.example.helpers;

import io.jsonwebtoken.Claims;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;
import java.util.Set;

/**
 * Middleware de autenticacion global para la aplicacion Javalin.
 * Intercepta todas las peticiones entrantes y valida el token JWT
 * antes de permitir el acceso a rutas protegidas.
 */
public class AuthMiddleware {

    /**
     * Conjunto de rutas que no requieren autenticacion.
     * Cualquier path fuera de esta lista sera interceptado por el middleware.
     */
    private static final Set<String> RUTAS_PUBLICAS = Set.of(
            "/",
            "/health",
            "/auth/login",
            "/auth/logout",
            "/usuarios/registrar",
            "/usuarios/validar",
            "/busqueda",
            "/sesion",
            "/api/agencias/handshake",
            "/api/hoteles-agencia",
            "/agencia/busqueda",
            "/agencia/reservaciones"
    );

    /**
     * Registra el middleware de autenticacion en la instancia de Javalin.
     * Se ejecuta antes de cada peticion. Si la ruta es publica o pertenece
     * al prefijo /agencia/, la deja pasar sin validar. De lo contrario,
     * exige un cookie auth_token valido y extrae los claims del usuario
     * para inyectarlos en el contexto.
     *
     * @param app instancia de Javalin donde se registra el middleware.
     */
    public static void registrar(Javalin app) {
        app.before(ctx -> {
            if (esRutaPublica(ctx)) return;

            // Las rutas de agencia usan su propio middleware de token
            if (ctx.path().startsWith("/agencia/")) return;

            String token = ctx.cookie("auth_token");

            if (token == null || token.isBlank()) {
                ctx.status(401).json(Map.of("mensaje", "No autenticado"));
                return;
            }

            if (!JwtHelper.esValido(token)) {
                ctx.status(401).json(Map.of("mensaje", "Sesion invalida o expirada"));
                return;
            }

            // Extraer claims y dejarlos disponibles para los controllers
            Claims claims = JwtHelper.verificarToken(token);
            ctx.attribute("usuarioId", JwtHelper.getUsuarioId(claims));
            ctx.attribute("username",  JwtHelper.getUsername(claims));
            ctx.attribute("rolId",     JwtHelper.getRolId(claims));
        });
    }

    /**
     * Determina si una peticion corresponde a una ruta publica.
     * Ademas de las rutas exactas del set, los GET de comentarios por hotel
     * e imagenes siempre se consideran publicos.
     *
     * @param ctx contexto de la peticion HTTP de Javalin.
     * @return true si la ruta no requiere autenticacion; false en caso contrario.
     */
    private static boolean esRutaPublica(Context ctx) {
        // Rutas exactas registradas como publicas
        if (RUTAS_PUBLICAS.contains(ctx.path())) return true;
        // GET de comentarios por hotel es publica
        if (ctx.method().name().equals("GET") && ctx.path().startsWith("/comentarios/hotel/")) return true;
        // GET de imagenes siempre es publica
        if (ctx.method().name().equals("GET") && ctx.path().startsWith("/imagenes/")) return true;
        return false;
    }
}