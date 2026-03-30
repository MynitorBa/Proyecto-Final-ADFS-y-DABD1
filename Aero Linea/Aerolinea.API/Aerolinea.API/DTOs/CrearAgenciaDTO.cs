namespace Aerolinea.API.DTOs
{
    public class CrearAgenciaDTO
    {
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public int UsuarioWebID { get; set; }
        public decimal PorcentajeDescuento { get; set; }
    }

    public class AgenciaResponseDTO
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public int UsuarioWebID { get; set; }
        public decimal PorcentajeDescuento { get; set; }
        public int EstadoAgenciaID { get; set; }
    }
}