namespace Aerolinea.API.Models
{
    public class Vuelo
    {
        public int Id { get; set; }
        public string NumeroVuelo { get; set; }
        public DateTime Fecha { get; set; }
        public TimeSpan HoraSalida { get; set; }
        public TimeSpan HoraLlegada { get; set; }
        public int EstadoId { get; set; }
        public int AvionId { get; set; }
        public int RutaId { get; set; }

        // Disponibilidad y precios almacenados directamente en el vuelo
        public int BoletosTurista { get; set; }
        public int BoletosEjecutivo { get; set; }
        public decimal PrecioTurista { get; set; }
        public decimal PrecioEjecutivo { get; set; }
    }
}