package org.example.helpers;

import io.jsonwebtoken.Claims;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;
import java.util.Set;

public class AuthMiddleware {

    private static final Set<String> RUTAS_PUBLICAS = Set.of(
            "/",
            "/health",
            "/auth/login",
            "/auth/logout",
            "/usuarios/registrar",
            "/usuarios/validar",
            "/busqueda"
    );

    public static void registrar(Javalin app) {
        app.before(ctx -> {
            if (esRutaPublica(ctx)) return;

            String token = ctx.cookie("auth_token");

            if (token == null || token.isBlank()) {
                ctx.status(401).json(Map.of("mensaje", "No autenticado"));
                return;
            }

            if (!JwtHelper.esValido(token)) {
                ctx.status(401).json(Map.of("mensaje", "Sesión inválida o expirada"));
                return;
            }

            Claims claims = JwtHelper.verificarToken(token);
            ctx.attribute("usuarioId", JwtHelper.getUsuarioId(claims));
            ctx.attribute("username",  JwtHelper.getUsername(claims));
            ctx.attribute("rolId",     JwtHelper.getRolId(claims));
        });
    }

    private static boolean esRutaPublica(Context ctx) {
        return RUTAS_PUBLICAS.contains(ctx.path());
    }
}