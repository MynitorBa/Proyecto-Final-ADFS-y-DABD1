package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.ComentarioRequestDTO;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.ComentarioService;
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
@DisplayName("ComentarioController - Tests unitarios")
class ComentarioControllerTest {

    @Mock private ComentarioService comentarioService;
    @Mock private Context ctx;
    @Mock private ComentarioRequestDTO requestDTO;
    private ComentarioController controller;

    @BeforeEach
    void setUp() {
        controller = new ComentarioController(comentarioService);
    }

    // ---- handleAgregarComentario ----

    @Test
    @DisplayName("handleAgregarComentario_datosValidos_retorna201")
    void handleAgregarComentario_datosValidos_retorna201() {
        when(ctx.attribute("usuarioId")).thenReturn(5);
        when(ctx.bodyAsClass(ComentarioRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.status(201)).thenReturn(ctx);
        doReturn(null).when(comentarioService).agregarComentario(requestDTO, 5);

        controller.handleAgregarComentario(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleAgregarComentario_argumentoInvalido_retorna400")
    void handleAgregarComentario_argumentoInvalido_retorna400() {
        when(ctx.attribute("usuarioId")).thenReturn(5);
        when(ctx.bodyAsClass(ComentarioRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.status(400)).thenReturn(ctx);
        when(comentarioService.agregarComentario(requestDTO, 5))
                .thenThrow(new IllegalArgumentException("Ya existe un comentario para ese hotel"));

        controller.handleAgregarComentario(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(201);
    }

    // ---- handleObtenerPorUsuario ----

    @Test
    @DisplayName("handleObtenerPorUsuario_usuarioConComentarios_retorna200ConLista")
    void handleObtenerPorUsuario_usuarioConComentarios_retorna200ConLista() {
        List<Object> lista = List.of(Map.of("id", 1), Map.of("id", 2));
        when(ctx.attribute("usuarioId")).thenReturn(5);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(comentarioService).obtenerComentariosPorUsuario(5);

        controller.handleObtenerPorUsuario(ctx);

        verify(ctx).status(200);
        verify(ctx).json(lista);
    }

    @Test
    @DisplayName("handleObtenerPorUsuario_usuarioSinComentarios_retorna200ConListaVacia")
    void handleObtenerPorUsuario_usuarioSinComentarios_retorna200ConListaVacia() {
        List<Object> lista = Collections.emptyList();
        when(ctx.attribute("usuarioId")).thenReturn(5);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(comentarioService).obtenerComentariosPorUsuario(5);

        controller.handleObtenerPorUsuario(ctx);

        verify(ctx).status(200);
        verify(ctx).json(lista);
    }

    // ---- handleObtenerPorHotel ----

    @Test
    @DisplayName("handleObtenerPorHotel_hotelValido_retorna200ConLista")
    void handleObtenerPorHotel_hotelValido_retorna200ConLista() {
        List<Object> lista = List.of(Map.of("id", 10));
        when(ctx.pathParam("hotelId")).thenReturn("3");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(comentarioService).obtenerComentariosPorHotel(3);

        controller.handleObtenerPorHotel(ctx);

        verify(ctx).status(200);
        verify(ctx).json(lista);
    }

    @Test
    @DisplayName("handleObtenerPorHotel_hotelSinComentarios_retorna200ConListaVacia")
    void handleObtenerPorHotel_hotelSinComentarios_retorna200ConListaVacia() {
        List<Object> lista = Collections.emptyList();
        when(ctx.pathParam("hotelId")).thenReturn("99");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(lista).when(comentarioService).obtenerComentariosPorHotel(99);

        controller.handleObtenerPorHotel(ctx);

        verify(ctx).status(200);
        verify(ctx).json(lista);
    }

    // ---- handleObtenerPorHotelAgencia ----

    @Test
    @DisplayName("handleObtenerPorHotelAgencia_authFalla_noInvocaServicio")
    void handleObtenerPorHotelAgencia_authFalla_noInvocaServicio() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            controller.handleObtenerPorHotelAgencia(ctx);

            verify(comentarioService, never()).obtenerComentariosPorHotel(anyInt());
            verify(ctx, never()).status(anyInt());
        }
    }

    @Test
    @DisplayName("handleObtenerPorHotelAgencia_authOk_retorna200ConLista")
    void handleObtenerPorHotelAgencia_authOk_retorna200ConLista() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            List<Object> lista = List.of(Map.of("id", 7));
            when(ctx.pathParam("hotelId")).thenReturn("2");
            when(ctx.status(200)).thenReturn(ctx);
            doReturn(lista).when(comentarioService).obtenerComentariosPorHotel(2);

            controller.handleObtenerPorHotelAgencia(ctx);

            verify(ctx).status(200);
            verify(ctx).json(lista);
        }
    }

    @Test
    @DisplayName("handleObtenerPorHotelAgencia_servicioLanzaExcepcion_retorna400")
    void handleObtenerPorHotelAgencia_servicioLanzaExcepcion_retorna400() {
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);

            when(ctx.pathParam("hotelId")).thenReturn("2");
            when(ctx.status(400)).thenReturn(ctx);
            when(comentarioService.obtenerComentariosPorHotel(2))
                    .thenThrow(new IllegalArgumentException("Hotel no encontrado"));

            controller.handleObtenerPorHotelAgencia(ctx);

            verify(ctx).status(400);
            verify(ctx).json(argThat(obj ->
                    obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
            ));
        }
    }
}
