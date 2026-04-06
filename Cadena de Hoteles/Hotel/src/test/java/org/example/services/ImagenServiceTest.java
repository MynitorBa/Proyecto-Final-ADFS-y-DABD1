package org.example.services;

import org.example.repositories.ImagenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ImagenService.
 * Covers obtenerImagenHotel, obtenerImagenHabitacion, and obtenerImagenAmenidad:
 * result present and result null.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImagenService Tests")
class ImagenServiceTest {

    @Mock
    private ImagenRepository imagenRepository;

    private ImagenService service;

    @BeforeEach
    void setUp() {
        service = new ImagenService(imagenRepository);
    }

    // -- obtenerImagenHotel

    @Test
    @DisplayName("obtenerImagenHotel_imagenExiste_retornaBytesCorrectos")
    void obtenerImagenHotel_imagenExiste_retornaBytesCorrectos() {
        byte[] bytes = {1, 2, 3};
        when(imagenRepository.obtenerImagenHotel(10)).thenReturn(bytes);

        byte[] resultado = service.obtenerImagenHotel(10);

        assertArrayEquals(bytes, resultado);
        verify(imagenRepository).obtenerImagenHotel(10);
    }

    @Test
    @DisplayName("obtenerImagenHotel_imagenNoExiste_retornaNull")
    void obtenerImagenHotel_imagenNoExiste_retornaNull() {
        when(imagenRepository.obtenerImagenHotel(99)).thenReturn(null);

        byte[] resultado = service.obtenerImagenHotel(99);

        assertNull(resultado);
        verify(imagenRepository).obtenerImagenHotel(99);
    }

    // -- obtenerImagenHabitacion

    @Test
    @DisplayName("obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos")
    void obtenerImagenHabitacion_imagenExiste_retornaBytesCorrectos() {
        byte[] bytes = {4, 5, 6};
        when(imagenRepository.obtenerImagenHabitacion(20)).thenReturn(bytes);

        byte[] resultado = service.obtenerImagenHabitacion(20);

        assertArrayEquals(bytes, resultado);
        verify(imagenRepository).obtenerImagenHabitacion(20);
    }

    @Test
    @DisplayName("obtenerImagenHabitacion_imagenNoExiste_retornaNull")
    void obtenerImagenHabitacion_imagenNoExiste_retornaNull() {
        when(imagenRepository.obtenerImagenHabitacion(88)).thenReturn(null);

        byte[] resultado = service.obtenerImagenHabitacion(88);

        assertNull(resultado);
        verify(imagenRepository).obtenerImagenHabitacion(88);
    }

    // -- obtenerImagenAmenidad

    @Test
    @DisplayName("obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos")
    void obtenerImagenAmenidad_imagenExiste_retornaBytesCorrectos() {
        byte[] bytes = {7, 8, 9};
        when(imagenRepository.obtenerImagenAmenidad(30)).thenReturn(bytes);

        byte[] resultado = service.obtenerImagenAmenidad(30);

        assertArrayEquals(bytes, resultado);
        verify(imagenRepository).obtenerImagenAmenidad(30);
    }

    @Test
    @DisplayName("obtenerImagenAmenidad_imagenNoExiste_retornaNull")
    void obtenerImagenAmenidad_imagenNoExiste_retornaNull() {
        when(imagenRepository.obtenerImagenAmenidad(77)).thenReturn(null);

        byte[] resultado = service.obtenerImagenAmenidad(77);

        assertNull(resultado);
        verify(imagenRepository).obtenerImagenAmenidad(77);
    }
}
