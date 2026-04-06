using System.Security.Claims;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Clase estatica de utilidad para acceder a los datos del usuario autenticado
    /// almacenados en los claims del JWT. Centraliza los nombres de los claims
    /// y expone metodos para leer el ID, rol, nombre y correo del usuario activo
    /// sin necesidad de manipular strings de claims directamente en los controllers.
    /// </summary>
    public static class SessionHelper
    {
        // Nombres de los claims, centralizados aquí para no usar strings sueltos por todo el proyecto
        public const string ClaimUsuarioId = "UsuarioId";
        public const string ClaimRolId = "RolId";
        public const string ClaimRolNombre = ClaimTypes.Role;
        public const string ClaimNombre = ClaimTypes.Name;
        public const string ClaimCorreo = ClaimTypes.Email;

        /// <summary>
        /// Retorna el ID del usuario autenticado extraido del claim JWT.
        /// Retorna null si el usuario no tiene sesion activa o el claim no existe.
        /// </summary>
        public static int? GetUsuarioId(HttpContext context)
        {
            var valor = context.User.FindFirstValue(ClaimUsuarioId);
            return int.TryParse(valor, out var id) ? id : null;
        }

        /// <summary>
        /// Retorna el ID del rol del usuario autenticado extraido del claim JWT.
        /// Retorna null si el usuario no tiene sesion activa o el claim no existe.
        /// </summary>
        public static int? GetRolId(HttpContext context)
        {
            var valor = context.User.FindFirstValue(ClaimRolId);
            return int.TryParse(valor, out var id) ? id : null;
        }

        /// <summary>
        /// Retorna el nombre del rol del usuario autenticado segun el claim de rol estandar de ASP.NET.
        /// Retorna null si no hay sesion activa.
        /// </summary>
        public static string? GetRolNombre(HttpContext context)
            => context.User.FindFirstValue(ClaimRolNombre);

        /// <summary>
        /// Retorna el nombre completo del usuario autenticado segun el claim de nombre estandar.
        /// Retorna null si no hay sesion activa.
        /// </summary>
        public static string? GetNombre(HttpContext context)
            => context.User.FindFirstValue(ClaimNombre);

        /// <summary>
        /// Retorna el correo electronico del usuario autenticado segun el claim de email estandar.
        /// Retorna null si no hay sesion activa.
        /// </summary>
        public static string? GetCorreo(HttpContext context)
            => context.User.FindFirstValue(ClaimCorreo);

        /// <summary>
        /// Indica si el contexto HTTP actual corresponde a un usuario con sesion autenticada.
        /// Retorna true si el usuario tiene una identidad valida y autenticada.
        /// </summary>
        public static bool EstaAutenticado(HttpContext context)
            => context.User.Identity?.IsAuthenticated == true;

        /// <summary>
        /// Verifica si el usuario autenticado posee el rol indicado segun los claims del JWT.
        /// Retorna true si el usuario pertenece al rol especificado.
        /// </summary>
        public static bool TieneRol(HttpContext context, string rolNombre)
            => context.User.IsInRole(rolNombre);
    }
}
