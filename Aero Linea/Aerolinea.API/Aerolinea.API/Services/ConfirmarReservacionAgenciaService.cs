using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class ConfirmarReservacionAgenciaService
    {
        private readonly ConfirmarReservacionAgenciaRepository _repository;

        public ConfirmarReservacionAgenciaService(ConfirmarReservacionAgenciaRepository repository)
        {
            _repository = repository;
        }

        public async Task<ConfirmacionAgenciaDTO> ConfirmarReservacion(
            int reservacionId,
            int agenciaId,
            ConfirmarReservacionAgenciaDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.NIT))
                throw new Exception("El NIT es requerido. Si no tienes, ingresa 'CF'.");

            if (string.IsNullOrWhiteSpace(dto.CodigoPostal))
                throw new Exception("El código postal es requerido.");

            return await _repository.ConfirmarReservacion(reservacionId, agenciaId, dto);
        }
    }
}