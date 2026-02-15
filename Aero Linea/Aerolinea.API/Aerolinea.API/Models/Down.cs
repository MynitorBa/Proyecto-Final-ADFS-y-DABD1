namespace Aerolinea.API.Models
{
    public class Down
    {
        public int Id { get; set; }
        public int UsuarioId { get; set; }
        public int ComentarioId { get; set; }
        public int Valor { get; set; } // 1 o -1
        public DateTime FechaVoto { get; set; }
    }
}