using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/reservaciones-agencia/gestion")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class ReservacionesAgenciaController : ControllerBase
    {
        private readonly GestionReservacionService _service;

        public ReservacionesAgenciaController(GestionReservacionService service)
        {
            _service = service;
        }

        // GET api/reservaciones-agencia
        /*
        [HttpGet]
        public async Task<IActionResult> ObtenerReservaciones()
        {
            try
            {
                int usuarioId = await ObtenerUsuarioWebId();
                var reservaciones = await _service.ObtenerMisReservaciones(usuarioId);
                return Ok(reservaciones);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }*/

        // GET api/reservaciones-agencia/{reservacionId}
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
        [HttpPost("{reservacionId}/cancelar")]
        public async Task<IActionResult> CancelarReservacion(int reservacionId, [FromBody] CancelarReservacionDTO dto)
        {
            try
            {
                int usuarioId = await ObtenerUsuarioWebId();
                await _service.CancelarReservacion(reservacionId, usuarioId, dto?.Motivo);
                return Ok(new { message = "Reservación cancelada exitosamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
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