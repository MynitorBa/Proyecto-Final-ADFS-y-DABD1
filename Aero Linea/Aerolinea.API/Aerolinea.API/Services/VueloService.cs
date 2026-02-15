using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class VueloService
    {
        private readonly VueloRepository _repository;

        public VueloService(VueloRepository repository)
        {
            _repository = repository;
        }
            
        public async Task<List<VueloDetalleDTO>> BuscarVuelos(BuscarVueloDTO dto)
        {
            return await _repository.BuscarVuelos(dto.OrigenId, dto.DestinoId, dto.Fecha, dto.CantidadPasajeros);
        }
    }
}