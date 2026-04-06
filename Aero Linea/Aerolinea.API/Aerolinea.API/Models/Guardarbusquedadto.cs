namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO utilizado para persistir los parametros de una busqueda de vuelo realizada por el usuario.
    /// Contiene origen, destino, fechas, numero de pasajeros y tipo de viaje.
    /// </summary>
    public class GuardarBusquedaDto
    {
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
    }
}
