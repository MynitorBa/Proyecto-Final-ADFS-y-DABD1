package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.services.HandshakeAerolineaService;

import java.util.Map;

/**
 * Controller que expone el endpoint publico de handshake para aerolineas aliadas.
 * Permite a una aerolinea externa autenticarse ante el sistema hotelero presentando
 * su URL y un token de entrada, y recibir un token de sesion para sus comunicaciones posteriores.
 * El endpoint no requiere autenticacion previa ya que es el primer punto de contacto.
 */
public class HandshakeAerolineaController {

    private final HandshakeAerolineaService service;

    /**
     * Crea una instancia del controller con el servicio de handshake de aerolineas.
     *
     * @param service instancia de HandshakeAerolineaService que contiene la logica del handshake.
     */
    public HandshakeAerolineaController(HandshakeAerolineaService service) {
        this.service = service;
    }

    /**
     * Registra las rutas publicas del handshake de aerolineas en el servidor Javalin.
     *
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // POST /api/aerolineas/handshake
        // Recibe { token_entrada, url_agencia } de la aerolinea y retorna { token_salida }
        // si la URL esta registrada en la tabla AerolineaAliado de la base de datos
        app.post("/api/aerolineas/handshake", this::handleHandshake);
    }

    void handleHandshake(Context ctx) {
        HandshakeRequestDTO dto = ctx.bodyAsClass(HandshakeRequestDTO.class);

        // Log de depuracion — confirma que los datos llegan correctamente al sistema hotelero
        System.out.println("[HANDSHAKE AEROLINEA] url_aerolinea recibida: '" + dto.getUrlAgencia() + "'");
        System.out.println("[HANDSHAKE AEROLINEA] token_entrada recibido: '" + dto.getTokenEntrada() + "'");

        try {
            HandshakeResponseDTO response = service.procesarHandshake(dto);
            System.out.println("[HANDSHAKE AEROLINEA] Handshake exitoso. Token generado y guardado.");
            ctx.json(response);
        } catch (IllegalArgumentException ex) {
            System.out.println("[HANDSHAKE AEROLINEA ERROR] " + ex.getMessage());
            ctx.status(400).json(Map.of("mensaje", ex.getMessage()));
        }
    }
}
