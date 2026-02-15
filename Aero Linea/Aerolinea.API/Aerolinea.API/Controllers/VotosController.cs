using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/votos")]
    public class VotosController : ControllerBase
    {
        private readonly DownService _service;

        public VotosController(DownService service)
        {
            _service = service;
        }

        [HttpPost]
        public async Task<IActionResult> VotarComentario([FromBody] VotarComentarioDTO dto)
        {
            try
            {
                var resultado = await _service.VotarComentario(dto);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpDelete("{usuarioId}/{comentarioId}")]
        public async Task<IActionResult> QuitarVoto(int usuarioId, int comentarioId)
        {
            try
            {
                var resultado = await _service.QuitarVoto(usuarioId, comentarioId);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet("{usuarioId}/{comentarioId}")]
        public async Task<IActionResult> ObtenerVotoUsuario(int usuarioId, int comentarioId)
        {
            try
            {
                var voto = await _service.ObtenerVotoUsuario(usuarioId, comentarioId);

                if (voto == null)
                {
                    return Ok(new { mensaje = "No ha votado", valor = (int?)null });
                }

                return Ok(new
                {
                    mensaje = voto == 1 ? "Upvote" : "Downvote",
                    valor = voto
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}