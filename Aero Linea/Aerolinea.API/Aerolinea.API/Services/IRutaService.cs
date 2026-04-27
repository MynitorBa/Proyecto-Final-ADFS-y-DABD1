using Aerolinea.API.Models.DTOs;

namespace Aerolinea.API.Services
{
    public interface IRutaService
    {
        Task<List<RutaDTO>> ObtenerTodas();
        Task<bool> ActualizarDuracion(int id, int duracionMinutos);
        Task<CalculoLlegadaResponseDTO> CalcularLlegada(CalculoLlegadaRequestDTO request);
        Task<bool> ExisteRuta(int origenId, int destinoId);
        Task<(bool creada, int rutaId, string mensaje)> CrearRuta(int origenId, int destinoId, int duracionEstimada);
        Task<(bool ok, string mensaje)> DesactivarRuta(int id);
        Task<(bool ok, string mensaje)> ActivarRuta(int id);
    }
}
