using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/mis-reservaciones")]
    public class MisReservacionesController : ControllerBase
    {
        private readonly GestionReservacionService _service;

        public MisReservacionesController(GestionReservacionService service)
        {
            _service = service;
        }

        [HttpGet("usuario/{usuarioId}")]
        public async Task<IActionResult> ObtenerMisReservaciones(int usuarioId)
        {
            try
            {
                var reservaciones = await _service.ObtenerMisReservaciones(usuarioId);
                return Ok(reservaciones);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet("{reservacionId}/usuario/{usuarioId}")]
        public async Task<IActionResult> ObtenerDetalleReservacion(int reservacionId, int usuarioId)
        {
            try
            {
                var reservacion = await _service.ObtenerDetalleReservacion(reservacionId, usuarioId);
                return Ok(reservacion);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet("resumen/usuario/{usuarioId}")]
        public async Task<IActionResult> ObtenerResumen(int usuarioId)
        {
            try
            {
                var resumen = await _service.ObtenerResumen(usuarioId);
                return Ok(resumen);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPost("{reservacionId}/cancelar/usuario/{usuarioId}")]
        public async Task<IActionResult> CancelarReservacion(int reservacionId, int usuarioId)
        {
            try
            {
                await _service.CancelarReservacion(reservacionId, usuarioId);
                return Ok(new { message = "Reservación cancelada exitosamente" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}