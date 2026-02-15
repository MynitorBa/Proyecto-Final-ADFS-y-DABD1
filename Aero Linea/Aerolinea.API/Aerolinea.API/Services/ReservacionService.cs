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

        public async Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto)
        {
            return await _repository.CrearReservacion(dto.UsuarioId, dto.Vuelos);
        }

        public async Task AgregarPasajeros(AgregarPasajerosDTO dto)
        {
            await _repository.AgregarPasajerosAReservacion(dto.ReservacionId, dto.Pasajeros);
        }

        public async Task ConfirmarReservacion(int reservacionId)
        {
            await _repository.ConfirmarReservacion(reservacionId);
        }
    }
}