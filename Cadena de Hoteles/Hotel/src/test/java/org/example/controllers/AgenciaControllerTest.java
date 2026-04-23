package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.CrearAgenciaAdminRequestDTO;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;
import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.services.AgenciaService;
import org.example.services.HandshakeService;
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
@DisplayName("AgenciaController - Tests unitarios")
class AgenciaControllerTest {

    @Mock private AgenciaService agenciaService;
    @Mock private HandshakeService handshakeService;
    @Mock private Context ctx;
    @Mock private CrearAgenciaRequestDTO crearDTO;
    @Mock private CrearAgenciaAdminRequestDTO crearAdminDTO;
    @Mock private EditarAgenciaRequestDTO editarDTO;
    @Mock private HandshakeRequestDTO handshakeRequestDTO;
    @Mock private HandshakeResponseDTO handshakeResponseDTO;
    private AgenciaController controller;

    @BeforeEach
    void setUp() {
        controller = new AgenciaController(agenciaService, handshakeService);
    }

    // ---- handleListarWebservice ----

    @Test
    @DisplayName("handleListarWebservice_rolWebservice_retornaLista")
    void handleListarWebservice_rolWebservice_retornaLista() {
        List<Object> lista = List.of(Map.of("id", 1), Map.of("id", 2));
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(20);
        doReturn(lista).when(agenciaService).listarPorUsuario(20);

        controller.handleListarWebservice(ctx);

        verify(ctx).json(lista);
        verify(ctx, never()).status(403);
    }

    @Test
    @DisplayName("handleListarWebservice_rolNoWebservice_retorna403")
    void handleListarWebservice_rolNoWebservice_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarWebservice(ctx);

