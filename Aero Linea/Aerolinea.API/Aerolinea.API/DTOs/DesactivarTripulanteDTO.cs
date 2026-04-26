namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para desactivar un tripulante. Incluye el nuevo estado y,
    /// cuando hay vuelos futuros afectados, la lista de reemplazos por vuelo
    /// para garantizar que cada vuelo mantenga la composicion minima de 1 piloto,
    /// 1 copiloto y 3 auxiliares antes de confirmar la desactivacion.
    /// </summary>
    public class DesactivarTripulanteDTO
    {
        public bool Activo { get; set; }

        /// <summary>
        /// Lista de asignaciones de reemplazo por vuelo. Requerida cuando Activo = false
        /// y el tripulante tiene vuelos futuros asignados con mas de 48 horas de anticipacion.
        /// </summary>
        public List<ReemplazoVueloDTO>? Reemplazos { get; set; }
    }

    /// <summary>
    /// Especifica que tripulantes nuevos se asignaran a un vuelo especifico
    /// para cubrir al tripulante que sera desactivado.
    /// </summary>
    public class ReemplazoVueloDTO
    {
        public int VueloId { get; set; }
        public List<int> NuevosTripulantesIds { get; set; } = new();
    }
}
