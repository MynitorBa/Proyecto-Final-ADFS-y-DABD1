using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/comentarios")]
    public class ComentariosController : ControllerBase
    {
        private readonly ComentarioService _service;

        public ComentariosController(ComentarioService service)
        {
            _service = service;
        }

        [HttpPost]
        public async Task<IActionResult> CrearComentario([FromBody] CrearComentarioDTO dto)
        {
            try
            {
                var comentario = await _service.CrearComentario(dto);
                return Ok(comentario);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet("ruta/{rutaId}")]
        public async Task<IActionResult> ObtenerComentariosPorRuta(int rutaId)
        {
            try
            {
                var comentarios = await _service.ObtenerComentariosPorRuta(rutaId);
                return Ok(comentarios);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}