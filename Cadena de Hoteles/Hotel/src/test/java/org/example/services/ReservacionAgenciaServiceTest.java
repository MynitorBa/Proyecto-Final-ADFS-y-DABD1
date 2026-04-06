package org.example.services;

import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionAgenciaResponseDTO;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.repositories.ReservacionAgenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ReservacionAgenciaService.
 * Verifica creacion, consulta, expiracion y detalle de reservaciones
 * de agencia sin acceder a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservacionAgenciaService — Pruebas unitarias")
class ReservacionAgenciaServiceTest {

    @Mock private ReservacionAgenciaRepository repository;

    private ReservacionAgenciaService service;

    /**
     * Inicializa el service con el mock antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        service = new ReservacionAgenciaService(repository);
    }

    // -- crearReservacion

    /**
     * Verifica que se lance IllegalArgumentException cuando la agencia no esta activa
     * (obtenerDatosAgencia retorna null).
     */
    @Test
    @DisplayName("crearReservacion lanza excepcion si la agencia no esta activa")
    void crearReservacion_agenciaNoActiva_lanzaExcepcion() {
        when(repository.obtenerDatosAgencia(5)).thenReturn(null);

        ReservacionRequestDTO request = new ReservacionRequestDTO();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearReservacion(request, 5)
        );

        assertEquals("La agencia no esta activa", ex.getMessage());
    }

    /**
     * Verifica que se lance IllegalArgumentException cuando la lista de habitaciones
     * esta vacia en la solicitud.
     */
    @Test
    @DisplayName("crearReservacion lanza excepcion si no hay habitaciones")
    void crearReservacion_sinHabitaciones_lanzaExcepcion() {
        when(repository.obtenerDatosAgencia(1)).thenReturn(new int[]{10});
        when(repository.obtenerDescuentoAgencia(1)).thenReturn(10.0);

        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearReservacion(request, 1)
        );

        assertEquals("Debe incluir al menos una habitacion", ex.getMessage());
    }

    /**
     * Verifica que la reservacion se cree correctamente y el response no sea null
     * cuando todos los datos son validos.
     */
    @Test
    @DisplayName("crearReservacion retorna response cuando los datos son validos")
    void crearReservacion_datosValidos_retornaResponse() {
        when(repository.obtenerDatosAgencia(2)).thenReturn(new int[]{1});
        when(repository.obtenerDescuentoAgencia(2)).thenReturn(10.0);
        when(repository.existeTraslape(anyInt(), any(Date.class), any(Date.class))).thenReturn(false);
        when(repository.obtenerPrecios(anyInt())).thenReturn(new double[]{100.0, 20.0, 2.0});
        when(repository.crearReservacion(anyString(), anyDouble(), anyInt(), any(), any())).thenReturn(42);
        doNothing().when(repository).crearDetalle(anyInt(), anyInt(), any(), any(), anyInt(), anyDouble());
        when(repository.obtenerReservacion(42)).thenReturn(
                new Object[]{42, "MIKU-001", 200.0, "2025-01-01", "2025-01-01 15:00", "Pendiente"}
        );

        HabitacionReservaRequestDTO hab = new HabitacionReservaRequestDTO();
        hab.setHabitacionId(1);
        hab.setFechaCheckIn("2027-01-01");
        hab.setFechaCheckOut("2027-01-05");
        hab.setCantidadPersonas(1);

        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(List.of(hab));

        ReservacionAgenciaResponseDTO response = service.crearReservacion(request, 2);

        assertNotNull(response);
    }

    // -- obtenerReservaciones

    /**
     * Verifica que obtenerReservaciones delegue directamente al repositorio
     * y retorne la lista resultante.
     */
    @Test
    @DisplayName("obtenerReservaciones retorna la lista del repositorio")
    void obtenerReservaciones_agenciaExistente_retornaLista() {
        ReservacionDetalleDTO dto = new ReservacionDetalleDTO();
        when(repository.obtenerReservacionesDeAgencia(3)).thenReturn(List.of(dto));

        List<ReservacionDetalleDTO> result = service.obtenerReservaciones(3);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).obtenerReservacionesDeAgencia(3);
    }

    /**
     * Verifica que obtenerReservaciones retorne una lista vacia cuando la agencia
     * no tiene reservaciones.
     */
    @Test
    @DisplayName("obtenerReservaciones retorna lista vacia cuando no hay reservaciones")
    void obtenerReservaciones_sinReservaciones_retornaListaVacia() {
        when(repository.obtenerReservacionesDeAgencia(99)).thenReturn(Collections.emptyList());

        List<ReservacionDetalleDTO> result = service.obtenerReservaciones(99);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // -- expirarReservacion

    /**
     * Verifica que se lance IllegalArgumentException cuando la reservacion no pertenece
     * a la agencia o no esta en estado pendiente.
     */
    @Test
    @DisplayName("expirarReservacion lanza excepcion si la reservacion no es valida")
    void expirarReservacion_reservacionInvalida_lanzaExcepcion() {
        when(repository.perteneceAAgenciaYEstaPendiente(10, 1)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.expirarReservacion(10, 1)
        );

        assertEquals(
                "La reservacion no existe, no pertenece a esta agencia, o no esta en estado pendiente",
                ex.getMessage()
        );
    }

    /**
     * Verifica que expirarReservacion invoque al repositorio cuando la reservacion es valida.
     */
    @Test
    @DisplayName("expirarReservacion invoca al repositorio cuando la reservacion es valida")
    void expirarReservacion_reservacionValida_invocaRepositorio() {
        when(repository.perteneceAAgenciaYEstaPendiente(7, 2)).thenReturn(true);

        service.expirarReservacion(7, 2);

        verify(repository).expirarReservacion(7);
    }

    // -- obtenerDetalleReservacion

    /**
     * Verifica que se lance IllegalArgumentException cuando el detalle retornado
     * por el repositorio es null o vacio.
     */
    @Test
    @DisplayName("obtenerDetalleReservacion lanza excepcion si no se encuentran detalles")
    void obtenerDetalleReservacion_sinDetalles_lanzaExcepcion() {
        when(repository.obtenerDetalleReservacionAgencia(20, 4))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.obtenerDetalleReservacion(20, 4)
        );

        assertEquals("Reservacion no encontrada o no pertenece a esta agencia", ex.getMessage());
    }

    /**
     * Verifica que obtenerDetalleReservacion cargue imagenes y retorne los detalles
     * cuando la reservacion existe y pertenece a la agencia.
     */
    @Test
    @DisplayName("obtenerDetalleReservacion retorna detalles con imagenes cargadas")
    void obtenerDetalleReservacion_conDetalles_retornaListaConImagenes() {
        ReservacionDetalleDTO dto = new ReservacionDetalleDTO();
        dto.setHotelId(1);
        dto.setHabitacionId(2);

        when(repository.obtenerDetalleReservacionAgencia(15, 3)).thenReturn(List.of(dto));
        when(repository.obtenerImagenesHotel(1)).thenReturn(List.of(10, 11));
        when(repository.obtenerImagenesHabitacion(2)).thenReturn(List.of(20));

        List<ReservacionDetalleDTO> result = service.obtenerDetalleReservacion(15, 3);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).obtenerImagenesHotel(1);
        verify(repository).obtenerImagenesHabitacion(2);
    }
}
