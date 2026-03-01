namespace Aerolinea.API.Models.DTOs
{
    public class VueloHistorialDTO
    {
        public int Id { get; set; }
        public string NumeroVuelo { get; set; } = string.Empty;

        public string Origen { get; set; } = string.Empty;
        public string Destino { get; set; } = string.Empty;

        public string Fecha { get; set; } = string.Empty;
        public string HoraSalida { get; set; } = string.Empty;
        public string HoraLlegada { get; set; } = string.Empty;

        /// <summary>
        /// Fecha de llegada al destino (nullable, puede ser distinta a Fecha).
        /// Formato yyyy-MM-dd. Null si no fue cargada.
        /// </summary>
        public string? FechaLlegada { get; set; }

        public string Estado { get; set; } = string.Empty;

        public int AsientosTotales { get; set; }
        public int BoletosTurista { get; set; }
        public int BoletosEjecutivo { get; set; }
        public int AsientosVendidos { get; set; }

        public decimal PrecioTurista { get; set; }
        public decimal PrecioEjecutiva { get; set; }
    }
}