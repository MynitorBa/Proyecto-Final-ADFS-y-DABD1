using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de autenticacion de agencias externas (handshake). Permite a una agencia
    /// externa autenticarse contra la API mediante un token de entrada y recibir un token de
    /// sesion para usar en las solicitudes posteriores a los endpoints de agencia.
    /// </summary>
    [ApiController]
    [Route("api/agencias")]
    public class HandshakeController : ControllerBase
    {
        private readonly HandshakeService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de handshake de agencias.
        /// </summary>
        public HandshakeController(HandshakeService service)
        {
            _service = service;
        }

        /// <summary>
        /// Recibe las credenciales de una agencia externa (URL y token de entrada), las valida
        /// contra la base de datos y retorna un token de sesion si la autenticacion es exitosa.
        /// </summary>
        [HttpPost("handshake")]
        public async Task<IActionResult> ProcesarHandshake([FromBody] HandshakeRequestDTO dto)
        {
            // Temporal para debug — ver qué está llegando
            Console.WriteLine($"[HANDSHAKE] url_agencia recibida: '{dto.UrlAgencia}'");
            Console.WriteLine($"[HANDSHAKE] token_entrada recibido: '{dto.TokenEntrada}'");

            try
            {
                var response = await _service.ProcesarHandshake(dto);
                return Ok(response);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[HANDSHAKE ERROR] {ex.Message}");
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
