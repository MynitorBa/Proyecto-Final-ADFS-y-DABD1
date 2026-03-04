using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class RutaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public RutaRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // ── Listar todas las rutas con info de timezone ───────────────────
        public async Task<List<RutaDTO>> ObtenerTodas()
        {
            var rutas = new List<RutaDTO>();
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            bool tieneZonaHoraria = await TablaExiste(connection, "ZonaHoraria") &&
                                    await ColumnaExiste(connection, "Aeropuerto", "ZonaHorariaID");

            string query = tieneZonaHoraria
                ? @"SELECT
                        r.ID,
                        ao.Codigo AS CodigoOrigen,  ao.Nombre AS NombreOrigen,
                        zho.Nombre AS TzOrigen,
                        ad.Codigo AS CodigoDestino, ad.Nombre AS NombreDestino,
                        zhd.Nombre AS TzDestino,
                        r.DuracionEstimada,
                        (SELECT COUNT(*) FROM Vuelo v WHERE v.RutaID = r.ID) AS TotalVuelos
                    FROM   Ruta r
                    INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                    INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                    LEFT  JOIN ZonaHoraria zho ON zho.ID = ao.ZonaHorariaID
                    LEFT  JOIN ZonaHoraria zhd ON zhd.ID = ad.ZonaHorariaID
                    ORDER BY ao.Codigo, ad.Codigo"
                : @"SELECT
                        r.ID,
                        ao.Codigo AS CodigoOrigen,  ao.Nombre AS NombreOrigen,
                        NULL AS TzOrigen,
                        ad.Codigo AS CodigoDestino, ad.Nombre AS NombreDestino,
                        NULL AS TzDestino,
                        r.DuracionEstimada,
                        (SELECT COUNT(*) FROM Vuelo v WHERE v.RutaID = r.ID) AS TotalVuelos
                    FROM   Ruta r
                    INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                    INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                    ORDER BY ao.Codigo, ad.Codigo";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                rutas.Add(new RutaDTO
                {
                    Id = reader.GetInt32(0),
                    CodigoOrigen = reader.GetString(1),
                    Origen = reader.GetString(2),
                    ZonaHorariaOrigen = reader.IsDBNull(3) ? null : reader.GetString(3),
                    CodigoDestino = reader.GetString(4),
                    Destino = reader.GetString(5),
                    ZonaHorariaDestino = reader.IsDBNull(6) ? null : reader.GetString(6),
                    DuracionEstimada = reader.GetInt32(7),
                    TotalVuelos = reader.GetInt32(8)
                });
            }

            return rutas;
        }

        // ── Actualizar solo la duración estimada de una ruta ──────────────
        public async Task<bool> ActualizarDuracion(int rutaId, int minutos)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE Ruta SET DuracionEstimada = @Minutos WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Minutos", minutos);
            command.Parameters.AddWithValue("@Id", rutaId);

            return await command.ExecuteNonQueryAsync() > 0;
        }

        // ── Obtener duración + timezones para calcular llegada ────────────
        // Recibe origenId + destinoId; busca la ruta por esos aeropuertos.
        // Devuelve (120, null, null) si la ruta aún no existe.
        public async Task<(int duracion, string? tzOrigen, string? tzDestino)> ObtenerInfoRuta(
            int origenId, int destinoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Verificar si la tabla ZonaHoraria y la columna ZonaHorariaID existen
            // antes de intentar el JOIN — evita errores si la migración no se ha ejecutado
            bool tieneZonaHoraria = await TablaExiste(connection, "ZonaHoraria") &&
                                    await ColumnaExiste(connection, "Aeropuerto", "ZonaHorariaID");

            string query = tieneZonaHoraria
                ? @"SELECT r.DuracionEstimada,
                           zho.Nombre AS TzOrigen,
                           zhd.Nombre AS TzDestino
                    FROM   Ruta r
                    INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                    INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                    LEFT  JOIN ZonaHoraria zho ON zho.ID = ao.ZonaHorariaID
                    LEFT  JOIN ZonaHoraria zhd ON zhd.ID = ad.ZonaHorariaID
                    WHERE  r.OrigenID = @OrigenId AND r.DestinoID = @DestinoId"
                : @"SELECT r.DuracionEstimada, NULL, NULL
                    FROM   Ruta r
                    WHERE  r.OrigenID = @OrigenId AND r.DestinoID = @DestinoId";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@OrigenId", origenId);
            command.Parameters.AddWithValue("@DestinoId", destinoId);
            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return (
                    duracion: reader.GetInt32(0),
                    tzOrigen: reader.IsDBNull(1) ? null : reader.GetString(1),
                    tzDestino: reader.IsDBNull(2) ? null : reader.GetString(2)
                );
            }

            return (120, null, null);
        }
        // ── Verificar si existe una ruta ──────────────────────────────────
        public async Task<bool> ExisteRuta(int origenId, int destinoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT COUNT(1) FROM Ruta
                WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@OrigenId", origenId);
            cmd.Parameters.AddWithValue("@DestinoId", destinoId);

            return Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0;
        }

        // ── Crear ruta manualmente ─────────────────────────────────────────
        public async Task<int> CrearRuta(int origenId, int destinoId, int duracionEstimada = 120)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Si ya existe devuelve su ID sin duplicar
            var queryBuscar = @"SELECT ID FROM Ruta WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId";
            using var cmdBuscar = new SqlCommand(queryBuscar, connection);
            cmdBuscar.Parameters.AddWithValue("@OrigenId", origenId);
            cmdBuscar.Parameters.AddWithValue("@DestinoId", destinoId);
            var existing = await cmdBuscar.ExecuteScalarAsync();
            if (existing != null) return Convert.ToInt32(existing);

            var queryCrear = @"
                INSERT INTO Ruta (OrigenID, DestinoID, DuracionEstimada)
                OUTPUT INSERTED.ID
                VALUES (@OrigenId, @DestinoId, @Duracion)";

            using var cmdCrear = new SqlCommand(queryCrear, connection);
            cmdCrear.Parameters.AddWithValue("@OrigenId", origenId);
            cmdCrear.Parameters.AddWithValue("@DestinoId", destinoId);
            cmdCrear.Parameters.AddWithValue("@Duracion", duracionEstimada);

            return Convert.ToInt32(await cmdCrear.ExecuteScalarAsync());
        }

        // ── Helpers de introspección ──────────────────────────────────────
        private static async Task<bool> TablaExiste(SqlConnection connection, string tabla)
        {
            var q = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=@T";
            using var cmd = new SqlCommand(q, connection);
            cmd.Parameters.AddWithValue("@T", tabla);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0;
        }

        private static async Task<bool> ColumnaExiste(SqlConnection connection, string tabla, string columna)
        {
            var q = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME=@T AND COLUMN_NAME=@C";
            using var cmd = new SqlCommand(q, connection);
            cmd.Parameters.AddWithValue("@T", tabla);
            cmd.Parameters.AddWithValue("@C", columna);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0;
        }

    }
}
// ── Verificar si existe una ruta ──────────────────────────
// Devuelve true si existe al menos una ruta entre los aeropuertos dados
// (en cualquier dirección no — solo origen→destino exacto).
