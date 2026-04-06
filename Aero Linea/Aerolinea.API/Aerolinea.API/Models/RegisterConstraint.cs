namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO que indica que campos unicos ya existen en el sistema durante el proceso de registro.
    /// Se utiliza para validar disponibilidad de correo, username y pasaporte antes de crear el usuario.
    /// </summary>
    public class RegisterConstraint
    {
        public bool CorreoExiste { get; set; }
        public bool UsernameExiste { get; set; }
        public bool PasaporteExiste { get; set; }
    }
}
