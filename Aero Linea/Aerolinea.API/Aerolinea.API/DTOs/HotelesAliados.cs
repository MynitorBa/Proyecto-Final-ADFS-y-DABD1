namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO con la informacion completa de un hotel disponible retornado al frontend.
    /// Incluye los datos del hotel de la API externa y la referencia al aliado
    /// registrado en la base de datos de aerolineas.
    /// </summary>
    public class HotelAliadoDTO
    {
        /// <summary>ID del hotel en el sistema de hoteles aliados.</summary>
        public int Id { get; set; }
        /// <summary>Nombre del hotel en el sistema de hoteles.</summary>
        public string Nombre { get; set; }
        /// <summary>Direccion fisica del hotel.</summary>
        public string Direccion { get; set; }
        /// <summary>Ciudad donde se ubica el hotel.</summary>
        public string Ciudad { get; set; }
        /// <summary>Pais donde se ubica el hotel.</summary>
        public string Pais { get; set; }
        /// <summary>Descripcion general del hotel.</summary>
        public string Descripcion { get; set; }
        /// <summary>Rating del hotel en su propio sistema.</summary>
        public double Rating { get; set; }
        /// <summary>ID del registro HotelAliado en la BD de aerolineas.</summary>
        public int AliadoId { get; set; }
        /// <summary>Nombre del aliado registrado en la BD de aerolineas.</summary>
        public string AliadoNombre { get; set; }
    }

    /// <summary>
    /// DTO interno con los datos de conexion de un hotel aliado registrado en la BD.
    /// Se usa para iterar y llamar dinamicamente a cada API de hotel.
    /// </summary>
    public class HotelAliadoConexionDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Url { get; set; }
        public string TokenHash { get; set; }
    }

    /// <summary>
    /// DTO con los criterios de busqueda de hoteles aliados.
    /// Las fechas deben enviarse en formato YYYY-MM-DD.
    /// </summary>
    public class BusquedaHotelesDTO
    {
        public string Ciudad { get; set; }
        public string Pais { get; set; }
        public string FechaCheckIn { get; set; }
        public string FechaCheckOut { get; set; }
        public int CantidadPersonas { get; set; }
    }

    /// <summary>
    /// DTO que usa el usuario Webservice para registrar su propio hotel aliado.
    /// Solo requiere nombre, URL de la API del hotel y URL para mostrar a los usuarios.
    /// Los tokens se generan automaticamente al establecer la conexion.
    /// </summary>
    public class CrearHotelWebserviceDTO
    {
        // Nombre identificador del hotel aliado en el sistema
        public string Nombre { get; set; } = string.Empty;

        // URL base de la API del hotel para que la aerolinea se comunique con el
        public string Url { get; set; } = string.Empty;

        // URL publica del hotel que se mostrara a los pasajeros
        public string UrlParaUsuario { get; set; } = string.Empty;
    }

    /// <summary>
    /// Vista resumida del hotel aliado devuelta al usuario Webservice propietario.
    /// No incluye los tokens de conexion ya que son de uso interno del sistema.
    /// </summary>
    public class MiHotelDTO
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;

        // ID del estado del aliado segun el catalogo EstadoAliado
        public int EstadoID { get; set; }

        // URL base de la API del hotel registrada al crear
        public string Url { get; set; } = string.Empty;

        // URL publica del hotel para mostrar a los pasajeros
        public string UrlParaUsuario { get; set; } = string.Empty;
    }

    // ── DTOs exclusivos del panel de administracion ───────────────────────────

    /// <summary>
    /// Vista completa de un hotel aliado para el panel de administracion.
    /// Incluye datos del usuario Webservice asignado como nombre y username.
    /// No expone el TokenHASH ya que es informacion interna del sistema.
    /// </summary>
    public class HotelAdminDTO
    {
        public int ID { get; set; }
        public string Nombre { get; set; } = string.Empty;

        // ID de estado del hotel segun catalogo EstadoAliado (1=Activo, 2=Inactivo, 3=Suspendido)
        public int EstadoID { get; set; }

        // URL de la API del hotel
        public string Url { get; set; } = string.Empty;

        // URL publica del hotel para los pasajeros
        public string UrlParaUsuario { get; set; } = string.Empty;

        // ID del usuario Webservice vinculado al hotel; null si no tiene ninguno
        public int? UsuarioWEBIs { get; set; }

        // Nombre del usuario Webservice vinculado; null si no esta asignado
        public string? UsuarioNombre { get; set; }

        // Username del usuario Webservice vinculado; null si no esta asignado
        public string? UsuarioUsername { get; set; }
    }

    /// <summary>
    /// DTO que usa el administrador para crear un hotel aliado y asignarlo
    /// directamente a un usuario Webservice. Requiere nombre, ambas URLs y el ID del usuario.
    /// </summary>
    public class CrearHotelAdminDTO
    {
        public string Nombre { get; set; } = string.Empty;

        // URL base de la API del hotel para la comunicacion con la aerolinea
        public string Url { get; set; } = string.Empty;

        // URL publica del hotel para mostrar a los pasajeros
        public string UrlParaUsuario { get; set; } = string.Empty;

        // ID del usuario Webservice al que se asignara el hotel
        public int UsuarioWEBIs { get; set; }
    }

    /// <summary>
    /// DTO para actualizar unicamente el estado de un hotel aliado desde el panel de administracion.
    /// El EstadoId debe corresponder a un registro valido de la tabla EstadoAliado.
    /// </summary>
    public class ActualizarEstadoHotelDTO
    {
        public int EstadoId { get; set; }
    }

    /// <summary>
    /// DTO para actualizar las URLs de un hotel aliado desde el panel de administracion.
    /// Permite modificar la URL de la API y la URL publica para usuarios en una sola operacion.
    /// </summary>
    public class ActualizarUrlHotelDTO
    {
        // Nueva URL base de la API del hotel
        public string Url { get; set; } = string.Empty;

        // Nueva URL publica del hotel para los pasajeros
        public string UrlParaUsuario { get; set; } = string.Empty;
    }

    /// <summary>
    /// DTO para asignar un usuario Webservice existente a un hotel aliado determinado.
    /// Contiene unicamente el identificador del usuario a vincular.
    /// </summary>
    public class AsignarUsuarioHotelDTO
    {
        public int UsuarioId { get; set; }
    }
}