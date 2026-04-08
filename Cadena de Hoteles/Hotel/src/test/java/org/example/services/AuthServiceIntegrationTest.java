package org.example.services;

import org.example.data.DatabaseManager;
import org.example.dtos.LoginRequestDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.helpers.PasswordHelper;
import org.example.repositories.AuthRepository;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para AuthService.
 * Conecta a Oracle real, inserta un usuario de prueba antes de cada caso
 * y lo elimina al finalizar para no dejar datos residuales.
 * Requiere que Oracle este corriendo en localhost:1521/XEPDB1.
 */
@DisplayName("Integracion: AuthService - Flujo de Login")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIntegrationTest {

    private static final String USERNAME_TEST    = "test_auth_integration";
    private static final String CORREO_TEST      = "test_auth_integration@hotel.com";
    private static final String CONTRASENA_PLANA = "TestPass123";

    private AuthService authService;
    private int usuarioIdInsertado;

    /**
     * Crea el service con el repositorio real e inserta un usuario de prueba en Oracle
     * antes de cada caso.
     */
    @BeforeEach
    void setUp() {
        authService = new AuthService(new AuthRepository());

        usuarioIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 2, 'IT-PASAPORTE')",
                "ID",
                USERNAME_TEST, CORREO_TEST, PasswordHelper.hashear(CONTRASENA_PLANA)
        );
    }

    /**
     * Elimina el usuario de prueba de Oracle al finalizar cada caso.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate("DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    /**
     * Verifica que el login sea exitoso contra Oracle cuando se proporciona
     * el username correcto y la contrasena coincide con el hash almacenado en la BD.
     */
    @Test
    @Order(1)
    @DisplayName("1. Login exitoso con username real almacenado en Oracle")
    void loginExitosoConUsername() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentificador(USERNAME_TEST);
        request.setContrasena(CONTRASENA_PLANA);

        AuthService.LoginResultado resultado = authService.login(request);

        assertNotNull(resultado,                                          "El resultado no debe ser null");
        assertNotNull(resultado.token(),                                  "Debe generarse un token JWT real");
        assertFalse(resultado.token().isBlank(),                          "El token no debe estar vacio");
        assertEquals(USERNAME_TEST, resultado.respuesta().getUsername(),  "El username debe coincidir con el insertado en Oracle");
        assertEquals(2,             resultado.respuesta().getRolId(),     "El rol debe ser 2");
        assertEquals("Login exitoso", resultado.respuesta().getMensaje(), "El mensaje debe ser correcto");
    }

    /**
     * Verifica que el login sea exitoso usando el correo electronico como identificador,
     * ya que Oracle busca por username y correo en la misma consulta.
     */
    @Test
    @Order(2)
    @DisplayName("2. Login exitoso con correo electronico real almacenado en Oracle")
    void loginExitosoConCorreo() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentificador(CORREO_TEST);
        request.setContrasena(CONTRASENA_PLANA);

        AuthService.LoginResultado resultado = authService.login(request);

        assertNotNull(resultado.token(),                                 "Debe generarse el token");
        assertEquals(USERNAME_TEST, resultado.respuesta().getUsername(), "El username debe coincidir con el de Oracle");
    }

    /**
     * Verifica que se lanza CredencialesInvalidasException cuando se busca
     * un identificador que no existe en la tabla Usuario de Oracle.
     */
    @Test
    @Order(3)
    @DisplayName("3. Lanza excepcion cuando el usuario no existe en Oracle")
    void loginFallaUsuarioInexistente() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentificador("usuario_que_no_existe_en_oracle");
        request.setContrasena("cualquier");

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.login(request),
                "Debe lanzar excepcion cuando Oracle no encuentra el usuario"
        );
    }

    /**
     * Verifica que se lanza CredencialesInvalidasException cuando el usuario existe
     * en Oracle pero la contrasena proporcionada no coincide con el hash BCrypt almacenado.
     */
    @Test
    @Order(4)
    @DisplayName("4. Lanza excepcion cuando la contrasena no coincide con el hash en Oracle")
    void loginFallaContrasenaIncorrecta() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentificador(USERNAME_TEST);
        request.setContrasena("ContrasenaEquivocada999");

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.login(request),
                "Debe lanzar excepcion cuando el hash BCrypt no coincide en Oracle"
        );
    }
}