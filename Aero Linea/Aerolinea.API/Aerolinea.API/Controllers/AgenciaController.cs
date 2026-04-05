using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/agencias")]
    public class AgenciaController : ControllerBase
    {
        private readonly AgenciaService _service;

        public AgenciaController(AgenciaService service)
        {
            _service = service;
        }

        // ── Admin: crea agencia para cualquier usuario Webservice ─────────────
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<IActionResult> CrearAgencia([FromBody] CrearAgenciaDTO dto)
        {
            try
            {
                var agencia = await _service.CrearAgencia(dto);
                return Ok(new { message = "Agencia creada correctamente", agencia });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Admin: listar todas las agencias ──────────────────────────────────
        [Authorize(Roles = "Administrador")]
        [HttpGet("todas")]
        public async Task<IActionResult> ObtenerTodasAdmin()
        {
            var agencias = await _service.ObtenerTodasAdmin();
            return Ok(agencias);
        }

        // ── Admin: usuarios Webservice sin agencia (para selector) ────────────
        [Authorize(Roles = "Administrador")]
        [HttpGet("webservice-disponibles")]
        public async Task<IActionResult> ObtenerWebserviceDisponibles()
        {
            var usuarios = await _service.ObtenerWebserviceSinAgencia();
            return Ok(usuarios);
        }

        // ── Admin: asignar usuario Webservice a una agencia ───────────────────
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id:int}/asignar-usuario")]
        public async Task<IActionResult> AsignarUsuario(int id, [FromBody] AsignarUsuarioAgenciaDTO dto)
        {
            try
            {
                await _service.AsignarUsuario(id, dto.UsuarioWebId);
                return Ok(new { message = "Usuario asignado correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Admin: actualizar descuento ───────────────────────────────────────
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id:int}/descuento")]
        public async Task<IActionResult> ActualizarDescuento(int id, [FromBody] ActualizarDescuentoDTO dto)
        {
            try
            {
                await _service.ActualizarDescuento(id, dto.Descuento);
                return Ok(new { message = "Descuento actualizado correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Admin: actualizar estado ──────────────────────────────────────────
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id:int}/estado")]
        public async Task<IActionResult> ActualizarEstado(int id, [FromBody] ActualizarEstadoAgenciaDTO dto)
        {
            try
            {
                await _service.ActualizarEstado(id, dto.EstadoId);
                return Ok(new { message = "Estado actualizado correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Webservice: consulta su propia agencia ────────────────────────────
        [Authorize]
        [HttpGet("mi-agencia")]
        public async Task<IActionResult> ObtenerMiAgencia()
        {
            var rolId = SessionHelper.GetRolId(HttpContext);
            if (rolId != 3)
                return StatusCode(403, new { message = "Acceso restringido a usuarios Webservice." });

            var usuarioId = SessionHelper.GetUsuarioId(HttpContext);
            if (usuarioId == null)
                return Unauthorized(new { message = "Sesión no válida." });

            var agencia = await _service.ObtenerMiAgencia(usuarioId.Value);

            if (agencia == null)
                return Ok(new { tieneAgencia = false, agencia = (object?)null });

            return Ok(new { tieneAgencia = true, agencia });
        }

        // ── Webservice: registra su propia agencia (solo una vez) ─────────────
        [Authorize]
        [HttpPost("mi-agencia")]
        public async Task<IActionResult> CrearMiAgencia([FromBody] CrearAgenciaWebserviceDTO dto)
        {
            var rolId = SessionHelper.GetRolId(HttpContext);
            if (rolId != 3)
                return StatusCode(403, new { message = "Acceso restringido a usuarios Webservice." });

            var usuarioId = SessionHelper.GetUsuarioId(HttpContext);
            if (usuarioId == null)
                return Unauthorized(new { message = "Sesión no válida." });

            try
            {
                var agencia = await _service.CrearAgenciaWebservice(usuarioId.Value, dto);
                return Ok(new { message = "Agencia creada correctamente.", agencia });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}