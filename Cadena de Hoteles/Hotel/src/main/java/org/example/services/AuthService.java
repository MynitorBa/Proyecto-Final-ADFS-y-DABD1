package org.example.services;

import org.example.dtos.LoginRequestDTO;
import org.example.dtos.LoginResponseDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.helpers.JwtHelper;
import org.example.helpers.PasswordHelper;
import org.example.models.Usuario;
import org.example.repositories.AuthRepository;

public class AuthService {

    private final AuthRepository authRepository = new AuthRepository();

    public record LoginResultado(String token, LoginResponseDTO respuesta) {}

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