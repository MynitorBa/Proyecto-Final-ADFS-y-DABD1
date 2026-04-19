using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador REST para la gestion administrativa de reservaciones.
    /// Todos los endpoints requieren autenticacion y rol Administrador.
    /// </summary>
    [ApiController]
    [Route("api/admin/reservaciones")]
    [Authorize(Roles = "Administrador")]
    public class AdminReservacionesController : ControllerBase
    {
        private readonly AdminReservacionesService _svc;
        private readonly ILogger<AdminReservacionesController> _logger;

        public AdminReservacionesController(
            AdminReservacionesService svc,
            ILogger<AdminReservacionesController> logger)
        {
            _svc = svc;
            _logger = logger;
        }

        // GET /api/admin/reservaciones/vuelos
        /// <summary>
        /// Retorna todos los vuelos que tienen al menos una reservacion con conteos
        /// por estado. Usado para la vista agrupada del panel administrativo.
        /// </summary>
        [HttpGet("vuelos")]
        public async Task<IActionResult> ObtenerVuelos()
        {
            try
            {
                var vuelos = await _svc.ObtenerVuelosConReservacionesAsync();
                return Ok(vuelos);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al obtener vuelos con reservaciones (admin)");
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // GET /api/admin/reservaciones/vuelo/{vueloId}
        /// <summary>
        /// Retorna las reservaciones que tienen boletos para el vuelo indicado.
        /// </summary>
        [HttpGet("vuelo/{vueloId:int}")]
        public async Task<IActionResult> ObtenerPorVuelo(int vueloId)
        {
            try
            {
                var reservaciones = await _svc.ObtenerPorVueloAsync(vueloId);
                return Ok(reservaciones);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al obtener reservaciones del vuelo {VueloId} (admin)", vueloId);
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // GET /api/admin/reservaciones
        /// <summary>Listado completo de todas las reservaciones.</summary>
        [HttpGet]
        public async Task<IActionResult> ObtenerTodas()
        {
            try
            {
                var reservaciones = await _svc.ObtenerTodasAsync();
                return Ok(reservaciones);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al obtener listado de reservaciones (admin)");
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // GET /api/admin/reservaciones/{id}
        /// <summary>Detalle completo de una reservacion por su ID.</summary>
        [HttpGet("{id:int}")]
        public async Task<IActionResult> ObtenerPorId(int id)
        {
            try
            {
                var detalle = await _svc.ObtenerPorIdAsync(id);
                if (detalle == null)
                    return NotFound(new { message = $"La reservacion con ID {id} no fue encontrada." });
                return Ok(detalle);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al obtener detalle de reservacion {Id} (admin)", id);
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // POST /api/admin/reservaciones/{id}/cancelar
        /// <summary>
        /// Cancela administrativamente una reservacion: cambia estado a Cancelada (3),
        /// cancela boletos, devuelve disponibilidad en el vuelo y notifica al usuario.
        /// </summary>
        [HttpPost("{id:int}/cancelar")]
        public async Task<IActionResult> Cancelar(int id, [FromBody] CancelarRequestDto body)
        {
            if (body == null || string.IsNullOrWhiteSpace(body.Motivo))
                return BadRequest(new { message = "El motivo de cancelacion es obligatorio." });

            try
            {
                var (ok, mensaje) = await _svc.CancelarAsync(id, body.Motivo);
                if (!ok)
                {
                    if (mensaje.Contains("no fue encontrada"))
                        return NotFound(new { message = mensaje });
                    return BadRequest(new { message = mensaje });
                }

                _logger.LogInformation(
                    "Reservacion {Id} cancelada administrativamente. Motivo: {Motivo}", id, body.Motivo);
                return Ok(new { message = mensaje });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al cancelar reservacion {Id} (admin)", id);
                return StatusCode(500, new { message = "Error interno al cancelar la reservacion." });
            }
        }

        public class CancelarRequestDto
        {
            public string Motivo { get; set; } = "";
        }
    }
}