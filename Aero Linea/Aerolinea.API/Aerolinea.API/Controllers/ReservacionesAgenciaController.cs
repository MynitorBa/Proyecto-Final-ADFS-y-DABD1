using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de gestion de reservaciones existentes para agencias de viaje. Permite a
    /// una agencia autenticada consultar el detalle de una reservacion, cancelarla y verificar
    /// si es posible cancelarla. Todos los endpoints requieren autenticacion de agencia.
    /// </summary>
    [ApiController]
    [Route("api/reservaciones-agencia/gestion")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class ReservacionesAgenciaController : ControllerBase
    {
        private readonly GestionReservacionService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de gestion de reservaciones.
        /// </summary>
        public ReservacionesAgenciaController(GestionReservacionService service)
        {
            _service = service;
        }

        // GET api/reservaciones-agencia/gestion/{reservacionId}
        /// <summary>
        /// Retorna el detalle completo de una reservacion especifica de la agencia autenticada,
        /// incluyendo boletos, pasajeros, vuelos y estado actual.
        /// </summary>
        [HttpGet("{reservacionId}")]
        public async Task<IActionResult> ObtenerDetalle(int reservacionId)
        {
            try
            {
                int usuarioId = await ObtenerUsuarioWebId();
                var reservacion = await _service.ObtenerDetalleReservacion(reservacionId, usuarioId);
                if (reservacion == null)
                    return NotFound(new { message = "Reservación no encontrada." });
                return Ok(reservacion);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // POST api/reservaciones-agencia/gestion/{reservacionId}/cancelar
        /// <summary>
        /// Cancela una reservacion activa de la agencia autenticada. El motivo de cancelacion
        /// es opcional. Solo se pueden cancelar reservaciones que aun no hayan sido completadas.
        /// </summary>
        [HttpPost("{reservacionId}/cancelar")]
        public async Task<IActionResult> CancelarReservacion(int reservacionId, [FromBody] CancelarReservacionDTO dto)
        {
            string? ip = HttpContext.Connection.RemoteIpAddress?.ToString();
            string? userAgent = Request.Headers["User-Agent"].ToString();
            try
            {
                int usuarioId = await ObtenerUsuarioWebId();
                await _service.CancelarReservacion(reservacionId, usuarioId, dto?.Motivo, ip, userAgent, esAgencia: true);
                return Ok(new { message = "Reservación cancelada exitosamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Verifica si una reservacion de la agencia puede ser cancelada segun las reglas de negocio
        /// (estado actual, tiempo antes del vuelo, etc.). Retorna un objeto con el resultado de la validacion.
        /// </summary>
        [HttpGet("{reservacionId}/puede-cancelar")]
        public async Task<IActionResult> PuedeCancelar(int reservacionId)
        {
            try
            {
                int usuarioId = await ObtenerUsuarioWebId();
                var resultado = await _service.PuedeCancelar(reservacionId, usuarioId);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        private async Task<int> ObtenerUsuarioWebId()
        {
            int agenciaId = (int)HttpContext.Items["agencia_id"]!;
            return await _service.ObtenerUsuarioWebIdDeAgencia(agenciaId);
        }
    }
}
