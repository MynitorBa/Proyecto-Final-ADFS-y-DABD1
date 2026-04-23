package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AmenidadDTO;
import org.example.dtos.HabitacionAdminDTO;
import org.example.dtos.HotelAdminDTO;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link HotelRepository}.
 * <p>
 * Conecta a Oracle real para verificar todas las operaciones de administracion de
 * hoteles: listado, creacion, actualizacion, cambio de estado, gestion de
 * habitaciones, conteo y metricas del sistema.
 * </p>
 * <p>
 * El {@code @BeforeEach} obtiene referencias reales de {@code Ciudad},
 * {@code Estado}, {@code TipoHabitacion} y {@code EstadoHabitacion}, y luego
 * crea un hotel de prueba mediante el propio metodo del repositorio
 * ({@code crearHotel}) para validar la ruta de insercion real. El
 * {@code @AfterEach} invoca {@code eliminarHotel} del repositorio, que realiza
 * el borrado en cascada (habitaciones, imagenes, amenidades y el propio hotel).
 * Si el hotel ya fue eliminado dentro de un caso de prueba, {@code hotelId} se
 * establece en {@code -1} para omitir el borrado redundante.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Ciudad}, {@code Estado}, {@code TipoHabitacion},
 * {@code EstadoHabitacion}, {@code Hotel}, {@code Habitacion} y {@code Amenidad}
 * accesibles.
 * </p>
 */
