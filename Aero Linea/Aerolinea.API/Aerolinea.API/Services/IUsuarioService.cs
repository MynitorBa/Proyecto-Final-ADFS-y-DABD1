using Aerolinea.API.DTOs;
using Aerolinea.API.Models;

namespace Aerolinea.API.Services
{
    public interface IUsuarioService
    {
        Task CrearUsuario(CrearUsuarioDTO dto);
        Task<RegisterConstraint> VerificarConstraints(CrearUsuarioDTO dto);
        Task<(bool exito, string mensaje)> CambiarRol(CambiarRolDTO dto);
        Task<List<object>> ObtenerTodos();
    }
}
