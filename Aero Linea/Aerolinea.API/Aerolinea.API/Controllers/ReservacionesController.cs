using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
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

        // POST api/reservaciones
        [HttpPost]
        [Authorize]
        public async Task<IActionResult> CrearReservacion([FromBody] CrearReservacionDTO dto)
        {
            try
            {
                int? usuarioId = SessionHelper.GetUsuarioId(HttpContext);
                var reservacion = await _service.CrearReservacion(dto, usuarioId);
                return Ok(reservacion);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // PUT api/reservaciones/{id}/pasajeros
        [HttpPut("{id}/pasajeros")]
        [Authorize]
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
                return Ok(new { message = "Pasajeros agregados correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}