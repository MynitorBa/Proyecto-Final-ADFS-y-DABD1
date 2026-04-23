package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link AerolineaAliadaRepository}.
 * <p>
 * Conecta a Oracle real para verificar autenticacion por token, busqueda de
 * ciudades, registro de busquedas (TipoBusquedaID = 3), busqueda de aerolinea
 * por URL y actualizacion del token de sesion.
 * </p>
 * <p>
 * El {@code @BeforeEach} obtiene un EstadoAliado existente de Oracle, inserta
 * un Usuario webservice y una AerolineaAliado con token y descuento del 12%.
 * El {@code @AfterEach} elimina en orden FK-inverso: Busqueda, AerolineaAliado, Usuario.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code EstadoAliado}, {@code AerolineaAliado}, {@code Busqueda},
 * {@code Usuario}, {@code Ciudad} y {@code Pais} accesibles.
 * </p>
 */
@DisplayName("Integracion: AerolineaAliadaRepository - Aerolinea aliada contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AerolineaAliadaRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private AerolineaAliadaRepository aerolineaAliadaRepository;

    /** ID del usuario webservice de prueba. */
    private int usuarioId;

    /** ID de la aerolinea aliada de prueba insertada en {@code @BeforeEach}. */
    private int aerolineaId;

    /** ID de una ciudad existente en Oracle obtenida en {@code @BeforeEach}. */
    private int ciudadId;

    /** Nombre de la ciudad real obtenida en {@code @BeforeEach}. */
    private String ciudadNombre;

    /** Nombre del pais de la ciudad real. */
    private String paisNombre;

    /** Username fijo del usuario webservice de prueba. */
    private static final String USERNAME      = "test_aliada_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO        = "test_aliada_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE     = "IT-ALIADA-001";

    /** Token fijo usado para la aerolinea de prueba. */
    private static final String TOKEN         = "IT-ALIADA-TOKEN-HASH-001";

    /** URL fija de handshake de la aerolinea de prueba. */
    private static final String URL_AEROLINEA = "http://it-aliada-test.com";

    /**
     * Inicializa el repositorio y prepara los datos de prueba en Oracle.
     * Realiza pre-limpieza defensiva, obtiene EstadoAliado y ciudad reales,
     * e inserta el usuario webservice y la AerolineaAliado con 12% de descuento.
     */
    @BeforeEach
    void setUp() {
        aerolineaAliadaRepository = new AerolineaAliadaRepository();

        // 0. Limpieza defensiva por identificadores fijos
        DatabaseManager.executeUpdate(
                "DELETE FROM Busqueda WHERE TipoBusquedaID = 3 AND UsuarioID IS NULL AND AgenciaID IS NULL");
        DatabaseManager.executeUpdate(
                "DELETE FROM AerolineaAliado WHERE URL = ?", URL_AEROLINEA);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE Username = ?", USERNAME);

        // 1. Obtiene un EstadoAliado existente en Oracle
        List<Integer> estados = DatabaseManager.executeQuery(
                "SELECT ID FROM EstadoAliado WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estados.isEmpty(),
                "No hay EstadoAliado en Oracle — se omite la prueba");
        int estadoAliadoId = estados.get(0);

        // 2. Obtiene una ciudad con su pais real
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
                "No hay ciudades con pais en Oracle — se omite la prueba");
        ciudadId    = (int)    ciudades.get(0)[0];
        ciudadNombre = (String) ciudades.get(0)[1];
        paisNombre   = (String) ciudades.get(0)[2];

        // 3. Inserta el usuario webservice de prueba
        usuarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, ?)",
                "ID", USERNAME, CORREO,
                PasswordHelper.hashear("TestPass123"), PASAPORTE
        );
        Assumptions.assumeTrue(usuarioId > 0,
                "No se pudo insertar el usuario de prueba — se omite la prueba");

        // 4. Inserta la aerolinea aliada de prueba con 12% descuento y token conocido
        aerolineaId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO AerolineaAliado " +
                        "(Nombre, UsuarioWebis, PorcentajeDescuento, EstadoID, URL, URLParaUsuario, TokenHASH) " +
                        "VALUES (?, ?, 12, ?, ?, ?, ?)",
                "ID",
                "Aerolinea IT Aliada Test",
                usuarioId,
                estadoAliadoId,
                URL_AEROLINEA,
                "http://it-aliada-user.com",
                TOKEN
        );
        Assumptions.assumeTrue(aerolineaId > 0,
                "No se pudo insertar la aerolinea aliada de prueba — se omite la prueba");
    }

    /**
     * Elimina en orden FK-inverso: Busqueda de tipo 3 para ciudad de prueba,
     * AerolineaAliado y Usuario.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Busqueda WHERE TipoBusquedaID = 3 AND CiudadID = ?",
                ciudadId
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM AerolineaAliado WHERE ID = ?", aerolineaId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioId);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link AerolineaAliadaRepository#obtenerAerolineaPorToken}
     * retorne un DTO no nulo con el ID correcto cuando el token pertenece
     * a la aerolinea activa de prueba.
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerAerolineaPorToken con token activo retorna DTO no nulo")
    void obtenerAerolineaPorToken_tokenActivo_retornaDto() {
        var resultado = aerolineaAliadaRepository.obtenerAerolineaPorToken(TOKEN);

        assertNotNull(resultado,
                "obtenerAerolineaPorToken no debe retornar null para un token activo");
        assertEquals(aerolineaId, resultado.getId(),
                "El ID del DTO debe coincidir con el ID de la aerolinea insertada");
    }

    /**
     * Verifica que {@link AerolineaAliadaRepository#obtenerAerolineaPorToken}
     * retorne {@code null} cuando el token no existe en la base de datos.
     */
    @Test
    @Order(2)
    @DisplayName("2. obtenerAerolineaPorToken con token inexistente retorna null")
    void obtenerAerolineaPorToken_tokenInexistente_retornaNull() {
        var resultado = aerolineaAliadaRepository.obtenerAerolineaPorToken(
                "TOKEN-INEXISTENTE-XYZ-9999");

        assertNull(resultado,
                "obtenerAerolineaPorToken debe retornar null para un token que no existe");
    }

    /**
     * Verifica que {@link AerolineaAliadaRepository#obtenerDescuentoAerolinea}
     * retorne un valor positivo para la aerolinea de prueba, la cual fue
     * insertada con 12% de descuento.
     */
    @Test
    @Order(3)
    @DisplayName("3. obtenerDescuentoAerolinea con token activo retorna descuento positivo")
    void obtenerDescuentoAerolinea_tokenActivo_retornaDescuentoPositivo() {
        Double descuento = aerolineaAliadaRepository.obtenerDescuentoAerolinea(TOKEN);

        assertNotNull(descuento,
                "obtenerDescuentoAerolinea no debe retornar null para un token activo");
        assertTrue(descuento > 0,
                "El descuento debe ser mayor a cero (aerolinea insertada con 12%)");
    }

    /**
     * Verifica que {@link AerolineaAliadaRepository#buscarCiudadId} retorne el
     * ID correcto para la ciudad real obtenida en el setup.
     */
    @Test
    @Order(4)
    @DisplayName("4. buscarCiudadId con ciudad existente retorna ID correcto")
    void buscarCiudadId_ciudadExistente_retornaId() {
        Integer resultado = aerolineaAliadaRepository.buscarCiudadId(ciudadNombre, paisNombre);

        assertNotNull(resultado,
                "buscarCiudadId no debe retornar null para una ciudad y pais existentes");
        assertEquals(ciudadId, resultado,
                "El ID debe coincidir con el de la ciudad obtenida en el setup");
    }

    /**
     * Verifica que {@link AerolineaAliadaRepository#guardarBusqueda} registre
     * la busqueda con TipoBusquedaID = 3 en Oracle sin lanzar excepcion.
     */
    @Test
    @Order(5)
    @DisplayName("5. guardarBusqueda registra busqueda de aerolinea en Oracle")
    void guardarBusqueda_datosValidos_persisteEnOracle() {
        Date checkIn  = Date.valueOf("2032-06-01");
        Date checkOut = Date.valueOf("2032-06-05");

        assertDoesNotThrow(
                () -> aerolineaAliadaRepository.guardarBusqueda(ciudadId, checkIn, checkOut, 2),
                "guardarBusqueda no debe lanzar excepcion con datos validos"
        );

        List<Integer> conteo = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS C FROM Busqueda WHERE CiudadID = ? AND TipoBusquedaID = 3",
                rs -> rs.getInt("C"),
                ciudadId
        );
        assertTrue(conteo.get(0) >= 1,
                "Debe haber al menos una busqueda con TipoBusquedaID=3 para la ciudad de prueba");
    }

    /**
     * Verifica que {@link AerolineaAliadaRepository#obtenerAerolineaIdPorURL}
     * retorne el ID correcto para la URL registrada de la aerolinea de prueba.
     */
    @Test
    @Order(6)
    @DisplayName("6. obtenerAerolineaIdPorURL con URL existente retorna ID correcto")
    void obtenerAerolineaIdPorURL_urlExistente_retornaId() {
        Integer resultado = aerolineaAliadaRepository.obtenerAerolineaIdPorURL(URL_AEROLINEA);

        assertNotNull(resultado,
                "obtenerAerolineaIdPorURL no debe retornar null para una URL existente");
        assertEquals(aerolineaId, resultado,
                "El ID retornado debe coincidir con el ID de la aerolinea insertada");
    }

    /**
     * Verifica que {@link AerolineaAliadaRepository#guardarTokensAerolinea}
     * actualice el TokenHASH de la aerolinea y retorne {@code true}.
     * Consulta Oracle directamente para confirmar el nuevo token.
     */
    @Test
    @Order(7)
    @DisplayName("7. guardarTokensAerolinea actualiza TokenHASH en Oracle y retorna true")
    void guardarTokensAerolinea_datosValidos_actualizaToken() {
        String tokenNuevo = "NUEVO-TOKEN-IT-ALIADA-002";

        boolean resultado = aerolineaAliadaRepository.guardarTokensAerolinea(
                aerolineaId, TOKEN, tokenNuevo);

        assertTrue(resultado,
                "guardarTokensAerolinea debe retornar true cuando actualiza el token");

        List<String> tokens = DatabaseManager.executeQuery(
                "SELECT TokenHASH FROM AerolineaAliado WHERE ID = ?",
                rs -> rs.getString("TokenHASH"),
                aerolineaId
        );
        assertFalse(tokens.isEmpty(),
                "Debe existir la aerolinea aliada en Oracle tras actualizar el token");
        assertEquals(tokenNuevo, tokens.get(0),
                "El TokenHASH en Oracle debe ser el token de salida recien guardado");
    }
}
