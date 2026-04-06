package org.example.services;

import org.example.dtos.LoginRequestDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.helpers.PasswordHelper;
import org.example.models.Usuario;
import org.example.repositories.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthService.
 * Usa BCrypt real (no se mockea PasswordHelper ni JwtHelper).
 * Cubre los tres caminos de login: usuario inexistente, contrasena erronea y exito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitarios")
class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(authRepository);
    }

    // -- login

    @Test
    @DisplayName("login_usuarioNoExiste_lanzaCredencialesInvalidasException")
    void login_usuarioNoExiste_lanzaCredencialesInvalidasException() {
        when(authRepository.buscarPorIdentificador("usuario_inexistente")).thenReturn(null);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentificador("usuario_inexistente");
        request.setContrasena("cualquierContrasena");

        assertThrows(CredencialesInvalidasException.class, () -> service.login(request));

        verify(authRepository).buscarPorIdentificador("usuario_inexistente");
    }

    @Test
    @DisplayName("login_contrasenaIncorrecta_lanzaCredencialesInvalidasException")
    void login_contrasenaIncorrecta_lanzaCredencialesInvalidasException() {
        // Se genera un hash real de "correctPass" con BCrypt
        String hashReal = PasswordHelper.hashear("correctPass");

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("user1");
        usuario.setContrasena(hashReal);
        usuario.setRolId(2);

        when(authRepository.buscarPorIdentificador("user1")).thenReturn(usuario);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentificador("user1");
        request.setContrasena("wrongPass");

        assertThrows(CredencialesInvalidasException.class, () -> service.login(request));

        verify(authRepository).buscarPorIdentificador("user1");
    }

    @Test
    @DisplayName("login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta")
    void login_credencialesCorrectas_retornaLoginResultadoConTokenYRespuesta() {
        // Se genera un hash real de "pass123" con BCrypt
        String hashReal = PasswordHelper.hashear("pass123");

        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setUsername("adminUser");
        usuario.setContrasena(hashReal);
        usuario.setRolId(1);

        when(authRepository.buscarPorIdentificador("adminUser")).thenReturn(usuario);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setIdentificador("adminUser");
        request.setContrasena("pass123");

        AuthService.LoginResultado resultado = service.login(request);

        assertNotNull(resultado);
        assertNotNull(resultado.token());
        assertFalse(resultado.token().isBlank());
        assertNotNull(resultado.respuesta());
        assertEquals("Login exitoso", resultado.respuesta().getMensaje());
        assertEquals("adminUser", resultado.respuesta().getUsername());
        assertEquals(1, resultado.respuesta().getRolId());
    }

}
