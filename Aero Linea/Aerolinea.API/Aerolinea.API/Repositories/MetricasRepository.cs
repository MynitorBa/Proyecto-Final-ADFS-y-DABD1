using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de metricas del sistema. Provee datos estadisticos sobre busquedas
    /// de vuelos, rutas mas buscadas, distribucion por tipo de acceso (Web/REST),
    /// ingresos reales desde facturas y listados paginados para exportacion o analisis.
    /// </summary>
    public class MetricasRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public MetricasRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // ── Gráfica 1: Búsquedas por día (últimos N días) ────────────────────
        /// <summary>
        /// Retorna el conteo de busquedas agrupadas por dia dentro del rango de fechas
        /// indicado. Si no se indica rango se usan los ultimos 30 dias. Ordenado ascendente.
        /// </summary>
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
        /// <summary>
        /// Retorna las 10 rutas mas buscadas en el rango de fechas indicado.
        /// Opcionalmente filtra por tipo de acceso (Web o REST).
        /// Incluye codigos y nombres de ciudad de origen y destino.
        /// </summary>
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
        /// <summary>
        /// Retorna la cantidad de busquedas agrupadas por tipo (Web y REST) en el
        /// rango de fechas indicado. Se usa para graficar la proporcion de cada canal.
        /// </summary>
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


        // ── Ingresos reales desde Factura ────────────────────────────────────
        // Factura.Total = lo que el usuario realmente pagó en checkout.
        // Filtra por fecha de la factura (fecha de compra), no por fecha del vuelo.
        /// <summary>
        /// Calcula los ingresos reales del periodo a partir de las facturas emitidas.
        /// Retorna un KPI con totales y ticket promedio, y una distribucion de ingresos
        /// y boletos por clase (Turista y Ejecutiva). Excluye reservaciones canceladas.
        /// </summary>
        public async Task<(IngresosKpiDTO kpi, List<DistribucionClaseDTO> dist)> ObtenerIngresos(
            DateTime? desde, DateTime? hasta)
        {
            var fechaDesde = desde ?? DateTime.Now.AddDays(-30);
            var fechaHasta = hasta ?? DateTime.Now;

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            // ── 1. Total de ingresos y reservaciones pagadas ─────────────────────
            string queryFacturas = @"
                SELECT
                    ISNULL(SUM(f.Total), 0) AS TotalIngresos,
                    COUNT(*)                AS TotalFacturas
                FROM Factura f
                INNER JOIN Reservacion r ON r.ID = f.ReservacionID
                WHERE f.Fecha >= @FechaDesde
                  AND f.Fecha <= @FechaHasta
                  AND r.EstadoReservaID <> 3";

            decimal totalIngresos = 0;
            int totalReservaciones = 0;
            using (var cmd = new SqlCommand(queryFacturas, conn))
            {
                cmd.Parameters.AddWithValue("@FechaDesde", fechaDesde.Date);
                cmd.Parameters.AddWithValue("@FechaHasta", fechaHasta.Date.AddDays(1).AddSeconds(-1));
                using var r = await cmd.ExecuteReaderAsync();
                if (await r.ReadAsync())
                {
                    totalIngresos = r.GetDecimal(0);
                    totalReservaciones = r.GetInt32(1);
                }
            }

            // ── 2. Desglose por clase: suma Boleto.Precio de los boletos comprados ─
            // Filtramos por fecha de Factura para que coincida con el periodo
            string queryDist = @"
                SELECT
                    c.TipoDeClase                  AS Clase,
                    ISNULL(SUM(b.Precio), 0)       AS Ingresos,
                    COUNT(*)                        AS Boletos
                FROM   Boleto     b
                INNER JOIN Clase       c  ON c.ID  = b.ClaseID
                INNER JOIN Reservacion rv ON rv.ID = b.ReservacionID
                INNER JOIN Factura     f  ON f.ReservacionID = rv.ID
                WHERE  f.Fecha >= @FechaDesde
                  AND  f.Fecha <= @FechaHasta
                  AND  rv.EstadoReservaID <> 3
                  AND  b.EstadoBoletoID IN (2, 3)
                GROUP BY c.TipoDeClase";

            var dist = new List<DistribucionClaseDTO>();
            using (var cmd = new SqlCommand(queryDist, conn))
            {
                cmd.Parameters.AddWithValue("@FechaDesde", fechaDesde.Date);
                cmd.Parameters.AddWithValue("@FechaHasta", fechaHasta.Date.AddDays(1).AddSeconds(-1));
                using var r = await cmd.ExecuteReaderAsync();
                while (await r.ReadAsync())
                {
                    dist.Add(new DistribucionClaseDTO
                    {
                        Clase = r.GetString(0),
                        Ingresos = r.GetDecimal(1),
                        Boletos = r.GetInt32(2)
                    });
                }
            }

            // ── 3. KPI derivados ─────────────────────────────────────────────────
            int totalBoletos = dist.Sum(d => d.Boletos);
            decimal ingTurista = dist.FirstOrDefault(d => d.Clase.ToLower().Contains("turista"))?.Ingresos ?? 0;
            decimal ingEjecutivo = dist.FirstOrDefault(d => d.Clase.ToLower().Contains("ejecutivo"))?.Ingresos ?? 0;

            var kpi = new IngresosKpiDTO
            {
                IngresosTotales = totalIngresos,
                IngresosTurista = ingTurista,
                IngresosEjecutivo = ingEjecutivo,
                TotalBoletos = totalBoletos,
                TotalReservaciones = totalReservaciones,
                // Ticket promedio = ingreso total / número de facturas (compras)
                TicketPromedio = totalReservaciones > 0
                    ? Math.Round(totalIngresos / totalReservaciones, 2) : 0
            };

            return (kpi, dist);
        }

        // ── Resumen general (KPIs) ────────────────────────────────────────────
        /// <summary>
        /// Construye el resumen general de metricas del sistema combinando busquedas por
        /// dia, rutas mas buscadas, distribucion por tipo y datos de ingresos para el
        /// rango de fechas indicado.
        /// </summary>
        public async Task<MetricasResumenDTO> ObtenerResumen(DateTime? desde, DateTime? hasta)
        {
            var porDia = await ObtenerBusquedasPorDia(desde, hasta);
            var rutas = await ObtenerRutasMasBuscadas(desde, hasta, null);
            var porTipo = await ObtenerBusquedasPorTipo(desde, hasta);

            int totalWeb = porTipo.FirstOrDefault(t => t.Tipo == "Web")?.Total ?? 0;
            int totalRest = porTipo.FirstOrDefault(t => t.Tipo == "REST")?.Total ?? 0;

            var (ingresoKpi, distribucion) = await ObtenerIngresos(desde, hasta);

            return new MetricasResumenDTO
            {
                BusquedasPorDia = porDia,
                RutasMasBuscadas = rutas,
                BusquedasPorTipo = porTipo,
                TotalBusquedas = porTipo.Sum(t => t.Total),   // todos los canales
                TotalBusquedasWeb = totalWeb,
                TotalBusquedasRest = totalRest,
                IngresosKpi = ingresoKpi,
                DistribucionClase = distribucion
            };
        }

        // ── Análisis de negocio 1: Embudo búsqueda → reservación → pago ─────────
        public async Task<EmbudoNegocioDTO> ObtenerEmbudo(DateTime? desde, DateTime? hasta)
        {
            var fechaDesde = desde ?? DateTime.Now.AddDays(-30);
            var fechaHasta = hasta ?? DateTime.Now;
            var dEnd = fechaHasta.Date.AddDays(1).AddSeconds(-1);

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            int busquedas = 0;
            using (var cmd = new SqlCommand(
                "SELECT COUNT(*) FROM Busqueda WHERE Fecha >= @D AND Fecha <= @H", conn))
            {
                cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
                cmd.Parameters.AddWithValue("@H", dEnd);
                busquedas = (int)(await cmd.ExecuteScalarAsync() ?? 0);
            }

            int reservaciones = 0, pagadas = 0, canceladas = 0, expiradas = 0, pendientes = 0, completadas = 0;
            var qRes = @"
                SELECT
                    COUNT(*) AS Total,
                    SUM(CASE WHEN EstadoReservaID = 2 THEN 1 ELSE 0 END) AS Pagadas,
                    SUM(CASE WHEN EstadoReservaID = 3 THEN 1 ELSE 0 END) AS Canceladas,
                    SUM(CASE WHEN EstadoReservaID = 4 THEN 1 ELSE 0 END) AS Expiradas,
                    SUM(CASE WHEN EstadoReservaID = 1 THEN 1 ELSE 0 END) AS Pendientes,
                    SUM(CASE WHEN EstadoReservaID = 5 THEN 1 ELSE 0 END) AS Completadas
                FROM Reservacion
                WHERE FechaCreacion >= @D AND FechaCreacion <= @H";

            using (var cmd = new SqlCommand(qRes, conn))
            {
                cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
                cmd.Parameters.AddWithValue("@H", dEnd);
                using var r = await cmd.ExecuteReaderAsync();
                if (await r.ReadAsync())
                {
                    reservaciones = r.GetInt32(0);
                    pagadas       = r.GetInt32(1);
                    canceladas    = r.GetInt32(2);
                    expiradas     = r.GetInt32(3);
                    pendientes    = r.GetInt32(4);
                    completadas   = r.GetInt32(5);
                }
            }

            return new EmbudoNegocioDTO
            {
                Busquedas     = busquedas,
                Reservaciones = reservaciones,
                Pagadas       = pagadas,
                Canceladas    = canceladas,
                Expiradas     = expiradas,
                Pendientes    = pendientes,
                Completadas   = completadas
            };
        }

        // ── Análisis de negocio 2: Rendimiento de rutas (revenue + boletos) ──────
        public async Task<List<RutaRendimientoDTO>> ObtenerRutasRendimiento(
            DateTime? desde, DateTime? hasta)
        {
            var fechaDesde = desde ?? DateTime.Now.AddDays(-30);
            var fechaHasta = hasta ?? DateTime.Now;

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            var query = @"
                SELECT TOP 10
                    ao.Codigo AS OrigenCodigo,
                    ad.Codigo AS DestinoCodigo,
                    ao.Codigo + ' → ' + ad.Codigo AS Ruta,
                    COUNT(DISTINCT r.ID)        AS TotalReservaciones,
                    ISNULL(SUM(f.Total), 0)     AS RevenueTotal,
                    COUNT(b.ID)                 AS BoletosVendidos,
                    ISNULL(bq.TotalBusquedas, 0) AS Busquedas
                FROM Factura f
                INNER JOIN Reservacion r ON f.ReservacionID  = r.ID
                INNER JOIN Boleto b      ON b.ReservacionID  = r.ID
                    AND b.EstadoBoletoID IN (2, 3)
                INNER JOIN Vuelo v       ON b.VueloID        = v.ID
                INNER JOIN Ruta ru       ON v.RutaID         = ru.ID
                INNER JOIN Aeropuerto ao ON ru.OrigenID      = ao.ID
                INNER JOIN Aeropuerto ad ON ru.DestinoID     = ad.ID
                LEFT JOIN (
                    SELECT r2.ID AS RutaID, COUNT(*) AS TotalBusquedas
                    FROM Busqueda b2
                    INNER JOIN Ruta r2 ON b2.RutaID = r2.ID
                    WHERE b2.Fecha >= @D AND b2.Fecha <= @H
                    GROUP BY r2.ID
                ) bq ON bq.RutaID = ru.ID
                WHERE f.Fecha >= @D AND f.Fecha <= @H
                  AND r.EstadoReservaID <> 3
                GROUP BY ao.Codigo, ad.Codigo, bq.TotalBusquedas
                ORDER BY RevenueTotal DESC";

            using var cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
            cmd.Parameters.AddWithValue("@H", fechaHasta.Date.AddDays(1).AddSeconds(-1));

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<RutaRendimientoDTO>();
            while (await reader.ReadAsync())
            {
                lista.Add(new RutaRendimientoDTO
                {
                    OrigenCodigo       = reader.GetString(0),
                    DestinoCodigo      = reader.GetString(1),
                    Ruta               = reader.GetString(2),
                    TotalReservaciones = reader.GetInt32(3),
                    RevenueTotal       = reader.GetDecimal(4),
                    BoletosVendidos    = reader.GetInt32(5),
                    Busquedas          = reader.GetInt32(6)
                });
            }
            return lista;
        }

        // ── Análisis de negocio 3: Cancelaciones (por ruta, tipo, anticipación) ─
        public async Task<CancelacionesAnalisisDTO> ObtenerCancelaciones(
            DateTime? desde, DateTime? hasta)
        {
            var fechaDesde = desde ?? DateTime.Now.AddDays(-30);
            var fechaHasta = hasta ?? DateTime.Now;
            var dEnd = fechaHasta.Date.AddDays(1).AddSeconds(-1);

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            // -- Por ruta (top 8 rutas con más cancelaciones)
            var porRuta = new List<CancelacionPorRutaDTO>();
            var qRuta = @"
                SELECT TOP 8
                    ao.Codigo AS OrigenCodigo,
                    ad.Codigo AS DestinoCodigo,
                    COUNT(DISTINCT r.ID) AS Total
                FROM Reservacion r
                INNER JOIN Boleto b      ON b.ReservacionID = r.ID
                INNER JOIN Vuelo v       ON b.VueloID       = v.ID
                INNER JOIN Ruta ru       ON v.RutaID        = ru.ID
                INNER JOIN Aeropuerto ao ON ru.OrigenID     = ao.ID
                INNER JOIN Aeropuerto ad ON ru.DestinoID    = ad.ID
                WHERE r.EstadoReservaID = 3
                  AND r.FechaCancelacion IS NOT NULL
                  AND r.FechaCancelacion >= @D AND r.FechaCancelacion <= @H
                GROUP BY ao.Codigo, ad.Codigo
                ORDER BY Total DESC";

            using (var cmd = new SqlCommand(qRuta, conn))
            {
                cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
                cmd.Parameters.AddWithValue("@H", dEnd);
                using var r = await cmd.ExecuteReaderAsync();
                while (await r.ReadAsync())
                    porRuta.Add(new CancelacionPorRutaDTO
                    {
                        OrigenCodigo  = r.GetString(0),
                        DestinoCodigo = r.GetString(1),
                        Total         = r.GetInt32(2)
                    });
            }

            // -- Por tipo via LogReservacion (Admin / Agencia / Usuario directo)
            var porTipo = new List<CancelacionPorTipoDTO>();
            var qTipo = @"
                SELECT
                    CASE lr.TipoEventoID
                        WHEN 23 THEN 'Administrador'
                        WHEN 20 THEN 'Agencia'
                        ELSE         'Usuario'
                    END AS Tipo,
                    COUNT(DISTINCT lr.ReservacionID) AS Total
                FROM LogReservacion lr
                INNER JOIN Reservacion r ON r.ID = lr.ReservacionID
                WHERE lr.TipoEventoID IN (18, 20, 23)
                  AND r.EstadoReservaID = 3
                  AND lr.Fecha >= @D AND lr.Fecha <= @H
                GROUP BY
                    CASE lr.TipoEventoID
                        WHEN 23 THEN 'Administrador'
                        WHEN 20 THEN 'Agencia'
                        ELSE         'Usuario'
                    END
                ORDER BY Total DESC";

            using (var cmd = new SqlCommand(qTipo, conn))
            {
                cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
                cmd.Parameters.AddWithValue("@H", dEnd);
                using var r = await cmd.ExecuteReaderAsync();
                while (await r.ReadAsync())
                    porTipo.Add(new CancelacionPorTipoDTO
                    {
                        Tipo  = r.GetString(0),
                        Total = r.GetInt32(1)
                    });
            }

            // -- Por anticipación: días entre creación de la reserva y el primer vuelo
            var porAnticipacion = new List<CancelacionPorAnticipacionDTO>();
            var qAntic = @"
                SELECT Bucket, COUNT(*) AS Total
                FROM (
                    SELECT
                        CASE
                            WHEN DATEDIFF(DAY, r.FechaCreacion, mf.FechaPrimerVuelo) < 1  THEN 'Menos de 24h'
                            WHEN DATEDIFF(DAY, r.FechaCreacion, mf.FechaPrimerVuelo) < 3  THEN '1-3 días'
                            WHEN DATEDIFF(DAY, r.FechaCreacion, mf.FechaPrimerVuelo) < 7  THEN '3-7 días'
                            ELSE 'Más de 7 días'
                        END AS Bucket
                    FROM Reservacion r
                    INNER JOIN (
                        SELECT b2.ReservacionID, MIN(v2.Fecha) AS FechaPrimerVuelo
                        FROM   Boleto b2
                        INNER JOIN Vuelo v2 ON b2.VueloID = v2.ID
                        GROUP BY b2.ReservacionID
                    ) mf ON mf.ReservacionID = r.ID
                    WHERE r.EstadoReservaID = 3
                      AND r.FechaCancelacion IS NOT NULL
                      AND r.FechaCancelacion >= @D AND r.FechaCancelacion <= @H
                ) sub
                GROUP BY Bucket
                ORDER BY
                    CASE Bucket
                        WHEN 'Menos de 24h' THEN 1
                        WHEN '1-3 días'     THEN 2
                        WHEN '3-7 días'     THEN 3
                        ELSE 4
                    END";

            using (var cmd = new SqlCommand(qAntic, conn))
            {
                cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
                cmd.Parameters.AddWithValue("@H", dEnd);
                using var r = await cmd.ExecuteReaderAsync();
                while (await r.ReadAsync())
                    porAnticipacion.Add(new CancelacionPorAnticipacionDTO
                    {
                        Bucket = r.GetString(0),
                        Total  = r.GetInt32(1)
                    });
            }

            // -- Reservaciones canceladas con escala: traer tramos ordenados por hora de salida
            var reservacionesConEscala = new List<ReservacionEscalaDTO>();
            var qEscala = @"
                SELECT r.ID, ao.Codigo AS Origen, ad.Codigo AS Destino, v.HoraSalida
                FROM Reservacion r
                INNER JOIN Boleto b   ON b.ReservacionID = r.ID
                INNER JOIN Vuelo v    ON b.VueloID = v.ID
                INNER JOIN Ruta ru    ON v.RutaID = ru.ID
                INNER JOIN Aeropuerto ao ON ru.OrigenID  = ao.ID
                INNER JOIN Aeropuerto ad ON ru.DestinoID = ad.ID
                WHERE r.EstadoReservaID = 3
                  AND r.FechaCancelacion >= @D AND r.FechaCancelacion <= @H
                  AND r.ID IN (
                      SELECT r2.ID
                      FROM Reservacion r2
                      INNER JOIN Boleto b2 ON b2.ReservacionID = r2.ID
                      INNER JOIN Vuelo v2  ON b2.VueloID = v2.ID
                      INNER JOIN Ruta ru2  ON v2.RutaID = ru2.ID
                      WHERE r2.EstadoReservaID = 3
                        AND r2.FechaCancelacion >= @D AND r2.FechaCancelacion <= @H
                      GROUP BY r2.ID HAVING COUNT(DISTINCT ru2.ID) > 1
                  )
                ORDER BY r.ID, v.HoraSalida";

            using (var cmd = new SqlCommand(qEscala, conn))
            {
                cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
                cmd.Parameters.AddWithValue("@H", dEnd);
                using var r = await cmd.ExecuteReaderAsync();

                // Agrupar tramos por reservación y construir cadena "GUA → MEX → MAD"
                var tramosMap = new Dictionary<int, List<(string o, string d)>>();
                while (await r.ReadAsync())
                {
                    int rid = r.GetInt32(0);
                    if (!tramosMap.ContainsKey(rid)) tramosMap[rid] = new();
                    tramosMap[rid].Add((r.GetString(1), r.GetString(2)));
                }

                foreach (var kvp in tramosMap)
                {
                    // Construir cadena eliminando aeropuertos intermedios duplicados
                    var codigos = new List<string> { kvp.Value[0].o };
                    foreach (var (_, d) in kvp.Value)
                        if (codigos[^1] != d) codigos.Add(d);

                    reservacionesConEscala.Add(new ReservacionEscalaDTO
                    {
                        Id   = kvp.Key,
                        Ruta = string.Join(" → ", codigos)
                    });
                }
            }

            return new CancelacionesAnalisisDTO
            {
                PorRuta              = porRuta,
                PorTipo              = porTipo,
                PorAnticipacion      = porAnticipacion,
                ReservacionesConEscala = reservacionesConEscala
            };
        }

        // ── Análisis de negocio 4: Ingresos mensuales por clase ──────────────────
        public async Task<List<IngresosMensualDTO>> ObtenerIngresosTendencia(
            DateTime? desde, DateTime? hasta)
        {
            var fechaDesde = desde ?? DateTime.Now.AddMonths(-6);
            var fechaHasta = hasta ?? DateTime.Now;

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            var query = @"
                SELECT
                    FORMAT(f.Fecha, 'yyyy-MM')       AS Mes,
                    c.TipoDeClase                    AS Clase,
                    ISNULL(SUM(b.Precio), 0)         AS Revenue,
                    COUNT(DISTINCT r.ID)              AS Reservaciones
                FROM Factura f
                INNER JOIN Reservacion r ON f.ReservacionID = r.ID
                INNER JOIN Boleto b      ON b.ReservacionID = r.ID
                    AND b.EstadoBoletoID IN (2, 3)
                INNER JOIN Clase c       ON c.ID = b.ClaseID
                WHERE f.Fecha >= @D AND f.Fecha <= @H
                  AND r.EstadoReservaID <> 3
                GROUP BY FORMAT(f.Fecha, 'yyyy-MM'), c.TipoDeClase
                ORDER BY Mes, c.TipoDeClase";

            using var cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
            cmd.Parameters.AddWithValue("@H", fechaHasta.Date.AddDays(1).AddSeconds(-1));

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<IngresosMensualDTO>();
            while (await reader.ReadAsync())
            {
                lista.Add(new IngresosMensualDTO
                {
                    Mes           = reader.GetString(0),
                    Clase         = reader.GetString(1),
                    Revenue       = reader.GetDecimal(2),
                    Reservaciones = reader.GetInt32(3)
                });
            }
            return lista;
        }

        // ── Análisis de negocio 5: Mapa de calor de búsquedas por día/hora ──────
        // Muestra en qué día de la semana y hora del día se concentran más las
        // búsquedas de vuelos, independientemente del período seleccionado.
        public async Task<List<HeatmapCeldaDTO>> ObtenerHeatmap(
            DateTime? desde, DateTime? hasta)
        {
            var fechaDesde = desde ?? DateTime.Now.AddDays(-30);
            var fechaHasta = hasta ?? DateTime.Now;

            using var conn = _connectionFactory.CreateConnection();
            await conn.OpenAsync();

            // Primero obtenemos el máximo para calcular intensidad relativa
            var query = @"
                WITH base AS (
                    SELECT
                        DATEPART(WEEKDAY, Fecha) AS DiaSemana,
                        DATEPART(HOUR,   Fecha)  AS Hora,
                        COUNT(*)                 AS Total
                    FROM Busqueda
                    WHERE Fecha >= @D AND Fecha <= @H
                    GROUP BY DATEPART(WEEKDAY, Fecha), DATEPART(HOUR, Fecha)
                ),
                maxval AS (SELECT MAX(Total) AS MaxTotal FROM base)
                SELECT
                    b.DiaSemana,
                    b.Hora,
                    CAST(b.Total AS FLOAT) / NULLIF(m.MaxTotal, 0) * 100 AS PctRelativo,
                    b.Total                                               AS TotalBusquedas,
                    CAST(m.MaxTotal AS FLOAT)                             AS MaxBusquedas
                FROM base b
                CROSS JOIN maxval m
                ORDER BY b.DiaSemana, b.Hora";

            using var cmd = new SqlCommand(query, conn);
            cmd.Parameters.AddWithValue("@D", fechaDesde.Date);
            cmd.Parameters.AddWithValue("@H", fechaHasta.Date.AddDays(1).AddSeconds(-1));

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<HeatmapCeldaDTO>();
            while (await reader.ReadAsync())
            {
                lista.Add(new HeatmapCeldaDTO
                {
                    DiaSemana         = reader.GetInt32(0),
                    Hora              = reader.GetInt32(1),
                    OcupacionPct      = Convert.ToDouble(reader.GetValue(2)), // % relativo al pico
                    AsientosVendidos  = Convert.ToInt32(reader.GetValue(3)),  // total búsquedas
                    CapacidadPromedio = Convert.ToDouble(reader.GetValue(4))  // pico del período
                });
            }
            return lista;
        }

        // ── Listado paginado con filtros (para exportar / ver tabla) ──────────
        /// <summary>
        /// Retorna un listado paginado de busquedas con filtros por fecha, tipo de acceso
        /// y usuario. Incluye conteo total para calcular paginas. Se usa para la tabla
        /// de detalle del panel de metricas y para exportaciones.
        /// </summary>
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
