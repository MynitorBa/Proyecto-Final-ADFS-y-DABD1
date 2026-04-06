package org.example.services;

import org.example.dtos.ReservacionDetalleDTO;
import org.example.repositories.PdfReservacionRepository;
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
 * Unit tests for EmailReservacionService.
 * Covers guard conditions in enviarCorreoReservacion.
 * The happy path reaches EmailHelper.enviar which throws RuntimeException
 * due to no real SMTP context; this is expected and verified.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailReservacionService - Unit Tests")
class EmailReservacionServiceTest {

    @Mock
    private PdfReservacionRepository repository;

    private EmailReservacionService emailReservacionService;

    @BeforeEach
    void setUp() {
        emailReservacionService = new EmailReservacionService(repository);
    }

    // -- enviarCorreoReservacion

    @Test
    @DisplayName("enviarCorreoReservacion_noPerteneceeAlUsuario_lanzaIllegalArgumentException")
    void enviarCorreoReservacion_noPerteneceAlUsuario_lanzaIllegalArgumentException() {
        when(repository.perteneceAlUsuario(1, 1)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailReservacionService.enviarCorreoReservacion(1, 1));

        assertEquals("Reservacion no encontrada", ex.getMessage());
        verify(repository, never()).obtenerCorreoUsuario(anyInt());
    }

    @Test
    @DisplayName("enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException")
    void enviarCorreoReservacion_correoNulo_lanzaIllegalArgumentException() {
        when(repository.perteneceAlUsuario(1, 1)).thenReturn(true);
        when(repository.obtenerCorreoUsuario(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailReservacionService.enviarCorreoReservacion(1, 1));

        assertEquals("No se encontro el correo del usuario", ex.getMessage());
        verify(repository, never()).obtenerDetalles(anyInt());
    }

    @Test
    @DisplayName("enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException")
    void enviarCorreoReservacion_detallesVacios_lanzaIllegalArgumentException() {
        when(repository.perteneceAlUsuario(1, 1)).thenReturn(true);
        when(repository.obtenerCorreoUsuario(1)).thenReturn("usuario@correo.com");
        when(repository.obtenerDetalles(1)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailReservacionService.enviarCorreoReservacion(1, 1));

        assertEquals("La reservacion no tiene detalles", ex.getMessage());
    }

    @Test
    @DisplayName("enviarCorreoReservacion_datosValidos_completaSinExcepcion")
    void enviarCorreoReservacion_datosValidos_completaSinExcepcion() {
        when(repository.perteneceAlUsuario(1, 1)).thenReturn(true);
        when(repository.obtenerCorreoUsuario(1)).thenReturn("usuario@correo.com");

        ReservacionDetalleDTO detalle = mock(ReservacionDetalleDTO.class);
        when(repository.obtenerDetalles(1)).thenReturn(List.of(detalle));
        // factura: [0]=id, [1]=fechaEmision, [2]=NIT, [3]=codigoPostal, [4]=total
        when(repository.obtenerFactura(1)).thenReturn(new Object[]{1, "2025-01-01", "CF", "01001", 200.0});

        assertDoesNotThrow(() -> emailReservacionService.enviarCorreoReservacion(1, 1));

        verify(repository).obtenerDetalles(1);
    }

    @Test
    @DisplayName("enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo")
    void enviarCorreoReservacion_reservacionDistintaUsuario_noConsultaCorreo() {
        when(repository.perteneceAlUsuario(5, 99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> emailReservacionService.enviarCorreoReservacion(5, 99));

        assertEquals("Reservacion no encontrada", ex.getMessage());
        verify(repository, never()).obtenerCorreoUsuario(anyInt());
        verify(repository, never()).obtenerDetalles(anyInt());
    }
}
