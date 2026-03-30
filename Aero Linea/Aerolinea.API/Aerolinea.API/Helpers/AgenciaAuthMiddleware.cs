using Aerolinea.API.Repositories;

namespace Aerolinea.API.Helpers
{
    public class AgenciaIdentidad
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string URLAgencia { get; set; } = string.Empty;
    }

    public class AgenciaAuthMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly AgenciaRepository _repo;

        public AgenciaAuthMiddleware(RequestDelegate next, AgenciaRepository repo)
        {
            _next = next;
            _repo = repo;
        }

        public async Task InvokeAsync(HttpContext context)
        {
            var token = context.Request.Headers["X-Agencia-Token"].FirstOrDefault();
            if (string.IsNullOrEmpty(token))
            {
                context.Response.StatusCode = 401;
                await context.Response.WriteAsJsonAsync(new { message = "token de agencia requerido" });
                return;
            }

            var agencia = await _repo.ObtenerAgenciaPorToken(token);
            if (agencia == null)
            {
                context.Response.StatusCode = 401;
                await context.Response.WriteAsJsonAsync(new { message = "token inválido — agencia no reconocida" });
                return;
            }

            context.Items["agencia_id"] = agencia.ID;
            context.Items["agencia_nombre"] = agencia.Nombre;
            context.Items["agencia_url"] = agencia.URLAgencia;

            await _next(context);
        }
    }
}