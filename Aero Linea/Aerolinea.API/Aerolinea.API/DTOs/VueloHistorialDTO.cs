namespace Aerolinea.API.Models.DTOs
{
    /// <summary>
    /// DTO de lectura que expone el historial de un vuelo para reportes administrativos.
    /// Incluye numero de vuelo, ruta, horarios, estado, capacidad, boletos vendidos y precios.
    /// </summary>
    public class VueloHistorialDTO
    {
        public int Id { get; set; }
        public string NumeroVuelo { get; set; } = string.Empty;

        public string Origen { get; set; } = string.Empty;
        public string Destino { get; set; } = string.Empty;

        public string Fecha { get; set; } = string.Empty;
        public string HoraSalida { get; set; } = string.Empty;
        public string HoraLlegada { get; set; } = string.Empty;

        public string? FechaLlegada { get; set; }

        public string Estado { get; set; } = string.Empty;

        public int AsientosTotales { get; set; }
        public int BoletosTurista { get; set; }
        public int BoletosEjecutivo { get; set; }
        public int AsientosVendidos { get; set; }

        public decimal PrecioTurista { get; set; }
        public decimal PrecioEjecutiva { get; set; }

        // Datos del avión asignado
        public int AvionId { get; set; }
        public string AvionNombre { get; set; } = string.Empty;

        // Boletos realmente vendidos (EstadoBoletoID IN (2,3))
        public int BoletosVendidosReal { get; set; }
    }
}
