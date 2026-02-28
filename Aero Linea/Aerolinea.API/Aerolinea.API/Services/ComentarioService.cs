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

        public async Task<ComentarioDTO> CrearComentarioRuta(int usuarioId, CrearComentarioRutaDTO dto)
        {
            if (dto.CantidadEstrellas < 1 || dto.CantidadEstrellas > 5)
                throw new Exception("La cantidad de estrellas debe estar entre 1 y 5.");

            if (string.IsNullOrWhiteSpace(dto.Contenido))
                throw new Exception("El contenido del comentario no puede estar vacío.");

            if (dto.Contenido.Length > 500)
                throw new Exception("El contenido no puede exceder 500 caracteres.");

            return await _repository.CrearComentarioRuta(usuarioId, dto);
        }

        public async Task<ComentarioDTO> CrearRespuesta(int usuarioId, CrearRespuestaDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Contenido))
                throw new Exception("El contenido de la respuesta no puede estar vacío.");

            if (dto.Contenido.Length > 500)
                throw new Exception("El contenido no puede exceder 500 caracteres.");

            return await _repository.CrearRespuesta(usuarioId, dto);
        }

        public async Task<List<ComentarioConVotoDTO>> ObtenerTodosConVoto(int usuarioId)
        {
            return await _repository.ObtenerTodosConVoto(usuarioId);
        }

        public async Task<List<ComentarioDTO>> ObtenerComentariosPorUsuario(int usuarioId)
        {
            return await _repository.ObtenerComentariosPorUsuario(usuarioId);
        }

        public async Task<List<ComentarioConVotoDTO>> ObtenerComentariosRutaConVoto(int rutaId, int usuarioId)
        {
            return await _repository.ObtenerComentariosRutaConVoto(rutaId, usuarioId);
        }

        public async Task<List<ComentarioDTO>> ObtenerComentariosPorRuta(int rutaId)
        {
            return await _repository.ObtenerComentariosPorRuta(rutaId);
        }
    }
}