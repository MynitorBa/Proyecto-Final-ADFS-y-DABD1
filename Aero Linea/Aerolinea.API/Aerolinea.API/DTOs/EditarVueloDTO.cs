namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para editar un vuelo existente. Solo permitido con mas de 60 dias de anticipacion.
    /// No se permite modificar el precio ni el codigo del vuelo (prefijo de 4 letras).
    /// </summary>
    public class EditarVueloDTO
    {
        public DateTime Fecha { get; set; }
        public string HoraSalida { get; set; } = string.Empty;
        public int AvionId { get; set; }
        public List<int> TripulantesIds { get; set; } = new();
        public string Motivo { get; set; } = string.Empty;
    }
}
