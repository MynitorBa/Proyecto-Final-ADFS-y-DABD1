namespace Aerolinea.API.DTOs
{
    // ── POST api/votos ────────────────────────────────────────────────────
    public class VotarComentarioDTO
    {
        public int ComentarioId { get; set; }
        public int Valor { get; set; }  // 1 o -1
    }

    // ── Respuesta de votos 
    public class ResultadoVotoDTO
    {
        public int ComentarioId { get; set; }
        public int NuevosDowns { get; set; }
        public string Accion { get; set; }  // votado | voto_cambiado | voto_eliminado
        public int? ValorAnterior { get; set; }
        public int? ValorNuevo { get; set; }
    }
}