package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.PagoAgenciaRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.PagoAgenciaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PagoAgenciaController - Tests unitarios")
class PagoAgenciaControllerTest {

    @Mock private PagoAgenciaService service;
    @Mock private Context ctx;
    private PagoAgenciaController controller;

    @BeforeEach
    void setUp() {
        controller = new PagoAgenciaController(service);
    }

    // -------------------------------------------------------------------------
    // handleProcesarPago - autenticacion fallida
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleProcesarPago_authFalla_noInvocaServicio")
    void handleProcesarPago_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleProcesarPago(ctx);

            verify(service, never()).procesarPago(anyInt(), anyInt(), any());
        }
    }

    // -------------------------------------------------------------------------
    // handleProcesarPago - caso feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleProcesarPago_pagoValido_retornaConfirmacion200")
    void handleProcesarPago_pagoValido_retornaConfirmacion200() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            PagoAgenciaRequestDTO requestDTO = mock(PagoAgenciaRequestDTO.class);

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("20");
            when(ctx.bodyAsClass(PagoAgenciaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(200)).thenReturn(ctx);
            doReturn(null).when(service).procesarPago(20, 5, requestDTO);

            controller.handleProcesarPago(ctx);

            verify(ctx).status(200);
            verify(ctx).json(any());
        }
    }

    // -------------------------------------------------------------------------
    // handleProcesarPago - IllegalArgumentException -> 400
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleProcesarPago_pagoInvalido_retorna400ConMensaje")
    void handleProcesarPago_pagoInvalido_retorna400ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "Metodo de pago no valido";
            PagoAgenciaRequestDTO requestDTO = mock(PagoAgenciaRequestDTO.class);

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("20");
            when(ctx.bodyAsClass(PagoAgenciaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(400)).thenReturn(ctx);
            when(service.procesarPago(20, 5, requestDTO))
                    .thenThrow(new IllegalArgumentException(errorMsg));

            controller.handleProcesarPago(ctx);

            verify(ctx).status(400);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }

    // -------------------------------------------------------------------------
    // handleProcesarPago - RuntimeException -> 500
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleProcesarPago_errorPasarela_retorna500ConMensaje")
    void handleProcesarPago_errorPasarela_retorna500ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "Error de comunicacion con la pasarela de pago";
            PagoAgenciaRequestDTO requestDTO = mock(PagoAgenciaRequestDTO.class);

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("20");
            when(ctx.bodyAsClass(PagoAgenciaRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(500)).thenReturn(ctx);
            when(service.procesarPago(20, 5, requestDTO))
                    .thenThrow(new RuntimeException(errorMsg));

            controller.handleProcesarPago(ctx);

            verify(ctx).status(500);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }
}
