package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.services.HandshakeAerolineaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HandshakeAerolineaController - Tests unitarios")
class HandshakeAerolineaControllerTest {

    @Mock private HandshakeAerolineaService service;
    @Mock private Context ctx;
    @Mock private HandshakeRequestDTO requestDTO;
    @Mock private HandshakeResponseDTO responseDTO;
    private HandshakeAerolineaController controller;

    @BeforeEach
    void setUp() {
        controller = new HandshakeAerolineaController(service);
    }

    // ---- handleHandshake ----

    @Test
    @DisplayName("handleHandshake_datosValidos_retornaResponse")
    void handleHandshake_datosValidos_retornaResponse() {
        when(ctx.bodyAsClass(HandshakeRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getUrlAgencia()).thenReturn("https://aerolinea.com");
        when(requestDTO.getTokenEntrada()).thenReturn("tok-entrada-abc");
        when(service.procesarHandshake(requestDTO)).thenReturn(responseDTO);

        controller.handleHandshake(ctx);

        verify(service).procesarHandshake(requestDTO);
        verify(ctx).json(responseDTO);
        verify(ctx, never()).status(anyInt());
    }

    @Test
    @DisplayName("handleHandshake_aerolineaNoRegistrada_retorna400")
    void handleHandshake_aerolineaNoRegistrada_retorna400() {
        when(ctx.bodyAsClass(HandshakeRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getUrlAgencia()).thenReturn("https://desconocida.com");
        when(requestDTO.getTokenEntrada()).thenReturn("tok-invalido");
        when(ctx.status(400)).thenReturn(ctx);
        when(service.procesarHandshake(requestDTO))
                .thenThrow(new IllegalArgumentException("URL de aerolinea no registrada"));

        controller.handleHandshake(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).json(responseDTO);
    }

    @Test
    @DisplayName("handleHandshake_tokenEntradaIncorrecto_retorna400")
    void handleHandshake_tokenEntradaIncorrecto_retorna400() {
        when(ctx.bodyAsClass(HandshakeRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getUrlAgencia()).thenReturn("https://aerolinea.com");
        when(requestDTO.getTokenEntrada()).thenReturn("tok-malo");
        when(ctx.status(400)).thenReturn(ctx);
        when(service.procesarHandshake(requestDTO))
                .thenThrow(new IllegalArgumentException("Token de entrada invalido"));

        controller.handleHandshake(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleHandshake_servicioExitoso_noLlamaStatus")
    void handleHandshake_servicioExitoso_noLlamaStatus() {
        when(ctx.bodyAsClass(HandshakeRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getUrlAgencia()).thenReturn("https://aerolinea-ok.com");
        when(requestDTO.getTokenEntrada()).thenReturn("tok-ok");
        when(service.procesarHandshake(requestDTO)).thenReturn(responseDTO);

        controller.handleHandshake(ctx);

        verify(ctx, never()).status(anyInt());
        verify(ctx).json(responseDTO);
    }
}
