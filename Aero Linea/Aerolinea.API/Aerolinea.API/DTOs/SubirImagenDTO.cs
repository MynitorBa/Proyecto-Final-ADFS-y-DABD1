using System.ComponentModel.DataAnnotations;

namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para subir una imagen codificada en Base64 de forma independiente a cualquier entidad.
    /// Se utiliza en endpoints de carga de imagenes para aeropuertos, aviones y tripulantes.
    /// </summary>
    public class SubirImagenDTO
    {
        [Required]
        public string ImagenBase64 { get; set; }
    }
}
