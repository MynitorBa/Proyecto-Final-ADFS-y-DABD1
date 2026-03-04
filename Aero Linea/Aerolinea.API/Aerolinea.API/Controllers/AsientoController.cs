using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/asientos")]
    [Authorize]
    public class AsientoController : ControllerBase
    {
        private readonly AsientoService _service;

        public AsientoController(AsientoService service)
        {
            _service = service;
        }

        // GET api/asientos/{vueloId}
        // Devuelve todos los asientos ocupados (estado 2 y 3) del vuelo,
        // más el asiento asignado al boleto de la reservación activa del usuario
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