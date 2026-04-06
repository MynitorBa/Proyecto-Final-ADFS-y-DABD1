package org.example.services;

import org.example.dtos.ComentarioResponseDTO;
import org.example.dtos.DownResponseDTO;
import org.example.repositories.ComentarioRepository;
import org.example.repositories.DownsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DownsService.
 * Covers agregarDown, eliminarDown, actualizarDown, obtenerDownsDeUsuario,
 * and obtenerDownsDeUsuarioPorHotel.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DownsService - Unit Tests")
class DownsServiceTest {

    @Mock
    private DownsRepository downsRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    private DownsService downsService;

    @BeforeEach
    void setUp() {
        downsService = new DownsService(downsRepository, comentarioRepository);
    }

    // -- agregarDown

    @Test
    @DisplayName("agregarDown_valorInvalido_lanzaIllegalArgumentException")
    void agregarDown_valorInvalido_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downsService.agregarDown(1, 1, 0));

        assertEquals("El valor del down debe ser 1 o -1", ex.getMessage());
    }

    @Test
    @DisplayName("agregarDown_valorDos_lanzaIllegalArgumentException")
    void agregarDown_valorDos_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downsService.agregarDown(1, 1, 2));

        assertEquals("El valor del down debe ser 1 o -1", ex.getMessage());
    }

    @Test
    @DisplayName("agregarDown_comentarioNoExiste_lanzaIllegalArgumentException")
    void agregarDown_comentarioNoExiste_lanzaIllegalArgumentException() {
        when(comentarioRepository.obtenerComentario(99)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downsService.agregarDown(99, 1, 1));

        assertEquals("El comentario no existe", ex.getMessage());
    }

    @Test
    @DisplayName("agregarDown_yaExisteDown_lanzaIllegalArgumentException")
    void agregarDown_yaExisteDown_lanzaIllegalArgumentException() {
        when(comentarioRepository.obtenerComentario(1)).thenReturn(new ComentarioResponseDTO());
        when(downsRepository.obtenerValorDown(1, 1)).thenReturn(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downsService.agregarDown(1, 1, 1));

        assertEquals("Ya tienes un down en este comentario, usa actualizar", ex.getMessage());
    }

    @Test
    @DisplayName("agregarDown_valido_insertaYActualizaContador")
    void agregarDown_valido_insertaYActualizaContador() {
        when(comentarioRepository.obtenerComentario(1)).thenReturn(new ComentarioResponseDTO());
        when(downsRepository.obtenerValorDown(1, 1)).thenReturn(null);

        assertDoesNotThrow(() -> downsService.agregarDown(1, 1, 1));

        verify(downsRepository).insertarDown(1, 1, 1);
        verify(downsRepository).actualizarContadorDown(1, 1);
    }

    @Test
    @DisplayName("agregarDown_valorNegativo_insertaYActualizaContador")
    void agregarDown_valorNegativo_insertaYActualizaContador() {
        when(comentarioRepository.obtenerComentario(2)).thenReturn(new ComentarioResponseDTO());
        when(downsRepository.obtenerValorDown(1, 2)).thenReturn(null);

        assertDoesNotThrow(() -> downsService.agregarDown(2, 1, -1));

        verify(downsRepository).insertarDown(1, 2, -1);
        verify(downsRepository).actualizarContadorDown(2, -1);
    }

    // -- eliminarDown

    @Test
    @DisplayName("eliminarDown_noExisteDown_lanzaIllegalArgumentException")
    void eliminarDown_noExisteDown_lanzaIllegalArgumentException() {
        when(downsRepository.obtenerValorDown(1, 1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downsService.eliminarDown(1, 1));

        assertEquals("No tienes ningun down en este comentario", ex.getMessage());
    }

    @Test
    @DisplayName("eliminarDown_downExistente_actualizaContadorYElimina")
    void eliminarDown_downExistente_actualizaContadorYElimina() {
        when(downsRepository.obtenerValorDown(1, 1)).thenReturn(1);

        assertDoesNotThrow(() -> downsService.eliminarDown(1, 1));

        verify(downsRepository).actualizarContadorDown(1, -1);
        verify(downsRepository).eliminarDown(1, 1);
    }

    @Test
    @DisplayName("eliminarDown_downNegativoExistente_actualizaContadorConPositivo")
    void eliminarDown_downNegativoExistente_actualizaContadorConPositivo() {
        // eliminarDown(comentarioId=1, usuarioId=2) → service llama obtenerValorDown(usuarioId=2, comentarioId=1)
        when(downsRepository.obtenerValorDown(2, 1)).thenReturn(-1);

        assertDoesNotThrow(() -> downsService.eliminarDown(1, 2));

        verify(downsRepository).actualizarContadorDown(1, 1);
        verify(downsRepository).eliminarDown(2, 1);
    }

    // -- actualizarDown

    @Test
    @DisplayName("actualizarDown_valorInvalido_lanzaIllegalArgumentException")
    void actualizarDown_valorInvalido_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downsService.actualizarDown(1, 1, 5));

        assertEquals("El valor del down debe ser 1 o -1", ex.getMessage());
    }

    @Test
    @DisplayName("actualizarDown_noExisteDown_lanzaIllegalArgumentException")
    void actualizarDown_noExisteDown_lanzaIllegalArgumentException() {
        when(downsRepository.obtenerValorDown(1, 1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downsService.actualizarDown(1, 1, 1));

        assertEquals("No tienes ningun down en este comentario, usa agregar", ex.getMessage());
    }

    @Test
    @DisplayName("actualizarDown_mismoValor_lanzaIllegalArgumentException")
    void actualizarDown_mismoValor_lanzaIllegalArgumentException() {
        when(downsRepository.obtenerValorDown(1, 1)).thenReturn(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downsService.actualizarDown(1, 1, 1));

        assertEquals("El down ya tiene ese valor", ex.getMessage());
    }

    @Test
    @DisplayName("actualizarDown_valorDistinto_eliminaEInsertaNuevoDown")
    void actualizarDown_valorDistinto_eliminaEInsertaNuevoDown() {
        when(downsRepository.obtenerValorDown(1, 1)).thenReturn(1);

        assertDoesNotThrow(() -> downsService.actualizarDown(1, 1, -1));

        // actualizarContadorDown se llama dos veces: una para deshacer el valor anterior y otra para aplicar el nuevo
        verify(downsRepository, times(2)).actualizarContadorDown(1, -1);
        verify(downsRepository).eliminarDown(1, 1);
        verify(downsRepository).insertarDown(1, 1, -1);
    }

    @Test
    @DisplayName("actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente")
    void actualizarDown_deNegativoAPositivo_reemplazaDownCorrectamente() {
        when(downsRepository.obtenerValorDown(1, 1)).thenReturn(-1);

        assertDoesNotThrow(() -> downsService.actualizarDown(1, 1, 1));

        // actualizarContadorDown se llama dos veces: una para deshacer el valor anterior y otra para aplicar el nuevo
        verify(downsRepository, times(2)).actualizarContadorDown(1, 1);
        verify(downsRepository).eliminarDown(1, 1);
        verify(downsRepository).insertarDown(1, 1, 1);
    }

    // -- obtenerDownsDeUsuario

    @Test
    @DisplayName("obtenerDownsDeUsuario_usuarioConDowns_retornaLista")
    void obtenerDownsDeUsuario_usuarioConDowns_retornaLista() {
        List<DownResponseDTO> lista = List.of(new DownResponseDTO(), new DownResponseDTO());
        when(downsRepository.obtenerDownsDeUsuario(1)).thenReturn(lista);

        List<DownResponseDTO> resultado = downsService.obtenerDownsDeUsuario(1);

        assertEquals(2, resultado.size());
        verify(downsRepository).obtenerDownsDeUsuario(1);
    }

    @Test
    @DisplayName("obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia")
    void obtenerDownsDeUsuario_usuarioSinDowns_retornaListaVacia() {
        when(downsRepository.obtenerDownsDeUsuario(99)).thenReturn(List.of());

        List<DownResponseDTO> resultado = downsService.obtenerDownsDeUsuario(99);

        assertTrue(resultado.isEmpty());
    }

    // -- obtenerDownsDeUsuarioPorHotel

    @Test
    @DisplayName("obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista")
    void obtenerDownsDeUsuarioPorHotel_usuarioConDownsEnHotel_retornaLista() {
        List<DownResponseDTO> lista = List.of(new DownResponseDTO());
        when(downsRepository.obtenerDownsDeUsuarioPorHotel(1, 5)).thenReturn(lista);

        List<DownResponseDTO> resultado = downsService.obtenerDownsDeUsuarioPorHotel(1, 5);

        assertEquals(1, resultado.size());
        verify(downsRepository).obtenerDownsDeUsuarioPorHotel(1, 5);
    }

    @Test
    @DisplayName("obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia")
    void obtenerDownsDeUsuarioPorHotel_sinDownsEnHotel_retornaListaVacia() {
        when(downsRepository.obtenerDownsDeUsuarioPorHotel(1, 99)).thenReturn(List.of());

        List<DownResponseDTO> resultado = downsService.obtenerDownsDeUsuarioPorHotel(1, 99);

        assertTrue(resultado.isEmpty());
    }
}
