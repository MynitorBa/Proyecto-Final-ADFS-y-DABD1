using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de nacionalidades. Expone un endpoint publico para obtener el catalogo
    /// de nacionalidades disponibles, utilizado en el formulario de registro de usuarios
    /// y en el ingreso de datos de pasajeros durante la reservacion.
    /// </summary>
    [ApiController]
    [Route("api/nacionalidades")]
    public class NacionalidadesController : ControllerBase
    {
        private readonly NacionalidadService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de nacionalidades.
        /// </summary>
        public NacionalidadesController(NacionalidadService service)
        {
            _service = service;
        }

        /// <summary>
        /// Retorna el listado completo de nacionalidades registradas en el sistema.
        /// Endpoint publico, no requiere autenticacion.
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> Get()
        {
            var data = await _service.ObtenerTodas();
            return Ok(data);
        }
    }
}
