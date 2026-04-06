namespace Aerolinea.API.DTOs
{
    public class CrearAgenciaDTO
    {
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public int UsuarioWebID { get; set; }
        public decimal PorcentajeDescuento { get; set; }
    }

    // DTO que usa el usuario Webservice para crear su propia agencia.
    public class CrearAgenciaWebserviceDTO
    {
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
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

    // Vista que se devuelve al Webservice sobre su propia agencia.
    public class MiAgenciaDTO
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public decimal PorcentajeDescuento { get; set; }
        public int EstadoAgenciaID { get; set; }
    }

    // Vista completa para el admin (incluye datos del usuario asignado).
    public class AgenciaAdminDTO
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public int? UsuarioWebID { get; set; }
        public string? UsuarioWebNombre { get; set; }
        public string? UsuarioWebUsername { get; set; }
        public decimal PorcentajeDescuento { get; set; }
        public int EstadoAgenciaID { get; set; }
    }

    // Usuario Webservice disponible para asignar a una agencia.
    public class UsuarioWebserviceDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string Username { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
    }

    // Asignar un usuario Webservice a una agencia existente.
    public class AsignarUsuarioAgenciaDTO
    {
        public int UsuarioWebId { get; set; }
    }

    // Actualizar solo el descuento de una agencia.
    public class ActualizarDescuentoDTO
    {
        public decimal Descuento { get; set; }
    }

    // Actualizar solo el estado de una agencia.
    public class ActualizarEstadoAgenciaDTO
    {
        public int EstadoId { get; set; }
    }
}