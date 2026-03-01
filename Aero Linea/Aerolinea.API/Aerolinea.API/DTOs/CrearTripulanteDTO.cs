using System.ComponentModel.DataAnnotations;

namespace Aerolinea.API.DTOs
{
    public class CrearTripulanteDTO
    {
        [Required]
        public string Nombre { get; set; }

        [Required]
        public string Apellido { get; set; }

        [Required]
        public int RolID { get; set; }

        // Base64 opcional al crear/editar
        public string? ImagenBase64 { get; set; }
    }
}