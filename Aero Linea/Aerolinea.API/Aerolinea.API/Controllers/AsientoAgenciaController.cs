using Aerolinea.API.DTOs.Agencia;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de gestion de asientos para agencias de viaje. Permite a una agencia
    /// autenticada consultar y cambiar los asientos de los boletos dentro de sus reservaciones.
    /// Todos los endpoints requieren autenticacion de agencia mediante AgenciaAuthMiddleware.
    /// </summary>
    [ApiController]
    [Route("api/asientos-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class AsientoAgenciaController : ControllerBase
    {
        private readonly AsientoAgenciaService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de asientos para agencias.
        /// </summary>
        public AsientoAgenciaController(AsientoAgenciaService service)
        {
            _service = service;
        }

        /// <summary>
        /// Retorna los asientos asignados a cada boleto de una reservacion especifica de la agencia.
        /// Verifica que la reservacion pertenezca a la agencia autenticada antes de retornar datos.
        /// </summary>
        [HttpGet("reservacion/{reservacionId}")]
        public async Task<IActionResult> ObtenerAsientosPorReservacion(int reservacionId)
        {
            try
            {
                int agenciaId = ObtenerAgenciaId();
                var result = await _service.ObtenerAsientosPorReservacion(reservacionId, agenciaId);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Cambia el asiento asignado a un boleto especifico de una reservacion de la agencia.
        /// Verifica que el boleto pertenezca a la agencia autenticada antes de aplicar el cambio.
        /// </summary>
        [HttpPut("{boletoId}")]
        public async Task<IActionResult> CambiarAsiento(int boletoId, [FromBody] CambiarAsientoAgenciaRequestDTO dto)
        {
            try
            {
                int agenciaId = ObtenerAgenciaId();
                await _service.CambiarAsiento(boletoId, dto.NuevoAsiento, agenciaId);
                return Ok(new { message = "Asiento actualizado exitosamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        private int ObtenerAgenciaId()
        {
            var agencia = HttpContext.Items["agencia_id"];
            if (agencia == null) throw new Exception("No autorizado.");
            return (int)agencia;
        }
    }
}
