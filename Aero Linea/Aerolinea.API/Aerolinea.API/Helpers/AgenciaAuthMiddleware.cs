using Aerolinea.API.Repositories;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Representa la identidad de una agencia externa autenticada.
    /// Contiene los datos basicos que se propagan en el contexto HTTP tras validar el token.
    /// </summary>
    public class AgenciaIdentidad
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string URLAgencia { get; set; } = string.Empty;
    }

    /// <summary>
    /// Filtro de autenticacion para agencias externas que consumen la API.
    /// Valida el token enviado en el header X-Agencia-Token antes de ejecutar el action del controller.
    /// Si el token es invalido o no existe, detiene la ejecucion y retorna 401 Unauthorized.
    /// </summary>
    public class AgenciaAuthMiddleware : IAsyncActionFilter
    {
        private readonly AgenciaRepository _repo;

        /// <summary>
        /// Inicializa el filtro con el repositorio de agencias necesario para validar el token.
        /// </summary>
        public AgenciaAuthMiddleware(AgenciaRepository repo)
        {
            _repo = repo;
        }

        /// <summary>
        /// Ejecuta la validacion del token de agencia antes de que el action del controller sea invocado.
        /// Extrae el token del header X-Agencia-Token, consulta la base de datos para verificarlo
        /// y almacena los datos de la agencia en HttpContext.Items si es valido.
        /// Retorna 401 si el token esta ausente o no corresponde a ninguna agencia registrada.
        /// </summary>
        public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
        {
            var token = context.HttpContext.Request.Headers["X-Agencia-Token"].FirstOrDefault();
            if (string.IsNullOrEmpty(token))
            {
                context.Result = new UnauthorizedObjectResult(new { message = "token de agencia requerido" });
                return;
            }

            var agencia = await _repo.ObtenerAgenciaPorToken(token);
            if (agencia == null)
            {
                context.Result = new UnauthorizedObjectResult(new { message = "token inválido — agencia no reconocida" });
                return;
            }

            context.HttpContext.Items["agencia_id"] = agencia.ID;
            context.HttpContext.Items["agencia_nombre"] = agencia.Nombre;
            context.HttpContext.Items["agencia_url"] = agencia.URLAgencia;

            await next();
        }
    }
}
