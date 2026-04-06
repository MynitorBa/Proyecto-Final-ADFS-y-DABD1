namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para crear una nueva reservacion de vuelo.
    /// El UsuarioId se obtiene de la cookie de sesion en el controller.
    /// Contiene la lista de vuelos seleccionados con clase y cantidad de pasajeros.
    /// </summary>
    public class CrearReservacionDTO
    {
        public List<SeleccionVueloDTO> Vuelos { get; set; }
    }

    /// <summary>
    /// DTO que representa la seleccion de un vuelo dentro de una reservacion.
    /// Especifica el vuelo elegido, la clase deseada y la cantidad de pasajeros.
    /// </summary>
    public class SeleccionVueloDTO
    {
        public int VueloId { get; set; }
        public int ClaseId { get; set; } // 1: Turista, 2: Ejecutivo
        public int CantidadPasajeros { get; set; }
    }
}
