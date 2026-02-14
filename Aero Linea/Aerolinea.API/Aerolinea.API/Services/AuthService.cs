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

        public async Task<bool> Login(LoginRequestDto request)
        {
            var usuario = await _repository
                .ObtenerPorCorreoOUsername(request.CorreoOUsername);

            if (usuario == null)
                return false;

            return BCrypt.Net.BCrypt.Verify(
                request.Contrasena,
                usuario.ContrasenaHash
            );
        }
    }
}
