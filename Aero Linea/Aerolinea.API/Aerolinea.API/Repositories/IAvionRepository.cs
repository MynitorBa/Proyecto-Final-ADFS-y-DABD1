using Aerolinea.API.Models;

namespace Aerolinea.API.Repositories
{
    public interface IAvionRepository
    {
        Task<List<Avion>> ObtenerTodos();
        Task<Avion?> ObtenerPorId(int id);
        Task<int> Crear(Avion avion);
        Task<bool> Actualizar(Avion avion);
        Task<bool> Eliminar(int id);
        Task GuardarImagen(int avionId, string imagenBase64);
        Task EliminarImagen(int avionId);
        Task<string?> ObtenerImagen(int avionId);
    }
}
