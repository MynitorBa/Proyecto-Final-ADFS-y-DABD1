using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/reservaciones")]
    public class ReservacionesController : ControllerBase
    {
        private readonly ReservacionService _service;

        public ReservacionesController(ReservacionService service)
        {
            _service = service;
        }

        [HttpPost]
        public async Task<IActionResult> CrearReservacion([FromBody] CrearReservacionDTO dto)
        {
            try
            {
                var reservacion = await _service.CrearReservacion(dto);
                return Ok(reservacion);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPut("{id}/pasajeros")]
        public async Task<IActionResult> AgregarPasajeros(int id, [FromBody] List<DatosPasajeroDTO> pasajeros)
        {
            try
            {
                var dto = new AgregarPasajerosDTO
                {
                    ReservacionId = id,
                    Pasajeros = pasajeros
                };

                await _service.AgregarPasajeros(dto);
                return Ok(new { message = "Pasajeros agregados correctamente" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPost("{id}/confirmar")]
        public async Task<IActionResult> ConfirmarReservacion(int id)
        {
            try
            {
                await _service.ConfirmarReservacion(id);
                return Ok(new { message = "Reservación confirmada exitosamente" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}