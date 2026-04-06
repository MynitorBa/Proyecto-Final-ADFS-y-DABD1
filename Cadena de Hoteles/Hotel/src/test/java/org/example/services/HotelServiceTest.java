package org.example.services;

import org.example.dtos.*;
import org.example.repositories.CiudadRepository;
import org.example.repositories.HotelRepository;
import org.example.repositories.PaisRepository;
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
 * Unit tests for HotelService.
 * Covers: crearAmenidad, listarTodos, crearHotel, editarHotel,
 * eliminarHotel, listarAmenidadesHotel, and crearHabitacion.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HotelService Tests")
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private CiudadRepository ciudadRepository;

    @Mock
    private PaisRepository paisRepository;

    private HotelService service;

    @BeforeEach
    void setUp() {
        service = new HotelService(hotelRepository, ciudadRepository, paisRepository);
    }

    // -- crearAmenidad

    @Test
    @DisplayName("crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje")
    void crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje() {
        when(hotelRepository.crearAmenidad("Piscina")).thenReturn(10);

        Map<String, Object> resultado = service.crearAmenidad("Piscina");

        assertNotNull(resultado);
        assertEquals(10, resultado.get("id"));
        assertEquals("Piscina", resultado.get("nombre"));
        assertEquals("Amenidad creada correctamente", resultado.get("mensaje"));
        verify(hotelRepository).crearAmenidad("Piscina");
    }

    @Test
    @DisplayName("crearAmenidad_nombreNulo_lanzaIllegalArgumentException")
    void crearAmenidad_nombreNulo_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearAmenidad(null)
        );
        assertEquals("El nombre de la amenidad es obligatorio", ex.getMessage());
        verify(hotelRepository, never()).crearAmenidad(anyString());
    }

    @Test
    @DisplayName("crearAmenidad_nombreBlanco_lanzaIllegalArgumentException")
    void crearAmenidad_nombreBlanco_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearAmenidad("   ")
        );
        assertEquals("El nombre de la amenidad es obligatorio", ex.getMessage());
        verify(hotelRepository, never()).crearAmenidad(anyString());
    }

    // -- listarTodos

    @Test
    @DisplayName("listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes")
    void listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes() {
        HotelAdminDTO hotel = new HotelAdminDTO();
        when(hotelRepository.listarTodos()).thenReturn(List.of(hotel));
        when(hotelRepository.contarHabitaciones(hotel.getId())).thenReturn(3);
        when(hotelRepository.obtenerImagenesIds(hotel.getId())).thenReturn(List.of(1, 2));

        List<HotelAdminDTO> resultado = service.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        HotelAdminDTO dto = resultado.get(0);
        assertEquals(3, dto.getCantidadHabitaciones(),
                "El service debe asignar la cantidad de habitaciones al DTO");
        assertEquals(List.of(1, 2), dto.getImagenesIds(),
                "El service debe asignar los IDs de imagenes al DTO");
        verify(hotelRepository).listarTodos();
        verify(hotelRepository).contarHabitaciones(hotel.getId());
        verify(hotelRepository).obtenerImagenesIds(hotel.getId());
    }

    @Test
    @DisplayName("listarTodos_repositorioRetornaListaVacia_retornaListaVacia")
    void listarTodos_repositorioRetornaListaVacia_retornaListaVacia() {
        when(hotelRepository.listarTodos()).thenReturn(Collections.emptyList());

        List<HotelAdminDTO> resultado = service.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(hotelRepository).listarTodos();
        verify(hotelRepository, never()).contarHabitaciones(anyInt());
    }

    // -- crearHotel

    @Test
    @DisplayName("crearHotel_datosValidos_retornaMapaConIdMensaje")
    void crearHotel_datosValidos_retornaMapaConIdMensaje() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Sol");
        req.setRating(4.0);
        req.setEstadoId(1);
        req.setCiudad("Guatemala");
        req.setPaisNombre("Guatemala");
        req.setDireccion("Av. Principal 1");
        req.setDescripcion("Un buen hotel");

        when(paisRepository.buscarOCrearPorNombre("Guatemala")).thenReturn(1);
        when(ciudadRepository.buscarOCrearPorNombre("Guatemala", 1)).thenReturn(2);
        when(hotelRepository.crearHotel(anyString(), anyString(), anyString(),
                anyDouble(), anyInt(), anyInt())).thenReturn(99);

        Map<String, Object> resultado = service.crearHotel(req);

        assertNotNull(resultado);
        assertEquals(99, resultado.get("id"));
        assertEquals("Hotel creado correctamente", resultado.get("mensaje"));
        verify(hotelRepository).crearHotel(anyString(), anyString(), anyString(),
                anyDouble(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("crearHotel_nombreNulo_lanzaIllegalArgumentException")
    void crearHotel_nombreNulo_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre(null);
        req.setRating(3.0);
        req.setEstadoId(1);
        req.setCiudad("Antigua");
        req.setPaisNombre("Guatemala");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearHotel(req)
        );
        assertEquals("El nombre del hotel no puede estar vacio", ex.getMessage());
    }

    @Test
    @DisplayName("crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException")
    void crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Luna");
        req.setRating(6.0);
        req.setEstadoId(1);
        req.setCiudad("Coban");
        req.setPaisNombre("Guatemala");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearHotel(req)
        );
        assertEquals("El rating debe estar entre 0 y 5", ex.getMessage());
    }

    @Test
    @DisplayName("crearHotel_estadoInvalido_lanzaIllegalArgumentException")
    void crearHotel_estadoInvalido_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Roca");
        req.setRating(3.0);
        req.setEstadoId(9);
        req.setCiudad("Xela");
        req.setPaisNombre("Guatemala");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearHotel(req)
        );
        assertEquals("Estado invalido: use 1 (Activo) o 2 (Cerrado)", ex.getMessage());
    }

    @Test
    @DisplayName("crearHotel_ciudadNula_lanzaIllegalArgumentException")
    void crearHotel_ciudadNula_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Mar");
        req.setRating(2.5);
        req.setEstadoId(1);
        req.setCiudad(null);
        req.setPaisNombre("Guatemala");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearHotel(req)
        );
        assertEquals("El nombre de la ciudad es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("crearHotel_paisNulo_lanzaIllegalArgumentException")
    void crearHotel_paisNulo_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Viento");
        req.setRating(2.5);
        req.setEstadoId(1);
        req.setCiudad("Flores");
        req.setPaisNombre(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearHotel(req)
        );
        assertEquals("El nombre del pais es obligatorio", ex.getMessage());
    }

    // -- editarHotel

    @Test
    @DisplayName("editarHotel_hotelExiste_invocaActualizarHotel")
    void editarHotel_hotelExiste_invocaActualizarHotel() {
        EditarHotelRequestDTO req = new EditarHotelRequestDTO();
        req.setNombre("Hotel Renovado");
        req.setRating(4.5);
        req.setEstadoId(1);
        req.setDireccion("Calle 5");
        req.setDescripcion("Renovado");

        when(hotelRepository.existe(10)).thenReturn(true);

        assertDoesNotThrow(() -> service.editarHotel(10, req));

        verify(hotelRepository).existe(10);
        verify(hotelRepository).actualizarHotel(eq(10), eq("Hotel Renovado"),
                anyString(), anyString(), eq(4.5), eq(1));
    }

    @Test
    @DisplayName("editarHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void editarHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        EditarHotelRequestDTO req = new EditarHotelRequestDTO();
        req.setNombre("Hotel X");
        req.setRating(3.0);
        req.setEstadoId(2);

        when(hotelRepository.existe(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.editarHotel(99, req)
        );
        assertEquals("Hotel no encontrado: 99", ex.getMessage());
        verify(hotelRepository, never()).actualizarHotel(anyInt(), anyString(),
                anyString(), anyString(), anyDouble(), anyInt());
    }

    // -- eliminarHotel

    @Test
    @DisplayName("eliminarHotel_hotelExiste_invocaEliminarHotel")
    void eliminarHotel_hotelExiste_invocaEliminarHotel() {
        when(hotelRepository.existe(5)).thenReturn(true);

        assertDoesNotThrow(() -> service.eliminarHotel(5));

        verify(hotelRepository).existe(5);
        verify(hotelRepository).eliminarHotel(5);
    }

    @Test
    @DisplayName("eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(77)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.eliminarHotel(77)
        );
        assertEquals("Hotel no encontrado: 77", ex.getMessage());
        verify(hotelRepository, never()).eliminarHotel(anyInt());
    }

    // -- listarAmenidadesHotel

    @Test
    @DisplayName("listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(3)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.listarAmenidadesHotel(3)
        );
        assertEquals("Hotel no encontrado: 3", ex.getMessage());
    }

    @Test
    @DisplayName("listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades")
    void listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades() {
        HotelAmenidadDTO amenidad = new HotelAmenidadDTO();
        when(hotelRepository.existe(4)).thenReturn(true);
        when(hotelRepository.listarAmenidadesHotel(4)).thenReturn(List.of(amenidad));
        when(hotelRepository.obtenerImagenesAmenidadIds(amenidad.getId())).thenReturn(Collections.emptyList());

        List<HotelAmenidadDTO> resultado = service.listarAmenidadesHotel(4);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(hotelRepository).listarAmenidadesHotel(4);
    }

    // -- crearHabitacion

    @Test
    @DisplayName("crearHabitacion_datosValidos_retornaMapaConIdMensaje")
    void crearHabitacion_datosValidos_retornaMapaConIdMensaje() {
        CrearHabitacionRequestDTO req = new CrearHabitacionRequestDTO();
        req.setHotelId(1);
        req.setTipoHabitacionId(2);
        req.setEstadoId(1);
        req.setDescripcion("Habitacion doble");

        when(hotelRepository.existe(1)).thenReturn(true);
        when(hotelRepository.crearHabitacion(1, 2, "Habitacion doble", 1)).thenReturn(50);

        Map<String, Object> resultado = service.crearHabitacion(req);

        assertNotNull(resultado);
        assertEquals(50, resultado.get("id"));
        assertEquals("Habitacion creada correctamente", resultado.get("mensaje"));
        verify(hotelRepository).crearHabitacion(1, 2, "Habitacion doble", 1);
    }

    @Test
    @DisplayName("crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException")
    void crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException() {
        CrearHabitacionRequestDTO req = new CrearHabitacionRequestDTO();
        req.setHotelId(9);
        req.setTipoHabitacionId(1);
        req.setEstadoId(1);

        when(hotelRepository.existe(9)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearHabitacion(req)
        );
        assertEquals("Hotel no encontrado: 9", ex.getMessage());
        verify(hotelRepository, never()).crearHabitacion(anyInt(), anyInt(), anyString(), anyInt());
    }

    @Test
    @DisplayName("crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException")
    void crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException() {
        CrearHabitacionRequestDTO req = new CrearHabitacionRequestDTO();
        req.setHotelId(1);
        req.setTipoHabitacionId(0);
        req.setEstadoId(1);

        when(hotelRepository.existe(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearHabitacion(req)
        );
        assertEquals("Tipo de habitacion invalido", ex.getMessage());
    }

    @Test
    @DisplayName("crearHabitacion_estadoInvalido_lanzaIllegalArgumentException")
    void crearHabitacion_estadoInvalido_lanzaIllegalArgumentException() {
        CrearHabitacionRequestDTO req = new CrearHabitacionRequestDTO();
        req.setHotelId(1);
        req.setTipoHabitacionId(2);
        req.setEstadoId(5);

        when(hotelRepository.existe(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearHabitacion(req)
        );
        assertEquals("Estado invalido: use 1 (Activa) o 2 (Cerrada)", ex.getMessage());
    }
}
