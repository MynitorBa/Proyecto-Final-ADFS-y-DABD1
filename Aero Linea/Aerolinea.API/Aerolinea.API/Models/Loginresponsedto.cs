namespace Aerolinea.API.DTOs
{
    public class LoginResponseDto
    {
        public int UsuarioId { get; set; }
        public string Nombre { get; set; }
        public string Correo { get; set; }
        public int RolId { get; set; }
        public string RolNombre { get; set; }
    }
}