using Aerolinea.API.DTOs;

namespace Aerolinea.API.Services
{
    public interface IReservacionService
    {
        Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto, int? usuarioId);
        Task AgregarPasajeros(AgregarPasajerosDTO dto);
    }
}
