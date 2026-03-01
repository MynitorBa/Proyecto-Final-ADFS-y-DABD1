using System.ComponentModel.DataAnnotations;

namespace Aerolinea.API.DTOs
{
    /// DTO para subir una imagen en Base64 de forma independiente
    public class SubirImagenDTO
    {
        [Required]
        public string ImagenBase64 { get; set; }
    }
}