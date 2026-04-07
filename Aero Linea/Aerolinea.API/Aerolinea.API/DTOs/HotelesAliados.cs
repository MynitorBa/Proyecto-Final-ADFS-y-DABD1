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
}