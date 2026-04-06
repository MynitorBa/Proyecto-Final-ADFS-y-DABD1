package org.example.services;

import org.example.dtos.AgenciaDTO;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;
import org.example.repositories.AgenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AgenciaService.
 * Cubre listarPorUsuario, crear, cambiarEstado, eliminar, listarTodas y editar.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgenciaService - Tests unitarios")
class AgenciaServiceTest {

    @Mock
    private AgenciaRepository repo;

    private AgenciaService service;

    @BeforeEach
    void setUp() {
        service = new AgenciaService(repo);
    }

    // -- listarPorUsuario

    @Test
    @DisplayName("listarPorUsuario_conUsuarioValido_retornaListaDelRepo")
    void listarPorUsuario_conUsuarioValido_retornaListaDelRepo() {
        AgenciaDTO dto = new AgenciaDTO();
        dto.setId(1);
        dto.setNombre("Agencia Test");
        List<AgenciaDTO> esperada = List.of(dto);

        when(repo.listarPorUsuario(5)).thenReturn(esperada);

        List<AgenciaDTO> resultado = service.listarPorUsuario(5);

        assertEquals(esperada, resultado);
        verify(repo).listarPorUsuario(5);
    }

    @Test
    @DisplayName("listarPorUsuario_sinAgencias_retornaListaVacia")
    void listarPorUsuario_sinAgencias_retornaListaVacia() {
        when(repo.listarPorUsuario(99)).thenReturn(Collections.emptyList());

        List<AgenciaDTO> resultado = service.listarPorUsuario(99);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repo).listarPorUsuario(99);
    }

    // -- crear

    @Test
    @DisplayName("crear_conRequestValido_retornaAgenciaCreadaDelRepo")
    void crear_conRequestValido_retornaAgenciaCreadaDelRepo() {
        CrearAgenciaRequestDTO req = new CrearAgenciaRequestDTO();
        AgenciaDTO creada = new AgenciaDTO();
        creada.setId(10);
        creada.setNombre("Nueva Agencia");

        when(repo.crear(3, req)).thenReturn(creada);

        AgenciaDTO resultado = service.crear(3, req);

        assertEquals(creada, resultado);
        verify(repo).crear(3, req);
    }

    // -- cambiarEstado

    @Test
    @DisplayName("cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos")
    void cambiarEstado_estadoActivo_invocaRepoConParametrosCorrectos() {
        service.cambiarEstado(1, 5, 1);

        verify(repo).cambiarEstado(1, 5, 1);
    }

    @Test
    @DisplayName("cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos")
    void cambiarEstado_estadoCerrado_invocaRepoConParametrosCorrectos() {
        service.cambiarEstado(2, 5, 2);

        verify(repo).cambiarEstado(2, 5, 2);
    }

    @Test
    @DisplayName("cambiarEstado_estadoInvalido_lanzaIllegalArgumentException")
    void cambiarEstado_estadoInvalido_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cambiarEstado(1, 5, 99)
        );

        assertEquals("Estado invalido. Use 1 (Activo) o 2 (Cerrado)", ex.getMessage());
        verify(repo, never()).cambiarEstado(anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("cambiarEstado_estadoCero_lanzaIllegalArgumentException")
    void cambiarEstado_estadoCero_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cambiarEstado(1, 5, 0)
        );

        assertEquals("Estado invalido. Use 1 (Activo) o 2 (Cerrado)", ex.getMessage());
        verify(repo, never()).cambiarEstado(anyInt(), anyInt(), anyInt());
    }

    // -- eliminar

    @Test
    @DisplayName("eliminar_conParametrosValidos_delegaAlRepo")
    void eliminar_conParametrosValidos_delegaAlRepo() {
        service.eliminar(10, 3);

        verify(repo).eliminar(10, 3);
    }

    // -- listarTodas

    @Test
    @DisplayName("listarTodas_retornaListaCompleta")
    void listarTodas_retornaListaCompleta() {
        AgenciaDTO a1 = new AgenciaDTO();
        a1.setId(1);
        AgenciaDTO a2 = new AgenciaDTO();
        a2.setId(2);
        List<AgenciaDTO> esperada = List.of(a1, a2);

        when(repo.listarTodas()).thenReturn(esperada);

        List<AgenciaDTO> resultado = service.listarTodas();

        assertEquals(2, resultado.size());
        verify(repo).listarTodas();
    }

    @Test
    @DisplayName("listarTodas_sinAgencias_retornaListaVacia")
    void listarTodas_sinAgencias_retornaListaVacia() {
        when(repo.listarTodas()).thenReturn(Collections.emptyList());

        List<AgenciaDTO> resultado = service.listarTodas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repo).listarTodas();
    }

    // -- editar

    @Test
    @DisplayName("editar_conRequestValido_delegaAlRepo")
    void editar_conRequestValido_delegaAlRepo() {
        EditarAgenciaRequestDTO req = new EditarAgenciaRequestDTO();

        service.editar(5, req);

        verify(repo).editar(5, req);
    }

}
