using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AeropuertoRepository
    {
        private readonly DbConnectionFactory _connectionFactory;
        private readonly ILogger<AeropuertoRepository> _logger;

        public AeropuertoRepository(DbConnectionFactory connectionFactory, ILogger<AeropuertoRepository> logger)
        {
            _connectionFactory = connectionFactory;
            _logger = logger;
        }

        // ── CONSULTAS ─────────────────────────────────────────────────────

        public async Task<List<AeropuertoDTO>> ObtenerTodos()
        {
            var aeropuertos = new List<AeropuertoDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // LEFT JOIN a ZonaHoraria para obtener el nombre IANA
            string query = @"
                SELECT 
                    a.ID, a.Nombre, a.Codigo,
                    c.Nombre AS Ciudad, p.Nombre AS Pais,
                    ia.Imagen,
                    CASE
                        WHEN COL_LENGTH('Aeropuerto', 'ZonaHorariaID') IS NOT NULL
                        THEN (SELECT zh.Nombre FROM ZonaHoraria zh
                              WHERE zh.ID = a.ZonaHorariaID)
                        ELSE NULL
                    END AS ZonaHoraria
                FROM Aeropuerto a
                INNER JOIN Ciudad c            ON a.CiudadID        = c.ID
                INNER JOIN Pais p              ON c.PaisID           = p.ID
                LEFT  JOIN ImagenAeropuerto ia ON ia.AeropuertoID   = a.ID
                ORDER BY a.Nombre";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                aeropuertos.Add(new AeropuertoDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Codigo = reader.GetString(2),
                    Ciudad = reader.GetString(3),
                    Pais = reader.GetString(4),
                    ImagenBase64 = reader.IsDBNull(5) ? null : reader.GetString(5),
                    ZonaHoraria = reader.IsDBNull(6) ? null : reader.GetString(6)
                });
            }

            return aeropuertos;
        }

        public async Task<AeropuertoDTO?> ObtenerPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    a.ID, a.Nombre, a.Codigo,
                    c.Nombre AS Ciudad, p.Nombre AS Pais,
                    ia.Imagen,
                    CASE
                        WHEN COL_LENGTH('Aeropuerto', 'ZonaHorariaID') IS NOT NULL
                        THEN (SELECT zh.Nombre FROM ZonaHoraria zh
                              WHERE zh.ID = a.ZonaHorariaID)
                        ELSE NULL
                    END AS ZonaHoraria
                FROM Aeropuerto a
                INNER JOIN Ciudad c            ON a.CiudadID        = c.ID
                INNER JOIN Pais p              ON c.PaisID           = p.ID
                LEFT  JOIN ImagenAeropuerto ia ON ia.AeropuertoID   = a.ID
                WHERE a.ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);
            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new AeropuertoDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Codigo = reader.GetString(2),
                    Ciudad = reader.GetString(3),
                    Pais = reader.GetString(4),
                    ImagenBase64 = reader.IsDBNull(5) ? null : reader.GetString(5),
                    ZonaHoraria = reader.IsDBNull(6) ? null : reader.GetString(6)
                };
            }

            return null;
        }

        // ── ESCRITURA ─────────────────────────────────────────────────────

        // ── Verificar duplicados de nombre o código ──────────────────────
        // Devuelve qué campo está duplicado, o null si está libre.
        // excludeId: ignora el aeropuerto propio al editar.
        public async Task<string?> VerificarDuplicado(string nombre, string codigo, int? excludeId = null)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT
                    CASE WHEN LOWER(Nombre) = LOWER(@Nombre) THEN 'nombre'
                         WHEN UPPER(Codigo) = UPPER(@Codigo) THEN 'codigo'
                         ELSE NULL
                    END AS Campo
                FROM Aeropuerto
                WHERE (LOWER(Nombre) = LOWER(@Nombre) OR UPPER(Codigo) = UPPER(@Codigo))
                  AND (@ExcludeId IS NULL OR ID <> @ExcludeId)";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@Nombre", nombre.Trim());
            cmd.Parameters.AddWithValue("@Codigo", codigo.ToUpper().Trim());
            cmd.Parameters.AddWithValue("@ExcludeId", excludeId.HasValue ? (object)excludeId.Value : DBNull.Value);

            using var reader = await cmd.ExecuteReaderAsync();
            if (await reader.ReadAsync())
                return reader.IsDBNull(0) ? null : reader.GetString(0);
            return null;
        }

        // ── Buscar aeropuerto por código IATA ────────────────────────────
        public async Task<int?> ObtenerIdPorCodigo(string codigo)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "SELECT ID FROM Aeropuerto WHERE Codigo = @Codigo";
            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@Codigo", codigo.ToUpper().Trim());
            var result = await cmd.ExecuteScalarAsync();
            return result != null ? Convert.ToInt32(result) : (int?)null;
        }

        public async Task<int> Crear(Aeropuerto aeropuerto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            _logger.LogInformation("=== REPO Crear: Nombre={Nombre}, Codigo={Codigo}, CiudadId={CiudadId}, ZonaHorariaId={ZonaHorariaId}",
                aeropuerto.Nombre, aeropuerto.Codigo, aeropuerto.CiudadId, aeropuerto.ZonaHorariaId?.ToString() ?? "null");

            // Detectar si la columna ZonaHorariaID ya existe (migración aplicada)
            bool tieneZonaHorariaId = await ColumnaExiste(connection, "Aeropuerto", "ZonaHorariaID");
            _logger.LogInformation("=== REPO Crear: tieneZonaHorariaId={Val}", tieneZonaHorariaId);

            string query = tieneZonaHorariaId
                ? @"INSERT INTO Aeropuerto (Nombre, Codigo, CiudadID, ZonaHorariaID)
                    VALUES (@Nombre, @Codigo, @CiudadID, @ZonaHorariaID);
                    SELECT CAST(SCOPE_IDENTITY() as int);"
                : @"INSERT INTO Aeropuerto (Nombre, Codigo, CiudadID)
                    VALUES (@Nombre, @Codigo, @CiudadID);
                    SELECT CAST(SCOPE_IDENTITY() as int);";

            _logger.LogInformation("=== REPO Crear: ejecutando query={Query}", query.Trim());

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Nombre", aeropuerto.Nombre);
            command.Parameters.AddWithValue("@Codigo", aeropuerto.Codigo);
            command.Parameters.AddWithValue("@CiudadID", aeropuerto.CiudadId);
            if (tieneZonaHorariaId)
                command.Parameters.AddWithValue("@ZonaHorariaID",
                    aeropuerto.ZonaHorariaId.HasValue
                        ? (object)aeropuerto.ZonaHorariaId.Value
                        : DBNull.Value);

            var nuevoId = Convert.ToInt32(await command.ExecuteScalarAsync());
            _logger.LogInformation("=== REPO Crear: nuevo ID={Id}", nuevoId);
            return nuevoId;
        }

        public async Task<bool> Actualizar(Aeropuerto aeropuerto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            bool tieneZonaHorariaId = await ColumnaExiste(connection, "Aeropuerto", "ZonaHorariaID");

            string query = tieneZonaHorariaId
                ? @"UPDATE Aeropuerto
                    SET Nombre = @Nombre, Codigo = @Codigo, CiudadID = @CiudadID, ZonaHorariaID = @ZonaHorariaID
                    WHERE ID = @Id"
                : @"UPDATE Aeropuerto
                    SET Nombre = @Nombre, Codigo = @Codigo, CiudadID = @CiudadID
                    WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", aeropuerto.Id);
            command.Parameters.AddWithValue("@Nombre", aeropuerto.Nombre);
            command.Parameters.AddWithValue("@Codigo", aeropuerto.Codigo);
            command.Parameters.AddWithValue("@CiudadID", aeropuerto.CiudadId);
            if (tieneZonaHorariaId)
                command.Parameters.AddWithValue("@ZonaHorariaID",
                    aeropuerto.ZonaHorariaId.HasValue
                        ? (object)aeropuerto.ZonaHorariaId.Value
                        : DBNull.Value);

            return await command.ExecuteNonQueryAsync() > 0;
        }

        public async Task<bool> Eliminar(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var deleteImagen = "DELETE FROM ImagenAeropuerto WHERE AeropuertoID = @Id";
            using var cmdImagen = new SqlCommand(deleteImagen, connection);
            cmdImagen.Parameters.AddWithValue("@Id", id);
            await cmdImagen.ExecuteNonQueryAsync();

            var query = "DELETE FROM Aeropuerto WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            return await command.ExecuteNonQueryAsync() > 0;
        }

        // ── ZONA HORARIA (catálogo) ────────────────────────────────────────
        // Busca el ID de una zona horaria por nombre IANA.
        // Si no existe en la tabla ZonaHoraria la crea (patrón ObtenerOCrear).
        // Devuelve null si nombreIana es null o vacío.

        public async Task<int?> ObtenerOCrearZonaHoraria(string? nombreIana)
        {
            _logger.LogInformation("=== REPO ObtenerOCrearZonaHoraria: nombreIana={Val}", nombreIana ?? "(null)");
            if (string.IsNullOrWhiteSpace(nombreIana))
                return null;

            var nombre = nombreIana.Trim();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Buscar primero
            var queryBuscar = "SELECT ID FROM ZonaHoraria WHERE Nombre = @Nombre";
            using var cmdBuscar = new SqlCommand(queryBuscar, connection);
            cmdBuscar.Parameters.AddWithValue("@Nombre", nombre);

            var existingId = await cmdBuscar.ExecuteScalarAsync();
            if (existingId != null)
                return Convert.ToInt32(existingId);

            // Si no existe, insertar
            var queryCrear = @"
                INSERT INTO ZonaHoraria (Nombre) VALUES (@Nombre);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var cmdCrear = new SqlCommand(queryCrear, connection);
            cmdCrear.Parameters.AddWithValue("@Nombre", nombre);

            return Convert.ToInt32(await cmdCrear.ExecuteScalarAsync());
        }

        // ── IMAGEN ────────────────────────────────────────────────────────

        public async Task GuardarImagen(int aeropuertoId, string imagenBase64)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var upsert = @"
                IF EXISTS (SELECT 1 FROM ImagenAeropuerto WHERE AeropuertoID = @AeropuertoID)
                    UPDATE ImagenAeropuerto SET Imagen = @Imagen WHERE AeropuertoID = @AeropuertoID
                ELSE
                    INSERT INTO ImagenAeropuerto (ID, AeropuertoID, Imagen)
                    VALUES (@AeropuertoID, @AeropuertoID, @Imagen)";

            using var command = new SqlCommand(upsert, connection);
            command.Parameters.AddWithValue("@AeropuertoID", aeropuertoId);
            command.Parameters.AddWithValue("@Imagen", imagenBase64);
            await command.ExecuteNonQueryAsync();
        }

        public async Task EliminarImagen(int aeropuertoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "DELETE FROM ImagenAeropuerto WHERE AeropuertoID = @AeropuertoID";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@AeropuertoID", aeropuertoId);
            await command.ExecuteNonQueryAsync();
        }

        // ── FECHAS CON VUELOS ─────────────────────────────────────────────

        public async Task<List<DateTime>> ObtenerFechasConVuelos()
        {
            var fechas = new List<DateTime>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT DISTINCT Fecha 
                FROM Vuelo 
                WHERE Fecha >= CAST(GETDATE() AS DATE)
                ORDER BY Fecha";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
                fechas.Add(reader.GetDateTime(0));

            return fechas;
        }

        public async Task<List<DateTime>> ObtenerFechasConVuelosPorRuta(
    int? origenId,
    int? destinoId,
    int cantidadPersonas = 1,
    int? claseId = null,
    int maxEscalas = 3)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string filtroClase = claseId == 1
                ? "AND v.BoletosTurista   >= @cantidadPersonas"
                : claseId == 2
                    ? "AND v.BoletosEjecutivo >= @cantidadPersonas"
                    : "AND (v.BoletosTurista >= @cantidadPersonas OR v.BoletosEjecutivo >= @cantidadPersonas)";

            // ── Vuelos directos ──────────────────────────────────────────────────
            string queryDirectos = $@"
        SELECT DISTINCT v.Fecha
        FROM Vuelo v
        INNER JOIN Ruta   r ON r.ID = v.RutaID
        INNER JOIN Estado e ON e.ID = v.EstadoID
        WHERE v.Fecha  >= CAST(GETDATE() AS DATE)
          AND e.Estatus = 'A tiempo'
          {filtroClase}";

            if (origenId.HasValue) queryDirectos += " AND r.OrigenID  = @origenId";
            if (destinoId.HasValue) queryDirectos += " AND r.DestinoID = @destinoId";

            var fechas = new HashSet<DateTime>();

            using (var cmd = new SqlCommand(queryDirectos, connection))
            {
                cmd.Parameters.AddWithValue("@cantidadPersonas", cantidadPersonas);
                if (origenId.HasValue) cmd.Parameters.AddWithValue("@origenId", origenId.Value);
                if (destinoId.HasValue) cmd.Parameters.AddWithValue("@destinoId", destinoId.Value);

                using var reader = await cmd.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                    fechas.Add(reader.GetDateTime(0));
            }

            // ── Vuelos con escalas (BFS igual que VueloRepository) ──────────────
            if (!origenId.HasValue || !destinoId.HasValue)
                return fechas.OrderBy(f => f).ToList();

            // Límite de tiempo de vuelo acumulado: 1.5x la ruta directa
            int limiteMinutos;
            using (var cmd = new SqlCommand(
                "SELECT TOP 1 DuracionEstimada FROM Ruta WHERE OrigenID = @o AND DestinoID = @d", connection))
            {
                cmd.Parameters.AddWithValue("@o", origenId.Value);
                cmd.Parameters.AddWithValue("@d", destinoId.Value);
                var result = await cmd.ExecuteScalarAsync();
                limiteMinutos = result != null && result != DBNull.Value
                    ? (int)((int)result * 1.5)
    :               maxEscalas * 8 * 60;
            }

            // Traemos TODOS los tramos útiles de una sola vez:
            // - salen del origen o de cualquier aeropuerto intermedio (no el destino final como origen)
            // - llegan al destino o a cualquier aeropuerto intermedio (no el origen como destino)
            // Esto es equivalente a lo que hace BuscarTramosDesdeLista en VueloRepository
            string sqlTodos = $@"
                        SELECT
                    v.ID, v.Fecha, v.HoraSalida, v.HoraLlegada, v.FechaLlegada,
                    r.OrigenID, r.DestinoID, r.DuracionEstimada,
                    v.BoletosTurista, v.BoletosEjecutivo
                FROM Vuelo v
                INNER JOIN Ruta   r ON r.ID = v.RutaID
                INNER JOIN Estado e ON e.ID = v.EstadoID
                WHERE v.Fecha  >= CAST(GETDATE() AS DATE)
                  AND e.Estatus = 'A tiempo'
                  AND r.OrigenID != @destinoId
                  {filtroClase}
                ORDER BY v.Fecha, v.HoraSalida";

            // (int Id, DateTime Fecha, TimeSpan HoraSalida, TimeSpan HoraLlegada,
            //  DateTime FechaLlegada, int OrigenId, int DestinoId, int DuracionMinutos,
            //  int? BolTurista, int? BolEjecutiva)
            var todosTramos = new List<(int Id, DateTime Fecha, TimeSpan HoraSalida, TimeSpan HoraLlegada,
                                        DateTime FechaLlegada, int OrigenId, int DestinoId,
                                        int DuracionMinutos, int? BolTurista, int? BolEjecutiva)>();

            using (var cmd = new SqlCommand(sqlTodos, connection))
            {
                cmd.Parameters.AddWithValue("@cantidadPersonas", cantidadPersonas);
                cmd.Parameters.AddWithValue("@origenId", origenId.Value);
                cmd.Parameters.AddWithValue("@destinoId", destinoId.Value);

                using var reader = await cmd.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                {
                    todosTramos.Add((
                        Id: reader.GetInt32(0),
                        Fecha: reader.GetDateTime(1),
                        HoraSalida: reader.IsDBNull(2) ? TimeSpan.Zero : reader.GetTimeSpan(2),
                        HoraLlegada: reader.IsDBNull(3) ? TimeSpan.Zero : reader.GetTimeSpan(3),
                        FechaLlegada: reader.GetDateTime(4),
                        OrigenId: reader.GetInt32(5),
                        DestinoId: reader.GetInt32(6),
                        DuracionMinutos: reader.IsDBNull(7) ? 0 : reader.GetInt32(7),
                        BolTurista: reader.IsDBNull(8) ? (int?)null : reader.GetInt32(8),
                        BolEjecutiva: reader.IsDBNull(9) ? (int?)null : reader.GetInt32(9)
                    ));
                }
            }

            // Indexamos por OrigenId para no hacer Where en cada iteración del BFS
            var tramosPorOrigen = todosTramos
                .GroupBy(t => t.OrigenId)
                .ToDictionary(g => g.Key, g => g.ToList());

            // BFS: cada camino activo es (FechaInicioViaje, FechaLlegadaUltimoTramo, HoraLlegadaUltimoTramo,
            //                             DuracionVueloAcumulada, UltimoDestinoId, AeropuertosVisitados,
            //                             BolTurista, BolEjecutiva)
            var caminosActivos = new List<(DateTime FechaInicio, DateTime FechaLlegada, TimeSpan HoraLlegada,
                                           int DuracionAcumulada, int UltimoDestino,
                                           HashSet<int> Visitados, int? BolTurista, int? BolEjecutiva)>();

            // Capa 0: tramos que salen del origen (excluimos destino final, igual que VueloRepository)
            if (tramosPorOrigen.TryGetValue(origenId.Value, out var tramosIniciales))
            {
                foreach (var t in tramosIniciales.Where(t => t.DestinoId != destinoId.Value))
                {
                    if (t.DuracionMinutos > limiteMinutos) continue;

                    caminosActivos.Add((
                        FechaInicio: t.Fecha.Date,
                        FechaLlegada: t.FechaLlegada,
                        HoraLlegada: t.HoraLlegada,
                        DuracionAcumulada: t.DuracionMinutos,
                        UltimoDestino: t.DestinoId,
                        Visitados: new HashSet<int> { origenId.Value, t.DestinoId },
                        BolTurista: t.BolTurista,
                        BolEjecutiva: t.BolEjecutiva
                    ));
                }
            }

            var combinacionesVistas = new HashSet<string>();

            for (int escala = 1; escala <= maxEscalas && caminosActivos.Count > 0; escala++)
            {
                var siguienteCapa = new List<(DateTime FechaInicio, DateTime FechaLlegada, TimeSpan HoraLlegada,
                                              int DuracionAcumulada, int UltimoDestino,
                                              HashSet<int> Visitados, int? BolTurista, int? BolEjecutiva)>();

                foreach (var camino in caminosActivos)
                {
                    if (!tramosPorOrigen.TryGetValue(camino.UltimoDestino, out var candidatos))
                        continue;

                    foreach (var tramo in candidatos)
                    {
                        // Sin ciclos
                        if (camino.Visitados.Contains(tramo.DestinoId) && tramo.DestinoId != destinoId.Value)
                            continue;

                        // Validar escala: entre 1h y 12h
                        var llegadaAnterior = camino.FechaLlegada.Date + camino.HoraLlegada;
                        var salidaActual = tramo.Fecha.Date + tramo.HoraSalida;
                        int minutosEscala = (int)(salidaActual - llegadaAnterior).TotalMinutes;

                        if (minutosEscala < 60 || minutosEscala > 720) continue;

                        // Validar duración acumulada de vuelo (sin escalas)
                        int nuevaDuracion = camino.DuracionAcumulada + tramo.DuracionMinutos;
                        if (nuevaDuracion > limiteMinutos) continue;

                        // Disponibilidad mínima entre tramos
                        int? dispTurista = camino.BolTurista.HasValue && tramo.BolTurista.HasValue
                            ? Math.Min(camino.BolTurista.Value, tramo.BolTurista.Value) : null;
                        int? dispEjecutiva = camino.BolEjecutiva.HasValue && tramo.BolEjecutiva.HasValue
                            ? Math.Min(camino.BolEjecutiva.Value, tramo.BolEjecutiva.Value) : null;

                        if (claseId == 1 && (dispTurista == null || dispTurista < cantidadPersonas)) continue;
                        if (claseId == 2 && (dispEjecutiva == null || dispEjecutiva < cantidadPersonas)) continue;
                        if (claseId == null &&
                            (dispTurista == null || dispTurista < cantidadPersonas) &&
                            (dispEjecutiva == null || dispEjecutiva < cantidadPersonas)) continue;

                        // Clave única para no repetir combinaciones
                        string key = $"{camino.FechaInicio:yyyyMMdd}-{camino.UltimoDestino}-{tramo.Id}";
                        if (!combinacionesVistas.Add(key)) continue;

                        // Si llegamos al destino final, registramos la fecha de inicio del viaje
                        if (tramo.DestinoId == destinoId.Value)
                        {
                            fechas.Add(camino.FechaInicio);
                            continue;
                        }

                        // Si no, seguimos expandiendo
                        if (escala < maxEscalas)
                        {
                            var nuevosVisitados = new HashSet<int>(camino.Visitados) { tramo.DestinoId };
                            siguienteCapa.Add((
                                FechaInicio: camino.FechaInicio,
                                FechaLlegada: tramo.FechaLlegada,
                                HoraLlegada: tramo.HoraLlegada,
                                DuracionAcumulada: nuevaDuracion,
                                UltimoDestino: tramo.DestinoId,
                                Visitados: nuevosVisitados,
                                BolTurista: dispTurista,
                                BolEjecutiva: dispEjecutiva
                            ));
                        }
                    }
                }

                caminosActivos = siguienteCapa;
            }

            return fechas.OrderBy(f => f).ToList();
        }
        // ── CIUDADES / PAÍSES ─────────────────────────────────────────────

        public async Task<List<CiudadDTO>> ObtenerCiudades()
        {
            var ciudades = new List<CiudadDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    c.ID, c.Nombre, c.PaisID, p.Nombre AS NombrePais
                FROM Ciudad c
                INNER JOIN Pais p ON c.PaisID = p.ID
                ORDER BY p.Nombre, c.Nombre";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                ciudades.Add(new CiudadDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    PaisId = reader.GetInt32(2),
                    NombrePais = reader.GetString(3),
                    NombreCompleto = $"{reader.GetString(1)}, {reader.GetString(3)}"
                });
            }

            return ciudades;
        }

        public async Task<int> ObtenerOCrearPais(string nombrePais)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var queryBuscar = "SELECT ID FROM Pais WHERE LOWER(Nombre) = LOWER(@Nombre)";
            using var commandBuscar = new SqlCommand(queryBuscar, connection);
            commandBuscar.Parameters.AddWithValue("@Nombre", nombrePais.Trim());

            var paisId = await commandBuscar.ExecuteScalarAsync();
            if (paisId != null) return Convert.ToInt32(paisId);

            var queryCrear = @"
                INSERT INTO Pais (Nombre) VALUES (@Nombre);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var commandCrear = new SqlCommand(queryCrear, connection);
            commandCrear.Parameters.AddWithValue("@Nombre", nombrePais.Trim());

            return Convert.ToInt32(await commandCrear.ExecuteScalarAsync());
        }

        public async Task<int> ObtenerOCrearCiudad(string nombreCiudad, int paisId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var queryBuscar = @"
                SELECT ID FROM Ciudad 
                WHERE LOWER(Nombre) = LOWER(@Nombre) AND PaisID = @PaisID";

            using var commandBuscar = new SqlCommand(queryBuscar, connection);
            commandBuscar.Parameters.AddWithValue("@Nombre", nombreCiudad.Trim());
            commandBuscar.Parameters.AddWithValue("@PaisID", paisId);

            var ciudadId = await commandBuscar.ExecuteScalarAsync();
            if (ciudadId != null) return Convert.ToInt32(ciudadId);

            var queryCrear = @"
                INSERT INTO Ciudad (Nombre, PaisID) VALUES (@Nombre, @PaisID);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var commandCrear = new SqlCommand(queryCrear, connection);
            commandCrear.Parameters.AddWithValue("@Nombre", nombreCiudad.Trim());
            commandCrear.Parameters.AddWithValue("@PaisID", paisId);

            return Convert.ToInt32(await commandCrear.ExecuteScalarAsync());
        }


        private static async Task<bool> ColumnaExiste(SqlConnection connection, string tabla, string columna)
        {
            var query = @"
                SELECT COUNT(1)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME   = @Tabla
                  AND COLUMN_NAME  = @Columna";
            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@Tabla", tabla);
            cmd.Parameters.AddWithValue("@Columna", columna);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0;
        }

    }
}