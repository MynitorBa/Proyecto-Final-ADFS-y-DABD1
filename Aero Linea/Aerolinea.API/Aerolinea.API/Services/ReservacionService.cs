using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class ReservacionService
    {
        private readonly ReservacionRepository _repository;

        public ReservacionService(ReservacionRepository repository)
        {
            _repository = repository;
        }

        public async Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto, int? usuarioId)
        {
            if (usuarioId == null)
                throw new Exception("Debes iniciar sesión para crear una reservación.");

            return await _repository.CrearReservacion(usuarioId.Value, dto.Vuelos);
        }

        public async Task AgregarPasajeros(AgregarPasajerosDTO dto)
        {
            await _repository.AgregarPasajerosAReservacion(dto.ReservacionId, dto.Pasajeros);
        }
    }
}