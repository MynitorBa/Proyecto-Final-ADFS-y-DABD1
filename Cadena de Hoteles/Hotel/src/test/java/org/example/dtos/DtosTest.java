package org.example.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para todos los DTOs del proyecto.
 * Valida constructores, setters/getters y valores por defecto.
 */
@DisplayName("DTOs - Pruebas de POJOs")
class DtosTest {

    // ===== AerolineaAdminDTO =====

    @Test
    @DisplayName("AerolineaAdminDTO - setters y getters funcionan")
    void aerolineaAdminDTO_settersYGetters_funcionan() {
        AerolineaAdminDTO dto = new AerolineaAdminDTO();
        dto.setId(10);
        dto.setNombre("Broom Air");
        dto.setUsuarioWebis(5);
        dto.setUsuarioUsername("webuser");
        dto.setPorcentajeDescuento(12.5);
        dto.setEstadoId(1);
        dto.setEstado("Activo");
        dto.setUrl("http://broom.com");
        dto.setUrlParaUsuario("http://broom.com/ui");

        assertEquals(10,             dto.getId());
        assertEquals("Broom Air",    dto.getNombre());
        assertEquals(5,              dto.getUsuarioWebis());
        assertEquals("webuser",      dto.getUsuarioUsername());
        assertEquals(12.5,           dto.getPorcentajeDescuento(), 0.001);
        assertEquals(1,              dto.getEstadoId());
        assertEquals("Activo",       dto.getEstado());
        assertEquals("http://broom.com",    dto.getUrl());
        assertEquals("http://broom.com/ui", dto.getUrlParaUsuario());
    }

    @Test
    @DisplayName("AerolineaAdminDTO - valores por defecto son 0 o null")
    void aerolineaAdminDTO_valoresPorDefecto_sonCero() {
        AerolineaAdminDTO dto = new AerolineaAdminDTO();
        assertEquals(0,    dto.getId());
        assertNull(dto.getNombre());
        assertEquals(0.0,  dto.getPorcentajeDescuento(), 0.001);
    }

    // ===== AerolineaIdentidadDTO =====

    @Test
    @DisplayName("AerolineaIdentidadDTO - constructor almacena nombre y url")
    void aerolineaIdentidadDTO_constructor_almacenaNombreYUrl() {
        AerolineaIdentidadDTO dto = new AerolineaIdentidadDTO(3, "Broom Air", "http://broom.com");
        assertEquals("Broom Air",      dto.getNombre());
        assertEquals("http://broom.com", dto.getUrlAerolinea());
    }

    // ===== AerolineaWebserviceDTO =====

    @Test
    @DisplayName("AerolineaWebserviceDTO - setters y getters funcionan")
    void aerolineaWebserviceDTO_settersYGetters_funcionan() {
        AerolineaWebserviceDTO dto = new AerolineaWebserviceDTO();
        dto.setId(7);
        dto.setNombre("FastAir");
        dto.setUsuarioWebis(2);
        dto.setPorcentajeDescuento(8.0);
        dto.setEstadoId(1);
        dto.setEstado("Activo");
        dto.setUrl("http://fastair.com");
        dto.setUrlParaUsuario("http://fastair.com/ui");

        assertEquals(7,            dto.getId());
        assertEquals("FastAir",    dto.getNombre());
        assertEquals(2,            dto.getUsuarioWebis());
        assertEquals(8.0,          dto.getPorcentajeDescuento(), 0.001);
        assertEquals(1,            dto.getEstadoId());
        assertEquals("Activo",     dto.getEstado());
    }

    // ===== AgenciaDTO =====

    @Test
    @DisplayName("AgenciaDTO - setters y getters funcionan")
    void agenciaDTO_settersYGetters_funcionan() {
        AgenciaDTO dto = new AgenciaDTO();
        dto.setId(20);
        dto.setNombre("Viajes GT");
        dto.setCorreo("agencia@gt.com");
        dto.setUsuarioWebisId(3);
        dto.setPorcentajeDescuento(15.0);
        dto.setEstadoId(1);
        dto.setEstado("Activo");
        dto.setUrlAgencia("http://viajes-gt.com");

        assertEquals(20,              dto.getId());
        assertEquals("Viajes GT",     dto.getNombre());
        assertEquals("agencia@gt.com",dto.getCorreo());
        assertEquals(3,               dto.getUsuarioWebisId());
        assertEquals(15.0,            dto.getPorcentajeDescuento(), 0.001);
        assertEquals("http://viajes-gt.com", dto.getUrlAgencia());
    }

    @Test
    @DisplayName("AgenciaDTO - valores por defecto son 0 o null")
    void agenciaDTO_valoresPorDefecto_sonCero() {
        AgenciaDTO dto = new AgenciaDTO();
        assertEquals(0,   dto.getId());
        assertNull(dto.getNombre());
        assertNull(dto.getCorreo());
    }

    // ===== AgenciaIdentidad =====

    @Test
    @DisplayName("AgenciaIdentidad - setters y getters funcionan")
    void agenciaIdentidad_settersYGetters_funcionan() {
        AgenciaIdentidad dto = new AgenciaIdentidad();
        dto.setId(5);
        dto.setNombre("Agencia Test");
        dto.setUrlAgencia("http://agencia-test.com");

        assertEquals(5,                        dto.getId());
        assertEquals("Agencia Test",           dto.getNombre());
        assertEquals("http://agencia-test.com",dto.getUrlAgencia());
    }

    // ===== AgregarAmenidadRequestDTO =====

    @Test
    @DisplayName("AgregarAmenidadRequestDTO - setters y getters funcionan")
    void agregarAmenidadRequestDTO_settersYGetters_funcionan() {
        AgregarAmenidadRequestDTO dto = new AgregarAmenidadRequestDTO();
        dto.setAmenidadId(3);
        dto.setDescripcion("Piscina cubierta");

        assertEquals(3,                  dto.getAmenidadId());
        assertEquals("Piscina cubierta", dto.getDescripcion());
    }

    // ===== AmenidadDTO =====

    @Test
    @DisplayName("AmenidadDTO - constructor almacena id y nombre")
    void amenidadDTO_constructor_almacenaIdYNombre() {
        AmenidadDTO dto = new AmenidadDTO(4, "Gimnasio");
        assertEquals(4,         dto.getId());
        assertEquals("Gimnasio",dto.getNombre());
    }

