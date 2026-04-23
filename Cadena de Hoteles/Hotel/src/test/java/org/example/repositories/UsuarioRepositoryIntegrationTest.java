package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.UsuarioAdminDTO;
import org.example.dtos.UsuarioPerfilResponseDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link UsuarioRepository}.
 * <p>
 * Conecta a Oracle real. Antes de cada caso inserta directamente via SQL un usuario
 * de prueba con campos conocidos y lo elimina al finalizar, garantizando aislamiento
 * completo. Tambien obtiene una ciudad existente en Oracle para las operaciones que
 * la requieren; si no existe ninguna ciudad la prueba se omite con
 * {@link Assumptions#assumeTrue}.
 * </p>
 * <p>
 * Requiere Oracle corriendo en {@code localhost:1521/XEPDB1} con las tablas
 * {@code Usuario}, {@code Ciudad} y {@code Rol} accesibles.
 * </p>
 */
@DisplayName("Integracion: UsuarioRepository - CRUD y validaciones de usuario contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private UsuarioRepository usuarioRepository;

    /** ID del usuario principal insertado en {@code @BeforeEach}. */
    private int usuarioIdInsertado;

    /** ID de la primera ciudad disponible en Oracle, obtenida en {@code @BeforeEach}. */
    private int ciudadIdReal;

    /** Username fijo del usuario de prueba principal. */
    private static final String USERNAME  = "test_usu_repo";

    /** Correo fijo del usuario de prueba principal. */
    private static final String CORREO    = "test_usu_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba principal. */
    private static final String PASAPORTE = "IT-USU-001";

    /**
     * Crea una nueva instancia del repositorio, obtiene una ciudad real de Oracle,
     * inserta directamente el usuario de prueba via SQL y guarda el ID generado
     * para operaciones y limpieza posteriores.
     * <p>
     * Si no hay ciudades disponibles en Oracle se omite la prueba completa.
     * </p>
     */
    @BeforeEach
    void setUp() {
        usuarioRepository = new UsuarioRepository();

        // Obtiene una ciudad existente en Oracle para usarla en crearUsuario
        List<Integer> ciudades = DatabaseManager.executeQuery(
                "SELECT ID FROM Ciudad WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay ciudades en Oracle; se omite la prueba");
        ciudadIdReal = ciudades.get(0);

        // Inserta el usuario de prueba directamente (sin llamar a crearUsuario)
        // para tener control exacto de los campos y el ID resultante
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
     * Elimina el usuario de prueba principal por su ID generado, garantizando que
     * ningun caso deje residuos en Oracle independientemente de su resultado.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    // -----------------------------------------------------------------------
    // Tests de existeUsername
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code existeUsername} retorne {@code true} cuando el username
     * buscado esta registrado en Oracle.
     */
    @Test
    @Order(1)
    @DisplayName("1. existeUsername con username existente retorna true")
    void existeUsername_usernameExistente_retornaTrue() {
        boolean existe = usuarioRepository.existeUsername(USERNAME);

        assertTrue(existe,
                "existeUsername debe retornar true para un username registrado en Oracle");
    }

    /**
     * Verifica que {@code existeUsername} retorne {@code false} cuando el username
     * buscado no esta registrado en Oracle.
     */
    @Test
    @Order(2)
    @DisplayName("2. existeUsername con username inexistente retorna false")
    void existeUsername_usernameInexistente_retornaFalse() {
        boolean existe = usuarioRepository.existeUsername("username_que_no_existe_jamas_xyz");

        assertFalse(existe,
                "existeUsername debe retornar false para un username que no existe en Oracle");
    }

    // -----------------------------------------------------------------------
    // Test de existeCorreo
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code existeCorreo} retorne {@code true} cuando el correo
     * buscado esta registrado en Oracle.
     */
    @Test
    @Order(3)
    @DisplayName("3. existeCorreo con correo existente retorna true")
    void existeCorreo_correoExistente_retornaTrue() {
        boolean existe = usuarioRepository.existeCorreo(CORREO);

        assertTrue(existe,
                "existeCorreo debe retornar true para un correo registrado en Oracle");
    }

    // -----------------------------------------------------------------------
    // Tests de existePasaporte
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code existePasaporte} retorne {@code true} cuando el numero
     * de pasaporte buscado esta registrado en Oracle.
     */
    @Test
    @Order(4)
    @DisplayName("4. existePasaporte con pasaporte existente retorna true")
    void existePasaporte_pasaporteExistente_retornaTrue() {
        boolean existe = usuarioRepository.existePasaporte(PASAPORTE);

        assertTrue(existe,
                "existePasaporte debe retornar true para un pasaporte registrado en Oracle");
    }

    /**
     * Verifica que {@code existePasaporte} retorne {@code false} de forma inmediata
     * cuando el pasaporte es {@code null} o una cadena vacia, sin consultar Oracle.
     */
    @Test
    @Order(5)
    @DisplayName("5. existePasaporte con pasaporte nulo o vacio retorna false")
    void existePasaporte_pasaporteNuloOVacio_retornaFalse() {
        assertFalse(usuarioRepository.existePasaporte(null),
                "existePasaporte debe retornar false cuando el pasaporte es null");
        assertFalse(usuarioRepository.existePasaporte(""),
                "existePasaporte debe retornar false cuando el pasaporte es cadena vacia");
    }

    // -----------------------------------------------------------------------
    // Test de crearUsuario
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code crearUsuario} inserte un nuevo usuario en Oracle y retorne
     * un ID positivo. El usuario secundario insertado durante esta prueba se elimina
     * dentro del propio metodo para mantener el esquema limpio de forma inmediata.
     */
    @Test
    @Order(6)
    @DisplayName("6. crearUsuario con datos validos retorna ID positivo")
    void crearUsuario_datosValidos_retornaIdPositivo() {
        int nuevoId = -1;
        try {
            nuevoId = usuarioRepository.crearUsuario(
                    "test_crear_usu@hotel.com",
                    PasswordHelper.hashear("CrearPass456!"),
                    "IT-CREAR-001",
                    "test_crear_usu",
                    "NombreTest",
                    "ApellidoTest",
                    "55551234",
                    Date.valueOf("1995-06-15"),
                    ciudadIdReal
            );

            assertTrue(nuevoId > 0,
                    "crearUsuario debe retornar un ID mayor a cero para datos validos");
        } catch (CamposDuplicadosException e) {
            fail("No se esperaba CamposDuplicadosException con datos unicos: " + e.getMessage());
        } finally {
            // Limpieza inmediata del usuario secundario dentro del propio test
            if (nuevoId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Usuario WHERE ID = ?", nuevoId);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Test de obtenerPerfil
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code obtenerPerfil} retorne un {@link UsuarioPerfilResponseDTO}
     * no nulo con los campos de identidad correctos para el usuario de prueba.
     */
    @Test
    @Order(7)
    @DisplayName("7. obtenerPerfil con usuario existente retorna DTO con datos correctos")
    void obtenerPerfil_usuarioExistente_retornaDtoConDatos() {
        UsuarioPerfilResponseDTO perfil = usuarioRepository.obtenerPerfil(usuarioIdInsertado);

        assertNotNull(perfil,
                "obtenerPerfil no debe retornar null para un usuario existente en Oracle");
        assertEquals(usuarioIdInsertado, perfil.getId(),
                "El ID del DTO debe coincidir con el ID del usuario insertado");
        assertEquals(USERNAME, perfil.getUsername(),
                "El username del DTO debe coincidir con el insertado");
        assertEquals(CORREO, perfil.getCorreo(),
                "El correo del DTO debe coincidir con el insertado");
    }

    // -----------------------------------------------------------------------
    // Test de actualizarTelefono
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code actualizarTelefono} modifique efectivamente el campo
     * {@code Telefono} del usuario en Oracle, comprobando el cambio mediante
     * {@code obtenerPerfil}.
     */
    @Test
    @Order(8)
    @DisplayName("8. actualizarTelefono con usuario existente cambia el campo en Oracle")
    void actualizarTelefono_usuarioExistente_cambiaElCampo() {
        String nuevoTelefono = "99887766";

        usuarioRepository.actualizarTelefono(usuarioIdInsertado, nuevoTelefono);

        UsuarioPerfilResponseDTO perfil = usuarioRepository.obtenerPerfil(usuarioIdInsertado);

        assertNotNull(perfil,
                "El perfil no debe ser null tras actualizar el telefono");
        assertEquals(nuevoTelefono, perfil.getTelefono(),
                "El telefono del perfil debe reflejar el valor actualizado en Oracle");
    }

    // -----------------------------------------------------------------------
    // Test de obtenerContrasena
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code obtenerContrasena} retorne una cadena no nula para un
     * usuario existente, confirmando que el hash fue almacenado correctamente
     * en Oracle durante el setup.
     */
    @Test
    @Order(9)
    @DisplayName("9. obtenerContrasena con usuario existente retorna hash no nulo")
    void obtenerContrasena_usuarioExistente_retornaHashNoNulo() {
        String contrasena = usuarioRepository.obtenerContrasena(usuarioIdInsertado);

        assertNotNull(contrasena,
                "obtenerContrasena no debe retornar null para un usuario existente en Oracle");
        assertFalse(contrasena.isBlank(),
                "La contrasena retornada no debe ser una cadena vacia o en blanco");
    }

    // -----------------------------------------------------------------------
    // Test de listarTodosConRol
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code listarTodosConRol} retorne una lista no nula y que al
     * menos contenga el usuario de prueba insertado en el {@code @BeforeEach},
     * confirmando que Oracle devuelve los registros correctamente.
     */
    @Test
    @Order(10)
    @DisplayName("10. listarTodosConRol retorna lista no nula que incluye el usuario de prueba")
    void listarTodosConRol_retornaListaNoNula() {
        List<UsuarioAdminDTO> lista = usuarioRepository.listarTodosConRol();

        assertNotNull(lista,
                "listarTodosConRol nunca debe retornar null");
        assertFalse(lista.isEmpty(),
                "La lista debe contener al menos el usuario de prueba insertado en setup");

        boolean encontrado = lista.stream()
                .anyMatch(dto -> dto.getId() == usuarioIdInsertado);
        assertTrue(encontrado,
                "La lista debe incluir el usuario de prueba con ID " + usuarioIdInsertado);
    }
}
