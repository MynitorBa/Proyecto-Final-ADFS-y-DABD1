using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AvionService
    {
        private readonly AvionRepository _avionRepository;

        public AvionService(AvionRepository avionRepository)
        {
            _avionRepository = avionRepository;
        }

        public async Task<List<AvionDTO>> ObtenerTodos()
        {
            var aviones = await _avionRepository.ObtenerTodos();

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
            var avion = await _avionRepository.ObtenerPorId(id);

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

        public async Task<AvionDTO> Crear(CrearAvionDTO crearAvionDto)
        {
            var avion = new Avion
            {
                Marca = crearAvionDto.Marca,
                Modelo = crearAvionDto.Modelo,
                CapacidadPasajeros = crearAvionDto.CapacidadPasajeros
            };

            var nuevoId = await _avionRepository.Crear(avion);
            avion.Id = nuevoId;

            return new AvionDTO
            {
                Id = avion.Id,
                Marca = avion.Marca,
                Modelo = avion.Modelo,
                CapacidadPasajeros = avion.CapacidadPasajeros,
                NombreCompleto = $"{avion.Marca} {avion.Modelo}"
            };
        }

        public async Task<bool> Actualizar(int id, CrearAvionDTO actualizarAvionDto)
        {
            var avion = new Avion
            {
                Id = id,
                Marca = actualizarAvionDto.Marca,
                Modelo = actualizarAvionDto.Modelo,
                CapacidadPasajeros = actualizarAvionDto.CapacidadPasajeros
            };

            return await _avionRepository.Actualizar(avion);
        }

      
    }
}