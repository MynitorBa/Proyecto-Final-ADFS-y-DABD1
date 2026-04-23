package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.CambiarContrasenaRequestDTO;
import org.example.dtos.CambiarRolRequestDTO;
import org.example.dtos.CambiarTelefonoRequestDTO;
import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.dtos.UsuarioValidacionResponseDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.helpers.CredencialesInvalidasException;
import org.example.services.UsuarioService;
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
@DisplayName("UsuarioController - Tests unitarios")
class UsuarioControllerTest {

    @Mock private UsuarioService usuarioService;
    @Mock private Context ctx;
    @Mock private UsuarioValidacionRequestDTO validacionDTO;
    @Mock private CambiarTelefonoRequestDTO telefonoDTO;
    @Mock private CambiarContrasenaRequestDTO contrasenaDTO;
    @Mock private CambiarRolRequestDTO rolDTO;
    private UsuarioController controller;

    @BeforeEach
    void setUp() {
        controller = new UsuarioController(usuarioService);
    }

    // ---- handleValidar ----

    @Test
    @DisplayName("handleValidar_requestValido_retornaResultado")
    void handleValidar_requestValido_retornaResultado() {
        when(ctx.bodyAsClass(UsuarioValidacionRequestDTO.class)).thenReturn(validacionDTO);
        doReturn(null).when(usuarioService).validarDisponibilidad(validacionDTO);

        controller.handleValidar(ctx);

        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleValidar_servicioRetornaFalse_retornaResultadoNoDisponible")
    void handleValidar_servicioRetornaFalse_retornaResultadoNoDisponible() {
        when(ctx.bodyAsClass(UsuarioValidacionRequestDTO.class)).thenReturn(validacionDTO);
        doReturn(null).when(usuarioService).validarDisponibilidad(validacionDTO);

        controller.handleValidar(ctx);

        verify(ctx).json(any());
        verify(usuarioService).validarDisponibilidad(validacionDTO);
    }

    // ---- handleRegistrar ----

    @Test
    @DisplayName("handleRegistrar_nuevoUsuario_retorna201ConId")
    void handleRegistrar_nuevoUsuario_retorna201ConId() throws CamposDuplicadosException {
        when(ctx.bodyAsClass(UsuarioValidacionRequestDTO.class)).thenReturn(validacionDTO);
        when(ctx.status(201)).thenReturn(ctx);
        when(usuarioService.registrarUsuario(validacionDTO)).thenReturn(42);

        controller.handleRegistrar(ctx);

        verify(ctx).status(201);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
                        && ((Map<?, ?>) obj).containsKey("usuarioId")
        ));
    }

    @Test
    @DisplayName("handleRegistrar_camposDuplicados_retorna409")
    void handleRegistrar_camposDuplicados_retorna409() throws CamposDuplicadosException {
        CamposDuplicadosException ex = mock(CamposDuplicadosException.class);
        doReturn(mock(UsuarioValidacionResponseDTO.class)).when(ex).getDetalle();
        when(ctx.bodyAsClass(UsuarioValidacionRequestDTO.class)).thenReturn(validacionDTO);
        when(ctx.status(409)).thenReturn(ctx);
        when(usuarioService.registrarUsuario(validacionDTO)).thenThrow(ex);

        controller.handleRegistrar(ctx);

        verify(ctx).status(409);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
                        && ((Map<?, ?>) obj).containsKey("campos")
        ));
        verify(ctx, never()).status(201);
    }

    // ---- handleObtenerPerfil ----

    @Test
    @DisplayName("handleObtenerPerfil_usuarioAutenticado_retornaPerfil")
    void handleObtenerPerfil_usuarioAutenticado_retornaPerfil() {
        when(ctx.attribute("usuarioId")).thenReturn(10);
        doReturn(null).when(usuarioService).obtenerPerfil(10);

        controller.handleObtenerPerfil(ctx);

        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleObtenerPerfil_diferentesUsuarios_llamaServicioConIdCorrecto")
    void handleObtenerPerfil_diferentesUsuarios_llamaServicioConIdCorrecto() {
        when(ctx.attribute("usuarioId")).thenReturn(99);
        doReturn(null).when(usuarioService).obtenerPerfil(99);

        controller.handleObtenerPerfil(ctx);

        verify(usuarioService).obtenerPerfil(99);
        verify(ctx).json(any());
    }

    // ---- handleCambiarTelefono ----

    @Test
    @DisplayName("handleCambiarTelefono_telefonoValido_retorna200")
    void handleCambiarTelefono_telefonoValido_retorna200() {
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.bodyAsClass(CambiarTelefonoRequestDTO.class)).thenReturn(telefonoDTO);
        when(telefonoDTO.getTelefono()).thenReturn("50250000000");
        when(ctx.status(200)).thenReturn(ctx);

        controller.handleCambiarTelefono(ctx);

        verify(usuarioService).cambiarTelefono(10, "50250000000");
        verify(ctx).status(200);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleCambiarTelefono_telefonoInvalido_retorna400")
    void handleCambiarTelefono_telefonoInvalido_retorna400() {
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.bodyAsClass(CambiarTelefonoRequestDTO.class)).thenReturn(telefonoDTO);
        when(telefonoDTO.getTelefono()).thenReturn("abc");
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Telefono invalido"))
                .when(usuarioService).cambiarTelefono(10, "abc");

        controller.handleCambiarTelefono(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(200);
    }

    // ---- handleCambiarContrasena ----

    @Test
    @DisplayName("handleCambiarContrasena_credencialesValidas_retorna200")
    void handleCambiarContrasena_credencialesValidas_retorna200() {
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.bodyAsClass(CambiarContrasenaRequestDTO.class)).thenReturn(contrasenaDTO);
        when(contrasenaDTO.getContrasenaActual()).thenReturn("actual123");
        when(contrasenaDTO.getContrasenaNueva()).thenReturn("nueva456");
        when(ctx.status(200)).thenReturn(ctx);

        controller.handleCambiarContrasena(ctx);

        verify(usuarioService).cambiarContrasena(10, "actual123", "nueva456");
        verify(ctx).status(200);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleCambiarContrasena_credencialesInvalidas_retorna401")
    void handleCambiarContrasena_credencialesInvalidas_retorna401() throws CredencialesInvalidasException {
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.bodyAsClass(CambiarContrasenaRequestDTO.class)).thenReturn(contrasenaDTO);
        when(contrasenaDTO.getContrasenaActual()).thenReturn("mala");
        when(contrasenaDTO.getContrasenaNueva()).thenReturn("nueva456");
        when(ctx.status(401)).thenReturn(ctx);
        doThrow(mock(CredencialesInvalidasException.class))
                .when(usuarioService).cambiarContrasena(10, "mala", "nueva456");

        controller.handleCambiarContrasena(ctx);

        verify(ctx).status(401);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(200);
    }

    // ---- handleListarAdmin ----

    @Test
    @DisplayName("handleListarAdmin_rolAdministrador_retornaLista")
    void handleListarAdmin_rolAdministrador_retornaLista() {
        List<Object> lista = List.of(Map.of("id", 1), Map.of("id", 2));
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(lista).when(usuarioService).listarTodosUsuarios();

        controller.handleListarAdmin(ctx);

        verify(ctx).json(lista);
        verify(ctx, never()).status(403);
    }

    @Test
    @DisplayName("handleListarAdmin_rolNoAutorizado_retorna403")
    void handleListarAdmin_rolNoAutorizado_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarAdmin(ctx);

        verify(ctx).status(403);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(usuarioService, never()).listarTodosUsuarios();
    }

    // ---- handleCambiarRol ----

    @Test
    @DisplayName("handleCambiarRol_adminActualiza_retorna200")
    void handleCambiarRol_adminActualiza_retorna200() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("15");
        when(ctx.bodyAsClass(CambiarRolRequestDTO.class)).thenReturn(rolDTO);
        when(rolDTO.getRolId()).thenReturn(1);
        when(ctx.status(200)).thenReturn(ctx);

        controller.handleCambiarRol(ctx);

        verify(usuarioService).cambiarRol(15, 1);
        verify(ctx).status(200);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }

    @Test
    @DisplayName("handleCambiarRol_rolNoAutorizado_retorna403")
    void handleCambiarRol_rolNoAutorizado_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCambiarRol(ctx);

        verify(ctx).status(403);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(usuarioService, never()).cambiarRol(anyInt(), anyInt());
    }

    @Test
    @DisplayName("handleCambiarRol_rolInvalido_retorna400")
    void handleCambiarRol_rolInvalido_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("15");
        when(ctx.bodyAsClass(CambiarRolRequestDTO.class)).thenReturn(rolDTO);
        when(rolDTO.getRolId()).thenReturn(99);
        when(ctx.status(400)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Rol no existe"))
                .when(usuarioService).cambiarRol(15, 99);

        controller.handleCambiarRol(ctx);

        verify(ctx).status(400);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(ctx, never()).status(200);
    }
}
