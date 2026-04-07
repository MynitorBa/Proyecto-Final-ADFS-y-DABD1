using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador que expone el endpoint para solicitar un token de alianza
    /// a un hotel aliado especifico. Requiere sesion activa del usuario.
    /// </summary>
    [ApiController]
    [Route("api/hoteles-aliados")]
    public class TokenHotelController : ControllerBase
    {
        private readonly TokenHotelService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de tokens de hotel.
        /// </summary>
        public TokenHotelController(TokenHotelService service)
        {
            _service = service;
        }

        // POST api/hoteles-aliados/{aliadoId}/token
        /// <summary>
        /// Solicita un token de alianza al hotel identificado por aliadoId.
        /// El hotel genera un token de un solo uso valido por 15 minutos
        /// y retorna la URL lista para redirigir al usuario.
        /// </summary>
        [HttpPost("{aliadoId}/token")]
        [Authorize]
        public async Task<IActionResult> SolicitarToken(int aliadoId, [FromBody] TokenHotelRequestDTO dto)
        {
            try
            {
                var resultado = await _service.SolicitarToken(aliadoId, dto);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}