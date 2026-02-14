using Microsoft.AspNetCore.Mvc;
using Aerolinea.API.Services;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/nacionalidades")]
    public class NacionalidadesController : ControllerBase
    {
        private readonly NacionalidadService _service;

        public NacionalidadesController(NacionalidadService service)
        {
            _service = service;
        }

        [HttpGet]
        public async Task<IActionResult> Get()
        {
            var data = await _service.ObtenerTodas();
            return Ok(data);
        }
    }
}
