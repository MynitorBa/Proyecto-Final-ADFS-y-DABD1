namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para registrar o actualizar el voto de un usuario sobre un comentario.
    /// El campo Valor debe ser 1 para voto positivo o -1 para voto negativo.
    /// </summary>
    public class VotarComentarioDTO
    {
        public int ComentarioId { get; set; }
        public int Valor { get; set; }  // 1 o -1
    }

    /// <summary>
    /// DTO de respuesta tras procesar un voto sobre un comentario.
    /// Indica la accion ejecutada, el nuevo conteo de votos y los valores anterior y nuevo del voto.
    /// </summary>
    public class ResultadoVotoDTO
    {
        public int ComentarioId { get; set; }
        public int NuevosDowns { get; set; }
        public string Accion { get; set; }  // votado | voto_cambiado | voto_eliminado
        public int? ValorAnterior { get; set; }
        public int? ValorNuevo { get; set; }
    }
}
