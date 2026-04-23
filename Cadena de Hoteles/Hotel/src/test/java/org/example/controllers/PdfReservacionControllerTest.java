package org.example.controllers;

import io.javalin.http.Context;
import org.example.services.PdfReservacionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfReservacionController - Tests unitarios")
class PdfReservacionControllerTest {

    @Mock private PdfReservacionService service;
    @Mock private Context ctx;
    private PdfReservacionController controller;

    @BeforeEach
    void setUp() {
        controller = new PdfReservacionController(service);
    }

    @Test
    @DisplayName("handleDescargarPdf_reservacionValida_retornaPdfComoAdjunto")
    void handleDescargarPdf_reservacionValida_retornaPdfComoAdjunto() {
        // arrange
        byte[] pdf = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
        when(ctx.attribute("usuarioId")).thenReturn(3);
        when(ctx.pathParam("id")).thenReturn("8");
        when(service.generarPdf(8, 3)).thenReturn(pdf);
        when(ctx.contentType("application/pdf")).thenReturn(ctx);
        when(ctx.header("Content-Disposition", "attachment; filename=\"MIKU-8.pdf\"")).thenReturn(ctx);

        // act
        controller.handleDescargarPdf(ctx);

        // assert
        verify(ctx).contentType("application/pdf");
        verify(ctx).header("Content-Disposition", "attachment; filename=\"MIKU-8.pdf\"");
        verify(ctx).result(pdf);
        verify(ctx, never()).status(anyInt());
    }

    @Test
    @DisplayName("handleDescargarPdf_reservacionNoEncontrada_retorna404")
    void handleDescargarPdf_reservacionNoEncontrada_retorna404() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(3);
        when(ctx.pathParam("id")).thenReturn("999");
        when(service.generarPdf(999, 3))
                .thenThrow(new IllegalArgumentException("Reservacion no existe"));
        when(ctx.status(404)).thenReturn(ctx);

        // act
        controller.handleDescargarPdf(ctx);

        // assert
        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Reservacion no existe"));
        verify(ctx, never()).contentType(anyString());
    }
}
