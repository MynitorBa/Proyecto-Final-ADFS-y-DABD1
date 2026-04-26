namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de lectura con la informacion del perfil del usuario autenticado.
    /// Incluye datos personales, credenciales de contacto y ubicacion geografica.
    /// </summary>
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

    /// <summary>
    /// DTO de peticion para que el usuario actualice su numero de telefono.
    /// </summary>
    public class ActualizarTelefonoDTO
    {
        public string Telefono { get; set; } = "";
    }

    /// <summary>
    /// DTO de peticion para que el usuario cambie su contrasena actual.
    /// Requiere la contrasena vigente y la nueva contrasena deseada.
    /// </summary>
    public class CambiarContrasenaDTO
    {
        public string ContrasenaActual { get; set; } = "";
        public string NuevaContrasena { get; set; } = "";
    }

    /// <summary>
    /// DTO de peticion para que el usuario actualice su correo electronico.
    /// </summary>
    public class ActualizarCorreoDTO
    {
        public string NuevoCorreo { get; set; } = "";
    }

    /// <summary>
    /// DTO de peticion para actualizar los datos personales del usuario:
    /// nombre, apellido, username, pasaporte, fecha de nacimiento, pais y ciudad.
    /// </summary>
    public class ActualizarDatosPersonalesDTO
    {
        public string Nombre   { get; set; } = "";
        public string Apellido { get; set; } = "";
        public string Username { get; set; } = "";
        public string Pasaporte { get; set; } = "";
        public DateTime? FechaNacimiento { get; set; }
        public string Pais   { get; set; } = "";
        public string Ciudad { get; set; } = "";
    }
}
