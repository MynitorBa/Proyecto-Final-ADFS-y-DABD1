namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de respuesta que agrupa los resultados de una busqueda de vuelos.
    /// Separa los vuelos directos disponibles de los itinerarios con escala.
    /// </summary>
    public class ResultadoBusquedaDTO
    {
        public List<VueloDetalleDTO> Directos { get; set; } = new();
        public List<VueloConEscalaDTO> ConEscala { get; set; } = new();
    }

    /// <summary>
    /// DTO que representa un itinerario de vuelo con una o mas escalas.
    /// Incluye duracion total, tiempo de espera en escala, precios y disponibilidad
    /// calculados como el minimo entre los tramos del itinerario.
    /// </summary>
    public class VueloConEscalaDTO
    {
        public int NumeroEscalas { get; set; }
        public int DuracionTotalMinutos { get; set; }  // vuelo1 + escala + vuelo2
        public int TiempoEscalaMinutos { get; set; }  // solo la espera
        public decimal? PrecioTuristaTotal { get; set; }  // suma tramo1 + tramo2
        public decimal? PrecioEjecutivaTotal { get; set; }
        public int? BoletosDisponiblesTurista { get; set; }  // mínimo entre tramos
        public int? BoletosDisponiblesEjecutiva { get; set; }
        public List<VueloDetalleDTO> Tramos { get; set; } = new();
    }
}
