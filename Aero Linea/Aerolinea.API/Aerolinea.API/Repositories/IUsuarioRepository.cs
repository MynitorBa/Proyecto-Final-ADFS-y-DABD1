using Aerolinea.API.DTOs;
using Aerolinea.API.Models;

namespace Aerolinea.API.Repositories
{
    public interface IUsuarioRepository
    {
        Task<int> CrearUsuario(Usuario usuario);
        Task AgregarNacionalidades(int usuarioId, List<string> nacionalidades);
        Task<RegisterConstraint> VerificarExistencia(string correo, string username, string pasaporte);
        Task<Usuario?> ObtenerPorCorreoOUsername(string correoOUsername);
        Task<string?> ObtenerNombreRol(int rolId);
        Task<bool> ActualizarRol(int usuarioId, int nuevoRolId);
        Task<bool> UsuarioExiste(int usuarioId);
        Task<bool> RolExiste(int rolId);
        Task<List<Usuario>> ObtenerTodos();
    }
}
