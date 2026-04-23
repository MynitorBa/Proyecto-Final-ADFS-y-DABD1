package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.PagoRequestDTO;
import org.example.services.PagoService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PagoController - Tests unitarios")
class PagoControllerTest {

    @Mock private PagoService service;
    @Mock private Context ctx;
    @Mock private PagoRequestDTO requestDTO;
    private PagoController controller;

    @BeforeEach
    void setUp() {
        controller = new PagoController(service);
    }

    @Test
    @DisplayName("handleProcesarPago_pagoExitoso_retorna200")
    void handleProcesarPago_pagoExitoso_retorna200() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("15");
        when(ctx.bodyAsClass(PagoRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(null).when(service).procesarPago(15, 7, requestDTO);

        // act
        controller.handleProcesarPago(ctx);

        // assert
        verify(ctx).status(200);
        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleProcesarPago_argumentoInvalido_retorna400")
    void handleProcesarPago_argumentoInvalido_retorna400() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("15");
        when(ctx.bodyAsClass(PagoRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.status(400)).thenReturn(ctx);
        when(service.procesarPago(15, 7, requestDTO))
                .thenThrow(new IllegalArgumentException("Metodo de pago invalido"));

        // act
        controller.handleProcesarPago(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Metodo de pago invalido"));
        verify(ctx, never()).status(200);
        verify(ctx, never()).status(500);
    }

    @Test
    @DisplayName("handleProcesarPago_errorRuntime_retorna500")
    void handleProcesarPago_errorRuntime_retorna500() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("15");
        when(ctx.bodyAsClass(PagoRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.status(500)).thenReturn(ctx);
        when(service.procesarPago(15, 7, requestDTO))
                .thenThrow(new RuntimeException("Error de conexion con pasarela"));

        // act
        controller.handleProcesarPago(ctx);

        // assert
        verify(ctx).status(500);
        verify(ctx).json(Map.of("mensaje", "Error de conexion con pasarela"));
        verify(ctx, never()).status(200);
        verify(ctx, never()).status(400);
    }
}
