using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/reservaciones-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class ReservacionAgenciaController : ControllerBase
    {
        private readonly ReservacionAgenciaService _service;

        public ReservacionAgenciaController(
            ReservacionAgenciaService service)
        {
            _service = service;
        }

        [HttpPost]
        public async Task<IActionResult> CrearReservacion([FromBody] CrearReservacionDTO dto)
        {
            var agencia = HttpContext.Items["agencia_id"];
            if (agencia == null)
                return Unauthorized(new { message = "Token de agencia requerido" });

            int agenciaId = (int)agencia;

            try
            {
                var reservacion = await _service.CrearReservacion(dto, agenciaId);
                return Ok(reservacion);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPost("{id}/expirar")]
        public async Task<IActionResult> ExpirarReservacion(int id)
        {
            var agencia = HttpContext.Items["agencia_id"];
            if (agencia == null)
                return Unauthorized(new { message = "Token de agencia requerido" });
            int agenciaId = (int)agencia;  

            try
            {
                await _service.ExpirarReservacion(id, agenciaId);
                return Ok(new { message = "Reservación expirada correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }






        [HttpPost("pasajeros")]
        public async Task<IActionResult> AgregarPasajeros([FromBody] AgregarPasajerosDTO dto)
        {
            var agencia = HttpContext.Items["agencia_id"];
            if (agencia == null)
                return Unauthorized(new { message = "Token de agencia requerido" });

            int agenciaId = (int)agencia;

            try
            {
                await _service.AgregarPasajeros(dto, agenciaId);
                return Ok(new { message = "Datos de pasajeros guardados correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}