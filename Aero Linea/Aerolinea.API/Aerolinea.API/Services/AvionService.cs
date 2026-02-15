using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AvionService
    {
        private readonly AvionRepository _repository;

        public AvionService(AvionRepository repository)
        {
            _repository = repository;
        }

        public async Task<List<AvionDTO>> ObtenerTodos()
        {
            var aviones = await _repository.ObtenerTodos();

            return aviones.Select(a => new AvionDTO
            {
                Id = a.Id,
                Marca = a.Marca,
                Modelo = a.Modelo,
                CapacidadPasajeros = a.CapacidadPasajeros,
                NombreCompleto = $"{a.Marca} {a.Modelo}"
            }).ToList();
        }

        public async Task<AvionDTO?> ObtenerPorId(int id)
        {
            var avion = await _repository.ObtenerPorId(id);

            if (avion == null)
                return null;

            return new AvionDTO
            {
                Id = avion.Id,
                Marca = avion.Marca,
                Modelo = avion.Modelo,
                CapacidadPasajeros = avion.CapacidadPasajeros,
                NombreCompleto = $"{avion.Marca} {avion.Modelo}"
            };
        }
    }
}