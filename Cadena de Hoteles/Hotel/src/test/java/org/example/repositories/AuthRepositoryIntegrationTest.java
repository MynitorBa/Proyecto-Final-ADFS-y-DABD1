package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.helpers.PasswordHelper;
import org.example.models.Usuario;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link AuthRepository}.
 * <p>
 * Conecta a Oracle real, inserta un usuario de prueba con contrasena hasheada
 * antes de cada caso y elimina el registro al finalizar, garantizando aislamiento
 * completo entre ejecuciones. Requiere que Oracle este corriendo en
 * localhost:1521/XEPDB1 con la tabla {@code Usuario} accesible.
 * </p>
 */
@DisplayName("Integracion: AuthRepository - Busqueda de usuario por identificador contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private AuthRepository authRepository;

    /** ID del usuario insertado en cada {@code @BeforeEach}, usado para limpieza. */
    private int usuarioIdInsertado;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME    = "test_auth_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO      = "test_auth_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE   = "IT-AUTH-001";

    /**
     * Crea una nueva instancia del repositorio, inserta un usuario de prueba con
     * contrasena hasheada en Oracle y guarda el ID generado para la limpieza posterior.
     */
    @BeforeEach
    void setUp() {
        authRepository = new AuthRepository();

        usuarioIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, ?)",
                "ID",
                USERNAME,
                CORREO,
                PasswordHelper.hashear("TestPass123!"),
                PASAPORTE
        );

        Assumptions.assumeTrue(usuarioIdInsertado > 0,
                "El INSERT de setup no retorno un ID valido; se omite la prueba");
    }

    /**
     * Elimina el usuario de prueba por su ID generado, garantizando que ninguna prueba
     * deje residuos en Oracle independientemente de su resultado.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code buscarPorIdentificador} retorne el {@link Usuario} correcto
     * cuando se busca por su {@code Username} exacto.
     * <p>
     * Se afirma primero que el resultado no es nulo y luego que el username
     * almacenado en Oracle coincide con el buscado.
     * </p>
     */
    @Test
    @Order(1)
    @DisplayName("1. buscarPorIdentificador por username retorna usuario no nulo con username correcto")
    void buscarPorIdentificador_porUsername_retornaUsuario() {
        Usuario usuario = authRepository.buscarPorIdentificador(USERNAME);

        assertNotNull(usuario,
                "El usuario buscado por username no debe ser null");
        assertEquals(USERNAME, usuario.getUsername(),
                "El username del usuario retornado debe coincidir con el buscado");
    }

    /**
     * Verifica que {@code buscarPorIdentificador} retorne el {@link Usuario} correcto
     * cuando se busca por su {@code Correo} exacto.
     * <p>
     * Se afirma primero que el resultado no es nulo y luego que el correo
     * almacenado en Oracle coincide con el buscado.
     * </p>
     */
    @Test
    @Order(2)
    @DisplayName("2. buscarPorIdentificador por correo retorna usuario no nulo con correo correcto")
    void buscarPorIdentificador_porCorreo_retornaUsuario() {
        Usuario usuario = authRepository.buscarPorIdentificador(CORREO);

        assertNotNull(usuario,
                "El usuario buscado por correo no debe ser null");
        assertEquals(CORREO, usuario.getCorreo(),
                "El correo del usuario retornado debe coincidir con el buscado");
    }

    /**
     * Verifica que {@code buscarPorIdentificador} retorne {@code null} cuando el
     * identificador proporcionado no corresponde a ningun usuario en Oracle.
     * <p>
     * Se usa un identificador con sufijo aleatorio para minimizar la probabilidad
     * de colision con datos reales en el entorno de prueba.
     * </p>
     */
    @Test
    @Order(3)
    @DisplayName("3. buscarPorIdentificador con identificador inexistente retorna null")
    void buscarPorIdentificador_identificadorInexistente_retornaNull() {
        Usuario usuario = authRepository.buscarPorIdentificador("no_existe_jamas_xyz");

        assertNull(usuario,
                "El resultado debe ser null cuando el identificador no existe en Oracle");
    }
}
