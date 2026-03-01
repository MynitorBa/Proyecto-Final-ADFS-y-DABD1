namespace Aerolinea.API.Models.DTOs
{
    public class CrearVueloAdminDTO
    {
        public string NumeroVuelo { get; set; }
        public int AeropuertoOrigenId { get; set; }
        public int AeropuertoDestinoId { get; set; }
        public int AvionId { get; set; }
        public DateTime Fecha { get; set; }
        public string HoraSalida { get; set; }
        public string HoraLlegada { get; set; }
        public int BoletosTurista { get; set; }
        public int BoletosEjecutivo { get; set; }
        public decimal PrecioTurista { get; set; }
        public decimal PrecioEjecutiva { get; set; }
        public List<int> TripulantesIds { get; set; }
    }
}