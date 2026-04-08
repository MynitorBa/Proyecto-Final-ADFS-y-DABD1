package org.example.services;

import org.example.dtos.PagoRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.dtos.TokenValidacionResponseDTO;
import org.example.repositories.PagoRepository;
import org.example.repositories.TokenValidacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PagoService.
 * Cubre el flujo de pago sin token, con token de alianza valido,
 * token invalido, y todas las validaciones de tarjeta y estado de reservacion.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService - Pruebas unitarias")
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private TokenValidacionRepository tokenValidacionRepository;

    private PagoService service;

    /**
     * Inicializa el service con ambos repositorios simulados antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        service = new PagoService(pagoRepository, tokenValidacionRepository);
    }

    /**
     * Construye un PagoRequestDTO con datos de tarjeta validos y sin token de alianza.
     * @return request listo para los casos de prueba de pago estandar.
     */
    private PagoRequestDTO buildRequestValido() {
        PagoRequestDTO request = new PagoRequestDTO();
        request.setNumeroTarjeta("1234567890123456");
        request.setNombreTitular("Test User");
        request.setFechaVencimiento("12/30");
        request.setCvv("123");
        request.setNit("CF");
        request.setCodigoPostal("01001");
        return request;
    }

    /**
     * Construye el arreglo que el repositorio devuelve para una reservacion en estado pendiente.
     * El arreglo sigue el orden {ID, No_Reservacion, Total, Estado, EstadoID}.
     * @return arreglo con datos de reservacion pendiente.
     */
    private Object[] reservacionPendiente() {
        return new Object[]{1, "MIKU-001", 100.0, "Pendiente", 1};
    }

    // -- procesarPago sin token de alianza

    /**
     * Verifica que el pago se procese correctamente cuando la reservacion esta pendiente
     * y los datos de tarjeta son validos, sin token de alianza.
     */
    @Test
    @DisplayName("procesarPago_datosValidosSinToken_retornaPagoResponseDTO")
    void procesarPago_datosValidosSinToken_retornaPagoResponseDTO() {
        PagoRequestDTO request = buildRequestValido();
        PagoResponseDTO facturaEsperada = new PagoResponseDTO();

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacionPendiente());
        when(pagoRepository.crearFactura(5, "CF", "01001", 100.0)).thenReturn(20);
        when(pagoRepository.obtenerFactura(20)).thenReturn(facturaEsperada);

        PagoResponseDTO resultado = service.procesarPago(5, 7, request);

        assertNotNull(resultado);
        verify(pagoRepository).confirmarReservacion(5);
        verify(pagoRepository).crearFactura(5, "CF", "01001", 100.0);
        verify(pagoRepository).obtenerFactura(20);
        verifyNoInteractions(tokenValidacionRepository);
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando la reservacion no existe
     * o no pertenece al usuario indicado.
     */
    @Test
    @DisplayName("procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException")
    void procesarPago_reservacionNoEncontrada_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildRequestValido();

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("Reservacion no encontrada o no pertenece al usuario", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando la reservacion existe
     * pero su estado no permite el pago (por ejemplo, ya esta confirmada).
     */
    @Test
    @DisplayName("procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException")
    void procesarPago_estadoNoEsPendiente_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildRequestValido();
        Object[] reservacionConfirmada = new Object[]{1, "MIKU-001", 100.0, "Confirmada", 2};

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacionConfirmada);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("La reservacion no puede ser pagada, estado actual: Confirmada", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    // -- validaciones de tarjeta

    /**
     * Verifica que se lanza IllegalArgumentException cuando el numero de tarjeta
     * no tiene los 16 digitos requeridos.
     */
    @Test
    @DisplayName("procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException")
    void procesarPago_numeroTarjetaInvalido_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildRequestValido();
        request.setNumeroTarjeta("1234");

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacionPendiente());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("Numero de tarjeta invalido", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando el nombre del titular
     * esta vacio.
     */
    @Test
    @DisplayName("procesarPago_nombreTitularVacio_lanzaIllegalArgumentException")
    void procesarPago_nombreTitularVacio_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildRequestValido();
        request.setNombreTitular("");

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacionPendiente());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("Nombre del titular requerido", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando la fecha de vencimiento
     * de la tarjeta ya paso.
     */
    @Test
    @DisplayName("procesarPago_tarjetaVencida_lanzaIllegalArgumentException")
    void procesarPago_tarjetaVencida_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildRequestValido();
        request.setFechaVencimiento("01/20");

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacionPendiente());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("La tarjeta esta vencida", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando el CVV
     * no tiene el formato valido.
     */
    @Test
    @DisplayName("procesarPago_cvvInvalido_lanzaIllegalArgumentException")
    void procesarPago_cvvInvalido_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildRequestValido();
        request.setCvv("12");

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacionPendiente());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("CVV invalido", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
    }

    // -- procesarPago con token de alianza

    /**
     * Verifica que cuando se incluye un token de alianza valido se aplica el descuento,
     * se actualiza el total en la reservacion y en los detalles, y el token queda marcado como usado.
     */
    @Test
    @DisplayName("procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado")
    void procesarPago_conTokenAlianzaValido_aplicaDescuentoYMarcaTokenUsado() {
        PagoRequestDTO request = buildRequestValido();
        request.setTokenAlianza("token-valido-uuid");

        // Reservacion con total de 200.0 y token que da 10% de descuento
        Object[] reservacion = new Object[]{5, "MIKU-002", 200.0, "Pendiente", 1};
        TokenValidacionResponseDTO datosToken =
                new TokenValidacionResponseDTO("Guatemala", "Guatemala", 10.0, "2030-01-01 00:00:00");
        PagoResponseDTO facturaEsperada = new PagoResponseDTO();

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacion);
        when(tokenValidacionRepository.buscarTokenValido("token-valido-uuid")).thenReturn(datosToken);
        when(pagoRepository.crearFactura(eq(5), eq("CF"), eq("01001"), eq(180.0))).thenReturn(30);
        when(pagoRepository.obtenerFactura(30)).thenReturn(facturaEsperada);

        PagoResponseDTO resultado = service.procesarPago(5, 7, request);

        assertNotNull(resultado);
        // Verifica que se actualizo el total con el descuento del 10%
        verify(pagoRepository).actualizarTotalReservacion(5, 180.0);
        verify(pagoRepository).actualizarTotalDetalles(5, 0.9);
        verify(pagoRepository).confirmarReservacion(5);
        verify(tokenValidacionRepository).marcarTokenUsado("token-valido-uuid", 5);
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando el token de alianza
     * no existe, ya fue usado o esta expirado.
     */
    @Test
    @DisplayName("procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException")
    void procesarPago_tokenAlianzaInvalido_lanzaIllegalArgumentException() {
        PagoRequestDTO request = buildRequestValido();
        request.setTokenAlianza("token-expirado");

        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacionPendiente());
        when(tokenValidacionRepository.buscarTokenValido("token-expirado")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarPago(5, 7, request)
        );
        assertEquals("Token de alianza invalido, ya utilizado o expirado", ex.getMessage());
        verify(pagoRepository, never()).confirmarReservacion(anyInt());
        verify(tokenValidacionRepository, never()).marcarTokenUsado(anyString(), anyInt());
    }

    /**
     * Verifica que cuando el token de alianza viene en blanco se ignora
     * y el pago se procesa sin descuento, sin consultar el repositorio de tokens.
     */
    @Test
    @DisplayName("procesarPago_tokenAlianzaBlanco_procesaSinDescuento")
    void procesarPago_tokenAlianzaBlanco_procesaSinDescuento() {
        PagoRequestDTO request = buildRequestValido();
        request.setTokenAlianza("   ");

        PagoResponseDTO facturaEsperada = new PagoResponseDTO();
        when(pagoRepository.obtenerReservacionParaPago(5, 7)).thenReturn(reservacionPendiente());
        when(pagoRepository.crearFactura(5, "CF", "01001", 100.0)).thenReturn(10);
        when(pagoRepository.obtenerFactura(10)).thenReturn(facturaEsperada);

        PagoResponseDTO resultado = service.procesarPago(5, 7, request);

        assertNotNull(resultado);
        verifyNoInteractions(tokenValidacionRepository);
        verify(pagoRepository).confirmarReservacion(5);
    }
}