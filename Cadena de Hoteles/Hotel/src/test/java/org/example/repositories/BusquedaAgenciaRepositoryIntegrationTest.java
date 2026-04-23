package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AmenidadHotelDTO;
import org.example.dtos.HotelResultadoDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link BusquedaAgenciaRepository}.
 * <p>
 * Conecta a Oracle real para verificar las operaciones de busqueda del canal
 * de agencias: obtencion de descuentos por usuario, busqueda de ciudad por
 * nombre y pais, registro de busquedas, consulta de hoteles por ciudad e
 * imagenes de hoteles y habitaciones.
 * </p>
 * <p>
 * El {@code @BeforeEach} obtiene una ciudad y un pais reales de Oracle para
 * tener datos de referencia validos, luego inserta un usuario webservice y
 * una agencia vinculada con un porcentaje de descuento del 15%. El
 * {@code @AfterEach} elimina en orden FK-inverso: busquedas de agencia,
 * agencia y usuario.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Ciudad}, {@code Pais}, {@code EstadoAgencia}, {@code Usuario},
 * {@code Agencia} y {@code Busqueda} accesibles.
 * </p>
 */
@DisplayName("Integracion: BusquedaAgenciaRepository - Busqueda de agencia contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusquedaAgenciaRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private BusquedaAgenciaRepository busquedaAgenciaRepository;

    /** ID del usuario webservice de prueba insertado en {@code @BeforeEach}. */
    private int usuarioId;

    /** ID de la agencia de prueba insertada en {@code @BeforeEach}. */
    private int agenciaId;

    /** ID de una ciudad existente en Oracle obtenida en {@code @BeforeEach}. */
    private int ciudadId;

    /** Nombre de la ciudad real obtenida en {@code @BeforeEach}. */
    private String ciudadNombre;

    /** Nombre del pais de la ciudad real obtenida en {@code @BeforeEach}. */
    private String paisNombre;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME  = "test_bus_agc_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO    = "test_bus_agc_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE = "IT-BAGC-001";

    /**
     * Inicializa el repositorio y prepara los datos de prueba en Oracle. Obtiene una
     * ciudad y su pais reales mediante {@link Assumptions#assumeTrue} para garantizar
     * que los filtros de busqueda operen sobre datos validos. Inserta el usuario
     * webservice y la agencia con 15% de descuento.
     */
    @BeforeEach
    void setUp() {
        busquedaAgenciaRepository = new BusquedaAgenciaRepository();

        // 1. Obtiene una ciudad y su pais reales de Oracle
        List<Object[]> ciudades = DatabaseManager.executeQuery(
                "SELECT c.ID, c.Nombre, p.Nombre AS PaisNombre " +
                        "FROM Ciudad c JOIN Pais p ON c.Pais_ID = p.ID " +
                        "WHERE ROWNUM = 1",
                rs -> new Object[]{
                        rs.getInt("ID"),
                        rs.getString("Nombre"),
                        rs.getString("PaisNombre")
                }
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay ciudades con pais en Oracle — se omite la prueba");

        ciudadId    = (int)    ciudades.get(0)[0];
        ciudadNombre = (String) ciudades.get(0)[1];
        paisNombre   = (String) ciudades.get(0)[2];

        // 2. Inserta el usuario webservice de prueba
        usuarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, ?)",
                "ID",
                USERNAME,
                CORREO,
                PasswordHelper.hashear("TestPass123"),
                PASAPORTE
        );
        Assumptions.assumeTrue(usuarioId > 0,
                "No se pudo insertar el usuario de prueba — se omite la prueba");

        // 3. Inserta la agencia de prueba con 15% de descuento
        agenciaId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Agencia (Nombre, Correo, UsuarioWebis_ID, PorcentajeDescuento, EstadoID, URL_Agencia) " +
                        "VALUES (?, ?, ?, 15, 1, ?)",
                "ID",
                "Agencia IT Busqueda",
                "agencia_it_bus@test.com",
                usuarioId,
                "http://it-bus-agencia.com"
        );
        Assumptions.assumeTrue(agenciaId > 0,
                "No se pudo insertar la agencia de prueba — se omite la prueba");
    }

    /**
     * Elimina en orden FK-inverso las busquedas de agencia creadas por los tests,
     * la agencia de prueba y el usuario de prueba. Garantiza que Oracle no quede
     * con residuos incluso si algun caso de prueba falla.
     */
    @AfterEach
    void tearDown() {
        // 1. Elimina busquedas de agencia del usuario de prueba (TipoBusquedaID = 2)
        DatabaseManager.executeUpdate(
                "DELETE FROM Busqueda WHERE UsuarioID = ? AND TipoBusquedaID = 2",
                usuarioId
        );
        // 2. Elimina la agencia de prueba
        DatabaseManager.executeUpdate(
                "DELETE FROM Agencia WHERE ID = ?",
                agenciaId
        );
        // 3. Elimina el usuario de prueba
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?",
                usuarioId
        );
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link BusquedaAgenciaRepository#buscarCiudadId} retorne un ID
     * no nulo cuando se busca con el nombre y pais de una ciudad existente en Oracle.
     * Confirma ademas que el ID retornado coincida con el ID obtenido en el setup.
     */
    @Test
    @Order(1)
    @DisplayName("1. buscarCiudadId con ciudad y pais existente retorna ID no nulo")
    void buscarCiudadId_ciudadYPaisExistente_retornaIdNoNulo() {
        Integer resultado = busquedaAgenciaRepository.buscarCiudadId(ciudadNombre, paisNombre);

        assertNotNull(resultado,
                "buscarCiudadId no debe retornar null para una ciudad y pais existentes en Oracle");
        assertEquals(ciudadId, resultado,
                "El ID retornado debe coincidir con el ID de la ciudad obtenida en el setup");
    }

    /**
     * Verifica que {@link BusquedaAgenciaRepository#buscarCiudadId} retorne
     * {@code null} cuando se busca con un nombre de ciudad que no existe en Oracle.
     */
    @Test
    @Order(2)
    @DisplayName("2. buscarCiudadId con ciudad inexistente retorna null")
    void buscarCiudadId_ciudadInexistente_retornaNull() {
        Integer resultado = busquedaAgenciaRepository.buscarCiudadId(
                "Ciudad_Inexistente_XYZ", "Pais_Inexistente_XYZ");

        assertNull(resultado,
                "buscarCiudadId debe retornar null cuando la ciudad y pais no existen en Oracle");
    }

    /**
     * Verifica que {@link BusquedaAgenciaRepository#obtenerDescuentoAgencia} retorne
     * un valor no nulo y positivo para el usuario de prueba, cuya agencia fue
     * insertada con un porcentaje de descuento del 15%.
     */
    @Test
    @Order(3)
    @DisplayName("3. obtenerDescuentoAgencia con usuario con agencia retorna descuento positivo")
    void obtenerDescuentoAgencia_usuarioConAgencia_retornaDescuento() {
        Double descuento = busquedaAgenciaRepository.obtenerDescuentoAgencia(usuarioId);

        assertNotNull(descuento,
                "obtenerDescuentoAgencia no debe retornar null para un usuario con agencia activa");
        assertTrue(descuento > 0,
                "El descuento debe ser mayor a cero (la agencia fue insertada con 15%)");
    }

    /**
     * Verifica que {@link BusquedaAgenciaRepository#guardarBusqueda} no lance
     * ninguna excepcion al registrar una busqueda valida, y que el registro quede
     * persistido en Oracle con {@code TipoBusquedaID = 2} para el usuario de prueba.
     */
    @Test
    @Order(4)
    @DisplayName("4. guardarBusqueda con datos validos no lanza excepcion y persiste en Oracle")
    void guardarBusqueda_datosValidos_noLanzaExcepcion() {
        Date checkIn  = Date.valueOf("2031-03-01");
        Date checkOut = Date.valueOf("2031-03-05");

        assertDoesNotThrow(
                () -> busquedaAgenciaRepository.guardarBusqueda(
                        ciudadId, checkIn, checkOut, 2, usuarioId),
                "guardarBusqueda no debe lanzar ninguna excepcion con datos validos"
        );

        // Verifica que el registro quedo en Oracle con TipoBusquedaID = 2
        List<Integer> conteo = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS C FROM Busqueda " +
                        "WHERE UsuarioID = ? AND TipoBusquedaID = 2",
                rs -> rs.getInt("C"),
                usuarioId
        );
        assertNotNull(conteo,
                "La lista de conteo no debe ser null");
        assertFalse(conteo.isEmpty(),
                "La consulta de conteo debe retornar al menos una fila");
        assertTrue(conteo.get(0) >= 1,
                "Debe existir al menos una busqueda con TipoBusquedaID=2 para el usuario de prueba");
    }

    /**
     * Verifica que {@link BusquedaAgenciaRepository#buscarHotelesPorCiudad} retorne
     * una lista no nula para la ciudad obtenida en el setup. La lista puede estar
     * vacia si no hay hoteles activos en esa ciudad, pero nunca debe ser {@code null}.
     */
    @Test
    @Order(5)
    @DisplayName("5. buscarHotelesPorCiudad retorna lista no nula para ciudad existente")
    void buscarHotelesPorCiudad_retornaListaNoNula() {
        List<HotelResultadoDTO> lista =
                busquedaAgenciaRepository.buscarHotelesPorCiudad(ciudadId);

        assertNotNull(lista,
                "buscarHotelesPorCiudad no debe retornar null aunque no haya hoteles activos en la ciudad");
    }
}
