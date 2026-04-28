using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Services;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de administracion de vuelos. Gestiona la creacion, cancelacion,
    /// historial y disponibilidad de aviones y tripulacion en la base de datos.
    /// </summary>
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
        /// <summary>
        /// Crea un nuevo vuelo en la base de datos dentro de una transaccion.
        /// Verifica o crea la ruta, calcula la hora de llegada segun zonas horarias,
        /// valida la capacidad del avion y asigna la tripulacion indicada.
        /// Retorna el ID del vuelo creado.
        /// </summary>
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
                // NOTA: Se usa SCOPE_IDENTITY() en lugar de OUTPUT INSERTED.ID porque la tabla
                // Vuelo tiene el trigger trg_Vuelo_Auditoria (AFTER INSERT) y SQL Server lanza
                // el error 334 cuando se usa OUTPUT sin INTO en tablas con triggers habilitados.
                var insertVuelo = @"
                    INSERT INTO Vuelo
                        (NumeroVuelo, Fecha, HoraSalida, HoraLlegada, FechaLlegada, EstadoID,
                         AvionID, RutaID, BoletosTurista, BoletosEjecutivo,
                         PrecioTurista, PrecioEjecutivo)
                    VALUES
                        (@NumeroVuelo, @Fecha, @HoraSalida, @HoraLlegada, @FechaLlegada, @EstadoId,
                         @AvionId, @RutaId, @BoletosTurista, @BoletosEjecutivo,
                         @PrecioTurista, @PrecioEjecutiva);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

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
                    cmd.Parameters.AddWithValue("@PrecioEjecutiva", dto.PrecioEjecutiva);
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
        /// <summary>
        /// Obtiene el historial completo de vuelos con informacion de ruta, avion,
        /// estado, boletos vendidos y zonas horarias. Los resultados se ordenan
        /// por fecha descendente.
        /// </summary>
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
                    zhd.Nombre AS TzDestino,
                    av.ID      AS AvionID,
                    av.Marca + ' ' + av.Modelo AS AvionNombre,
                    (SELECT COUNT(*) FROM Boleto b WHERE b.VueloID = v.ID AND b.EstadoBoletoID IN (2, 3)) AS BoletosVendidosReal,
                    r.OrigenID  AS AeropuertoOrigenId,
                    r.DestinoID AS AeropuertoDestinoId
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
                    PrecioEjecutiva = reader.IsDBNull(13) ? 0 : reader.GetDecimal(13),
                    AvionId = reader.GetInt32(16),
                    AvionNombre = reader.IsDBNull(17) ? "" : reader.GetString(17),
                    BoletosVendidosReal = reader.IsDBNull(18) ? 0 : reader.GetInt32(18),
                    AeropuertoOrigenId  = reader.GetInt32(19),
                    AeropuertoDestinoId = reader.GetInt32(20)
                });
            }

            return vuelos;
        }

        // ─────────────────────────────────────────────────────────────────
        //  CANCELAR VUELO
        // ─────────────────────────────────────────────────────────────────
        /// <summary>
        /// Cancela un vuelo activo o en curso. Marca el vuelo con estado Cancelado,
        /// cancela todos los boletos activos y actualiza las reservaciones relacionadas
        /// al estado Cancelado dentro de una transaccion atomica.
        /// </summary>
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
        //  USUARIOS AFECTADOS POR CANCELACION DE VUELO
        // ─────────────────────────────────────────────────────────────────

        /// <summary>
        /// Representa los datos de un usuario afectado por la cancelacion de un vuelo.
        /// Se usa para construir el correo masivo de notificacion.
        /// </summary>
        public record UsuarioAfectadoVuelo(
            int    ReservacionID,
            string NoReservacion,
            string NombreUsuario,
            string EmailUsuario,
            string NumeroVuelo,
            string OrigenCodigo,
            string DestinoCodigo,
            string FechaVuelo
        );

        /// <summary>
        /// Retorna la lista de usuarios con reservaciones activas en el vuelo indicado,
        /// incluyendo datos de contacto y datos del vuelo para el correo de notificacion.
        /// Solo considera boletos en estado Activo (2) o Vendido (3) cuyas reservaciones
        /// no esten ya canceladas o expiradas.
        /// Debe llamarse ANTES de ejecutar la cancelacion del vuelo.
        /// </summary>
        public async Task<List<UsuarioAfectadoVuelo>> ObtenerAfectadosPorVuelo(int vueloId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // JOIN completo para obtener email, nombre, numero de reservacion
            // y datos del vuelo (ruta y fecha) en una sola consulta
            var query = @"
                SELECT DISTINCT
                    r.ID                          AS ReservacionID,
                    r.NoReservacion,
                    u.Nombre + ' ' + u.Apellido  AS NombreUsuario,
                    u.Correo                      AS EmailUsuario,
                    v.NumeroVuelo,
                    ao.Codigo                     AS OrigenCodigo,
                    ad.Codigo                     AS DestinoCodigo,
                    CONVERT(VARCHAR(10), v.Fecha, 120) AS FechaVuelo
                FROM  Boleto b
                INNER JOIN Reservacion r  ON r.ID  = b.ReservacionID
                INNER JOIN Usuario u      ON u.ID  = r.UsuarioID
                INNER JOIN Vuelo v        ON v.ID  = b.VueloID
                INNER JOIN Ruta ru        ON ru.ID = v.RutaID
                INNER JOIN Aeropuerto ao  ON ao.ID = ru.OrigenID
                INNER JOIN Aeropuerto ad  ON ad.ID = ru.DestinoID
                WHERE b.VueloID            = @VueloId
                  AND b.ReservacionID      IS NOT NULL
                  AND b.EstadoBoletoID     IN (2, 3)
                  AND r.EstadoReservaID    IN (1, 2)";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@VueloId", vueloId);

            var afectados = new List<UsuarioAfectadoVuelo>();
            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                afectados.Add(new UsuarioAfectadoVuelo(
                    ReservacionID: reader.IsDBNull(0) ? 0 : reader.GetInt32(0),
                    NoReservacion: reader.IsDBNull(1) ? "" : reader.GetString(1),
                    NombreUsuario: reader.IsDBNull(2) ? "" : reader.GetString(2),
                    EmailUsuario:  reader.IsDBNull(3) ? "" : reader.GetString(3),
                    NumeroVuelo:   reader.IsDBNull(4) ? "" : reader.GetString(4),
                    OrigenCodigo:  reader.IsDBNull(5) ? "" : reader.GetString(5),
                    DestinoCodigo: reader.IsDBNull(6) ? "" : reader.GetString(6),
                    FechaVuelo:    reader.IsDBNull(7) ? "" : reader.GetString(7)
                ));
            }

            return afectados;
        }

        // ─────────────────────────────────────────────────────────────────
        //  DISPONIBILIDAD
        // ─────────────────────────────────────────────────────────────────

        /// <summary>
        /// Devuelve los IDs de aviones que no estan disponibles para un vuelo nuevo.
        /// Un avion no esta disponible si:
        /// a) Todavia esta en vuelo al momento de la nueva salida.
        /// b) Aterrizo en el mismo aeropuerto de origen hace menos de 24 horas.
        /// c) Aterrizo en un aeropuerto diferente hace menos de 48 horas.
        /// FechaLlegada + HoraLlegada define el momento real de aterrizaje.
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
        /// Devuelve los IDs de tripulantes que no estan disponibles para la fecha y hora indicada.
        /// Un tripulante no esta disponible si:
        /// a) Tiene un vuelo asignado el mismo dia con estado activo.
        /// b) Su vuelo mas reciente finaliza menos de 24 horas antes de la salida solicitada.
        /// FechaLlegada + HoraLlegada define el momento real de fin del vuelo.
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

        // ─────────────────────────────────────────────────────────────────
        //  SIGUIENTE NÚMERO DE VUELO
        // ─────────────────────────────────────────────────────────────────
        /// <summary>
        /// Devuelve el entero siguiente al maximo numero de secuencia usado para un prefijo dado.
        /// Busca vuelos cuyo NumeroVuelo comience con "{prefijo} " y extrae el numero que sigue al espacio.
        /// Si no existen vuelos con ese prefijo devuelve 1.
        /// </summary>
        public async Task<string> ObtenerSiguienteNumeroVuelo(string prefijo)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Busca el numero maximo en la tabla Vuelo (ambos tipos de vuelo, normal y con escalas,
            // se persisten en la misma tabla, por lo que una sola consulta cubre todos los casos).
            const string query = @"
                SELECT ISNULL(
                    MAX(TRY_CAST(LTRIM(SUBSTRING(NumeroVuelo, LEN(@Prefijo) + 2, 20)) AS INT)),
                    0
                )
                FROM Vuelo
                WHERE NumeroVuelo LIKE @PrefijoLike";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@Prefijo",     prefijo.ToUpper());
            cmd.Parameters.AddWithValue("@PrefijoLike", prefijo.ToUpper() + " %");

            var result = await cmd.ExecuteScalarAsync();
            int siguiente = Convert.ToInt32(result) + 1;
            return siguiente.ToString("D4");   // "0001", "0042", etc.
        }

        private static async Task<bool> ColumnaExiste(SqlConnection connection, string tabla, string columna)
        {
            const string q = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME=@T AND COLUMN_NAME=@C";
            using var cmd = new SqlCommand(q, connection);
            cmd.Parameters.AddWithValue("@T", tabla);
            cmd.Parameters.AddWithValue("@C", columna);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0;
        }

        // ─────────────────────────────────────────────────────────────────
        //  EDITAR VUELO — HELPERS
        // ─────────────────────────────────────────────────────────────────

        /// <summary>Obtiene un vuelo por ID para operaciones administrativas.</summary>
        public async Task<Vuelo?> ObtenerVueloPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var cmd = new SqlCommand(
                "SELECT ID, NumeroVuelo, Fecha, HoraSalida, HoraLlegada, EstadoID, AvionID, RutaID, BoletosTurista, BoletosEjecutivo, PrecioTurista, PrecioEjecutivo FROM Vuelo WHERE ID = @id",
                connection);
            cmd.Parameters.AddWithValue("@id", id);

            using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync()) return null;

            return new Vuelo
            {
                Id             = reader.GetInt32(0),
                NumeroVuelo    = reader.GetString(1),
                Fecha          = reader.GetDateTime(2),
                HoraSalida     = reader.GetTimeSpan(3),
                HoraLlegada    = reader.GetTimeSpan(4),
                EstadoId       = reader.GetInt32(5),
                AvionId        = reader.GetInt32(6),
                RutaId         = reader.GetInt32(7),
                BoletosTurista = reader.GetInt32(8),
                BoletosEjecutivo = reader.GetInt32(9),
                PrecioTurista  = reader.GetDecimal(10),
                PrecioEjecutivo = reader.GetDecimal(11)
            };
        }

        /// <summary>Obtiene datos básicos de un avión por ID.</summary>
        public async Task<Avion?> ObtenerAvionPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var cmd = new SqlCommand(
                "SELECT ID, Modelo, Marca, CapacidadPasajeros FROM Avion WHERE ID = @id",
                connection);
            cmd.Parameters.AddWithValue("@id", id);

            using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync()) return null;

            return new Avion
            {
                Id                 = reader.GetInt32(0),
                Modelo             = reader.GetString(1),
                Marca              = reader.GetString(2),
                CapacidadPasajeros = reader.GetInt32(3)
            };
        }

        /// <summary>Actualiza únicamente el avión asignado a un vuelo.</summary>
        public async Task CambiarAvionVuelo(int vueloId, int nuevoAvionId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var cmd = new SqlCommand(
                "UPDATE Vuelo SET AvionID = @AvionId WHERE ID = @VueloId",
                connection);
            cmd.Parameters.AddWithValue("@AvionId", nuevoAvionId);
            cmd.Parameters.AddWithValue("@VueloId", vueloId);
            await cmd.ExecuteNonQueryAsync();
        }

        /// <summary>Reemplaza la tripulacion completa de un vuelo con la nueva lista de tripulantes.</summary>
        public async Task CambiarTripulacionVuelo(int vueloId, List<int> nuevosTripulantesIds)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // Eliminar tripulación actual
                using (var cmdDel = new SqlCommand(
                    "DELETE FROM EquipoPivote WHERE VueloID = @vueloId", connection, transaction))
                {
                    cmdDel.Parameters.AddWithValue("@vueloId", vueloId);
                    await cmdDel.ExecuteNonQueryAsync();
                }

                // Insertar nueva tripulación
                foreach (var tripId in nuevosTripulantesIds)
                {
                    using var cmdIns = new SqlCommand(
                        "INSERT INTO EquipoPivote (VueloID, MiembroTripulacionID) VALUES (@vueloId, @tripId)",
                        connection, transaction);
                    cmdIns.Parameters.AddWithValue("@vueloId", vueloId);
                    cmdIns.Parameters.AddWithValue("@tripId", tripId);
                    await cmdIns.ExecuteNonQueryAsync();
                }

                transaction.Commit();
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        /// <summary>Cuenta los boletos vendidos/reservados (EstadoBoletoID IN (2,3)) para un vuelo.</summary>
        public async Task<int> ObtenerBoletosVendidos(int vueloId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var cmd = new SqlCommand(
                "SELECT COUNT(*) FROM Boleto WHERE VueloID = @VueloId AND EstadoBoletoID IN (2, 3)",
                connection);
            cmd.Parameters.AddWithValue("@VueloId", vueloId);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync());
        }

        /// <summary>Obtiene tripulantes con información completa por lista de IDs (incluyendo nombres de roles).</summary>
        public async Task<List<TripulanteDTO>> ObtenerTripulantesDTOPorIds(List<int> ids)
        {
            if (ids.Count == 0) return new List<TripulanteDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var paramNames = ids.Select((_, i) => $"@id{i}").ToList();
            string inClause = string.Join(", ", paramNames);

            using var cmd = new SqlCommand(
                $@"SELECT mt.ID,
                          mt.Nombre,
                          mt.Apellido,
                          mt.RolID,
                          rt.Cargo AS NombreRol,
                          mt.Imagen AS ImagenBase64
                   FROM   MiembroTripulacion mt
                   INNER JOIN RolTripulacion rt ON rt.ID = mt.RolID
                   WHERE  mt.ID IN ({inClause})",
                connection);

            for (int i = 0; i < ids.Count; i++)
                cmd.Parameters.AddWithValue(paramNames[i], ids[i]);

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<TripulanteDTO>();
            while (await reader.ReadAsync())
            {
                lista.Add(new TripulanteDTO
                {
                    Id             = reader.GetInt32(0),
                    Nombre         = reader.GetString(1),
                    Apellido       = reader.GetString(2),
                    RolID          = reader.GetInt32(3),
                    NombreRol      = reader.IsDBNull(4) ? "" : reader.GetString(4),
                    NombreCompleto = $"{reader.GetString(1)} {reader.GetString(2)}",
                    ImagenBase64   = reader.IsDBNull(5) ? null : reader.GetString(5)
                });
            }

            return lista;
        }

        /// <summary>Obtiene tripulantes por lista de IDs para validar composición de roles.</summary>
        public async Task<List<Tripulante>> ObtenerTripulantesPorIds(List<int> ids)
        {
            if (ids.Count == 0) return new List<Tripulante>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var paramNames = ids.Select((_, i) => $"@id{i}").ToList();
            string inClause = string.Join(", ", paramNames);

            using var cmd = new SqlCommand(
                $"SELECT ID, Nombre, Apellido, RolID FROM MiembroTripulacion WHERE ID IN ({inClause})",
                connection);

            for (int i = 0; i < ids.Count; i++)
                cmd.Parameters.AddWithValue(paramNames[i], ids[i]);

            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<Tripulante>();
            while (await reader.ReadAsync())
                lista.Add(new Tripulante
                {
                    Id       = reader.GetInt32(0),
                    Nombre   = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID    = reader.GetInt32(3)
                });

            return lista;
        }

        /// <summary>
        /// Obtiene la duración estimada y las zonas horarias de una ruta por su ID.
        /// Retorna (120, null, null) como fallback si la ruta no existe.
        /// </summary>
        public async Task<(int duracion, string? tzOrigen, string? tzDestino)> ObtenerInfoRutaPorId(int rutaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            bool tieneZonaHoraria =
                await ColumnaExiste(connection, "Aeropuerto", "ZonaHorariaID");

            string query = tieneZonaHoraria
                ? @"SELECT r.DuracionEstimada,
                           zho.Nombre AS TzOrigen,
                           zhd.Nombre AS TzDestino
                    FROM   Ruta r
                    INNER JOIN Aeropuerto ao  ON ao.ID = r.OrigenID
                    INNER JOIN Aeropuerto ad  ON ad.ID = r.DestinoID
                    LEFT  JOIN ZonaHoraria zho ON zho.ID = ao.ZonaHorariaID
                    LEFT  JOIN ZonaHoraria zhd ON zhd.ID = ad.ZonaHorariaID
                    WHERE  r.ID = @rutaId"
                : "SELECT DuracionEstimada, NULL, NULL FROM Ruta WHERE ID = @rutaId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);

            using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync()) return (120, null, null);

            return (
                duracion: reader.GetInt32(0),
                tzOrigen:  reader.IsDBNull(1) ? null : reader.GetString(1),
                tzDestino: reader.IsDBNull(2) ? null : reader.GetString(2)
            );
        }

        /// <summary>Actualiza los datos editables de un vuelo y reasigna la tripulación en una transacción.</summary>
        public async Task ActualizarVuelo(int vueloId, EditarVueloDTO dto, TimeSpan horaLlegada, DateTime fechaLlegada)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // Actualizar datos del vuelo (precio y boletos NO se modifican)
                using (var cmd = new SqlCommand(@"
                    UPDATE Vuelo SET
                        Fecha        = @fecha,
                        HoraSalida   = @horaSalida,
                        HoraLlegada  = @horaLlegada,
                        FechaLlegada = @fechaLlegada,
                        AvionID      = @avionId
                    WHERE ID = @vueloId", connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@fecha",        dto.Fecha.Date);
                    cmd.Parameters.AddWithValue("@horaSalida",   TimeSpan.Parse(dto.HoraSalida));
                    cmd.Parameters.AddWithValue("@horaLlegada",  horaLlegada);
                    cmd.Parameters.AddWithValue("@fechaLlegada", fechaLlegada.Date);
                    cmd.Parameters.AddWithValue("@avionId",      dto.AvionId);
                    cmd.Parameters.AddWithValue("@vueloId",      vueloId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // Reemplazar tripulación
                using (var cmdDel = new SqlCommand(
                    "DELETE FROM EquipoPivote WHERE VueloID = @vueloId", connection, transaction))
                {
                    cmdDel.Parameters.AddWithValue("@vueloId", vueloId);
                    await cmdDel.ExecuteNonQueryAsync();
                }

                foreach (var tripId in dto.TripulantesIds)
                {
                    using var cmdIns = new SqlCommand(
                        "INSERT INTO EquipoPivote (VueloID, MiembroTripulacionID) VALUES (@vueloId, @tripId)",
                        connection, transaction);
                    cmdIns.Parameters.AddWithValue("@vueloId", vueloId);
                    cmdIns.Parameters.AddWithValue("@tripId",  tripId);
                    await cmdIns.ExecuteNonQueryAsync();
                }

                transaction.Commit();
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        // ─────────────────────────────────────────────────────────────────
        //  ESCALAS — HELPERS COMPARTIDOS
        // ─────────────────────────────────────────────────────────────────

        /// <summary>
        /// Obtiene código, nombre y zona horaria de un aeropuerto.
        /// Devuelve null si el aeropuerto no existe.
        /// </summary>
        public async Task<(string codigo, string nombre, string? tz)?> ObtenerInfoAeropuerto(
            int aeropuertoId, SqlConnection connection, SqlTransaction? transaction = null)
        {
            var query = @"
                SELECT a.Codigo, a.Nombre, zh.Nombre AS Tz
                FROM   Aeropuerto a
                LEFT   JOIN ZonaHoraria zh ON zh.ID = a.ZonaHorariaID
                WHERE  a.ID = @id";

            using var cmd = transaction != null
                ? new SqlCommand(query, connection, transaction)
                : new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@id", aeropuertoId);

            using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync()) return null;

            return (
                reader.GetString(0),
                reader.GetString(1),
                reader.IsDBNull(2) ? null : reader.GetString(2)
            );
        }

        /// <summary>
        /// Obtiene o crea una ruta entre dos aeropuertos dentro de la transaccion indicada.
        /// Si la ruta no tiene duración estimada (0) usa 120 min como fallback.
        /// </summary>
        public async Task<(int rutaId, int duracion, string? tzOrigen, string? tzDestino)>
            ObtenerOCrearRutaConZonas(
                int origenId, int destinoId,
                SqlConnection connection, SqlTransaction transaction)
        {
            // Buscar ruta existente con zonas horarias
            var queryBuscar = @"
                SELECT r.ID, r.DuracionEstimada, zho.Nombre, zhd.Nombre
                FROM   Ruta r
                INNER JOIN Aeropuerto ao  ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad  ON ad.ID = r.DestinoID
                LEFT  JOIN ZonaHoraria zho ON zho.ID = ao.ZonaHorariaID
                LEFT  JOIN ZonaHoraria zhd ON zhd.ID = ad.ZonaHorariaID
                WHERE  r.OrigenID = @o AND r.DestinoID = @d";

            using (var cmd = new SqlCommand(queryBuscar, connection, transaction))
            {
                cmd.Parameters.AddWithValue("@o", origenId);
                cmd.Parameters.AddWithValue("@d", destinoId);
                using var reader = await cmd.ExecuteReaderAsync();
                if (await reader.ReadAsync())
                {
                    int duracion = reader.GetInt32(1);
                    return (
                        reader.GetInt32(0),
                        duracion > 0 ? duracion : 120,
                        reader.IsDBNull(2) ? null : reader.GetString(2),
                        reader.IsDBNull(3) ? null : reader.GetString(3)
                    );
                }
            }

            // Crear ruta con duración default 120 min
            const string queryCrear = @"
                INSERT INTO Ruta (OrigenID, DestinoID, DuracionEstimada)
                OUTPUT INSERTED.ID
                VALUES (@o, @d, 120)";

            using var cmdCrear = new SqlCommand(queryCrear, connection, transaction);
            cmdCrear.Parameters.AddWithValue("@o", origenId);
            cmdCrear.Parameters.AddWithValue("@d", destinoId);
            int nuevaRutaId = Convert.ToInt32(await cmdCrear.ExecuteScalarAsync());

            // Obtener zonas de los aeropuertos recién asociados
            var (_, _, tzO) = (await ObtenerInfoAeropuerto(origenId, connection, transaction))
                              ?? ("", "", null);
            var (_, _, tzD) = (await ObtenerInfoAeropuerto(destinoId, connection, transaction))
                              ?? ("", "", null);

            return (nuevaRutaId, 120, tzO, tzD);
        }

        // ─────────────────────────────────────────────────────────────────
        //  TRIPULANTES DE UN VUELO
        // ─────────────────────────────────────────────────────────────────

        /// <summary>
        /// Retorna la lista de tripulantes asignados a un vuelo concreto,
        /// incluyendo nombre completo, rol e imagen en Base64.
        /// </summary>
        public async Task<List<TripulanteDTO>> ObtenerTripulantesDelVuelo(int vueloId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT mt.ID,
                       mt.Nombre,
                       mt.Apellido,
                       mt.RolID,
                       rt.Cargo AS NombreRol,
                       mt.Imagen AS ImagenBase64
                FROM   EquipoPivote ep
                INNER JOIN MiembroTripulacion mt ON mt.ID = ep.MiembroTripulacionID
                INNER JOIN RolTripulacion     rt ON rt.ID = mt.RolID
                WHERE  ep.VueloID = @VueloId
                ORDER BY rt.Cargo, mt.Nombre";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@VueloId", vueloId);
            using var reader = await cmd.ExecuteReaderAsync();

            var lista = new List<TripulanteDTO>();
            while (await reader.ReadAsync())
            {
                lista.Add(new TripulanteDTO
                {
                    Id             = reader.GetInt32(0),
                    Nombre         = reader.GetString(1),
                    Apellido       = reader.GetString(2),
                    RolID          = reader.GetInt32(3),
                    NombreRol      = reader.IsDBNull(4) ? "" : reader.GetString(4),
                    NombreCompleto = $"{reader.GetString(1)} {reader.GetString(2)}",
                    ImagenBase64   = reader.IsDBNull(5) ? null : reader.GetString(5)
                });
            }
            return lista;
        }
    }
}
