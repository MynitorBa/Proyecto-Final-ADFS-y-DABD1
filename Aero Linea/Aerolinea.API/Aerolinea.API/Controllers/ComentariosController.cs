using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de comentarios y respuestas sobre rutas. Permite a los usuarios autenticados
    /// publicar comentarios en rutas, responder comentarios existentes y consultar los comentarios
    /// con su estado de voto. Las agencias tambien pueden consultar comentarios de rutas.
    /// </summary>
    [ApiController]
    [Route("api/comentarios")]
    public class ComentariosController : ControllerBase
    {
        private readonly ComentarioService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de comentarios.
        /// </summary>
        public ComentariosController(ComentarioService service)
        {
            _service = service;
        }

        // POST api/comentarios/ruta
        /// <summary>
        /// Publica un nuevo comentario sobre una ruta especifica. Requiere sesion activa.
        /// El comentario queda vinculado al usuario autenticado y a la ruta indicada en el DTO.
        /// </summary>
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
        /// <summary>
        /// Publica una respuesta a un comentario existente. Requiere sesion activa.
        /// La respuesta queda vinculada al comentario padre indicado en el DTO.
        /// </summary>
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
        /// <summary>
        /// Retorna todos los comentarios del sistema incluyendo el estado de voto del usuario
        /// autenticado en cada comentario. Requiere sesion activa.
        /// </summary>
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
        /// <summary>
        /// Retorna los comentarios publicados por el usuario autenticado. Requiere sesion activa.
        /// </summary>
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
        /// <summary>
        /// Retorna los comentarios de una ruta especifica incluyendo el estado de voto del usuario
        /// autenticado en cada comentario. Requiere sesion activa.
        /// </summary>
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
        /// <summary>
        /// Retorna los comentarios de una ruta especifica. Endpoint publico, no requiere autenticacion.
        /// </summary>
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
        /// <summary>
        /// Retorna los comentarios de una ruta para consumo de agencias. Requiere autenticacion
        /// de agencia mediante AgenciaAuthMiddleware.
        /// </summary>
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
