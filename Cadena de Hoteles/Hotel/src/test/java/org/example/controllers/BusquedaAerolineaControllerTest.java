package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.BusquedaRequestDTO;
import org.example.helpers.AerolineaAuthMiddleware;
import org.example.services.BusquedaAerolineaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BusquedaAerolineaController - Tests unitarios")
class BusquedaAerolineaControllerTest {

    @Mock private BusquedaAerolineaService service;
    @Mock private Context ctx;
    private BusquedaAerolineaController controller;

    @BeforeEach
    void setUp() {
        controller = new BusquedaAerolineaController(service);
    }

    // -------------------------------------------------------------------------
    // handleBuscar - autenticacion fallida
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleBuscar_authFalla_noInvocaServicio")
    void handleBuscar_authFalla_noInvocaServicio() {
        try (MockedStatic<AerolineaAuthMiddleware> mocked = mockStatic(AerolineaAuthMiddleware.class)) {
            mocked.when(() -> AerolineaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleBuscar(ctx);

            verify(service, never()).buscar(any(), any());
            verify(ctx, never()).status(anyInt());
        }
    }

    // -------------------------------------------------------------------------
    // handleBuscar - caso feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleBuscar_requestValido_retornaResultados200")
    void handleBuscar_requestValido_retornaResultados200() {
        try (MockedStatic<AerolineaAuthMiddleware> mocked = mockStatic(AerolineaAuthMiddleware.class)) {
            mocked.when(() -> AerolineaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String token = "aero-tok-456";
            BusquedaRequestDTO requestDTO = new BusquedaRequestDTO();
            List<Object> resultados = List.of("vuelo1", "vuelo2");

            when(ctx.header("X-Aerolinea-Token")).thenReturn(token);
            when(ctx.bodyAsClass(BusquedaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(200)).thenReturn(ctx);
            doReturn(resultados).when(service).buscar(requestDTO, token);

            controller.handleBuscar(ctx);

            verify(ctx).status(200);
            verify(ctx).json(resultados);
        }
    }

    // -------------------------------------------------------------------------
    // handleBuscar - IllegalArgumentException -> 400
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleBuscar_requestInvalido_retorna400ConMensaje")
    void handleBuscar_requestInvalido_retorna400ConMensaje() {
        try (MockedStatic<AerolineaAuthMiddleware> mocked = mockStatic(AerolineaAuthMiddleware.class)) {
            mocked.when(() -> AerolineaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String token = "aero-tok-bad";
            BusquedaRequestDTO requestDTO = new BusquedaRequestDTO();
            String errorMsg = "Destino no disponible";

            when(ctx.header("X-Aerolinea-Token")).thenReturn(token);
            when(ctx.bodyAsClass(BusquedaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(400)).thenReturn(ctx);
            when(service.buscar(requestDTO, token))
                    .thenThrow(new IllegalArgumentException(errorMsg));

            controller.handleBuscar(ctx);

            verify(ctx).status(400);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }
}
