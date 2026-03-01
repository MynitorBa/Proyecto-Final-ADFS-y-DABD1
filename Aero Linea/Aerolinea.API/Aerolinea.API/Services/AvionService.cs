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
                NombreCompleto = $"{a.Marca} {a.Modelo}",
                ImagenBase64 = a.ImagenBase64
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
                NombreCompleto = $"{avion.Marca} {avion.Modelo}",
                ImagenBase64 = avion.ImagenBase64
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

            // Si viene imagen, guardarla
            if (!string.IsNullOrEmpty(crearAvionDto.ImagenBase64))
            {
                await _avionRepository.GuardarImagen(nuevoId, crearAvionDto.ImagenBase64);
                avion.ImagenBase64 = crearAvionDto.ImagenBase64;
            }

            return new AvionDTO
            {
                Id = avion.Id,
                Marca = avion.Marca,
                Modelo = avion.Modelo,
                CapacidadPasajeros = avion.CapacidadPasajeros,
                NombreCompleto = $"{avion.Marca} {avion.Modelo}",
                ImagenBase64 = avion.ImagenBase64
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

            var resultado = await _avionRepository.Actualizar(avion);

            // Actualizar imagen si se proporcionó una nueva
            if (resultado && !string.IsNullOrEmpty(actualizarAvionDto.ImagenBase64))
            {
                await _avionRepository.GuardarImagen(id, actualizarAvionDto.ImagenBase64);
            }

            return resultado;
        }

        public async Task<bool> Eliminar(int id)
        {
            return await _avionRepository.Eliminar(id);
        }

        public async Task GuardarImagen(int avionId, string imagenBase64)
        {
            await _avionRepository.GuardarImagen(avionId, imagenBase64);
        }

        public async Task EliminarImagen(int avionId)
        {
            await _avionRepository.EliminarImagen(avionId);
        }
    }
}