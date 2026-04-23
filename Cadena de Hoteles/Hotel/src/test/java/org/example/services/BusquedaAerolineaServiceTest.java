package org.example.services;

import org.example.dtos.*;
import org.example.repositories.AerolineaAliadaRepository;
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
 * Pruebas unitarias para BusquedaAerolineaService.
 * Mockea AerolineaAliadaRepository para verificar la logica de busqueda
 * con descuento de aerolinea: validacion de token, ciudad y aplicacion de descuento.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BusquedaAerolineaService - Tests unitarios")
class BusquedaAerolineaServiceTest {

    @Mock
    private AerolineaAliadaRepository repository;

    private BusquedaAerolineaService service;

    /** Request de busqueda con fechas validas para usar en todos los tests. */
    private BusquedaRequestDTO requestBase;

    @BeforeEach
    void setUp() {
        service = new BusquedaAerolineaService(repository);

        requestBase = new BusquedaRequestDTO();
        requestBase.setCiudad("Guatemala");
        requestBase.setPais("Guatemala");
        requestBase.setFechaCheckIn("2026-07-01");
        requestBase.setFechaCheckOut("2026-07-05");
        requestBase.setCantidadPersonas(2);
    }

    /**
     * Verifica que buscar con token invalido lanza IllegalArgumentException
     * sin consultar ciudad ni hoteles.
     */
    @Test
    @DisplayName("buscar_tokenInvalido_lanzaIllegalArgumentException")
    void buscar_tokenInvalido_lanzaIllegalArgumentException() {
        when(repository.obtenerDescuentoAerolinea("token-invalido")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.buscar(requestBase, "token-invalido")
        );

        assertEquals("Token invalido o aerolinea no activa", ex.getMessage());
        verify(repository, never()).buscarCiudadId(anyString(), anyString());
        verify(repository, never()).buscarHotelesPorCiudad(anyInt());
    }

    /**
     * Verifica que buscar con ciudad no registrada lanza IllegalArgumentException.
     */
    @Test
    @DisplayName("buscar_ciudadNoEncontrada_lanzaIllegalArgumentException")
    void buscar_ciudadNoEncontrada_lanzaIllegalArgumentException() {
        when(repository.obtenerDescuentoAerolinea("token-ok")).thenReturn(10.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.buscar(requestBase, "token-ok")
        );

        assertTrue(ex.getMessage().contains("Guatemala"));
        verify(repository, never()).guardarBusqueda(anyInt(), any(), any(), anyInt());
        verify(repository, never()).buscarHotelesPorCiudad(anyInt());
    }

    /**
     * Verifica que buscar con token valido y ciudad existente retorne lista (puede ser vacia).
     */
    @Test
    @DisplayName("buscar_tokenValidoCiudadExistente_retornaListaDeHoteles")
    void buscar_tokenValidoCiudadExistente_retornaListaDeHoteles() {
        when(repository.obtenerDescuentoAerolinea("token-ok")).thenReturn(10.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(1);
        doNothing().when(repository).guardarBusqueda(anyInt(), any(), any(), anyInt());
        when(repository.buscarHotelesPorCiudad(1)).thenReturn(Collections.emptyList());

        List<HotelResultadoDTO> resultado = service.buscar(requestBase, "token-ok");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository).obtenerDescuentoAerolinea("token-ok");
        verify(repository).buscarCiudadId("Guatemala", "Guatemala");
        verify(repository).guardarBusqueda(eq(1), any(), any(), eq(2));
        verify(repository).buscarHotelesPorCiudad(1);
    }

