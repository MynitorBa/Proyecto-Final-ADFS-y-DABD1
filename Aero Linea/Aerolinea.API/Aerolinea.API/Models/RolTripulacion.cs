namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que define un rol o cargo dentro de la tripulacion de un vuelo.
    /// Ejemplos de cargos: Piloto, Copiloto, Auxiliar de vuelo.
    /// </summary>
    public class RolTripulacion
    {
        public int Id { get; set; }
        public string Cargo { get; set; }
    }
}
