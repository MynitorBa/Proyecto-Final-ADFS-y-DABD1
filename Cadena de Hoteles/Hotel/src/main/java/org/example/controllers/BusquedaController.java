package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.BusquedaRequestDTO;
import org.example.services.BusquedaService;
import org.example.helpers.JwtHelper;
import io.jsonwebtoken.Claims;

import java.util.Map;

/**
 * Controller que expone el endpoint publico de busqueda de vuelos.
 * Si el usuario tiene sesion activa, asocia el usuarioId a la busqueda;
 * de lo contrario la procesa de forma anonima.
 */
public class BusquedaController {

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
        app.post("/busqueda", ctx -> {
            BusquedaRequestDTO request = ctx.bodyAsClass(BusquedaRequestDTO.class);

            // Intenta extraer el usuarioId del token si existe y es valido, sin bloquear la peticion si no hay sesion
            Integer usuarioId = null;
            String token = ctx.cookie("auth_token");
            if (token != null && !token.isBlank() && JwtHelper.esValido(token)) {
                Claims claims = JwtHelper.verificarToken(token);
                usuarioId = JwtHelper.getUsuarioId(claims);
            }

            try {
                ctx.status(200).json(busquedaService.buscar(request, usuarioId));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });
    }
}