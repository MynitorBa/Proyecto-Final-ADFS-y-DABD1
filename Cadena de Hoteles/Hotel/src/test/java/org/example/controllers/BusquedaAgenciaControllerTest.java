package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.BusquedaRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.BusquedaAgenciaService;
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
@DisplayName("BusquedaAgenciaController - Tests unitarios")
class BusquedaAgenciaControllerTest {

    @Mock private BusquedaAgenciaService service;
    @Mock private Context ctx;
    private BusquedaAgenciaController controller;

    @BeforeEach
    void setUp() {
        controller = new BusquedaAgenciaController(service);
    }

    // -------------------------------------------------------------------------
    // handleBuscar - autenticacion fallida
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleBuscar_authFalla_noInvocaServicio")
    void handleBuscar_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleBuscar(ctx);

            verify(service, never()).buscarPorToken(any(), any());
            verify(ctx, never()).status(anyInt());
        }
    }

    // -------------------------------------------------------------------------
    // handleBuscar - caso feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleBuscar_requestValido_retornaResultados200")
    void handleBuscar_requestValido_retornaResultados200() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String token = "tok-123";
            BusquedaRequestDTO requestDTO = new BusquedaRequestDTO();
            List<Object> resultados = List.of("hotel1", "hotel2");

            when(ctx.header("X-Agencia-Token")).thenReturn(token);
            when(ctx.bodyAsClass(BusquedaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(200)).thenReturn(ctx);
            doReturn(resultados).when(service).buscarPorToken(requestDTO, token);

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
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String token = "tok-bad";
            BusquedaRequestDTO requestDTO = new BusquedaRequestDTO();
            String errorMsg = "Parametros de busqueda invalidos";

            when(ctx.header("X-Agencia-Token")).thenReturn(token);
            when(ctx.bodyAsClass(BusquedaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(400)).thenReturn(ctx);
            when(service.buscarPorToken(requestDTO, token))
                    .thenThrow(new IllegalArgumentException(errorMsg));

            controller.handleBuscar(ctx);

            verify(ctx).status(400);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }
}
