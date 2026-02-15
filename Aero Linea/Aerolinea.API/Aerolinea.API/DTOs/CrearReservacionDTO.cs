namespace Aerolinea.API.DTOs
{
    public class CrearReservacionDTO
    {
        public int UsuarioId { get; set; }
        public List<SeleccionVueloDTO> Vuelos { get; set; }
    }

    public class SeleccionVueloDTO
    {
        public int VueloId { get; set; }
        public int ClaseId { get; set; } // 1: Turista, 2: Ejecutivo
        public int CantidadPasajeros { get; set; }
    }
}