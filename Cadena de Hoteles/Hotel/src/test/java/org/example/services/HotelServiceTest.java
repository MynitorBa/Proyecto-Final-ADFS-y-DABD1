package org.example.services;

import org.example.clients.MoventClient;
import org.example.dtos.*;
import org.example.helpers.EmailHelper;
import org.example.repositories.CiudadRepository;
import org.example.repositories.HotelRepository;
import org.example.repositories.PaisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para HotelService.
 * Cubre todos los metodos publicos: catalogo, hoteles, amenidades,
 * imagenes, habitaciones y los flujos de cierre/reactivacion con cancelaciones.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HotelService — Pruebas Unitarias")
class HotelServiceTest {

    @Mock private HotelRepository                  hotelRepository;
    @Mock private CiudadRepository                ciudadRepository;
    @Mock private PaisRepository                  paisRepository;
    @Mock private AgenciaNotificadorExternoService agenciaNotificador;

    private HotelService service;

    @BeforeEach
    void setUp() {
        service = new HotelService(hotelRepository, ciudadRepository,
                                   paisRepository, agenciaNotificador);
    }

    // =========================================================================
    // listarAmenidades
    // =========================================================================

    @Test
    @DisplayName("listarAmenidades_delegaAlRepositorio")
    void listarAmenidades_delegaAlRepositorio() {
        AmenidadDTO a = new AmenidadDTO();
        when(hotelRepository.listarAmenidades()).thenReturn(List.of(a));

        List<AmenidadDTO> resultado = service.listarAmenidades();

        assertEquals(1, resultado.size());
        verify(hotelRepository).listarAmenidades();
    }

    // =========================================================================
    // listarPaises
    // =========================================================================

    @Test
    @DisplayName("listarPaises_delegaAlRepositorioDePaises")
    void listarPaises_delegaAlRepositorioDePaises() {
        when(paisRepository.listarPaises()).thenReturn(Collections.emptyList());

        List<PaisDTO> resultado = service.listarPaises();

        assertTrue(resultado.isEmpty());
        verify(paisRepository).listarPaises();
    }

    // =========================================================================
    // listarCiudades
    // =========================================================================

    @Test
    @DisplayName("listarCiudades_delegaAlRepositorioDePaises")
    void listarCiudades_delegaAlRepositorioDePaises() {
        when(paisRepository.listarCiudades()).thenReturn(Collections.emptyList());

        List<CiudadDTO> resultado = service.listarCiudades();

        assertTrue(resultado.isEmpty());
        verify(paisRepository).listarCiudades();
    }

    // =========================================================================
    // crearAmenidad
    // =========================================================================

    @Test
    @DisplayName("crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje")
    void crearAmenidad_nombreValido_retornaMapaConIdNombreMensaje() {
        when(hotelRepository.crearAmenidad("Piscina")).thenReturn(10);

        Map<String, Object> resultado = service.crearAmenidad("Piscina");

        assertNotNull(resultado);
        assertEquals(10,        resultado.get("id"));
        assertEquals("Piscina", resultado.get("nombre"));
        assertEquals("Amenidad creada correctamente", resultado.get("mensaje"));
        verify(hotelRepository).crearAmenidad("Piscina");
    }

