namespace Aerolinea.API.Models.DTOs
{
    public class VueloHistorialDTO
    {
        public int Id { get; set; }
        public string NumeroVuelo { get; set; }

        // Separados para que el frontend pueda mostrarlos individualmente
        public string Origen { get; set; }
        public string Destino { get; set; }

        public string Fecha { get; set; }
        public string HoraSalida { get; set; }
        public string HoraLlegada { get; set; }
        public string Estado { get; set; }

        public int AsientosTotales { get; set; }
        public int BoletosTurista { get; set; }
        public int BoletosEjecutivo { get; set; }
        public int AsientosVendidos { get; set; }

        public decimal PrecioTurista { get; set; }
        public decimal PrecioEjecutiva { get; set; }
    }
}