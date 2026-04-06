namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para autenticar a un usuario en el sistema.
    /// Acepta correo electronico o username junto con la contrasena del usuario.
    /// </summary>
    public class LoginRequestDto
    {
        public string CorreoOUsername { get; set; }
        public string Contrasena { get; set; }
    }
}
