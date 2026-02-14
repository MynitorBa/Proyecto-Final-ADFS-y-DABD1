using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/health")]
    public class HealthController : ControllerBase
    {
        [HttpGet]
        public IActionResult Get()
        {
            return Ok(new
            {
                status = "OK",
                service = "Aerolinea.API",
                time = DateTime.UtcNow
            });
        }
    }
}
