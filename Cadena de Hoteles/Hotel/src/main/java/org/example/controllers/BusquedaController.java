package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.BusquedaRequestDTO;
import org.example.services.BusquedaService;
import org.example.helpers.JwtHelper;
import io.jsonwebtoken.Claims;

import java.util.Map;

/**
 * Controller que expone el endpoint publico de busqueda de hoteles.
 * Si el usuario tiene sesion activa, asocia el usuarioId a la busqueda;
 * de lo contrario la procesa de forma anonima.
 */
public class BusquedaController {

    /**
     * Nombre de la cookie de sesion, igual al usado en AuthMiddleware.
     * Se lee de la variable de entorno COOKIE_NAME para que cada instancia
     * de hotel identifique correctamente su propia cookie.
     */
    private static final String COOKIE_NAME =
            System.getenv().getOrDefault("COOKIE_NAME", "auth_token");

    private final BusquedaService busquedaService;

    /**
     * Crea una instancia de BusquedaController con sus dependencias inyectadas.
     */
    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    /**
     * Registra la ruta de busqueda en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registra la ruta.
     */
    public void registerRoutes(Javalin app) {

        // Endpoint publico: acepta busquedas con o sin sesion iniciada
        app.post("/busqueda", this::handleBuscar);
    }

    void handleBuscar(Context ctx) {
        BusquedaRequestDTO request = ctx.bodyAsClass(BusquedaRequestDTO.class);

        // Intenta extraer el usuarioId del token si existe y es valido,
        // usando el mismo nombre de cookie que AuthMiddleware (COOKIE_NAME).
        Integer usuarioId = null;
        String token = ctx.cookie(COOKIE_NAME);
        if (token != null && !token.isBlank() && JwtHelper.esValido(token)) {
            Claims claims = JwtHelper.verificarToken(token);
            usuarioId = JwtHelper.getUsuarioId(claims);
        }

        try {
            // Si el caller pasa ?registrar=false, se obtienen los hoteles sin guardar
            // la busqueda en la tabla Busqueda (util para checks de disponibilidad internos).
            boolean registrar = !"false".equalsIgnoreCase(ctx.queryParam("registrar"));
            var resultado = busquedaService.buscar(request, usuarioId, registrar);
            ctx.status(200).json(resultado);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        }
    }
}