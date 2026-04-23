package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.DownRequestDTO;
import org.example.services.DownsService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DownsController - Tests unitarios")
class DownsControllerTest {

    @Mock private DownsService downsService;
    @Mock private Context ctx;
    @Mock private DownRequestDTO requestDTO;
    private DownsController controller;

    @BeforeEach
    void setUp() {
        controller = new DownsController(downsService);
    }

    // ---- handleAgregarDown ----

    @Test
    @DisplayName("handleAgregarDown_datosValidos_retorna201ConMensaje")
    void handleAgregarDown_datosValidos_retorna201ConMensaje() {
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("12");
        when(ctx.bodyAsClass(DownRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getValor()).thenReturn(-1);
        when(ctx.status(201)).thenReturn(ctx);

        controller.handleAgregarDown(ctx);

        verify(downsService).agregarDown(12, 7, -1);
        verify(ctx).status(201);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleAgregarDown_argumentoInvalido_retorna400")
    void handleAgregarDown_argumentoInvalido_retorna400() {
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("12");
        when(ctx.bodyAsClass(DownRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getValor()).thenReturn(-1);
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Ya existe un down para ese comentario"))
                .when(downsService).agregarDown(12, 7, -1);

        controller.handleAgregarDown(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(201);
    }

    // ---- handleEliminarDown ----

    @Test
    @DisplayName("handleEliminarDown_downExistente_retorna200ConMensaje")
    void handleEliminarDown_downExistente_retorna200ConMensaje() {
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("12");
        when(ctx.status(200)).thenReturn(ctx);

        controller.handleEliminarDown(ctx);

        verify(downsService).eliminarDown(12, 7);
        verify(ctx).status(200);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleEliminarDown_downNoExistente_retorna400")
    void handleEliminarDown_downNoExistente_retorna400() {
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("12");
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Down no encontrado"))
                .when(downsService).eliminarDown(12, 7);

        controller.handleEliminarDown(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(200);
    }

    // ---- handleActualizarDown ----

    @Test
    @DisplayName("handleActualizarDown_datosValidos_retorna200ConMensaje")
    void handleActualizarDown_datosValidos_retorna200ConMensaje() {
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("12");
        when(ctx.bodyAsClass(DownRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getValor()).thenReturn(-2);
        when(ctx.status(200)).thenReturn(ctx);

        controller.handleActualizarDown(ctx);

        verify(downsService).actualizarDown(12, 7, -2);
        verify(ctx).status(200);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleActualizarDown_downNoExistente_retorna400")
    void handleActualizarDown_downNoExistente_retorna400() {
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("id")).thenReturn("12");
        when(ctx.bodyAsClass(DownRequestDTO.class)).thenReturn(requestDTO);
        when(requestDTO.getValor()).thenReturn(-2);
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Down no encontrado para actualizar"))
                .when(downsService).actualizarDown(12, 7, -2);

        controller.handleActualizarDown(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(200);
    }

    // ---- handleObtenerDowns ----

    @Test
    @DisplayName("handleObtenerDowns_usuarioConDowns_retorna200ConLista")
    void handleObtenerDowns_usuarioConDowns_retorna200ConLista() {
        List<Object> lista = List.of(Map.of("comentarioId", 1), Map.of("comentarioId", 2));
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(downsService).obtenerDownsDeUsuario(7);

        controller.handleObtenerDowns(ctx);

        verify(ctx).status(200);
        verify(ctx).json(lista);
    }

    @Test
    @DisplayName("handleObtenerDowns_usuarioSinDowns_retorna200ConListaVacia")
    void handleObtenerDowns_usuarioSinDowns_retorna200ConListaVacia() {
        List<Object> lista = Collections.emptyList();
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(downsService).obtenerDownsDeUsuario(7);

        controller.handleObtenerDowns(ctx);

        verify(ctx).status(200);
        verify(ctx).json(lista);
    }

    // ---- handleObtenerDownsPorHotel ----

    @Test
    @DisplayName("handleObtenerDownsPorHotel_hotelValido_retorna200ConLista")
    void handleObtenerDownsPorHotel_hotelValido_retorna200ConLista() {
        List<Object> lista = List.of(Map.of("comentarioId", 5));
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("hotelId")).thenReturn("4");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(downsService).obtenerDownsDeUsuarioPorHotel(7, 4);

        controller.handleObtenerDownsPorHotel(ctx);

        verify(ctx).status(200);
        verify(ctx).json(lista);
    }

    @Test
    @DisplayName("handleObtenerDownsPorHotel_hotelSinDowns_retorna200ConListaVacia")
    void handleObtenerDownsPorHotel_hotelSinDowns_retorna200ConListaVacia() {
        List<Object> lista = Collections.emptyList();
        when(ctx.attribute("usuarioId")).thenReturn(7);
        when(ctx.pathParam("hotelId")).thenReturn("99");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(downsService).obtenerDownsDeUsuarioPorHotel(7, 99);

        controller.handleObtenerDownsPorHotel(ctx);

        verify(ctx).status(200);
        verify(ctx).json(lista);
    }
}
