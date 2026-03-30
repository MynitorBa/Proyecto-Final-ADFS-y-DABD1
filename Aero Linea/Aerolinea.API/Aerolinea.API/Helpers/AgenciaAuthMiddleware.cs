using Aerolinea.API.Repositories;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace Aerolinea.API.Helpers
{
    public class AgenciaIdentidad
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string URLAgencia { get; set; } = string.Empty;
    }

    public class AgenciaAuthMiddleware : IAsyncActionFilter
    {
        private readonly AgenciaRepository _repo;

        public AgenciaAuthMiddleware(AgenciaRepository repo)
        {
            _repo = repo;
        }

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