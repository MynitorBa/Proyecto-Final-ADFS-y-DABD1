namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para que un administrador cree una nueva agencia y la asigne a un usuario Webservice.
    /// Incluye nombre, correo, ID del usuario web, porcentaje de descuento y URL publica.
    /// </summary>
    public class CrearAgenciaDTO
    {
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public int UsuarioWebID { get; set; }
        public decimal PorcentajeDescuento { get; set; }

        // URL publica de la agencia para que el sistema pueda comunicarse con ella
        public string UrlAgencia { get; set; } = string.Empty;
    }

    /// <summary>
    /// DTO que usa el usuario Webservice para crear su propia agencia.
    /// Solo requiere nombre, correo y URL; el usuario queda vinculado automaticamente.
    /// </summary>
    public class CrearAgenciaWebserviceDTO
    {
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;

        // URL publica de la agencia proporcionada por el propio usuario Webservice
        public string UrlAgencia { get; set; } = string.Empty;
    }

    /// <summary>
    /// DTO de respuesta con la informacion completa de una agencia registrada en el sistema.
    /// Incluye identificador, datos de contacto, usuario web asignado, descuento y estado.
    /// </summary>
    public class AgenciaResponseDTO
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public int UsuarioWebID { get; set; }
        public decimal PorcentajeDescuento { get; set; }
        public int EstadoAgenciaID { get; set; }
        public string UrlAgencia { get; set; } = string.Empty;
    }

    /// <summary>
    /// Vista resumida de la agencia devuelta al usuario Webservice para consultar su propia agencia.
    /// No incluye datos del usuario web asignado.
    /// </summary>
    public class MiAgenciaDTO
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public decimal PorcentajeDescuento { get; set; }
        public int EstadoAgenciaID { get; set; }

        // URL publica registrada por el usuario al crear la agencia
        public string UrlAgencia { get; set; } = string.Empty;
    }

    /// <summary>
    /// Vista completa de una agencia para el panel de administracion.
    /// Incluye datos del usuario Webservice asignado como nombre y username.
    /// </summary>
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
        public string UrlAgencia { get; set; } = string.Empty;
    }

    /// <summary>
    /// DTO que representa un usuario con rol Webservice disponible para ser asignado a una
    /// agencia o hotel. Solo incluye usuarios que no tienen ninguna entidad asignada aun.
    /// </summary>
    public class UsuarioWebserviceDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public string Username { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
    }

    /// <summary>
    /// DTO para asignar un usuario Webservice existente a una agencia determinada.
    /// </summary>
    public class AsignarUsuarioAgenciaDTO
    {
        public int UsuarioWebId { get; set; }
    }

    /// <summary>
    /// DTO para actualizar unicamente el porcentaje de descuento de una agencia.
    /// </summary>
    public class ActualizarDescuentoDTO
    {
        public decimal Descuento { get; set; }
    }

    /// <summary>
    /// DTO para actualizar unicamente el estado de una agencia registrada en el sistema.
    /// </summary>
    public class ActualizarEstadoAgenciaDTO
    {
        public int EstadoId { get; set; }
    }

    /// <summary>
    /// DTO para actualizar unicamente la URL publica de una agencia desde el panel de administracion.
    /// </summary>
    public class ActualizarUrlAgenciaDTO
    {
        public string UrlAgencia { get; set; } = string.Empty;
    }
}