    @Test
    @DisplayName("AmenidadDTO - setters sobrescriben valores del constructor")
    void amenidadDTO_setters_sobrescribenValores() {
        AmenidadDTO dto = new AmenidadDTO(4, "Gimnasio");
        dto.setId(99);
        dto.setNombre("Spa");

        assertEquals(99,    dto.getId());
        assertEquals("Spa", dto.getNombre());
    }

    // ===== AmenidadHotelDTO =====

    @Test
    @DisplayName("AmenidadHotelDTO - setters y getters funcionan")
    void amenidadHotelDTO_settersYGetters_funcionan() {
        AmenidadHotelDTO dto = new AmenidadHotelDTO();
        dto.setHotelAmenidadId(1);
        dto.setAmenidadId(2);
        dto.setNombre("Piscina");
        dto.setDescripcion("Piscina olimpica");

        assertEquals(1,                  dto.getHotelAmenidadId());
        assertEquals(2,                  dto.getAmenidadId());
        assertEquals("Piscina",          dto.getNombre());
        assertEquals("Piscina olimpica", dto.getDescripcion());
    }

    // ===== BusquedaRequestDTO =====

    @Test
    @DisplayName("BusquedaRequestDTO - setters y getters funcionan")
    void busquedaRequestDTO_settersYGetters_funcionan() {
        BusquedaRequestDTO dto = new BusquedaRequestDTO();
        dto.setPais("Guatemala");
        dto.setCiudad("Ciudad de Guatemala");
        dto.setFechaCheckIn("2026-05-01");
        dto.setFechaCheckOut("2026-05-05");
        dto.setCantidadPersonas(2);

        assertEquals("Guatemala",             dto.getPais());
        assertEquals("Ciudad de Guatemala",   dto.getCiudad());
        assertEquals("2026-05-01",            dto.getFechaCheckIn());
        assertEquals("2026-05-05",            dto.getFechaCheckOut());
        assertEquals(2,                       dto.getCantidadPersonas());
    }

    @Test
    @DisplayName("BusquedaRequestDTO - valores por defecto son null o 0")
    void busquedaRequestDTO_valoresPorDefecto_sonNullOCero() {
        BusquedaRequestDTO dto = new BusquedaRequestDTO();
        assertNull(dto.getPais());
        assertNull(dto.getCiudad());
        assertEquals(0, dto.getCantidadPersonas());
    }

    // ===== CambiarContrasenaRequestDTO =====

    @Test
    @DisplayName("CambiarContrasenaRequestDTO - setters y getters funcionan")
    void cambiarContrasenaRequestDTO_settersYGetters_funcionan() {
        CambiarContrasenaRequestDTO dto = new CambiarContrasenaRequestDTO();
        dto.setContrasenaActual("oldPass123");
        dto.setContrasenaNueva("newPass456");

        assertEquals("oldPass123", dto.getContrasenaActual());
        assertEquals("newPass456", dto.getContrasenaNueva());
    }

    // ===== CambiarRolRequestDTO =====

    @Test
    @DisplayName("CambiarRolRequestDTO - setter y getter de rolId funcionan")
    void cambiarRolRequestDTO_setterYGetter_funcionan() {
        CambiarRolRequestDTO dto = new CambiarRolRequestDTO();
        dto.setRolId(3);
        assertEquals(3, dto.getRolId());
    }

    // ===== CambiarTelefonoRequestDTO =====

    @Test
    @DisplayName("CambiarTelefonoRequestDTO - setter y getter de telefono funcionan")
    void cambiarTelefonoRequestDTO_setterYGetter_funcionan() {
        CambiarTelefonoRequestDTO dto = new CambiarTelefonoRequestDTO();
        dto.setTelefono("55551234");
        assertEquals("55551234", dto.getTelefono());
    }

    // ===== CancelacionRequestDTO =====

    @Test
    @DisplayName("CancelacionRequestDTO - setter y getter de motivo funcionan")
    void cancelacionRequestDTO_setterYGetter_funcionan() {
        CancelacionRequestDTO dto = new CancelacionRequestDTO();
        dto.setMotivoCancelacion("Cambio de planes");
        assertEquals("Cambio de planes", dto.getMotivoCancelacion());
    }

    // ===== CiudadDTO =====

    @Test
    @DisplayName("CiudadDTO - setters y getters funcionan")
    void ciudadDTO_settersYGetters_funcionan() {
        CiudadDTO dto = new CiudadDTO();
        dto.setId(1);
        dto.setNombre("Guatemala");
        dto.setPaisId(2);
        dto.setPaisNombre("Guatemala");

        assertEquals(1,           dto.getId());
        assertEquals("Guatemala", dto.getNombre());
        assertEquals(2,           dto.getPaisId());
        assertEquals("Guatemala", dto.getPaisNombre());
    }

    // ===== ComentarioRequestDTO =====

    @Test
    @DisplayName("ComentarioRequestDTO - setters y getters funcionan incluyendo Integer null")
    void comentarioRequestDTO_settersYGetters_funcionan() {
        ComentarioRequestDTO dto = new ComentarioRequestDTO();
        dto.setHotelId(5);
        dto.setComentarioPadreId(null);
        dto.setResena(4);
        dto.setContenido("Excelente hotel");

        assertEquals(5,                  dto.getHotelId());
        assertNull(dto.getComentarioPadreId());
        assertEquals(4,                  dto.getResena());
        assertEquals("Excelente hotel",  dto.getContenido());
    }

    // ===== ComentarioResponseDTO =====

    @Test
    @DisplayName("ComentarioResponseDTO - setters y getters funcionan")
    void comentarioResponseDTO_settersYGetters_funcionan() {
        ComentarioResponseDTO dto = new ComentarioResponseDTO();
        dto.setId(10);
        dto.setUsuarioId(3);
        dto.setUsername("miku_user");
        dto.setHotelId(7);
        dto.setContenido("Muy buen lugar");
        dto.setFecha("2026-04-23");
        dto.setDowns(2);

        assertEquals(10,              dto.getId());
        assertEquals(3,               dto.getUsuarioId());
        assertEquals("miku_user",     dto.getUsername());
        assertEquals(7,               dto.getHotelId());
        assertEquals("Muy buen lugar",dto.getContenido());
        assertEquals("2026-04-23",    dto.getFecha());
        assertEquals(2,               dto.getDowns());
    }

    // ===== CrearAerolineaAdminRequestDTO =====

