using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de aeropuertos. Gestiona la logica de negocio para consultar, crear,
    /// actualizar y eliminar aeropuertos, incluyendo manejo de imagenes y fechas disponibles.
    /// </summary>
    public class AeropuertoService
    {
        private readonly AeropuertoRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de aeropuertos.
        /// </summary>
        public AeropuertoService(AeropuertoRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Retorna la lista completa de aeropuertos registrados en el sistema.
        /// </summary>
        public async Task<List<AeropuertoDTO>> ObtenerAeropuertos()
            => await _repository.ObtenerTodos();

        /// <summary>
        /// Busca y retorna un aeropuerto por su identificador unico.
        /// Retorna null si no existe.
        /// </summary>
        public async Task<AeropuertoDTO?> ObtenerPorId(int id)
            => await _repository.ObtenerPorId(id);

        /// <summary>
        /// Retorna todas las fechas para las que existe al menos un vuelo programado,
        /// sin importar la ruta.
        /// </summary>
        public async Task<List<DateTime>> ObtenerFechasDisponibles()
            => await _repository.ObtenerFechasConVuelos();

        /// <summary>
        /// Retorna las fechas disponibles con vuelos para una ruta especifica definida por
        /// aeropuerto de origen, destino, cantidad de personas y clase de vuelo.
        /// </summary>
        public async Task<List<DateTime>> ObtenerFechasDisponiblesPorRuta(
            int? origenId,
            int? destinoId,
            int cantidadPersonas = 1,
            int? claseId = null)
            => await _repository.ObtenerFechasConVuelosPorRuta(origenId, destinoId, cantidadPersonas, claseId);

        /// <summary>
        /// Crea un nuevo aeropuerto a partir del DTO recibido. Resuelve o crea el pais,
        /// ciudad y zona horaria correspondientes. Si ya existe un aeropuerto con el mismo
        /// codigo IATA, lo actualiza en lugar de crear uno nuevo para evitar duplicados.
        /// </summary>
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

        /// <summary>
        /// Actualiza los datos de un aeropuerto existente. Verifica que no existan duplicados
        /// de nombre o codigo IATA con otros aeropuertos, y resuelve la zona horaria indicada.
        /// Si se proporciona imagen nueva, la guarda junto con los demas cambios.
        /// </summary>
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

        /// <summary>
        /// Elimina el aeropuerto con el identificador indicado del sistema.
        /// </summary>
        public async Task<bool> Eliminar(int id)
            => await _repository.Eliminar(id);

        /// <summary>
        /// Guarda o reemplaza la imagen en formato Base64 asociada al aeropuerto indicado.
        /// </summary>
        public async Task GuardarImagen(int aeropuertoId, string imagenBase64)
            => await _repository.GuardarImagen(aeropuertoId, imagenBase64);

        /// <summary>
        /// Elimina la imagen asociada al aeropuerto indicado.
        /// </summary>
        public async Task EliminarImagen(int aeropuertoId)
            => await _repository.EliminarImagen(aeropuertoId);
    }
}
