package org.example.services;

import org.example.dtos.ResultadoNotificacionDTO;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AdminReservacionService.
 * Cubre listarTodas y cancelarReservacion con todas sus variantes de estado,
 * incluyendo la verificacion de la llamada al notificador externo de agencia
 * y la captura del ResultadoNotificacionDTO retornado.
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

    /**
     * Verifica que listarTodas retorne la misma lista que devuelve el repositorio.
     */
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

    /**
     * Verifica que listarTodas retorne lista vacia cuando el repositorio no tiene datos.
     */
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

    /**
     * Verifica que cancelarReservacion lanza excepcion cuando la reservacion no existe.
     */
    @Test
    @DisplayName("cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException")
    void cancelarReservacion_reservacionNoExiste_lanzaIllegalArgumentException() {
        when(repo.obtenerReservacion(99)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cancelarReservacion(99, "motivo cualquiera")
        );

        assertEquals("Reservacion #99 no encontrada", ex.getMessage());
        verify(repo, never()).cancelarReservacion(anyInt(), anyString());
        verify(notificadorAgencia, never()).notificarCancelacion(anyInt(), anyString());
    }

    /**
     * Verifica que cancelarReservacion en estado Pendiente ejecuta la cancelacion,
     * invoca al notificador de agencia y retorna el DTO de notificacion.
     */
    @Test
    @DisplayName("cancelarReservacion_estadoPendiente_ejecutaCancelacionYNotificaAgencia")
    void cancelarReservacion_estadoPendiente_ejecutaCancelacionYNotificaAgencia() {
        Object[] datos = {10, 1, "Pendiente"};
        when(repo.obtenerReservacion(10)).thenReturn(datos);
        when(repo.obtenerDatosUsuarioPorReservacion(10)).thenReturn(null); // omite correo

        ResultadoNotificacionDTO dtoEsperado = new ResultadoNotificacionDTO();
        dtoEsperado.setEsReservaDeAgencia(false);
        when(notificadorAgencia.notificarCancelacion(eq(10), eq("El cliente solicito la cancelacion")))
                .thenReturn(dtoEsperado);

        ResultadoNotificacionDTO resultado = service.cancelarReservacion(10, "El cliente solicito la cancelacion");

        assertNotNull(resultado);
        assertFalse(resultado.isEsReservaDeAgencia());
        verify(repo).obtenerReservacion(10);
        verify(repo).obtenerDatosUsuarioPorReservacion(10);
        verify(notificadorAgencia).notificarCancelacion(10, "El cliente solicito la cancelacion");
        verify(repo).cancelarReservacion(10, "El cliente solicito la cancelacion");
    }

    /**
     * Verifica que cancelarReservacion en estado Confirmada ejecuta la cancelacion,
     * invoca al notificador de agencia y retorna el DTO de notificacion.
     */
    @Test
    @DisplayName("cancelarReservacion_estadoConfirmada_ejecutaCancelacionYNotificaAgencia")
    void cancelarReservacion_estadoConfirmada_ejecutaCancelacionYNotificaAgencia() {
        Object[] datos = {20, 2, "Confirmada"};
        when(repo.obtenerReservacion(20)).thenReturn(datos);
        when(repo.obtenerDatosUsuarioPorReservacion(20)).thenReturn(null);

        ResultadoNotificacionDTO dtoEsperado = new ResultadoNotificacionDTO();
        dtoEsperado.setEsReservaDeAgencia(true);
        dtoEsperado.setNombreAgencia("Viajes Test");
        dtoEsperado.setEnviado(true);
        dtoEsperado.setHttpStatus(200);
        when(notificadorAgencia.notificarCancelacion(eq(20), eq("Cambio de planes")))
                .thenReturn(dtoEsperado);

        ResultadoNotificacionDTO resultado = service.cancelarReservacion(20, "Cambio de planes");

        assertNotNull(resultado);
        assertTrue(resultado.isEsReservaDeAgencia());
        assertTrue(resultado.isEnviado());
        assertEquals(200, resultado.getHttpStatus());
        verify(repo).obtenerDatosUsuarioPorReservacion(20);
        verify(notificadorAgencia).notificarCancelacion(20, "Cambio de planes");
        verify(repo).cancelarReservacion(20, "Cambio de planes");
    }

    /**
     * Verifica que cuando el notificador retorna que no es reserva de agencia,
     * la cancelacion se completa igualmente en BD.
     */
    @Test
    @DisplayName("cancelarReservacion_notificadorRetornaNoEsReservaAgencia_completaCancelacion")
    void cancelarReservacion_notificadorRetornaNoEsReservaAgencia_completaCancelacion() {
        Object[] datos = {15, 1, "Pendiente"};
        when(repo.obtenerReservacion(15)).thenReturn(datos);
        when(repo.obtenerDatosUsuarioPorReservacion(15)).thenReturn(null);

        ResultadoNotificacionDTO dtoNoAgencia = new ResultadoNotificacionDTO();
        dtoNoAgencia.setEsReservaDeAgencia(false);
        when(notificadorAgencia.notificarCancelacion(eq(15), anyString()))
                .thenReturn(dtoNoAgencia);

        ResultadoNotificacionDTO resultado = service.cancelarReservacion(15, "Sin motivo");

        assertNotNull(resultado);
        assertFalse(resultado.isEsReservaDeAgencia());
        assertFalse(resultado.isEnviado());
        verify(repo).cancelarReservacion(15, "Sin motivo");
    }

    /**
     * Verifica que cuando el notificador retorna un error HTTP,
     * la cancelacion se aplica igualmente en BD y el DTO de error se retorna al llamador.
     */
    @Test
    @DisplayName("cancelarReservacion_notificadorRetornaErrorHTTP_completaCancelacionConError")
    void cancelarReservacion_notificadorRetornaErrorHTTP_completaCancelacionConError() {
        Object[] datos = {25, 2, "Confirmada"};
        when(repo.obtenerReservacion(25)).thenReturn(datos);
        when(repo.obtenerDatosUsuarioPorReservacion(25)).thenReturn(null);

        ResultadoNotificacionDTO dtoError = new ResultadoNotificacionDTO();
        dtoError.setEsReservaDeAgencia(true);
        dtoError.setNombreAgencia("Agencia XYZ");
        dtoError.setEnviado(false);
        dtoError.setError("Connection timeout");
        when(notificadorAgencia.notificarCancelacion(eq(25), anyString()))
                .thenReturn(dtoError);

        ResultadoNotificacionDTO resultado = service.cancelarReservacion(25, "Motivo admin");

        assertNotNull(resultado);
        assertTrue(resultado.isEsReservaDeAgencia());
        assertFalse(resultado.isEnviado());
        assertEquals("Connection timeout", resultado.getError());
        // La cancelacion en BD ocurre aunque el notificador falle
        verify(repo).cancelarReservacion(25, "Motivo admin");
    }

    /**
     * Verifica que cancelarReservacion en estado Cancelada lanza excepcion.
     */
    @Test
    @DisplayName("cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException")
    void cancelarReservacion_estadoCancelada_lanzaIllegalArgumentException() {
        Object[] datos = {30, 4, "Cancelada"};
        when(repo.obtenerReservacion(30)).thenReturn(datos);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cancelarReservacion(30, "motivo")
        );

        assertEquals("No se puede cancelar: estado actual es \"Cancelada\"", ex.getMessage());
        verify(repo, never()).cancelarReservacion(anyInt(), anyString());
        verify(notificadorAgencia, never()).notificarCancelacion(anyInt(), anyString());
    }

    /**
     * Verifica que cancelarReservacion en estado Expirada lanza excepcion.
     */
    @Test
    @DisplayName("cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException")
    void cancelarReservacion_estadoExpirada_lanzaIllegalArgumentException() {
        Object[] datos = {40, 3, "Expirada"};
        when(repo.obtenerReservacion(40)).thenReturn(datos);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.cancelarReservacion(40, "motivo")
        );

        assertEquals("No se puede cancelar: estado actual es \"Expirada\"", ex.getMessage());
        verify(repo, never()).cancelarReservacion(anyInt(), anyString());
        verify(notificadorAgencia, never()).notificarCancelacion(anyInt(), anyString());
    }
}
