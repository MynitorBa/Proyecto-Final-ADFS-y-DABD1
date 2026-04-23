package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.CancelacionRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.CancelacionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelacionAgenciaController - Tests unitarios")
class CancelacionAgenciaControllerTest {

    @Mock private CancelacionService service;
    @Mock private Context ctx;
    private CancelacionAgenciaController controller;

    @BeforeEach
    void setUp() {
        controller = new CancelacionAgenciaController(service);
    }

    // =========================================================================
    // handlePuedeCancelar
    // =========================================================================

    @Test
    @DisplayName("handlePuedeCancelar_authFalla_noInvocaServicio")
    void handlePuedeCancelar_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handlePuedeCancelar(ctx);

            verify(service, never()).puedeCancelar(anyInt(), anyInt());
        }
    }

    @Test
    @DisplayName("handlePuedeCancelar_reservacionValida_retornaResultado200")
    void handlePuedeCancelar_reservacionValida_retornaResultado200() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("10");
            when(ctx.status(200)).thenReturn(ctx);
            doReturn(null).when(service).puedeCancelar(10, 5);

            controller.handlePuedeCancelar(ctx);

            verify(ctx).status(200);
            verify(ctx).json(any());
        }
    }

    @Test
    @DisplayName("handlePuedeCancelar_errorServicio_retorna500ConMensaje")
    void handlePuedeCancelar_errorServicio_retorna500ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "Error interno al verificar cancelacion";
            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("10");
            when(ctx.status(500)).thenReturn(ctx);
            when(service.puedeCancelar(10, 5))
                    .thenThrow(new RuntimeException(errorMsg));

            controller.handlePuedeCancelar(ctx);

            verify(ctx).status(500);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }

    // =========================================================================
    // handleCancelar
    // =========================================================================

    @Test
    @DisplayName("handleCancelar_authFalla_noInvocaServicio")
    void handleCancelar_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleCancelar(ctx);

            verify(service, never()).cancelarReservacionAgencia(anyInt(), anyInt(), any());
        }
    }

    @Test
    @DisplayName("handleCancelar_motivoValido_cancelaYRetorna200")
    void handleCancelar_motivoValido_cancelaYRetorna200() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            CancelacionRequestDTO requestDTO = mock(CancelacionRequestDTO.class);
            when(requestDTO.getMotivoCancelacion()).thenReturn("Cambio de planes");

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("10");
            when(ctx.bodyAsClass(CancelacionRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(200)).thenReturn(ctx);

            controller.handleCancelar(ctx);

            verify(service).cancelarReservacionAgencia(10, 5, "Cambio de planes");
            verify(ctx).status(200);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
            ));
        }
    }

    @Test
    @DisplayName("handleCancelar_motivoInvalido_retorna400ConMensaje")
    void handleCancelar_motivoInvalido_retorna400ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "El motivo de cancelacion es requerido";
            CancelacionRequestDTO requestDTO = mock(CancelacionRequestDTO.class);
            when(requestDTO.getMotivoCancelacion()).thenReturn("");

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("10");
            when(ctx.bodyAsClass(CancelacionRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(400)).thenReturn(ctx);
            doThrow(new IllegalArgumentException(errorMsg))
                    .when(service).cancelarReservacionAgencia(10, 5, "");

            controller.handleCancelar(ctx);

            verify(ctx).status(400);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }
}
