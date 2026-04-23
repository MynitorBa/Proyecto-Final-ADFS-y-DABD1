package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AerolineaWebserviceDTO;
import org.example.dtos.CrearAerolineaRequestDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link AerolineaWebserviceRepository}.
 * <p>
 * Conecta a Oracle real para verificar el ciclo completo de gestion de aerolineas
 * aliadas desde el portal webservice: listado por usuario, creacion y cambio de
 * estado. Cada caso opera sobre datos propios insertados en {@code @BeforeEach}
 * y eliminados en {@code @AfterEach}.
 * </p>
 * <p>
 * El {@code @BeforeEach} inserta en orden FK un usuario webservice de prueba
 * (con Rol_ID = 3) y a continuacion crea una aerolinea vinculada a ese usuario
 * mediante el propio repositorio bajo prueba. El {@code @AfterEach} elimina
 * primero la aerolinea y luego el usuario, respetando el orden FK-inverso para
 * no dejar residuos independientemente del resultado de cada caso.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Usuario}, {@code AerolineaAliado} y {@code EstadoAliado} accesibles
 * y con al menos un registro en {@code EstadoAliado} cuyo valor sea {@code 'activo'}.
 * </p>
 */
@DisplayName("Integracion: AerolineaWebserviceRepository - Gestion de aerolineas desde el portal webservice")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AerolineaWebserviceRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private AerolineaWebserviceRepository aerolineaWebserviceRepository;

    /** ID del usuario webservice de prueba insertado en {@code @BeforeEach}. */
    private int usuarioId;

    /** ID de la aerolinea de prueba creada en {@code @BeforeEach}. */
    private int aerolineaId;

    /** Username fijo del usuario webservice de prueba. */
    private static final String USERNAME  = "test_aero_ws_repo";

    /** Correo fijo del usuario webservice de prueba. */
    private static final String CORREO    = "test_aero_ws_repo@hotel.com";

    /** Pasaporte fijo del usuario webservice de prueba. */
    private static final String PASAPORTE = "IT-AERO-WS-001";

    /**
     * Inicializa el repositorio, inserta el usuario webservice de prueba en Oracle
     * con Rol_ID = 3 (Webservice) y luego crea la aerolinea de prueba vinculada a ese
     * usuario mediante {@link AerolineaWebserviceRepository#crear}. Si cualquier
     * insercion falla, la prueba se omite via {@link Assumptions#assumeTrue} para
     * evitar falsos negativos por problemas del entorno de prueba.
     */
    @BeforeEach
    void setUp() {
        aerolineaWebserviceRepository = new AerolineaWebserviceRepository();

        // 1. Inserta el usuario webservice de prueba con Rol_ID = 3
        usuarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 3, ?)",
                "ID",
                USERNAME,
                CORREO,
                PasswordHelper.hashear("TestPass123"),
                PASAPORTE
        );
        Assumptions.assumeTrue(usuarioId > 0,
                "No se pudo insertar el usuario webservice de prueba — se omite la prueba");

        // 2. Crea la aerolinea de prueba mediante el repositorio
        CrearAerolineaRequestDTO req = new CrearAerolineaRequestDTO();
        req.setNombre("Aerolinea WS Test IT");
        req.setUrl("http://api-ws-test.aerolinea.com");
        req.setUrlParaUsuario("http://ws-test.aerolinea.com");

        AerolineaWebserviceDTO creada = aerolineaWebserviceRepository.crear(usuarioId, req);
        aerolineaId = creada.getId();
        Assumptions.assumeTrue(aerolineaId > 0,
                "No se pudo crear la aerolinea de prueba — se omite la prueba");
    }

    /**
     * Elimina en orden FK-inverso la aerolinea (si fue creada) y luego el usuario
     * webservice de prueba, garantizando que Oracle no quede con residuos incluso
     * cuando un caso de prueba cambia el estado de la aerolinea internamente.
     */
    @AfterEach
    void tearDown() {
        // 1. Elimina la aerolinea de prueba si fue creada exitosamente
        if (aerolineaId > 0) {
            DatabaseManager.executeUpdate(
                    "DELETE FROM AerolineaAliado WHERE ID = ?", aerolineaId);
        }
        // 2. Elimina el usuario webservice de prueba
        if (usuarioId > 0) {
            DatabaseManager.executeUpdate(
                    "DELETE FROM Usuario WHERE ID = ?", usuarioId);
        }
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link AerolineaWebserviceRepository#listarPorUsuario} retorne una
     * lista no nula con al menos una aerolinea cuando el usuario tiene una aerolinea
     * vinculada. Comprueba ademas que el ID y el nombre del primer elemento sean
     * correctos y coherentes con los datos insertados en el setup.
     */
    @Test
    @Order(1)
    @DisplayName("1. listarPorUsuario con usuario con aerolinea retorna lista con al menos una")
    void listarPorUsuario_usuarioConAerolinea_retornaListaConAlMenosUna() {
        List<AerolineaWebserviceDTO> lista =
                aerolineaWebserviceRepository.listarPorUsuario(usuarioId);

        assertNotNull(lista,
                "listarPorUsuario no debe retornar null para un usuario con aerolinea");
        assertFalse(lista.isEmpty(),
                "La lista debe tener al menos una aerolinea para el usuario de prueba");

        AerolineaWebserviceDTO primera = lista.get(0);
        assertNotNull(primera,
                "El primer elemento de la lista no debe ser null");
        assertTrue(primera.getId() > 0,
                "El ID de la aerolinea retornada debe ser positivo");
        assertNotNull(primera.getNombre(),
                "El nombre de la aerolinea no debe ser null");
        assertEquals("Aerolinea WS Test IT", primera.getNombre(),
                "El nombre de la aerolinea debe coincidir con el insertado en el setup");
        assertEquals(usuarioId, primera.getUsuarioWebis(),
                "El usuarioWebis de la aerolinea debe coincidir con el usuario de prueba");
    }

    /**
     * Verifica que {@link AerolineaWebserviceRepository#crear} retorne un
     * {@link AerolineaWebserviceDTO} con un ID positivo y con los campos nombre, URL,
     * estado y usuarioWebis tal como fueron configurados en el request. Usa un segundo
     * usuario auxiliar para no colisionar con la restriccion de una entidad por usuario.
     * El usuario auxiliar y la segunda aerolinea se limpian en un bloque {@code finally}.
     */
    @Test
    @Order(2)
    @DisplayName("2. crear con datos validos retorna aerolinea con ID positivo")
    void crear_datosValidos_retornaAerolineaConId() {
        int usuarioAuxId   = -1;
        int aerolineaAuxId = -1;
        try {
            // Segundo usuario auxiliar con Rol_ID = 3
            usuarioAuxId = DatabaseManager.executeInsertReturnId(
                    "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                            "VALUES (?, ?, ?, 3, ?)",
                    "ID",
                    "test_aero_ws_crear",
                    "test_aero_ws_crear@hotel.com",
                    PasswordHelper.hashear("TestPass123"),
                    "IT-AERO-WS-002"
            );
            Assumptions.assumeTrue(usuarioAuxId > 0,
                    "No se pudo insertar el usuario auxiliar — se omite la verificacion");

            CrearAerolineaRequestDTO req = new CrearAerolineaRequestDTO();
            req.setNombre("Aerolinea WS Crear IT");
            req.setUrl("http://api-crear-ws-it.aerolinea.com");
            req.setUrlParaUsuario("http://crear-ws-it.aerolinea.com");

            AerolineaWebserviceDTO resultado =
                    aerolineaWebserviceRepository.crear(usuarioAuxId, req);

            assertNotNull(resultado,
                    "crear no debe retornar null cuando los datos son validos");
            aerolineaAuxId = resultado.getId();
            assertTrue(aerolineaAuxId > 0,
                    "El ID de la aerolinea creada debe ser positivo");
            assertNotNull(resultado.getNombre(),
                    "El nombre de la aerolinea creada no debe ser null");
            assertEquals("Aerolinea WS Crear IT", resultado.getNombre(),
                    "El nombre debe coincidir con el valor enviado en el request");
            assertNotNull(resultado.getUrl(),
                    "La URL de la aerolinea creada no debe ser null");
            assertEquals("http://api-crear-ws-it.aerolinea.com", resultado.getUrl(),
                    "La URL debe coincidir con el valor enviado en el request");
            assertEquals(1, resultado.getEstadoId(),
                    "El EstadoID de una nueva aerolinea debe ser 1 (Activo)");
            assertNotNull(resultado.getEstado(),
                    "El estado en texto de la aerolinea creada no debe ser null");
            assertEquals("Activo", resultado.getEstado(),
                    "El estado en texto debe ser 'Activo' para una aerolinea recien creada");
            assertEquals(usuarioAuxId, resultado.getUsuarioWebis(),
                    "El usuarioWebis del DTO debe coincidir con el ID del usuario auxiliar");
        } finally {
            if (aerolineaAuxId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM AerolineaAliado WHERE ID = ?", aerolineaAuxId);
            }
            if (usuarioAuxId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Usuario WHERE ID = ?", usuarioAuxId);
            }
        }
    }

    /**
     * Verifica que {@link AerolineaWebserviceRepository#cambiarEstado} actualice el
     * {@code EstadoID} de la aerolinea de prueba al valor indicado. Invoca el metodo
     * para cambiar el estado a 2 (Cerrado) y luego consulta directamente Oracle para
     * confirmar que la columna {@code EstadoID} fue modificada correctamente.
     */
    @Test
    @Order(3)
    @DisplayName("3. cambiarEstado con aerolinea activa actualiza el EstadoID en Oracle")
    void cambiarEstado_aerolineaActiva_actualizaEstado() {
        // Cambia el estado a 2 (Cerrado) verificando que pertenece al usuario de prueba
        aerolineaWebserviceRepository.cambiarEstado(aerolineaId, usuarioId, 2);

        // Verifica el cambio directamente en Oracle
        List<Integer> estadoIds = DatabaseManager.executeQuery(
                "SELECT EstadoID FROM AerolineaAliado WHERE ID = ?",
                rs -> rs.getInt("EstadoID"),
                aerolineaId
        );

        assertNotNull(estadoIds,
                "La lista de EstadoID no debe ser null tras cambiarEstado");
        assertFalse(estadoIds.isEmpty(),
                "Debe existir la aerolinea en Oracle tras cambiar su estado");
        assertEquals(2, estadoIds.get(0).intValue(),
                "El EstadoID debe ser 2 (Cerrado) tras la llamada a cambiarEstado");
    }

    /**
     * Verifica que {@link AerolineaWebserviceRepository#listarPorUsuario} retorne una
     * lista vacia cuando el {@code usuarioId} proporcionado no tiene ninguna aerolinea
     * registrada. Se usa el ID ficticio {@code -999} que no puede coincidir con ningun
     * registro real en Oracle.
     */
    @Test
    @Order(4)
    @DisplayName("4. listarPorUsuario con usuario sin aerolinea retorna lista vacia")
    void listarPorUsuario_usuarioSinAerolinea_retornaListaVacia() {
        List<AerolineaWebserviceDTO> lista =
                aerolineaWebserviceRepository.listarPorUsuario(-999);

        assertNotNull(lista,
                "listarPorUsuario no debe retornar null incluso cuando el usuario no tiene aerolineas");
        assertTrue(lista.isEmpty(),
                "La lista debe estar vacia para un usuarioId que no existe en Oracle");
    }
}
