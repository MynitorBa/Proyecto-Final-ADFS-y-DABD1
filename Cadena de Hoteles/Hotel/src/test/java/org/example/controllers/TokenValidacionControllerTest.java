package org.example.controllers;

import io.javalin.http.Context;
import org.example.services.TokenValidacionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TokenValidacionController - Tests unitarios")
class TokenValidacionControllerTest {

    @Mock private TokenValidacionService tokenValidacionService;
    @Mock private Context ctx;
    private TokenValidacionController controller;

    @BeforeEach
    void setUp() {
        controller = new TokenValidacionController(tokenValidacionService);
    }

    // ---- handleValidar - token null ----

    @Test
    @DisplayName("handleValidar_tokenNull_retorna400SinLlamarServicio")
    void handleValidar_tokenNull_retorna400SinLlamarServicio() {
        when(ctx.attribute("usuarioId")).thenReturn(3);
        when(ctx.queryParam("token")).thenReturn(null);
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleValidar(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(tokenValidacionService, never()).validar(any());
    }

    // ---- handleValidar - token en blanco ----

    @Test
    @DisplayName("handleValidar_tokenBlanco_retorna400SinLlamarServicio")
    void handleValidar_tokenBlanco_retorna400SinLlamarServicio() {
        when(ctx.attribute("usuarioId")).thenReturn(3);
        when(ctx.queryParam("token")).thenReturn("   ");
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleValidar(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(tokenValidacionService, never()).validar(any());
    }

    // ---- handleValidar - token valido ----

    @Test
    @DisplayName("handleValidar_tokenValido_retorna200ConResultado")
    void handleValidar_tokenValido_retorna200ConResultado() {
        when(ctx.attribute("usuarioId")).thenReturn(3);
        when(ctx.queryParam("token")).thenReturn("uuid-token-valido");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(null).when(tokenValidacionService).validar("uuid-token-valido");

        controller.handleValidar(ctx);

        verify(tokenValidacionService).validar("uuid-token-valido");
        verify(ctx).status(200);
        verify(ctx).json(any());
    }

    // ---- handleValidar - servicio lanza excepcion ----

    @Test
    @DisplayName("handleValidar_tokenExpirado_retorna400ConMensaje")
    void handleValidar_tokenExpirado_retorna400ConMensaje() {
        when(ctx.attribute("usuarioId")).thenReturn(3);
        when(ctx.queryParam("token")).thenReturn("uuid-expirado");
        when(ctx.status(400)).thenReturn(ctx);
        when(tokenValidacionService.validar("uuid-expirado"))
                .thenThrow(new IllegalArgumentException("Token expirado o ya utilizado"));

        controller.handleValidar(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(200);
    }

    @Test
    @DisplayName("handleValidar_tokenVacio_retorna400SinLlamarServicio")
    void handleValidar_tokenVacio_retorna400SinLlamarServicio() {
        when(ctx.attribute("usuarioId")).thenReturn(3);
        when(ctx.queryParam("token")).thenReturn("");
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleValidar(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(tokenValidacionService, never()).validar(any());
    }
}
