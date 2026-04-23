package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.CrearAerolineaAdminRequestDTO;
import org.example.dtos.EditarAerolineaRequestDTO;
import org.example.services.AerolineaAdminService;
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
@DisplayName("AerolineaAdminController - Tests unitarios")
class AerolineaAdminControllerTest {

    @Mock private AerolineaAdminService service;
    @Mock private Context ctx;
    private AerolineaAdminController controller;

    @BeforeEach
    void setUp() {
        controller = new AerolineaAdminController(service);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleListar
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleListar_conRolAdmin_retornaListaDeAerolineas")
    void handleListar_conRolAdmin_retornaListaDeAerolineas() {
        // arrange
        List<Object> lista = List.of("Aerolinea A", "Aerolinea B");
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(lista).when(service).listarTodas();

        // act
        controller.handleListar(ctx);

        // assert
        verify(service).listarTodas();
        verify(ctx).json(lista);
    }

    @Test
    @DisplayName("handleListar_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleListar_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleListar(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).listarTodas();
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
        verify(service, never()).listarTodas();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleCrear
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleCrear_conRolAdminYDatosValidos_retorna201ConNuevaAerolinea")
    void handleCrear_conRolAdminYDatosValidos_retorna201ConNuevaAerolinea() {
        // arrange
        CrearAerolineaAdminRequestDTO dto = mock(CrearAerolineaAdminRequestDTO.class);
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.bodyAsClass(CrearAerolineaAdminRequestDTO.class)).thenReturn(dto);
        when(ctx.status(201)).thenReturn(ctx);
        doReturn(null).when(service).crear(dto);

        // act
        controller.handleCrear(ctx);

        // assert
        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(service).crear(dto);
    }

    @Test
    @DisplayName("handleCrear_conRolAdminYDatosInvalidos_retorna400ConMensaje")
    void handleCrear_conRolAdminYDatosInvalidos_retorna400ConMensaje() {
        // arrange
        CrearAerolineaAdminRequestDTO dto = mock(CrearAerolineaAdminRequestDTO.class);
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.bodyAsClass(CrearAerolineaAdminRequestDTO.class)).thenReturn(dto);
        when(ctx.status(400)).thenReturn(ctx);
        when(service.crear(dto)).thenThrow(new IllegalArgumentException("Datos invalidos"));

        // act
        controller.handleCrear(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Datos invalidos"));
    }

    @Test
    @DisplayName("handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleCrear_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleCrear(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).crear(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleEditar
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleEditar_conRolAdminYDatosValidos_retornaMensajeExito")
    void handleEditar_conRolAdminYDatosValidos_retornaMensajeExito() {
        // arrange
        EditarAerolineaRequestDTO dto = mock(EditarAerolineaRequestDTO.class);
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        when(ctx.bodyAsClass(EditarAerolineaRequestDTO.class)).thenReturn(dto);

        // act
        controller.handleEditar(ctx);

        // assert
        verify(service).editar(5, dto);
        verify(ctx).json(Map.of("mensaje", "Aerolinea actualizada correctamente"));
    }

    @Test
    @DisplayName("handleEditar_conRolAdminYDatosInvalidos_retorna400ConMensaje")
    void handleEditar_conRolAdminYDatosInvalidos_retorna400ConMensaje() {
        // arrange
        EditarAerolineaRequestDTO dto = mock(EditarAerolineaRequestDTO.class);
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        when(ctx.bodyAsClass(EditarAerolineaRequestDTO.class)).thenReturn(dto);
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Nombre ya existe")).when(service).editar(5, dto);

        // act
        controller.handleEditar(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Nombre ya existe"));
    }

    @Test
    @DisplayName("handleEditar_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleEditar_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleEditar(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).editar(anyInt(), any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleListarLibres
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleListarLibres_conRolAdmin_retornaListaDeWebserviceLibres")
    void handleListarLibres_conRolAdmin_retornaListaDeWebserviceLibres() {
        // arrange
        List<Object> libres = List.of("Usuario WS 1", "Usuario WS 2");
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(libres).when(service).listarWebserviceLibres();

        // act
        controller.handleListarLibres(ctx);

        // assert
        verify(service).listarWebserviceLibres();
        verify(ctx).json(libres);
    }

    @Test
    @DisplayName("handleListarLibres_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleListarLibres_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleListarLibres(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).listarWebserviceLibres();
    }
}
