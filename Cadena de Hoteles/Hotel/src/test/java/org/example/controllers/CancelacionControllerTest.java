package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.CancelacionRequestDTO;
import org.example.services.CancelacionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelacionController - Tests unitarios")
class CancelacionControllerTest {

    @Mock private CancelacionService service;
    @Mock private Context ctx;
    @Mock private CancelacionRequestDTO requestDTO;
    private CancelacionController controller;

    @BeforeEach
    void setUp() {
        controller = new CancelacionController(service);
    }

    @Test
    @DisplayName("handleCancelarReservacion_reservacionValida_retorna200")
    void handleCancelarReservacion_reservacionValida_retorna200() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(5);
        when(ctx.pathParam("id")).thenReturn("42");
        when(ctx.bodyAsClass(CancelacionRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getMotivoCancelacion()).thenReturn("Ya no voy");
        when(ctx.status(200)).thenReturn(ctx);
        doNothing().when(service).cancelarReservacion(42, 5, "Ya no voy");

        // act
        controller.handleCancelarReservacion(ctx);

        // assert
        verify(service).cancelarReservacion(42, 5, "Ya no voy");
        verify(ctx).status(200);
        verify(ctx).json(Map.of("mensaje", "Reservacion cancelada correctamente"));
    }

    @Test
    @DisplayName("handleCancelarReservacion_reservacionInvalida_retorna400")
    void handleCancelarReservacion_reservacionInvalida_retorna400() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(5);
        when(ctx.pathParam("id")).thenReturn("99");
        when(ctx.bodyAsClass(CancelacionRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getMotivoCancelacion()).thenReturn("motivo");
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Reservacion no encontrada"))
                .when(service).cancelarReservacion(99, 5, "motivo");

        // act
        controller.handleCancelarReservacion(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Reservacion no encontrada"));
        verify(ctx, never()).status(200);
    }
}
