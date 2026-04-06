using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador para la creacion y gestion inicial de reservaciones por parte de agencias de viaje.
    /// Permite a una agencia autenticada crear reservaciones, agregar datos de pasajeros y
    /// expirar reservaciones pendientes. Todos los endpoints requieren autenticacion de agencia.
    /// </summary>
    [ApiController]
    [Route("api/reservaciones-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class ReservacionAgenciaController : ControllerBase
    {
        private readonly ReservacionAgenciaService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de reservaciones de agencia.
        /// </summary>
        public ReservacionAgenciaController(
            ReservacionAgenciaService service)
        {
            _service = service;
        }

        /// <summary>
        /// Crea una nueva reservacion en estado pendiente para la agencia autenticada.
        /// Retorna el detalle de la reservacion creada incluyendo los boletos generados.
        /// </summary>
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

        /// <summary>
        /// Marca una reservacion pendiente como expirada. Se usa cuando la agencia no completa
        /// el flujo de confirmacion dentro del tiempo permitido.
        /// </summary>
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

        /// <summary>
        /// Guarda los datos de los pasajeros asociados a una reservacion de la agencia.
        /// Debe llamarse antes de confirmar la reservacion para registrar la informacion
        /// requerida de cada boleto.
        /// </summary>
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
