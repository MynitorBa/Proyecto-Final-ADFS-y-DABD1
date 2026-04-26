using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de autenticacion. Verifica credenciales, retorna datos de sesion
    /// y registra un log por cada intento de login.
    /// </summary>
    public class AuthService : IAuthService
    {
        private readonly IUsuarioRepository _repository;
        private readonly LogRepository _logRepository;

        public AuthService(IUsuarioRepository repository, LogRepository logRepository)
        {
            _repository = repository;
            _logRepository = logRepository;
        }

        /// <summary>
        /// Autentica al usuario usando correo o username y contrasena.
        /// Registra LOGIN_EXITOSO, LOGIN_FALLIDO o LOGIN_ERROR_INTERNO segun el resultado.
        /// </summary>
        public async Task<LoginResponseDto?> Login(LoginRequestDto request, string? ip, string? userAgent)
        {
            try
            {
                var usuario = await _repository.ObtenerPorCorreoOUsername(request.CorreoOUsername);

                if (usuario == null || !BCrypt.Net.BCrypt.Verify(request.Contrasena, usuario.ContrasenaHash))
                {
                    await _logRepository.Registrar(
                        LogRepository.TipoLoginFallido,
                        null,
                        request.CorreoOUsername,
                        false,
                        ip,
                        userAgent,
                        null
                    );
                    return null;
                }

                var rolNombre = await _repository.ObtenerNombreRol(usuario.RolID);

                await _logRepository.Registrar(
                    LogRepository.TipoLoginExitoso,
                    usuario.Id,
                    usuario.Username,
                    true,
                    ip,
                    userAgent,
                    null
                );

                return new LoginResponseDto
                {
                    UsuarioId = usuario.Id,
                    Nombre = usuario.Nombre,
                    Correo = usuario.Correo,
                    RolId = usuario.RolID,
                    RolNombre = rolNombre ?? "Usuario Registrado"
                };
            }
            catch (Exception e)
            {
                await _logRepository.Registrar(
                    LogRepository.TipoLoginErrorInterno,
                    null,
                    request.CorreoOUsername,
                    false,
                    ip,
                    userAgent,
                    e.Message
                );
                throw;
            }
        }



        public async Task Logout(int? usuarioId, string? username, string? ip, string? userAgent)
        {
            try
            {
                await _logRepository.Registrar(
                    LogRepository.TipoLogoutExitoso,
                    usuarioId,
                    username,
                    true,
                    ip,
                    userAgent,
                    null
                );
            }
            catch (Exception e)
            {
                await _logRepository.Registrar(
                    LogRepository.TipoLogoutErrorInterno,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.Message
                );
            }
        }
    }
}