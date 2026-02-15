using Aerolinea.API.DTOs;
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
                // Obtener nombre del rol
                var nombreRol = await _repository.ObtenerNombreRol(tripulante.RolID);

                tripulantesDTO.Add(new TripulanteDTO
                {
                    Id = tripulante.Id,
                    Nombre = tripulante.Nombre,
                    Apellido = tripulante.Apellido,
                    RolID = tripulante.RolID,
                    NombreRol = nombreRol ?? "Desconocido",
                    NombreCompleto = $"{tripulante.Nombre} {tripulante.Apellido}"
                });
            }

            return tripulantesDTO;
        }

        public async Task<TripulanteDTO?> ObtenerPorId(int id)
        {
            var tripulante = await _repository.ObtenerPorId(id);
            
            if (tripulante == null)
                return null;

            // Obtener nombre del rol
            var nombreRol = await _repository.ObtenerNombreRol(tripulante.RolID);

            return new TripulanteDTO
            {
                Id = tripulante.Id,
                Nombre = tripulante.Nombre,
                Apellido = tripulante.Apellido,
                RolID = tripulante.RolID,
                NombreRol = nombreRol ?? "Desconocido",
                NombreCompleto = $"{tripulante.Nombre} {tripulante.Apellido}"
            };
        }
    }
}