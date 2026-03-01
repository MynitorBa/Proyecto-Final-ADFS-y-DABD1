namespace Aerolinea.API.DTOs
{
    // Resultado completo de búsqueda 
    public class ResultadoBusquedaDTO
    {
        public List<VueloDetalleDTO> Directos { get; set; } = new();
        public List<VueloConEscalaDTO> ConEscala { get; set; } = new();
    }

    // Un itinerario con escala
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