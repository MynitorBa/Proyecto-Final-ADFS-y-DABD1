package org.example.services;

import org.example.dtos.LoginRequestDTO;
import org.example.dtos.LoginResponseDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.helpers.JwtHelper;
import org.example.helpers.PasswordHelper;
import org.example.models.Usuario;
import org.example.repositories.AuthRepository;

/**
 * Service de autenticacion de usuarios.
 * Valida credenciales y genera el token JWT para la sesion.
 */
public class AuthService {

    private final AuthRepository authRepository = new AuthRepository();

    /**
     * Agrupa el token JWT y los datos de respuesta tras un login exitoso.
     * @param token    JWT generado para la sesion.
     * @param respuesta datos del usuario autenticado.
     */
    public record LoginResultado(String token, LoginResponseDTO respuesta) {}

    /**
     * Autentica a un usuario con su identificador y contrasena.
     * Busca el usuario, verifica el hash de la contrasena y genera el JWT.
     * @param request datos de login con identificador y contrasena.
     * @return LoginResultado con el token y los datos del usuario.
     * @throws CredencialesInvalidasException si el usuario no existe o la contrasena no coincide.
     */
    public LoginResultado login(LoginRequestDTO request) {

        Usuario usuario = authRepository.buscarPorIdentificador(request.getIdentificador());

        if (usuario == null || !PasswordHelper.verificar(request.getContrasena(), usuario.getContrasena())) {
            throw new CredencialesInvalidasException();
        }

        String token = JwtHelper.generarToken(usuario.getId(), usuario.getUsername(), usuario.getRolId());

        LoginResponseDTO respuesta = new LoginResponseDTO(
                "Login exitoso",
                usuario.getUsername(),
                usuario.getRolId()
        );

        return new LoginResultado(token, respuesta);
    }
}