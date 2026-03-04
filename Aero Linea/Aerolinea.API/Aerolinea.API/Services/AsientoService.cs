using Aerolinea.API.Controllers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AsientoService
    {
        private readonly AsientoRepository _repository;

        public AsientoService(AsientoRepository repository)
        {
            _repository = repository;
        }

        public async Task<AsientosVueloDTO> ObtenerAsientosVuelo(int vueloId, int usuarioId)
        {
            return await _repository.ObtenerAsientosVuelo(vueloId, usuarioId);
        }

        public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int usuarioId)
        {
            if (string.IsNullOrWhiteSpace(nuevoAsiento))
                throw new Exception("El asiento no puede estar vacío.");

            await _repository.CambiarAsiento(boletoId, nuevoAsiento.Trim().ToUpper(), usuarioId);
        }
    }
}