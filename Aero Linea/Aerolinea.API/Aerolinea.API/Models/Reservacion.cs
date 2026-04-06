namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa una reservacion de vuelo realizada por un usuario.
    /// Contiene numero unico de reserva, fechas de creacion y expiracion, total
    /// a pagar y estado actual del proceso de reserva.
    /// </summary>
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
