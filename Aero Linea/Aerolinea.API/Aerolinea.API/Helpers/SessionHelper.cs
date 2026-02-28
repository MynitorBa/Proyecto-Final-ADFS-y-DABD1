using System.Security.Claims;

namespace Aerolinea.API.Helpers
{

    public static class SessionHelper
    {
        // Nombres de los claims, centralizados aquí para no usar strings sueltos por todo el proyecto
        public const string ClaimUsuarioId = "UsuarioId";
        public const string ClaimRolId = "RolId";
        public const string ClaimRolNombre = ClaimTypes.Role;
        public const string ClaimNombre = ClaimTypes.Name;
        public const string ClaimCorreo = ClaimTypes.Email;

        /// Devuelve el Id del usuario autenticado, o null si no hay sesión.
        public static int? GetUsuarioId(HttpContext context)
        {
            var valor = context.User.FindFirstValue(ClaimUsuarioId);
            return int.TryParse(valor, out var id) ? id : null;
        }

        /// Devuelve el Id del rol, o null si no hay sesión.
        public static int? GetRolId(HttpContext context)
        {
            var valor = context.User.FindFirstValue(ClaimRolId);
            return int.TryParse(valor, out var id) ? id : null;
        }

        /// Devuelve el nombre del rol
        public static string? GetRolNombre(HttpContext context)
            => context.User.FindFirstValue(ClaimRolNombre);

        ///>Devuelve el nombre del usuario, o null.
        public static string? GetNombre(HttpContext context)
            => context.User.FindFirstValue(ClaimNombre);

        /// Devuelve el correo del usuario, o null.
        public static string? GetCorreo(HttpContext context)
            => context.User.FindFirstValue(ClaimCorreo);

        /// Indica si el usuario tiene una sesión activa.
        public static bool EstaAutenticado(HttpContext context)
            => context.User.Identity?.IsAuthenticated == true;

        /// Comprueba si el usuario tiene un rol concreto.
        public static bool TieneRol(HttpContext context, string rolNombre)
            => context.User.IsInRole(rolNombre);
    }
}