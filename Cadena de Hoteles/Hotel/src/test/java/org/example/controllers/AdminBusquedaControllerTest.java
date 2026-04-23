package org.example.controllers;

import io.javalin.http.Context;
import io.javalin.validation.Validator;
import org.example.services.AdminBusquedaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminBusquedaController - Tests unitarios")
class AdminBusquedaControllerTest {

    @Mock private AdminBusquedaService service;
    @Mock private Context ctx;
    private AdminBusquedaController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminBusquedaController(service);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers para mockear queryParamAsClass
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void mockQueryParamsDefaults() {
        Validator<String>  tipoV      = mock(Validator.class);
        Validator<Integer> paginaV    = mock(Validator.class);
        Validator<Integer> porPaginaV = mock(Validator.class);

        when(ctx.queryParamAsClass("tipo",      String.class) ).thenReturn(tipoV);
        when(ctx.queryParamAsClass("pagina",    Integer.class)).thenReturn(paginaV);
        when(ctx.queryParamAsClass("porPagina", Integer.class)).thenReturn(porPaginaV);

        when(tipoV.getOrDefault("todos")).thenReturn("todos");
        when(paginaV.getOrDefault(1)).thenReturn(1);
        when(porPaginaV.getOrDefault(25)).thenReturn(25);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleListarBusquedas
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleListarBusquedas_conRolAdmin_llamaServicioYRetornaResultado")
    void handleListarBusquedas_conRolAdmin_llamaServicioYRetornaResultado() {
        // arrange
        Object resultado = Map.of("busquedas", List.of(), "total", 0);
        when(ctx.<Integer>attribute("rolId")).thenReturn(2);
        when(ctx.queryParam("destino")).thenReturn(null);
        when(ctx.queryParam("usuarioAgencia")).thenReturn(null);
        when(ctx.queryParam("fechaDesde")).thenReturn(null);
        when(ctx.queryParam("fechaHasta")).thenReturn(null);
        mockQueryParamsDefaults();
        doReturn(resultado).when(service).listar(null, null, "todos", null, null, 1, 25);

        // act
        controller.handleListarBusquedas(ctx);

        // assert
        verify(service).listar(null, null, "todos", null, null, 1, 25);
        verify(ctx).json(resultado);
    }

    @Test
    @DisplayName("handleListarBusquedas_conRolAdmin_aplicaFiltrosDeQueryParams")
    void handleListarBusquedas_conRolAdmin_aplicaFiltrosDeQueryParams() {
        // arrange
        Object resultado = Map.of("busquedas", List.of("b1"), "total", 1);
        when(ctx.<Integer>attribute("rolId")).thenReturn(2);
        when(ctx.queryParam("destino")).thenReturn("Guatemala");
        when(ctx.queryParam("usuarioAgencia")).thenReturn("agencia1");
        when(ctx.queryParam("fechaDesde")).thenReturn("2026-01-01");
        when(ctx.queryParam("fechaHasta")).thenReturn("2026-01-31");

        @SuppressWarnings("unchecked")
        Validator<String> tipoV = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Validator<Integer> paginaV = mock(Validator.class);
        @SuppressWarnings("unchecked")
        Validator<Integer> porPaginaV = mock(Validator.class);
        when(ctx.queryParamAsClass("tipo",      String.class) ).thenReturn(tipoV);
        when(ctx.queryParamAsClass("pagina",    Integer.class)).thenReturn(paginaV);
        when(ctx.queryParamAsClass("porPagina", Integer.class)).thenReturn(porPaginaV);
        when(tipoV.getOrDefault("todos")).thenReturn("web");
        when(paginaV.getOrDefault(1)).thenReturn(2);
        when(porPaginaV.getOrDefault(25)).thenReturn(10);

        doReturn(resultado).when(service).listar("Guatemala", "agencia1", "web", "2026-01-01", "2026-01-31", 2, 10);

        // act
        controller.handleListarBusquedas(ctx);

        // assert
        verify(service).listar("Guatemala", "agencia1", "web", "2026-01-01", "2026-01-31", 2, 10);
        verify(ctx).json(resultado);
    }

    @Test
    @DisplayName("handleListarBusquedas_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleListarBusquedas_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.<Integer>attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleListarBusquedas(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).listar(any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleResumen
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleResumen_conRolAdmin_retornaResumenEstadistico")
    void handleResumen_conRolAdmin_retornaResumenEstadistico() {
        // arrange
        Object resumen = Map.of("totalWeb", 5, "totalRest", 3);
        when(ctx.<Integer>attribute("rolId")).thenReturn(2);
        doReturn(resumen).when(service).resumen();

        // act
        controller.handleResumen(ctx);

        // assert
        verify(service).resumen();
        verify(ctx).json(resumen);
    }

    @Test
    @DisplayName("handleResumen_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleResumen_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.<Integer>attribute("rolId")).thenReturn(3);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleResumen(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).resumen();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleExportar
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleExportar_conRolIncorrecto_retorna403YNoLlamaServicio")
    void handleExportar_conRolIncorrecto_retorna403YNoLlamaServicio() {
        // arrange
        when(ctx.<Integer>attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleExportar(ctx);

        // assert
        verify(ctx).status(403);
        verify(service, never()).exportar(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("handleExportar_conEmailInvalido_retorna400ConMensaje")
    void handleExportar_conEmailInvalido_retorna400ConMensaje() {
        // arrange
        when(ctx.<Integer>attribute("rolId")).thenReturn(2);
        Map<String, Object> body = new HashMap<>();
        body.put("email", "correo-sin-arroba");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        when(ctx.status(400)).thenReturn(ctx);

        // act
        controller.handleExportar(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Correo electronico invalido"));
        verify(service, never()).exportar(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("handleExportar_conEmailBlanco_retorna400ConMensaje")
    void handleExportar_conEmailBlanco_retorna400ConMensaje() {
        // arrange
        when(ctx.<Integer>attribute("rolId")).thenReturn(2);
        Map<String, Object> body = new HashMap<>();
        body.put("email", "   ");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        when(ctx.status(400)).thenReturn(ctx);

        // act
        controller.handleExportar(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Correo electronico invalido"));
        verify(service, never()).exportar(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("handleExportar_conEmailValidoSinFiltros_llamaServicioYRetornaMensaje")
    void handleExportar_conEmailValidoSinFiltros_llamaServicioYRetornaMensaje() {
        // arrange
        when(ctx.<Integer>attribute("rolId")).thenReturn(2);
        Map<String, Object> body = new HashMap<>();
        body.put("email", "admin@ejemplo.com");
        // sin clave "filtros" → usa Map.of()
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);

        // act
        controller.handleExportar(ctx);

        // assert
        verify(service).exportar("admin@ejemplo.com", null, null, "todos", null, null);
        verify(ctx).json(Map.of("mensaje", "Reporte enviado correctamente a admin@ejemplo.com"));
    }

    @Test
    @DisplayName("handleExportar_conEmailValidoYFiltros_llamaServicioConFiltros")
    void handleExportar_conEmailValidoYFiltros_llamaServicioConFiltros() {
        // arrange
        when(ctx.<Integer>attribute("rolId")).thenReturn(2);

        Map<String, Object> filtros = new HashMap<>();
        filtros.put("destino", "Antigua");
        filtros.put("usuarioAgencia", "agencia2");
        filtros.put("tipo", "rest");
        filtros.put("fechaDesde", "2026-03-01");
        filtros.put("fechaHasta", "2026-03-31");

        Map<String, Object> body = new HashMap<>();
        body.put("email", "reporte@empresa.com");
        body.put("filtros", filtros);
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);

        // act
        controller.handleExportar(ctx);

        // assert
        verify(service).exportar(
                "reporte@empresa.com",
                "Antigua",
                "agencia2",
                "rest",
                "2026-03-01",
                "2026-03-31"
        );
        verify(ctx).json(Map.of("mensaje", "Reporte enviado correctamente a reporte@empresa.com"));
    }
}
