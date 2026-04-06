package org.example.services;

import org.example.dtos.BusquedaRequestDTO;
import org.example.dtos.HotelResultadoDTO;
import org.example.repositories.BusquedaAgenciaRepository;
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
 * Tests unitarios para BusquedaAgenciaService.
 * Cubre buscar (por usuarioId) y buscarPorToken con casos de error y exito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BusquedaAgenciaService - Tests unitarios")
class BusquedaAgenciaServiceTest {

    @Mock
    private BusquedaAgenciaRepository repository;

    private BusquedaAgenciaService service;

    @BeforeEach
    void setUp() {
        service = new BusquedaAgenciaService(repository);
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
    @DisplayName("buscar_sinAgenciaActiva_lanzaIllegalArgumentException")
    void buscar_sinAgenciaActiva_lanzaIllegalArgumentException() {
        when(repository.obtenerDescuentoAgencia(10)).thenReturn(null);

        BusquedaRequestDTO req = requestValido();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.buscar(req, 10)
        );

        assertEquals("El usuario no tiene una agencia activa asociada", ex.getMessage());
        verify(repository, never()).buscarCiudadId(anyString(), anyString());
    }

    @Test
    @DisplayName("buscar_ciudadNoEncontrada_lanzaIllegalArgumentException")
    void buscar_ciudadNoEncontrada_lanzaIllegalArgumentException() {
        when(repository.obtenerDescuentoAgencia(10)).thenReturn(15.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(null);

        BusquedaRequestDTO req = requestValido();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.buscar(req, 10)
        );

        assertTrue(ex.getMessage().contains("No se encontro la ciudad 'Guatemala'"));
        assertTrue(ex.getMessage().contains("en el pais 'Guatemala'"));
    }

    @Test
    @DisplayName("buscar_parametrosValidos_retornaListaDeHoteles")
    void buscar_parametrosValidos_retornaListaDeHoteles() {
        when(repository.obtenerDescuentoAgencia(10)).thenReturn(10.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(1);
        when(repository.buscarHotelesPorCiudad(1)).thenReturn(Collections.emptyList());

        BusquedaRequestDTO req = requestValido();

        List<HotelResultadoDTO> resultado = service.buscar(req, 10);

        assertNotNull(resultado);
        verify(repository).buscarHotelesPorCiudad(1);
    }

    @Test
    @DisplayName("buscar_parametrosValidos_guardaBusquedaEnRepo")
    void buscar_parametrosValidos_guardaBusquedaEnRepo() {
        when(repository.obtenerDescuentoAgencia(7)).thenReturn(5.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(1);
        when(repository.buscarHotelesPorCiudad(1)).thenReturn(Collections.emptyList());

        BusquedaRequestDTO req = requestValido();

        service.buscar(req, 7);

        verify(repository).guardarBusqueda(eq(1), any(), any(), eq(2), eq(7));
    }

    // -- buscarPorToken

    @Test
    @DisplayName("buscarPorToken_tokenInvalido_lanzaIllegalArgumentException")
    void buscarPorToken_tokenInvalido_lanzaIllegalArgumentException() {
        when(repository.obtenerDescuentoAgenciaPorToken("token-invalido")).thenReturn(null);

        BusquedaRequestDTO req = requestValido();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.buscarPorToken(req, "token-invalido")
        );

        assertEquals("Token invalido o agencia no activa", ex.getMessage());
        verify(repository, never()).buscarCiudadId(anyString(), anyString());
    }

    @Test
    @DisplayName("buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException")
    void buscarPorToken_ciudadNoEncontrada_lanzaIllegalArgumentException() {
        when(repository.obtenerDescuentoAgenciaPorToken("token-valido")).thenReturn(20.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(null);

        BusquedaRequestDTO req = requestValido();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.buscarPorToken(req, "token-valido")
        );

        assertTrue(ex.getMessage().contains("No se encontro la ciudad 'Guatemala'"));
        assertTrue(ex.getMessage().contains("en el pais 'Guatemala'"));
    }

    @Test
    @DisplayName("buscarPorToken_parametrosValidos_retornaListaDeHoteles")
    void buscarPorToken_parametrosValidos_retornaListaDeHoteles() {
        when(repository.obtenerDescuentoAgenciaPorToken("abc123")).thenReturn(10.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(1);
        when(repository.buscarHotelesPorCiudad(1)).thenReturn(Collections.emptyList());

        BusquedaRequestDTO req = requestValido();

        List<HotelResultadoDTO> resultado = service.buscarPorToken(req, "abc123");

        assertNotNull(resultado);
        verify(repository).buscarHotelesPorCiudad(1);
    }

    @Test
    @DisplayName("buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario")
    void buscarPorToken_parametrosValidos_guardaBusquedaSinUsuario() {
        when(repository.obtenerDescuentoAgenciaPorToken("miToken")).thenReturn(0.0);
        when(repository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(1);
        when(repository.buscarHotelesPorCiudad(1)).thenReturn(Collections.emptyList());

        BusquedaRequestDTO req = requestValido();

        service.buscarPorToken(req, "miToken");

        verify(repository).guardarBusquedaSinUsuario(eq(1), any(), any(), eq(2));
    }
}
