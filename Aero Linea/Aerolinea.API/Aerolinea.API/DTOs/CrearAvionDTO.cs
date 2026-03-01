using System.ComponentModel.DataAnnotations;

namespace Aerolinea.API.DTOs
{
    public class CrearAvionDTO
    {
        [Required]
        public string Marca { get; set; }

        [Required]
        public string Modelo { get; set; }

        [Required]
        [Range(1, int.MaxValue)]
        public int CapacidadPasajeros { get; set; }

        // Base64 opcional al crear/editar
        public string? ImagenBase64 { get; set; }
    }
}