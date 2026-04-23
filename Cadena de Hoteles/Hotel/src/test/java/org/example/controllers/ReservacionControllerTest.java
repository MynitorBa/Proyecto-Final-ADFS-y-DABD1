package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.ReservacionRequestDTO;
import org.example.services.ReservacionService;
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
@DisplayName("ReservacionController - Tests unitarios")
class ReservacionControllerTest {

    @Mock private ReservacionService service;
    @Mock private Context ctx;
    @Mock private ReservacionRequestDTO requestDTO;
    private ReservacionController controller;

    @BeforeEach
    void setUp() {
        controller = new ReservacionController(service);
    }

    // ---- handleCrearReservacion ----

    @Test
    @DisplayName("handleCrearReservacion_datosValidos_retorna201")
    void handleCrearReservacion_datosValidos_retorna201() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(4);
        when(ctx.bodyAsClass(ReservacionRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.status(201)).thenReturn(ctx);
        doReturn(null).when(service).crearReservacion(requestDTO, 4);

        // act
        controller.handleCrearReservacion(ctx);

        // assert
        verify(ctx).status(201);
        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleCrearReservacion_argumentoInvalido_retorna400")
    void handleCrearReservacion_argumentoInvalido_retorna400() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(4);
        when(ctx.bodyAsClass(ReservacionRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.status(400)).thenReturn(ctx);
        when(service.crearReservacion(requestDTO, 4))
                .thenThrow(new IllegalArgumentException("Habitacion no disponible"));

        // act
        controller.handleCrearReservacion(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Habitacion no disponible"));
        verify(ctx, never()).status(201);
        verify(ctx, never()).status(500);
    }

    @Test
    @DisplayName("handleCrearReservacion_errorRuntime_retorna500")
    void handleCrearReservacion_errorRuntime_retorna500() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(4);
        when(ctx.bodyAsClass(ReservacionRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.status(500)).thenReturn(ctx);
        when(service.crearReservacion(requestDTO, 4))
                .thenThrow(new RuntimeException("Error interno al guardar"));

        // act
        controller.handleCrearReservacion(ctx);

        // assert
        verify(ctx).status(500);
        verify(ctx).json(Map.of("mensaje", "Error interno al guardar"));
        verify(ctx, never()).status(201);
        verify(ctx, never()).status(400);
    }

    // ---- handleObtenerReservaciones ----

    @Test
    @DisplayName("handleObtenerReservaciones_usuarioConReservaciones_retorna200ConLista")
    void handleObtenerReservaciones_usuarioConReservaciones_retorna200ConLista() {
        // arrange
        List<Object> lista = List.of(Map.of("id", 1), Map.of("id", 2));
        when(ctx.attribute("usuarioId")).thenReturn(4);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(service).obtenerReservaciones(4);

        // act
        controller.handleObtenerReservaciones(ctx);

        // assert
        verify(ctx).status(200);
        verify(ctx).json(lista);
    }

    @Test
    @DisplayName("handleObtenerReservaciones_usuarioSinReservaciones_retorna200ConListaVacia")
    void handleObtenerReservaciones_usuarioSinReservaciones_retorna200ConListaVacia() {
        // arrange
        List<Object> lista = Collections.emptyList();
        when(ctx.attribute("usuarioId")).thenReturn(4);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(service).obtenerReservaciones(4);

        // act
        controller.handleObtenerReservaciones(ctx);

        // assert
        verify(ctx).status(200);
        verify(ctx).json(lista);
    }
}
