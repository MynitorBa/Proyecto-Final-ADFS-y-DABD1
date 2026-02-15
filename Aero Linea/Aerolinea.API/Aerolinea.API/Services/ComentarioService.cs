using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class ComentarioService
    {
        private readonly ComentarioRepository _repository;

        public ComentarioService(ComentarioRepository repository)
        {
            _repository = repository;
        }

        public async Task<ComentarioDTO> CrearComentario(CrearComentarioDTO dto)
        {
            // Validar estrellas
            if (dto.CantidadEstrellas < 1 || dto.CantidadEstrellas > 5)
            {
                throw new Exception("La cantidad de estrellas debe estar entre 1 y 5.");
            }

            // Validar contenido
            if (string.IsNullOrWhiteSpace(dto.Contenido))
            {
                throw new Exception("El contenido del comentario no puede estar vacío.");
            }

            if (dto.Contenido.Length > 500)
            {
                throw new Exception("El contenido del comentario no puede exceder 500 caracteres.");
            }

            return await _repository.CrearComentario(dto);
        }

        public async Task<List<ComentarioDTO>> ObtenerComentariosPorRuta(int rutaId)
        {
            return await _repository.ObtenerComentariosPorRuta(rutaId);
        }
    }
}