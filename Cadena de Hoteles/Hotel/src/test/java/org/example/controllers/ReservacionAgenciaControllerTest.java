package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.ReservacionRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.ReservacionAgenciaService;
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
@DisplayName("ReservacionAgenciaController - Tests unitarios")
class ReservacionAgenciaControllerTest {

    @Mock private ReservacionAgenciaService service;
    @Mock private Context ctx;
    private ReservacionAgenciaController controller;

    @BeforeEach
    void setUp() {
        controller = new ReservacionAgenciaController(service);
    }

    // =========================================================================
    // handleCrearReservacion
    // =========================================================================

    @Test
    @DisplayName("handleCrearReservacion_authFalla_noInvocaServicio")
    void handleCrearReservacion_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleCrearReservacion(ctx);

            verify(service, never()).crearReservacion(any(), anyInt());
        }
    }

    @Test
    @DisplayName("handleCrearReservacion_requestValido_retornaReservacion201")
    void handleCrearReservacion_requestValido_retornaReservacion201() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            ReservacionRequestDTO requestDTO = mock(ReservacionRequestDTO.class);

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.bodyAsClass(ReservacionRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(201)).thenReturn(ctx);
            doReturn(null).when(service).crearReservacion(requestDTO, 5);

            controller.handleCrearReservacion(ctx);

            verify(ctx).status(201);
            verify(ctx).json(any());
        }
    }

    @Test
    @DisplayName("handleCrearReservacion_datosInvalidos_retorna400ConMensaje")
    void handleCrearReservacion_datosInvalidos_retorna400ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "Fechas de reservacion invalidas";
            ReservacionRequestDTO requestDTO = mock(ReservacionRequestDTO.class);

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.bodyAsClass(ReservacionRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(400)).thenReturn(ctx);
            when(service.crearReservacion(requestDTO, 5))
                    .thenThrow(new IllegalArgumentException(errorMsg));

            controller.handleCrearReservacion(ctx);

            verify(ctx).status(400);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }

    @Test
    @DisplayName("handleCrearReservacion_errorInterno_retorna500ConMensaje")
    void handleCrearReservacion_errorInterno_retorna500ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "Error de base de datos";
            ReservacionRequestDTO requestDTO = mock(ReservacionRequestDTO.class);

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.bodyAsClass(ReservacionRequestDTO.class)).thenReturn(requestDTO);
            when(ctx.status(500)).thenReturn(ctx);
            when(service.crearReservacion(requestDTO, 5))
                    .thenThrow(new RuntimeException(errorMsg));

            controller.handleCrearReservacion(ctx);

            verify(ctx).status(500);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }

    // =========================================================================
    // handleObtenerReservaciones
    // =========================================================================

    @Test
    @DisplayName("handleObtenerReservaciones_authFalla_noInvocaServicio")
    void handleObtenerReservaciones_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleObtenerReservaciones(ctx);

            verify(service, never()).obtenerReservaciones(anyInt());
        }
    }

    @Test
    @DisplayName("handleObtenerReservaciones_agenciaValida_retornaLista200")
    void handleObtenerReservaciones_agenciaValida_retornaLista200() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            List<Object> reservaciones = List.of(Map.of("id", 1), Map.of("id", 2));

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.status(200)).thenReturn(ctx);
            doReturn(reservaciones).when(service).obtenerReservaciones(5);

            controller.handleObtenerReservaciones(ctx);

            verify(ctx).status(200);
            verify(ctx).json(reservaciones);
        }
    }

    // =========================================================================
    // handleExpirarReservacion
    // =========================================================================

    @Test
    @DisplayName("handleExpirarReservacion_authFalla_noInvocaServicio")
    void handleExpirarReservacion_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleExpirarReservacion(ctx);

            verify(service, never()).expirarReservacion(anyInt(), anyInt());
        }
    }

    @Test
    @DisplayName("handleExpirarReservacion_reservacionValida_expiraYRetornaMensaje")
    void handleExpirarReservacion_reservacionValida_expiraYRetornaMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            when(ctx.pathParam("id")).thenReturn("15");
            when(ctx.attribute("agenciaId")).thenReturn(5);

            controller.handleExpirarReservacion(ctx);

            verify(service).expirarReservacion(15, 5);
            // ctx.json() directamente (sin .status() chain) — solo verificamos json
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
            ));
        }
    }

    @Test
    @DisplayName("handleExpirarReservacion_reservacionInvalida_retorna400ConMensaje")
    void handleExpirarReservacion_reservacionInvalida_retorna400ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "La reservacion no pertenece a la agencia";
            when(ctx.pathParam("id")).thenReturn("15");
            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.status(400)).thenReturn(ctx);
            doThrow(new IllegalArgumentException(errorMsg))
                    .when(service).expirarReservacion(15, 5);

            controller.handleExpirarReservacion(ctx);

            verify(ctx).status(400);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }

    // =========================================================================
    // handleObtenerDetalleReservacion
    // =========================================================================

    @Test
    @DisplayName("handleObtenerDetalleReservacion_authFalla_noInvocaServicio")
    void handleObtenerDetalleReservacion_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleObtenerDetalleReservacion(ctx);

            verify(service, never()).obtenerDetalleReservacion(anyInt(), anyInt());
        }
    }

    @Test
    @DisplayName("handleObtenerDetalleReservacion_reservacionExiste_retornaDetalle200")
    void handleObtenerDetalleReservacion_reservacionExiste_retornaDetalle200() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("30");
            when(ctx.status(200)).thenReturn(ctx);
            doReturn(null).when(service).obtenerDetalleReservacion(30, 5);

            controller.handleObtenerDetalleReservacion(ctx);

            verify(ctx).status(200);
            verify(ctx).json(any());
        }
    }

    @Test
    @DisplayName("handleObtenerDetalleReservacion_reservacionNoEncontrada_retorna404ConMensaje")
    void handleObtenerDetalleReservacion_reservacionNoEncontrada_retorna404ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "Reservacion no encontrada";
            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("30");
            when(ctx.status(404)).thenReturn(ctx);
            when(service.obtenerDetalleReservacion(30, 5))
                    .thenThrow(new IllegalArgumentException(errorMsg));

            controller.handleObtenerDetalleReservacion(ctx);

            verify(ctx).status(404);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }

    @Test
    @DisplayName("handleObtenerDetalleReservacion_errorInterno_retorna500ConMensaje")
    void handleObtenerDetalleReservacion_errorInterno_retorna500ConMensaje() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            String errorMsg = "Fallo al consultar la base de datos";
            when(ctx.attribute("agenciaId")).thenReturn(5);
            when(ctx.pathParam("id")).thenReturn("30");
            when(ctx.status(500)).thenReturn(ctx);
            when(service.obtenerDetalleReservacion(30, 5))
                    .thenThrow(new RuntimeException(errorMsg));

            controller.handleObtenerDetalleReservacion(ctx);

            verify(ctx).status(500);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).get("mensaje").equals(errorMsg)
            ));
        }
    }
}
