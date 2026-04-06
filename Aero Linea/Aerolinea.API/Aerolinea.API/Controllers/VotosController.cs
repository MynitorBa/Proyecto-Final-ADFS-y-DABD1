using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de votos en comentarios. Permite a los usuarios autenticados emitir
    /// votos positivos o negativos en comentarios, quitar votos previamente emitidos y
    /// consultar su voto actual sobre un comentario especifico.
    /// Todos los endpoints requieren sesion activa.
    /// </summary>
    [ApiController]
    [Route("api/votos")]
    [Authorize] // Todas requieren sesión
    public class VotosController : ControllerBase
    {
        private readonly DownService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de votos.
        /// </summary>
        public VotosController(DownService service)
        {
            _service = service;
        }

        // POST api/votos
        // Body: { "comentarioId": 1, "valor": 1 }   (1 = upvote, -1 = downvote)
        /// <summary>
        /// Registra un voto positivo (1) o negativo (-1) del usuario autenticado sobre un comentario.
        /// Si el usuario ya habia votado el mismo valor, el voto se anula; si era distinto, se actualiza.
        /// </summary>
        [HttpPost]
        public async Task<IActionResult> VotarComentario([FromBody] VotarComentarioDTO dto)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var resultado = await _service.VotarComentario(usuarioId, dto);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // DELETE api/votos/{comentarioId}
        /// <summary>
        /// Elimina el voto del usuario autenticado sobre un comentario especifico.
        /// Retorna el nuevo conteo de votos del comentario tras la eliminacion.
        /// </summary>
        [HttpDelete("{comentarioId}")]
        public async Task<IActionResult> QuitarVoto(int comentarioId)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var resultado = await _service.QuitarVoto(usuarioId, comentarioId);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/votos/{comentarioId}
        /// <summary>
        /// Retorna el voto actual del usuario autenticado sobre un comentario especifico.
        /// El valor puede ser 1 (upvote), -1 (downvote) o null si no ha votado.
        /// </summary>
        [HttpGet("{comentarioId}")]
        public async Task<IActionResult> ObtenerVotoUsuario(int comentarioId)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var voto = await _service.ObtenerVotoUsuario(usuarioId, comentarioId);
                return Ok(new
                {
                    mensaje = voto == null ? "No ha votado" : voto == 1 ? "Upvote" : "Downvote",
                    valor = voto
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }


        private int ObtenerUsuarioId()
        {
            int? id = SessionHelper.GetUsuarioId(HttpContext);
            if (id == null)
                throw new Exception("No se pudo obtener la sesión del usuario.");
            return id.Value;
        }
    }
}
