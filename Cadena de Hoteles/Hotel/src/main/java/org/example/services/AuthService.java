package org.example.services;

import org.example.dtos.LoginRequestDTO;
import org.example.dtos.LoginResponseDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.helpers.JwtHelper;
import org.example.helpers.PasswordHelper;
import org.example.models.Usuario;
import org.example.repositories.AuthRepository;
import org.example.repositories.LogRepository;

import io.jsonwebtoken.Claims;

/**
 * Service de autenticacion de usuarios.
 * Valida credenciales, genera el token JWT para la sesion
 * y registra un log por cada intento de login.
 */
public class AuthService {

    private final AuthRepository authRepository;
    private final LogRepository  logRepository;

    /**
     * Crea una instancia de AuthService con sus dependencias inyectadas.
     *
     * @param authRepository repository de autenticacion de usuarios.
     * @param logRepository  repository de logs de sesion.
     */
    public AuthService(AuthRepository authRepository, LogRepository logRepository) {
        this.authRepository = authRepository;
        this.logRepository  = logRepository;
    }

    /**
     * Agrupa el token JWT y los datos de respuesta tras un login exitoso.
     * @param token     JWT generado para la sesion.
     * @param respuesta datos del usuario autenticado.
     */
    public record LoginResultado(String token, LoginResponseDTO respuesta) {}

    /**
     * Autentica a un usuario con su identificador y contrasena.
     * Busca el usuario, verifica el hash de la contrasena y genera el JWT.
     * Registra un log de LOGIN_EXITOSO, LOGIN_FALLIDO o LOGIN_ERROR_INTERNO segun el resultado.
     *
     * @param request  datos de login con identificador y contrasena.
     * @param ip       IP del cliente extraida del contexto HTTP.
     * @param userAgent User-Agent del cliente extraido del contexto HTTP.
     * @return LoginResultado con el token y los datos del usuario.
     * @throws CredencialesInvalidasException si el usuario no existe o la contrasena no coincide.
     */
    public LoginResultado login(LoginRequestDTO request, String ip, String userAgent) {
        try {
            Usuario usuario = authRepository.buscarPorIdentificador(request.getIdentificador());

            if (usuario == null || !PasswordHelper.verificar(request.getContrasena(), usuario.getContrasena())) {
                // Login fallido: credenciales incorrectas
                logRepository.registrar(
                        LogRepository.TIPO_LOGIN_FALLIDO,
                        null,
                        request.getIdentificador(),
                        false,
                        ip,
                        userAgent,
                        null
                );
                throw new CredencialesInvalidasException();
            }

            String token = JwtHelper.generarToken(usuario.getId(), usuario.getUsername(), usuario.getRolId());

            // Login exitoso
            logRepository.registrar(
                    LogRepository.TIPO_LOGIN_EXITOSO,
                    usuario.getId(),
                    usuario.getUsername(),
                    true,
                    ip,
                    userAgent,
                    null
            );

            LoginResponseDTO respuesta = new LoginResponseDTO(
                    "Login exitoso",
                    usuario.getUsername(),
                    usuario.getRolId()
            );

            return new LoginResultado(token, respuesta);

        } catch (CredencialesInvalidasException e) {
            // Re-lanza sin loggear de nuevo, ya se registro arriba
            throw e;
        } catch (Exception e) {
            // Error interno inesperado (BD caida, JWT fallo, etc.)
            logRepository.registrar(
                    LogRepository.TIPO_LOGIN_ERROR_INTERNO,
                    null,
                    request.getIdentificador(),
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw new RuntimeException("Error interno durante el login", e);
        }
    }






    /**
     * Registra el logout de un usuario en el log de sesion.
     * Intenta parsear el JWT para obtener los datos del usuario.
     * Si el token es invalido o nulo, registra un LOGOUT_ERROR_INTERNO.
     *
     * @param token     JWT extraido de la cookie, puede ser null o vacio.
     * @param ip        IP del cliente.
     * @param userAgent User-Agent del cliente.
     */
    public void logout(String token, String ip, String userAgent) {
        if (token == null || token.isBlank()) {
            logRepository.registrar(
                    LogRepository.TIPO_LOGOUT_ERROR_INTERNO,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    "Logout sin token presente"
            );
            return;
        }

        try {
            Claims claims     = JwtHelper.verificarToken(token);
            int    usuarioId  = JwtHelper.getUsuarioId(claims);
            String username   = JwtHelper.getUsername(claims);

            logRepository.registrar(
                    LogRepository.TIPO_LOGOUT_EXITOSO,
                    usuarioId,
                    username,
                    true,
                    ip,
                    userAgent,
                    null
            );

        } catch (Exception e) {
            // Token expirado, malformado o firma invalida
            logRepository.registrar(
                    LogRepository.TIPO_LOGOUT_ERROR_INTERNO,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    "Token invalido al hacer logout: " + e.getMessage()
            );
        }
    }

}