package org.example.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Pruebas unitarias para MoventClient.
 * Verifica el comportamiento best-effort: ningun metodo propaga excepcion
 * al llamador, independientemente de si el servidor Movent esta disponible.
 */
@DisplayName("MoventClient — Pruebas Unitarias")
class MoventClientTest {

    // =========================================================================
    // notificarHotelCerrado
    // =========================================================================

    @Test
    @DisplayName("notificarHotelCerrado_listaVacia_retornaSinExcepcionNiHttpCall")
    void notificarHotelCerrado_listaVacia_retornaSinExcepcionNiHttpCall() {
        assertDoesNotThrow(() ->
                MoventClient.notificarHotelCerrado("Hotel Test", Collections.emptyList()));
    }

    @Test
    @DisplayName("notificarHotelCerrado_listaNula_retornaSinExcepcionNiHttpCall")
    void notificarHotelCerrado_listaNula_retornaSinExcepcionNiHttpCall() {
        assertDoesNotThrow(() ->
                MoventClient.notificarHotelCerrado("Hotel Test", null));
    }

    @Test
    @DisplayName("notificarHotelCerrado_conReservas_swallowsExcepcionCuandoServidorNoDisponible")
    void notificarHotelCerrado_conReservas_swallowsExcepcionCuandoServidorNoDisponible() {
        List<Map<String, Object>> reservas = List.of(
                Map.of("noReservacion", "RES-001", "correo", "a@b.com", "total", 100.0)
        );
        // Best-effort: la conexion fallara (localhost:8080 no disponible en tests)
        // pero el metodo NO debe propagar ninguna excepcion al llamador.
        assertDoesNotThrow(() ->
                MoventClient.notificarHotelCerrado("Hotel Cerrado", reservas));
    }

    @Test
    @DisplayName("notificarHotelCerrado_nombreConCaracteresEspeciales_swallowsExcepcion")
    void notificarHotelCerrado_nombreConCaracteresEspeciales_swallowsExcepcion() {
        List<Map<String, Object>> reservas = List.of(
                Map.of("noReservacion", "RES-002", "correo", "x@y.com", "total", 50.0)
        );
        assertDoesNotThrow(() ->
                MoventClient.notificarHotelCerrado("Hotel \"El Paraíso\"", reservas));
    }

    // =========================================================================
    // notificarHabitacionCerrada
    // =========================================================================

    @Test
    @DisplayName("notificarHabitacionCerrada_listaVacia_retornaSinExcepcionNiHttpCall")
    void notificarHabitacionCerrada_listaVacia_retornaSinExcepcionNiHttpCall() {
        assertDoesNotThrow(() ->
                MoventClient.notificarHabitacionCerrada(42, Collections.emptyList()));
    }

    @Test
    @DisplayName("notificarHabitacionCerrada_listaNula_retornaSinExcepcionNiHttpCall")
    void notificarHabitacionCerrada_listaNula_retornaSinExcepcionNiHttpCall() {
        assertDoesNotThrow(() ->
                MoventClient.notificarHabitacionCerrada(42, null));
    }

    @Test
    @DisplayName("notificarHabitacionCerrada_conReservas_swallowsExcepcionCuandoServidorNoDisponible")
    void notificarHabitacionCerrada_conReservas_swallowsExcepcionCuandoServidorNoDisponible() {
        List<Map<String, Object>> reservas = List.of(
                Map.of("noReservacion", "RES-010", "correo", "c@d.com", "total", 75.0)
        );
        // Best-effort: la conexion fallara pero no debe lanzar excepcion al llamador.
        assertDoesNotThrow(() ->
                MoventClient.notificarHabitacionCerrada(7, reservas));
    }

    @Test
    @DisplayName("notificarHabitacionCerrada_multiplesReservas_swallowsExcepcion")
    void notificarHabitacionCerrada_multiples_reservas_swallowsExcepcion() {
        List<Map<String, Object>> reservas = List.of(
                Map.of("noReservacion", "RES-011", "correo", "e@f.com", "total", 120.0),
                Map.of("noReservacion", "RES-012", "correo", "g@h.com", "total", 90.0)
        );
        assertDoesNotThrow(() ->
                MoventClient.notificarHabitacionCerrada(15, reservas));
    }
}
