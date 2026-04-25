using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio principal de vuelos. Soporta busqueda por termino libre, busqueda
    /// directa por ruta y fecha, y busqueda con escalas mediante BFS por capas.
    /// Tambien registra cada busqueda realizada para alimentar las metricas del sistema.
    /// </summary>
    public class VueloRepository : IVueloRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public VueloRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Representa un camino parcial durante el recorrido BFS de vuelos con escalas.
        /// Acumula los tramos, precios, disponibilidad y tiempo de vuelo hasta el momento.
        /// </summary>
        private class CaminoParcial
        {
            public List<VueloDetalleDTO> Tramos { get; set; } = new();
            public DateTime UltimaFechaLlegada { get; set; }
            public TimeSpan UltimaHoraLlegada { get; set; }
            public int DuracionVueloAcumulada { get; set; }   // solo tiempo en aire, sin escalas
            public HashSet<int> AeropuertosVisitados { get; set; } = new();
            public int UltimoDestinoId { get; set; }
            public decimal? PrecioTuristaAcumulado { get; set; }
            public decimal? PrecioEjecutivaAcumulado { get; set; }
            public int? BoletosDisponiblesTurista { get; set; }
            public int? BoletosDisponiblesEjecutiva { get; set; }
        }

        /// <summary>
        /// Realiza una busqueda libre de vuelos activos que coincidan con el termino
        /// ingresado contra ciudad, pais, aeropuerto o numero de vuelo. Retorna hasta
        /// 50 resultados futuros con disponibilidad, ordenados por fecha y hora de salida.
        /// </summary>
        public async Task<List<VueloDetalleDTO>> BusquedaGeneral(string query)
        {
            var vuelos = new List<VueloDetalleDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string filtro = @"
                AND (
                    co.Nombre        LIKE @busqueda
                    OR cd.Nombre     LIKE @busqueda
                    OR ao.Codigo     LIKE @busqueda
                    OR ad.Codigo     LIKE @busqueda
                    OR po.Nombre     LIKE @busqueda
                    OR pd.Nombre     LIKE @busqueda
                    OR ao.Nombre     LIKE @busqueda
                    OR ad.Nombre     LIKE @busqueda
                    OR v.NumeroVuelo LIKE @busqueda
                )";

            string sql = $@"
                SELECT TOP 50
                    v.ID, v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada,
                    e.ID AS EstadoId, e.Estatus,
                    a.ID AS AvionId, a.Modelo, a.Marca, a.CapacidadPasajeros,
                    ao.ID, ao.Nombre, ao.Codigo, co.Nombre, po.Nombre,
                    ad.ID, ad.Nombre, ad.Codigo, cd.Nombre, pd.Nombre,
                    r.ID AS RutaId, r.DuracionEstimada,
                    v.PrecioTurista, v.PrecioEjecutivo,
                    v.BoletosTurista, v.BoletosEjecutivo,
                    v.FechaLlegada
                FROM Vuelo v
                INNER JOIN Estado     e  ON v.EstadoID  = e.ID
                INNER JOIN Avion      a  ON v.AvionID   = a.ID
                INNER JOIN Ruta       r  ON v.RutaID    = r.ID
                INNER JOIN Aeropuerto ao ON r.OrigenID  = ao.ID
                INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                INNER JOIN Ciudad     co ON ao.CiudadID = co.ID
                INNER JOIN Ciudad     cd ON ad.CiudadID = cd.ID
                INNER JOIN Pais       po ON co.PaisID   = po.ID
                INNER JOIN Pais       pd ON cd.PaisID   = pd.ID
                WHERE e.Estatus = 'A tiempo'
                  AND v.Fecha >= CAST(GETDATE() AS DATE)
                  AND (v.BoletosTurista > 0 OR v.BoletosEjecutivo > 0)
                  {filtro}
                ORDER BY v.Fecha, v.HoraSalida";

            using var cmd = new SqlCommand(sql, connection);
            cmd.Parameters.AddWithValue("@busqueda", $"%{query}%");

            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
                vuelos.Add(MapearVuelo(reader));

            reader.Close();

            foreach (var vuelo in vuelos)
                vuelo.Tripulantes = await ObtenerTripulantesPorVuelo(connection, vuelo.Id);

            return vuelos;
        }

        /// <summary>
        /// Busca vuelos directos entre dos aeropuertos en una fecha especifica con la
        /// cantidad de pasajeros indicada. Permite filtrar por clase (Turista o Ejecutivo).
        /// Retorna los vuelos disponibles ordenados por hora de salida, cada uno con su
        /// lista de tripulantes asignados.
        /// </summary>
        public async Task<List<VueloDetalleDTO>> BuscarVuelos(
            int origenId, int destinoId, DateTime fecha,
            int cantidadPasajeros, int? claseId = null)
        {
            var vuelos = new List<VueloDetalleDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string filtroClase = BuildFiltroClase(claseId);

            string query = $@"
                SELECT
                    v.ID, v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada,
                    e.ID AS EstadoId, e.Estatus,
                    a.ID AS AvionId, a.Modelo, a.Marca, a.CapacidadPasajeros,
                    ao.ID, ao.Nombre, ao.Codigo, co.Nombre, po.Nombre,
                    ad.ID, ad.Nombre, ad.Codigo, cd.Nombre, pd.Nombre,
                    r.ID AS RutaId, r.DuracionEstimada,
                    v.PrecioTurista, v.PrecioEjecutivo,
                    v.BoletosTurista, v.BoletosEjecutivo,
                    v.FechaLlegada
                FROM Vuelo v
                INNER JOIN Estado     e  ON v.EstadoID  = e.ID
                INNER JOIN Avion      a  ON v.AvionID   = a.ID
                INNER JOIN Ruta       r  ON v.RutaID    = r.ID
                INNER JOIN Aeropuerto ao ON r.OrigenID  = ao.ID
                INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                INNER JOIN Ciudad     co ON ao.CiudadID = co.ID
                INNER JOIN Ciudad     cd ON ad.CiudadID = cd.ID
                INNER JOIN Pais       po ON co.PaisID   = po.ID
                INNER JOIN Pais       pd ON cd.PaisID   = pd.ID
                WHERE r.OrigenID  = @origenId
                  AND r.DestinoID = @destinoId
                  AND v.Fecha     = @fecha
                  AND e.Estatus   = 'A tiempo'
                  {filtroClase}
                ORDER BY v.HoraSalida";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@origenId", origenId);
            cmd.Parameters.AddWithValue("@destinoId", destinoId);
            cmd.Parameters.AddWithValue("@fecha", fecha.Date);
            cmd.Parameters.AddWithValue("@cantidadPasajeros", cantidadPasajeros);

            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
                vuelos.Add(MapearVuelo(reader));

            reader.Close();

            foreach (var vuelo in vuelos)
                vuelo.Tripulantes = await ObtenerTripulantesPorVuelo(connection, vuelo.Id);

            return vuelos;
        }

        /// <summary>
        /// Busca combinaciones de vuelos con escalas entre dos aeropuertos usando BFS por capas.
        /// Aplica reglas de escala entre 1h y 12h, limita la duracion total de vuelo al 1.5x
        /// de la ruta directa y evita ciclos en aeropuertos intermedios.
        /// Admite hasta maxEscalas (default 3) y filtra por clase y disponibilidad.
        /// Retorna los resultados ordenados por duracion total incluyendo tiempo de espera.
        /// </summary>
        public async Task<List<VueloConEscalaDTO>> BuscarVuelosConEscalas(
            int origenId, int destinoId, DateTime fecha,
            int cantidadPasajeros, int? claseId = null,
            int maxEscalas = 3)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Buscamos la duración de la ruta directa para aplicar la regla del 1.5x solo en tiempo de vuelo
            int limiteVueloMinutos = await ObtenerLimiteVuelo(connection, origenId, destinoId, maxEscalas);

            // Capa 0: todos los vuelos que salen del origen en la fecha dada
            // Excluimos el destino final para que no aparezca como primer tramo directo
            var tramosIniciales = await BuscarTramosDesdeLista(
                connection,
                new List<int> { origenId },
                destinoId,
                fecha,
                cantidadPasajeros,
                claseId,
                excluirDestino: true);

            // Inicializamos los caminos parciales con cada vuelo del primer tramo
            var caminosActivos = new List<CaminoParcial>();
            foreach (var tramo in tramosIniciales)
            {
                // Saltamos vuelos que ya superan el límite de tiempo en aire solos
                if (tramo.DuracionMinutos > limiteVueloMinutos)
                    continue;

                caminosActivos.Add(new CaminoParcial
                {
                    Tramos = new List<VueloDetalleDTO> { tramo },
                    UltimaFechaLlegada = tramo.FechaLlegada,
                    UltimaHoraLlegada = tramo.HoraLlegada,
                    DuracionVueloAcumulada = tramo.DuracionMinutos,
                    UltimoDestinoId = tramo.DestinoId,
                    AeropuertosVisitados = new HashSet<int> { origenId, tramo.DestinoId },
                    PrecioTuristaAcumulado = tramo.PrecioTurista,
                    PrecioEjecutivaAcumulado = tramo.PrecioEjecutiva,
                    BoletosDisponiblesTurista = tramo.BoletosDisponiblesTurista,
                    BoletosDisponiblesEjecutiva = tramo.BoletosDisponiblesEjecutiva,
                });
            }

            var resultados = new List<VueloConEscalaDTO>();
            var combinacionesVistas = new HashSet<string>();

            // BFS: expandimos capa por capa hasta maxEscalas
            for (int escala = 1; escala <= maxEscalas && caminosActivos.Count > 0; escala++)
            {
                // Recopilamos los aeropuertos únicos desde donde necesitamos seguir buscando
                var origenesActuales = caminosActivos
                    .Select(c => c.UltimoDestinoId)
                    .Distinct()
                    .ToList();

                // Traemos todos los vuelos que salen de esos aeropuertos
                // Incluimos el destino final porque en esta capa ya podría ser el último tramo
                var tramosNuevos = await BuscarTramosDesdeLista(
                    connection,
                    origenesActuales,
                    destinoId,
                    fecha,
                    cantidadPasajeros,
                    claseId,
                    excluirDestino: false);

                // Indexamos los nuevos tramos por origen para no hacer Where en cada iteración
                var tramosPorOrigen = tramosNuevos
                    .GroupBy(t => t.OrigenId)
                    .ToDictionary(g => g.Key, g => g.ToList());

                var caminosSiguienteCapa = new List<CaminoParcial>();

                foreach (var camino in caminosActivos)
                {
                    if (!tramosPorOrigen.TryGetValue(camino.UltimoDestinoId, out var candidatos))
                        continue;

                    foreach (var tramo in candidatos)
                    {
                        // Evitamos ciclos: el tramo no puede ir a un aeropuerto ya visitado
                        // (excepto si es el destino final, que tampoco debería estar en visitados)
                        if (camino.AeropuertosVisitados.Contains(tramo.DestinoId) && tramo.DestinoId != destinoId)
                            continue;

                        // Validamos la escala: tiempo entre llegada del tramo anterior y salida de este
                        var llegadaAnterior = camino.UltimaFechaLlegada.Date + camino.UltimaHoraLlegada;
                        var salidaActual = tramo.Fecha.Date + tramo.HoraSalida;

                        int minutosEscala = (int)(salidaActual - llegadaAnterior).TotalMinutes;

                        // Regla: espera en aeropuerto entre 1h y 12h
                        if (minutosEscala < 60 || minutosEscala > 720)
                            continue;

                        // Regla: duración acumulada de vuelo (sin escalas) no supera el multiplicador de la ruta directa
                        int nuevaDuracionVuelo = camino.DuracionVueloAcumulada + tramo.DuracionMinutos;
                        if (nuevaDuracionVuelo > limiteVueloMinutos)
                            continue;

                        // Disponibilidad: tomamos el mínimo entre todos los tramos
                        int? dispTurista = MinDisponible(camino.BoletosDisponiblesTurista, tramo.BoletosDisponiblesTurista);
                        int? dispEjecutiva = MinDisponible(camino.BoletosDisponiblesEjecutiva, tramo.BoletosDisponiblesEjecutiva);

                        // Validamos disponibilidad según la clase pedida
                        if (!TieneDisponibilidad(claseId, dispTurista, dispEjecutiva, cantidadPasajeros))
                            continue;

                        // Construimos la clave única para no repetir combinaciones
                        var tramosIds = string.Join("-", camino.Tramos.Select(t => t.Id)) + $"-{tramo.Id}";
                        if (!combinacionesVistas.Add(tramosIds))
                            continue;

                        // Precios acumulados
                        decimal? precioTurista = SumarPrecios(camino.PrecioTuristaAcumulado, tramo.PrecioTurista);
                        decimal? precioEjecutiva = SumarPrecios(camino.PrecioEjecutivaAcumulado, tramo.PrecioEjecutiva);

                        var nuevosTramos = new List<VueloDetalleDTO>(camino.Tramos) { tramo };

                        // Si llegamos al destino final guardamos el resultado
                        if (tramo.DestinoId == destinoId)
                        {
                            // Duración total real = tiempo de vuelo + tiempo de todas las escalas
                            int duracionTotalConEscalas = nuevaDuracionVuelo +
                                CalcularTiempoTotalEscalas(nuevosTramos);

                            resultados.Add(new VueloConEscalaDTO
                            {
                                NumeroEscalas = escala,
                                DuracionTotalMinutos = duracionTotalConEscalas,
                                TiempoEscalaMinutos = duracionTotalConEscalas - nuevaDuracionVuelo,
                                PrecioTuristaTotal = precioTurista,
                                PrecioEjecutivaTotal = precioEjecutiva,
                                BoletosDisponiblesTurista = dispTurista,
                                BoletosDisponiblesEjecutiva = dispEjecutiva,
                                Tramos = nuevosTramos
                            });

                            continue;
                        }

                        // Si no llegamos al destino y aún tenemos escalas disponibles, seguimos expandiendo
                        if (escala < maxEscalas)
                        {
                            var nuevosVisitados = new HashSet<int>(camino.AeropuertosVisitados) { tramo.DestinoId };

                            caminosSiguienteCapa.Add(new CaminoParcial
                            {
                                Tramos = nuevosTramos,
                                UltimaFechaLlegada = tramo.FechaLlegada,
                                UltimaHoraLlegada = tramo.HoraLlegada,
                                DuracionVueloAcumulada = nuevaDuracionVuelo,
                                UltimoDestinoId = tramo.DestinoId,
                                AeropuertosVisitados = nuevosVisitados,
                                PrecioTuristaAcumulado = precioTurista,
                                PrecioEjecutivaAcumulado = precioEjecutiva,
                                BoletosDisponiblesTurista = dispTurista,
                                BoletosDisponiblesEjecutiva = dispEjecutiva,
                            });
                        }
                    }
                }

                caminosActivos = caminosSiguienteCapa;
            }

            // Cargamos tripulantes solo de los vuelos únicos que aparecen en los resultados
            var vuelosUnicos = resultados
                .SelectMany(r => r.Tramos)
                .GroupBy(v => v.Id)
                .Select(g => g.First())
                .ToList();

            var tripulantesPorVuelo = new Dictionary<int, List<TripulanteDTO>>();
            foreach (var vuelo in vuelosUnicos)
                tripulantesPorVuelo[vuelo.Id] = await ObtenerTripulantesPorVuelo(connection, vuelo.Id);

            foreach (var resultado in resultados)
                foreach (var tramo in resultado.Tramos)
                    tramo.Tripulantes = tripulantesPorVuelo[tramo.Id];

            return resultados.OrderBy(r => r.DuracionTotalMinutos).Take(50).ToList();
        }

        private async Task<List<VueloDetalleDTO>> BuscarTramosDesdeLista(
            SqlConnection connection,
            List<int> origenIds,
            int destinoFinalId,
            DateTime fechaDesde,
            int cantidadPasajeros,
            int? claseId,
            bool excluirDestino)
        {
            if (origenIds.Count == 0)
                return new List<VueloDetalleDTO>();

            string filtroClase = BuildFiltroClase(claseId);

            // Construimos los parámetros para el IN (...)
            var paramNames = origenIds.Select((_, i) => $"@origen{i}").ToList();
            string inClause = string.Join(", ", paramNames);

            // Si es la primera capa o una intermedia, excluimos el destino final para no mezclar
            // con vuelos directos. En la última capa no excluimos para poder cerrar el camino.
            string filtroDestino = excluirDestino
                ? "AND r.DestinoID != @destinoFinalId"
                : string.Empty;

            // Buscamos en un rango amplio de fechas para cubrir escalas que cruzan días
            string query = $@"
                SELECT
                    v.ID, v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada,
                    e.ID AS EstadoId, e.Estatus,
                    a.ID AS AvionId, a.Modelo, a.Marca, a.CapacidadPasajeros,
                    ao.ID, ao.Nombre, ao.Codigo, co.Nombre, po.Nombre,
                    ad.ID, ad.Nombre, ad.Codigo, cd.Nombre, pd.Nombre,
                    r.ID AS RutaId, r.DuracionEstimada,
                    v.PrecioTurista, v.PrecioEjecutivo,
                    v.BoletosTurista, v.BoletosEjecutivo,
                    v.FechaLlegada
                FROM Vuelo v
                INNER JOIN Estado     e  ON v.EstadoID  = e.ID
                INNER JOIN Avion      a  ON v.AvionID   = a.ID
                INNER JOIN Ruta       r  ON v.RutaID    = r.ID
                INNER JOIN Aeropuerto ao ON r.OrigenID  = ao.ID
                INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                INNER JOIN Ciudad     co ON ao.CiudadID = co.ID
                INNER JOIN Ciudad     cd ON ad.CiudadID = cd.ID
                INNER JOIN Pais       po ON co.PaisID   = po.ID
                INNER JOIN Pais       pd ON cd.PaisID   = pd.ID
                WHERE r.OrigenID IN ({inClause})
                  AND v.Fecha   >= @fechaDesde
                  AND e.Estatus  = 'A tiempo'
                  {filtroDestino}
                  {filtroClase}
                ORDER BY v.Fecha, v.HoraSalida";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@fechaDesde", fechaDesde.Date);
            cmd.Parameters.AddWithValue("@cantidadPasajeros", cantidadPasajeros);

            if (excluirDestino)
                cmd.Parameters.AddWithValue("@destinoFinalId", destinoFinalId);

            for (int i = 0; i < origenIds.Count; i++)
                cmd.Parameters.AddWithValue(paramNames[i], origenIds[i]);

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<VueloDetalleDTO>();
            while (await reader.ReadAsync())
                lista.Add(MapearVuelo(reader));

            return lista;
        }

        private async Task<int> ObtenerLimiteVuelo(
            SqlConnection connection,
            int origenId, int destinoId,
            int maxEscalas)
        {
            using var cmd = new SqlCommand(
                "SELECT TOP 1 DuracionEstimada FROM Ruta WHERE OrigenID = @origenId AND DestinoID = @destinoId",
                connection);

            cmd.Parameters.AddWithValue("@origenId", origenId);
            cmd.Parameters.AddWithValue("@destinoId", destinoId);

            var result = await cmd.ExecuteScalarAsync();

            if (result != null && result != DBNull.Value)
            {
                // Multiplicador simple: 1.5x la duración directa
                return (int)((int)result * 1.5);
            }

            // Fallback si no hay ruta directa: 8 horas por escala
            return maxEscalas * 8 * 60;
        }

        /// <summary>
        /// Registra una busqueda de vuelos en la tabla Busqueda para uso en metricas.
        /// Solo inserta el registro si existe una ruta directa entre los aeropuertos indicados.
        /// </summary>
        public async Task GuardarBusqueda(
            int origenId, int destinoId, DateTime fechaSalida,
            int cantidadPersonas, int? usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            int? rutaId = null;
            using (var cmd = new SqlCommand(
                "SELECT TOP 1 ID FROM Ruta WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId", connection))
            {
                cmd.Parameters.AddWithValue("@OrigenId", origenId);
                cmd.Parameters.AddWithValue("@DestinoId", destinoId);
                var result = await cmd.ExecuteScalarAsync();
                if (result != null) rutaId = (int)result;
            }

            if (!rutaId.HasValue) return;

            using var cmdInsert = new SqlCommand(@"
                INSERT INTO Busqueda (RutaID, FechaSalida, CantidadPersonas, UsuarioID, TipoBusquedaID, Fecha)
                VALUES (@RutaId, @FechaSalida, @CantidadPersonas, @UsuarioId, 1, @FechaHoy)", connection);

            cmdInsert.Parameters.AddWithValue("@RutaId", rutaId.Value);
            cmdInsert.Parameters.AddWithValue("@FechaSalida", fechaSalida.Date);
            cmdInsert.Parameters.AddWithValue("@CantidadPersonas", cantidadPersonas);
            cmdInsert.Parameters.AddWithValue("@UsuarioId", usuarioId.HasValue ? (object)usuarioId.Value : DBNull.Value);
            cmdInsert.Parameters.AddWithValue("@FechaHoy", DateTime.Now);

            await cmdInsert.ExecuteNonQueryAsync();
        }

        private static string BuildFiltroClase(int? claseId) => claseId == 1
            ? "AND v.BoletosTurista   >= @cantidadPasajeros"
            : claseId == 2
                ? "AND v.BoletosEjecutivo >= @cantidadPasajeros"
                : "AND (v.BoletosTurista >= @cantidadPasajeros OR v.BoletosEjecutivo >= @cantidadPasajeros)";

        private static int? MinDisponible(int? a, int? b) =>
            a.HasValue && b.HasValue ? Math.Min(a.Value, b.Value) : null;

        private static decimal? SumarPrecios(decimal? a, decimal? b) =>
            a.HasValue && b.HasValue ? a + b : null;

        private static bool TieneDisponibilidad(
            int? claseId, int? dispTurista, int? dispEjecutiva, int cantidadPasajeros)
        {
            if (claseId == 1) return dispTurista.HasValue && dispTurista >= cantidadPasajeros;
            if (claseId == 2) return dispEjecutiva.HasValue && dispEjecutiva >= cantidadPasajeros;

            // Sin clase específica: al menos una clase debe tener disponibilidad
            bool okTurista = dispTurista.HasValue && dispTurista >= cantidadPasajeros;
            bool okEjecutiva = dispEjecutiva.HasValue && dispEjecutiva >= cantidadPasajeros;
            return okTurista || okEjecutiva;
        }

        private static int CalcularTiempoTotalEscalas(List<VueloDetalleDTO> tramos)
        {
            int totalEscalas = 0;
            for (int i = 0; i < tramos.Count - 1; i++)
            {
                var llegada = tramos[i].FechaLlegada.Date + tramos[i].HoraLlegada;
                var salida = tramos[i + 1].Fecha.Date + tramos[i + 1].HoraSalida;
                totalEscalas += (int)(salida - llegada).TotalMinutes;
            }
            return totalEscalas;
        }

        private VueloDetalleDTO MapearVuelo(SqlDataReader reader)
        {
            return new VueloDetalleDTO
            {
                Id = reader.GetInt32(0),
                NumeroVuelo = reader.GetString(1),
                Fecha = reader.GetDateTime(2),
                HoraSalida = reader.IsDBNull(3) ? TimeSpan.Zero : reader.GetTimeSpan(3),
                HoraLlegada = reader.IsDBNull(4) ? TimeSpan.Zero : reader.GetTimeSpan(4),
                EstadoId = reader.GetInt32(5),
                Estado = reader.GetString(6),
                AvionId = reader.GetInt32(7),
                AvionModelo = reader.GetString(8),
                AvionMarca = reader.GetString(9),
                CapacidadPasajeros = reader.GetInt32(10),
                OrigenId = reader.GetInt32(11),
                OrigenNombre = reader.GetString(12),
                OrigenCodigo = reader.GetString(13),
                OrigenCiudad = reader.GetString(14),
                OrigenPais = reader.GetString(15),
                DestinoId = reader.GetInt32(16),
                DestinoNombre = reader.GetString(17),
                DestinoCodigo = reader.GetString(18),
                DestinoCiudad = reader.GetString(19),
                DestinoPais = reader.GetString(20),
                RutaId = reader.GetInt32(21),
                DuracionMinutos = reader.IsDBNull(22) ? 0 : reader.GetInt32(22),
                PrecioTurista = reader.IsDBNull(23) ? null : reader.GetDecimal(23),
                PrecioEjecutiva = reader.IsDBNull(24) ? null : reader.GetDecimal(24),
                BoletosDisponiblesTurista = reader.IsDBNull(25) ? null : reader.GetInt32(25),
                BoletosDisponiblesEjecutiva = reader.IsDBNull(26) ? null : reader.GetInt32(26),
                FechaLlegada = reader.GetDateTime(27),
                Tripulantes = new List<TripulanteDTO>()
            };
        }

        private async Task<List<TripulanteDTO>> ObtenerTripulantesPorVuelo(SqlConnection connection, int vueloId)
        {
            var tripulantes = new List<TripulanteDTO>();

            string query = @"
                SELECT mt.ID, mt.Nombre, mt.Apellido, mt.RolID, rt.Cargo
                FROM EquipoPivote ep
                INNER JOIN MiembroTripulacion mt ON ep.MiembroTripulacionID = mt.ID
                INNER JOIN RolTripulacion     rt ON mt.RolID = rt.ID
                WHERE ep.VueloID = @vueloId
                ORDER BY rt.Cargo, mt.Nombre";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@vueloId", vueloId);
            using var reader = await cmd.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                tripulantes.Add(new TripulanteDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID = reader.GetInt32(3),
                    NombreRol = reader.GetString(4),
                    NombreCompleto = $"{reader.GetString(1)} {reader.GetString(2)}"
                });
            }

            return tripulantes;
        }
    }
}
