using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class DownService
    {
        private readonly DownRepository _repository;

        public DownService(DownRepository repository)
        {
            _repository = repository;
        }

        public async Task<ResultadoVotoDTO> VotarComentario(int usuarioId, VotarComentarioDTO dto)
        {
            if (dto.Valor != 1 && dto.Valor != -1)
                throw new Exception("El valor del voto debe ser 1 (upvote) o -1 (downvote).");

            return await _repository.VotarComentario(usuarioId, dto);
        }

        public async Task<ResultadoVotoDTO> QuitarVoto(int usuarioId, int comentarioId)
        {
            return await _repository.QuitarVoto(usuarioId, comentarioId);
        }

        public async Task<int?> ObtenerVotoUsuario(int usuarioId, int comentarioId)
        {
            return await _repository.ObtenerVotoUsuario(usuarioId, comentarioId);
        }
    }
}