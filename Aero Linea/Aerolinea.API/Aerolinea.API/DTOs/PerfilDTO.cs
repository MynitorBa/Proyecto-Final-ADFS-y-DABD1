namespace Aerolinea.API.DTOs
{
    public class PerfilDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; } = "";
        public string Apellido { get; set; } = "";
        public string Correo { get; set; } = "";
        public string Username { get; set; } = "";
        public string Telefono { get; set; } = "";
        public string Pasaporte { get; set; } = "";
        public DateTime FechaNacimiento { get; set; }
        public string Pais { get; set; } = "";
        public string Ciudad { get; set; } = "";
    }

    public class ActualizarTelefonoDTO
    {
        public string Telefono { get; set; } = "";
    }

    public class CambiarContrasenaDTO
    {
        public string ContrasenaActual { get; set; } = "";
        public string NuevaContrasena { get; set; } = "";
    }
}