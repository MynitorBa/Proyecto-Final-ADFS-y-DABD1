using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de reservaciones para usuarios del portal web. Expone los endpoints
    /// para crear una reservacion y agregar los datos de los pasajeros asociados.
    /// La creacion de reservacion requiere sesion activa; el alta de pasajeros tambien.
    /// </summary>
    [ApiController]
    [Route("api/reservaciones")]
    public class ReservacionesController : ControllerBase
    {
        private readonly ReservacionService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de reservaciones.
        /// </summary>
        public ReservacionesController(ReservacionService service)
        {
            _service = service;
        }

        // POST api/reservaciones
        /// <summary>
        /// Crea una nueva reservacion en estado pendiente para el usuario autenticado.
        /// Asigna automaticamente los boletos y asientos segun el vuelo y la clase seleccionados.
        /// </summary>
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
        /// <summary>
        /// Registra o actualiza los datos de los pasajeros de una reservacion existente.
        /// Debe llamarse antes de confirmar la compra para asociar la informacion de cada
        /// pasajero al boleto correspondiente.
        /// </summary>
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
