namespace Aerolinea.API.DTOs
{
    public class UsuarioInfoDTO
    {
        public int Id { get; set; }
        public string Correo { get; set; }
        public string Pasaporte { get; set; }
        public string Username { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Telefono { get; set; }
        public DateTime FechaNacimiento { get; set; }
        public string Pais { get; set; }
        public string Ciudad { get; set; }
        public int RolID { get; set; }
        public string NombreRol { get; set; }
        public List<string> Nacionalidades { get; set; }
    }
}