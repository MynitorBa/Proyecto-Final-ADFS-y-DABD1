using Aerolinea.API.DTOs;
using Aerolinea.API.Models;

namespace Aerolinea.API.Services
{
    public interface ITripulacionService
    {
        Task<List<TripulanteDTO>> ObtenerTodos();
        Task<TripulanteDTO?> ObtenerPorId(int id);
        Task<TripulanteDTO> Crear(CrearTripulanteDTO crearTripulanteDTO);
        Task<bool> Actualizar(int id, CrearTripulanteDTO actualizarTripulanteDto);
        Task<bool> Eliminar(int id);
        Task GuardarImagen(int tripulanteId, string imagenBase64);
        Task EliminarImagen(int tripulanteId);
        Task<List<RolTripulacion>> ObtenerRoles();
    }
}
