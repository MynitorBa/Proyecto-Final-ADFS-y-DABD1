namespace Aerolinea.API.Models.DTOs
{
    public class VueloHistorialDTO
    {
        public int Id { get; set; }
        public string NumeroVuelo { get; set; }
        public string Ruta { get; set; } // "Ciudad Origen → Ciudad Destino"
        public string Fecha { get; set; } // formato "yyyy-MM-dd"
        public string HoraSalida { get; set; } // formato "HH:mm"
        public string HoraLlegada { get; set; } // formato "HH:mm"
        public string Estado { get; set; } // "activo", "completado", "cancelado"
        public int AsientosVendidos { get; set; }
        public int AsientosTotales { get; set; }
    }
}