package org.example.services;

import org.example.repositories.AdminBusquedaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AdminBusquedaService.
 * Cubre listar (paginado con filtros), resumen de dashboard y exportar.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminBusquedaService - Tests unitarios")
class AdminBusquedaServiceTest {

    @Mock
    private AdminBusquedaRepository repo;

    private AdminBusquedaService service;

    @BeforeEach
    void setUp() {
        service = new AdminBusquedaService(repo);
    }

    // -- listar

    @Test
    @DisplayName("listar_sinFiltros_retornaMapaConBusquedasYTotal")
    void listar_sinFiltros_retornaMapaConBusquedasYTotal() {
        List<Map<String, Object>> listaEsperada = List.of(Map.of("id", 1, "destino", "Guatemala"));
        when(repo.listar(isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(listaEsperada);
        when(repo.contar(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(1);

        Map<String, Object> resultado = service.listar(null, null, null, null, null, 1, 10);

        assertNotNull(resultado);
        assertEquals(listaEsperada, resultado.get("busquedas"));
        assertEquals(1, resultado.get("total"));
        verify(repo).listar(isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10));
        verify(repo).contar(isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("listar_conTipoWeb_pasaTipoBusquedaId1")
    void listar_conTipoWeb_pasaTipoBusquedaId1() {
        when(repo.listar(eq("Antigua"), isNull(), eq(1), isNull(), isNull(), eq(0), eq(5)))
                .thenReturn(Collections.emptyList());
        when(repo.contar(eq("Antigua"), isNull(), eq(1), isNull(), isNull()))
                .thenReturn(0);

        Map<String, Object> resultado = service.listar("Antigua", null, "web", null, null, 1, 5);

        assertNotNull(resultado);
        assertEquals(0, resultado.get("total"));
        verify(repo).listar(eq("Antigua"), isNull(), eq(1), isNull(), isNull(), eq(0), eq(5));
        verify(repo).contar(eq("Antigua"), isNull(), eq(1), isNull(), isNull());
    }

    @Test
    @DisplayName("listar_conTipoRest_pasaTipoBusquedaId2")
    void listar_conTipoRest_pasaTipoBusquedaId2() {
        when(repo.listar(isNull(), isNull(), eq(2), isNull(), isNull(), eq(10), eq(10)))
                .thenReturn(Collections.emptyList());
        when(repo.contar(isNull(), isNull(), eq(2), isNull(), isNull()))
                .thenReturn(0);

        Map<String, Object> resultado = service.listar(null, null, "rest", null, null, 2, 10);

        assertNotNull(resultado);
        verify(repo).listar(isNull(), isNull(), eq(2), isNull(), isNull(), eq(10), eq(10));
    }

    @Test
    @DisplayName("listar_conTipoTodos_pasaTipoBusquedaIdNull")
    void listar_conTipoTodos_pasaTipoBusquedaIdNull() {
        when(repo.listar(isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(Collections.emptyList());
        when(repo.contar(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(0);

        Map<String, Object> resultado = service.listar(null, null, "todos", null, null, 1, 10);

        assertNotNull(resultado);
        verify(repo).listar(isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10));
    }

    @Test
    @DisplayName("listar_conFechasValidas_parseaCorrectamenteLasFechas")
    void listar_conFechasValidas_parseaCorrectamenteLasFechas() {
        Date expectedDesde = Date.valueOf("2025-01-01");
        Date expectedHasta = Date.valueOf("2025-12-31");

        when(repo.listar(isNull(), isNull(), isNull(), eq(expectedDesde), eq(expectedHasta), eq(0), eq(10)))
                .thenReturn(Collections.emptyList());
        when(repo.contar(isNull(), isNull(), isNull(), eq(expectedDesde), eq(expectedHasta)))
                .thenReturn(0);

        Map<String, Object> resultado = service.listar(null, null, null, "2025-01-01", "2025-12-31", 1, 10);

        assertNotNull(resultado);
        verify(repo).listar(isNull(), isNull(), isNull(), eq(expectedDesde), eq(expectedHasta), eq(0), eq(10));
    }

    @Test
    @DisplayName("listar_conFechasInvalidas_trataNullLasFechas")
    void listar_conFechasInvalidas_trataNullLasFechas() {
        when(repo.listar(isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(Collections.emptyList());
        when(repo.contar(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(0);

        Map<String, Object> resultado = service.listar(null, null, null, "no-es-fecha", "tampoco", 1, 10);

        assertNotNull(resultado);
        verify(repo).listar(isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10));
    }

    @Test
    @DisplayName("listar_pagina3ConPorPagina5_calculaOffsetCorrecto")
    void listar_pagina3ConPorPagina5_calculaOffsetCorrecto() {
        // offset = (3-1)*5 = 10
        when(repo.listar(isNull(), isNull(), isNull(), isNull(), isNull(), eq(10), eq(5)))
                .thenReturn(Collections.emptyList());
        when(repo.contar(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(0);

        service.listar(null, null, null, null, null, 3, 5);

        verify(repo).listar(isNull(), isNull(), isNull(), isNull(), isNull(), eq(10), eq(5));
    }

    // -- resumen

    @Test
    @DisplayName("resumen_llamadoNormal_retornaMapaConTodasLasClaves")
    void resumen_llamadoNormal_retornaMapaConTodasLasClaves() {
        when(repo.contarPorTipo(1)).thenReturn(42);
        when(repo.contarPorTipo(2)).thenReturn(18);
        when(repo.busquedasPorDia()).thenReturn(List.of(Map.of("dia", "2025-01-01", "total", 5)));
        when(repo.topDestinos()).thenReturn(List.of(Map.of("nombre", "Antigua", "total", 10)));

        Map<String, Object> resultado = service.resumen();

        assertNotNull(resultado);
        assertEquals(42, resultado.get("totalWeb"));
        assertEquals(18, resultado.get("totalRest"));
        assertNotNull(resultado.get("porDia"));
        assertNotNull(resultado.get("topDestinos"));
    }

    @Test
    @DisplayName("resumen_llamadoNormal_invocaRepoConArgumentosCorrectos")
    void resumen_llamadoNormal_invocaRepoConArgumentosCorrectos() {
        when(repo.contarPorTipo(1)).thenReturn(0);
        when(repo.contarPorTipo(2)).thenReturn(0);
        when(repo.busquedasPorDia()).thenReturn(Collections.emptyList());
        when(repo.topDestinos()).thenReturn(Collections.emptyList());

        service.resumen();

        verify(repo).contarPorTipo(1);
        verify(repo).contarPorTipo(2);
        verify(repo).busquedasPorDia();
        verify(repo).topDestinos();
    }

    // -- exportar

    @Test
    @DisplayName("exportar_conFiltros_invocaRepoExportarYEnviaCorreo")
    void exportar_conFiltros_invocaRepoExportarYEnviaCorreo() {
        when(repo.exportar(eq("Guatemala"), isNull(), eq(1), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                service.exportar("test@test.com", "Guatemala", null, "web", null, null)
        );

        verify(repo).exportar(eq("Guatemala"), isNull(), eq(1), isNull(), isNull());
    }

    @Test
    @DisplayName("exportar_sinFiltros_invocaRepoExportarConNulls")
    void exportar_sinFiltros_invocaRepoExportarConNulls() {
        when(repo.exportar(isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                service.exportar("destinatario@dominio.com", null, null, "todos", null, null)
        );

        verify(repo).exportar(isNull(), isNull(), isNull(), isNull(), isNull());
    }
}
