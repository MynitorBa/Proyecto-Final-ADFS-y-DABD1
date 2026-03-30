package org.example.helpers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.example.dtos.AgenciaIdentidad;
import org.example.repositories.AgenciaRepository;

import java.util.Map;

public class AgenciaAuthMiddleware {

    private static final AgenciaRepository repo = new AgenciaRepository();

    // Llama a este método dentro de cada ruta protegida de agencia
    public static boolean verificar(Context ctx) {
        String token = ctx.header("X-Agencia-Token");

        if (token == null || token.isBlank()) {
            ctx.status(401).json(Map.of("mensaje", "Token de agencia requerido"));
            return false;
        }

        AgenciaIdentidad agencia = repo.obtenerAgenciaPorToken(token);
        if (agencia == null) {
            ctx.status(401).json(Map.of("mensaje", "Token inválido — agencia no reconocida"));
            return false;
        }

        // Inyectar identidad en el contexto, igual que el AuthMiddleware de sesiones
        ctx.attribute("agenciaId",     agencia.getId());
        ctx.attribute("agenciaNombre", agencia.getNombre());
        ctx.attribute("agenciaUrl",    agencia.getUrlAgencia());
        return true;
    }
}