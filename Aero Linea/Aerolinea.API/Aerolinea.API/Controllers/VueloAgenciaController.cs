using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de busqueda de vuelos para agencias de viaje externas. Expone el endpoint
    /// de busqueda que aplica el descuento negociado de la agencia a las tarifas retornadas.
    /// Requiere autenticacion de agencia mediante AgenciaAuthMiddleware.
    /// </summary>
    [ApiController]
    [Route("api/vuelos-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class VueloAgenciaController : ControllerBase
    {
        private readonly VueloAgenciaService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de vuelos para agencias.
        /// </summary>
        public VueloAgenciaController(VueloAgenciaService service)
        {
            _service = service;
        }

        /// <summary>
        /// Busca vuelos disponibles segun los criterios del DTO y aplica el descuento de la agencia
        /// autenticada a las tarifas retornadas. Requiere autenticacion de agencia.
        /// </summary>
        [HttpPost("buscar")]
        public async Task<IActionResult> BuscarVuelos([FromBody] BuscarVueloAgenciaDTO dto)
        {
            try
            {
                int agenciaId = (int)HttpContext.Items["agencia_id"]!;
                var resultado = await _service.BuscarVuelos(dto, agenciaId);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
