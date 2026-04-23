package org.example.services;

import org.example.repositories.AdminReservacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AdminReservacionService.
 * Cubre listarTodas y cancelarReservacion con todas sus variantes de estado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminReservacionService - Tests unitarios")
class AdminReservacionServiceTest {

    @Mock
    private AdminReservacionRepository repo;

    @Mock
    private AgenciaNotificadorExternoService notificadorAgencia;

    private AdminReservacionService service;

    @BeforeEach
    void setUp() {
        service = new AdminReservacionService(repo, notificadorAgencia);
    }

    // -- listarTodas

    @Test
    @DisplayName("listarTodas_repositorioRetornaLista_devuelveMismaLista")
    void listarTodas_repositorioRetornaLista_devuelveMismaLista() {
        List<Map<String, Object>> esperada = List.of(
                Map.of("id", 1, "estado", "pendiente"),
                Map.of("id", 2, "estado", "confirmada")
        );
        when(repo.listarTodas()).thenReturn(esperada);

        List<Map<String, Object>> resultado = service.listarTodas();

        assertEquals(esperada, resultado);
        verify(repo).listarTodas();
    }

    @Test
    @DisplayName("listarTodas_repositorioRetornaListaVacia_devuelveListaVacia")
    void listarTodas_repositorioRetornaListaVacia_devuelveListaVacia() {
        when(repo.listarTodas()).thenReturn(Collections.emptyList());

        List<Map<String, Object>> resultado = service.listarTodas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repo).listarTodas();
    }

    // -- cancelarReservacion

    @Test
    @DisplayName("cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException")
    void cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException() {
        when(repo.obtenerReservacion(99)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cancelarReservacion(99, "motivo cualquiera")
        );

        assertEquals("Reservacion #99 no encontrada", ex.getMessage());
        verify(repo, never()).cancelarReservacion(anyInt(), anyString());
    }

    @Test
    @DisplayName("cancelarReservacion_estadoPendiente_ejecutaCancelacion")
    void cancelarReservacion_estadoPendiente_ejecutaCancelacion() {
        // estadoId=1 (Pendiente)
        Object[] datos = {10, 1, "Pendiente"};
        when(repo.obtenerReservacion(10)).thenReturn(datos);

        service.cancelarReservacion(10, "El cliente solicito la cancelacion");

        verify(repo).cancelarReservacion(10, "El cliente solicito la cancelacion");
    }

    @Test
    @DisplayName("cancelarReservacion_estadoConfirmada_ejecutaCancelacion")
    void cancelarReservacion_estadoConfirmada_ejecutaCancelacion() {
        // estadoId=2 (Confirmada)
        Object[] datos = {20, 2, "Confirmada"};
        when(repo.obtenerReservacion(20)).thenReturn(datos);

        service.cancelarReservacion(20, "Cambio de planes");

        verify(repo).cancelarReservacion(20, "Cambio de planes");
    }

    @Test
    @DisplayName("cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException")
    void cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException() {
        // estadoId=4 (Cancelada), no puede cancelarse de nuevo
        Object[] datos = {30, 4, "Cancelada"};
        when(repo.obtenerReservacion(30)).thenReturn(datos);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cancelarReservacion(30, "motivo")
        );

        assertEquals("No se puede cancelar: estado actual es \"Cancelada\"", ex.getMessage());
        verify(repo, never()).cancelarReservacion(anyInt(), anyString());
    }

    @Test
    @DisplayName("cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException")
    void cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException() {
        // estadoId=3 (Expirada), tampoco se puede cancelar
        Object[] datos = {40, 3, "Expirada"};
        when(repo.obtenerReservacion(40)).thenReturn(datos);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cancelarReservacion(40, "motivo")
        );

        assertEquals("No se puede cancelar: estado actual es \"Expirada\"", ex.getMessage());
        verify(repo, never()).cancelarReservacion(anyInt(), anyString());
    }
}
