using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de gestion de asientos para usuarios autenticados. Permite al usuario
    /// consultar la disponibilidad de asientos en un vuelo y cambiar el asiento de su boleto.
    /// Todos los endpoints requieren sesion activa.
    /// </summary>
    [ApiController]
    [Route("api/asientos")]
    [Authorize]
    public class AsientoController : ControllerBase
    {
        private readonly AsientoService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de asientos.
        /// </summary>
        public AsientoController(AsientoService service)
        {
            _service = service;
        }

        // GET api/asientos/{vueloId}
        // Devuelve todos los asientos ocupados (estado 2 y 3) del vuelo,
        // más el asiento asignado al boleto de la reservación activa del usuario
        /// <summary>
        /// Devuelve la lista de asientos del vuelo indicado, marcando cuales estan ocupados
        /// y cual es el asiento actualmente asignado al usuario autenticado. Se usa para
        /// renderizar el mapa de asientos en el flujo de reservacion.
        /// </summary>
        [HttpGet("{vueloId}")]
        public async Task<IActionResult> ObtenerAsientosVuelo(int vueloId)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var resultado = await _service.ObtenerAsientosVuelo(vueloId, usuarioId);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // PUT api/asientos/{boletoId}
        // Cambia el NoAsiento del boleto indicado al nuevo asiento
        // Body: { "nuevoAsiento": "B3" }
        /// <summary>
        /// Cambia el asiento asignado al boleto indicado. Verifica que el boleto pertenezca
        /// al usuario autenticado y que el nuevo asiento este disponible antes de aplicar el cambio.
        /// </summary>
        [HttpPut("{boletoId}")]
        public async Task<IActionResult> CambiarAsiento(int boletoId, [FromBody] CambiarAsientoRequestDTO dto)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                await _service.CambiarAsiento(boletoId, dto.NuevoAsiento, usuarioId);
                return Ok(new { message = "Asiento actualizado correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        private int ObtenerUsuarioId()
        {
            int? id = SessionHelper.GetUsuarioId(HttpContext);
            if (id == null)
                throw new Exception("No se pudo obtener la sesión del usuario.");
            return id.Value;
        }
    }
}
