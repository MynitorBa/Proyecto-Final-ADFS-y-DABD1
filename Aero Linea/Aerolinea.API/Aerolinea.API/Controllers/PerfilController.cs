using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/perfil")]
    [Authorize]   // Todas las rutas de perfil requieren sesión activa
    public class PerfilController : ControllerBase
    {
        private readonly PerfilService _service;

        public PerfilController(PerfilService service)
        {
            _service = service;
        }

        // Comprueba que el usuario de la sesión es el dueño del recurso
        private bool EsPropietario(int routeUsuarioId)
        {
            var sesionId = SessionHelper.GetUsuarioId(HttpContext);
            return sesionId.HasValue && sesionId.Value == routeUsuarioId;
        }

        [HttpGet("{usuarioId}")]
        public async Task<IActionResult> ObtenerPerfil(int usuarioId)
        {
            if (!EsPropietario(usuarioId))
                return StatusCode(403, new { message = "Acceso denegado." });

            var perfil = await _service.ObtenerPerfil(usuarioId);
            if (perfil == null)
                return NotFound(new { message = "Usuario no encontrado." });

            return Ok(perfil);
        }

        [HttpPatch("{usuarioId}/telefono")]
        public async Task<IActionResult> ActualizarTelefono(
            int usuarioId,
            [FromBody] ActualizarTelefonoDTO dto)
        {
            if (!EsPropietario(usuarioId))
                return StatusCode(403, new { message = "Acceso denegado." });

            var (exito, mensaje) = await _service.ActualizarTelefono(usuarioId, dto.Telefono);
            return exito ? Ok(new { message = mensaje }) : BadRequest(new { message = mensaje });
        }

        [HttpPatch("{usuarioId}/contrasena")]
        public async Task<IActionResult> CambiarContrasena(
            int usuarioId,
            [FromBody] CambiarContrasenaDTO dto)
        {
            if (!EsPropietario(usuarioId))
                return StatusCode(403, new { message = "Acceso denegado." });

            var (exito, mensaje) = await _service.CambiarContrasena(usuarioId, dto);
            return exito ? Ok(new { message = mensaje }) : BadRequest(new { message = mensaje });
        }
    }
}