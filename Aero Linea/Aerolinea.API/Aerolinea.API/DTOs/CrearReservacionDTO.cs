namespace Aerolinea.API.DTOs
{
    // El UsuarioId ya NO va en el body — se lee de la cookie de sesión en el controller
    public class CrearReservacionDTO
    {
        public List<SeleccionVueloDTO> Vuelos { get; set; }
    }

    public class SeleccionVueloDTO
    {
        public int VueloId { get; set; }
        public int ClaseId { get; set; } // 1: Turista, 2: Ejecutivo
        public int CantidadPasajeros { get; set; }
    }
}