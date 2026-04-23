using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de tripulacion. Gestiona la logica de negocio para registrar, consultar,
    /// actualizar y eliminar tripulantes de vuelo, incluyendo la asignacion de roles,
    /// manejo de imagenes y consulta del catalogo de roles disponibles.
    /// </summary>
    public class TripulacionService : ITripulacionService
    {
        private readonly ITripulacionRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de tripulacion.
        /// </summary>
        public TripulacionService(ITripulacionRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Retorna la lista completa de tripulantes registrados en el sistema.
        /// Por cada tripulante resuelve el nombre del rol mediante una consulta adicional
        /// al repositorio y construye el DTO con el nombre completo concatenado.
        /// </summary>
        public async Task<List<TripulanteDTO>> ObtenerTodos()
        {
            var tripulantes = await _repository.ObtenerTodos();
            var tripulantesDTO = new List<TripulanteDTO>();

            foreach (var tripulante in tripulantes)
            {
                var nombreRol = await _repository.ObtenerNombreRol(tripulante.RolID);

                tripulantesDTO.Add(new TripulanteDTO
                {
                    Id = tripulante.Id,
                    Nombre = tripulante.Nombre,
                    Apellido = tripulante.Apellido,
                    RolID = tripulante.RolID,
                    NombreRol = nombreRol ?? "Desconocido",
                    NombreCompleto = $"{tripulante.Nombre} {tripulante.Apellido}",
                    ImagenBase64 = tripulante.ImagenBase64
                });
            }

            return tripulantesDTO;
        }

        /// <summary>
        /// Busca y retorna los datos de un tripulante especifico por su ID,
        /// incluyendo el nombre del rol resuelto desde el repositorio.
        /// Retorna null si el tripulante no existe.
        /// </summary>
        public async Task<TripulanteDTO?> ObtenerPorId(int id)
        {
            var tripulante = await _repository.ObtenerPorId(id);

            if (tripulante == null)
                return null;

            var nombreRol = await _repository.ObtenerNombreRol(tripulante.RolID);

            return new TripulanteDTO
            {
                Id = tripulante.Id,
                Nombre = tripulante.Nombre,
                Apellido = tripulante.Apellido,
                RolID = tripulante.RolID,
                NombreRol = nombreRol ?? "Desconocido",
                NombreCompleto = $"{tripulante.Nombre} {tripulante.Apellido}",
                ImagenBase64 = tripulante.ImagenBase64
            };
        }

        /// <summary>
        /// Crea un nuevo tripulante a partir del DTO recibido y retorna su DTO con el ID asignado.
        /// </summary>
        public async Task<TripulanteDTO> Crear(CrearTripulanteDTO crearTripulanteDTO)
        {
            var tripulante = new Tripulante
            {
                Nombre = crearTripulanteDTO.Nombre,
                Apellido = crearTripulanteDTO.Apellido,
                RolID = crearTripulanteDTO.RolID,
                ImagenBase64 = crearTripulanteDTO.ImagenBase64
            };

            var nuevoId = await _repository.Crear(tripulante);
            tripulante.Id = nuevoId;

            return new TripulanteDTO
            {
                Id = tripulante.Id,
                Nombre = tripulante.Nombre,
                Apellido = tripulante.Apellido,
                RolID = tripulante.RolID,
                NombreCompleto = $"{tripulante.Nombre} {tripulante.Apellido}",
                ImagenBase64 = tripulante.ImagenBase64
            };
        }

        /// <summary>
        /// Actualiza los datos de un tripulante existente usando el ID y el DTO proporcionados.
        /// </summary>
        public async Task<bool> Actualizar(int id, CrearTripulanteDTO actualizarTripulanteDto)
        {
            var tripulante = new Tripulante
            {
                Id = id,
                Nombre = actualizarTripulanteDto.Nombre,
                Apellido = actualizarTripulanteDto.Apellido,
                RolID = actualizarTripulanteDto.RolID,
                ImagenBase64 = actualizarTripulanteDto.ImagenBase64
            };

            return await _repository.Actualizar(tripulante);
        }

        /// <summary>
        /// Elimina el tripulante con el identificador indicado del sistema.
        /// </summary>
        public async Task<bool> Eliminar(int id)
        {
            return await _repository.Eliminar(id);
        }

        /// <summary>
        /// Guarda o reemplaza la imagen en formato Base64 del tripulante indicado.
        /// </summary>
        public async Task GuardarImagen(int tripulanteId, string imagenBase64)
        {
            await _repository.GuardarImagen(tripulanteId, imagenBase64);
        }

        /// <summary>
        /// Elimina la imagen asociada al tripulante indicado.
        /// </summary>
        public async Task EliminarImagen(int tripulanteId)
        {
            await _repository.EliminarImagen(tripulanteId);
        }

        /// <summary>
        /// Retorna el catalogo completo de roles de tripulacion disponibles en el sistema,
        /// como piloto, copiloto, auxiliar de vuelo, etc.
        /// </summary>
        public async Task<List<RolTripulacion>> ObtenerRoles()
        {
            return await _repository.ObtenerRoles();
        }
    }
}
