using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
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

        public async Task<AeropuertoDTO?> ObtenerPorId(int id)
        {
            return await _repository.ObtenerPorId(id);
        }

        public async Task<List<DateTime>> ObtenerFechasDisponibles()
        {
            return await _repository.ObtenerFechasConVuelos();
        }

        public async Task<List<DateTime>> ObtenerFechasDisponiblesPorRuta(int? origenId, int? destinoId)
        {
            return await _repository.ObtenerFechasConVuelosPorRuta(origenId, destinoId);
        }

        public async Task<AeropuertoDTO> Crear(CrearAeropuertoDTO crearAeropuertoDTO)
        {
            // 1. Obtener o crear el país
            var paisId = await _repository.ObtenerOCrearPais(crearAeropuertoDTO.Pais);

            // 2. Obtener o crear la ciudad en ese país
            var ciudadId = await _repository.ObtenerOCrearCiudad(crearAeropuertoDTO.Ciudad, paisId);

            // 3. Crear el aeropuerto con el ciudadId obtenido
            var aeropuerto = new Aeropuerto
            {
                Nombre = crearAeropuertoDTO.Nombre,
                Codigo = crearAeropuertoDTO.Codigo.ToUpper(),
                CiudadId = ciudadId
            };

            var nuevoId = await _repository.Crear(aeropuerto);
            aeropuerto.Id = nuevoId;

            // Obtener el aeropuerto completo con la información de ciudad y país
            return await _repository.ObtenerPorId(nuevoId);
        }

        public async Task<bool> Actualizar(int id, CrearAeropuertoDTO actualizarAeropuertoDto)
        {
            // 1. Obtener o crear el país
            var paisId = await _repository.ObtenerOCrearPais(actualizarAeropuertoDto.Pais);

            // 2. Obtener o crear la ciudad en ese país
            var ciudadId = await _repository.ObtenerOCrearCiudad(actualizarAeropuertoDto.Ciudad, paisId);

            // 3. Actualizar el aeropuerto con el ciudadId obtenido
            var aeropuerto = new Aeropuerto
            {
                Id = id,
                Nombre = actualizarAeropuertoDto.Nombre,
                Codigo = actualizarAeropuertoDto.Codigo.ToUpper(),
                CiudadId = ciudadId
            };

            return await _repository.Actualizar(aeropuerto);
        }
    }
}