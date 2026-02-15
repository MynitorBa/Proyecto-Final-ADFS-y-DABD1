namespace Aerolinea.API.Models
{
    public class DatosPasajero
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Pasaporte { get; set; }
        public string Telefono { get; set; }
        public int PaisId { get; set; }
        public int CiudadId { get; set; }
    }
}