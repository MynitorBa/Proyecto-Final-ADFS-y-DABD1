using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
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

        // POST api/auth/login
        [HttpPost("login")]
        public async Task<IActionResult> Login(LoginRequestDto request)
        {
            var result = await _service.Login(request);

            if (result == null)
                return Unauthorized("Credenciales inválidas");

            // Construimos los claims que viajan dentro de la cookie cifrada
            var claims = new List<Claim>
            {
                new Claim(SessionHelper.ClaimUsuarioId, result.UsuarioId.ToString()),
                new Claim(SessionHelper.ClaimRolId,     result.RolId.ToString()),
                new Claim(SessionHelper.ClaimRolNombre, result.RolNombre),   // ClaimTypes.Role
                new Claim(SessionHelper.ClaimNombre,    result.Nombre),       // ClaimTypes.Name
                new Claim(SessionHelper.ClaimCorreo,    result.Correo),       // ClaimTypes.Email
            };

            var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
            var principal = new ClaimsPrincipal(identity);

            // Firmamos y enviamos la cookie al navegador
            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                principal,
                new AuthenticationProperties
                {
                    IsPersistent = true,                       
                    ExpiresUtc = DateTimeOffset.UtcNow.AddHours(8)
                });

            // Devolvemos los datos al frontend para que los use en UI
            return Ok(result);
        }

        // GET api/auth/sesion
        // Devuelve la información de la sesión activa.
        [Authorize]
        [HttpGet("sesion")]
        public IActionResult ObtenerSesion()
        {
            return Ok(new
            {
                UsuarioId = SessionHelper.GetUsuarioId(HttpContext),
                Nombre = SessionHelper.GetNombre(HttpContext),
                Correo = SessionHelper.GetCorreo(HttpContext),
                RolId = SessionHelper.GetRolId(HttpContext),
                RolNombre = SessionHelper.GetRolNombre(HttpContext)
            });
        }

        // POST api/auth/logout
        // Destruye la cookie de sesión
        [Authorize]
        [HttpPost("logout")]
        public async Task<IActionResult> Logout()
        {
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
            return Ok("Sesión cerrada correctamente");
        }
    }
}