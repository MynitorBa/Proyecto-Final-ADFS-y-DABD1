using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de aviones. Gestiona la logica de negocio para registrar, consultar,
    /// actualizar y eliminar aviones de la flota, incluyendo el manejo de imagenes.
    /// </summary>
    public class AvionService
    {
        private readonly AvionRepository _avionRepository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de aviones.
        /// </summary>
        public AvionService(AvionRepository avionRepository)
        {
            _avionRepository = avionRepository;
        }

        /// <summary>
        /// Retorna la lista completa de aviones registrados en el sistema,
        /// incluyendo marca, modelo, capacidad e imagen en Base64.
        /// </summary>
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

        /// <summary>
        /// Busca y retorna un avion por su identificador unico.
        /// Retorna null si el avion no existe en el sistema.
        /// </summary>
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

        /// <summary>
        /// Crea un nuevo avion en el sistema a partir del DTO recibido.
        /// Si se incluye imagen en Base64, la guarda de manera independiente en el repositorio.
        /// Retorna el DTO del avion recien creado con su ID asignado.
        /// </summary>
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

        /// <summary>
        /// Actualiza los datos de un avion existente. Si se proporciona una nueva imagen
        /// en Base64, tambien la actualiza en el repositorio.
        /// </summary>
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

        /// <summary>
        /// Elimina el avion con el identificador indicado del sistema.
        /// </summary>
        public async Task<bool> Eliminar(int id)
        {
            return await _avionRepository.Eliminar(id);
        }

        /// <summary>
        /// Guarda o reemplaza la imagen en formato Base64 asociada al avion indicado.
        /// </summary>
        public async Task GuardarImagen(int avionId, string imagenBase64)
        {
            await _avionRepository.GuardarImagen(avionId, imagenBase64);
        }

        /// <summary>
        /// Elimina la imagen asociada al avion indicado.
        /// </summary>
        public async Task EliminarImagen(int avionId)
        {
            await _avionRepository.EliminarImagen(avionId);
        }
    }
}
