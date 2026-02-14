namespace Aerolinea.API.DTOs
{
    public class VueloDetalleDTO
    {
        public int Id { get; set; }
        public string NumeroVuelo { get; set; }
        public DateTime Fecha { get; set; }
        public TimeSpan HoraSalida { get; set; }
        public TimeSpan HoraLlegada { get; set; }
        public int DuracionMinutos { get; set; }

        // Estado
        public int EstadoId { get; set; }
        public string Estado { get; set; }

        // Avión
        public int AvionId { get; set; }
        public string AvionModelo { get; set; }
        public string AvionMarca { get; set; }
        public int CapacidadPasajeros { get; set; }

        // Origen
        public int OrigenId { get; set; }
        public string OrigenNombre { get; set; }
        public string OrigenCodigo { get; set; }
        public string OrigenCiudad { get; set; }
        public string OrigenPais { get; set; }

        // Destino
        public int DestinoId { get; set; }
        public string DestinoNombre { get; set; }
        public string DestinoCodigo { get; set; }
        public string DestinoCiudad { get; set; }
        public string DestinoPais { get; set; }

        // Ruta
        public int RutaId { get; set; }
    }
}