package org.example.services;

import org.example.dtos.AerolineaWebserviceDTO;
import org.example.dtos.CrearAerolineaRequestDTO;
import org.example.repositories.AerolineaWebserviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AerolineaWebserviceService.
 * Mockea AerolineaWebserviceRepository para aislar la logica del portal
 * webservice de aerolineas aliadas, incluyendo la validacion de estados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AerolineaWebserviceService - Tests unitarios")
class AerolineaWebserviceServiceTest {

    @Mock
    private AerolineaWebserviceRepository repo;

    private AerolineaWebserviceService service;

    @BeforeEach
    void setUp() {
        service = new AerolineaWebserviceService(repo);
    }

    // -- listarPorUsuario

    /**
     * Verifica que listarPorUsuario retorne las aerolineas del usuario indicado.
     */
    @Test
    @DisplayName("listarPorUsuario_usuarioConAerolineas_retornaListaDelRepo")
    void listarPorUsuario_usuarioConAerolineas_retornaListaDelRepo() {
        AerolineaWebserviceDTO dto = new AerolineaWebserviceDTO();
        dto.setId(1);
        dto.setNombre("AerolineasWS Test");
        List<AerolineaWebserviceDTO> esperada = List.of(dto);

        when(repo.listarPorUsuario(8)).thenReturn(esperada);

        List<AerolineaWebserviceDTO> resultado = service.listarPorUsuario(8);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(esperada, resultado);
        verify(repo).listarPorUsuario(8);
    }

    /**
     * Verifica que listarPorUsuario retorne lista vacia cuando el usuario no tiene aerolineas.
     */
    @Test
    @DisplayName("listarPorUsuario_usuarioSinAerolineas_retornaListaVacia")
    void listarPorUsuario_usuarioSinAerolineas_retornaListaVacia() {
        when(repo.listarPorUsuario(99)).thenReturn(Collections.emptyList());

        List<AerolineaWebserviceDTO> resultado = service.listarPorUsuario(99);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repo).listarPorUsuario(99);
    }

    // -- crear

    /**
     * Verifica que crear delegue al repositorio y retorne el DTO de la aerolinea creada.
     */
    @Test
    @DisplayName("crear_requestValido_retornaAerolineaCreadaDelRepo")
    void crear_requestValido_retornaAerolineaCreadaDelRepo() {
        CrearAerolineaRequestDTO req = new CrearAerolineaRequestDTO();
        req.setNombre("Mi Aerolinea WS");
        req.setUrl("https://miaerolinea.com/api");
        req.setUrlParaUsuario("https://miaerolinea.com");

        AerolineaWebserviceDTO creada = new AerolineaWebserviceDTO();
        creada.setId(5);
        creada.setNombre("Mi Aerolinea WS");
        creada.setUsuarioWebis(8);
        creada.setEstadoId(1);
        creada.setEstado("Activo");

        when(repo.crear(8, req)).thenReturn(creada);

        AerolineaWebserviceDTO resultado = service.crear(8, req);

        assertNotNull(resultado);
        assertEquals(5, resultado.getId());
        assertEquals("Activo", resultado.getEstado());
        verify(repo).crear(8, req);
    }

    // -- cambiarEstado

    /**
     * Verifica que cambiarEstado con estado Activo (1) invoca al repositorio correctamente.
     */
    @Test
    @DisplayName("cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos")
    void cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos() {
        service.cambiarEstado(3, 8, 1);

        verify(repo).cambiarEstado(3, 8, 1);
    }

    /**
     * Verifica que cambiarEstado con estado Cerrado (2) invoca al repositorio correctamente.
     */
    @Test
    @DisplayName("cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos")
    void cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos() {
        service.cambiarEstado(3, 8, 2);

        verify(repo).cambiarEstado(3, 8, 2);
    }

    /**
     * Verifica que cambiarEstado con estado invalido lanza IllegalArgumentException
     * sin llegar a invocar al repositorio.
     */
    @Test
    @DisplayName("cambiarEstado_estadoInvalido_lanzaIllegalArgumentExceptionSinInvocarRepo")
    void cambiarEstado_estadoInvalido_lanzaIllegalArgumentExceptionSinInvocarRepo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cambiarEstado(3, 8, 99)
        );

        assertEquals("Estado invalido. Use 1 (Activo) o 2 (Cerrado)", ex.getMessage());
        verify(repo, never()).cambiarEstado(anyInt(), anyInt(), anyInt());
    }

    /**
     * Verifica que cambiarEstado con estado cero (0) lanza IllegalArgumentException.
     */
    @Test
    @DisplayName("cambiarEstado_estadoCero_lanzaIllegalArgumentException")
    void cambiarEstado_estadoCero_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cambiarEstado(3, 8, 0)
        );

        assertEquals("Estado invalido. Use 1 (Activo) o 2 (Cerrado)", ex.getMessage());
        verify(repo, never()).cambiarEstado(anyInt(), anyInt(), anyInt());
    }
}
