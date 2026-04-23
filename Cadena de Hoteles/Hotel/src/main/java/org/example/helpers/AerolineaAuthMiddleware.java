package org.example.helpers;

import io.javalin.http.Context;
import org.example.dtos.AerolineaIdentidadDTO;
import org.example.repositories.AerolineaAliadaRepository;

import java.util.Map;

/**
 * Middleware de autenticacion para rutas protegidas de aerolineas aliadas.
 * Valida el token enviado en el header X-Aerolinea-Token e inyecta
 * la identidad de la aerolinea en el contexto de la peticion.
 */
public class AerolineaAuthMiddleware {

    /** Repositorio de aerolineas. Package-private para permitir sustitucion en tests unitarios. */
    static AerolineaAliadaRepository repo = new AerolineaAliadaRepository();

    /**
     * Verifica el token de aerolinea en el header de la peticion.
     * Si el token es valido, inyecta el ID, nombre y URL de la aerolinea
     * como atributos del contexto para que los controllers puedan usarlos.
     *
     * @param ctx contexto de la peticion HTTP de Javalin.
     * @return true si el token es valido; false si falta o no corresponde
     *         a ninguna aerolinea activa, escribiendo ya la respuesta 401.
     */
    public static boolean verificar(Context ctx) {
        String token = ctx.header("X-Aerolinea-Token");

        if (token == null || token.isBlank()) {
            ctx.status(401).json(Map.of("mensaje", "Token de aerolinea requerido"));
            return false;
        }

        AerolineaIdentidadDTO aerolinea = repo.obtenerAerolineaPorToken(token);
        if (aerolinea == null) {
            ctx.status(401).json(Map.of("mensaje", "Token invalido — aerolinea no reconocida"));
            return false;
        }

        ctx.attribute("aerolineaId",     aerolinea.getId());
        ctx.attribute("aerolineaNombre", aerolinea.getNombre());
        ctx.attribute("aerolineaUrl",    aerolinea.getUrlAerolinea());
        return true;
    }
}