    @Test
    @DisplayName("crearAmenidad_nombreNulo_lanzaIllegalArgumentException")
    void crearAmenidad_nombreNulo_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearAmenidad(null));

        assertEquals("El nombre de la amenidad es obligatorio", ex.getMessage());
        verify(hotelRepository, never()).crearAmenidad(anyString());
    }

    @Test
    @DisplayName("crearAmenidad_nombreBlanco_lanzaIllegalArgumentException")
    void crearAmenidad_nombreBlanco_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearAmenidad("   "));

        assertEquals("El nombre de la amenidad es obligatorio", ex.getMessage());
        verify(hotelRepository, never()).crearAmenidad(anyString());
    }

    // =========================================================================
    // listarTodos
    // =========================================================================

    @Test
    @DisplayName("listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes")
    void listarTodos_repositorioRetornaHoteles_enriqueceConHabitacionesEImagenes() {
        HotelAdminDTO hotel = new HotelAdminDTO();
        when(hotelRepository.listarTodos()).thenReturn(List.of(hotel));
        when(hotelRepository.contarHabitaciones(hotel.getId())).thenReturn(3);
        when(hotelRepository.obtenerImagenesIds(hotel.getId())).thenReturn(List.of(1, 2));

        List<HotelAdminDTO> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(3,             resultado.get(0).getCantidadHabitaciones());
        assertEquals(List.of(1, 2), resultado.get(0).getImagenesIds());
        verify(hotelRepository).listarTodos();
        verify(hotelRepository).contarHabitaciones(hotel.getId());
        verify(hotelRepository).obtenerImagenesIds(hotel.getId());
    }

    @Test
    @DisplayName("listarTodos_repositorioRetornaListaVacia_retornaListaVacia")
    void listarTodos_repositorioRetornaListaVacia_retornaListaVacia() {
        when(hotelRepository.listarTodos()).thenReturn(Collections.emptyList());

        List<HotelAdminDTO> resultado = service.listarTodos();

        assertTrue(resultado.isEmpty());
        verify(hotelRepository).listarTodos();
        verify(hotelRepository, never()).contarHabitaciones(anyInt());
    }

    // =========================================================================
    // crearHotel
    // =========================================================================

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

        assertEquals(99,                         resultado.get("id"));
        assertEquals("Hotel creado correctamente", resultado.get("mensaje"));
        verify(hotelRepository).crearHotel(anyString(), anyString(), anyString(),
                anyDouble(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("crearHotel_nombreNulo_lanzaIllegalArgumentException")
    void crearHotel_nombreNulo_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre(null); req.setRating(3.0); req.setEstadoId(1);
        req.setCiudad("Antigua"); req.setPaisNombre("Guatemala");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearHotel(req));
        assertEquals("El nombre del hotel no puede estar vacio", ex.getMessage());
    }

    @Test
    @DisplayName("crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException")
    void crearHotel_ratingFueraDeRango_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Luna"); req.setRating(6.0); req.setEstadoId(1);
        req.setCiudad("Coban"); req.setPaisNombre("Guatemala");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearHotel(req));
        assertEquals("El rating debe estar entre 0 y 5", ex.getMessage());
    }

    @Test
    @DisplayName("crearHotel_estadoInvalido_lanzaIllegalArgumentException")
    void crearHotel_estadoInvalido_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Roca"); req.setRating(3.0); req.setEstadoId(9);
        req.setCiudad("Xela"); req.setPaisNombre("Guatemala");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearHotel(req));
        assertEquals("Estado invalido: use 1 (Activo) o 2 (Cerrado)", ex.getMessage());
    }

    @Test
    @DisplayName("crearHotel_ciudadNula_lanzaIllegalArgumentException")
    void crearHotel_ciudadNula_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Mar"); req.setRating(2.5); req.setEstadoId(1);
        req.setCiudad(null); req.setPaisNombre("Guatemala");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearHotel(req));
        assertEquals("El nombre de la ciudad es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("crearHotel_paisNulo_lanzaIllegalArgumentException")
    void crearHotel_paisNulo_lanzaIllegalArgumentException() {
        CrearHotelRequestDTO req = new CrearHotelRequestDTO();
        req.setNombre("Hotel Viento"); req.setRating(2.5); req.setEstadoId(1);
        req.setCiudad("Flores"); req.setPaisNombre(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearHotel(req));
        assertEquals("El nombre del pais es obligatorio", ex.getMessage());
    }

    // =========================================================================
    // editarHotel
    // =========================================================================

    @Test
    @DisplayName("editarHotel_hotelExiste_invocaActualizarHotel")
    void editarHotel_hotelExiste_invocaActualizarHotel() {
        EditarHotelRequestDTO req = new EditarHotelRequestDTO();
        req.setNombre("Hotel Renovado"); req.setRating(4.5); req.setEstadoId(1);
        req.setDireccion("Calle 5"); req.setDescripcion("Renovado");

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
        req.setNombre("Hotel X"); req.setRating(3.0); req.setEstadoId(2);

        when(hotelRepository.existe(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.editarHotel(99, req));
        assertEquals("Hotel no encontrado: 99", ex.getMessage());
        verify(hotelRepository, never()).actualizarHotel(anyInt(), anyString(),
                anyString(), anyString(), anyDouble(), anyInt());
    }

    // =========================================================================
    // eliminarHotel
    // =========================================================================

    @Test
    @DisplayName("eliminarHotel_hotelExisteSinReservasActivas_invocaEliminarHotel")
    void eliminarHotel_hotelExisteSinReservasActivas_invocaEliminarHotel() {
        when(hotelRepository.existe(5)).thenReturn(true);
        when(hotelRepository.contarReservasActivasHotel(5)).thenReturn(0);

        assertDoesNotThrow(() -> service.eliminarHotel(5));

        verify(hotelRepository).existe(5);
        verify(hotelRepository).contarReservasActivasHotel(5);
        verify(hotelRepository).eliminarHotel(5);
    }

    @Test
    @DisplayName("eliminarHotel_hotelExisteConReservasActivas_lanzaIllegalArgumentException")
    void eliminarHotel_hotelExisteConReservasActivas_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(5)).thenReturn(true);
        when(hotelRepository.contarReservasActivasHotel(5)).thenReturn(3);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.eliminarHotel(5));
        assertTrue(ex.getMessage().contains("3 reservacion(es)"));
        verify(hotelRepository, never()).eliminarHotel(anyInt());
    }

    @Test
    @DisplayName("eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void eliminarHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(77)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.eliminarHotel(77));
        assertEquals("Hotel no encontrado: 77", ex.getMessage());
        verify(hotelRepository, never()).eliminarHotel(anyInt());
    }

    // =========================================================================
    // obtenerReservasActivasHotel
    // =========================================================================

    @Test
    @DisplayName("obtenerReservasActivasHotel_hotelExisteConReservas_retornaMapaConDatos")
    void obtenerReservasActivasHotel_hotelExisteConReservas_retornaMapaConDatos() {
        Object[] fila = {1, "RES-001", "u@test.com", "Juan Perez", 250.0};
        List<Object[]> filas = Collections.singletonList(fila);
        when(hotelRepository.existe(1)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHotel(1)).thenReturn(filas);

        Map<String, Object> resultado = service.obtenerReservasActivasHotel(1);

        assertEquals(1, resultado.get("count"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista = (List<Map<String, Object>>) resultado.get("reservaciones");
        assertEquals(1,           lista.size());
        assertEquals("RES-001",   lista.get(0).get("noReservacion"));
        assertEquals("u@test.com", lista.get(0).get("correo"));
        assertEquals("Juan Perez", lista.get(0).get("nombre"));
    }

    @Test
    @DisplayName("obtenerReservasActivasHotel_hotelExisteSinReservas_retornaCountCero")
    void obtenerReservasActivasHotel_hotelExisteSinReservas_retornaCountCero() {
        when(hotelRepository.existe(1)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHotel(1))
                .thenReturn(Collections.emptyList());

        Map<String, Object> resultado = service.obtenerReservasActivasHotel(1);

        assertEquals(0, resultado.get("count"));
        @SuppressWarnings("unchecked")
        List<?> lista = (List<?>) resultado.get("reservaciones");
        assertTrue(lista.isEmpty());
    }

    @Test
    @DisplayName("obtenerReservasActivasHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void obtenerReservasActivasHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.obtenerReservasActivasHotel(99));
        assertEquals("Hotel no encontrado: 99", ex.getMessage());
    }

    // =========================================================================
    // reactivarHotel
    // =========================================================================

    @Test
    @DisplayName("reactivarHotel_hotelExiste_invocaReactivarHotel")
    void reactivarHotel_hotelExiste_invocaReactivarHotel() {
        when(hotelRepository.existe(3)).thenReturn(true);

        assertDoesNotThrow(() -> service.reactivarHotel(3));

        verify(hotelRepository).reactivarHotel(3);
    }

    @Test
    @DisplayName("reactivarHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void reactivarHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.reactivarHotel(99));
        assertEquals("Hotel no encontrado: 99", ex.getMessage());
        verify(hotelRepository, never()).reactivarHotel(anyInt());
    }

    // =========================================================================
    // cerrarHotelConCancelaciones
    // =========================================================================

    @Test
    @DisplayName("cerrarHotelConCancelaciones_hotelNoExiste_lanzaIllegalArgumentException")
    void cerrarHotelConCancelaciones_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.cerrarHotelConCancelaciones(99, "Hotel X", false));
        assertEquals("Hotel no encontrado: 99", ex.getMessage());
    }

    @Test
    @DisplayName("cerrarHotelConCancelaciones_sinReservas_cierraHotelSinEliminar")
    void cerrarHotelConCancelaciones_sinReservas_cierraHotelSinEliminar() {
        when(hotelRepository.existe(1)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHotel(1))
                .thenReturn(Collections.emptyList());

        Map<String, Object> result =
                service.cerrarHotelConCancelaciones(1, "Hotel Test", false);

        assertEquals(0, result.get("cancelaciones"));
        assertEquals(0, result.get("emailsEnviados"));
        assertEquals(0, result.get("emailsFallidos"));
        verify(hotelRepository).cerrarHotel(1);
        verify(hotelRepository, never()).eliminarHotel(anyInt());
    }

    @Test
    @DisplayName("cerrarHotelConCancelaciones_sinReservas_eliminaHotelDefinitivo")
    void cerrarHotelConCancelaciones_sinReservas_eliminaHotelDefinitivo() {
        when(hotelRepository.existe(2)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHotel(2))
                .thenReturn(Collections.emptyList());

        Map<String, Object> result =
                service.cerrarHotelConCancelaciones(2, "Hotel Demo", true);

        assertEquals(0, result.get("cancelaciones"));
        verify(hotelRepository).eliminarHotel(2);
        verify(hotelRepository, never()).cerrarHotel(anyInt());
    }

    @Test
    @DisplayName("cerrarHotelConCancelaciones_conReservas_cancelaEmailsYCierra")
    void cerrarHotelConCancelaciones_conReservas_cancelaEmailsYCierra() {
        Object[] fila = {1, "RES-001", "ana@test.com", "Ana Lopez", 300.0};
        List<Object[]> filas = Collections.singletonList(fila);
        when(hotelRepository.existe(1)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHotel(1)).thenReturn(filas);

        ResultadoNotificacionDTO dto = new ResultadoNotificacionDTO();
        dto.setEsReservaDeAgencia(false);
        when(agenciaNotificador.notificarCancelacion(eq(1), anyString())).thenReturn(dto);

        try (MockedStatic<EmailHelper> emailMock = Mockito.mockStatic(EmailHelper.class)) {
            emailMock.when(() -> EmailHelper.enviar(anyString(), anyString(), anyString()))
                     .thenAnswer(inv -> null);

            Map<String, Object> result =
                    service.cerrarHotelConCancelaciones(1, "Hotel Test", false);

            assertEquals(1, result.get("cancelaciones"));
            assertEquals(1, result.get("emailsEnviados"));
            assertEquals(0, result.get("emailsFallidos"));
            verify(hotelRepository).cancelarReservacionesActivasHotel(eq(1), anyString());
            verify(hotelRepository).cerrarHotel(1);
            verify(hotelRepository, never()).eliminarHotel(anyInt());
        }
    }

    // =========================================================================
    // listarAmenidadesHotel
    // =========================================================================

    @Test
    @DisplayName("listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void listarAmenidadesHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(3)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.listarAmenidadesHotel(3));
        assertEquals("Hotel no encontrado: 3", ex.getMessage());
    }

    @Test
    @DisplayName("listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades")
    void listarAmenidadesHotel_hotelExiste_retornaListaDeAmenidades() {
        HotelAmenidadDTO amenidad = new HotelAmenidadDTO();
        when(hotelRepository.existe(4)).thenReturn(true);
        when(hotelRepository.listarAmenidadesHotel(4)).thenReturn(List.of(amenidad));
        when(hotelRepository.obtenerImagenesAmenidadIds(amenidad.getId()))
                .thenReturn(Collections.emptyList());

        List<HotelAmenidadDTO> resultado = service.listarAmenidadesHotel(4);

        assertEquals(1, resultado.size());
        verify(hotelRepository).listarAmenidadesHotel(4);
    }

    // =========================================================================
    // agregarAmenidadHotel
    // =========================================================================

    @Test
    @DisplayName("agregarAmenidadHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void agregarAmenidadHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(5)).thenReturn(false);
        AgregarAmenidadRequestDTO req = new AgregarAmenidadRequestDTO();
        req.setAmenidadId(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.agregarAmenidadHotel(5, req));
    }

    @Test
    @DisplayName("agregarAmenidadHotel_amenidadIdInvalida_lanzaIllegalArgumentException")
    void agregarAmenidadHotel_amenidadIdInvalida_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(5)).thenReturn(true);
        AgregarAmenidadRequestDTO req = new AgregarAmenidadRequestDTO();
        req.setAmenidadId(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.agregarAmenidadHotel(5, req));
        assertEquals("Amenidad invalida", ex.getMessage());
    }

    @Test
    @DisplayName("agregarAmenidadHotel_hotelYaTieneAmenidad_lanzaIllegalArgumentException")
    void agregarAmenidadHotel_hotelYaTieneAmenidad_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(5)).thenReturn(true);
        when(hotelRepository.tieneAmenidad(5, 2)).thenReturn(true);
        AgregarAmenidadRequestDTO req = new AgregarAmenidadRequestDTO();
        req.setAmenidadId(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.agregarAmenidadHotel(5, req));
        assertEquals("Este hotel ya tiene esa amenidad asignada.", ex.getMessage());
    }

    @Test
    @DisplayName("agregarAmenidadHotel_datosValidos_retornaMapaConIdMensaje")
    void agregarAmenidadHotel_datosValidos_retornaMapaConIdMensaje() {
        when(hotelRepository.existe(5)).thenReturn(true);
        when(hotelRepository.tieneAmenidad(5, 2)).thenReturn(false);
        when(hotelRepository.agregarAmenidadHotel(5, 2, "")).thenReturn(77);
        AgregarAmenidadRequestDTO req = new AgregarAmenidadRequestDTO();
        req.setAmenidadId(2);

        Map<String, Object> result = service.agregarAmenidadHotel(5, req);

        assertEquals(77,               result.get("id"));
        assertEquals("Amenidad agregada", result.get("mensaje"));
    }

    // =========================================================================
    // actualizarAmenidadHotel
    // =========================================================================

    @Test
    @DisplayName("actualizarAmenidadHotel_delegaAlRepositorio")
    void actualizarAmenidadHotel_delegaAlRepositorio() {
        AgregarAmenidadRequestDTO req = new AgregarAmenidadRequestDTO();
        req.setDescripcion("Nueva desc");

        assertDoesNotThrow(() -> service.actualizarAmenidadHotel(10, req));

        verify(hotelRepository).actualizarAmenidadHotel(10, "Nueva desc");
    }

    // =========================================================================
    // eliminarAmenidadHotel
    // =========================================================================

    @Test
    @DisplayName("eliminarAmenidadHotel_delegaAlRepositorio")
    void eliminarAmenidadHotel_delegaAlRepositorio() {
        assertDoesNotThrow(() -> service.eliminarAmenidadHotel(15));

        verify(hotelRepository).eliminarAmenidadHotel(15);
    }

    // =========================================================================
    // agregarImagenAmenidad
    // =========================================================================

    @Test
    @DisplayName("agregarImagenAmenidad_base64Valido_retornaMapaConId")
    void agregarImagenAmenidad_base64Valido_retornaMapaConId() {
        String base64 = Base64.getEncoder().encodeToString("imagen".getBytes());
        when(hotelRepository.agregarImagenAmenidad(eq(3), any(byte[].class))).thenReturn(88);

        Map<String, Object> result = service.agregarImagenAmenidad(3, base64);

        assertEquals(88,                           result.get("id"));
        assertEquals("Imagen de amenidad agregada", result.get("mensaje"));
    }

    @Test
    @DisplayName("agregarImagenAmenidad_base64Nulo_lanzaIllegalArgumentException")
    void agregarImagenAmenidad_base64Nulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.agregarImagenAmenidad(3, null));
    }

    // =========================================================================
    // eliminarImagenAmenidad
    // =========================================================================

    @Test
    @DisplayName("eliminarImagenAmenidad_delegaAlRepositorio")
    void eliminarImagenAmenidad_delegaAlRepositorio() {
        assertDoesNotThrow(() -> service.eliminarImagenAmenidad(20));

        verify(hotelRepository).eliminarImagenAmenidad(20);
    }

    // =========================================================================
    // agregarImagenHotel
    // =========================================================================

    @Test
    @DisplayName("agregarImagenHotel_hotelExiste_retornaMapaConId")
    void agregarImagenHotel_hotelExiste_retornaMapaConId() {
        String base64 = Base64.getEncoder().encodeToString("img".getBytes());
        when(hotelRepository.existe(7)).thenReturn(true);
        when(hotelRepository.agregarImagenHotel(eq(7), any(byte[].class))).thenReturn(55);

        Map<String, Object> result = service.agregarImagenHotel(7, base64);

        assertEquals(55,              result.get("id"));
        assertEquals("Imagen agregada", result.get("mensaje"));
    }

    @Test
    @DisplayName("agregarImagenHotel_hotelNoExiste_lanzaIllegalArgumentException")
    void agregarImagenHotel_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(99)).thenReturn(false);
        String base64 = Base64.getEncoder().encodeToString("img".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> service.agregarImagenHotel(99, base64));
    }

    // =========================================================================
    // eliminarImagenHotel
    // =========================================================================

    @Test
    @DisplayName("eliminarImagenHotel_delegaAlRepositorio")
    void eliminarImagenHotel_delegaAlRepositorio() {
        assertDoesNotThrow(() -> service.eliminarImagenHotel(30));

        verify(hotelRepository).eliminarImagenHotel(30);
    }

    // =========================================================================
    // listarHabitaciones
    // =========================================================================

    @Test
    @DisplayName("listarHabitaciones_hotelExiste_retornaListaConImagenes")
    void listarHabitaciones_hotelExiste_retornaListaConImagenes() {
        HabitacionAdminDTO hab = new HabitacionAdminDTO();
        when(hotelRepository.existe(1)).thenReturn(true);
        when(hotelRepository.listarHabitacionesPorHotel(1)).thenReturn(List.of(hab));
        when(hotelRepository.obtenerImagenesHabitacionIds(hab.getId()))
                .thenReturn(List.of(10, 11));

        List<HabitacionAdminDTO> resultado = service.listarHabitaciones(1);

        assertEquals(1,             resultado.size());
        assertEquals(List.of(10, 11), resultado.get(0).getImagenesIds());
    }

    @Test
    @DisplayName("listarHabitaciones_hotelNoExiste_lanzaIllegalArgumentException")
    void listarHabitaciones_hotelNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existe(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.listarHabitaciones(99));
        assertEquals("Hotel no encontrado: 99", ex.getMessage());
    }

    // =========================================================================
    // crearHabitacion
    // =========================================================================

    @Test
    @DisplayName("crearHabitacion_datosValidos_retornaMapaConIdMensaje")
    void crearHabitacion_datosValidos_retornaMapaConIdMensaje() {
        CrearHabitacionRequestDTO req = new CrearHabitacionRequestDTO();
        req.setHotelId(1); req.setTipoHabitacionId(2);
        req.setEstadoId(1); req.setDescripcion("Habitacion doble");

        when(hotelRepository.existe(1)).thenReturn(true);
        when(hotelRepository.crearHabitacion(1, 2, "Habitacion doble", 1)).thenReturn(50);

        Map<String, Object> resultado = service.crearHabitacion(req);

        assertEquals(50, resultado.get("id"));
        assertEquals("Habitacion creada correctamente", resultado.get("mensaje"));
        verify(hotelRepository).crearHabitacion(1, 2, "Habitacion doble", 1);
    }

    @Test
    @DisplayName("crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException")
    void crearHabitacion_hotelNoExiste_lanzaIllegalArgumentException() {
        CrearHabitacionRequestDTO req = new CrearHabitacionRequestDTO();
        req.setHotelId(9); req.setTipoHabitacionId(1); req.setEstadoId(1);

        when(hotelRepository.existe(9)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearHabitacion(req));
        assertEquals("Hotel no encontrado: 9", ex.getMessage());
        verify(hotelRepository, never()).crearHabitacion(anyInt(), anyInt(), anyString(), anyInt());
    }

    @Test
    @DisplayName("crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException")
    void crearHabitacion_tipoHabitacionInvalido_lanzaIllegalArgumentException() {
        CrearHabitacionRequestDTO req = new CrearHabitacionRequestDTO();
        req.setHotelId(1); req.setTipoHabitacionId(0); req.setEstadoId(1);

        when(hotelRepository.existe(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearHabitacion(req));
        assertEquals("Tipo de habitacion invalido", ex.getMessage());
    }

    @Test
    @DisplayName("crearHabitacion_estadoInvalido_lanzaIllegalArgumentException")
    void crearHabitacion_estadoInvalido_lanzaIllegalArgumentException() {
        CrearHabitacionRequestDTO req = new CrearHabitacionRequestDTO();
        req.setHotelId(1); req.setTipoHabitacionId(2); req.setEstadoId(5);

        when(hotelRepository.existe(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crearHabitacion(req));
        assertEquals("Estado invalido: use 1 (Activa) o 2 (Cerrada)", ex.getMessage());
    }

    // =========================================================================
    // editarHabitacion
    // =========================================================================

    @Test
    @DisplayName("editarHabitacion_habitacionExiste_invocaActualizarHabitacion")
    void editarHabitacion_habitacionExiste_invocaActualizarHabitacion() {
        EditarHabitacionRequestDTO req = new EditarHabitacionRequestDTO();
        req.setTipoHabitacionId(2); req.setNumeroHabitacion("101");
        req.setDescripcion("Suite"); req.setEstadoId(1);

        when(hotelRepository.existeHabitacion(10)).thenReturn(true);

        assertDoesNotThrow(() -> service.editarHabitacion(10, req));

        verify(hotelRepository).actualizarHabitacion(10, 2, "101", "Suite", 1);
    }

    @Test
    @DisplayName("editarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException")
    void editarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException() {
        EditarHabitacionRequestDTO req = new EditarHabitacionRequestDTO();
        req.setTipoHabitacionId(1); req.setEstadoId(1);

        when(hotelRepository.existeHabitacion(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.editarHabitacion(99, req));
        assertEquals("Habitacion no encontrada: 99", ex.getMessage());
    }

    @Test
    @DisplayName("editarHabitacion_tipoInvalido_lanzaIllegalArgumentException")
    void editarHabitacion_tipoInvalido_lanzaIllegalArgumentException() {
        EditarHabitacionRequestDTO req = new EditarHabitacionRequestDTO();
        req.setTipoHabitacionId(0); req.setEstadoId(1);

        when(hotelRepository.existeHabitacion(10)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.editarHabitacion(10, req));
        assertEquals("Tipo de habitacion invalido", ex.getMessage());
    }

    @Test
    @DisplayName("editarHabitacion_estadoInvalido_lanzaIllegalArgumentException")
    void editarHabitacion_estadoInvalido_lanzaIllegalArgumentException() {
        EditarHabitacionRequestDTO req = new EditarHabitacionRequestDTO();
        req.setTipoHabitacionId(1); req.setEstadoId(9);

        when(hotelRepository.existeHabitacion(10)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.editarHabitacion(10, req));
        assertEquals("Estado invalido: use 1 (Activa) o 2 (Cerrada)", ex.getMessage());
    }

    // =========================================================================
    // eliminarHabitacion
    // =========================================================================

    @Test
    @DisplayName("eliminarHabitacion_habitacionExisteSinReservas_invocaEliminar")
    void eliminarHabitacion_habitacionExisteSinReservas_invocaEliminar() {
        when(hotelRepository.existeHabitacion(10)).thenReturn(true);
        when(hotelRepository.contarReservasActivasHabitacion(10)).thenReturn(0);

        assertDoesNotThrow(() -> service.eliminarHabitacion(10));

        verify(hotelRepository).eliminarHabitacion(10);
    }

    @Test
    @DisplayName("eliminarHabitacion_habitacionExisteConReservas_lanzaIllegalArgumentException")
    void eliminarHabitacion_habitacionExisteConReservas_lanzaIllegalArgumentException() {
        when(hotelRepository.existeHabitacion(10)).thenReturn(true);
        when(hotelRepository.contarReservasActivasHabitacion(10)).thenReturn(2);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.eliminarHabitacion(10));
        assertTrue(ex.getMessage().contains("2 reservacion(es)"));
        verify(hotelRepository, never()).eliminarHabitacion(anyInt());
    }

    @Test
    @DisplayName("eliminarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException")
    void eliminarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existeHabitacion(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.eliminarHabitacion(99));
        assertEquals("Habitacion no encontrada: 99", ex.getMessage());
        verify(hotelRepository, never()).eliminarHabitacion(anyInt());
    }

    // =========================================================================
    // obtenerReservasActivasHabitacion
    // =========================================================================

    @Test
    @DisplayName("obtenerReservasActivasHabitacion_habitacionExisteConReservas_retornaMapaConDatos")
    void obtenerReservasActivasHabitacion_habitacionExisteConReservas_retornaMapaConDatos() {
        Object[] fila = {5, "RES-010", "h@test.com", "Maria Garcia", 180.0};
        List<Object[]> filas = Collections.singletonList(fila);
        when(hotelRepository.existeHabitacion(7)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHabitacion(7)).thenReturn(filas);

        Map<String, Object> resultado = service.obtenerReservasActivasHabitacion(7);

        assertEquals(1, resultado.get("count"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista = (List<Map<String, Object>>) resultado.get("reservaciones");
        assertEquals("RES-010",    lista.get(0).get("noReservacion"));
        assertEquals("h@test.com", lista.get(0).get("correo"));
        assertEquals(180.0,        lista.get(0).get("total"));
    }

    @Test
    @DisplayName("obtenerReservasActivasHabitacion_habitacionExisteSinReservas_retornaCountCero")
    void obtenerReservasActivasHabitacion_habitacionExisteSinReservas_retornaCountCero() {
        when(hotelRepository.existeHabitacion(7)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHabitacion(7))
                .thenReturn(Collections.emptyList());

        Map<String, Object> resultado = service.obtenerReservasActivasHabitacion(7);

        assertEquals(0, resultado.get("count"));
    }

    @Test
    @DisplayName("obtenerReservasActivasHabitacion_habitacionNoExiste_lanzaIllegalArgumentException")
    void obtenerReservasActivasHabitacion_habitacionNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existeHabitacion(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.obtenerReservasActivasHabitacion(99));
        assertEquals("Habitacion no encontrada: 99", ex.getMessage());
    }

    // =========================================================================
    // reactivarHabitacion
    // =========================================================================

    @Test
    @DisplayName("reactivarHabitacion_habitacionExiste_invocaReactivar")
    void reactivarHabitacion_habitacionExiste_invocaReactivar() {
        when(hotelRepository.existeHabitacion(8)).thenReturn(true);

        assertDoesNotThrow(() -> service.reactivarHabitacion(8));

        verify(hotelRepository).reactivarHabitacion(8);
    }

    @Test
    @DisplayName("reactivarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException")
    void reactivarHabitacion_habitacionNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existeHabitacion(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.reactivarHabitacion(99));
        assertEquals("Habitacion no encontrada: 99", ex.getMessage());
        verify(hotelRepository, never()).reactivarHabitacion(anyInt());
    }

    // =========================================================================
    // cerrarHabitacionConCancelaciones
    // =========================================================================

    @Test
    @DisplayName("cerrarHabitacionConCancelaciones_habitacionNoExiste_lanzaIllegalArgumentException")
    void cerrarHabitacionConCancelaciones_habitacionNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existeHabitacion(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.cerrarHabitacionConCancelaciones(99, "Hab 101", false));
        assertEquals("Habitacion no encontrada: 99", ex.getMessage());
    }

    @Test
    @DisplayName("cerrarHabitacionConCancelaciones_sinReservas_cierraHabitacion")
    void cerrarHabitacionConCancelaciones_sinReservas_cierraHabitacion() {
        when(hotelRepository.existeHabitacion(10)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHabitacion(10))
                .thenReturn(Collections.emptyList());

        Map<String, Object> result =
                service.cerrarHabitacionConCancelaciones(10, "Hab 101", false);

        assertEquals(0, result.get("cancelaciones"));
        assertEquals(0, result.get("emailsEnviados"));
        verify(hotelRepository).cerrarHabitacion(10);
        verify(hotelRepository, never()).eliminarHabitacion(anyInt());
    }

    @Test
    @DisplayName("cerrarHabitacionConCancelaciones_sinReservas_eliminaHabitacionDefinitiva")
    void cerrarHabitacionConCancelaciones_sinReservas_eliminaHabitacionDefinitiva() {
        when(hotelRepository.existeHabitacion(10)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHabitacion(10))
                .thenReturn(Collections.emptyList());

        Map<String, Object> result =
                service.cerrarHabitacionConCancelaciones(10, "Hab 101", true);

        assertEquals(0, result.get("cancelaciones"));
        verify(hotelRepository).eliminarHabitacion(10);
        verify(hotelRepository, never()).cerrarHabitacion(anyInt());
    }

    @Test
    @DisplayName("cerrarHabitacionConCancelaciones_conReservas_cancelaEmailsYCierra")
    void cerrarHabitacionConCancelaciones_conReservas_cancelaEmailsYCierra() {
        Object[] fila = {3, "RES-020", "c@test.com", "Carlos Ruiz", 220.0};
        List<Object[]> filas = Collections.singletonList(fila);
        when(hotelRepository.existeHabitacion(10)).thenReturn(true);
        when(hotelRepository.obtenerReservacionesActivasHabitacion(10))
                .thenReturn(filas);

        ResultadoNotificacionDTO dto = new ResultadoNotificacionDTO();
        dto.setEsReservaDeAgencia(false);
        when(agenciaNotificador.notificarCancelacion(eq(3), anyString())).thenReturn(dto);

        try (MockedStatic<EmailHelper> emailMock = Mockito.mockStatic(EmailHelper.class)) {
            emailMock.when(() -> EmailHelper.enviar(anyString(), anyString(), anyString()))
                     .thenAnswer(inv -> null);

            Map<String, Object> result =
                    service.cerrarHabitacionConCancelaciones(10, "Hab 101", false);

            assertEquals(1, result.get("cancelaciones"));
            assertEquals(1, result.get("emailsEnviados"));
            assertEquals(0, result.get("emailsFallidos"));
            verify(hotelRepository).cancelarReservacionesActivasHabitacion(eq(10), anyString());
            verify(hotelRepository).cerrarHabitacion(10);
            verify(hotelRepository, never()).eliminarHabitacion(anyInt());
        }
    }

    // =========================================================================
    // agregarImagenHabitacion
    // =========================================================================

    @Test
    @DisplayName("agregarImagenHabitacion_habitacionExiste_retornaMapaConId")
    void agregarImagenHabitacion_habitacionExiste_retornaMapaConId() {
        String base64 = Base64.getEncoder().encodeToString("hab".getBytes());
        when(hotelRepository.existeHabitacion(5)).thenReturn(true);
        when(hotelRepository.agregarImagenHabitacion(eq(5), any(byte[].class))).thenReturn(99);

        Map<String, Object> result = service.agregarImagenHabitacion(5, base64);

        assertEquals(99,              result.get("id"));
        assertEquals("Imagen agregada", result.get("mensaje"));
    }

    @Test
    @DisplayName("agregarImagenHabitacion_habitacionNoExiste_lanzaIllegalArgumentException")
    void agregarImagenHabitacion_habitacionNoExiste_lanzaIllegalArgumentException() {
        when(hotelRepository.existeHabitacion(99)).thenReturn(false);
        String base64 = Base64.getEncoder().encodeToString("hab".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> service.agregarImagenHabitacion(99, base64));
    }

    // =========================================================================
    // eliminarImagenHabitacion
    // =========================================================================

    @Test
    @DisplayName("eliminarImagenHabitacion_delegaAlRepositorio")
    void eliminarImagenHabitacion_delegaAlRepositorio() {
        assertDoesNotThrow(() -> service.eliminarImagenHabitacion(40));

        verify(hotelRepository).eliminarImagenHabitacion(40);
    }

    // =========================================================================
    // listarTodasReservaciones
    // =========================================================================

    @Test
    @DisplayName("listarTodasReservaciones_delegaAlRepositorio")
    void listarTodasReservaciones_delegaAlRepositorio() {
        List<Map<String, Object>> lista = List.of(Map.of("id", 1));
        when(hotelRepository.listarTodasReservaciones()).thenReturn(lista);

        List<Map<String, Object>> resultado = service.listarTodasReservaciones();

        assertEquals(1, resultado.size());
        verify(hotelRepository).listarTodasReservaciones();
    }

    // =========================================================================
    // obtenerMetricas
    // =========================================================================

    @Test
    @DisplayName("obtenerMetricas_delegaAlRepositorio")
    void obtenerMetricas_delegaAlRepositorio() {
        Map<String, Object> metricas = Map.of("totalReservaciones", 100);
        when(hotelRepository.obtenerMetricas()).thenReturn(metricas);

        Map<String, Object> resultado = service.obtenerMetricas();

        assertEquals(100, resultado.get("totalReservaciones"));
        verify(hotelRepository).obtenerMetricas();
    }
}
