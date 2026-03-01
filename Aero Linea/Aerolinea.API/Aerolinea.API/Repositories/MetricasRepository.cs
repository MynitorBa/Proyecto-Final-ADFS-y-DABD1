using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class MetricasRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public MetricasRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // ── Gráfica 1: Búsquedas por día (últimos N días) ────────────────────
        public async Task<List<BusquedasPorDiaDTO>> ObtenerBusquedasPorDia(
            DateTime? desde, DateTime? hasta)
        {
            var fechaDesde = desde ?? DateTime.Now.AddDays(-30);
            var fechaHasta = hasta ?? DateTime.Now;

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            string query = @"
                SELECT 
                    CONVERT(varchar(10), b.Fecha, 23) AS Fecha,
                    COUNT(*) AS Total
                FROM Busqueda b
                WHERE b.Fecha >= @FechaDesde AND b.Fecha <= @FechaHasta
                GROUP BY CONVERT(varchar(10), b.Fecha, 23)
                ORDER BY Fecha ASC";

            using var cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@FechaDesde", fechaDesde.Date);
            cmd.Parameters.AddWithValue("@FechaHasta", fechaHasta.Date.AddDays(1).AddSeconds(-1));

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<BusquedasPorDiaDTO>();
            while (await reader.ReadAsync())
            {
                lista.Add(new BusquedasPorDiaDTO
                {
                    Fecha = reader.GetString(0),
                    Total = reader.GetInt32(1)
                });
            }
            return lista;
        }

        // ── Gráfica 2: Top 10 rutas más buscadas ─────────────────────────────
        public async Task<List<RutaMasBuscadaDTO>> ObtenerRutasMasBuscadas(
            DateTime? desde, DateTime? hasta, string? tipo)
        {
            var fechaDesde = desde ?? DateTime.Now.AddDays(-30);
            var fechaHasta = hasta ?? DateTime.Now;

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            string filtroTipo = tipo switch
            {
                "Web" => "AND tb.Tipo = 'Web'",
                "REST" => "AND tb.Tipo = 'REST'",
                _ => ""
            };

            string query = $@"
                SELECT TOP 10
                    ao.Codigo AS OrigenCodigo,
                    ad.Codigo AS DestinoCodigo,
                    co.Nombre + ' (' + ao.Codigo + ') → ' + cd.Nombre + ' (' + ad.Codigo + ')' AS Ruta,
                    COUNT(*) AS Total
                FROM Busqueda b
                INNER JOIN Ruta        r  ON b.RutaID          = r.ID
                INNER JOIN TipoBusqueda tb ON b.TipoBusquedaID = tb.ID
                INNER JOIN Aeropuerto  ao ON r.OrigenID         = ao.ID
                INNER JOIN Aeropuerto  ad ON r.DestinoID        = ad.ID
                INNER JOIN Ciudad      co ON ao.CiudadID        = co.ID
                INNER JOIN Ciudad      cd ON ad.CiudadID        = cd.ID
                WHERE b.Fecha >= @FechaDesde AND b.Fecha <= @FechaHasta
                {filtroTipo}
                GROUP BY ao.Codigo, ad.Codigo, co.Nombre, ao.Codigo, cd.Nombre, ad.Codigo
                ORDER BY Total DESC";

            using var cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@FechaDesde", fechaDesde.Date);
            cmd.Parameters.AddWithValue("@FechaHasta", fechaHasta.Date.AddDays(1).AddSeconds(-1));

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<RutaMasBuscadaDTO>();
            while (await reader.ReadAsync())
            {
                lista.Add(new RutaMasBuscadaDTO
                {
                    OrigenCodigo = reader.GetString(0),
                    DestinoCodigo = reader.GetString(1),
                    Ruta = reader.GetString(2),
                    Total = reader.GetInt32(3)
                });
            }
            return lista;
        }

        // ── Gráfica 3: Proporción Web vs REST ────────────────────────────────
        public async Task<List<BusquedasPorTipoDTO>> ObtenerBusquedasPorTipo(
            DateTime? desde, DateTime? hasta)
        {
            var fechaDesde = desde ?? DateTime.Now.AddDays(-30);
            var fechaHasta = hasta ?? DateTime.Now;

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            string query = @"
                SELECT tb.Tipo, COUNT(*) AS Total
                FROM Busqueda b
                INNER JOIN TipoBusqueda tb ON b.TipoBusquedaID = tb.ID
                WHERE b.Fecha >= @FechaDesde AND b.Fecha <= @FechaHasta
                GROUP BY tb.Tipo";

            using var cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@FechaDesde", fechaDesde.Date);
            cmd.Parameters.AddWithValue("@FechaHasta", fechaHasta.Date.AddDays(1).AddSeconds(-1));

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<BusquedasPorTipoDTO>();
            while (await reader.ReadAsync())
            {
                lista.Add(new BusquedasPorTipoDTO
                {
                    Tipo = reader.GetString(0),
                    Total = reader.GetInt32(1)
                });
            }
            return lista;
        }

        // ── Resumen general (KPIs) ────────────────────────────────────────────
        public async Task<MetricasResumenDTO> ObtenerResumen(DateTime? desde, DateTime? hasta)
        {
            var porDia = await ObtenerBusquedasPorDia(desde, hasta);
            var rutas = await ObtenerRutasMasBuscadas(desde, hasta, null);
            var porTipo = await ObtenerBusquedasPorTipo(desde, hasta);

            int totalWeb = porTipo.FirstOrDefault(t => t.Tipo == "Web")?.Total ?? 0;
            int totalRest = porTipo.FirstOrDefault(t => t.Tipo == "REST")?.Total ?? 0;

            return new MetricasResumenDTO
            {
                BusquedasPorDia = porDia,
                RutasMasBuscadas = rutas,
                BusquedasPorTipo = porTipo,
                TotalBusquedas = totalWeb + totalRest,
                TotalBusquedasWeb = totalWeb,
                TotalBusquedasRest = totalRest
            };
        }

        // ── Listado paginado con filtros (para exportar / ver tabla) ──────────
        public async Task<ListadoBusquedasDTO> ObtenerListado(MetricasFiltroDTO filtro)
        {
            var fechaDesde = filtro.FechaDesde != null
                ? DateTime.Parse(filtro.FechaDesde)
                : DateTime.Now.AddDays(-30);
            var fechaHasta = filtro.FechaHasta != null
                ? DateTime.Parse(filtro.FechaHasta)
                : DateTime.Now;

            string filtroTipo = filtro.Tipo switch
            {
                "Web" => "AND tb.Tipo = 'Web'",
                "REST" => "AND tb.Tipo = 'REST'",
                _ => ""
            };

            string filtroUsuario = string.IsNullOrWhiteSpace(filtro.Usuario)
                ? ""
                : "AND (u.Username LIKE @Usuario OR u.Correo LIKE @Usuario)";

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            // COUNT total para paginado
            string queryCount = $@"
                SELECT COUNT(*)
                FROM Busqueda b
                INNER JOIN TipoBusqueda tb ON b.TipoBusquedaID = tb.ID
                LEFT  JOIN Usuario      u  ON b.UsuarioID       = u.Id
                WHERE b.Fecha >= @FechaDesde AND b.Fecha <= @FechaHasta
                {filtroTipo}
                {filtroUsuario}";

            using var cmdCount = new SqlCommand(queryCount, conn);
            cmdCount.Parameters.AddWithValue("@FechaDesde", fechaDesde.Date);
            cmdCount.Parameters.AddWithValue("@FechaHasta", fechaHasta.Date.AddDays(1).AddSeconds(-1));
            if (!string.IsNullOrWhiteSpace(filtro.Usuario))
                cmdCount.Parameters.AddWithValue("@Usuario", $"%{filtro.Usuario}%");

            int total = (int)(await cmdCount.ExecuteScalarAsync() ?? 0);

            int tamPagina = filtro.TamañoPagina > 0 ? filtro.TamañoPagina : 25;
            int pagina = filtro.Pagina > 0 ? filtro.Pagina : 1;
            int offset = (pagina - 1) * tamPagina;
            int paginas = (int)Math.Ceiling((double)total / tamPagina);

            // Datos paginados
            string queryData = $@"
                SELECT 
                    b.ID,
                    ao.Nombre AS Origen, ao.Codigo AS OrigenCodigo,
                    ad.Nombre AS Destino, ad.Codigo AS DestinoCodigo,
                    CONVERT(varchar(10), b.FechaSalida, 23) AS FechaSalida,
                    b.CantidadPersonas,
                    u.Username,
                    tb.Tipo,
                    CONVERT(varchar(19), b.Fecha, 120) AS FechaBusqueda
                FROM Busqueda b
                INNER JOIN Ruta         r  ON b.RutaID          = r.ID
                INNER JOIN TipoBusqueda tb ON b.TipoBusquedaID  = tb.ID
                INNER JOIN Aeropuerto   ao ON r.OrigenID         = ao.ID
                INNER JOIN Aeropuerto   ad ON r.DestinoID        = ad.ID
                LEFT  JOIN Usuario      u  ON b.UsuarioID        = u.Id
                WHERE b.Fecha >= @FechaDesde AND b.Fecha <= @FechaHasta
                {filtroTipo}
                {filtroUsuario}
                ORDER BY b.Fecha DESC
                OFFSET @Offset ROWS FETCH NEXT @TamPagina ROWS ONLY";

            using var cmd = new SqlCommand(queryData, conn);
            cmd.Parameters.AddWithValue("@FechaDesde", fechaDesde.Date);
            cmd.Parameters.AddWithValue("@FechaHasta", fechaHasta.Date.AddDays(1).AddSeconds(-1));
            cmd.Parameters.AddWithValue("@Offset", offset);
            cmd.Parameters.AddWithValue("@TamPagina", tamPagina);
            if (!string.IsNullOrWhiteSpace(filtro.Usuario))
                cmd.Parameters.AddWithValue("@Usuario", $"%{filtro.Usuario}%");

            using var reader = await cmd.ExecuteReaderAsync();
            var registros = new List<BusquedaDetalleDTO>();
            while (await reader.ReadAsync())
            {
                registros.Add(new BusquedaDetalleDTO
                {
                    Id = reader.GetInt32(0),
                    Origen = reader.GetString(1),
                    OrigenCodigo = reader.GetString(2),
                    Destino = reader.GetString(3),
                    DestinoCodigo = reader.GetString(4),
                    FechaSalida = reader.GetString(5),
                    CantidadPersonas = reader.GetInt32(6),
                    Usuario = reader.IsDBNull(7) ? null : reader.GetString(7),
                    Tipo = reader.GetString(8),
                    FechaBusqueda = reader.GetString(9)
                });
            }

            return new ListadoBusquedasDTO
            {
                Registros = registros,
                TotalRegistros = total,
                TotalPaginas = paginas,
                PaginaActual = pagina
            };
        }
    }
}