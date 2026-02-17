using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/perfil")]
    public class PerfilController : ControllerBase
    {
        private readonly PerfilService _service;

        public PerfilController(PerfilService service)
        {
            _service = service;
        }
        private bool UsuarioAutorizado(int headerUsuarioId, int routeUsuarioId)
            => headerUsuarioId == routeUsuarioId;

        [HttpGet("{usuarioId}")]
        public async Task<IActionResult> ObtenerPerfil(
            int usuarioId,
            [FromHeader(Name = "X-UsuarioId")] int headerUsuarioId)
        {
            if (!UsuarioAutorizado(headerUsuarioId, usuarioId))
                return StatusCode(403, new { message = "Acceso denegado." });

            var perfil = await _service.ObtenerPerfil(usuarioId);
            if (perfil == null)
                return NotFound(new { message = "Usuario no encontrado." });

            return Ok(perfil);
        }

        [HttpPatch("{usuarioId}/telefono")]
        public async Task<IActionResult> ActualizarTelefono(
            int usuarioId,
            [FromHeader(Name = "X-UsuarioId")] int headerUsuarioId,
            [FromBody] ActualizarTelefonoDTO dto)
        {
            if (!UsuarioAutorizado(headerUsuarioId, usuarioId))
                return StatusCode(403, new { message = "Acceso denegado." });

            var (exito, mensaje) = await _service.ActualizarTelefono(usuarioId, dto.Telefono);
            return exito ? Ok(new { message = mensaje }) : BadRequest(new { message = mensaje });
        }

        [HttpPatch("{usuarioId}/contrasena")]
        public async Task<IActionResult> CambiarContrasena(
            int usuarioId,
            [FromHeader(Name = "X-UsuarioId")] int headerUsuarioId,
            [FromBody] CambiarContrasenaDTO dto)
        {
            if (!UsuarioAutorizado(headerUsuarioId, usuarioId))
                return StatusCode(403, new { message = "Acceso denegado." });

            var (exito, mensaje) = await _service.CambiarContrasena(usuarioId, dto);
            return exito ? Ok(new { message = mensaje }) : BadRequest(new { message = mensaje });
        }
    }
}