    @Test
    @DisplayName("CrearAerolineaAdminRequestDTO - setters y getters funcionan")
    void crearAerolineaAdminRequestDTO_settersYGetters_funcionan() {
        CrearAerolineaAdminRequestDTO dto = new CrearAerolineaAdminRequestDTO();
        dto.setNombre("SkyLine Air");
        dto.setUrl("http://skyline.com");
        dto.setUrlParaUsuario("http://skyline.com/ui");
        dto.setUsuarioWebisId(4);

        assertEquals("SkyLine Air",         dto.getNombre());
        assertEquals("http://skyline.com",  dto.getUrl());
        assertEquals("http://skyline.com/ui",dto.getUrlParaUsuario());
        assertEquals(4,                     dto.getUsuarioWebisId());
    }

    // ===== CrearAerolineaRequestDTO =====

    @Test
    @DisplayName("CrearAerolineaRequestDTO - setters y getters funcionan")
    void crearAerolineaRequestDTO_settersYGetters_funcionan() {
        CrearAerolineaRequestDTO dto = new CrearAerolineaRequestDTO();
        dto.setNombre("SunAir");
        dto.setUrl("http://sunair.com");
        dto.setUrlParaUsuario("http://sunair.com/ui");

        assertEquals("SunAir",              dto.getNombre());
        assertEquals("http://sunair.com",   dto.getUrl());
        assertEquals("http://sunair.com/ui",dto.getUrlParaUsuario());
    }

    // ===== CrearAgenciaAdminRequestDTO =====

    @Test
    @DisplayName("CrearAgenciaAdminRequestDTO - setters y getters funcionan")
    void crearAgenciaAdminRequestDTO_settersYGetters_funcionan() {
        CrearAgenciaAdminRequestDTO dto = new CrearAgenciaAdminRequestDTO();
        dto.setNombre("Agencia Central");
        dto.setCorreo("central@agt.com");
        dto.setUrlAgencia("http://central.com");
        dto.setUsuarioWebisId(9);

        assertEquals("Agencia Central",   dto.getNombre());
        assertEquals("central@agt.com",   dto.getCorreo());
        assertEquals("http://central.com",dto.getUrlAgencia());
        assertEquals(9,                   dto.getUsuarioWebisId());
    }

    // ===== CrearAgenciaRequestDTO =====

    @Test
    @DisplayName("CrearAgenciaRequestDTO - setters y getters funcionan")
    void crearAgenciaRequestDTO_settersYGetters_funcionan() {
        CrearAgenciaRequestDTO dto = new CrearAgenciaRequestDTO();
        dto.setNombre("Agencia Express");
        dto.setCorreo("express@gt.com");
        dto.setUrlAgencia("http://express.com");

        assertEquals("Agencia Express",  dto.getNombre());
        assertEquals("express@gt.com",   dto.getCorreo());
        assertEquals("http://express.com",dto.getUrlAgencia());
    }

    // ===== CrearHabitacionRequestDTO =====

    @Test
    @DisplayName("CrearHabitacionRequestDTO - setters y getters funcionan")
    void crearHabitacionRequestDTO_settersYGetters_funcionan() {
        CrearHabitacionRequestDTO dto = new CrearHabitacionRequestDTO();
        dto.setHotelId(2);
        dto.setTipoHabitacionId(1);
        dto.setDescripcion("Suite junior");
        dto.setEstadoId(1);

        assertEquals(2,              dto.getHotelId());
        assertEquals(1,              dto.getTipoHabitacionId());
        assertEquals("Suite junior", dto.getDescripcion());
        assertEquals(1,              dto.getEstadoId());
    }

    // ===== CrearHotelRequestDTO =====

    @Test
    @DisplayName("CrearHotelRequestDTO - setters y getters funcionan")
    void crearHotelRequestDTO_settersYGetters_funcionan() {
        CrearHotelRequestDTO dto = new CrearHotelRequestDTO();
        dto.setNombre("Miku Inn");
        dto.setDireccion("Zona 10, Calle 5");
        dto.setDescripcion("Hotel boutique");
        dto.setRating(4.5);
        dto.setEstadoId(1);
        dto.setCiudad("Ciudad de Guatemala");
        dto.setPaisNombre("Guatemala");

        assertEquals("Miku Inn",             dto.getNombre());
        assertEquals("Zona 10, Calle 5",     dto.getDireccion());
        assertEquals("Hotel boutique",       dto.getDescripcion());
        assertEquals(4.5,                    dto.getRating(), 0.001);
        assertEquals(1,                      dto.getEstadoId());
        assertEquals("Ciudad de Guatemala",  dto.getCiudad());
        assertEquals("Guatemala",            dto.getPaisNombre());
    }

    // ===== DownRequestDTO =====

    @Test
    @DisplayName("DownRequestDTO - setter y getter de valor funcionan")
    void downRequestDTO_setterYGetter_funcionan() {
        DownRequestDTO dto = new DownRequestDTO();
        dto.setValor(1);
        assertEquals(1, dto.getValor());
    }

    @Test
    @DisplayName("DownRequestDTO - valor negativo se almacena correctamente")
    void downRequestDTO_valorNegativo_seAlmacenaCorrectamente() {
        DownRequestDTO dto = new DownRequestDTO();
        dto.setValor(-1);
        assertEquals(-1, dto.getValor());
    }

    // ===== DownResponseDTO =====

    @Test
    @DisplayName("DownResponseDTO - setters y getters funcionan")
    void downResponseDTO_settersYGetters_funcionan() {
        DownResponseDTO dto = new DownResponseDTO();
        dto.setId(1);
        dto.setComentarioId(5);
        dto.setValor(1);
        dto.setFecha("2026-04-23");
        dto.setHotelId(3);
        dto.setContenidoComentario("Gran hotel");

        assertEquals(1,             dto.getId());
        assertEquals(5,             dto.getComentarioId());
        assertEquals(1,             dto.getValor());
        assertEquals("2026-04-23",  dto.getFecha());
        assertEquals(3,             dto.getHotelId());
        assertEquals("Gran hotel",  dto.getContenidoComentario());
    }

    // ===== EditarAerolineaRequestDTO =====

    @Test
    @DisplayName("EditarAerolineaRequestDTO - setters y getters funcionan")
    void editarAerolineaRequestDTO_settersYGetters_funcionan() {
        EditarAerolineaRequestDTO dto = new EditarAerolineaRequestDTO();
        dto.setNombre("Nueva Aerolinea");
        dto.setUrl("http://nueva.com");
        dto.setUrlParaUsuario("http://nueva.com/ui");
        dto.setPorcentajeDescuento(10.0);
        dto.setEstadoId(2);

        assertEquals("Nueva Aerolinea",    dto.getNombre());
        assertEquals("http://nueva.com",   dto.getUrl());
        assertEquals(10.0,                 dto.getPorcentajeDescuento(), 0.001);
        assertEquals(2,                    dto.getEstadoId());
    }

