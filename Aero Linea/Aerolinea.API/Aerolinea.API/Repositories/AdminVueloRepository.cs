using Aerolinea.API.Data;
using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Services;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AdminVueloRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public AdminVueloRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // ─────────────────────────────────────────────────────────────────
        //  CREAR VUELO
        // ─────────────────────────────────────────────────────────────────
        public async Task<int> CrearVuelo(CrearVueloAdminDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar o crear la ruta y obtener su duración
                var (rutaId, duracionMinutos) = await ObtenerOCrearRuta(
                    dto.AeropuertoOrigenId, dto.AeropuertoDestinoId, connection, transaction);

                // 2. Obtener zonas horarias via FK → ZonaHoraria.Nombre
                var (tzOrigen, tzDestino) = await ObtenerZonasHorarias(
                    dto.AeropuertoOrigenId, dto.AeropuertoDestinoId, connection, transaction);

                // 3. Calcular hora y fecha de llegada automáticamente
                var horaSalida = TimeSpan.Parse(dto.HoraSalida);
                var (horaLlegada, fechaLlegada, _, _) = RutaService.CalcularLlegadaConZonas(
                    dto.Fecha, horaSalida, duracionMinutos, tzOrigen, tzDestino);

                // 4. Validar que los boletos no superen la capacidad del avión
                int capacidadAvion = await ObtenerCapacidadAvion(dto.AvionId, connection, transaction);
                if (dto.BoletosTurista + dto.BoletosEjecutivo > capacidadAvion)
                    throw new ArgumentException(
                        $"La suma de boletos ({dto.BoletosTurista + dto.BoletosEjecutivo}) " +
                        $"supera la capacidad del avión ({capacidadAvion}).");

                // 5. Crear el vuelo
                var insertVuelo = @"
                    INSERT INTO Vuelo
                        (NumeroVuelo, Fecha, HoraSalida, HoraLlegada, FechaLlegada, EstadoID,
                         AvionID, RutaID, BoletosTurista, BoletosEjecutivo,
                         PrecioTurista, PrecioEjecutivo)
                    OUTPUT INSERTED.ID
                    VALUES
                        (@NumeroVuelo, @Fecha, @HoraSalida, @HoraLlegada, @FechaLlegada, @EstadoId,
                         @AvionId, @RutaId, @BoletosTurista, @BoletosEjecutivo,
                         @PrecioTurista, @PrecioEjecutivo)";

                int vueloId;
                using (var cmd = new SqlCommand(insertVuelo, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@NumeroVuelo", dto.NumeroVuelo);
                    cmd.Parameters.AddWithValue("@Fecha", dto.Fecha.Date);
                    cmd.Parameters.AddWithValue("@HoraSalida", horaSalida);
                    cmd.Parameters.AddWithValue("@HoraLlegada", horaLlegada);
                    cmd.Parameters.AddWithValue("@FechaLlegada", fechaLlegada.Date);
                    cmd.Parameters.AddWithValue("@EstadoId", 1);
                    cmd.Parameters.AddWithValue("@AvionId", dto.AvionId);
                    cmd.Parameters.AddWithValue("@RutaId", rutaId);
                    cmd.Parameters.AddWithValue("@BoletosTurista", dto.BoletosTurista);
                    cmd.Parameters.AddWithValue("@BoletosEjecutivo", dto.BoletosEjecutivo);
                    cmd.Parameters.AddWithValue("@PrecioTurista", dto.PrecioTurista);
                    cmd.Parameters.AddWithValue("@PrecioEjecutivo", dto.PrecioEjecutiva);
                    vueloId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                // 6. Asignar tripulación
                if (dto.TripulantesIds != null && dto.TripulantesIds.Count > 0)
                {
                    foreach (var tripulanteId in dto.TripulantesIds)
                    {
                        var insertTrip = @"
                            INSERT INTO EquipoPivote (VueloID, MiembroTripulacionID)
                            VALUES (@VueloId, @TripulanteId)";

                        using var cmdTrip = new SqlCommand(insertTrip, connection, transaction);
                        cmdTrip.Parameters.AddWithValue("@VueloId", vueloId);
                        cmdTrip.Parameters.AddWithValue("@TripulanteId", tripulanteId);
                        await cmdTrip.ExecuteNonQueryAsync();
                    }
                }

                transaction.Commit();
                return vueloId;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        // ─────────────────────────────────────────────────────────────────
        //  HELPERS PRIVADOS
        // ─────────────────────────────────────────────────────────────────

        private async Task<(int rutaId, int duracion)> ObtenerOCrearRuta(
            int origenId, int destinoId, SqlConnection connection, SqlTransaction transaction)
        {
            var queryBuscar = @"
                SELECT ID, DuracionEstimada FROM Ruta
                WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId";

            using var cmdBuscar = new SqlCommand(queryBuscar, connection, transaction);
            cmdBuscar.Parameters.AddWithValue("@OrigenId", origenId);
            cmdBuscar.Parameters.AddWithValue("@DestinoId", destinoId);

            using var reader = await cmdBuscar.ExecuteReaderAsync();
            if (await reader.ReadAsync())
            {
                var id = reader.GetInt32(0);
                var duracion = reader.GetInt32(1);
                return (id, duracion);
            }
            await reader.CloseAsync();

            var queryCrear = @"
                INSERT INTO Ruta (OrigenID, DestinoID, DuracionEstimada)
                OUTPUT INSERTED.ID
                VALUES (@OrigenId, @DestinoId, @DuracionEstimada)";

            using var cmdCrear = new SqlCommand(queryCrear, connection, transaction);
            cmdCrear.Parameters.AddWithValue("@OrigenId", origenId);
            cmdCrear.Parameters.AddWithValue("@DestinoId", destinoId);
            cmdCrear.Parameters.AddWithValue("@DuracionEstimada", 120);

            return (Convert.ToInt32(await cmdCrear.ExecuteScalarAsync()), 120);
        }

        /// <summary>
        /// Obtiene los identificadores IANA de dos aeropuertos
        /// haciendo JOIN con la tabla ZonaHoraria.
        /// Devuelve null si el aeropuerto no tiene zona asignada.
        /// </summary>
        private async Task<(string? tzOrigen, string? tzDestino)> ObtenerZonasHorarias(
            int origenId, int destinoId, SqlConnection connection, SqlTransaction transaction)
        {
            var query = @"
                SELECT a.ID, zh.Nombre
                FROM  Aeropuerto a
                LEFT  JOIN ZonaHoraria zh ON zh.ID = a.ZonaHorariaID
                WHERE a.ID IN (@OrigenId, @DestinoId)";

            using var cmd = new SqlCommand(query, connection, transaction);
            cmd.Parameters.AddWithValue("@OrigenId", origenId);
            cmd.Parameters.AddWithValue("@DestinoId", destinoId);

            string? tzOrigen = null, tzDestino = null;
            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                var id = reader.GetInt32(0);
                var tz = reader.IsDBNull(1) ? null : reader.GetString(1);
                if (id == origenId) tzOrigen = tz;
                if (id == destinoId) tzDestino = tz;
            }

            return (tzOrigen, tzDestino);
        }

        private async Task<int> ObtenerCapacidadAvion(
            int avionId, SqlConnection connection, SqlTransaction transaction)
        {
            const string query = "SELECT CapacidadPasajeros FROM Avion WHERE ID = @AvionId";
            using var cmd = new SqlCommand(query, connection, transaction);
            cmd.Parameters.AddWithValue("@AvionId", avionId);
            var resultado = await cmd.ExecuteScalarAsync();
            return resultado != null ? (int)resultado : 0;
        }

        // ─────────────────────────────────────────────────────────────────
        //  HISTORIAL
        //  JOIN con ZonaHoraria para resolver los identificadores IANA.
        // ─────────────────────────────────────────────────────────────────
        public async Task<List<VueloHistorialDTO>> ObtenerHistorialVuelos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT
                    v.ID,
                    v.NumeroVuelo,
                    aorigen.Codigo  + ' - ' + corigen.Nombre   AS Origen,
                    adestino.Codigo + ' - ' + cdestino.Nombre  AS Destino,
                    v.Fecha,
                    v.HoraSalida,
                    v.HoraLlegada,
                    v.FechaLlegada,
                    v.EstadoID,
                    av.CapacidadPasajeros,
                    v.BoletosTurista,
                    v.BoletosEjecutivo,
                    v.PrecioTurista,
                    v.PrecioEjecutivo,
                    zho.Nombre AS TzOrigen,
                    zhd.Nombre AS TzDestino
                FROM  Vuelo v
                INNER JOIN Ruta r              ON r.ID  = v.RutaID
                INNER JOIN Aeropuerto aorigen  ON aorigen.ID  = r.OrigenID
                INNER JOIN Aeropuerto adestino ON adestino.ID = r.DestinoID
                INNER JOIN Ciudad corigen      ON corigen.ID  = aorigen.CiudadID
                INNER JOIN Ciudad cdestino     ON cdestino.ID = adestino.CiudadID
                INNER JOIN Avion av            ON av.ID = v.AvionID
                LEFT  JOIN ZonaHoraria zho     ON zho.ID = aorigen.ZonaHorariaID
                LEFT  JOIN ZonaHoraria zhd     ON zhd.ID = adestino.ZonaHorariaID
                ORDER BY v.Fecha DESC, v.HoraSalida DESC";

            using var cmd = new SqlCommand(query, connection);
            using var reader = await cmd.ExecuteReaderAsync();

            var vuelos = new List<VueloHistorialDTO>();
            while (await reader.ReadAsync())
            {
                var estadoId = reader.GetInt32(8);
                string estado = estadoId switch
                {
                    1 => "Activo",
                    2 => "En curso",
                    3 => "Finalizado",
                    4 => "Cancelado",
                    _ => "Activo"
                };

                int capacidad = reader.GetInt32(9);
                int bolTurista = reader.IsDBNull(10) ? 0 : reader.GetInt32(10);
                int bolEjecutivo = reader.IsDBNull(11) ? 0 : reader.GetInt32(11);
                int boletosVendidos = capacidad - (bolTurista + bolEjecutivo);

                vuelos.Add(new VueloHistorialDTO
                {
                    Id = reader.GetInt32(0),
                    NumeroVuelo = reader.GetString(1),
                    Origen = reader.GetString(2),
                    Destino = reader.GetString(3),
                    Fecha = reader.GetDateTime(4).ToString("yyyy-MM-dd"),
                    HoraSalida = reader.GetTimeSpan(5).ToString(@"hh\:mm"),
                    HoraLlegada = reader.GetTimeSpan(6).ToString(@"hh\:mm"),
                    FechaLlegada = reader.IsDBNull(7) ? null : reader.GetDateTime(7).ToString("yyyy-MM-dd"),
                    Estado = estado,
                    AsientosTotales = capacidad,
                    BoletosTurista = bolTurista,
                    BoletosEjecutivo = bolEjecutivo,
                    AsientosVendidos = boletosVendidos,
                    PrecioTurista = reader.IsDBNull(12) ? 0 : reader.GetDecimal(12),
                    PrecioEjecutiva = reader.IsDBNull(13) ? 0 : reader.GetDecimal(13)
                });
            }

            return vuelos;
        }

        // ─────────────────────────────────────────────────────────────────
        //  CANCELAR VUELO
        // ─────────────────────────────────────────────────────────────────
        public async Task<bool> CancelarVuelo(int vueloId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                var queryVuelo = @"
                    UPDATE Vuelo
                    SET EstadoID         = 4,
                        BoletosTurista   = 0,
                        BoletosEjecutivo = 0
                    WHERE ID = @VueloId AND EstadoID IN (1, 2)";

                int filasVuelo;
                using (var cmd = new SqlCommand(queryVuelo, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@VueloId", vueloId);
                    filasVuelo = await cmd.ExecuteNonQueryAsync();
                }

                if (filasVuelo == 0) { transaction.Rollback(); return false; }

                var reservacionIds = new List<int>();
                var queryReservaciones = @"
                    SELECT DISTINCT ReservacionID
                    FROM Boleto
                    WHERE VueloID = @VueloId
                      AND ReservacionID IS NOT NULL
                      AND EstadoBoletoID IN (2, 3)";

                using (var cmd = new SqlCommand(queryReservaciones, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@VueloId", vueloId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                        reservacionIds.Add(reader.GetInt32(0));
                }

                var queryBoletos = @"
                    UPDATE Boleto
                    SET EstadoBoletoID = 4
                    WHERE VueloID = @VueloId AND EstadoBoletoID IN (2, 3)";

                using (var cmd = new SqlCommand(queryBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@VueloId", vueloId);
                    await cmd.ExecuteNonQueryAsync();
                }

                if (reservacionIds.Count > 0)
                {
                    var ids = string.Join(",", reservacionIds);
                    var queryCancel = $@"
                        UPDATE Reservacion
                        SET EstadoReservaID   = 3,
                            FechaCancelacion  = GETDATE(),
                            MotivoCancelacion = 'Vuelo cancelado por la aerolínea'
                        WHERE ID IN ({ids})
                          AND EstadoReservaID NOT IN (3, 4)";

                    using var cmd = new SqlCommand(queryCancel, connection, transaction);
                    await cmd.ExecuteNonQueryAsync();
                }

                transaction.Commit();
                return true;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }
        // ─────────────────────────────────────────────────────────────────
        //  DISPONIBILIDAD
        // ─────────────────────────────────────────────────────────────────

        /// <summary>
        /// Devuelve los IDs de aviones que NO están disponibles para un vuelo
        /// que parte de @aeropuertoOrigenId en la fecha/hora indicada.
        ///
        /// Un avión NO está disponible si:
        ///   a) Todavía está en vuelo al momento de la nueva salida, O
        ///   b) Aterrizó en el mismo aeropuerto de origen y hace menos de 24 h, O
        ///   c) Aterrizó en un aeropuerto diferente y hace menos de 48 h.
        ///
        /// FechaLlegada + HoraLlegada = momento real de aterrizaje.
        /// </summary>
        public async Task<HashSet<int>> ObtenerAvionesOcupados(
            DateTime fecha, TimeSpan horaSalida, int aeropuertoOrigenId)
        {
            var ocupados = new HashSet<int>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var salidaDateTime = fecha.Date + horaSalida;

            // Verificar si las columnas FechaLlegada y HoraLlegada existen
            bool tieneFechaLlegada = await ColumnaExiste(connection, "Vuelo", "FechaLlegada");
            bool tieneHoraLlegada  = await ColumnaExiste(connection, "Vuelo", "HoraLlegada");

            string query;
            if (tieneFechaLlegada && tieneHoraLlegada)
            {
                // Consulta completa con reglas de 24h (mismo aeropuerto) y 48h (diferente aeropuerto)
                query = @"
                    SELECT DISTINCT v.AvionID
                    FROM  Vuelo v
                    INNER JOIN Ruta r ON r.ID = v.RutaID
                    WHERE v.EstadoID <> 4
                      AND (
                        /* Regla 1: el avión sigue en el aire al momento de la nueva salida */
                        DATEADD(SECOND, DATEDIFF(SECOND, 0, v.HoraLlegada),
                                CAST(v.FechaLlegada AS DATETIME)) > @SalidaDateTime

                        OR

                        /* Regla 2: aterrizó en el mismo aeropuerto → requiere 24 h de descanso */
                        (
                            r.DestinoID = @AeropuertoOrigenId
                            AND DATEADD(SECOND, DATEDIFF(SECOND, 0, v.HoraLlegada),
                                        CAST(v.FechaLlegada AS DATETIME)) <= @SalidaDateTime
                            AND DATEADD(SECOND, DATEDIFF(SECOND, 0, v.HoraLlegada),
                                        CAST(v.FechaLlegada AS DATETIME)) > DATEADD(HOUR, -24, @SalidaDateTime)
                        )

                        OR

                        /* Regla 3: aterrizó en aeropuerto diferente → requiere 48 h de descanso */
                        (
                            r.DestinoID <> @AeropuertoOrigenId
                            AND DATEADD(SECOND, DATEDIFF(SECOND, 0, v.HoraLlegada),
                                        CAST(v.FechaLlegada AS DATETIME)) <= @SalidaDateTime
                            AND DATEADD(SECOND, DATEDIFF(SECOND, 0, v.HoraLlegada),
                                        CAST(v.FechaLlegada AS DATETIME)) > DATEADD(HOUR, -48, @SalidaDateTime)
                        )
                      )";
            }
            else
            {
                // Fallback: bloquear si tiene cualquier vuelo activo el mismo día
                query = @"
                    SELECT DISTINCT AvionID
                    FROM   Vuelo
                    WHERE  Fecha    = @Fecha
                      AND  EstadoID <> 4";
            }

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@Fecha", fecha.Date);
            if (tieneFechaLlegada && tieneHoraLlegada)
            {
                cmd.Parameters.AddWithValue("@SalidaDateTime",    salidaDateTime);
                cmd.Parameters.AddWithValue("@AeropuertoOrigenId", aeropuertoOrigenId);
            }

            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
                ocupados.Add(reader.GetInt32(0));

            return ocupados;
        }

        /// <summary>
        /// Devuelve los IDs de tripulantes que NO están disponibles para
        /// la fecha/hora indicada.
        ///
        /// Un tripulante NO está disponible si:
        ///   a) Tiene un vuelo asignado el mismo día (EstadoID != 4), O
        ///   b) Su vuelo más reciente finaliza menos de 24h antes de la
        ///      fecha/hora de salida solicitada.
        ///
        /// FechaLlegada + HoraLlegada = momento real de fin del vuelo.
        /// </summary>
        public async Task<HashSet<int>> ObtenerTripulantesOcupados(DateTime fecha, TimeSpan horaSalida)
        {
            var ocupados = new HashSet<int>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Verificar si las columnas FechaLlegada y HoraLlegada existen en la tabla Vuelo
            bool tieneFechaLlegada = await ColumnaExiste(connection, "Vuelo", "FechaLlegada");
            bool tieneHoraLlegada = await ColumnaExiste(connection, "Vuelo", "HoraLlegada");

            var salidaDateTime = fecha.Date + horaSalida;

            string query;
            if (tieneFechaLlegada && tieneHoraLlegada)
            {
                // Consulta completa con regla de 24h usando FechaLlegada + HoraLlegada
                query = @"
                    SELECT DISTINCT ep.MiembroTripulacionID
                    FROM EquipoPivote ep
                    INNER JOIN Vuelo v ON v.ID = ep.VueloID
                    WHERE v.EstadoID <> 4
                      AND (
                        v.Fecha = @Fecha
                        OR (
                            v.FechaLlegada IS NOT NULL
                            AND v.HoraLlegada  IS NOT NULL
                            AND DATEADD(
                                    SECOND,
                                    DATEDIFF(SECOND, 0, v.HoraLlegada),
                                    CAST(v.FechaLlegada AS DATETIME)
                                ) > DATEADD(HOUR, -24, @SalidaDateTime)
                            AND DATEADD(
                                    SECOND,
                                    DATEDIFF(SECOND, 0, v.HoraLlegada),
                                    CAST(v.FechaLlegada AS DATETIME)
                                ) <= @SalidaDateTime
                        )
                      )";
            }
            else
            {
                // Fallback: solo verificar si tienen vuelo el mismo día
                query = @"
                    SELECT DISTINCT ep.MiembroTripulacionID
                    FROM EquipoPivote ep
                    INNER JOIN Vuelo v ON v.ID = ep.VueloID
                    WHERE v.EstadoID <> 4
                      AND v.Fecha = @Fecha";
            }

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@Fecha", fecha.Date);
            if (tieneFechaLlegada && tieneHoraLlegada)
                cmd.Parameters.AddWithValue("@SalidaDateTime", salidaDateTime);
            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
                ocupados.Add(reader.GetInt32(0));

            return ocupados;
        }

        private static async Task<bool> ColumnaExiste(SqlConnection connection, string tabla, string columna)
        {
            const string q = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME=@T AND COLUMN_NAME=@C";
            using var cmd = new SqlCommand(q, connection);
            cmd.Parameters.AddWithValue("@T", tabla);
            cmd.Parameters.AddWithValue("@C", columna);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0;
        }

    }
}