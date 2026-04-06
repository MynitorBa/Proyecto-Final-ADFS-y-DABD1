package org.example.services;

import org.example.repositories.ReservacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExpiracionService.
 * Covers constructor wiring, iniciar, and detener.
 * The scheduled task runs every 1 minute so expirarReservaciones
 * is not called immediately; its logic is tested indirectly via
 * service construction and lifecycle methods.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExpiracionService - Unit Tests")
class ExpiracionServiceTest {

    @Mock
    private ReservacionRepository reservacionRepository;

    private ExpiracionService expiracionService;

    @BeforeEach
    void setUp() {
        expiracionService = new ExpiracionService(reservacionRepository);
    }

    // -- constructor

    @Test
    @DisplayName("constructor_repositoryValido_noLanzaExcepcion")
    void constructor_repositoryValido_noLanzaExcepcion() {
        assertDoesNotThrow(() -> new ExpiracionService(reservacionRepository));
    }

    // -- iniciar

    @Test
    @DisplayName("iniciar_noLanzaExcepcion")
    void iniciar_noLanzaExcepcion() {
        assertDoesNotThrow(() -> expiracionService.iniciar());
        expiracionService.detener();
    }

    @Test
    @DisplayName("iniciar_llamadaMultiple_noLanzaExcepcion")
    void iniciar_llamadaMultiple_noLanzaExcepcion() {
        // Calling iniciar twice schedules twice but should not throw
        assertDoesNotThrow(() -> {
            expiracionService.iniciar();
            expiracionService.iniciar();
        });
        expiracionService.detener();
    }

    // -- detener

    @Test
    @DisplayName("detener_noLanzaExcepcion")
    void detener_noLanzaExcepcion() {
        expiracionService.iniciar();
        assertDoesNotThrow(() -> expiracionService.detener());
    }

    @Test
    @DisplayName("detener_sinIniciarPreviamente_noLanzaExcepcion")
    void detener_sinIniciarPreviamente_noLanzaExcepcion() {
        // Scheduler is created inline; shutdown on a fresh scheduler should not throw
        assertDoesNotThrow(() -> expiracionService.detener());
    }

    @Test
    @DisplayName("iniciar_luegoDdetener_cicloCompletoCorrecto")
    void iniciar_luegoDdetener_cicloCompletoCorrecto() {
        assertDoesNotThrow(() -> {
            expiracionService.iniciar();
            expiracionService.detener();
        });
    }

    // -- expirarReservaciones (logica interna)

    /**
     * Verifica que expirarReservaciones invoca expirarReservacionesVencidas en el repositorio.
     * Se usa reflexion para acceder al metodo privado sin modificar el codigo de produccion.
     */
    @Test
    @DisplayName("expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio")
    void expirarReservaciones_cuandoEsInvocado_llamaAlRepositorio() throws Exception {
        when(reservacionRepository.expirarReservacionesVencidas()).thenReturn(0);

        Method metodo = ExpiracionService.class.getDeclaredMethod("expirarReservaciones");
        metodo.setAccessible(true);
        metodo.invoke(expiracionService);

        verify(reservacionRepository).expirarReservacionesVencidas();
    }
}