    // ===== EditarAgenciaRequestDTO =====

    @Test
    @DisplayName("EditarAgenciaRequestDTO - setters y getters funcionan")
    void editarAgenciaRequestDTO_settersYGetters_funcionan() {
        EditarAgenciaRequestDTO dto = new EditarAgenciaRequestDTO();
        dto.setNombre("Agencia Editada");
        dto.setCorreo("edit@agt.com");
        dto.setUrlAgencia("http://edit.com");
        dto.setPorcentajeDescuento(5.5);
        dto.setEstadoId(1);

        assertEquals("Agencia Editada",  dto.getNombre());
        assertEquals("edit@agt.com",     dto.getCorreo());
        assertEquals("http://edit.com",  dto.getUrlAgencia());
        assertEquals(5.5,                dto.getPorcentajeDescuento(), 0.001);
        assertEquals(1,                  dto.getEstadoId());
    }

    // ===== EditarHabitacionRequestDTO =====

    @Test
    @DisplayName("EditarHabitacionRequestDTO - setters y getters funcionan")
    void editarHabitacionRequestDTO_settersYGetters_funcionan() {
        EditarHabitacionRequestDTO dto = new EditarHabitacionRequestDTO();
        dto.setTipoHabitacionId(2);
        dto.setNumeroHabitacion("101");
        dto.setDescripcion("Suite renovada");
        dto.setEstadoId(1);

        assertEquals(2,               dto.getTipoHabitacionId());
        assertEquals("101",           dto.getNumeroHabitacion());
        assertEquals("Suite renovada",dto.getDescripcion());
        assertEquals(1,               dto.getEstadoId());
    }

    // ===== EditarHotelRequestDTO =====

    @Test
    @DisplayName("EditarHotelRequestDTO - setters y getters funcionan")
    void editarHotelRequestDTO_settersYGetters_funcionan() {
        EditarHotelRequestDTO dto = new EditarHotelRequestDTO();
        dto.setNombre("Hotel Editado");
        dto.setDireccion("Nueva Direccion 10");
        dto.setDescripcion("Nueva descripcion");
        dto.setRating(4.8);
        dto.setEstadoId(1);

        assertEquals("Hotel Editado",      dto.getNombre());
        assertEquals("Nueva Direccion 10", dto.getDireccion());
        assertEquals("Nueva descripcion",  dto.getDescripcion());
        assertEquals(4.8,                  dto.getRating(), 0.001);
        assertEquals(1,                    dto.getEstadoId());
    }

    // ===== HabitacionAdminDTO =====

    @Test
    @DisplayName("HabitacionAdminDTO - setters y getters funcionan")
    void habitacionAdminDTO_settersYGetters_funcionan() {
        HabitacionAdminDTO dto = new HabitacionAdminDTO();
        dto.setId(1);
        dto.setHotelId(2);
        dto.setTipoHabitacionId(3);
        dto.setTipoHabitacion("Suite");
        dto.setNumeroHabitacion("201");
        dto.setTipoCama("King");
        dto.setPrecioPorPersona(150.0);
        dto.setPrecioPorNoche(200.0);
        dto.setCapacidadMaxima(2);
        dto.setMetrosCuadrados(45.5);
        dto.setDescripcion("Suite de lujo");
        dto.setEstadoId(1);
        dto.setEstado("Disponible");

        assertEquals(1,             dto.getId());
        assertEquals(2,             dto.getHotelId());
        assertEquals("Suite",       dto.getTipoHabitacion());
        assertEquals("201",         dto.getNumeroHabitacion());
        assertEquals("King",        dto.getTipoCama());
        assertEquals(150.0,         dto.getPrecioPorPersona(), 0.001);
        assertEquals(200.0,         dto.getPrecioPorNoche(), 0.001);
        assertEquals(2,             dto.getCapacidadMaxima());
        assertEquals(45.5,          dto.getMetrosCuadrados(), 0.001);
        assertEquals("Disponible",  dto.getEstado());
    }

    // ===== HabitacionAgenciaDTO =====

    @Test
    @DisplayName("HabitacionAgenciaDTO - setters y getters de descuento funcionan")
    void habitacionAgenciaDTO_settersYGetters_funcionan() {
        HabitacionAgenciaDTO dto = new HabitacionAgenciaDTO();
        dto.setPorcentajeDescuento(10.0);
        dto.setPrecioPorNocheConDescuento(180.0);
        dto.setPrecioPorPersonaConDescuento(135.0);

        assertEquals(10.0,  dto.getPorcentajeDescuento(), 0.001);
        assertEquals(180.0, dto.getPrecioPorNocheConDescuento(), 0.001);
        assertEquals(135.0, dto.getPrecioPorPersonaConDescuento(), 0.001);
    }

    // ===== HabitacionAgenciaResponseDTO =====

    @Test
    @DisplayName("HabitacionAgenciaResponseDTO - setters y getters funcionan")
    void habitacionAgenciaResponseDTO_settersYGetters_funcionan() {
        HabitacionAgenciaResponseDTO dto = new HabitacionAgenciaResponseDTO();
        dto.setHabitacionId(5);
        dto.setPrecioPorNoche(120.0);
        dto.setPrecioPorPersona(80.0);
        dto.setPersonasExtra(1);

        assertEquals(5,     dto.getHabitacionId());
        assertEquals(120.0, dto.getPrecioPorNoche(), 0.001);
        assertEquals(80.0,  dto.getPrecioPorPersona(), 0.001);
        assertEquals(1,     dto.getPersonasExtra());
    }

    // ===== HabitacionDTO =====

    @Test
    @DisplayName("HabitacionDTO - setters y getters funcionan")
    void habitacionDTO_settersYGetters_funcionan() {
        HabitacionDTO dto = new HabitacionDTO();
        dto.setId(10);
        dto.setTipoHabitacion("Doble");
        dto.setPrecioPorPersona(75.0);
        dto.setPrecioPorNoche(120.0);
        dto.setCapacidadMaxima(3);
        dto.setTipoCama("Queen");
        dto.setMetrosCuadrados(30.0);
        dto.setDescripcion("Habitacion doble comoda");
        dto.setEstado("Disponible");

        assertEquals(10,                          dto.getId());
        assertEquals("Doble",                     dto.getTipoHabitacion());
        assertEquals(75.0,                        dto.getPrecioPorPersona(), 0.001);
        assertEquals(120.0,                       dto.getPrecioPorNoche(), 0.001);
        assertEquals(3,                           dto.getCapacidadMaxima());
        assertEquals("Queen",                     dto.getTipoCama());
        assertEquals(30.0,                        dto.getMetrosCuadrados(), 0.001);
        assertEquals("Habitacion doble comoda",   dto.getDescripcion());
        assertEquals("Disponible",                dto.getEstado());
    }

