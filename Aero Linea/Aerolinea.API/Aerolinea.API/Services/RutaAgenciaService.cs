using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class RutaAgenciaService
    {
        private readonly RutaAgenciaRepository _repository;

        public RutaAgenciaService(RutaAgenciaRepository repository)
        {
            _repository = repository;
        }

        public async Task<List<RutaAgenciaDTO>> ObtenerTodasLasRutas()
        {
            return await _repository.ObtenerTodasLasRutas();
        }
    }
}