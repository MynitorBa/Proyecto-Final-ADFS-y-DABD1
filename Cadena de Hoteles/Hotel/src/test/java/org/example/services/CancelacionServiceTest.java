package org.example.services;

import org.example.dtos.PuedeCancelarDTO;
import org.example.repositories.CancelacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CancelacionService.
 * Covers cancelarReservacion, puedeCancelar, and cancelarReservacionAgencia.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancelacionService - Unit Tests")
class CancelacionServiceTest {

    @Mock
    private CancelacionRepository cancelacionRepository;

    private CancelacionService cancelacionService;

    @BeforeEach
    void setUp() {
        cancelacionService = new CancelacionService(cancelacionRepository);
    }

    // -- cancelarReservacion

    @Test
    @DisplayName("cancelarReservacion_reservacionNula_lanzaIllegalArgumentException")
    void cancelarReservacion_reservacionNula_lanzaIllegalArgumentException() {
        when(cancelacionRepository.obtenerReservacionParaCancelar(1, 1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacion(1, 1, "motivo"));

        assertEquals("Reservacion no encontrada o no pertenece al usuario", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException")
    void cancelarReservacion_estadoNoPermite_lanzaIllegalArgumentException() {
        Object[] reservacion = new Object[]{1, 3, "Completada"};
        when(cancelacionRepository.obtenerReservacionParaCancelar(1, 1)).thenReturn(reservacion);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacion(1, 1, "motivo"));

        assertEquals("La reservacion no puede cancelarse, estado actual: Completada", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException")
    void cancelarReservacion_sinHabitaciones_lanzaIllegalArgumentException() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionParaCancelar(1, 1)).thenReturn(reservacion);
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacion(1, 1, "motivo"));

        assertEquals("La reservacion no tiene habitaciones asociadas", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException")
    void cancelarReservacion_menosDe24Horas_lanzaIllegalArgumentException() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionParaCancelar(1, 1)).thenReturn(reservacion);
        Date fechaHoy = Date.valueOf(LocalDate.now());
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(fechaHoy);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacion(1, 1, "motivo"));

        assertEquals("No se puede cancelar con menos de 24 horas de anticipacion al check-in", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente")
    void cancelarReservacion_estadoPendienteCheckInPasado_cancelaExitosamente() {
        // estadoId == 1 (Pendiente): the 24h rule does NOT apply
        Object[] reservacion = new Object[]{1, 1, "Pendiente"};
        when(cancelacionRepository.obtenerReservacionParaCancelar(1, 1)).thenReturn(reservacion);
        Date fechaPasada = Date.valueOf(LocalDate.now().minusDays(1));
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(fechaPasada);

        assertDoesNotThrow(() -> cancelacionService.cancelarReservacion(1, 1, "motivo"));
        verify(cancelacionRepository).cancelarReservacion(1, "motivo");
    }

    @Test
    @DisplayName("cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente")
    void cancelarReservacion_estadoConfirmadoFuturoLejano_cancelaExitosamente() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionParaCancelar(1, 1)).thenReturn(reservacion);
        Date fechaFutura = Date.valueOf(LocalDate.now().plusDays(2));
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(fechaFutura);

        assertDoesNotThrow(() -> cancelacionService.cancelarReservacion(1, 1, "motivo"));
        verify(cancelacionRepository).cancelarReservacion(1, "motivo");
    }

    // -- puedeCancelar

    @Test
    @DisplayName("puedeCancelar_reservacionNula_retornaFalso")
    void puedeCancelar_reservacionNula_retornaFalso() {
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(null);

        PuedeCancelarDTO resultado = cancelacionService.puedeCancelar(1, 99);

        assertFalse(resultado.isPuedeCancelar());
        assertEquals("Reservacion no encontrada o no pertenece a esta agencia", resultado.getRazon());
    }

    @Test
    @DisplayName("puedeCancelar_estadoNoPermite_retornaFalso")
    void puedeCancelar_estadoNoPermite_retornaFalso() {
        Object[] reservacion = new Object[]{1, 3, "Completada"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);

        PuedeCancelarDTO resultado = cancelacionService.puedeCancelar(1, 99);

        assertFalse(resultado.isPuedeCancelar());
        assertEquals("Estado actual no permite cancelacion: Completada", resultado.getRazon());
    }

