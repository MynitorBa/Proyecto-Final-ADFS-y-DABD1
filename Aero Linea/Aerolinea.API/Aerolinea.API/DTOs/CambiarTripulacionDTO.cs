namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para cambiar solo la tripulación asignada a un vuelo.
    /// Requiere exactamente 5 tripulantes: 1 piloto, 1 copiloto, 3 auxiliares.
    /// </summary>
    public class CambiarTripulacionDTO
    {
        public List<int> TripulantesIds { get; set; } = new();
        public string Motivo { get; set; } = string.Empty;
    }
}