    // ===== HabitacionReservaRequestDTO =====

    @Test
    @DisplayName("HabitacionReservaRequestDTO - setters y getters funcionan")
    void habitacionReservaRequestDTO_settersYGetters_funcionan() {
        HabitacionReservaRequestDTO dto = new HabitacionReservaRequestDTO();
        dto.setHabitacionId(3);
        dto.setCantidadPersonas(2);
        dto.setFechaCheckIn("2026-06-01");
        dto.setFechaCheckOut("2026-06-05");

        assertEquals(3,            dto.getHabitacionId());
        assertEquals(2,            dto.getCantidadPersonas());
        assertEquals("2026-06-01", dto.getFechaCheckIn());
        assertEquals("2026-06-05", dto.getFechaCheckOut());
    }

    // ===== HabitacionResumenDTO =====

    @Test
    @DisplayName("HabitacionResumenDTO - setters y getters funcionan")
    void habitacionResumenDTO_settersYGetters_funcionan() {
        HabitacionResumenDTO dto = new HabitacionResumenDTO();
        dto.setId(1);
        dto.setNumeroHabitacion("301");

        assertEquals(1,     dto.getId());
        assertEquals("301", dto.getNumeroHabitacion());
    }

    // ===== HandshakeRequestDTO =====

    @Test
    @DisplayName("HandshakeRequestDTO - setters y getters funcionan")
    void handshakeRequestDTO_settersYGetters_funcionan() {
        HandshakeRequestDTO dto = new HandshakeRequestDTO();
        dto.setTokenEntrada("token-entrada-abc");
        dto.setUrlAgencia("http://agencia.com");

        assertEquals("token-entrada-abc",  dto.getTokenEntrada());
        assertEquals("http://agencia.com", dto.getUrlAgencia());
    }

    // ===== HandshakeResponseDTO =====

    @Test
    @DisplayName("HandshakeResponseDTO - constructor almacena tokenSalida")
    void handshakeResponseDTO_constructor_almacenaToken() {
        HandshakeResponseDTO dto = new HandshakeResponseDTO("token-salida-xyz");
        assertEquals("token-salida-xyz", dto.getTokenSalida());
    }

    @Test
    @DisplayName("HandshakeResponseDTO - setter sobrescribe token")
    void handshakeResponseDTO_setter_sobrescribeToken() {
        HandshakeResponseDTO dto = new HandshakeResponseDTO("original");
        dto.setTokenSalida("nuevo-token");
        assertEquals("nuevo-token", dto.getTokenSalida());
    }

    // ===== HotelAdminDTO =====

    @Test
    @DisplayName("HotelAdminDTO - setters y getters funcionan")
    void hotelAdminDTO_settersYGetters_funcionan() {
        HotelAdminDTO dto = new HotelAdminDTO();
        dto.setId(1);
        dto.setNombre("Hotel Central");
        dto.setDireccion("Av. Reforma 10");
        dto.setDescripcion("Hotel de negocios");
        dto.setRating(4.2);
        dto.setEstadoId(1);
        dto.setEstado("Abierto");
        dto.setCiudad("Guatemala");
        dto.setPais("Guatemala");
        dto.setCantidadHabitaciones(50);

        assertEquals(1,                   dto.getId());
        assertEquals("Hotel Central",     dto.getNombre());
        assertEquals("Av. Reforma 10",    dto.getDireccion());
        assertEquals(4.2,                 dto.getRating(), 0.001);
        assertEquals("Abierto",           dto.getEstado());
        assertEquals("Guatemala",         dto.getCiudad());
        assertEquals(50,                  dto.getCantidadHabitaciones());
    }

    // ===== HotelAgenciaDTO =====

    @Test
    @DisplayName("HotelAgenciaDTO - setters y getters funcionan")
    void hotelAgenciaDTO_settersYGetters_funcionan() {
        HotelAgenciaDTO dto = new HotelAgenciaDTO();
        dto.setId(2);
        dto.setNombre("Hotel Agencia");
        dto.setCiudad("Xela");
        dto.setPais("Guatemala");

        assertEquals(2,              dto.getId());
        assertEquals("Hotel Agencia",dto.getNombre());
        assertEquals("Xela",         dto.getCiudad());
        assertEquals("Guatemala",    dto.getPais());
    }

    // ===== HotelAmenidadDTO =====

    @Test
    @DisplayName("HotelAmenidadDTO - setters y getters funcionan")
    void hotelAmenidadDTO_settersYGetters_funcionan() {
        HotelAmenidadDTO dto = new HotelAmenidadDTO();
        dto.setId(1);
        dto.setHotelId(3);
        dto.setAmenidadId(2);
        dto.setAmenidadNombre("WiFi");
        dto.setDescripcion("WiFi de alta velocidad");

        assertEquals(1,                       dto.getId());
        assertEquals(3,                       dto.getHotelId());
        assertEquals(2,                       dto.getAmenidadId());
        assertEquals("WiFi",                  dto.getAmenidadNombre());
        assertEquals("WiFi de alta velocidad",dto.getDescripcion());
    }

    // ===== HotelResultadoDTO =====

    @Test
    @DisplayName("HotelResultadoDTO - setters y getters basicos funcionan")
    void hotelResultadoDTO_settersYGetters_funcionan() {
        HotelResultadoDTO dto = new HotelResultadoDTO();
        dto.setId(5);
        dto.setNombre("Resort Paraiso");
        dto.setDireccion("Playa Norte 1");
        dto.setCiudad("Antigua");
        dto.setPais("Guatemala");
        dto.setDescripcion("Resort frente al mar");
        dto.setRating(4.9);
        dto.setEstado("Abierto");

        assertEquals(5,                   dto.getId());
        assertEquals("Resort Paraiso",    dto.getNombre());
        assertEquals("Playa Norte 1",     dto.getDireccion());
        assertEquals("Antigua",           dto.getCiudad());
        assertEquals("Guatemala",         dto.getPais());
        assertEquals(4.9,                 dto.getRating(), 0.001);
        assertEquals("Abierto",           dto.getEstado());
    }

