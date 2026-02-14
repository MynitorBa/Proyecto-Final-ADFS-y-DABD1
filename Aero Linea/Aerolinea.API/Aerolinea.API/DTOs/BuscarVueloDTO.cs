namespace Aerolinea.API.DTOs
{
    public class BuscarVueloDTO
    {
        public int OrigenId { get; set; }
        public int DestinoId { get; set; }
        public DateTime Fecha { get; set; }
        public int CantidadPasajeros { get; set; } = 1; 
    }
}