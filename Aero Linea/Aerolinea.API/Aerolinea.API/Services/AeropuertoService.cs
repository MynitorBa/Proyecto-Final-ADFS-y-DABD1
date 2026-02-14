using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AeropuertoService
    {
        private readonly AeropuertoRepository _repository;

        public AeropuertoService(AeropuertoRepository repository)
        {
            _repository = repository;
        }

        public async Task<List<AeropuertoDTO>> ObtenerAeropuertos()
        {
            return await _repository.ObtenerTodos();
        }

        public async Task<List<DateTime>> ObtenerFechasDisponibles()
        {
            return await _repository.ObtenerFechasConVuelos();
        }

        public async Task<List<DateTime>> ObtenerFechasDisponiblesPorRuta(int? origenId, int? destinoId)
        {
            return await _repository.ObtenerFechasConVuelosPorRuta(origenId, destinoId);
        }
    }
}