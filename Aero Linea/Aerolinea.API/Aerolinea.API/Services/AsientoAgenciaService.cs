using Aerolinea.API.DTOs.Agencia;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AsientoAgenciaService
    {
        private readonly AsientoAgenciaRepository _repository;

        public AsientoAgenciaService(AsientoAgenciaRepository repository)
        {
            _repository = repository;
        }

        public async Task<List<AsientosVueloAgenciaDTO>> ObtenerAsientosPorReservacion(int reservacionId, int agenciaId)
        {
            return await _repository.ObtenerAsientosPorReservacion(reservacionId, agenciaId);
        }

        public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int agenciaId)
        {
            if (string.IsNullOrWhiteSpace(nuevoAsiento))
                throw new Exception("El asiento es obligatorio.");

            await _repository.CambiarAsiento(boletoId, nuevoAsiento.Trim().ToUpper(), agenciaId);
        }
    }
}