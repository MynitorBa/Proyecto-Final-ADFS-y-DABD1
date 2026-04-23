using Aerolinea.API.DTOs;

namespace Aerolinea.API.Repositories
{
    public interface IReservacionRepository
    {
        Task<ReservacionCreadaDTO> CrearReservacion(int? usuarioId, List<SeleccionVueloDTO> vuelos);
        Task AgregarPasajerosAReservacion(int reservacionId, List<DatosPasajeroDTO> pasajeros);
    }
}
