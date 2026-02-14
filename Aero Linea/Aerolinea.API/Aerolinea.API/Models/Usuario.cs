namespace Aerolinea.API.Models
{
    public class Usuario
    {
        public int Id { get; set; }
        public string Correo { get; set; }
        public string ContrasenaHash { get; set; }
        public string Pasaporte { get; set; }
        public string Username { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Telefono { get; set; }
        public DateTime FechaNacimiento { get; set; }
        public int PaisId { get; set; }
        public int CiudadId { get; set; }
        public int RolID { get; set; }
    }
}