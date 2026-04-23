package org.example.controllers;

import io.javalin.http.Context;
import org.example.services.ImagenService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImagenController - Tests unitarios")
class ImagenControllerTest {

    @Mock private ImagenService service;
    @Mock private Context ctx;
    private ImagenController controller;

    @BeforeEach
    void setUp() {
        controller = new ImagenController(service);
    }

    // ---- handleObtenerImagenHotel ----

    @Test
    @DisplayName("handleObtenerImagenHotel_imagenExiste_retornaImagenJpeg")
    void handleObtenerImagenHotel_imagenExiste_retornaImagenJpeg() {
        // arrange
        byte[] imagen = new byte[]{1, 2, 3};
        when(ctx.pathParam("id")).thenReturn("10");
        when(service.obtenerImagenHotel(10)).thenReturn(imagen);
        when(ctx.contentType("image/jpeg")).thenReturn(ctx);

        // act
        controller.handleObtenerImagenHotel(ctx);

        // assert
        verify(ctx).contentType("image/jpeg");
        verify(ctx).result(imagen);
        verify(ctx, never()).status(404);
    }

    @Test
    @DisplayName("handleObtenerImagenHotel_imagenNoExiste_retorna404")
    void handleObtenerImagenHotel_imagenNoExiste_retorna404() {
        // arrange
        when(ctx.pathParam("id")).thenReturn("99");
        when(service.obtenerImagenHotel(99)).thenReturn(null);

        // act
        controller.handleObtenerImagenHotel(ctx);

        // assert
        verify(ctx).status(404);
        verify(ctx, never()).contentType(anyString());
    }

    // ---- handleObtenerImagenHabitacion ----

    @Test
    @DisplayName("handleObtenerImagenHabitacion_imagenExiste_retornaImagenJpeg")
    void handleObtenerImagenHabitacion_imagenExiste_retornaImagenJpeg() {
        // arrange
        byte[] imagen = new byte[]{4, 5, 6};
        when(ctx.pathParam("id")).thenReturn("20");
        when(service.obtenerImagenHabitacion(20)).thenReturn(imagen);
        when(ctx.contentType("image/jpeg")).thenReturn(ctx);

        // act
        controller.handleObtenerImagenHabitacion(ctx);

        // assert
        verify(ctx).contentType("image/jpeg");
        verify(ctx).result(imagen);
        verify(ctx, never()).status(404);
    }

    @Test
    @DisplayName("handleObtenerImagenHabitacion_imagenNoExiste_retorna404")
    void handleObtenerImagenHabitacion_imagenNoExiste_retorna404() {
        // arrange
        when(ctx.pathParam("id")).thenReturn("99");
        when(service.obtenerImagenHabitacion(99)).thenReturn(null);

        // act
        controller.handleObtenerImagenHabitacion(ctx);

        // assert
        verify(ctx).status(404);
        verify(ctx, never()).contentType(anyString());
    }

    // ---- handleObtenerImagenAmenidad ----

    @Test
    @DisplayName("handleObtenerImagenAmenidad_imagenExiste_retornaImagenJpeg")
    void handleObtenerImagenAmenidad_imagenExiste_retornaImagenJpeg() {
        // arrange
        byte[] imagen = new byte[]{7, 8, 9};
        when(ctx.pathParam("id")).thenReturn("30");
        when(service.obtenerImagenAmenidad(30)).thenReturn(imagen);
        when(ctx.contentType("image/jpeg")).thenReturn(ctx);

        // act
        controller.handleObtenerImagenAmenidad(ctx);

        // assert
        verify(ctx).contentType("image/jpeg");
        verify(ctx).result(imagen);
        verify(ctx, never()).status(404);
    }

    @Test
    @DisplayName("handleObtenerImagenAmenidad_imagenNoExiste_retorna404")
    void handleObtenerImagenAmenidad_imagenNoExiste_retorna404() {
        // arrange
        when(ctx.pathParam("id")).thenReturn("99");
        when(service.obtenerImagenAmenidad(99)).thenReturn(null);

        // act
        controller.handleObtenerImagenAmenidad(ctx);

        // assert
        verify(ctx).status(404);
        verify(ctx, never()).contentType(anyString());
    }
}
