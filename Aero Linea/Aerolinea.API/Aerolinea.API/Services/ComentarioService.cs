using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de comentarios. Gestiona la logica de negocio para crear comentarios
    /// con calificacion sobre rutas, publicar respuestas a comentarios y consultar
    /// comentarios con informacion de votos por usuario.
    /// </summary>
    public class ComentarioService
    {
        private readonly ComentarioRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de comentarios.
        /// </summary>
        public ComentarioService(ComentarioRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Crea un comentario con calificacion de estrellas sobre una ruta especifica.
        /// Valida que la cantidad de estrellas este entre 1 y 5 y que el contenido
        /// no este vacio ni supere los 500 caracteres.
        /// </summary>
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

        /// <summary>
        /// Crea una respuesta a un comentario existente. Valida que el contenido
        /// no este vacio y no supere los 500 caracteres.
        /// </summary>
        public async Task<ComentarioDTO> CrearRespuesta(int usuarioId, CrearRespuestaDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Contenido))
                throw new Exception("El contenido de la respuesta no puede estar vacío.");

            if (dto.Contenido.Length > 500)
                throw new Exception("El contenido no puede exceder 500 caracteres.");

            return await _repository.CrearRespuesta(usuarioId, dto);
        }

        /// <summary>
        /// Retorna todos los comentarios del sistema incluyendo el voto que el usuario
        /// autenticado ha emitido sobre cada uno, si lo hay.
        /// </summary>
        public async Task<List<ComentarioConVotoDTO>> ObtenerTodosConVoto(int usuarioId)
        {
            return await _repository.ObtenerTodosConVoto(usuarioId);
        }

        /// <summary>
        /// Retorna todos los comentarios publicados por un usuario especifico.
        /// </summary>
        public async Task<List<ComentarioDTO>> ObtenerComentariosPorUsuario(int usuarioId)
        {
            return await _repository.ObtenerComentariosPorUsuario(usuarioId);
        }

        /// <summary>
        /// Retorna los comentarios de una ruta especifica incluyendo el estado del voto
        /// del usuario autenticado en cada comentario.
        /// </summary>
        public async Task<List<ComentarioConVotoDTO>> ObtenerComentariosRutaConVoto(int rutaId, int usuarioId)
        {
            return await _repository.ObtenerComentariosRutaConVoto(rutaId, usuarioId);
        }

        /// <summary>
        /// Retorna todos los comentarios asociados a una ruta especifica sin informacion de votos.
        /// </summary>
        public async Task<List<ComentarioDTO>> ObtenerComentariosPorRuta(int rutaId)
        {
            return await _repository.ObtenerComentariosPorRuta(rutaId);
        }
    }
}