@DisplayName("Integracion: HotelRepository - Operaciones de administracion de hoteles contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HotelRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private HotelRepository hotelRepository;

    /**
     * ID del hotel de prueba creado en {@code @BeforeEach}.
     * Se fija en {@code -1} cuando el test lo elimina internamente para evitar
     * doble borrado en {@code @AfterEach}.
     */
    private int hotelId;

    /** ID de la ciudad real obtenida de Oracle en {@code @BeforeEach}. */
    private int ciudadId;

    /** ID del estado activo obtenido de Oracle en {@code @BeforeEach}. */
    private int estadoId;

    /** ID del tipo de habitacion real obtenido de Oracle en {@code @BeforeEach}. */
    private int tipoHabitacionId;

    /** ID del estado de habitacion real obtenido de Oracle en {@code @BeforeEach}. */
    private int estadoHabitacionId;

    /**
     * ID de la amenidad creada durante el test {@code crearAmenidad_nombreValido_retornaIdPositivo}.
     * Se usa en {@code @AfterEach} para eliminarla si no fue limpiada en el propio test.
     */
    private int amenidadIdCreada = -1;

    /**
     * Inicializa el repositorio, obtiene referencias reales de catalogos en Oracle
     * y crea el hotel de prueba usando {@link HotelRepository#crearHotel}.
     * <p>
     * Si no existen registros de referencia necesarios en Oracle, la suite completa
     * se omite mediante {@link Assumptions#assumeTrue}.
     * </p>
     */
    @BeforeEach
    void setUp() {
        hotelRepository = new HotelRepository();

        // 1. Obtiene una ciudad existente en Oracle
        List<Integer> ciudades = DatabaseManager.executeQuery(
                "SELECT ID FROM Ciudad WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay ciudades en Oracle — se omite la prueba");
        ciudadId = ciudades.get(0);

        // 2. Obtiene un estado de hotel existente (ID activo)
        List<Integer> estados = DatabaseManager.executeQuery(
                "SELECT ID FROM Estado WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estados.isEmpty(),
                "No hay estados de hotel en Oracle — se omite la prueba");
        estadoId = estados.get(0);

        // 3. Obtiene un tipo de habitacion existente en Oracle
        List<Integer> tiposHabitacion = DatabaseManager.executeQuery(
                "SELECT ID FROM TipoHabitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!tiposHabitacion.isEmpty(),
                "No hay tipos de habitacion en Oracle — se omite la prueba");
        tipoHabitacionId = tiposHabitacion.get(0);

        // 4. Obtiene un estado de habitacion existente en Oracle
        List<Integer> estadosHabitacion = DatabaseManager.executeQuery(
                "SELECT ID FROM EstadoHabitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estadosHabitacion.isEmpty(),
                "No hay estados de habitacion en Oracle — se omite la prueba");
        estadoHabitacionId = estadosHabitacion.get(0);

        // 5. Crea el hotel de prueba usando el propio metodo del repositorio
        hotelId = hotelRepository.crearHotel(
                "Hotel Test Admin IT",
                "Calle IT 1",
                "Desc IT",
                0.0,
                estadoId,
                ciudadId
        );
        Assumptions.assumeTrue(hotelId > 0,
                "No se pudo crear el hotel de prueba — se omite la prueba");
    }

    /**
     * Elimina todos los recursos creados durante la prueba.
     * Si {@code hotelId} es distinto de {@code -1}, invoca
     * {@link HotelRepository#eliminarHotel} que borra en cascada habitaciones,
     * imagenes, amenidades del hotel y el propio hotel. Si el hotel ya fue
     * eliminado dentro del test, se omite el borrado para evitar errores.
     * Ademas elimina cualquier amenidad suelta creada durante los tests.
     */
    @AfterEach
    void tearDown() {
        if (amenidadIdCreada > 0) {
            DatabaseManager.executeUpdate(
                    "DELETE FROM Amenidad WHERE ID = ?", amenidadIdCreada
            );
            amenidadIdCreada = -1;
        }
        if (hotelId != -1) {
            hotelRepository.eliminarHotel(hotelId);
        }
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code listarAmenidades} retorne una lista no nula desde Oracle.
     * La lista puede estar vacia si no hay amenidades registradas, pero nunca debe
     * ser {@code null}.
     */
    @Test
    @Order(1)
    @DisplayName("1. listarAmenidades retorna lista no nula desde Oracle")
    void listarAmenidades_retornaListaNoNula() {
        List<AmenidadDTO> amenidades = hotelRepository.listarAmenidades();

        assertNotNull(amenidades,
                "listarAmenidades no debe retornar null");
    }

    /**
     * Verifica que {@code crearAmenidad} inserte una amenidad en Oracle y retorne
     * un ID positivo. La amenidad creada se elimina en un bloque {@code finally}
     * para garantizar la limpieza aunque el assertion falle.
     */
    @Test
    @Order(2)
    @DisplayName("2. crearAmenidad con nombre valido retorna ID positivo")
    void crearAmenidad_nombreValido_retornaIdPositivo() {
        int id = -1;
        try {
            id = hotelRepository.crearAmenidad("Amenidad IT Test");

            assertNotNull(id,
                    "crearAmenidad no debe retornar null");
            assertTrue(id > 0,
                    "crearAmenidad debe retornar un ID positivo generado por Oracle");
        } finally {
            if (id > 0) {
                DatabaseManager.executeUpdate("DELETE FROM Amenidad WHERE ID = ?", id);
            }
        }
    }

    /**
     * Verifica que {@code listarTodos} retorne una lista que contenga el hotel
     * insertado en el {@code @BeforeEach}, identificado por su ID.
     * Confirma que la consulta JOIN con {@code Estado}, {@code Ciudad} y
     * {@code Pais} funciona correctamente en Oracle.
     */
    @Test
    @Order(3)
    @DisplayName("3. listarTodos retorna lista que contiene el hotel insertado en setup")
    void listarTodos_retornaListaConElHotelInsertado() {
        List<HotelAdminDTO> hoteles = hotelRepository.listarTodos();

        assertNotNull(hoteles,
                "listarTodos no debe retornar null");

        boolean encontrado = hoteles.stream().anyMatch(h -> h.getId() == hotelId);
        assertTrue(encontrado,
                "La lista de hoteles debe contener el hotel insertado con ID=" + hotelId);
    }

    /**
     * Verifica que {@code actualizarHotel} persista el nuevo nombre en Oracle.
     * Luego de la actualizacion llama a {@code listarTodos} para obtener el registro
     * actualizado y confirma que el nombre sea el nuevo valor enviado.
     */
    @Test
    @Order(4)
    @DisplayName("4. actualizarHotel con datos nuevos actualiza el nombre en Oracle")
    void actualizarHotel_datosNuevos_actualizaNombreEnOracle() {
        String nuevoNombre = "Hotel Test Admin IT Actualizado";
        hotelRepository.actualizarHotel(
                hotelId, nuevoNombre, "Calle IT Actualizada", "Desc Actualizada", 4.5, estadoId
        );

        List<HotelAdminDTO> hoteles = hotelRepository.listarTodos();
        assertNotNull(hoteles,
                "listarTodos no debe retornar null tras la actualizacion");

        HotelAdminDTO hotelActualizado = hoteles.stream()
                .filter(h -> h.getId() == hotelId)
                .findFirst()
                .orElse(null);

        assertNotNull(hotelActualizado,
                "El hotel actualizado debe seguir existiendo en Oracle");
        assertEquals(nuevoNombre, hotelActualizado.getNombre(),
                "El nombre del hotel debe coincidir con el valor actualizado");
    }

    /**
     * Verifica que {@code cerrarHotel} cambie el {@code EstadoID} del hotel a 2
     * en Oracle. Consulta directamente la tabla {@code Hotel} despues de la
     * operacion para confirmar el valor persistido.
     */
    @Test
    @Order(5)
    @DisplayName("5. cerrarHotel con hotel activo cambia EstadoID a 2 en Oracle")
    void cerrarHotel_hotelActivo_cambiaEstadoId() {
        hotelRepository.cerrarHotel(hotelId);

        List<Integer> estadoActual = DatabaseManager.executeQuery(
                "SELECT EstadoID FROM Hotel WHERE ID = ?",
                rs -> rs.getInt("EstadoID"),
                hotelId
        );

        assertNotNull(estadoActual,
                "La consulta de estado no debe retornar null");
        assertFalse(estadoActual.isEmpty(),
                "Debe existir el hotel en Oracle para verificar el estado");
        assertEquals(2, estadoActual.get(0),
                "cerrarHotel debe fijar EstadoID = 2 en Oracle");
    }

    /**
     * Verifica que {@code reactivarHotel} restaure el {@code EstadoID} del hotel a 1
     * despues de haberlo cerrado. Confirma la ida y vuelta del estado directamente
     * contra Oracle.
     */
    @Test
    @Order(6)
    @DisplayName("6. reactivarHotel despues de cerrar restaura EstadoID a 1 en Oracle")
    void reactivarHotel_hotelCerrado_restauraEstadoId() {
        hotelRepository.cerrarHotel(hotelId);
        hotelRepository.reactivarHotel(hotelId);

        List<Integer> estadoActual = DatabaseManager.executeQuery(
                "SELECT EstadoID FROM Hotel WHERE ID = ?",
                rs -> rs.getInt("EstadoID"),
                hotelId
        );

        assertNotNull(estadoActual,
                "La consulta de estado no debe retornar null");
        assertFalse(estadoActual.isEmpty(),
                "Debe existir el hotel en Oracle para verificar el estado");
        assertEquals(1, estadoActual.get(0),
                "reactivarHotel debe restaurar EstadoID = 1 en Oracle");
    }

    /**
     * Verifica que {@code existe} retorne {@code true} para el hotel insertado en
     * el setup y {@code false} para un ID ficticio ({@code -999}) que no puede
     * existir en Oracle.
     */
    @Test
    @Order(7)
    @DisplayName("7. existe retorna true para hotel existente y false para ID inexistente")
    void existe_hotelExistente_retornaTrue() {
        boolean existeReal    = hotelRepository.existe(hotelId);
        boolean existeFicticio = hotelRepository.existe(-999);

        assertTrue(existeReal,
                "existe debe retornar true para el hotel insertado en el setup");
        assertFalse(existeFicticio,
                "existe debe retornar false para un ID que no existe en Oracle");
    }

    /**
     * Verifica que {@code crearHabitacion} inserte una habitacion en Oracle y
     * retorne un ID positivo. Inmediatamente despues verifica que
     * {@code contarHabitaciones} devuelva al menos 1 para ese hotel.
     * La habitacion creada se elimina mediante {@code eliminarHabitacion} en un
     * bloque {@code finally} para no contaminar el teardown.
     */
    @Test
    @Order(8)
    @DisplayName("8-9. crearHabitacion retorna ID positivo y contarHabitaciones refleja al menos 1")
    void crearHabitacion_datosValidos_retornaIdPositivo() {
        int habitacionId = -1;
        try {
            habitacionId = hotelRepository.crearHabitacion(
                    hotelId, tipoHabitacionId, "Habitacion IT Admin", estadoHabitacionId
            );

            assertNotNull(habitacionId,
                    "crearHabitacion no debe retornar null");
            assertTrue(habitacionId > 0,
                    "crearHabitacion debe retornar un ID positivo generado por Oracle");

            int conteo = hotelRepository.contarHabitaciones(hotelId);
            assertTrue(conteo >= 1,
                    "contarHabitaciones debe retornar al menos 1 tras crear una habitacion activa");
        } finally {
            if (habitacionId > 0) {
                hotelRepository.eliminarHabitacion(habitacionId);
            }
        }
    }

    /**
     * Verifica que {@code obtenerMetricas} retorne un mapa no nulo que contenga
     * todas las claves documentadas del sistema: {@code totalUsuarios},
     * {@code hotelesActivos}, {@code reservasActivas}, {@code reservasTotales},
     * {@code ingresosTotales} y {@code hotesTotales}. Confirma ademas que ninguno
     * de esos valores sea {@code null}.
     */
    @Test
    @Order(10)
    @DisplayName("10. obtenerMetricas retorna mapa con todas las claves requeridas y valores no nulos")
    void obtenerMetricas_retornaMapaConClaves() {
        Map<String, Object> metricas = hotelRepository.obtenerMetricas();

        assertNotNull(metricas,
                "obtenerMetricas no debe retornar null");

        String[] clavesRequeridas = {
                "totalUsuarios",
                "hotelesActivos",
                "reservasActivas",
                "reservasTotales",
                "ingresosTotales",
                "hotesTotales"
        };

        for (String clave : clavesRequeridas) {
            assertTrue(metricas.containsKey(clave),
                    "El mapa de metricas debe contener la clave '" + clave + "'");
            assertNotNull(metricas.get(clave),
                    "El valor de la clave '" + clave + "' no debe ser null");
        }
    }
}
