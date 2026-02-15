namespace Aerolinea.API.DTOs
{
    public class VotarComentarioDTO
    {
        public int UsuarioId { get; set; }
        public int ComentarioId { get; set; }
        public int Valor { get; set; } // 1 para upvote, -1 para downvote
    }

    public class ResultadoVotoDTO
    {
        public int ComentarioId { get; set; }
        public int NuevosDowns { get; set; }
        public string Accion { get; set; } // "votado", "voto_cambiado", "voto_eliminado"
        public int? ValorAnterior { get; set; }
        public int? ValorNuevo { get; set; }
    }
}