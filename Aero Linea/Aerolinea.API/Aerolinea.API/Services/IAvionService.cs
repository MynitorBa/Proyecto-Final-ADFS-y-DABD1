using Aerolinea.API.DTOs;

namespace Aerolinea.API.Services
{
    public interface IAvionService
    {
        Task<List<AvionDTO>> ObtenerTodos(bool incluirInactivos = false);
        Task<AvionDTO?> ObtenerPorId(int id);
        Task<AvionDTO> Crear(CrearAvionDTO crearAvionDto);
        Task<bool> Actualizar(int id, CrearAvionDTO actualizarAvionDto);
        Task<bool> Eliminar(int id);
        Task GuardarImagen(int avionId, string imagenBase64);
        Task EliminarImagen(int avionId);
        Task<bool> CambiarEstado(int id, bool activo);
        Task<(int totalFuturos, List<string> numeros48h)> VerificarVuelosActivos(int avionId);
    }
}
