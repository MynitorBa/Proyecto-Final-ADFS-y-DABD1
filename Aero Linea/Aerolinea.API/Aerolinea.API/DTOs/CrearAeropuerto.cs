using System.ComponentModel.DataAnnotations;

namespace Aerolinea.API.DTOs
{
    public class CrearAeropuertoDTO
    {
        [Required]
        public string Nombre { get; set; }

        [Required]
        public string Codigo { get; set; }

        [Required]
        public string Ciudad { get; set; }  // Nombre de la ciudad

        [Required]
        public string Pais { get; set; }    // Nombre del país

        // Base64 opcional al crear/editar
        public string? ImagenBase64 { get; set; }
    }
}