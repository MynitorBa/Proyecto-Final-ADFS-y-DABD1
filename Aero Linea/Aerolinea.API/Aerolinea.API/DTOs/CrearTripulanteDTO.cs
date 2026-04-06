using System.ComponentModel.DataAnnotations;

namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para registrar o actualizar un miembro de la tripulacion en el sistema.
    /// Requiere nombre, apellido y rol asignado. Acepta imagen en Base64 de forma opcional.
    /// </summary>
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
