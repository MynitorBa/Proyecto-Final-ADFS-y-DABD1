using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AuthService
    {
        private readonly UsuarioRepository _repository;

        public AuthService(UsuarioRepository repository)
        {
            _repository = repository;
        }

        public async Task<LoginResponseDto?> Login(LoginRequestDto request)
        {
            var usuario = await _repository.ObtenerPorCorreoOUsername(request.CorreoOUsername);

            if (usuario == null)
                return null;

            bool passwordOk = BCrypt.Net.BCrypt.Verify(request.Contrasena, usuario.ContrasenaHash);

            if (!passwordOk)
                return null;

            var rolNombre = await _repository.ObtenerNombreRol(usuario.RolID);

            return new LoginResponseDto
            {
                UsuarioId = usuario.Id,
                Nombre = usuario.Nombre,
                Correo = usuario.Correo,
                RolId = usuario.RolID,
                RolNombre = rolNombre ?? "Usuario Registrado"
            };
        }
    }
}