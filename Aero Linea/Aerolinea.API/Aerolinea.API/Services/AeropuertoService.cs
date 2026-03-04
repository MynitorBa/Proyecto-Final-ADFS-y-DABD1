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

        public async Task<AeropuertoDTO?> Crear(CrearAeropuertoDTO crearAeropuertoDTO)
        {
            var paisId = await _repository.ObtenerOCrearPais(crearAeropuertoDTO.Pais);
            var ciudadId = await _repository.ObtenerOCrearCiudad(crearAeropuertoDTO.Ciudad, paisId);

            // Resolver FK de zona horaria: buscar o crear en la tabla ZonaHoraria
            var zonaHorariaId = await _repository.ObtenerOCrearZonaHoraria(crearAeropuertoDTO.ZonaHoraria);

            var codigoUpper = crearAeropuertoDTO.Codigo.ToUpper().Trim();

            // Verificar duplicado de nombre (el código IATA duplicado se maneja como upsert)
            var campoDuplicado = await _repository.VerificarDuplicado(
                crearAeropuertoDTO.Nombre, codigoUpper);
            if (campoDuplicado == "nombre")
                throw new InvalidOperationException(
                    $"Ya existe un aeropuerto con el nombre '{crearAeropuertoDTO.Nombre}'. " +
                    "Usa un nombre diferente o edita el aeropuerto existente.");

            // Si ya existe un aeropuerto con ese código IATA, actualizarlo en lugar de crear uno nuevo
            // Esto evita la violación del UNIQUE KEY constraint en Aeropuerto.Codigo
            var idExistente = await _repository.ObtenerIdPorCodigo(codigoUpper);
            if (idExistente.HasValue)
            {
                var aeroExistente = new Aeropuerto
                {
                    Id = idExistente.Value,
                    Nombre = crearAeropuertoDTO.Nombre,
                    Codigo = codigoUpper,
                    CiudadId = ciudadId,
                    ZonaHorariaId = zonaHorariaId
                };
                await _repository.Actualizar(aeroExistente);

                if (!string.IsNullOrEmpty(crearAeropuertoDTO.ImagenBase64))
                    await _repository.GuardarImagen(idExistente.Value, crearAeropuertoDTO.ImagenBase64);

                return await _repository.ObtenerPorId(idExistente.Value);
            }

            // No existe — crear nuevo
            var aeropuerto = new Aeropuerto
            {
                Nombre = crearAeropuertoDTO.Nombre,
                Codigo = codigoUpper,
                CiudadId = ciudadId,
                ZonaHorariaId = zonaHorariaId
            };

            var nuevoId = await _repository.Crear(aeropuerto);

            if (!string.IsNullOrEmpty(crearAeropuertoDTO.ImagenBase64))
                await _repository.GuardarImagen(nuevoId, crearAeropuertoDTO.ImagenBase64);

            return await _repository.ObtenerPorId(nuevoId);
        }

        public async Task<bool> Actualizar(int id, CrearAeropuertoDTO actualizarAeropuertoDto)
        {
            var paisId = await _repository.ObtenerOCrearPais(actualizarAeropuertoDto.Pais);
            var ciudadId = await _repository.ObtenerOCrearCiudad(actualizarAeropuertoDto.Ciudad, paisId);

            // Verificar duplicados excluyendo el propio aeropuerto
            var campoDuplicado = await _repository.VerificarDuplicado(
                actualizarAeropuertoDto.Nombre, actualizarAeropuertoDto.Codigo, excludeId: id);
            if (campoDuplicado == "nombre")
                throw new InvalidOperationException(
                    $"Ya existe otro aeropuerto con el nombre '{actualizarAeropuertoDto.Nombre}'.");
            if (campoDuplicado == "codigo")
                throw new InvalidOperationException(
                    $"Ya existe otro aeropuerto con el código IATA '{actualizarAeropuertoDto.Codigo.ToUpper()}'.");

            // Si envía zona horaria la resolvemos; si envía null/vacío → se borra la FK (null)
            var zonaHorariaId = await _repository.ObtenerOCrearZonaHoraria(actualizarAeropuertoDto.ZonaHoraria);

            var aeropuerto = new Aeropuerto
            {
                Id = id,
                Nombre = actualizarAeropuertoDto.Nombre,
                Codigo = actualizarAeropuertoDto.Codigo.ToUpper(),
                CiudadId = ciudadId,
                ZonaHorariaId = zonaHorariaId
            };

            var resultado = await _repository.Actualizar(aeropuerto);

            if (resultado && !string.IsNullOrEmpty(actualizarAeropuertoDto.ImagenBase64))
                await _repository.GuardarImagen(id, actualizarAeropuertoDto.ImagenBase64);

            return resultado;
        }

        public async Task<bool> Eliminar(int id)
            => await _repository.Eliminar(id);

        public async Task GuardarImagen(int aeropuertoId, string imagenBase64)
            => await _repository.GuardarImagen(aeropuertoId, imagenBase64);

        public async Task EliminarImagen(int aeropuertoId)
            => await _repository.EliminarImagen(aeropuertoId);
    }
}