using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/admin/reservaciones")]
    [Authorize(Roles = "Administrador")]
    public class AdminReservacionesController : ControllerBase
    {
        private readonly AdminReservacionesService _svc;
        private readonly ILogger<AdminReservacionesController> _logger;

        public AdminReservacionesController(
            AdminReservacionesService svc,
            ILogger<AdminReservacionesController> logger)
        {
            _svc = svc;
            _logger = logger;
        }

        [HttpGet("vuelos")]
        public async Task<IActionResult> ObtenerVuelos()
        {
            try { return Ok(await _svc.ObtenerVuelosConReservacionesAsync()); }
            catch (Exception ex) { return StatusCode(500, new { message = ex.Message }); }
        }

        [HttpGet("vuelo/{vueloId:int}")]
        public async Task<IActionResult> ObtenerPorVuelo(int vueloId)
        {
            try { return Ok(await _svc.ObtenerPorVueloAsync(vueloId)); }
            catch (Exception ex) { return StatusCode(500, new { message = ex.Message }); }
        }

        [HttpGet]
        public async Task<IActionResult> ObtenerTodas()
        {
            try { return Ok(await _svc.ObtenerTodasAsync()); }
            catch (Exception ex) { return StatusCode(500, new { message = ex.Message }); }
        }

        [HttpGet("{id:int}")]
        public async Task<IActionResult> ObtenerPorId(int id)
        {
            try
            {
                var d = await _svc.ObtenerPorIdAsync(id);
                return d == null
                    ? NotFound(new { message = $"Reservacion {id} no encontrada." })
                    : Ok(d);
            }
            catch (Exception ex) { return StatusCode(500, new { message = ex.Message }); }
        }

        [HttpPost("{id:int}/cancelar")]
        public async Task<IActionResult> Cancelar(int id, [FromBody] CancelarRequestDto body)
        {
            if (body == null || string.IsNullOrWhiteSpace(body.Motivo))
                return BadRequest(new { message = "El motivo de cancelacion es obligatorio." });

            try
            {
                var (ok, mensaje, agencia) = await _svc.CancelarAsync(id, body.Motivo);

                if (!ok)
                    return mensaje.Contains("no fue encontrada")
                        ? NotFound(new { message = mensaje })
                        : BadRequest(new { message = mensaje });

                _logger.LogInformation("Reservacion {Id} cancelada. Motivo: {Motivo}", id, body.Motivo);

                // Devolver tambien la respuesta de la agencia para que puedas verla
                return Ok(new
                {
                    message = mensaje,
                    notificacionAgencia = agencia == null ? null : new
                    {
                        esReservaDeAgencia = agencia.EsReservaDeAgencia,
                        nombreAgencia = agencia.NombreAgencia,
                        enviado = agencia.Enviado,
                        httpStatus = agencia.HttpStatus,
                        respuestaAgencia = agencia.RespuestaAgencia,
                        error = agencia.Error,
                    }
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error interno al cancelar." });
            }
        }

        public class CancelarRequestDto
        {
            public string Motivo { get; set; } = "";
        }
    }
}