package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AerolineaAdminDTO;
import org.example.dtos.CrearAerolineaAdminRequestDTO;
import org.example.dtos.EditarAerolineaRequestDTO;
import org.example.dtos.UsuarioWebserviceLibreDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link AerolineaAdminRepository}.
 * <p>
 * Conecta a Oracle real para verificar el ciclo completo de gestion de aerolineas
 * aliadas desde el panel de administracion: listado de todas las aerolineas,
 * creacion, edicion y consulta de usuarios webservice libres.
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
@DisplayName("Integracion: AerolineaAdminRepository - Gestion de aerolineas desde el panel admin")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AerolineaAdminRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private AerolineaAdminRepository aerolineaAdminRepository;

    /** ID del usuario webservice de prueba insertado en {@code @BeforeEach}. */
    private int usuarioId;

    /** ID de la aerolinea de prueba creada en {@code @BeforeEach}. */
    private int aerolineaId;

    /** Username fijo del usuario webservice de prueba. */
    private static final String USERNAME  = "test_aero_admin_repo";

    /** Correo fijo del usuario webservice de prueba. */
    private static final String CORREO    = "test_aero_admin_repo@hotel.com";

    /** Pasaporte fijo del usuario webservice de prueba. */
    private static final String PASAPORTE = "IT-AERO-ADM-001";

    /**
     * Inicializa el repositorio, inserta el usuario webservice de prueba en Oracle
     * con Rol_ID = 3 (Webservice) y luego crea la aerolinea de prueba vinculada a ese
     * usuario mediante {@link AerolineaAdminRepository#crear}. Si cualquier insercion
     * falla, la prueba se omite via {@link Assumptions#assumeTrue} para evitar falsos
     * negativos por problemas del entorno de prueba.
     */
    @BeforeEach
    void setUp() {
        aerolineaAdminRepository = new AerolineaAdminRepository();

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
        CrearAerolineaAdminRequestDTO req = new CrearAerolineaAdminRequestDTO();
        req.setNombre("Aerolinea Admin Test IT");
        req.setUrl("http://api-admin-test.aerolinea.com");
        req.setUrlParaUsuario("http://admin-test.aerolinea.com");
        req.setUsuarioWebisId(usuarioId);

        AerolineaAdminDTO creada = aerolineaAdminRepository.crear(req);
        aerolineaId = creada.getId();
        Assumptions.assumeTrue(aerolineaId > 0,
                "No se pudo crear la aerolinea de prueba — se omite la prueba");
    }

    /**
     * Elimina en orden FK-inverso la aerolinea (si aun existe) y luego el usuario
     * webservice de prueba, garantizando que Oracle no quede con residuos incluso
     * cuando un caso de prueba modifica el estado de la aerolinea internamente.
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
     * Verifica que {@link AerolineaAdminRepository#listarTodas} retorne una lista no
     * nula. Como el setup garantiza que existe al menos una aerolinea, la lista no debe
     * estar vacia. Se comprueba ademas que el primer elemento tenga un nombre no nulo.
     */
    @Test
    @Order(1)
    @DisplayName("1. listarTodas retorna lista no nula con al menos una aerolinea")
    void listarTodas_retornaListaNoNula() {
        List<AerolineaAdminDTO> lista = aerolineaAdminRepository.listarTodas();

        assertNotNull(lista,
                "listarTodas no debe retornar null");
        assertFalse(lista.isEmpty(),
                "La lista debe tener al menos una aerolinea tras el setup");

        AerolineaAdminDTO primera = lista.get(0);
        assertNotNull(primera,
                "El primer elemento de la lista no debe ser null");
        assertNotNull(primera.getNombre(),
                "El nombre de la aerolinea no debe ser null");
    }

    /**
     * Verifica que {@link AerolineaAdminRepository#crear} retorne un
     * {@link AerolineaAdminDTO} con un ID positivo y con los campos nombre, URL y
     * estado tal como fueron configurados en el request. Usa un segundo usuario
     * auxiliar para no colisionar con la restriccion de una entidad por usuario.
     * El usuario auxiliar y la segunda aerolinea se limpian en un bloque
     * {@code finally} para no interferir con el teardown principal.
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
                    "test_aero_adm_crear",
                    "test_aero_adm_crear@hotel.com",
                    PasswordHelper.hashear("TestPass123"),
                    "IT-AERO-ADM-002"
            );
            Assumptions.assumeTrue(usuarioAuxId > 0,
                    "No se pudo insertar el usuario auxiliar — se omite la verificacion");

            CrearAerolineaAdminRequestDTO req = new CrearAerolineaAdminRequestDTO();
            req.setNombre("Aerolinea Admin Crear IT");
            req.setUrl("http://api-crear-it.aerolinea.com");
            req.setUrlParaUsuario("http://crear-it.aerolinea.com");
            req.setUsuarioWebisId(usuarioAuxId);

            AerolineaAdminDTO resultado = aerolineaAdminRepository.crear(req);

            assertNotNull(resultado,
                    "crear no debe retornar null cuando los datos son validos");
            aerolineaAuxId = resultado.getId();
            assertTrue(aerolineaAuxId > 0,
                    "El ID de la aerolinea creada debe ser positivo");
            assertNotNull(resultado.getNombre(),
                    "El nombre de la aerolinea creada no debe ser null");
            assertEquals("Aerolinea Admin Crear IT", resultado.getNombre(),
                    "El nombre debe coincidir con el valor enviado en el request");
            assertNotNull(resultado.getUrl(),
                    "La URL de la aerolinea creada no debe ser null");
            assertEquals("http://api-crear-it.aerolinea.com", resultado.getUrl(),
                    "La URL debe coincidir con el valor enviado en el request");
            assertEquals(1, resultado.getEstadoId(),
                    "El EstadoID de una nueva aerolinea debe ser 1 (Activo)");
            assertNotNull(resultado.getEstado(),
                    "El estado en texto de la aerolinea creada no debe ser null");
            assertEquals("Activo", resultado.getEstado(),
                    "El estado en texto debe ser 'Activo' para una aerolinea recien creada");
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
     * Verifica que {@link AerolineaAdminRepository#editar} actualice correctamente los
     * campos nombre, URL y porcentaje de descuento de la aerolinea de prueba creada en
     * el setup. Confirma el cambio consultando {@link AerolineaAdminRepository#listarTodas}
     * y filtrando por el ID de la aerolinea de prueba.
     */
    @Test
    @Order(3)
    @DisplayName("3. editar con aerolinea existente actualiza datos en Oracle")
    void editar_aerolineaExistente_actualizaDatos() {
        EditarAerolineaRequestDTO req = new EditarAerolineaRequestDTO();
        req.setNombre("Aerolinea Admin Editada IT");
        req.setUrl("http://api-editada-it.aerolinea.com");
        req.setUrlParaUsuario("http://editada-it.aerolinea.com");
        req.setPorcentajeDescuento(10.0);
        req.setEstadoId(1);

        // Invoca el metodo bajo prueba
        aerolineaAdminRepository.editar(aerolineaId, req);

        // Verifica el cambio consultando directamente Oracle
        List<Object[]> filas = DatabaseManager.executeQuery(
                "SELECT Nombre, URL, PorcentajeDescuento FROM AerolineaAliado WHERE ID = ?",
                rs -> new Object[]{
                        rs.getString("Nombre"),
                        rs.getString("URL"),
                        rs.getDouble("PorcentajeDescuento")
                },
                aerolineaId
        );

        assertNotNull(filas,
                "La consulta de verificacion no debe retornar null");
        assertFalse(filas.isEmpty(),
                "Debe existir la aerolinea en Oracle tras la edicion");

        Object[] fila = filas.get(0);
        assertNotNull(fila[0],
                "El nombre de la aerolinea no debe ser null tras la edicion");
        assertEquals("Aerolinea Admin Editada IT", fila[0],
                "El nombre debe reflejar el valor nuevo tras invocar editar");
        assertNotNull(fila[1],
                "La URL de la aerolinea no debe ser null tras la edicion");
        assertEquals("http://api-editada-it.aerolinea.com", fila[1],
                "La URL debe reflejar el valor nuevo tras invocar editar");
        assertEquals(10.0, ((Number) fila[2]).doubleValue(), 0.001,
                "El porcentaje de descuento debe reflejar el valor nuevo tras la edicion");
    }

    /**
     * Verifica que {@link AerolineaAdminRepository#listarWebserviceLibres} retorne una
     * lista no nula. Como el usuario de prueba ya tiene una aerolinea asignada, no debe
     * aparecer en esa lista. El metodo retorna todos los usuarios con Rol_ID = 3 que no
     * tienen entidad asociada, por lo que la lista puede estar vacia si todos los
     * usuarios webservice ya tienen una entidad.
     */
    @Test
    @Order(4)
    @DisplayName("4. listarWebserviceLibres retorna lista no nula")
    void listarWebserviceLibres_retornaListaNoNula() {
        List<UsuarioWebserviceLibreDTO> lista = aerolineaAdminRepository.listarWebserviceLibres();

        assertNotNull(lista,
                "listarWebserviceLibres no debe retornar null");

        // El usuario de prueba ya tiene aerolinea asignada, por lo que no debe aparecer
        boolean contieneUsuarioPrueba = lista.stream()
                .anyMatch(dto -> dto.getId() == usuarioId);
        assertFalse(contieneUsuarioPrueba,
                "El usuario de prueba no debe aparecer como libre porque ya tiene aerolinea asignada");

        // Si hay algun resultado, verifica que los DTOs tengan datos basicos validos
        if (!lista.isEmpty()) {
            UsuarioWebserviceLibreDTO primero = lista.get(0);
            assertNotNull(primero,
                    "El primer elemento de la lista de libres no debe ser null");
            assertTrue(primero.getId() > 0,
                    "El ID del usuario libre debe ser positivo");
            assertNotNull(primero.getUsername(),
                    "El username del usuario libre no debe ser null");
        }
    }
}
