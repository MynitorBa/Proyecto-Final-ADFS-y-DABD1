using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de autenticacion. Verifica las credenciales del usuario contra la base de datos
    /// y retorna la informacion de sesion si la autenticacion es exitosa.
    /// </summary>
    public class AuthService
    {
        private readonly UsuarioRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de usuarios.
        /// </summary>
        public AuthService(UsuarioRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Autentica al usuario usando correo o nombre de usuario y contrasena.
        /// Verifica el hash de contrasena con BCrypt. Retorna los datos de sesion
        /// incluyendo rol del usuario, o null si las credenciales son incorrectas.
        /// </summary>
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
