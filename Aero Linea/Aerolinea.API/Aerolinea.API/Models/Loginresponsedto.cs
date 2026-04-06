namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de respuesta devuelto al cliente despues de un inicio de sesion exitoso.
    /// Contiene identificacion del usuario, nombre, correo y datos del rol asignado.
    /// </summary>
    public class LoginResponseDto
    {
        public int UsuarioId { get; set; }
        public string Nombre { get; set; }
        public string Correo { get; set; }
        public int RolId { get; set; }
        public string RolNombre { get; set; }
    }
}
