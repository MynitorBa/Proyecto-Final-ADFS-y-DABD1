using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de votos en comentarios. Gestiona la logica de negocio para emitir,
    /// quitar y consultar votos (upvote/downvote) de los usuarios sobre comentarios de rutas.
    /// </summary>
    public class DownService
    {
        private readonly DownRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de votos.
        /// </summary>
        public DownService(DownRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Registra o actualiza el voto de un usuario sobre un comentario.
        /// El valor del voto debe ser 1 para upvote o -1 para downvote.
        /// Retorna el nuevo conteo de votos positivos y negativos del comentario.
        /// </summary>
        public async Task<ResultadoVotoDTO> VotarComentario(int usuarioId, VotarComentarioDTO dto)
        {
            if (dto.Valor != 1 && dto.Valor != -1)
                throw new Exception("El valor del voto debe ser 1 (upvote) o -1 (downvote).");

            return await _repository.VotarComentario(usuarioId, dto);
        }

        /// <summary>
        /// Elimina el voto que el usuario habia emitido sobre un comentario especifico.
        /// Retorna el nuevo conteo de votos del comentario tras la eliminacion.
        /// </summary>
        public async Task<ResultadoVotoDTO> QuitarVoto(int usuarioId, int comentarioId)
        {
            return await _repository.QuitarVoto(usuarioId, comentarioId);
        }

        /// <summary>
        /// Retorna el valor del voto que el usuario ha emitido sobre un comentario (1, -1 o null).
        /// Retorna null si el usuario no ha votado en ese comentario.
        /// </summary>
        public async Task<int?> ObtenerVotoUsuario(int usuarioId, int comentarioId)
        {
            return await _repository.ObtenerVotoUsuario(usuarioId, comentarioId);
        }
    }
}
