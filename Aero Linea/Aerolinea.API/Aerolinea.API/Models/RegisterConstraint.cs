namespace Aerolinea.API.DTOs
{
    public class RegisterConstraint
    {
        public bool CorreoExiste { get; set; }
        public bool UsernameExiste { get; set; }
        public bool PasaporteExiste { get; set; }
    }
}