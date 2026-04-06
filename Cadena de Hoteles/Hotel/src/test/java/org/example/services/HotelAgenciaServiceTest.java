package org.example.services;

import org.example.dtos.HotelAgenciaDTO;
import org.example.repositories.HotelAgenciaRepository;
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
 * Unit tests for HotelAgenciaService.
 * Covers the obtenerHotelesParaAgencia method: list with elements and empty list.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HotelAgenciaService Tests")
class HotelAgenciaServiceTest {

    @Mock
    private HotelAgenciaRepository repository;

    private HotelAgenciaService service;

    @BeforeEach
    void setUp() {
        service = new HotelAgenciaService(repository);
    }

    // -- obtenerHotelesParaAgencia

    @Test
    @DisplayName("obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento")
    void obtenerHotelesParaAgencia_repositorioRetornaUnElemento_retornaListaConUnElemento() {
        HotelAgenciaDTO dto = new HotelAgenciaDTO();
        when(repository.listarHotelesParaAgencia()).thenReturn(List.of(dto));

        List<HotelAgenciaDTO> resultado = service.obtenerHotelesParaAgencia();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(repository).listarHotelesParaAgencia();
    }

    @Test
    @DisplayName("obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia")
    void obtenerHotelesParaAgencia_repositorioRetornaListaVacia_retornaListaVacia() {
        when(repository.listarHotelesParaAgencia()).thenReturn(Collections.emptyList());

        List<HotelAgenciaDTO> resultado = service.obtenerHotelesParaAgencia();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository).listarHotelesParaAgencia();
    }
}
