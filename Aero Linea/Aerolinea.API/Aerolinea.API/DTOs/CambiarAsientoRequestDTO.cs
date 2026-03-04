namespace Aerolinea.API.Controllers
{
    // Request

    public class CambiarAsientoRequestDTO
    {
        public string NuevoAsiento { get; set; }
    }

    // Response 

    public class AsientosVueloDTO
    {
        /// >Capacidad total del avión asignado al vuelo.
        public int CapacidadPasajeros { get; set; }

        /// Columnas del avión (siempre A-F por configuración).
        public List<string> Columnas { get; set; }

        /// Número de filas ejecutivas (prefijo E-).
        public int FilasEjecutiva { get; set; }

        public int TotalFilas { get; set; }

        /// Asientos ya ocupados por otros pasajeros (estado 2 o 3,
        /// excluyendo los boletos de la reservación activa del usuario).

        public List<string> AsientosOcupados { get; set; }


        /// Boletos de la reservación pendiente activa del usuario en este vuelo.
        /// Incluye el asiento asignado automáticamente para mostrarlo pre-marcado.

        public List<BoletoAsientoDTO> BoletosUsuario { get; set; }
    }

 
    public class BoletoAsientoDTO
    {
        public int BoletoId { get; set; }
        public string NoBoleto { get; set; }
        public string Asiento { get; set; }
        public int ClaseId { get; set; }
        public string Clase { get; set; }
    }
}