namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para activar o desactivar un recurso (avion, tripulante) sin eliminarlo fisicamente.
    /// </summary>
    public class CambiarEstadoDTO
    {
        public bool Activo { get; set; }
    }
}
