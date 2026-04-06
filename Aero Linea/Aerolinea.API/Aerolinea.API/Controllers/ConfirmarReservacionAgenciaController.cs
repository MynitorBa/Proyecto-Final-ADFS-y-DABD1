using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador para confirmar reservaciones de agencias de viaje. Expone el endpoint
    /// de confirmacion que finaliza el proceso de compra de una reservacion pendiente
    /// creada por una agencia autenticada.
    /// </summary>
    [ApiController]
    [Route("api/reservaciones-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class ConfirmarReservacionAgenciaController : ControllerBase
    {
        private readonly ConfirmarReservacionAgenciaService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de confirmacion de reservaciones de agencia.
        /// </summary>
        public ConfirmarReservacionAgenciaController(ConfirmarReservacionAgenciaService service)
        {
            _service = service;
        }

        // POST api/reservaciones-agencia/{id}/confirmar
        /// <summary>
        /// Confirma y finaliza el pago de una reservacion existente de la agencia. Verifica que
        /// la reservacion pertenezca a la agencia autenticada antes de procesar la confirmacion.
        /// </summary>
        [HttpPost("{id}/confirmar")]
        public async Task<IActionResult> ConfirmarReservacion(
            int id,
            [FromBody] ConfirmarReservacionAgenciaDTO dto)
        {
            try
            {
                var agencia = HttpContext.Items["agencia_id"];
                if (agencia == null)
                    return Unauthorized(new { message = "Token de agencia requerido." });

                int agenciaId = (int)agencia;
                var resultado = await _service.ConfirmarReservacion(id, agenciaId, dto);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
