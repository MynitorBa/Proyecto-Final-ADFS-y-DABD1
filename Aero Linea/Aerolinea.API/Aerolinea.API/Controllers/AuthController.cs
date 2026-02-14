using Microsoft.AspNetCore.Mvc;
using Aerolinea.API.DTOs;
using Aerolinea.API.Services;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ControllerBase
    {
        private readonly AuthService _service;

        public AuthController(AuthService service)
        {
            _service = service;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login(LoginRequestDto request)
        {
            var success = await _service.Login(request);

            if (!success)
                return Unauthorized("Credenciales inválidas");

            return Ok("Login correcto");
        }
    }
}
