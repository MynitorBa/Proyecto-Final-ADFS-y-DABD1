using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
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

        // POST api/comentarios/ruta
        [HttpPost("ruta")]
        [Authorize]
        public async Task<IActionResult> CrearComentarioRuta([FromBody] CrearComentarioRutaDTO dto)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var comentario = await _service.CrearComentarioRuta(usuarioId, dto);
                return Ok(comentario);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // POST api/comentarios/respuesta
        [HttpPost("respuesta")]
        [Authorize]
        public async Task<IActionResult> CrearRespuesta([FromBody] CrearRespuestaDTO dto)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var comentario = await _service.CrearRespuesta(usuarioId, dto);
                return Ok(comentario);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/comentarios/todos
        [HttpGet("todos")]
        [Authorize]
        public async Task<IActionResult> ObtenerTodosConVoto()
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var comentarios = await _service.ObtenerTodosConVoto(usuarioId);
                return Ok(comentarios);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/comentarios/usuario
        [HttpGet("usuario")]
        [Authorize]
        public async Task<IActionResult> ObtenerMisComentarios()
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var comentarios = await _service.ObtenerComentariosPorUsuario(usuarioId);
                return Ok(comentarios);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/comentarios/ruta/{rutaId}/con-voto
        [HttpGet("ruta/{rutaId}/con-voto")]
        [Authorize]
        public async Task<IActionResult> ObtenerComentariosRutaConVoto(int rutaId)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var comentarios = await _service.ObtenerComentariosRutaConVoto(rutaId, usuarioId);
                return Ok(comentarios);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/comentarios/ruta/{rutaId}
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

        // GET api/comentarios/agencia/ruta/{rutaId}
        [HttpGet("agencia/ruta/{rutaId}")]
        [ServiceFilter(typeof(AgenciaAuthMiddleware))]
        public async Task<IActionResult> ObtenerComentariosRutaAgencia(int rutaId)
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

        private int ObtenerUsuarioId()
        {
            int? id = SessionHelper.GetUsuarioId(HttpContext);
            if (id == null)
                throw new Exception("No se pudo obtener la sesión del usuario.");
            return id.Value;
        }
    }
}