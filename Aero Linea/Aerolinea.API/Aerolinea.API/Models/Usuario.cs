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
        public int Edad { get; set; }
        public string NumeroEmergencia { get; set; }
        public int NacionalidadId { get; set; }
    }
}
