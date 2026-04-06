using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de estado de la API. Expone un endpoint publico para verificar que el
    /// servicio esta en linea y obtener informacion basica del ambiente y la hora del servidor.
    /// Utilizado por herramientas de monitoreo y orquestacion de contenedores.
    /// </summary>
    [ApiController]
    [Route("[controller]")]
    public class HealthController : ControllerBase
    {
        /// <summary>
        /// Retorna el estado actual de la API, el ambiente de ejecucion (Development, Production, etc.)
        /// y la hora UTC del servidor. Endpoint publico, no requiere autenticacion.
        /// </summary>
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
