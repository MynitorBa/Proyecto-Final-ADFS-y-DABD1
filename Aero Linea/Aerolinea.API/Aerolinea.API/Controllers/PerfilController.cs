using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de perfil de usuario. Permite al usuario autenticado consultar sus datos,
    /// actualizar su numero de telefono y cambiar su contrasena. Aplica verificacion de
    /// propiedad para garantizar que cada usuario solo pueda modificar su propio perfil.
    /// Todos los endpoints requieren sesion activa.
    /// </summary>
    [ApiController]
    [Route("api/perfil")]
    [Authorize]   // Todas las rutas de perfil requieren sesión activa
    public class PerfilController : ControllerBase
    {
        private readonly PerfilService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de perfil de usuario.
        /// </summary>
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

        /// <summary>
        /// Retorna los datos del perfil de un usuario especifico. Solo el propio usuario
        /// puede consultar su perfil; retorna 403 si el id de ruta no coincide con la sesion.
        /// </summary>
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

        /// <summary>
        /// Actualiza el numero de telefono del usuario especificado. Solo el propio usuario
        /// puede modificar su telefono; retorna 403 si el id de ruta no coincide con la sesion.
        /// </summary>
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

        /// <summary>
        /// Cambia la contrasena del usuario especificado tras validar la contrasena actual.
        /// Solo el propio usuario puede cambiar su contrasena; retorna 403 si el id de ruta
        /// no coincide con la sesion activa.
        /// </summary>
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

        /// <summary>
        /// Actualiza los datos personales del usuario: nombre, apellido, username, pasaporte,
        /// fecha de nacimiento, pais y ciudad. Solo el propio usuario puede modificar su perfil.
        /// </summary>
        [HttpPatch("{usuarioId}/datos-personales")]
        public async Task<IActionResult> ActualizarDatosPersonales(
            int usuarioId,
            [FromBody] ActualizarDatosPersonalesDTO dto)
        {
            if (!EsPropietario(usuarioId))
                return StatusCode(403, new { message = "Acceso denegado." });

            var (exito, mensaje) = await _service.ActualizarDatosPersonales(usuarioId, dto);
            return exito ? Ok(new { message = mensaje }) : BadRequest(new { message = mensaje });
        }

        /// <summary>
        /// Actualiza el correo electronico del usuario especificado tras validar formato y unicidad.
        /// Solo el propio usuario puede cambiar su correo; retorna 403 si el id de ruta no coincide.
        /// </summary>
        [HttpPatch("{usuarioId}/correo")]
        public async Task<IActionResult> ActualizarCorreo(
            int usuarioId,
            [FromBody] ActualizarCorreoDTO dto)
        {
            if (!EsPropietario(usuarioId))
                return StatusCode(403, new { message = "Acceso denegado." });

            var (exito, mensaje) = await _service.ActualizarCorreo(usuarioId, dto.NuevoCorreo);
            return exito ? Ok(new { message = mensaje }) : BadRequest(new { message = mensaje });
        }
    }
}
