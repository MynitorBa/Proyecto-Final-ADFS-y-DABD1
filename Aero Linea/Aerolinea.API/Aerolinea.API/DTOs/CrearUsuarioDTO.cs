namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para registrar un nuevo usuario en el sistema.
    /// Contiene credenciales de acceso, datos personales, ubicacion geografica
    /// y lista de nacionalidades. El rol por defecto es usuario regular (RolID = 2).
    /// </summary>
    public class CrearUsuarioDTO
    {
        public string Correo { get; set; }
        public string Contrasena { get; set; }
        public string Pasaporte { get; set; }
        public string Username { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Telefono { get; set; }
        public DateTime FechaNacimiento { get; set; }
        public string Pais { get; set; }
        public string Ciudad { get; set; }
        public List<string> Nacionalidades { get; set; } = new();
        public int RolID { get; set; } = 2;
    }
}
