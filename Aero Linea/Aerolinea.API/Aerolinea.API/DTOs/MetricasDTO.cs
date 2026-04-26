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

    // ── Análisis de negocio ────────────────────────────────────────────────────

    /// <summary>Embudo: búsquedas → reservaciones → pagadas / expiradas / canceladas.</summary>
    public class EmbudoNegocioDTO
    {
        public int Busquedas     { get; set; }
        public int Reservaciones { get; set; }
        public int Pagadas       { get; set; }
        public int Canceladas    { get; set; }
        public int Expiradas     { get; set; }
        public int Pendientes    { get; set; }
        public int Completadas   { get; set; }
    }

    /// <summary>Revenue, boletos y búsquedas de una ruta en el período.</summary>
    public class RutaRendimientoDTO
    {
        public string  OrigenCodigo        { get; set; } = "";
        public string  DestinoCodigo       { get; set; } = "";
        public string  Ruta                { get; set; } = "";
        public int     TotalReservaciones  { get; set; }
        public decimal RevenueTotal        { get; set; }
        public int     BoletosVendidos     { get; set; }
        public int     Busquedas           { get; set; }
    }

    public class CancelacionPorRutaDTO
    {
        public string OrigenCodigo  { get; set; } = "";
        public string DestinoCodigo { get; set; } = "";
        public int    Total         { get; set; }
    }

    /// <summary>Reservación cancelada que tenía escala, con la cadena de rutas recorridas.</summary>
    public class ReservacionEscalaDTO
    {
        public int    Id   { get; set; }
        public string Ruta { get; set; } = "";   // ej. "GUA → MEX → MAD"
    }

    public class CancelacionPorTipoDTO
    {
        public string Tipo  { get; set; } = "";
        public int    Total { get; set; }
    }

    public class CancelacionPorAnticipacionDTO
    {
        public string Bucket { get; set; } = "";
        public int    Total  { get; set; }
    }

    public class CancelacionesAnalisisDTO
    {
        public List<CancelacionPorRutaDTO>         PorRuta              { get; set; } = new();
        public List<CancelacionPorTipoDTO>         PorTipo              { get; set; } = new();
        public List<CancelacionPorAnticipacionDTO> PorAnticipacion      { get; set; } = new();
        /// <summary>Reservaciones canceladas que tenían vuelos con escala, con su cadena de rutas.</summary>
        public List<ReservacionEscalaDTO>          ReservacionesConEscala { get; set; } = new();
    }

    /// <summary>Revenue mensual por clase (Turista / Ejecutivo).</summary>
    public class IngresosMensualDTO
    {
        public string  Mes           { get; set; } = "";
        public string  Clase         { get; set; } = "";
        public decimal Revenue       { get; set; }
        public int     Reservaciones { get; set; }
    }

    /// <summary>Celda del mapa de calor: día de semana × hora de salida → ocupación %.</summary>
    public class HeatmapCeldaDTO
    {
        public int    DiaSemana         { get; set; }  // SQL WEEKDAY: 1=Dom, 2=Lun ... 7=Sáb
        public int    Hora              { get; set; }
        public double OcupacionPct      { get; set; }
        public int    AsientosVendidos  { get; set; }
        public double CapacidadPromedio { get; set; }
    }

    /// <summary>Respuesta consolidada de los 5 gráficos de análisis de negocio.</summary>
    public class NegocioMetricasDTO
    {
        public EmbudoNegocioDTO         Embudo           { get; set; } = new();
        public List<RutaRendimientoDTO> RutasRendimiento { get; set; } = new();
        public CancelacionesAnalisisDTO Cancelaciones    { get; set; } = new();
        public List<IngresosMensualDTO> IngresosTendencia { get; set; } = new();
        public List<HeatmapCeldaDTO>    Heatmap          { get; set; } = new();
    }
}
