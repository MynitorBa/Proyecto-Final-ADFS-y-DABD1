namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para editar un vuelo existente. Solo permitido con mas de 48h de anticipacion.
    /// </summary>
    public class EditarVueloDTO
    {
        public DateTime Fecha { get; set; }
        public string HoraSalida { get; set; } = string.Empty;
        public int AvionId { get; set; }
        public List<int> TripulantesIds { get; set; } = new();
        public decimal PrecioTurista { get; set; }
        public decimal PrecioEjecutiva { get; set; }
        public int BoletosTurista { get; set; }
        public int BoletosEjecutivo { get; set; }
    }
}
