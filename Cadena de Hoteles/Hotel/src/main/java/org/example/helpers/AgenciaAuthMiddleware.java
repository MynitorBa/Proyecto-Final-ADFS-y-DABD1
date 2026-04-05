package org.example.helpers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.example.dtos.AgenciaIdentidad;
import org.example.repositories.AgenciaRepository;
import java.util.Map;

/**
 * Middleware de autenticacion para rutas protegidas de agencias externas.
 * Valida el token enviado en el header X-Agencia-Token e inyecta
 * la identidad de la agencia en el contexto de la peticion.
 */
public class AgenciaAuthMiddleware {

    private static final AgenciaRepository repo = new AgenciaRepository();

    /**
     * Verifica el token de agencia en el header de la peticion.
     * Si el token es valido, inyecta el ID, nombre y URL de la agencia
     * como atributos del contexto para que los controllers puedan usarlos.
     * Debe llamarse al inicio de cada ruta protegida de agencia.
     *
     * @param ctx contexto de la peticion HTTP de Javalin.
     * @return true si el token es valido y la agencia fue identificada;
     *         false si el token falta o no corresponde a ninguna agencia,
     *         en cuyo caso ya se escribe la respuesta 401 en el contexto.
     */
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