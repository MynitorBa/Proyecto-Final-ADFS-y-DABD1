namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO que agrupa la cantidad total de busquedas realizadas en un dia especifico.
    /// Utilizado en graficas y reportes del panel de metricas administrativo.
    /// </summary>
    public class BusquedasPorDiaDTO
    {
        public string Fecha { get; set; } = "";
        public int Total { get; set; }
    }

    /// <summary>
    /// DTO que representa una ruta con su conteo de busquedas, usada para identificar
    /// las rutas mas populares del sistema.
    /// </summary>
    public class RutaMasBuscadaDTO
    {
        public string Ruta { get; set; } = "";
        public string OrigenCodigo { get; set; } = "";
        public string DestinoCodigo { get; set; } = "";
        public int Total { get; set; }
    }

    /// <summary>
    /// DTO que clasifica el total de busquedas segun el tipo de canal utilizado (Web o REST).
    /// </summary>
    public class BusquedasPorTipoDTO
    {
        public string Tipo { get; set; } = "";
        public int Total { get; set; }
    }

    /// <summary>
    /// DTO con el detalle completo de una busqueda de vuelo registrada en el historial.
    /// Incluye origen, destino, fechas, cantidad de personas, usuario y canal de busqueda.
    /// </summary>
    public class BusquedaDetalleDTO
    {
        public int Id { get; set; }
        public string Origen { get; set; } = "";
        public string Destino { get; set; } = "";
        public string OrigenCodigo { get; set; } = "";
        public string DestinoCodigo { get; set; } = "";
        public string FechaSalida { get; set; } = "";
        public int CantidadPersonas { get; set; }
        public string? Usuario { get; set; }
        public string Tipo { get; set; } = "";
        public string FechaBusqueda { get; set; } = "";
    }

    /// <summary>
    /// DTO de filtros para consultar el listado paginado de busquedas en el panel de metricas.
    /// Permite filtrar por rango de fechas, tipo de canal, username parcial y pagina.
    /// </summary>
    public class MetricasFiltroDTO
    {
        public string? FechaDesde { get; set; }
        public string? FechaHasta { get; set; }
        public string? Tipo { get; set; }       // "Web", "REST", o null (ambos)
        public string? Usuario { get; set; }    // username parcial
        public int Pagina { get; set; } = 1;
        public int TamañoPagina { get; set; } = 25;
    }


    /// <summary>
    /// DTO con los indicadores clave de ingresos del sistema.
    /// Incluye totales por clase, cantidad de boletos, reservaciones y ticket promedio.
    /// </summary>
    public class IngresosKpiDTO
    {
        public decimal IngresosTotales { get; set; }
        public decimal IngresosTurista { get; set; }
        public decimal IngresosEjecutivo { get; set; }
        public int TotalBoletos { get; set; }
        public int TotalReservaciones { get; set; }
        public decimal TicketPromedio { get; set; }
    }

    /// <summary>
    /// DTO que muestra la distribucion de ingresos y boletos vendidos por clase de vuelo.
    /// </summary>
    public class DistribucionClaseDTO
    {
        public string Clase { get; set; } = "";
        public decimal Ingresos { get; set; }
        public int Boletos { get; set; }
    }

    /// <summary>
    /// DTO de resumen general de metricas del sistema para el panel administrativo.
    /// Consolida busquedas por dia, rutas populares, tipos de canal, KPIs de ingresos
    /// y distribucion por clase en una sola respuesta.
    /// </summary>
    public class MetricasResumenDTO
    {
        public List<BusquedasPorDiaDTO> BusquedasPorDia { get; set; } = new();
        public List<RutaMasBuscadaDTO> RutasMasBuscadas { get; set; } = new();
        public List<BusquedasPorTipoDTO> BusquedasPorTipo { get; set; } = new();
        public int TotalBusquedas { get; set; }
        public int TotalBusquedasWeb { get; set; }
        public int TotalBusquedasRest { get; set; }
        public IngresosKpiDTO IngresosKpi { get; set; } = new();
        public List<DistribucionClaseDTO> DistribucionClase { get; set; } = new();
    }

    /// <summary>
    /// DTO de respuesta paginada con el listado detallado de busquedas registradas en el sistema.
    /// Incluye los registros de la pagina actual, totales y numero de paginas disponibles.
    /// </summary>
    public class ListadoBusquedasDTO
    {
        public List<BusquedaDetalleDTO> Registros { get; set; } = new();
        public int TotalRegistros { get; set; }
        public int TotalPaginas { get; set; }
        public int PaginaActual { get; set; }
    }
}
