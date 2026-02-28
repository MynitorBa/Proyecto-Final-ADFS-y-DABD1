using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/votos")]
    [Authorize] // Todas requieren sesión
    public class VotosController : ControllerBase
    {
        private readonly DownService _service;

        public VotosController(DownService service)
        {
            _service = service;
        }

        // POST api/votos
        // Body: { "comentarioId": 1, "valor": 1 }   (1 = upvote, -1 = downvote)
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