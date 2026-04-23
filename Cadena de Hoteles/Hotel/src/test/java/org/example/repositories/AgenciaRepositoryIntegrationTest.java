package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AgenciaDTO;
import org.example.dtos.AgenciaIdentidad;
import org.example.dtos.CrearAgenciaRequestDTO;
import org.example.dtos.EditarAgenciaRequestDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link AgenciaRepository}.
 * <p>
 * Conecta a Oracle real para verificar el ciclo completo de gestion de agencias:
 * listado por usuario, creacion, edicion, cambio de estado, eliminacion y
 * busqueda por token. Cada caso opera sobre datos propios insertados en
 * {@code @BeforeEach} y eliminados en {@code @AfterEach}.
 * </p>
 * <p>
 * El {@code @BeforeEach} inserta en orden correcto de FK un usuario webservice
 * de prueba y a continuacion crea una agencia vinculada a ese usuario mediante
 * el propio repositorio bajo prueba. El {@code @AfterEach} elimina primero la
 * agencia (si aun existe) y luego el usuario, respetando el orden FK-inverso.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Usuario}, {@code Agencia} y {@code EstadoAgencia} accesibles.
 * </p>
 */
@DisplayName("Integracion: AgenciaRepository - Gestion de agencias contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgenciaRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private AgenciaRepository agenciaRepository;

    /** ID del usuario webservice de prueba insertado en {@code @BeforeEach}. */
    private int usuarioId;

    /** ID de la agencia de prueba creada en {@code @BeforeEach}. */
    private int agenciaId;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME  = "test_agencia_ws";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO    = "test_agencia_ws@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE = "IT-AGC-001";

    /**
     * Inicializa el repositorio, inserta el usuario webservice de prueba en Oracle
     * y crea la agencia de prueba mediante {@link AgenciaRepository#crear} para que
     * cada caso de prueba encuentre el escenario listo. Si la insercion del usuario
     * falla, la prueba se omite via {@link Assumptions#assumeTrue}.
     */
    @BeforeEach
    void setUp() {
        agenciaRepository = new AgenciaRepository();

        // 1. Inserta el usuario webservice de prueba
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

        // 2. Crea la agencia de prueba mediante el repositorio
        CrearAgenciaRequestDTO req = new CrearAgenciaRequestDTO();
        req.setNombre("Agencia Test IT");
        req.setCorreo("agencia_it@test.com");
        req.setUrlAgencia("http://test-it.agencia.com");

        AgenciaDTO creada = agenciaRepository.crear(usuarioId, req);
        agenciaId = creada.getId();
        Assumptions.assumeTrue(agenciaId > 0,
                "No se pudo crear la agencia de prueba — se omite la prueba");
    }

    /**
     * Elimina en orden FK-inverso la agencia (si aun existe) y luego el usuario
     * de prueba, garantizando que Oracle no quede con residuos incluso cuando un
     * caso de prueba llama a {@link AgenciaRepository#eliminar} internamente.
     */
    @AfterEach
    void tearDown() {
        // 1. Intenta eliminar la agencia si todavia existe
        if (agenciaId > 0) {
            try {
                agenciaRepository.eliminar(agenciaId, usuarioId);
            } catch (Exception ignorada) {
                // Ya fue eliminada por el propio test o no pertenece al usuario
                DatabaseManager.executeUpdate(
                        "DELETE FROM Agencia WHERE ID = ?", agenciaId);
            }
        }
        // 2. Elimina el usuario de prueba
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioId);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link AgenciaRepository#listarPorUsuario} retorne una lista no
     * nula con al menos una agencia cuando el usuario tiene una agencia vinculada.
     * Comprueba ademas que el nombre y el estado del primer elemento no sean nulos.
     */
    @Test
    @Order(1)
    @DisplayName("1. listarPorUsuario con usuario con agencia retorna lista con al menos una")
    void listarPorUsuario_usuarioConAgencia_retornaListaConAlMenosUna() {
        List<AgenciaDTO> lista = agenciaRepository.listarPorUsuario(usuarioId);

        assertNotNull(lista,
                "listarPorUsuario no debe retornar null para un usuario con agencia");
        assertFalse(lista.isEmpty(),
                "La lista debe tener al menos una agencia para el usuario de prueba");

        AgenciaDTO primera = lista.get(0);
        assertNotNull(primera,
                "El primer elemento de la lista no debe ser null");
        assertNotNull(primera.getNombre(),
                "El nombre de la agencia no debe ser null");
        assertNotNull(primera.getEstado(),
                "El estado de la agencia no debe ser null");
    }

    /**
     * Verifica que {@link AgenciaRepository#crear} retorne un {@link AgenciaDTO}
     * con un ID positivo y con los campos nombre y correo tal como fueron enviados.
     * La agencia creada en este test se elimina en un bloque {@code finally} para
     * no interferir con el tearDown principal.
     */
    @Test
    @Order(2)
    @DisplayName("2. crear con datos validos retorna agencia con ID positivo")
    void crear_datosValidos_retornaAgenciaConId() {
        // Se necesita un segundo usuario para poder crear una segunda agencia,
        // ya que el repositorio rechaza un segundo registro para el mismo usuario.
        int segundoUsuarioId = -1;
        int segundaAgenciaId = -1;
        try {
            segundoUsuarioId = DatabaseManager.executeInsertReturnId(
                    "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                            "VALUES (?, ?, ?, 1, ?)",
                    "ID",
                    "test_agencia_ws2",
                    "test_agencia_ws2@hotel.com",
                    PasswordHelper.hashear("TestPass123"),
                    "IT-AGC-002"
            );
            Assumptions.assumeTrue(segundoUsuarioId > 0,
                    "No se pudo insertar el segundo usuario — se omite la verificacion");

            CrearAgenciaRequestDTO req = new CrearAgenciaRequestDTO();
            req.setNombre("Agencia Test IT Crear");
            req.setCorreo("agencia_crear@test.com");
            req.setUrlAgencia("http://crear-it.agencia.com");

            AgenciaDTO resultado = agenciaRepository.crear(segundoUsuarioId, req);

            assertNotNull(resultado,
                    "crear no debe retornar null cuando los datos son validos");
            segundaAgenciaId = resultado.getId();
            assertTrue(segundaAgenciaId > 0,
                    "El ID de la agencia creada debe ser positivo");
            assertNotNull(resultado.getNombre(),
                    "El nombre de la agencia creada no debe ser null");
            assertEquals("Agencia Test IT Crear", resultado.getNombre(),
                    "El nombre de la agencia debe coincidir con el valor enviado");
            assertNotNull(resultado.getCorreo(),
                    "El correo de la agencia creada no debe ser null");
            assertEquals("agencia_crear@test.com", resultado.getCorreo(),
                    "El correo de la agencia debe coincidir con el valor enviado");
        } finally {
            if (segundaAgenciaId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Agencia WHERE ID = ?", segundaAgenciaId);
            }
            if (segundoUsuarioId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Usuario WHERE ID = ?", segundoUsuarioId);
            }
        }
    }

    /**
     * Verifica que {@link AgenciaRepository#editar} actualice correctamente el
     * nombre de una agencia existente. Crea una segunda agencia con un segundo
     * usuario auxiliar, la edita y confirma el cambio consultando
     * {@link AgenciaRepository#listarPorUsuario}.
     */
    @Test
    @Order(3)
    @DisplayName("3. editar con datos nuevos actualiza el nombre de la agencia en Oracle")
    void editar_datosNuevos_actualizaNombre() {
        int usuarioAuxId  = -1;
        int agenciaAuxId  = -1;
        try {
            // Inserta un usuario auxiliar para poder crear una agencia editable
            usuarioAuxId = DatabaseManager.executeInsertReturnId(
                    "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                            "VALUES (?, ?, ?, 1, ?)",
                    "ID",
                    "test_agencia_ws_edit",
                    "test_agencia_ws_edit@hotel.com",
                    PasswordHelper.hashear("TestPass123"),
                    "IT-AGC-EDIT-001"
            );
            Assumptions.assumeTrue(usuarioAuxId > 0,
                    "No se pudo insertar el usuario auxiliar — se omite la verificacion");

            CrearAgenciaRequestDTO crearReq = new CrearAgenciaRequestDTO();
            crearReq.setNombre("Agencia Antes de Editar");
            crearReq.setCorreo("agencia_antes@test.com");
            crearReq.setUrlAgencia("http://antes.agencia.com");

            AgenciaDTO creada = agenciaRepository.crear(usuarioAuxId, crearReq);
            agenciaAuxId = creada.getId();
            Assumptions.assumeTrue(agenciaAuxId > 0,
                    "No se pudo crear la agencia auxiliar — se omite la verificacion");

            // Edita la agencia con datos nuevos
            EditarAgenciaRequestDTO editarReq = new EditarAgenciaRequestDTO();
            editarReq.setNombre("Agencia Despues de Editar");
            editarReq.setCorreo("agencia_despues@test.com");
            editarReq.setUrlAgencia("http://despues.agencia.com");
            editarReq.setPorcentajeDescuento(5.0);
            editarReq.setEstadoId(1);

            agenciaRepository.editar(agenciaAuxId, editarReq);

            // Verifica el cambio consultando la agencia del usuario auxiliar
            List<AgenciaDTO> lista = agenciaRepository.listarPorUsuario(usuarioAuxId);
            assertNotNull(lista,
                    "listarPorUsuario no debe retornar null tras la edicion");
            assertFalse(lista.isEmpty(),
                    "La lista no debe estar vacia tras la edicion");

            AgenciaDTO editada = lista.get(0);
            assertNotNull(editada.getNombre(),
                    "El nombre de la agencia editada no debe ser null");
            assertEquals("Agencia Despues de Editar", editada.getNombre(),
                    "El nombre debe reflejar el valor nuevo tras la edicion");
        } finally {
            if (agenciaAuxId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Agencia WHERE ID = ?", agenciaAuxId);
            }
            if (usuarioAuxId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Usuario WHERE ID = ?", usuarioAuxId);
            }
        }
    }

    /**
     * Verifica que {@link AgenciaRepository#cambiarEstado} actualice el EstadoID
     * de una agencia activa a Inactiva (ID 2). Confirma el cambio mediante una
     * consulta directa a Oracle sobre la columna {@code EstadoID}.
     */
    @Test
    @Order(4)
    @DisplayName("4. cambiarEstado con agencia activa cambia el EstadoID en Oracle")
    void cambiarEstado_agenciaActiva_cambiaEstado() {
        // Llama a cambiarEstado para pasar la agencia a EstadoID = 2 (Inactiva)
        agenciaRepository.cambiarEstado(agenciaId, usuarioId, 2);

        // Verifica el cambio directamente en Oracle
        List<Integer> estadoIds = DatabaseManager.executeQuery(
                "SELECT EstadoID FROM Agencia WHERE ID = ?",
                rs -> rs.getInt("EstadoID"),
                agenciaId
        );

        assertNotNull(estadoIds,
                "La lista de EstadoID no debe ser null");
        assertFalse(estadoIds.isEmpty(),
                "Debe existir la agencia en Oracle tras cambiar su estado");
        assertEquals(2, estadoIds.get(0),
                "El EstadoID debe ser 2 (Inactiva) tras la llamada a cambiarEstado");
    }

    /**
     * Verifica que {@link AgenciaRepository#obtenerAgenciaPorToken} retorne
     * {@code null} cuando se busca un token que no existe en Oracle.
     */
    @Test
    @Order(5)
    @DisplayName("5. obtenerAgenciaPorToken con token inexistente retorna null")
    void obtenerAgenciaPorToken_sinToken_retornaNull() {
        AgenciaIdentidad resultado = agenciaRepository.obtenerAgenciaPorToken(
                "token_inexistente_xyz");

        assertNull(resultado,
                "obtenerAgenciaPorToken debe retornar null cuando el token no existe en Oracle");
    }
}