    @Test
    @DisplayName("HotelResultadoDTO - listas se pueden asignar")
    void hotelResultadoDTO_listas_seAsignanCorrectamente() {
        HotelResultadoDTO dto = new HotelResultadoDTO();
        List<AmenidadHotelDTO> amenidades = new ArrayList<>();
        amenidades.add(new AmenidadHotelDTO());
        dto.setAmenidades(amenidades);

        assertNotNull(dto.getAmenidades());
        assertEquals(1, dto.getAmenidades().size());
    }

    // ===== LoginRequestDTO =====

    @Test
    @DisplayName("LoginRequestDTO - setters y getters funcionan")
    void loginRequestDTO_settersYGetters_funcionan() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setIdentificador("usuario@test.com");
        dto.setContrasena("clave123");

        assertEquals("usuario@test.com", dto.getIdentificador());
        assertEquals("clave123",         dto.getContrasena());
    }

    @Test
    @DisplayName("LoginRequestDTO - valores por defecto son null")
    void loginRequestDTO_valoresPorDefecto_sonNull() {
        LoginRequestDTO dto = new LoginRequestDTO();
        assertNull(dto.getIdentificador());
        assertNull(dto.getContrasena());
    }

    // ===== LoginResponseDTO =====

    @Test
    @DisplayName("LoginResponseDTO - constructor almacena mensaje username y rolId")
    void loginResponseDTO_constructor_almacenaTodosLosCampos() {
        LoginResponseDTO dto = new LoginResponseDTO("Bienvenido", "admin_user", 1);
        assertEquals("Bienvenido",  dto.getMensaje());
        assertEquals("admin_user",  dto.getUsername());
        assertEquals(1,             dto.getRolId());
    }

    @Test
    @DisplayName("LoginResponseDTO - diferente rol se almacena correctamente")
    void loginResponseDTO_diferencteRol_seAlmacenaCorrectamente() {
        LoginResponseDTO dto = new LoginResponseDTO("OK", "guest_user", 3);
        assertEquals(3,          dto.getRolId());
        assertEquals("guest_user",dto.getUsername());
    }

    // ===== PagoAgenciaRequestDTO =====

    @Test
    @DisplayName("PagoAgenciaRequestDTO - setters y getters funcionan")
    void pagoAgenciaRequestDTO_settersYGetters_funcionan() {
        PagoAgenciaRequestDTO dto = new PagoAgenciaRequestDTO();
        dto.setNit("123456-7");
        dto.setCodigoPostal("01001");

        assertEquals("123456-7", dto.getNit());
        assertEquals("01001",    dto.getCodigoPostal());
    }

    // ===== PagoRequestDTO =====

    @Test
    @DisplayName("PagoRequestDTO - setters y getters de facturacion funcionan")
    void pagoRequestDTO_settersYGetters_facturacion_funcionan() {
        PagoRequestDTO dto = new PagoRequestDTO();
        dto.setNit("987654-3");
        dto.setCodigoPostal("01010");
        dto.setNumeroTarjeta("4111111111111111");
        dto.setNombreTitular("Juan Perez");
        dto.setFechaVencimiento("12/28");
        dto.setCvv("123");

        assertEquals("987654-3",          dto.getNit());
        assertEquals("01010",             dto.getCodigoPostal());
        assertEquals("4111111111111111",  dto.getNumeroTarjeta());
        assertEquals("Juan Perez",        dto.getNombreTitular());
        assertEquals("12/28",             dto.getFechaVencimiento());
        assertEquals("123",               dto.getCvv());
    }

    // ===== PagoResponseDTO =====

    @Test
    @DisplayName("PagoResponseDTO - setters y getters funcionan")
    void pagoResponseDTO_settersYGetters_funcionan() {
        PagoResponseDTO dto = new PagoResponseDTO();
        dto.setFacturaId(100);
        dto.setNoReservacion("RES-001");
        dto.setEstado("Pagado");
        dto.setFecha("2026-04-23");
        dto.setNit("123456-7");
        dto.setCodigoPostal("01001");
        dto.setTotal(350.75);

        assertEquals(100,        dto.getFacturaId());
        assertEquals("RES-001",  dto.getNoReservacion());
        assertEquals("Pagado",   dto.getEstado());
        assertEquals("2026-04-23",dto.getFecha());
        assertEquals(350.75,     dto.getTotal(), 0.001);
    }

    // ===== PaisDTO =====

    @Test
    @DisplayName("PaisDTO - constructor almacena id y nombre")
    void paisDTO_constructor_almacenaIdYNombre() {
        PaisDTO dto = new PaisDTO(10, "Guatemala");
        assertEquals(10,         dto.getId());
        assertEquals("Guatemala",dto.getNombre());
    }

    @Test
    @DisplayName("PaisDTO - setters sobrescriben valores del constructor")
    void paisDTO_setters_sobrescribenValores() {
        PaisDTO dto = new PaisDTO(10, "Guatemala");
        dto.setId(20);
        dto.setNombre("Mexico");
        assertEquals(20,      dto.getId());
        assertEquals("Mexico",dto.getNombre());
    }

    // ===== PuedeCancelarDTO =====

    @Test
    @DisplayName("PuedeCancelarDTO - constructor con true almacena valores correctos")
    void puedeCancelarDTO_constructorTrue_almacenaCorrecto() {
        PuedeCancelarDTO dto = new PuedeCancelarDTO(true, "Dentro del plazo permitido");
        assertTrue(dto.isPuedeCancelar());
        assertEquals("Dentro del plazo permitido", dto.getRazon());
    }

    @Test
    @DisplayName("PuedeCancelarDTO - constructor con false almacena valores correctos")
    void puedeCancelarDTO_constructorFalse_almacenaCorrecto() {
        PuedeCancelarDTO dto = new PuedeCancelarDTO(false, "Ya supero el plazo");
        assertFalse(dto.isPuedeCancelar());
        assertEquals("Ya supero el plazo", dto.getRazon());
    }

    // ===== ReservacionAgenciaResponseDTO =====

    @Test
    @DisplayName("ReservacionAgenciaResponseDTO - setters y getters funcionan")
    void reservacionAgenciaResponseDTO_settersYGetters_funcionan() {
        ReservacionAgenciaResponseDTO dto = new ReservacionAgenciaResponseDTO();
        dto.setId(1);
        dto.setNoReservacion("AG-RES-001");
        dto.setTotal(500.0);
        dto.setFechaCreacion("2026-04-23");

        assertEquals(1,            dto.getId());
        assertEquals("AG-RES-001", dto.getNoReservacion());
        assertEquals(500.0,        dto.getTotal(), 0.001);
        assertEquals("2026-04-23", dto.getFechaCreacion());
    }

    // ===== ReservacionDetalleDTO =====

    @Test
    @DisplayName("ReservacionDetalleDTO - setters y getters funcionan")
    void reservacionDetalleDTO_settersYGetters_funcionan() {
        ReservacionDetalleDTO dto = new ReservacionDetalleDTO();
        dto.setId(2);
        dto.setNoReservacion("RES-DETALLE-001");
        dto.setTotal(750.0);
        dto.setEstado("Pendiente");

        assertEquals(2,                   dto.getId());
        assertEquals("RES-DETALLE-001",   dto.getNoReservacion());
        assertEquals(750.0,               dto.getTotal(), 0.001);
        assertEquals("Pendiente",         dto.getEstado());
    }

    // ===== ReservacionRequestDTO =====

    @Test
    @DisplayName("ReservacionRequestDTO - setter de lista de habitaciones funciona")
    void reservacionRequestDTO_setterHabitaciones_funciona() {
        ReservacionRequestDTO dto = new ReservacionRequestDTO();
        List<HabitacionReservaRequestDTO> habitaciones = new ArrayList<>();
        HabitacionReservaRequestDTO h = new HabitacionReservaRequestDTO();
        h.setHabitacionId(1);
        habitaciones.add(h);
        dto.setHabitaciones(habitaciones);

        assertNotNull(dto.getHabitaciones());
        assertEquals(1, dto.getHabitaciones().size());
        assertEquals(1, dto.getHabitaciones().get(0).getHabitacionId());
    }

    @Test
    @DisplayName("ReservacionRequestDTO - lista vacia se asigna correctamente")
    void reservacionRequestDTO_listaVacia_seAsignaCorrectamente() {
        ReservacionRequestDTO dto = new ReservacionRequestDTO();
        dto.setHabitaciones(new ArrayList<>());
        assertNotNull(dto.getHabitaciones());
        assertTrue(dto.getHabitaciones().isEmpty());
    }

    // ===== ReservacionResponseDTO =====

    @Test
    @DisplayName("ReservacionResponseDTO - setters y getters funcionan")
    void reservacionResponseDTO_settersYGetters_funcionan() {
        ReservacionResponseDTO dto = new ReservacionResponseDTO();
        dto.setId(5);
        dto.setNoReservacion("RES-2026-001");
        dto.setTotal(900.0);
        dto.setEstado("Confirmada");
        dto.setFechaCreacion("2026-04-23");
        dto.setFechaExpiracion("2026-04-25");

        assertEquals(5,             dto.getId());
        assertEquals("RES-2026-001",dto.getNoReservacion());
        assertEquals(900.0,         dto.getTotal(), 0.001);
        assertEquals("Confirmada",  dto.getEstado());
        assertEquals("2026-04-23",  dto.getFechaCreacion());
        assertEquals("2026-04-25",  dto.getFechaExpiracion());
    }

    // ===== ResultadoNotificacionDTO =====

    @Test
    @DisplayName("ResultadoNotificacionDTO - setters y getters booleanos funcionan")
    void resultadoNotificacionDTO_settersYGetters_funcionan() {
        ResultadoNotificacionDTO dto = new ResultadoNotificacionDTO();
        dto.setEsReservaDeAgencia(true);
        dto.setNombreAgencia("Viajes GT");
        dto.setEnviado(true);
        dto.setHttpStatus(200);

        assertTrue(dto.isEsReservaDeAgencia());
        assertEquals("Viajes GT", dto.getNombreAgencia());
        assertTrue(dto.isEnviado());
        assertEquals(200, dto.getHttpStatus());
    }

    // ===== SesionDTO =====

    @Test
    @DisplayName("SesionDTO - setters y getters funcionan incluyendo boolean")
    void sesionDTO_settersYGetters_funcionan() {
        SesionDTO dto = new SesionDTO();
        dto.setUsuarioId(10);
        dto.setUsername("miku_admin");
        dto.setRolId(1);
        dto.setRol("ADMIN");
        dto.setAutenticado(true);

        assertEquals(10,           dto.getUsuarioId());
        assertEquals("miku_admin", dto.getUsername());
        assertEquals(1,            dto.getRolId());
        assertEquals("ADMIN",      dto.getRol());
        assertTrue(dto.isAutenticado());
    }

    @Test
    @DisplayName("SesionDTO - autenticado false por defecto")
    void sesionDTO_autenticadoFalsePorDefecto() {
        SesionDTO dto = new SesionDTO();
        assertFalse(dto.isAutenticado());
        assertEquals(0, dto.getUsuarioId());
        assertNull(dto.getUsername());
    }

    // ===== SubirImagenRequestDTO =====

    @Test
    @DisplayName("SubirImagenRequestDTO - setter y getter de base64 funcionan")
    void subirImagenRequestDTO_setterYGetter_funcionan() {
        SubirImagenRequestDTO dto = new SubirImagenRequestDTO();
        dto.setBase64("data:image/png;base64,iVBORw0KGgo=");
        assertEquals("data:image/png;base64,iVBORw0KGgo=", dto.getBase64());
    }

    // ===== TipoHabitacionResultadoDTO =====

    @Test
    @DisplayName("TipoHabitacionResultadoDTO - setters y getters funcionan")
    void tipoHabitacionResultadoDTO_settersYGetters_funcionan() {
        TipoHabitacionResultadoDTO dto = new TipoHabitacionResultadoDTO();
        dto.setTipoHabitacionId(1);
        dto.setTipoHabitacion("Suite Presidencial");
        dto.setPrecioPorPersona(200.0);
        dto.setPrecioPorNoche(350.0);

        assertEquals(1,                     dto.getTipoHabitacionId());
        assertEquals("Suite Presidencial",  dto.getTipoHabitacion());
        assertEquals(200.0,                 dto.getPrecioPorPersona(), 0.001);
        assertEquals(350.0,                 dto.getPrecioPorNoche(), 0.001);
    }

    // ===== TokenAerolineaRequestDTO =====

    @Test
    @DisplayName("TokenAerolineaRequestDTO - setters y getters funcionan")
    void tokenAerolineaRequestDTO_settersYGetters_funcionan() {
        TokenAerolineaRequestDTO dto = new TokenAerolineaRequestDTO();
        dto.setCiudad("Ciudad de Guatemala");
        dto.setPais("Guatemala");

        assertEquals("Ciudad de Guatemala", dto.getCiudad());
        assertEquals("Guatemala",           dto.getPais());
    }

    // ===== TokenAerolineaResponseDTO =====

    @Test
    @DisplayName("TokenAerolineaResponseDTO - constructor almacena todos los campos")
    void tokenAerolineaResponseDTO_constructor_almacenaTodosLosCampos() {
        TokenAerolineaResponseDTO dto = new TokenAerolineaResponseDTO(
                "jwt-token-xyz",
                "http://redirect.com",
                "2026-05-01T00:00:00"
        );

        assertEquals("jwt-token-xyz",          dto.getToken());
        assertEquals("http://redirect.com",    dto.getUrlRedireccion());
        assertEquals("2026-05-01T00:00:00",    dto.getFechaExpiracion());
    }

    // ===== TokenValidacionResponseDTO =====

    @Test
    @DisplayName("TokenValidacionResponseDTO - constructor almacena todos los campos")
    void tokenValidacionResponseDTO_constructor_almacenaTodosLosCampos() {
        TokenValidacionResponseDTO dto = new TokenValidacionResponseDTO(
                "Ciudad de Guatemala", "Guatemala", 15.0, "2026-05-01T23:59:59");

        assertEquals("Ciudad de Guatemala", dto.getCiudad());
        assertEquals("Guatemala",           dto.getPais());
        assertEquals(15.0,                  dto.getPorcentajeDescuento(), 0.001);
        assertEquals("2026-05-01T23:59:59", dto.getFechaExpiracion());
    }

    @Test
    @DisplayName("TokenValidacionResponseDTO - porcentaje cero se almacena correctamente")
    void tokenValidacionResponseDTO_porcentajeCero_seAlmacenaCorrectamente() {
        TokenValidacionResponseDTO dto = new TokenValidacionResponseDTO(
                "Xela", "Guatemala", 0.0, "2026-06-01T00:00:00");
        assertEquals(0.0, dto.getPorcentajeDescuento(), 0.001);
    }

    // ===== UsuarioAdminDTO =====

    @Test
    @DisplayName("UsuarioAdminDTO - setters y getters funcionan")
    void usuarioAdminDTO_settersYGetters_funcionan() {
        UsuarioAdminDTO dto = new UsuarioAdminDTO();
        dto.setId(1);
        dto.setUsername("admin_test");
        dto.setNombre("Ricardo");
        dto.setApellido("Gonzalez");
        dto.setCorreo("ric@test.com");
        dto.setTelefono("55551234");
        dto.setFechaNacimiento("1999-01-01");
        dto.setRolId(1);
        dto.setRolNombre("ADMIN");

        assertEquals(1,             dto.getId());
        assertEquals("admin_test",  dto.getUsername());
        assertEquals("Ricardo",     dto.getNombre());
        assertEquals("Gonzalez",    dto.getApellido());
        assertEquals("ric@test.com",dto.getCorreo());
        assertEquals("55551234",    dto.getTelefono());
        assertEquals("ADMIN",       dto.getRolNombre());
    }

    // ===== UsuarioPerfilResponseDTO =====

    @Test
    @DisplayName("UsuarioPerfilResponseDTO - setters y getters funcionan")
    void usuarioPerfilResponseDTO_settersYGetters_funcionan() {
        UsuarioPerfilResponseDTO dto = new UsuarioPerfilResponseDTO();
        dto.setId(5);
        dto.setUsername("miku_user");
        dto.setCorreo("miku@hotel.com");
        dto.setPasaporte("GT123456");
        dto.setNombre("Miku");
        dto.setApellido("Inn");
        dto.setTelefono("44447890");
        dto.setFechaNacimiento("2000-03-14");
        dto.setRolId(2);
        dto.setPais("Guatemala");

        assertEquals(5,              dto.getId());
        assertEquals("miku_user",    dto.getUsername());
        assertEquals("miku@hotel.com",dto.getCorreo());
        assertEquals("GT123456",     dto.getPasaporte());
        assertEquals("Miku",         dto.getNombre());
        assertEquals("Inn",          dto.getApellido());
        assertEquals("Guatemala",    dto.getPais());
    }

    // ===== UsuarioValidacionRequestDTO =====

    @Test
    @DisplayName("UsuarioValidacionRequestDTO - setters y getters funcionan")
    void usuarioValidacionRequestDTO_settersYGetters_funcionan() {
        UsuarioValidacionRequestDTO dto = new UsuarioValidacionRequestDTO();
        dto.setUsername("nuevo_user");
        dto.setCorreo("nuevo@test.com");
        dto.setContrasena("pass1234");
        dto.setPasaporte("US789012");

        assertEquals("nuevo_user",    dto.getUsername());
        assertEquals("nuevo@test.com",dto.getCorreo());
        assertEquals("pass1234",      dto.getContrasena());
        assertEquals("US789012",      dto.getPasaporte());
    }

    // ===== UsuarioValidacionResponseDTO =====

    @Test
    @DisplayName("UsuarioValidacionResponseDTO - constructor almacena todos los booleans")
    void usuarioValidacionResponseDTO_constructor_almacenaBooleans() {
        UsuarioValidacionResponseDTO dto = new UsuarioValidacionResponseDTO(true, false, true);
        assertTrue(dto.isUsernameExiste());
        assertFalse(dto.isCorreoExiste());
        assertTrue(dto.isPasaporteExiste());
    }

    @Test
    @DisplayName("UsuarioValidacionResponseDTO - todos false cuando ninguno existe")
    void usuarioValidacionResponseDTO_todosFalse_cuandoNingunExiste() {
        UsuarioValidacionResponseDTO dto = new UsuarioValidacionResponseDTO(false, false, false);
        assertFalse(dto.isUsernameExiste());
        assertFalse(dto.isCorreoExiste());
        assertFalse(dto.isPasaporteExiste());
    }

    // ===== UsuarioWebserviceLibreDTO =====

    @Test
    @DisplayName("UsuarioWebserviceLibreDTO - setters y getters funcionan")
    void usuarioWebserviceLibreDTO_settersYGetters_funcionan() {
        UsuarioWebserviceLibreDTO dto = new UsuarioWebserviceLibreDTO();
        dto.setId(15);
        dto.setUsername("ws_libre_user");

        assertEquals(15,              dto.getId());
        assertEquals("ws_libre_user", dto.getUsername());
    }
}
