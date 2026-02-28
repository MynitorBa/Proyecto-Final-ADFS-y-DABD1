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
            => await _repository.ObtenerTodos();

        public async Task<AeropuertoDTO?> ObtenerPorId(int id)
            => await _repository.ObtenerPorId(id);

        public async Task<List<DateTime>> ObtenerFechasDisponibles()
            => await _repository.ObtenerFechasConVuelos();

        public async Task<List<DateTime>> ObtenerFechasDisponiblesPorRuta(
            int? origenId,
            int? destinoId,
            int cantidadPersonas = 1,
            int? claseId = null)
            => await _repository.ObtenerFechasConVuelosPorRuta(origenId, destinoId, cantidadPersonas, claseId);

        public async Task<AeropuertoDTO> Crear(CrearAeropuertoDTO crearAeropuertoDTO)
        {
            var paisId = await _repository.ObtenerOCrearPais(crearAeropuertoDTO.Pais);
            var ciudadId = await _repository.ObtenerOCrearCiudad(crearAeropuertoDTO.Ciudad, paisId);

            var aeropuerto = new Aeropuerto
            {
                Nombre = crearAeropuertoDTO.Nombre,
                Codigo = crearAeropuertoDTO.Codigo.ToUpper(),
                CiudadId = ciudadId
            };

            var nuevoId = await _repository.Crear(aeropuerto);
            aeropuerto.Id = nuevoId;
            return await _repository.ObtenerPorId(nuevoId);
        }

        public async Task<bool> Actualizar(int id, CrearAeropuertoDTO actualizarAeropuertoDto)
        {
            var paisId = await _repository.ObtenerOCrearPais(actualizarAeropuertoDto.Pais);
            var ciudadId = await _repository.ObtenerOCrearCiudad(actualizarAeropuertoDto.Ciudad, paisId);

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