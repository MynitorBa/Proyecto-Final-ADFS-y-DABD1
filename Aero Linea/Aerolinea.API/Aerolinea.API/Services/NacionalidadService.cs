using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class NacionalidadService
    {
        private readonly NacionalidadRepository _repository;

        public NacionalidadService(NacionalidadRepository repository)
        {
            _repository = repository;
        }

        public async Task<List<NacionalidadDto>> ObtenerTodas()
        {
            var nacionalidades = await _repository.ObtenerTodas();

            return nacionalidades.Select(n => new NacionalidadDto
            {
                Id = n.Id,
                Pais = n.Pais
            }).ToList();
        }
    }
}
