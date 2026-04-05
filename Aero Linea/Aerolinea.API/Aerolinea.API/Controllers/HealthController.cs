using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class HealthController : ControllerBase
    {
        [HttpGet]
        public IActionResult Get()
        {
            return Ok(new
            {
                status = "OK",
                ambiente = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT"),
                hora = DateTime.UtcNow
            });
        }
    }
}