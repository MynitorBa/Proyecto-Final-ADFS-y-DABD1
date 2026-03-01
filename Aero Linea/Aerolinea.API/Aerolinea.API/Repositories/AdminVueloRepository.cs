using Aerolinea.API.Data;
using Aerolinea.API.Models.DTOs;
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
                // 2. Verificar o crear la ruta
                int rutaId = await ObtenerOCrearRuta(
                    dto.AeropuertoOrigenId, dto.AeropuertoDestinoId, connection, transaction);

                // 3. Validar que los boletos no superen la capacidad del avión
                int capacidadAvion = await ObtenerCapacidadAvion(dto.AvionId, connection, transaction);

                if (dto.BoletosTurista + dto.BoletosEjecutivo > capacidadAvion)
                    throw new ArgumentException(
                        $"La suma de boletos ({dto.BoletosTurista + dto.BoletosEjecutivo}) " +
                        $"supera la capacidad del avión ({capacidadAvion}).");

                // 4. Crear el vuelo — los precios y boletos disponibles van directo en la tabla
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
                    cmd.Parameters.AddWithValue("@HoraSalida", TimeSpan.Parse(dto.HoraSalida));
                    cmd.Parameters.AddWithValue("@HoraLlegada", TimeSpan.Parse(dto.HoraLlegada));
                    cmd.Parameters.AddWithValue("@FechaLlegada",
                        dto.FechaLlegada.HasValue ? (object)dto.FechaLlegada.Value.Date : DBNull.Value);
                    cmd.Parameters.AddWithValue("@EstadoId", 1);
                    cmd.Parameters.AddWithValue("@AvionId", dto.AvionId);
                    cmd.Parameters.AddWithValue("@RutaId", rutaId);
                    cmd.Parameters.AddWithValue("@BoletosTurista", dto.BoletosTurista);
                    cmd.Parameters.AddWithValue("@BoletosEjecutivo", dto.BoletosEjecutivo);
                    cmd.Parameters.AddWithValue("@PrecioTurista", dto.PrecioTurista);
                    cmd.Parameters.AddWithValue("@PrecioEjecutivo", dto.PrecioEjecutiva);

                    vueloId = (int)await cmd.ExecuteScalarAsync();
                }

                // 5. Asignar tripulación (EquipoPivote)
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
        private async Task<int> ObtenerOCrearRuta(
            int origenId, int destinoId, SqlConnection connection, SqlTransaction transaction)
        {
            var queryBuscar = @"
                SELECT ID FROM Ruta
                WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId";

            using var cmdBuscar = new SqlCommand(queryBuscar, connection, transaction);
            cmdBuscar.Parameters.AddWithValue("@OrigenId", origenId);
            cmdBuscar.Parameters.AddWithValue("@DestinoId", destinoId);
            var resultado = await cmdBuscar.ExecuteScalarAsync();

            if (resultado != null)
                return (int)resultado;

            var queryCrear = @"
                INSERT INTO Ruta (OrigenID, DestinoID, DuracionEstimada)
                OUTPUT INSERTED.ID
                VALUES (@OrigenId, @DestinoId, @DuracionEstimada)";

            using var cmdCrear = new SqlCommand(queryCrear, connection, transaction);
            cmdCrear.Parameters.AddWithValue("@OrigenId", origenId);
            cmdCrear.Parameters.AddWithValue("@DestinoId", destinoId);
            cmdCrear.Parameters.AddWithValue("@DuracionEstimada", 120);
            return (int)await cmdCrear.ExecuteScalarAsync();
        }

        private async Task<int> ObtenerCapacidadAvion(
            int avionId, SqlConnection connection, SqlTransaction transaction)
        {
            var query = "SELECT CapacidadPasajeros FROM Avion WHERE ID = @AvionId";
            using var cmd = new SqlCommand(query, connection, transaction);
            cmd.Parameters.AddWithValue("@AvionId", avionId);
            var resultado = await cmd.ExecuteScalarAsync();
            return resultado != null ? (int)resultado : 0;
        }

        // ─────────────────────────────────────────────────────────────────
        //  HISTORIAL
        // ─────────────────────────────────────────────────────────────────
        public async Task<List<VueloHistorialDTO>> ObtenerHistorialVuelos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT
                    v.ID,
                    v.NumeroVuelo,
                    aorigen.Codigo + ' - ' + corigen.Nombre  AS Origen,
                    adestino.Codigo + ' - ' + cdestino.Nombre AS Destino,
                    v.Fecha,
                    v.HoraSalida,
                    v.HoraLlegada,
                    v.FechaLlegada,
                    v.EstadoID,
                    av.CapacidadPasajeros,
                    v.BoletosTurista,
                    v.BoletosEjecutivo,
                    v.PrecioTurista,
                    v.PrecioEjecutivo
                FROM Vuelo v
                INNER JOIN Ruta r         ON v.RutaID    = r.ID
                INNER JOIN Aeropuerto aorigen  ON r.OrigenID  = aorigen.ID
                INNER JOIN Aeropuerto adestino ON r.DestinoID = adestino.ID
                INNER JOIN Ciudad corigen      ON aorigen.CiudadID  = corigen.ID
                INNER JOIN Ciudad cdestino     ON adestino.CiudadID = cdestino.ID
                INNER JOIN Avion av            ON v.AvionID   = av.ID
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
        //  Cancela: vuelo → boletos activos → reservaciones afectadas
        //  Devuelve false si el vuelo ya estaba cancelado/finalizado
        // ─────────────────────────────────────────────────────────────────
        public async Task<bool> CancelarVuelo(int vueloId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Cancelar el vuelo (solo si está Activo=1 o En curso=2)
                var queryVuelo = @"
                    UPDATE Vuelo
                    SET EstadoID        = 4,
                        BoletosTurista  = 0,
                        BoletosEjecutivo = 0
                    WHERE ID = @VueloId AND EstadoID IN (1, 2)";

                int filasVuelo;
                using (var cmd = new SqlCommand(queryVuelo, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@VueloId", vueloId);
                    filasVuelo = await cmd.ExecuteNonQueryAsync();
                }

                if (filasVuelo == 0)
                {
                    transaction.Rollback();
                    return false;
                }

                // 2. Obtener IDs de reservaciones afectadas (boletos reservados/vendidos de este vuelo)
                var reservacionIds = new List<int>();
                var queryReservaciones = @"
                    SELECT DISTINCT ReservacionID
                    FROM Boleto
                    WHERE VueloID = @VueloId
                      AND ReservacionID IS NOT NULL
                      AND EstadoBoletoID IN (2, 3)";   // 2=Reservado, 3=Pagado/Vendido

                using (var cmd = new SqlCommand(queryReservaciones, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@VueloId", vueloId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                        reservacionIds.Add(reader.GetInt32(0));
                }

                // 3. Cancelar los boletos activos de este vuelo
                var queryBoletos = @"
                    UPDATE Boleto
                    SET EstadoBoletoID = 4
                    WHERE VueloID = @VueloId
                      AND EstadoBoletoID IN (2, 3)";

                using (var cmd = new SqlCommand(queryBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@VueloId", vueloId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 4. Cancelar las reservaciones afectadas (EstadoReservaID = 3 = Cancelada)
                if (reservacionIds.Count > 0)
                {
                    var ids = string.Join(",", reservacionIds);
                    var queryCancel = $@"
                        UPDATE Reservacion
                        SET EstadoReservaID   = 3,
                            FechaCancelacion  = GETDATE(),
                            MotivoCancelacion = 'Vuelo cancelado por la aerolínea'
                        WHERE ID IN ({ids})
                          AND EstadoReservaID NOT IN (3, 4)";   // No cancelar las ya canceladas

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
    }
}