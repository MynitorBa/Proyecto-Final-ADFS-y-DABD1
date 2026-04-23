package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.CrearAerolineaRequestDTO;
import org.example.services.AerolineaWebserviceService;
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
@DisplayName("AerolineaWebserviceController - Tests unitarios")
class AerolineaWebserviceControllerTest {

    @Mock private AerolineaWebserviceService service;
    @Mock private Context ctx;
    private AerolineaWebserviceController controller;

    @BeforeEach
    void setUp() {
        controller = new AerolineaWebserviceController(service);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleListar
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleListar_conRolWebservice_retornaAerolineasDelUsuario")
    void handleListar_conRolWebservice_retornaAerolineasDelUsuario() {
        // arrange
        List<Object> lista = List.of("Aerolinea X");
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(10);
        doReturn(lista).when(service).listarPorUsuario(10);

        // act
        controller.handleListar(ctx);

        // assert
        verify(service).listarPorUsuario(10);
        verify(ctx).json(lista);
    }

    @Test
    @DisplayName("handleListar_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleListar_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleListar(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).listarPorUsuario(anyInt());
    }

    @Test
    @DisplayName("handleListar_conRolNulo_retorna403YNoLlamaServicio")
    void handleListar_conRolNulo_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleListar(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).listarPorUsuario(anyInt());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleCrear
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleCrear_conRolWebserviceYDatosValidos_retorna201ConNuevaAerolinea")
    void handleCrear_conRolWebserviceYDatosValidos_retorna201ConNuevaAerolinea() {
        // arrange
        CrearAerolineaRequestDTO dto = mock(CrearAerolineaRequestDTO.class);
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.bodyAsClass(CrearAerolineaRequestDTO.class)).thenReturn(dto);
        when(ctx.status(201)).thenReturn(ctx);
        doReturn(null).when(service).crear(10, dto);

        // act
        controller.handleCrear(ctx);

        // assert
        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(service).crear(10, dto);
    }

    @Test
    @DisplayName("handleCrear_conRolWebserviceYDatosInvalidos_retorna400ConMensaje")
    void handleCrear_conRolWebserviceYDatosInvalidos_retorna400ConMensaje() {
        // arrange
        CrearAerolineaRequestDTO dto = mock(CrearAerolineaRequestDTO.class);
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.bodyAsClass(CrearAerolineaRequestDTO.class)).thenReturn(dto);
        when(ctx.status(400)).thenReturn(ctx);
        when(service.crear(10, dto)).thenThrow(new IllegalArgumentException("Nombre duplicado"));

        // act
        controller.handleCrear(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Nombre duplicado"));
    }

    @Test
    @DisplayName("handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleCrear(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).crear(anyInt(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleCambiarEstado
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleCambiarEstado_conRolWebserviceYDatosValidos_retornaMensajeExito")
    void handleCambiarEstado_conRolWebserviceYDatosValidos_retornaMensajeExito() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.pathParam("id")).thenReturn("7");
        when(ctx.bodyAsClass(Map.class)).thenReturn(Map.of("estadoId", "1"));

        // act
        controller.handleCambiarEstado(ctx);

        // assert
        verify(service).cambiarEstado(7, 10, 1);
        verify(ctx).json(Map.of("mensaje", "Estado actualizado correctamente"));
    }

    @Test
    @DisplayName("handleCambiarEstado_conRolWebserviceYEstadoInvalido_retorna400ConMensaje")
    void handleCambiarEstado_conRolWebserviceYEstadoInvalido_retorna400ConMensaje() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.pathParam("id")).thenReturn("7");
        when(ctx.bodyAsClass(Map.class)).thenReturn(Map.of("estadoId", "99"));
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Estado no valido")).when(service).cambiarEstado(7, 10, 99);

        // act
        controller.handleCambiarEstado(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Estado no valido"));
    }

    @Test
    @DisplayName("handleCambiarEstado_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleCambiarEstado_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleCambiarEstado(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).cambiarEstado(anyInt(), anyInt(), anyInt());
    }
}
