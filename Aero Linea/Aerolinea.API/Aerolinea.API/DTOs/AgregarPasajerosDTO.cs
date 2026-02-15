namespace Aerolinea.API.DTOs
{
    public class AgregarPasajerosDTO
    {
        public int ReservacionId { get; set; }
        public List<DatosPasajeroDTO> Pasajeros { get; set; }
    }

    public class DatosPasajeroDTO
    {
        public int BoletoId { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Pasaporte { get; set; }
        public string Telefono { get; set; }
        public int PaisId { get; set; }
        public int CiudadId { get; set; }
    }
}