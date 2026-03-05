namespace Aerolinea.API.DTOs
{
    public class BusquedasPorDiaDTO
    {
        public string Fecha { get; set; } = "";
        public int Total { get; set; }
    }

    public class RutaMasBuscadaDTO
    {
        public string Ruta { get; set; } = "";
        public string OrigenCodigo { get; set; } = "";
        public string DestinoCodigo { get; set; } = "";
        public int Total { get; set; }
    }

    public class BusquedasPorTipoDTO
    {
        public string Tipo { get; set; } = "";
        public int Total { get; set; }
    }

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

    public class MetricasFiltroDTO
    {
        public string? FechaDesde { get; set; }
        public string? FechaHasta { get; set; }
        public string? Tipo { get; set; }       // "Web", "REST", o null (ambos)
        public string? Usuario { get; set; }    // username parcial
        public int Pagina { get; set; } = 1;
        public int TamañoPagina { get; set; } = 25;
    }


    public class IngresosKpiDTO
    {
        public decimal IngresosTotales { get; set; }
        public decimal IngresosTurista { get; set; }
        public decimal IngresosEjecutivo { get; set; }
        public int TotalBoletos { get; set; }
        public int TotalReservaciones { get; set; }
        public decimal TicketPromedio { get; set; }
    }

    public class DistribucionClaseDTO
    {
        public string Clase { get; set; } = "";
        public decimal Ingresos { get; set; }
        public int Boletos { get; set; }
    }
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

    public class ListadoBusquedasDTO
    {
        public List<BusquedaDetalleDTO> Registros { get; set; } = new();
        public int TotalRegistros { get; set; }
        public int TotalPaginas { get; set; }
        public int PaginaActual { get; set; }
    }
}