    @Test
    @DisplayName("puedeCancelar_estadoPendiente_retornaVerdadero")
    void puedeCancelar_estadoPendiente_retornaVerdadero() {
        Object[] reservacion = new Object[]{1, 1, "Pendiente"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);

        PuedeCancelarDTO resultado = cancelacionService.puedeCancelar(1, 99);

        assertTrue(resultado.isPuedeCancelar());
        assertEquals("Reservacion pendiente, puede cancelarse", resultado.getRazon());
    }

    @Test
    @DisplayName("puedeCancelar_confirmadaSinHabitaciones_retornaFalso")
    void puedeCancelar_confirmadaSinHabitaciones_retornaFalso() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(null);

        PuedeCancelarDTO resultado = cancelacionService.puedeCancelar(1, 99);

        assertFalse(resultado.isPuedeCancelar());
        assertEquals("La reservacion no tiene habitaciones asociadas", resultado.getRazon());
    }

    @Test
    @DisplayName("puedeCancelar_confirmadaMenosDe24Horas_retornaFalso")
    void puedeCancelar_confirmadaMenosDe24Horas_retornaFalso() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);
        Date fechaHoy = Date.valueOf(LocalDate.now());
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(fechaHoy);

        PuedeCancelarDTO resultado = cancelacionService.puedeCancelar(1, 99);

        assertFalse(resultado.isPuedeCancelar());
        assertEquals("No se puede cancelar con menos de 24 horas de anticipacion al check-in", resultado.getRazon());
    }

    @Test
    @DisplayName("puedeCancelar_confirmadaFuturoLejano_retornaVerdadero")
    void puedeCancelar_confirmadaFuturoLejano_retornaVerdadero() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);
        Date fechaFutura = Date.valueOf(LocalDate.now().plusDays(10));
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(fechaFutura);

        PuedeCancelarDTO resultado = cancelacionService.puedeCancelar(1, 99);

        assertTrue(resultado.isPuedeCancelar());
        assertTrue(resultado.getRazon().startsWith("Puede cancelarse. Faltan "));
    }

    // -- cancelarReservacionAgencia

    @Test
    @DisplayName("cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException")
    void cancelarReservacionAgencia_reservacionNula_lanzaIllegalArgumentException() {
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacionAgencia(1, 99, "motivo"));

        assertEquals("Reservacion no encontrada o no pertenece a esta agencia", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException")
    void cancelarReservacionAgencia_estadoNoPermite_lanzaIllegalArgumentException() {
        Object[] reservacion = new Object[]{1, 4, "Cancelada"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacionAgencia(1, 99, "motivo"));

        assertEquals("La reservacion no puede cancelarse, estado actual: Cancelada", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException")
    void cancelarReservacionAgencia_sinHabitaciones_lanzaIllegalArgumentException() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacionAgencia(1, 99, "motivo"));

        assertEquals("La reservacion no tiene habitaciones asociadas", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException")
    void cancelarReservacionAgencia_menosDe24Horas_lanzaIllegalArgumentException() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);
        Date fechaHoy = Date.valueOf(LocalDate.now());
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(fechaHoy);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacionAgencia(1, 99, "motivo"));

        assertEquals("No se puede cancelar con menos de 24 horas de anticipacion al check-in", ex.getMessage());
    }

    @Test
    @DisplayName("cancelarReservacionAgencia_valida_cancelaExitosamente")
    void cancelarReservacionAgencia_valida_cancelaExitosamente() {
        Object[] reservacion = new Object[]{1, 2, "Confirmada"};
        when(cancelacionRepository.obtenerReservacionAgenciaParaCancelar(1, 99)).thenReturn(reservacion);
        Date fechaFutura = Date.valueOf(LocalDate.now().plusDays(5));
        when(cancelacionRepository.obtenerFechaCheckInMasReciente(1)).thenReturn(fechaFutura);

        assertDoesNotThrow(() -> cancelacionService.cancelarReservacionAgencia(1, 99, "motivo agencia"));
        verify(cancelacionRepository).cancelarReservacion(1, "motivo agencia");
    }
}
