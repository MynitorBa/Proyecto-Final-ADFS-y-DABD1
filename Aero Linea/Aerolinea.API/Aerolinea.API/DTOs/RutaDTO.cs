namespace Aerolinea.API.Models.DTOs
{
    /// <summary>
    /// DTO para mostrar una ruta en el panel de administracion.
    /// Incluye aeropuertos, zonas horarias y duracion editable.
    /// </summary>
    public class RutaDTO
    {
        public int Id { get; set; }

        // Aeropuerto de origen
        public string Origen { get; set; }
        public string CodigoOrigen { get; set; }

        // Aeropuerto de destino
        public string Destino { get; set; }
        public string CodigoDestino { get; set; }

        /// <summary>Duración estimada del vuelo en minutos.</summary>
        public int DuracionEstimada { get; set; }

        /// <summary>Timezone IANA del aeropuerto de origen (puede ser null si no fue configurado).</summary>
        public string? ZonaHorariaOrigen { get; set; }

        /// <summary>Timezone IANA del aeropuerto de destino (puede ser null si no fue configurado).</summary>
        public string? ZonaHorariaDestino { get; set; }

        /// <summary>Cantidad de vuelos creados bajo esta ruta.</summary>
        public int TotalVuelos { get; set; }
    }

    /// <summary>
    /// DTO para actualizar solo la duración estimada de una ruta.
    /// </summary>
    public class EditarDuracionRutaDTO
    {
        public int DuracionEstimada { get; set; }
    }

    /// <summary>
    /// DTO de respuesta al calcular hora de llegada con zonas horarias.
    /// </summary>
    public class CalculoLlegadaResponseDTO
    {
        public string HoraLlegada { get; set; }       // "HH:mm"
        public string FechaLlegada { get; set; }      // "yyyy-MM-dd"
        public int DuracionMinutos { get; set; }
        public string? ZonaHorariaOrigen { get; set; }
        public string? ZonaHorariaDestino { get; set; }
        public bool UsoZonasHorarias { get; set; }
        public string Nota { get; set; }
    }

    /// <summary>
    /// DTO de petición para calcular hora de llegada.
    /// </summary>
    public class CalculoLlegadaRequestDTO
    {
        public int AeropuertoOrigenId { get; set; }
        public int AeropuertoDestinoId { get; set; }
        public string FechaSalida { get; set; }   // "yyyy-MM-dd"
        public string HoraSalida { get; set; }    // "HH:mm"
    }
    /// <summary>DTO para crear una ruta manualmente desde el panel admin.</summary>
    public class CrearRutaDTO
    {
        public int OrigenId { get; set; }
        public int DestinoId { get; set; }
        public int DuracionEstimada { get; set; } = 120;
    }

}
