namespace Aerolinea.API.DTOs
{
    public class CrearUsuarioDTO
    {
        public string Correo { get; set; }
        public string Contrasena { get; set; }
        public string Pasaporte { get; set; }
        public string Username { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public int Edad { get; set; }
        public string NumeroEmergencia { get; set; }
        public int NacionalidadId { get; set; }
        public int RolID { get; set; } = 1;
    }
}
