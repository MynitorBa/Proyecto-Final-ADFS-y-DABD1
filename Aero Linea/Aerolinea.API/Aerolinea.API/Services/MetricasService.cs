using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de metricas del sistema. Provee datos estadisticos sobre busquedas de vuelos,
    /// rutas mas solicitadas y distribucion por tipo, con soporte para filtros de fecha.
    /// Tambien permite exportar el listado completo sin paginacion.
    /// </summary>
    public class MetricasService
    {
        private readonly MetricasRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de metricas.
        /// </summary>
        public MetricasService(MetricasRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Retorna un resumen de las metricas del sistema en el rango de fechas indicado.
        /// Incluye totales de busquedas, usuarios activos y reservaciones generadas.
        /// </summary>
        public async Task<MetricasResumenDTO> ObtenerResumen(string? fechaDesde, string? fechaHasta)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;
            return await _repository.ObtenerResumen(desde, hasta);
        }

        /// <summary>
        /// Retorna la cantidad de busquedas realizadas por dia en el rango de fechas indicado.
        /// Util para graficar la evolucion de la demanda a lo largo del tiempo.
        /// </summary>
        public async Task<List<BusquedasPorDiaDTO>> ObtenerBusquedasPorDia(
            string? fechaDesde, string? fechaHasta)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;
            return await _repository.ObtenerBusquedasPorDia(desde, hasta);
        }

        /// <summary>
        /// Retorna el listado de rutas con mayor cantidad de busquedas en el rango de fechas,
        /// filtrado opcionalmente por tipo de busqueda (directo, con escala, etc.).
        /// </summary>
        public async Task<List<RutaMasBuscadaDTO>> ObtenerRutasMasBuscadas(
            string? fechaDesde, string? fechaHasta, string? tipo)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;
            return await _repository.ObtenerRutasMasBuscadas(desde, hasta, tipo);
        }

        /// <summary>
        /// Retorna la distribucion de busquedas agrupadas por tipo en el rango de fechas indicado.
        /// Permite identificar que tipo de viaje es mas demandado por los usuarios.
        /// </summary>
        public async Task<List<BusquedasPorTipoDTO>> ObtenerBusquedasPorTipo(
            string? fechaDesde, string? fechaHasta)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;
            return await _repository.ObtenerBusquedasPorTipo(desde, hasta);
        }

        /// <summary>
        /// Retorna un listado paginado de busquedas aplicando los filtros del objeto de filtro recibido.
        /// Incluye informacion de usuario, fechas y parametros de cada busqueda.
        /// </summary>
        public async Task<ListadoBusquedasDTO> ObtenerListado(MetricasFiltroDTO filtro)
        {
            return await _repository.ObtenerListado(filtro);
        }

        /// <summary>
        /// Retorna los 5 graficos de analisis de negocio consolidados en un solo objeto.
        /// Incluye embudo de conversion, rendimiento de rutas, cancelaciones, tendencia mensual
        /// de ingresos por clase y mapa de calor de ocupacion por dia y hora de salida.
        /// </summary>
        public async Task<NegocioMetricasDTO> ObtenerNegocio(string? fechaDesde, string? fechaHasta)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;

            var embudo           = await _repository.ObtenerEmbudo(desde, hasta);
            var rutasRendimiento = await _repository.ObtenerRutasRendimiento(desde, hasta);
            var cancelaciones    = await _repository.ObtenerCancelaciones(desde, hasta);
            var ingresosTend     = await _repository.ObtenerIngresosTendencia(desde, hasta);
            var heatmap          = await _repository.ObtenerHeatmap(desde, hasta);

            return new NegocioMetricasDTO
            {
                Embudo            = embudo,
                RutasRendimiento  = rutasRendimiento,
                Cancelaciones     = cancelaciones,
                IngresosTendencia = ingresosTend,
                Heatmap           = heatmap
            };
        }

        /// <summary>
        /// Retorna el listado completo de busquedas sin paginacion, aplicando solo los filtros
        /// de fecha, tipo y usuario. Pensado para exportacion de datos en reportes.
        /// </summary>
        public async Task<ListadoBusquedasDTO> ObtenerListadoCompleto(MetricasFiltroDTO filtro)
        {
            var sinPaginado = new MetricasFiltroDTO
            {
                FechaDesde = filtro.FechaDesde,
                FechaHasta = filtro.FechaHasta,
                Tipo = filtro.Tipo,
                Usuario = filtro.Usuario,
                Pagina = 1,
                TamañoPagina = 9999   // sin límite práctico
            };
            return await _repository.ObtenerListado(sinPaginado);
        }
    }
}
