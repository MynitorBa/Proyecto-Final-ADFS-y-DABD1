using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de rutas para agencias. Provee acceso al catalogo completo de rutas
    /// disponibles que las agencias pueden usar al buscar y reservar vuelos.
    /// </summary>
    public class RutaAgenciaService
    {
        private readonly RutaAgenciaRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de rutas de agencia.
        /// </summary>
        public RutaAgenciaService(RutaAgenciaRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Retorna la lista completa de rutas disponibles en el sistema
        /// con los datos necesarios para que una agencia realice busquedas de vuelos.
        /// </summary>
        public async Task<List<RutaAgenciaDTO>> ObtenerTodasLasRutas()
        {
            return await _repository.ObtenerTodasLasRutas();
        }
    }
}
