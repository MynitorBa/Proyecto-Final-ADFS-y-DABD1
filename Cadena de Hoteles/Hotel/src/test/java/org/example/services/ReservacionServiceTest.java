package org.example.services;

import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.dtos.ReservacionResponseDTO;
import org.example.repositories.ReservacionRepository;
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
 * Pruebas unitarias para ReservacionService.
 * Verifica la creacion de reservaciones con validaciones de fechas,
 * disponibilidad y consulta de reservaciones sin acceder a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservacionService — Pruebas unitarias")
class ReservacionServiceTest {

    @Mock private ReservacionRepository reservacionRepository;

    private ReservacionService service;

    /**
     * Inicializa el service con el mock antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        service = new ReservacionService(reservacionRepository);
    }

    // -- crearReservacion

    /**
     * Verifica que se lance IllegalArgumentException cuando la lista de habitaciones
     * esta vacia en la solicitud.
     */
    @Test
    @DisplayName("crearReservacion lanza excepcion si no se incluyen habitaciones")
    void crearReservacion_habitacionesVacias_lanzaExcepcion() {
        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearReservacion(request, 1)
        );

        assertEquals("Debe incluir al menos una habitaci\u00F3n", ex.getMessage());
    }

    /**
     * Verifica que se lance IllegalArgumentException cuando la lista de habitaciones
     * es null.
     */
    @Test
    @DisplayName("crearReservacion lanza excepcion si la lista de habitaciones es null")
    void crearReservacion_habitacionesNull_lanzaExcepcion() {
        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearReservacion(request, 1)
        );
    }

    /**
     * Verifica que la reservacion se cree correctamente y el response no sea null
     * cuando todos los datos son validos.
     */
    @Test
    @DisplayName("crearReservacion retorna response cuando los datos son validos")
    void crearReservacion_datosValidos_retornaResponse() {
        when(reservacionRepository.existeTraslape(anyInt(), any(Date.class), any(Date.class)))
                .thenReturn(false);
        when(reservacionRepository.obtenerPrecios(anyInt()))
                .thenReturn(new double[]{100.0, 20.0, 2.0});
        when(reservacionRepository.crearReservacion(anyString(), anyDouble(), anyInt(), any(), any()))
                .thenReturn(1);
        doNothing().when(reservacionRepository).expirarPendientesDeUsuario(anyInt(), anyInt());
        doNothing().when(reservacionRepository).crearDetalle(
                anyInt(), anyInt(), any(), any(), anyInt(), anyDouble());
        when(reservacionRepository.obtenerReservacion(1)).thenReturn(
                new Object[]{1, "MIKU-001", 100.0, "2030-01-10", "2030-01-15", "Pendiente"}
        );

        HabitacionReservaRequestDTO hab = new HabitacionReservaRequestDTO();
        hab.setHabitacionId(1);
        hab.setFechaCheckIn("2030-01-10");
        hab.setFechaCheckOut("2030-01-15");
        hab.setCantidadPersonas(1);

        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(List.of(hab));

        ReservacionResponseDTO response = service.crearReservacion(request, 10);

        assertNotNull(response);
    }

    /**
     * Verifica que se lance IllegalArgumentException cuando la habitacion ya tiene
     * una reservacion activa que se traslapa con las fechas solicitadas.
     */
    @Test
    @DisplayName("crearReservacion lanza excepcion si existe traslape de fechas")
    void crearReservacion_traslape_lanzaExcepcion() {
        when(reservacionRepository.existeTraslape(anyInt(), any(Date.class), any(Date.class)))
                .thenReturn(true);
        // obtenerPrecios nunca se alcanza: el service lanza IllegalArgumentException antes

        HabitacionReservaRequestDTO hab = new HabitacionReservaRequestDTO();
        hab.setHabitacionId(5);
        hab.setFechaCheckIn("2030-03-01");
        hab.setFechaCheckOut("2030-03-05");
        hab.setCantidadPersonas(1);

        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(List.of(hab));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearReservacion(request, 1)
        );
    }

    // -- obtenerReservaciones

    /**
     * Verifica que obtenerReservaciones cargue imagenes para cada reservacion
     * y retorne la lista completa.
     */
    @Test
    @DisplayName("obtenerReservaciones retorna lista con imagenes cargadas")
    void obtenerReservaciones_usuarioConReservaciones_retornaListaConImagenes() {
        ReservacionDetalleDTO dto = new ReservacionDetalleDTO();
        dto.setHotelId(1);
        dto.setHabitacionId(2);

        when(reservacionRepository.obtenerReservacionesDeUsuario(7)).thenReturn(List.of(dto));
        when(reservacionRepository.obtenerImagenesHotel(1)).thenReturn(List.of(100));
        when(reservacionRepository.obtenerImagenesHabitacion(2)).thenReturn(List.of(200, 201));

        List<ReservacionDetalleDTO> result = service.obtenerReservaciones(7);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservacionRepository).obtenerImagenesHotel(1);
        verify(reservacionRepository).obtenerImagenesHabitacion(2);
    }

    /**
     * Verifica que obtenerReservaciones retorne una lista vacia cuando el usuario
     * no tiene reservaciones registradas.
     */
    @Test
    @DisplayName("obtenerReservaciones retorna lista vacia si el usuario no tiene reservaciones")
    void obtenerReservaciones_sinReservaciones_retornaListaVacia() {
        when(reservacionRepository.obtenerReservacionesDeUsuario(99))
                .thenReturn(Collections.emptyList());

        List<ReservacionDetalleDTO> result = service.obtenerReservaciones(99);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
