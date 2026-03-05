using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class MetricasService
    {
        private readonly MetricasRepository _repository;

        public MetricasService(MetricasRepository repository)
        {
            _repository = repository;
        }

        public async Task<MetricasResumenDTO> ObtenerResumen(string? fechaDesde, string? fechaHasta)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;
            return await _repository.ObtenerResumen(desde, hasta);
        }

        public async Task<List<BusquedasPorDiaDTO>> ObtenerBusquedasPorDia(
            string? fechaDesde, string? fechaHasta)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;
            return await _repository.ObtenerBusquedasPorDia(desde, hasta);
        }

        public async Task<List<RutaMasBuscadaDTO>> ObtenerRutasMasBuscadas(
            string? fechaDesde, string? fechaHasta, string? tipo)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;
            return await _repository.ObtenerRutasMasBuscadas(desde, hasta, tipo);
        }

        public async Task<List<BusquedasPorTipoDTO>> ObtenerBusquedasPorTipo(
            string? fechaDesde, string? fechaHasta)
        {
            DateTime? desde = fechaDesde != null ? DateTime.Parse(fechaDesde) : null;
            DateTime? hasta = fechaHasta != null ? DateTime.Parse(fechaHasta) : null;
            return await _repository.ObtenerBusquedasPorTipo(desde, hasta);
        }

        public async Task<ListadoBusquedasDTO> ObtenerListado(MetricasFiltroDTO filtro)
        {
            return await _repository.ObtenerListado(filtro);
        }

        // Obtiene TODO el listado sin paginado (para exportar)
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