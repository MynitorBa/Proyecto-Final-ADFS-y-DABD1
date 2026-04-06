namespace Aerolinea.API.DTOs.Agencia
{
    /// <summary>
    /// DTO de respuesta que muestra el mapa de asientos de un vuelo para una agencia.
    /// Incluye configuracion del avion, asientos ocupados y boletos reservados por la agencia.
    /// </summary>
    public class AsientosVueloAgenciaDTO
    {
        public int VueloId { get; set; }
        public string NumeroVuelo { get; set; }
        public int CapacidadPasajeros { get; set; }
        public List<string> Columnas { get; set; }
        public int FilasEjecutiva { get; set; }
        public int TotalFilas { get; set; }
        public List<string> AsientosOcupados { get; set; }
        public List<BoletoAsientoAgenciaDTO> BoletosAgencia { get; set; }
    }

    /// <summary>
    /// DTO que representa un boleto con su asiento asignado dentro del contexto de una agencia.
    /// Contiene identificacion del boleto, numero, asiento y clase correspondiente.
    /// </summary>
    public class BoletoAsientoAgenciaDTO
    {
        public int BoletoId { get; set; }
        public string NoBoleto { get; set; }
        public string Asiento { get; set; }
        public int ClaseId { get; set; }
        public string Clase { get; set; }
    }

    /// <summary>
    /// DTO de peticion para cambiar el asiento de un boleto desde el contexto de una agencia.
    /// Contiene unicamente el nuevo asiento solicitado.
    /// </summary>
    public class CambiarAsientoAgenciaRequestDTO
    {
        public string NuevoAsiento { get; set; }
    }
}
