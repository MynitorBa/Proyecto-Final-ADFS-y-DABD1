using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador que inicia el proceso de handshake entre la aerolinea y un hotel aliado.
    /// Genera un token de entrada, lo envia al hotel y guarda el token de sesion resultante
    /// en la base de datos. Solo accesible por administradores autenticados.
    /// </summary>
    [ApiController]
    [Route("api/hoteles-aliados")]
    [Authorize]
    public class HandshakeHotelController : ControllerBase
    {
        private readonly HandshakeHotelService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de handshake de hoteles aliados.
        /// </summary>
        public HandshakeHotelController(HandshakeHotelService service)
        {
            _service = service;
        }

        // POST api/hoteles-aliados/{hotelId}/handshake
        /// <summary>
        /// Inicia el handshake de autenticacion con el hotel aliado identificado por hotelId.
        /// Genera un token de entrada, lo envia al endpoint /api/aerolineas/handshake del hotel
        /// y guarda el token de sesion recibido en HotelAliado.TokenHASH.
        /// Retorna el token de sesion resultante si el proceso fue exitoso.
        /// </summary>
        [HttpPost("{hotelId}/handshake")]
        public async Task<IActionResult> IniciarHandshake(int hotelId)
        {
            // Log de depuracion — confirma que la solicitud llego con el ID correcto
            Console.WriteLine($"[HANDSHAKE HOTEL] Iniciando handshake con hotel aliado ID: {hotelId}");

            try
            {
                string tokenSalida = await _service.IniciarHandshake(hotelId);

                Console.WriteLine($"[HANDSHAKE HOTEL] Handshake exitoso con hotel ID {hotelId}. Token guardado en BD.");

                return Ok(new
                {
                    mensaje = "Handshake con el hotel aliado completado exitosamente.",
                    token_salida = tokenSalida
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[HANDSHAKE HOTEL ERROR] Hotel ID {hotelId}: {ex.Message}");
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}