using Aerolinea.API.Models.DTOs;

namespace Aerolinea.API.Repositories
{
    public interface IRutaRepository
    {
        Task<List<RutaDTO>> ObtenerTodas();
        Task<bool> ActualizarDuracion(int rutaId, int minutos);
        Task<(int duracion, string? tzOrigen, string? tzDestino)> ObtenerInfoRuta(int origenId, int destinoId);
        Task<bool> ExisteRuta(int origenId, int destinoId);
        Task<int> CrearRuta(int origenId, int destinoId, int duracionEstimada);
    }
}
