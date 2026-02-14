namespace Aerolinea.API.Models
{
    public class RegisterConstraint
    {
        public bool CorreoExiste { get; set; }
        public bool UsernameExiste { get; set; }
        public bool PasaporteExiste { get; set; }
    }
}