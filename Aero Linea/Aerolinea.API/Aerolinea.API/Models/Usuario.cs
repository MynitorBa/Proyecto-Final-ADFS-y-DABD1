namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad principal que representa a un usuario registrado en el sistema.
    /// Almacena credenciales de acceso, datos personales, ubicacion geografica
    /// y el rol asignado que determina los permisos dentro de la plataforma.
    /// </summary>
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
        public int CiudadId { get; set; }
        public int RolID { get; set; }
    }
}
