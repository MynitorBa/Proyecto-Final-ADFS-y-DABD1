using Aerolinea.API.Models;

namespace Aerolinea.API.Repositories
{
    public interface ITripulacionRepository
    {
        Task<List<Tripulante>> ObtenerTodos();
        Task<Tripulante?> ObtenerPorId(int id);
        Task<string?> ObtenerNombreRol(int rolId);
        Task<int> Crear(Tripulante tripulante);
        Task<bool> Actualizar(Tripulante tripulante);
        Task<bool> Eliminar(int id);
        Task GuardarImagen(int tripulanteId, string imagenBase64);
        Task EliminarImagen(int tripulanteId);
        Task<List<RolTripulacion>> ObtenerRoles();
    }
}
