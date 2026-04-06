package org.example.services;

import org.example.dtos.ComentarioRequestDTO;
import org.example.dtos.ComentarioResponseDTO;
import org.example.repositories.ComentarioRepository;
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
 * Unit tests for ComentarioService.
 * Covers agregarComentario, obtenerComentariosPorUsuario, and obtenerComentariosPorHotel.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComentarioService - Unit Tests")
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    private ComentarioService comentarioService;

    @BeforeEach
    void setUp() {
        comentarioService = new ComentarioService(comentarioRepository);
    }

    // -- agregarComentario

    @Test
    @DisplayName("agregarComentario_contenidoNulo_lanzaIllegalArgumentException")
    void agregarComentario_contenidoNulo_lanzaIllegalArgumentException() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.agregarComentario(request, 1));

        assertEquals("El contenido no puede estar vacio", ex.getMessage());
    }

    @Test
    @DisplayName("agregarComentario_contenidoBlanco_lanzaIllegalArgumentException")
    void agregarComentario_contenidoBlanco_lanzaIllegalArgumentException() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.agregarComentario(request, 1));

        assertEquals("El contenido no puede estar vacio", ex.getMessage());
    }

    @Test
    @DisplayName("agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException")
    void agregarComentario_contenidoSuperaLimite_lanzaIllegalArgumentException() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("a".repeat(501));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.agregarComentario(request, 1));

        assertEquals("El contenido no puede superar 500 caracteres", ex.getMessage());
    }

    @Test
    @DisplayName("agregarComentario_respuestaConResena_lanzaIllegalArgumentException")
    void agregarComentario_respuestaConResena_lanzaIllegalArgumentException() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("Mi respuesta");
        request.setComentarioPadreId(5);
        request.setResena(4);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.agregarComentario(request, 1));

        assertEquals("Las respuestas a comentarios no llevan resena", ex.getMessage());
    }

    @Test
    @DisplayName("agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException")
    void agregarComentario_comentarioPrincipalSinResena_lanzaIllegalArgumentException() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("Mi comentario");
        request.setComentarioPadreId(null);
        request.setResena(null);
        request.setHotelId(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.agregarComentario(request, 1));

        assertEquals("Los comentarios de hotel requieren una resena (1-5 estrellas)", ex.getMessage());
    }

    @Test
    @DisplayName("agregarComentario_resenaMenorACero_lanzaIllegalArgumentException")
    void agregarComentario_resenaMenorACero_lanzaIllegalArgumentException() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("Mi comentario");
        request.setComentarioPadreId(null);
        request.setResena(0);
        request.setHotelId(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.agregarComentario(request, 1));

        assertEquals("La resena debe ser entre 1 y 5 estrellas", ex.getMessage());
    }

    @Test
    @DisplayName("agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException")
    void agregarComentario_resenaMayorACinco_lanzaIllegalArgumentException() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("Mi comentario");
        request.setComentarioPadreId(null);
        request.setResena(6);
        request.setHotelId(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.agregarComentario(request, 1));

        assertEquals("La resena debe ser entre 1 y 5 estrellas", ex.getMessage());
    }

    @Test
    @DisplayName("agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException")
    void agregarComentario_yaExisteResenaEnHotel_lanzaIllegalArgumentException() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("Mi comentario");
        request.setComentarioPadreId(null);
        request.setResena(5);
        request.setHotelId(1);

        when(comentarioRepository.existeComentarioConResena(1, 1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comentarioService.agregarComentario(request, 1));

        assertEquals("Ya tienes un comentario con resena en este hotel", ex.getMessage());
    }

    @Test
    @DisplayName("agregarComentario_comentarioPrincipalValido_retornaComentario")
    void agregarComentario_comentarioPrincipalValido_retornaComentario() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("Excelente hotel");
        request.setComentarioPadreId(null);
        request.setResena(5);
        request.setHotelId(1);

        when(comentarioRepository.existeComentarioConResena(1, 1)).thenReturn(false);
        when(comentarioRepository.crearComentario(1, 1, null, 5, "Excelente hotel")).thenReturn(10);
        ComentarioResponseDTO responseDTO = new ComentarioResponseDTO();
        when(comentarioRepository.obtenerComentario(10)).thenReturn(responseDTO);

        ComentarioResponseDTO resultado = comentarioService.agregarComentario(request, 1);

        assertNotNull(resultado);
        verify(comentarioRepository).actualizarRatingHotel(1);
    }

    @Test
    @DisplayName("agregarComentario_respuestaValida_noActualizaRating")
    void agregarComentario_respuestaValida_noActualizaRating() {
        ComentarioRequestDTO request = new ComentarioRequestDTO();
        request.setContenido("Gracias por tu comentario");
        request.setComentarioPadreId(3);
        request.setResena(null);
        request.setHotelId(1);

        when(comentarioRepository.crearComentario(1, 1, 3, null, "Gracias por tu comentario")).thenReturn(20);
        ComentarioResponseDTO responseDTO = new ComentarioResponseDTO();
        when(comentarioRepository.obtenerComentario(20)).thenReturn(responseDTO);

        ComentarioResponseDTO resultado = comentarioService.agregarComentario(request, 1);

        assertNotNull(resultado);
        verify(comentarioRepository, never()).actualizarRatingHotel(anyInt());
    }

    // -- obtenerComentariosPorUsuario

    @Test
    @DisplayName("obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista")
    void obtenerComentariosPorUsuario_usuarioConComentarios_retornaLista() {
        List<ComentarioResponseDTO> lista = List.of(new ComentarioResponseDTO(), new ComentarioResponseDTO());
        when(comentarioRepository.obtenerComentariosPorUsuario(1)).thenReturn(lista);

        List<ComentarioResponseDTO> resultado = comentarioService.obtenerComentariosPorUsuario(1);

        assertEquals(2, resultado.size());
        verify(comentarioRepository).obtenerComentariosPorUsuario(1);
    }

    @Test
    @DisplayName("obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia")
    void obtenerComentariosPorUsuario_usuarioSinComentarios_retornaListaVacia() {
        when(comentarioRepository.obtenerComentariosPorUsuario(99)).thenReturn(List.of());

        List<ComentarioResponseDTO> resultado = comentarioService.obtenerComentariosPorUsuario(99);

        assertTrue(resultado.isEmpty());
    }

    // -- obtenerComentariosPorHotel

    @Test
    @DisplayName("obtenerComentariosPorHotel_hotelConComentarios_retornaLista")
    void obtenerComentariosPorHotel_hotelConComentarios_retornaLista() {
        List<ComentarioResponseDTO> lista = List.of(new ComentarioResponseDTO());
        when(comentarioRepository.obtenerComentariosPorHotel(5)).thenReturn(lista);

        List<ComentarioResponseDTO> resultado = comentarioService.obtenerComentariosPorHotel(5);

        assertEquals(1, resultado.size());
        verify(comentarioRepository).obtenerComentariosPorHotel(5);
    }

    @Test
    @DisplayName("obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia")
    void obtenerComentariosPorHotel_hotelSinComentarios_retornaListaVacia() {
        when(comentarioRepository.obtenerComentariosPorHotel(99)).thenReturn(List.of());

        List<ComentarioResponseDTO> resultado = comentarioService.obtenerComentariosPorHotel(99);

        assertTrue(resultado.isEmpty());
    }
}