    /**
     * Verifica que buscar guarda la busqueda en el repositorio con la ciudad y personas correctas.
     */
    @Test
    @DisplayName("buscar_tokenValidoCiudadExistente_guardaBusquedaEnRepo")
    void buscar_tokenValidoCiudadExistente_guardaBusquedaEnRepo() {
        requestBase.setCantidadPersonas(4);
        when(repository.obtenerDescuentoAerolinea("token-ok")).thenReturn(5.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(3);
        doNothing().when(repository).guardarBusqueda(anyInt(), any(), any(), anyInt());
        when(repository.buscarHotelesPorCiudad(3)).thenReturn(Collections.emptyList());

        service.buscar(requestBase, "token-ok");

        verify(repository).guardarBusqueda(eq(3), any(), any(), eq(4));
    }

    /**
     * Verifica que buscar aplica el descuento de la aerolinea a los precios del hotel.
     */
    @Test
    @DisplayName("buscar_conDescuento10Porciento_aplicaDescuentoAPrecios")
    void buscar_conDescuento10Porciento_aplicaDescuentoAPrecios() {
        when(repository.obtenerDescuentoAerolinea("token-ok")).thenReturn(10.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(1);
        doNothing().when(repository).guardarBusqueda(anyInt(), any(), any(), anyInt());

        // Hotel con un tipo de habitacion
        HotelResultadoDTO hotel = new HotelResultadoDTO();
        hotel.setId(10);
        hotel.setNombre("Hotel Prueba");
        when(repository.buscarHotelesPorCiudad(1)).thenReturn(List.of(hotel));
        when(repository.buscarImagenesHotel(10)).thenReturn(Collections.emptyList());
        when(repository.buscarAmenidadesHotel(10)).thenReturn(Collections.emptyList());

        // Tipo de habitacion con precio antes de descuento
        TipoHabitacionResultadoDTO tipo = new TipoHabitacionResultadoDTO();
        tipo.setTipoHabitacionId(1);
        tipo.setPrecioPorPersona(100.0);
        tipo.setPrecioPorNoche(200.0);
        tipo.setCapacidadMaxima(2);

        when(repository.buscarTiposHabitacionDisponibles(eq(10), eq(2), any(), any()))
                .thenReturn(List.of(tipo));
        when(repository.buscarTiposHabitacionDisponibles(eq(10), eq(1), any(), any()))
                .thenReturn(Collections.emptyList());
        when(repository.buscarImagenesHabitacion(1)).thenReturn(Collections.emptyList());
        when(repository.buscarHabitacionesResumenPorTipo(eq(10), eq(1), any(), any()))
                .thenReturn(Collections.emptyList());

        List<HotelResultadoDTO> resultado = service.buscar(requestBase, "token-ok");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        // El precio deberia haberse reducido en 10%
        List<TipoHabitacionResultadoDTO> tipos = resultado.get(0).getTiposHabitacion();
        assertNotNull(tipos);
        assertEquals(1, tipos.size());
        assertEquals(90.0, tipos.get(0).getPrecioPorPersona(), 0.01);
        assertEquals(180.0, tipos.get(0).getPrecioPorNoche(), 0.01);
    }

    /**
     * Verifica que buscar con lista de hoteles retorna la lista completa sin modificar su tamano.
     */
    @Test
    @DisplayName("buscar_dosHotelesEnCiudad_retornaListaConDosHoteles")
    void buscar_dosHotelesEnCiudad_retornaListaConDosHoteles() {
        when(repository.obtenerDescuentoAerolinea("token-ok")).thenReturn(0.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(1);
        doNothing().when(repository).guardarBusqueda(anyInt(), any(), any(), anyInt());

        HotelResultadoDTO h1 = new HotelResultadoDTO();
        h1.setId(10);
        HotelResultadoDTO h2 = new HotelResultadoDTO();
        h2.setId(11);
        when(repository.buscarHotelesPorCiudad(1)).thenReturn(List.of(h1, h2));
        when(repository.buscarImagenesHotel(anyInt())).thenReturn(Collections.emptyList());
        when(repository.buscarAmenidadesHotel(anyInt())).thenReturn(Collections.emptyList());
        when(repository.buscarTiposHabitacionDisponibles(anyInt(), anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<HotelResultadoDTO> resultado = service.buscar(requestBase, "token-ok");

        assertEquals(2, resultado.size());
    }
}
