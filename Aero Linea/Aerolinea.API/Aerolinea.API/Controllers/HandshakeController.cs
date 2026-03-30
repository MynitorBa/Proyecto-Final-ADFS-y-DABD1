using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/agencias")]
    public class HandshakeController : ControllerBase
    {
        private readonly HandshakeService _service;

        public HandshakeController(HandshakeService service)
        {
            _service = service;
        }


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
