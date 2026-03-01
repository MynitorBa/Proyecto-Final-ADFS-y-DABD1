using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class TripulacionService
    {
        private readonly TripulacionRepository _repository;

        public TripulacionService(TripulacionRepository repository)
        {
            _repository = repository;
        }

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

        public async Task<bool> Eliminar(int id)
        {
            return await _repository.Eliminar(id);
        }

        public async Task GuardarImagen(int tripulanteId, string imagenBase64)
        {
            await _repository.GuardarImagen(tripulanteId, imagenBase64);
        }

        public async Task EliminarImagen(int tripulanteId)
        {
            await _repository.EliminarImagen(tripulanteId);
        }

        public async Task<List<RolTripulacion>> ObtenerRoles()
        {
            return await _repository.ObtenerRoles();
        }
    }
}