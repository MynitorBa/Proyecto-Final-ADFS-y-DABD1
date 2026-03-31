namespace Aerolinea.API.DTOs.Agencia
{
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

    public class BoletoAsientoAgenciaDTO
    {
        public int BoletoId { get; set; }
        public string NoBoleto { get; set; }
        public string Asiento { get; set; }
        public int ClaseId { get; set; }
        public string Clase { get; set; }
    }

    public class CambiarAsientoAgenciaRequestDTO
    {
        public string NuevoAsiento { get; set; }
    }
}