        verify(ctx).status(403);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(agenciaService, never()).listarPorUsuario(anyInt());
    }

    // ---- handleCrearWebservice ----

    @Test
    @DisplayName("handleCrearWebservice_datosValidos_retorna201")
    void handleCrearWebservice_datosValidos_retorna201() {
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(20);
        when(ctx.bodyAsClass(CrearAgenciaRequestDTO.class)).thenReturn(crearDTO);
        when(ctx.status(201)).thenReturn(ctx);
        doReturn(null).when(agenciaService).crear(20, crearDTO);

        controller.handleCrearWebservice(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleCrearWebservice_rolNoWebservice_retorna403")
    void handleCrearWebservice_rolNoWebservice_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCrearWebservice(ctx);

        verify(ctx).status(403);
        verify(agenciaService, never()).crear(anyInt(), any());
    }

    @Test
    @DisplayName("handleCrearWebservice_argumentoInvalido_retorna400")
    void handleCrearWebservice_argumentoInvalido_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(20);
        when(ctx.bodyAsClass(CrearAgenciaRequestDTO.class)).thenReturn(crearDTO);
        when(ctx.status(400)).thenReturn(ctx);
        when(agenciaService.crear(20, crearDTO))
                .thenThrow(new IllegalArgumentException("Nombre de agencia duplicado"));

        controller.handleCrearWebservice(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(201);
    }

    // ---- handleCambiarEstadoWebservice ----

    @Test
    @DisplayName("handleCambiarEstadoWebservice_datosValidos_retornaMensaje")
    void handleCambiarEstadoWebservice_datosValidos_retornaMensaje() {
        Map<String, Object> body = Map.of("estadoId", 2);
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(20);
        when(ctx.pathParam("id")).thenReturn("7");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);

        controller.handleCambiarEstadoWebservice(ctx);

        verify(agenciaService).cambiarEstado(7, 20, 2);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleCambiarEstadoWebservice_rolNoWebservice_retorna403")
    void handleCambiarEstadoWebservice_rolNoWebservice_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCambiarEstadoWebservice(ctx);

        verify(ctx).status(403);
        verify(agenciaService, never()).cambiarEstado(anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("handleCambiarEstadoWebservice_estadoInvalido_retorna400")
    void handleCambiarEstadoWebservice_estadoInvalido_retorna400() {
        Map<String, Object> body = Map.of("estadoId", 99);
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(20);
        when(ctx.pathParam("id")).thenReturn("7");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Estado no valido"))
                .when(agenciaService).cambiarEstado(7, 20, 99);

        controller.handleCambiarEstadoWebservice(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    // ---- handleEliminarWebservice ----

    @Test
    @DisplayName("handleEliminarWebservice_agenciaExistente_retornaMensaje")
    void handleEliminarWebservice_agenciaExistente_retornaMensaje() {
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(20);
        when(ctx.pathParam("id")).thenReturn("7");

        controller.handleEliminarWebservice(ctx);

        verify(agenciaService).eliminar(7, 20);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleEliminarWebservice_rolNoWebservice_retorna403")
    void handleEliminarWebservice_rolNoWebservice_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEliminarWebservice(ctx);

        verify(ctx).status(403);
        verify(agenciaService, never()).eliminar(anyInt(), anyInt());
    }

    @Test
    @DisplayName("handleEliminarWebservice_agenciaNoPertenece_retorna400")
    void handleEliminarWebservice_agenciaNoPertenece_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.attribute("usuarioId")).thenReturn(20);
        when(ctx.pathParam("id")).thenReturn("7");
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Agencia no pertenece al usuario"))
                .when(agenciaService).eliminar(7, 20);

        controller.handleEliminarWebservice(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    // ---- handleHandshake ----

    @Test
    @DisplayName("handleHandshake_datosValidos_retornaResponse")
    void handleHandshake_datosValidos_retornaResponse() {
        when(ctx.body()).thenReturn("{\"urlAgencia\":\"https://agencia.com\"}");
        when(ctx.bodyAsClass(HandshakeRequestDTO.class)).thenReturn(handshakeRequestDTO);
        when(handshakeRequestDTO.getUrlAgencia()).thenReturn("https://agencia.com");
        when(handshakeRequestDTO.getTokenEntrada()).thenReturn("tok-in");
        when(handshakeService.procesarHandshake(handshakeRequestDTO)).thenReturn(handshakeResponseDTO);

        controller.handleHandshake(ctx);

        verify(handshakeService).procesarHandshake(handshakeRequestDTO);
        verify(ctx).json(handshakeResponseDTO);
        verify(ctx, never()).status(anyInt());
    }

    @Test
    @DisplayName("handleHandshake_agenciaNoRegistrada_retorna400")
    void handleHandshake_agenciaNoRegistrada_retorna400() {
        when(ctx.body()).thenReturn("{\"urlAgencia\":\"https://desconocida.com\"}");
        when(ctx.bodyAsClass(HandshakeRequestDTO.class)).thenReturn(handshakeRequestDTO);
        when(handshakeRequestDTO.getUrlAgencia()).thenReturn("https://desconocida.com");
        when(handshakeRequestDTO.getTokenEntrada()).thenReturn("tok-mal");
        when(ctx.status(400)).thenReturn(ctx);
        when(handshakeService.procesarHandshake(handshakeRequestDTO))
                .thenThrow(new IllegalArgumentException("Agencia no registrada"));

        controller.handleHandshake(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    // ---- handleListarAdmin ----

    @Test
    @DisplayName("handleListarAdmin_rolAdministrador_retornaTodasLasAgencias")
    void handleListarAdmin_rolAdministrador_retornaTodasLasAgencias() {
        List<Object> lista = List.of(Map.of("id", 1), Map.of("id", 2), Map.of("id", 3));
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(lista).when(agenciaService).listarTodas();

        controller.handleListarAdmin(ctx);

        verify(ctx).json(lista);
        verify(ctx, never()).status(403);
    }

    @Test
    @DisplayName("handleListarAdmin_rolNoAdministrador_retorna403")
    void handleListarAdmin_rolNoAdministrador_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarAdmin(ctx);

        verify(ctx).status(403);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(agenciaService, never()).listarTodas();
    }

    // ---- handleCrearAdmin ----

    @Test
    @DisplayName("handleCrearAdmin_datosValidos_retorna201")
    void handleCrearAdmin_datosValidos_retorna201() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.bodyAsClass(CrearAgenciaAdminRequestDTO.class)).thenReturn(crearAdminDTO);
        when(ctx.status(201)).thenReturn(ctx);
        doReturn(null).when(agenciaService).crearDesdeAdmin(crearAdminDTO);

        controller.handleCrearAdmin(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleCrearAdmin_rolNoAdministrador_retorna403")
    void handleCrearAdmin_rolNoAdministrador_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCrearAdmin(ctx);

        verify(ctx).status(403);
        verify(agenciaService, never()).crearDesdeAdmin(any());
    }

    @Test
    @DisplayName("handleCrearAdmin_datosInvalidos_retorna400")
    void handleCrearAdmin_datosInvalidos_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.bodyAsClass(CrearAgenciaAdminRequestDTO.class)).thenReturn(crearAdminDTO);
        when(ctx.status(400)).thenReturn(ctx);
        when(agenciaService.crearDesdeAdmin(crearAdminDTO))
                .thenThrow(new IllegalArgumentException("Usuario webservice no encontrado"));

        controller.handleCrearAdmin(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(201);
    }

    // ---- handleEditarAdmin ----

    @Test
    @DisplayName("handleEditarAdmin_datosValidos_retornaMensaje")
    void handleEditarAdmin_datosValidos_retornaMensaje() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        when(ctx.bodyAsClass(EditarAgenciaRequestDTO.class)).thenReturn(editarDTO);

        controller.handleEditarAdmin(ctx);

        verify(agenciaService).editar(5, editarDTO);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleEditarAdmin_rolNoAdministrador_retorna403")
    void handleEditarAdmin_rolNoAdministrador_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEditarAdmin(ctx);

        verify(ctx).status(403);
        verify(agenciaService, never()).editar(anyInt(), any());
    }

    @Test
    @DisplayName("handleEditarAdmin_agenciaNoEncontrada_retorna400")
    void handleEditarAdmin_agenciaNoEncontrada_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        when(ctx.bodyAsClass(EditarAgenciaRequestDTO.class)).thenReturn(editarDTO);
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Agencia no encontrada"))
                .when(agenciaService).editar(5, editarDTO);

        controller.handleEditarAdmin(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }
}
