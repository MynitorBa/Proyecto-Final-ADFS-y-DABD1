namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que registra una busqueda de vuelo realizada por un usuario o agencia.
    /// Almacena origen, destino, fechas, cantidad de pasajeros y tipo de viaje
    /// para fines de analisis y metricas del sistema.
    /// </summary>
    public class BusquedaVuelo
    {
        public string Id { get; set; } = Guid.NewGuid().ToString();
        public int OrigenId { get; set; }
        public int DestinoId { get; set; }
        public string OrigenNombre { get; set; }
        public string DestinoNombre { get; set; }
        public string OrigenCodigo { get; set; }
        public string DestinoCodigo { get; set; }
        public string FechaIda { get; set; }
        public string FechaVuelta { get; set; }
        public int Pasajeros { get; set; }
        public string TripType { get; set; }
        public DateTime FechaCreacion { get; set; } = DateTime.UtcNow;
    }
}
