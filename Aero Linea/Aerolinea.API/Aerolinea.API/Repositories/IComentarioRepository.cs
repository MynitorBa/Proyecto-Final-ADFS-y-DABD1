using Aerolinea.API.DTOs;

namespace Aerolinea.API.Repositories
{
    public interface IComentarioRepository
    {
        Task<ComentarioDTO> CrearComentarioRuta(int usuarioId, CrearComentarioRutaDTO dto);
        Task<ComentarioDTO> CrearRespuesta(int usuarioId, CrearRespuestaDTO dto);
        Task<List<ComentarioConVotoDTO>> ObtenerTodosConVoto(int usuarioId);
        Task<List<ComentarioDTO>> ObtenerComentariosPorUsuario(int usuarioId);
        Task<List<ComentarioConVotoDTO>> ObtenerComentariosRutaConVoto(int rutaId, int usuarioId);
        Task<List<ComentarioDTO>> ObtenerComentariosPorRuta(int rutaId);
    }
}
