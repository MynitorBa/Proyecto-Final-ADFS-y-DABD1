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
            var lista = await _repository.ObtenerTodas();
            return lista.Select(n => new NacionalidadDto { Id = n.Id, Nombre = n.Nombre }).ToList();
        }
    }
}