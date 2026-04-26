using Aerolinea.API.DTOs;

namespace Aerolinea.API.Services
{
    public interface IReservacionService
    {
        Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto, int? usuarioId, string? ip, string? userAgent);
        Task AgregarPasajeros(AgregarPasajerosDTO dto, string? ip, string? userAgent);
    }
}