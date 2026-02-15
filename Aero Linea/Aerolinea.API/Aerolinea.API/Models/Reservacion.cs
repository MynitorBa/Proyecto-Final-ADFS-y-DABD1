namespace Aerolinea.API.Models
{
    public class Reservacion
    {
        public int Id { get; set; }
        public string NoReservacion { get; set; }
        public int UsuarioId { get; set; }
        public DateTime FechaCreacion { get; set; }
        public DateTime? FechaExpiracion { get; set; }
        public decimal Total { get; set; }
        public int EstadoReservaId { get; set; }
    }
}