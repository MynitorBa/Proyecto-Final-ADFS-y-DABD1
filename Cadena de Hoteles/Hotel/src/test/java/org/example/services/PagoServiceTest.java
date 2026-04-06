package org.example.services;

import org.example.dtos.PagoRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.repositories.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PagoService.
 * Covers procesarPago: success with valid card data, reservacion no encontrada,
 * estado no permite pago, tarjeta invalida (numero, nombre, fecha, CVV).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService Tests")
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    private PagoService service;

    @BeforeEach
    void setUp() {
        service = new PagoService(pagoRepository);
    }

    /**
     * Builds a PagoRequestDTO with valid card data ready to pass TarjetaHelper.validar.
     */
    private PagoRequestDTO buildValidRequest() {
        PagoRequestDTO request = new PagoRequestDTO();
        request.setNumeroTarjeta("1234567890123456");
        request.setNombreTitular("Test User");
        request.setFechaVencimiento("12/30");
        request.setCvv("123");
        request.setNit("CF");
        request.setCodigoPostal("01001");
        return request;
    }

    // -- procesarPago

    @Test
    @DisplayName("procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO")
    void procesarPago_datosValidosEstadoPendiente_retornaPagoResponseDTO() {
        PagoRequestDTO request = buildValidRequest();
        Object[] reservacion = new Object[]{1, "MIKU-001", 100.0, "Pendiente", 1};
        PagoResponseDTO facturaEsperada = new PagoResponseDTO();

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacion);
        when(pagoRepository.crearFactura(5, "CF", "01001", 100.0)).thenReturn(20);
        when(pagoRepository.obtenerFactura(20)).thenReturn(facturaEsperada);

        PagoResponseDTO resultado = service.procesarPago(5, 7, request);

        assertNotNull(resultado);
        verify(pagoRepository).confirmarReservacion(5);
        verify(pagoRepository).crearFactura(5, "CF", "01001", 100.0);
        verify(pagoRepository).obtenerFactura(20);
    }

    @Test
    @DisplayName("procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException")
    void procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildValidRequest();

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("Reservacion no encontrada o no pertenece al usuario", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    @Test
    @DisplayName("procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException")
    void procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildValidRequest();
        Object[] reservacion = new Object[]{1, "MIKU-001", 100.0, "Confirmada", 2};

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacion);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("La reservacion no puede ser pagada, estado actual: Confirmada", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    @Test
    @DisplayName("procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException")
    void procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildValidRequest();
        request.setNumeroTarjeta("1234");

        Object[] reservacion = new Object[]{1, "MIKU-001", 100.0, "Pendiente", 1};
        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacion);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("Numero de tarjeta invalido", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    @Test
    @DisplayName("procesarPago_nombreTitularVacio_lanzaIllegalArgumentException")
    void procesarPago_nombreTitularVacio_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildValidRequest();
        request.setNombreTitular("");

        Object[] reservacion = new Object[]{1, "MIKU-001", 100.0, "Pendiente", 1};
        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacion);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("Nombre del titular requerido", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    @Test
    @DisplayName("procesarPago_tarjetaVencida_lanzaIllegalArgumentException")
    void procesarPago_tarjetaVencida_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildValidRequest();
        request.setFechaVencimiento("01/20");

        Object[] reservacion = new Object[]{1, "MIKU-001", 100.0, "Pendiente", 1};
        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacion);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("La tarjeta esta vencida", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    @Test
    @DisplayName("procesarPago_cvvInvalido_lanzaIllegalArgumentException")
    void procesarPago_cvvInvalido_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildValidRequest();
        request.setCvv("12");

        Object[] reservacion = new Object[]{1, "MIKU-001", 100.0, "Pendiente", 1};
        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacion);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("CVV invalido", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }
}
