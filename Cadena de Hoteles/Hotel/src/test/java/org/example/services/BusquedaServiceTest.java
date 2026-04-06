package org.example.services;

import org.example.dtos.BusquedaRequestDTO;
import org.example.dtos.HotelResultadoDTO;
import org.example.repositories.BusquedaRepository;
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
 * Tests unitarios para BusquedaService.
 * Cubre buscar con ciudad inexistente, exito con usuario autenticado
 * y exito sin usuario (busqueda anonima).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BusquedaService - Tests unitarios")
class BusquedaServiceTest {

    @Mock
    private BusquedaRepository busquedaRepository;

    private BusquedaService service;

    @BeforeEach
    void setUp() {
        service = new BusquedaService(busquedaRepository);
    }

    /** Construye un BusquedaRequestDTO con datos validos para las pruebas de exito. */
    private BusquedaRequestDTO requestValido() {
        BusquedaRequestDTO req = new BusquedaRequestDTO();
        req.setCiudad("Guatemala");
        req.setPais("Guatemala");
        req.setFechaCheckIn("2025-12-01");
        req.setFechaCheckOut("2025-12-05");
        req.setCantidadPersonas(2);
        return req;
    }

    // -- buscar

    @Test
    @DisplayName("buscar_ciudadNoEncontrada_lanzaIllegalArgumentException")
    void buscar_ciudadNoEncontrada_lanzaIllegalArgumentException() {
        when(busquedaRepository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(null);

        BusquedaRequestDTO req = requestValido();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.buscar(req, 1)
        );

        assertTrue(ex.getMessage().contains("No se encontro la ciudad 'Guatemala'"));
        assertTrue(ex.getMessage().contains("en el pais 'Guatemala'"));
        verify(busquedaRepository, never()).buscarHotelesPorCiudad(anyInt());
    }

    @Test
    @DisplayName("buscar_ciudadNoEncontrada_noInvocaBuscarHoteles")
    void buscar_ciudadNoEncontrada_noInvocaBuscarHoteles() {
        when(busquedaRepository.buscarCiudadId(anyString(), anyString())).thenReturn(null);

        BusquedaRequestDTO req = requestValido();

        assertThrows(IllegalArgumentException.class, () -> service.buscar(req, null));

        verify(busquedaRepository, never()).guardarBusqueda(anyInt(), any(), any(), anyInt(), any());
        verify(busquedaRepository, never()).buscarHotelesPorCiudad(anyInt());
    }

    @Test
    @DisplayName("buscar_ciudadEncontrada_retornaListaDeHoteles")
    void buscar_ciudadEncontrada_retornaListaDeHoteles() {
        when(busquedaRepository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(5);
        when(busquedaRepository.buscarHotelesPorCiudad(5)).thenReturn(Collections.emptyList());

        BusquedaRequestDTO req = requestValido();

        List<HotelResultadoDTO> resultado = service.buscar(req, 1);

        assertNotNull(resultado);
        verify(busquedaRepository).buscarHotelesPorCiudad(5);
    }

    @Test
    @DisplayName("buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId")
    void buscar_conUsuarioAutenticado_guardaBusquedaConUsuarioId() {
        when(busquedaRepository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(5);
        when(busquedaRepository.buscarHotelesPorCiudad(5)).thenReturn(Collections.emptyList());

        BusquedaRequestDTO req = requestValido();

        service.buscar(req, 42);

        verify(busquedaRepository).guardarBusqueda(eq(5), any(), any(), eq(2), eq(42));
    }

    @Test
    @DisplayName("buscar_sinUsuario_guardaBusquedaConUsuarioIdNull")
    void buscar_sinUsuario_guardaBusquedaConUsuarioIdNull() {
        when(busquedaRepository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(5);
        when(busquedaRepository.buscarHotelesPorCiudad(5)).thenReturn(Collections.emptyList());

        BusquedaRequestDTO req = requestValido();

        service.buscar(req, null);

        verify(busquedaRepository).guardarBusqueda(eq(5), any(), any(), eq(2), isNull());
    }

    @Test
    @DisplayName("buscar_ciudadEncontradaConHoteles_retornaListaNoVacia")
    void buscar_ciudadEncontradaConHoteles_retornaListaNoVacia() {
        HotelResultadoDTO hotel = new HotelResultadoDTO();
        hotel.setId(100);
        hotel.setNombre("Hotel Central");

        when(busquedaRepository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(5);
        when(busquedaRepository.buscarHotelesPorCiudad(5)).thenReturn(List.of(hotel));
        // El service itera sobre cada hotel y llama a metodos adicionales del repo
        when(busquedaRepository.buscarImagenesHotel(100)).thenReturn(Collections.emptyList());
        when(busquedaRepository.buscarAmenidadesHotel(100)).thenReturn(Collections.emptyList());
        when(busquedaRepository.buscarTiposHabitacionDisponibles(eq(100), eq(2), any(), any()))
                .thenReturn(Collections.emptyList());
        when(busquedaRepository.buscarTiposHabitacionDisponibles(eq(100), eq(1), any(), any()))
                .thenReturn(Collections.emptyList());

        BusquedaRequestDTO req = requestValido();

        List<HotelResultadoDTO> resultado = service.buscar(req, 1);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(100, resultado.get(0).getId());
    }
}
