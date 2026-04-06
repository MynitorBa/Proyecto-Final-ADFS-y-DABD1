package org.example.services;

import org.example.dtos.HotelResultadoDTO;
import org.example.repositories.DestinosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DestinosService.
 * Covers obtenerDestinos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DestinosService - Unit Tests")
class DestinosServiceTest {

    @Mock
    private DestinosRepository destinosRepository;

    private DestinosService destinosService;

    @BeforeEach
    void setUp() {
        destinosService = new DestinosService(destinosRepository);
    }

    // -- obtenerDestinos

    @Test
    @DisplayName("obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas")
    void obtenerDestinos_unHotelConImagenes_retornaListaConImagenesAsignadas() {
        HotelResultadoDTO hotel = mock(HotelResultadoDTO.class);
        when(hotel.getId()).thenReturn(1);

        List<HotelResultadoDTO> hoteles = new ArrayList<>();
        hoteles.add(hotel);

        when(destinosRepository.obtenerTodosLosHoteles()).thenReturn(hoteles);
        when(destinosRepository.obtenerImagenesHotel(1)).thenReturn(List.of(10, 20));

        List<HotelResultadoDTO> resultado = destinosService.obtenerDestinos();

        assertEquals(1, resultado.size());
        verify(destinosRepository).obtenerImagenesHotel(1);
        verify(hotel).setImagenesIds(List.of(10, 20));
    }

    @Test
    @DisplayName("obtenerDestinos_sinHoteles_retornaListaVacia")
    void obtenerDestinos_sinHoteles_retornaListaVacia() {
        when(destinosRepository.obtenerTodosLosHoteles()).thenReturn(new ArrayList<>());

        List<HotelResultadoDTO> resultado = destinosService.obtenerDestinos();

        assertTrue(resultado.isEmpty());
        verify(destinosRepository, never()).obtenerImagenesHotel(anyInt());
    }

    @Test
    @DisplayName("obtenerDestinos_variosHoteles_asignaImagenesAcadaUno")
    void obtenerDestinos_variosHoteles_asignaImagenesAcadaUno() {
        HotelResultadoDTO hotel1 = mock(HotelResultadoDTO.class);
        HotelResultadoDTO hotel2 = mock(HotelResultadoDTO.class);
        when(hotel1.getId()).thenReturn(1);
        when(hotel2.getId()).thenReturn(2);

        List<HotelResultadoDTO> hoteles = new ArrayList<>();
        hoteles.add(hotel1);
        hoteles.add(hotel2);

        when(destinosRepository.obtenerTodosLosHoteles()).thenReturn(hoteles);
        when(destinosRepository.obtenerImagenesHotel(1)).thenReturn(List.of(10));
        when(destinosRepository.obtenerImagenesHotel(2)).thenReturn(List.of(30, 40));

        List<HotelResultadoDTO> resultado = destinosService.obtenerDestinos();

        assertEquals(2, resultado.size());
        verify(hotel1).setImagenesIds(List.of(10));
        verify(hotel2).setImagenesIds(List.of(30, 40));
    }

    @Test
    @DisplayName("obtenerDestinos_hotelSinImagenes_asignaListaVacia")
    void obtenerDestinos_hotelSinImagenes_asignaListaVacia() {
        HotelResultadoDTO hotel = mock(HotelResultadoDTO.class);
        when(hotel.getId()).thenReturn(5);

        List<HotelResultadoDTO> hoteles = new ArrayList<>();
        hoteles.add(hotel);

        when(destinosRepository.obtenerTodosLosHoteles()).thenReturn(hoteles);
        when(destinosRepository.obtenerImagenesHotel(5)).thenReturn(List.of());

        List<HotelResultadoDTO> resultado = destinosService.obtenerDestinos();

        assertEquals(1, resultado.size());
        verify(hotel).setImagenesIds(List.of());
    }
}
