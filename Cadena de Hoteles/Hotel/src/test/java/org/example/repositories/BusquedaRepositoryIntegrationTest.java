package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AmenidadHotelDTO;
import org.example.dtos.HotelResultadoDTO;
import org.example.dtos.TipoHabitacionResultadoDTO;
import org.example.dtos.HabitacionResumenDTO;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link BusquedaRepository}.
 * <p>
 * Conecta a Oracle real para verificar las consultas de busqueda de ciudades,
 * registro de busquedas anonimas, recuperacion de hoteles por ciudad,
 * imagenes de hotel, amenidades y listas de imagenes de habitaciones.
 * </p>
 * <p>
 * El {@code @BeforeEach} obtiene una ciudad y pais existentes en Oracle y los usa
 * a lo largo de los casos que necesiten datos reales. El {@code @AfterEach} elimina
 * unicamente las busquedas anonimas insertadas durante la ejecucion de los tests.
 * No se insertan usuarios propios en esta suite para evitar dependencias de FK
 * con la tabla {@code Usuario}; las busquedas se registran siempre con
 * {@code UsuarioID = NULL}.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en localhost:1521/XEPDB1 con las tablas
 * {@code Ciudad}, {@code Pais} y {@code Busqueda} accesibles.
 * </p>
 */
@DisplayName("Integracion: BusquedaRepository - Consultas de disponibilidad y busquedas contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusquedaRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private BusquedaRepository busquedaRepository;

    /** ID de la ciudad real obtenida de Oracle en {@code @BeforeEach}. */
    private int ciudadIdReal;

    /** Nombre de la ciudad real obtenida de Oracle en {@code @BeforeEach}. */
    private String ciudadNombreReal;

    /** Nombre del pais real obtenido de Oracle en {@code @BeforeEach}. */
    private String paisNombreReal;

    /** Fecha de check-in usada en los tests de guardarBusqueda (10 dias desde hoy). */
    private Date fechaCheckIn;

    /** Fecha de check-out usada en los tests de guardarBusqueda (13 dias desde hoy). */
    private Date fechaCheckOut;

    /**
     * Inicializa el repositorio y obtiene los datos de una ciudad y pais existentes
     * en Oracle para usarlos como precondicion en los casos de prueba.
     * Si no existe ninguna ciudad con pais asociado en la base de datos se omite
     * toda la suite.
     */
    @BeforeEach
    void setUp() {
        busquedaRepository = new BusquedaRepository();

        // Obtiene una ciudad con su pais desde Oracle para los tests que los necesiten
        List<Object[]> ciudades = DatabaseManager.executeQuery(
                "SELECT c.ID, c.Nombre, p.Nombre AS PaisNombre " +
                        "FROM Ciudad c JOIN Pais p ON c.Pais_ID = p.ID WHERE ROWNUM = 1",
                rs -> new Object[]{
                        rs.getInt("ID"),
                        rs.getString("Nombre"),
                        rs.getString("PaisNombre")
                }
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay ciudades con pais en Oracle, se omite la prueba");

        ciudadIdReal    = (int)    ciudades.get(0)[0];
        ciudadNombreReal = (String) ciudades.get(0)[1];
        paisNombreReal   = (String) ciudades.get(0)[2];

        // Fechas futuras para no interferir con reservaciones activas
        fechaCheckIn  = Date.valueOf(LocalDate.now().plusDays(10));
        fechaCheckOut = Date.valueOf(LocalDate.now().plusDays(13));
    }

    /**
     * Elimina las busquedas anonimas insertadas durante la ejecucion de los tests,
     * filtrando por la ciudad real usada y la fecha actual para no afectar datos
     * preexistentes en Oracle.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Busqueda " +
                        "WHERE CiudadID = ? AND TRUNC(Fecha) = TRUNC(SYSDATE) AND UsuarioID IS NULL",
                ciudadIdReal
        );
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code buscarCiudadId} retorne un ID no nulo y positivo cuando
     * se busca con el nombre de ciudad y pais reales obtenidos de Oracle.
     * <p>
     * Confirma que la comparacion case-insensitive del repositorio funciona
     * correctamente con datos que ya existen en la base de datos.
     * </p>
     */
    @Test
    @Order(1)
    @DisplayName("1. buscarCiudadId con ciudad y pais existente retorna ID no nulo")
    void buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo() {
        Integer id = busquedaRepository.buscarCiudadId(ciudadNombreReal, paisNombreReal);

        assertNotNull(id,
                "buscarCiudadId debe retornar un ID no nulo para ciudad y pais existentes en Oracle");
        assertTrue(id > 0,
                "El ID retornado por buscarCiudadId debe ser positivo");
        assertEquals(ciudadIdReal, id,
                "El ID retornado debe coincidir con el ID de la ciudad obtenida en el setup");
    }

    /**
     * Verifica que {@code buscarCiudadId} retorne {@code null} cuando se proporciona
     * un nombre de ciudad y pais que no existen en Oracle.
     * <p>
     * Usa nombres con sufijos improbables para garantizar que no coincidan con
     * ningun registro real en la base de datos.
     * </p>
     */
    @Test
    @Order(2)
    @DisplayName("2. buscarCiudadId con ciudad inexistente retorna null")
    void buscarCiudadId_ciudadInexistente_retornaNull() {
        Integer id = busquedaRepository.buscarCiudadId(
                "Ciudad_Inexistente_XYZ", "Pais_Inexistente_XYZ");

        assertNull(id,
                "buscarCiudadId debe retornar null cuando la ciudad y pais no existen en Oracle");
    }

    /**
     * Verifica que {@code guardarBusqueda} con {@code usuarioId = null} (busqueda anonima)
     * se ejecute sin lanzar ninguna excepcion y que el registro quede almacenado en Oracle.
     * <p>
     * La busqueda registrada es eliminada en {@code @AfterEach} para no dejar residuos.
     * </p>
     */
    @Test
    @Order(3)
    @DisplayName("3. guardarBusqueda sin usuario no lanza excepcion y registra en Oracle")
    void guardarBusqueda_sinUsuario_noLanzaExcepcion() {
        assertDoesNotThrow(
                () -> busquedaRepository.guardarBusqueda(
                        ciudadIdReal, fechaCheckIn, fechaCheckOut, 2, null),
                "guardarBusqueda con usuarioId null no debe lanzar ninguna excepcion"
        );

        // Verifica que el registro se inserto efectivamente en Oracle
        List<Integer> busquedas = DatabaseManager.executeQuery(
                "SELECT ID FROM Busqueda " +
                        "WHERE CiudadID = ? AND TRUNC(Fecha) = TRUNC(SYSDATE) AND UsuarioID IS NULL",
                rs -> rs.getInt("ID"),
                ciudadIdReal
        );
        assertFalse(busquedas.isEmpty(),
                "Debe existir al menos una busqueda anonima en Oracle tras llamar a guardarBusqueda");
    }

    /**
     * Verifica que {@code buscarHotelesPorCiudad} retorne una lista no nula para
     * la ciudad real obtenida de Oracle.
     * <p>
     * La lista puede estar vacia si no hay hoteles activos en esa ciudad; lo
     * importante es que la consulta no lance excepcion y retorne una coleccion
     * correctamente inicializada.
     * </p>
     */
    @Test
    @Order(4)
    @DisplayName("4. buscarHotelesPorCiudad retorna lista no nula para ciudad existente")
    void buscarHotelesPorCiudad_retornaListaNoNula() {
        List<HotelResultadoDTO> hoteles = busquedaRepository.buscarHotelesPorCiudad(ciudadIdReal);

        assertNotNull(hoteles,
                "buscarHotelesPorCiudad no debe retornar null para una ciudad existente en Oracle");
    }

    /**
     * Verifica que {@code buscarImagenesHotel} retorne una lista vacia cuando se
     * proporciona un ID de hotel que no existe en Oracle ({@code -1}).
     * <p>
     * Confirma que el repositorio maneja correctamente la ausencia de registros
     * sin lanzar excepcion y retornando una coleccion vacia en lugar de null.
     * </p>
     */
    @Test
    @Order(5)
    @DisplayName("5. buscarImagenesHotel con hotel inexistente retorna lista vacia")
    void buscarImagenesHotel_hotelInexistente_retornaListaVacia() {
        List<Integer> imagenes = busquedaRepository.buscarImagenesHotel(-1);

        assertNotNull(imagenes,
                "buscarImagenesHotel no debe retornar null aunque el hotel no exista");
        assertTrue(imagenes.isEmpty(),
                "buscarImagenesHotel debe retornar una lista vacia para un hotel inexistente");
    }

    /**
     * Verifica que {@code buscarAmenidadesHotel} retorne una lista vacia cuando se
     * proporciona un ID de hotel que no existe en Oracle ({@code -1}).
     * <p>
     * Confirma que el repositorio no lanza excepcion y retorna una coleccion vacia
     * inicializada correctamente cuando no hay amenidades asociadas.
     * </p>
     */
    @Test
    @Order(6)
    @DisplayName("6. buscarAmenidadesHotel con hotel inexistente retorna lista vacia")
    void buscarAmenidadesHotel_hotelInexistente_retornaListaVacia() {
        List<AmenidadHotelDTO> amenidades = busquedaRepository.buscarAmenidadesHotel(-1);

        assertNotNull(amenidades,
                "buscarAmenidadesHotel no debe retornar null aunque el hotel no exista");
        assertTrue(amenidades.isEmpty(),
                "buscarAmenidadesHotel debe retornar una lista vacia para un hotel inexistente");
    }

    /**
     * Verifica que {@code buscarImagenesHabitacion} retorne una lista vacia cuando se
     * proporciona un ID de habitacion que no existe en Oracle ({@code -1}).
     * <p>
     * Confirma que el repositorio maneja correctamente la ausencia de imagenes sin
     * lanzar excepcion y retornando una coleccion vacia en lugar de null.
     * </p>
     */
    @Test
    @Order(7)
    @DisplayName("7. buscarImagenesHabitacion con habitacion inexistente retorna lista vacia")
    void buscarImagenesHabitacion_habitacionInexistente_retornaListaVacia() {
        List<Integer> imagenes = busquedaRepository.buscarImagenesHabitacion(-1);

        assertNotNull(imagenes,
                "buscarImagenesHabitacion no debe retornar null aunque la habitacion no exista");
        assertTrue(imagenes.isEmpty(),
                "buscarImagenesHabitacion debe retornar una lista vacia para una habitacion inexistente");
    }
}
