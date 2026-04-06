package org.example.services;

import org.example.dtos.PagoAgenciaRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.repositories.PagoAgenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PagoAgenciaService.
 * Covers procesarPago: success, NIT blank, codigo postal blank,
 * reservacion no encontrada, and estado no permite pago.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PagoAgenciaService Tests")
class PagoAgenciaServiceTest {

    @Mock
    private PagoAgenciaRepository pagoRepository;

    private PagoAgenciaService service;

    @BeforeEach
    void setUp() {
        service = new PagoAgenciaService(pagoRepository);
    }

    // -- procesarPago

    @Test
    @DisplayName("procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO")
    void procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO() {
        PagoAgenciaRequestDTO request = new PagoAgenciaRequestDTO();
        request.setNit("12345678");
        request.setCodigoPostal("01001");

        Object[] reservacion = new Object[]{1, "MIKU-001", 100.0, "Pendiente", 1};
        PagoResponseDTO facturaEsperada = new PagoResponseDTO();

        when(pagoRepository.obtenerReservacionParaPago(10, 3)).thenReturn(reservacion);
        when(pagoRepository.crearFactura(10, "12345678", "01001", 100.0)).thenReturn(55);
        when(pagoRepository.obtenerFactura(55)).thenReturn(facturaEsperada);

        PagoResponseDTO resultado = service.procesarPago(10, 3, request);

        assertNotNull(resultado);
        verify(pagoRepository).confirmarReservacion(10);
        verify(pagoRepository).crearFactura(10, "12345678", "01001", 100.0);
        verify(pagoRepository).obtenerFactura(55);
    }

    @Test
    @DisplayName("procesarPago_nitNulo_lanzaIllegalArgumentException")
    void procesarPago_nitNulo_lanzaIllegalArgumentException() {
        PagoAgenciaRequestDTO request = new PagoAgenciaRequestDTO();
        request.setNit(null);
        request.setCodigoPostal("01001");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(10, 3, request)
        );
        assertEquals("El NIT es requerido. Si no tienes, ingresa 'CF'.", ex.getMessage());
        verify(pagoRepository, never()).obtenerReservacionParaPago(anyInt(), anyInt());
    }

    @Test
    @DisplayName("procesarPago_nitBlanco_lanzaIllegalArgumentException")
    void procesarPago_nitBlanco_lanzaIllegalArgumentException() {
        PagoAgenciaRequestDTO request = new PagoAgenciaRequestDTO();
        request.setNit("   ");
        request.setCodigoPostal("01001");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(10, 3, request)
        );
        assertEquals("El NIT es requerido. Si no tienes, ingresa 'CF'.", ex.getMessage());
        verify(pagoRepository, never()).obtenerReservacionParaPago(anyInt(), anyInt());
    }

    @Test
    @DisplayName("procesarPago_codigoPostalNulo_lanzaIllegalArgumentException")
    void procesarPago_codigoPostalNulo_lanzaIllegalArgumentException() {
        PagoAgenciaRequestDTO request = new PagoAgenciaRequestDTO();
        request.setNit("CF");
        request.setCodigoPostal(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(10, 3, request)
        );
        assertEquals("El codigo postal es requerido.", ex.getMessage());
        verify(pagoRepository, never()).obtenerReservacionParaPago(anyInt(), anyInt());
    }

    @Test
    @DisplayName("procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException")
    void procesarPago_codigoPostalBlanco_lanzaIllegalArgumentException() {
        PagoAgenciaRequestDTO request = new PagoAgenciaRequestDTO();
        request.setNit("CF");
        request.setCodigoPostal("   ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(10, 3, request)
        );
        assertEquals("El codigo postal es requerido.", ex.getMessage());
        verify(pagoRepository, never()).obtenerReservacionParaPago(anyInt(), anyInt());
    }

    @Test
    @DisplayName("procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException")
    void procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException() {
        PagoAgenciaRequestDTO request = new PagoAgenciaRequestDTO();
        request.setNit("CF");
        request.setCodigoPostal("01001");

        when(pagoRepository.obtenerReservacionParaPago(10, 3)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(10, 3, request)
        );
        assertEquals("Reservacion no encontrada o no pertenece a esta agencia.", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    @Test
    @DisplayName("procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException")
    void procesarPago_estadoNoPermitePago_lanzaIllegalArgumentException() {
        PagoAgenciaRequestDTO request = new PagoAgenciaRequestDTO();
        request.setNit("CF");
        request.setCodigoPostal("01001");

        Object[] reservacion = new Object[]{1, "MIKU-001", 100.0, "Confirmada", 2};
        when(pagoRepository.obtenerReservacionParaPago(10, 3)).thenReturn(reservacion);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(10, 3, request)
        );
        assertEquals("La reservacion no puede ser pagada, estado actual: Confirmada", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }
}
