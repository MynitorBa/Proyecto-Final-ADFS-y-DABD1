using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class ReservacionAgenciaService
    {
        private readonly ReservacionAgenciaRepository _repository;
        private readonly AgenciaRepository _agenciaRepository;

        public ReservacionAgenciaService(
            ReservacionAgenciaRepository repository,
            AgenciaRepository agenciaRepository)
        {
            _repository = repository;
            _agenciaRepository = agenciaRepository;
        }

        public async Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto, int agenciaId)
        {
            decimal descuento = await _agenciaRepository.ObtenerDescuento(agenciaId);
            return await _repository.CrearReservacion(dto.Vuelos, descuento, agenciaId);
        }

        public async Task ExpirarReservacion(int reservacionId, int agenciaId)
        {
            bool valida = await _repository.PerteneceAAgenciaYEstaPendiente(reservacionId, agenciaId);

            if (!valida)
                throw new Exception("La reservación no existe, no pertenece a esta agencia, o no está en estado pendiente.");

            await _repository.ExpirarReservacion(reservacionId);
        }
    }
}