using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de agencias de viaje. Expone endpoints para que el administrador gestione
    /// agencias y para que usuarios Webservice consulten y registren su propia agencia.
    /// </summary>
    [ApiController]
    [Route("api/agencias")]
    public class AgenciaController : ControllerBase
    {
        private readonly AgenciaService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de agencias.
        /// </summary>
        public AgenciaController(AgenciaService service)
        {
            _service = service;
        }

        // ── Admin: crea agencia para cualquier usuario Webservice ─────────────
        /// <summary>
        /// Crea una nueva agencia vinculada a un usuario Webservice. Requiere rol Administrador.
        /// Verifica que el usuario no tenga ya una agencia ni un hotel aliado asignados.
        /// </summary>
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
        /// <summary>
        /// Retorna la lista completa de agencias registradas en el sistema. Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet("todas")]
        public async Task<IActionResult> ObtenerTodasAdmin()
        {
            var agencias = await _service.ObtenerTodasAdmin();
            return Ok(agencias);
        }

        // ── Admin: usuarios Webservice libres (sin agencia ni hotel) ──────────
        /// <summary>
        /// Retorna los usuarios con rol Webservice que no tienen ninguna entidad asignada
        /// (ni agencia ni hotel aliado). Se usa para poblar los selectores de asignacion
        /// tanto en el panel de agencias como en el de hoteles.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet("webservice-disponibles")]
        public async Task<IActionResult> ObtenerWebserviceDisponibles()
        {
            var usuarios = await _service.ObtenerWebserviceSinAgencia();
            return Ok(usuarios);
        }

        // ── Admin: asignar usuario Webservice a una agencia ───────────────────
        /// <summary>
        /// Asigna un usuario Webservice a una agencia existente. Requiere rol Administrador.
        /// El usuario no puede tener ya una agencia ni un hotel aliado registrado.
        /// </summary>
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
        /// <summary>
        /// Actualiza el porcentaje de descuento aplicado a las reservaciones de una agencia.
        /// Requiere rol Administrador.
        /// </summary>
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
        /// <summary>
        /// Actualiza el estado de una agencia (activa, suspendida, etc.). Requiere rol Administrador.
        /// </summary>
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

        // ── Admin: actualizar URL de la agencia ───────────────────────────────
        /// <summary>
        /// Actualiza la URL publica de una agencia desde el panel de administracion.
        /// Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id:int}/url")]
        public async Task<IActionResult> ActualizarUrl(int id, [FromBody] ActualizarUrlAgenciaDTO dto)
        {
            try
            {
                await _service.ActualizarUrl(id, dto.UrlAgencia);
                return Ok(new { message = "URL actualizada correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Webservice: consulta su propia agencia ────────────────────────────
        /// <summary>
        /// Retorna los datos de la agencia asociada al usuario Webservice autenticado.
        /// Si el usuario no tiene agencia registrada, retorna tieneAgencia = false.
        /// Solo accesible para usuarios con rol Webservice (rolId = 3).
        /// </summary>
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
        /// <summary>
        /// Permite a un usuario Webservice autenticado crear su propia agencia por primera vez.
        /// Solo accesible para usuarios con rol Webservice (rolId = 3).
        /// </summary>
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