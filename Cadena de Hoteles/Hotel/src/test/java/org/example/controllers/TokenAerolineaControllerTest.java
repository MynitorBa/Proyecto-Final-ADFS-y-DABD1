package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.TokenAerolineaRequestDTO;
import org.example.helpers.AerolineaAuthMiddleware;
import org.example.services.TokenAerolineaService;
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
@DisplayName("TokenAerolineaController - Tests unitarios")
class TokenAerolineaControllerTest {

    @Mock private TokenAerolineaService tokenService;
    @Mock private Context ctx;
    @Mock private TokenAerolineaRequestDTO requestDTO;
    private TokenAerolineaController controller;

    @BeforeEach
    void setUp() {
        controller = new TokenAerolineaController(tokenService);
    }

    // ---- handleGenerarToken ----

    @Test
    @DisplayName("handleGenerarToken_authFalla_noInvocaServicio")
    void handleGenerarToken_authFalla_noInvocaServicio() {
        try (MockedStatic<AerolineaAuthMiddleware> mocked = mockStatic(AerolineaAuthMiddleware.class)) {
            mocked.when(() -> AerolineaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleGenerarToken(ctx);

            verify(tokenService, never()).generarToken(any(), any());
            verify(ctx, never()).status(anyInt());
        }
    }

    @Test
    @DisplayName("handleGenerarToken_authOkYDatosValidos_retorna201ConToken")
    void handleGenerarToken_authOkYDatosValidos_retorna201ConToken() {
        try (MockedStatic<AerolineaAuthMiddleware> mocked = mockStatic(AerolineaAuthMiddleware.class)) {
            mocked.when(() -> AerolineaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String tokenHash = "hash-aerolinea-xyz";

            when(ctx.header("X-Aerolinea-Token")).thenReturn(tokenHash);
            when(ctx.bodyAsClass(TokenAerolineaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(201)).thenReturn(ctx);
            doReturn(null).when(tokenService).generarToken(requestDTO, tokenHash);

            controller.handleGenerarToken(ctx);

            verify(ctx).status(201);
            verify(ctx).json(any());
        }
    }

    @Test
    @DisplayName("handleGenerarToken_servicioLanzaExcepcion_retorna400")
    void handleGenerarToken_servicioLanzaExcepcion_retorna400() {
        try (MockedStatic<AerolineaAuthMiddleware> mocked = mockStatic(AerolineaAuthMiddleware.class)) {
            mocked.when(() -> AerolineaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String tokenHash = "hash-invalido";
            when(ctx.header("X-Aerolinea-Token")).thenReturn(tokenHash);
            when(ctx.bodyAsClass(TokenAerolineaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(400)).thenReturn(ctx);
            when(tokenService.generarToken(requestDTO, tokenHash))
                    .thenThrow(new IllegalArgumentException("Aerolinea no registrada"));

            controller.handleGenerarToken(ctx);

            verify(ctx).status(400);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
            ));
            verify(ctx, never()).status(201);
        }
    }

    @Test
    @DisplayName("handleGenerarToken_authOkYTokenHashNulo_servicioLlamadoConNulo")
    void handleGenerarToken_authOkYTokenHashNulo_servicioLlamadoConNulo() {
        try (MockedStatic<AerolineaAuthMiddleware> mocked = mockStatic(AerolineaAuthMiddleware.class)) {
            mocked.when(() -> AerolineaAuthMiddleware.verificar(ctx)).thenReturn(true);

            when(ctx.header("X-Aerolinea-Token")).thenReturn(null);
            when(ctx.bodyAsClass(TokenAerolineaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(400)).thenReturn(ctx);
            when(tokenService.generarToken(requestDTO, null))
                    .thenThrow(new IllegalArgumentException("Token de aerolinea requerido"));

            controller.handleGenerarToken(ctx);

            verify(tokenService).generarToken(requestDTO, null);
            verify(ctx).status(400);
        }
    }
}
