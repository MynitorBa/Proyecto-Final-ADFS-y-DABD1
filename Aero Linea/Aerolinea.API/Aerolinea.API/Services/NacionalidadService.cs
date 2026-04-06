using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de nacionalidades. Expone la logica de negocio para consultar
    /// el catalogo de nacionalidades disponibles en el sistema.
    /// </summary>
    public class NacionalidadService
    {
        private readonly NacionalidadRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de nacionalidades.
        /// </summary>
        public NacionalidadService(NacionalidadRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Retorna la lista completa de nacionalidades disponibles mapeadas a su DTO,
        /// incluyendo ID y nombre de cada una.
        /// </summary>
        public async Task<List<NacionalidadDto>> ObtenerTodas()
        {
            var lista = await _repository.ObtenerTodas();
            return lista.Select(n => new NacionalidadDto { Id = n.Id, Nombre = n.Nombre }).ToList();
        }
    }
}
