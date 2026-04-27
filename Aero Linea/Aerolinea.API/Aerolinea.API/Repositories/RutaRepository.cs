using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de rutas. Permite listar, crear, buscar y actualizar rutas entre
    /// aeropuertos. Incluye introspeccion de esquema para soportar instalaciones con
    /// o sin la tabla ZonaHoraria, adaptando las consultas segun la estructura disponible.
    /// </summary>
    public class RutaRepository : IRutaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        /// <summary>
        /// Cache estático del resultado de la verificación de esquema ZonaHoraria.
        /// Se evalúa una sola vez por vida del proceso para evitar consultas repetidas
        /// a INFORMATION_SCHEMA en cada petición.
        /// </summary>
        private static bool? _tieneZonaHoraria;

        public RutaRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Cache del resultado de la verificación de columna Activo en Ruta.
        /// </summary>
        private static bool? _tieneActivoRuta;

        /// <summary>
        /// Retorna (y cachea) si la columna Activo existe en la tabla Ruta.
        /// </summary>
        private static async Task<bool> TieneActivoRutaAsync(SqlConnection connection)
        {
            if (_tieneActivoRuta.HasValue)
                return _tieneActivoRuta.Value;

            _tieneActivoRuta = await ColumnaExiste(connection, "Ruta", "Activo");
            return _tieneActivoRuta.Value;
        }

        /// <summary>
        /// Retorna (y cachea) si la tabla ZonaHoraria y la columna ZonaHorariaID existen.
        /// Solo consulta INFORMATION_SCHEMA la primera vez; las siguientes devuelven el valor cacheado.
        /// </summary>
        private static async Task<bool> TieneZonaHorariaAsync(SqlConnection connection)
        {
            if (_tieneZonaHoraria.HasValue)
                return _tieneZonaHoraria.Value;

            _tieneZonaHoraria = await TablaExiste(connection, "ZonaHoraria") &&
                                await ColumnaExiste(connection, "Aeropuerto", "ZonaHorariaID");
            return _tieneZonaHoraria.Value;
        }

        /// <summary>
        /// Retorna todas las rutas con informacion de aeropuertos y zonas horarias.
        /// Si la tabla ZonaHoraria o la columna ZonaHorariaID no existen en el esquema
        /// actual, omite los JOINs correspondientes y retorna null en esos campos.
        /// Si la columna Activo no existe aun, retorna true para todas las rutas.
        /// </summary>
        public async Task<List<RutaDTO>> ObtenerTodas()
        {
            var rutas = new List<RutaDTO>();
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            bool tieneZonaHoraria = await TieneZonaHorariaAsync(connection);
            bool tieneActivo      = await TieneActivoRutaAsync(connection);

            string activoCol = tieneActivo ? "r.Activo" : "CAST(1 AS BIT) AS Activo";

            string query = tieneZonaHoraria
                ? $@"SELECT
                        r.ID,
                        ao.Codigo AS CodigoOrigen,  ao.Nombre AS NombreOrigen,
                        zho.Nombre AS TzOrigen,
                        ad.Codigo AS CodigoDestino, ad.Nombre AS NombreDestino,
                        zhd.Nombre AS TzDestino,
                        r.DuracionEstimada,
                        (SELECT COUNT(*) FROM Vuelo v WHERE v.RutaID = r.ID) AS TotalVuelos,
                        {activoCol}
                    FROM   Ruta r
                    INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                    INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                    LEFT  JOIN ZonaHoraria zho ON zho.ID = ao.ZonaHorariaID
                    LEFT  JOIN ZonaHoraria zhd ON zhd.ID = ad.ZonaHorariaID
                    ORDER BY ao.Codigo, ad.Codigo"
                : $@"SELECT
                        r.ID,
                        ao.Codigo AS CodigoOrigen,  ao.Nombre AS NombreOrigen,
                        NULL AS TzOrigen,
                        ad.Codigo AS CodigoDestino, ad.Nombre AS NombreDestino,
                        NULL AS TzDestino,
                        r.DuracionEstimada,
                        (SELECT COUNT(*) FROM Vuelo v WHERE v.RutaID = r.ID) AS TotalVuelos,
                        {activoCol}
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
                    TotalVuelos = reader.GetInt32(8),
                    Activo = reader.GetBoolean(9)
                });
            }

            return rutas;
        }

        /// <summary>
        /// Actualiza la duracion estimada en minutos de una ruta existente.
        /// Retorna true si la actualizacion afecto al menos una fila.
        /// </summary>
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

        /// <summary>
        /// Obtiene la duracion estimada y las zonas horarias de origen y destino para
        /// calcular la hora de llegada de un vuelo dado el par de aeropuertos.
        /// Si la tabla ZonaHoraria no existe en el esquema, retorna null en las zonas horarias.
        /// Retorna (120, null, null) como fallback si la ruta no existe.
        /// </summary>
        public async Task<(int duracion, string? tzOrigen, string? tzDestino)> ObtenerInfoRuta(
            int origenId, int destinoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Verificar si la tabla ZonaHoraria y la columna ZonaHorariaID existen
            // antes de intentar el JOIN — evita errores si la migración no se ha ejecutado
            bool tieneZonaHoraria = await TieneZonaHorariaAsync(connection);

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

        /// <summary>
        /// Verifica si existe una ruta directa entre los aeropuertos de origen y destino indicados.
        /// Retorna true si existe al menos una ruta con ese par exacto origen-destino.
        /// </summary>
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

        /// <summary>
        /// Crea una nueva ruta entre los aeropuertos indicados con la duracion estimada dada.
        /// Si ya existe una ruta con el mismo par origen-destino, retorna su ID sin duplicar.
        /// </summary>
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

        /// <summary>
        /// Verifica si alguna reservacion activa (Pendiente o Confirmada) tiene boletos
        /// en vuelos que pertenecen a la ruta indicada. Una ruta con reservaciones activas
        /// no puede ser desactivada.
        /// </summary>
        public async Task<bool> TieneReservacionesActivas(int rutaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // EstadoReservaID: Pendiente=1, Confirmada=2
            var query = @"
                SELECT COUNT(1)
                FROM   Boleto b
                INNER JOIN Reservacion r ON r.ID = b.ReservacionID
                INNER JOIN Vuelo       v ON v.ID = b.VueloID
                WHERE  v.RutaID = @rutaId
                  AND  r.EstadoReservaID IN (1, 2)";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0;
        }

        /// <summary>
        /// Desactiva una ruta (Activo = 0). Requiere que la columna Activo exista en la tabla Ruta
        /// (ALTER TABLE Ruta ADD Activo BIT NOT NULL DEFAULT 1).
        /// Retorna false si la ruta no existe.
        /// </summary>
        public async Task<bool> DesactivarRuta(int rutaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE Ruta SET Activo = 0 WHERE ID = @rutaId";
            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);
            return await cmd.ExecuteNonQueryAsync() > 0;
        }

        /// <summary>
        /// Reactiva una ruta previamente desactivada (Activo = 1).
        /// Retorna false si la ruta no existe.
        /// </summary>
        public async Task<bool> ActivarRuta(int rutaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE Ruta SET Activo = 1 WHERE ID = @rutaId";
            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);
            return await cmd.ExecuteNonQueryAsync() > 0;
        }

        /// <summary>
        /// Retorna los codigos IATA y nombres de ciudad de origen y destino de la ruta indicada.
        /// </summary>
        public async Task<(string origenCodigo, string origenCiudad, string destinoCodigo, string destinoCiudad)> ObtenerDescripcionRuta(int rutaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT ao.Codigo, co.Nombre, ad.Codigo, cd.Nombre
                FROM Ruta r
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Ciudad     co ON co.ID = ao.CiudadID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                INNER JOIN Ciudad     cd ON cd.ID = ad.CiudadID
                WHERE r.ID = @rutaId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);
            using var reader = await cmd.ExecuteReaderAsync();
            if (await reader.ReadAsync())
            {
                return (
                    reader.IsDBNull(0) ? "" : reader.GetString(0),
                    reader.IsDBNull(1) ? "" : reader.GetString(1),
                    reader.IsDBNull(2) ? "" : reader.GetString(2),
                    reader.IsDBNull(3) ? "" : reader.GetString(3)
                );
            }
            return ("", "", "", "");
        }

        /// <summary>
        /// Retorna los correos y nombres de contacto de todos los usuarios WebService
        /// de las agencias registradas en el sistema.
        /// </summary>
        public async Task<List<(string email, string nombreContacto, string nombreAgencia)>> ObtenerEmailsAgencias()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT DISTINCT u.Correo, u.Nombre + ' ' + u.Apellido AS NombreContacto, ag.Nombre AS NombreAgencia
                FROM Agencia ag
                INNER JOIN Usuario u ON u.ID = ag.UsuarioWebID
                WHERE u.Correo IS NOT NULL AND u.Correo <> ''";

            using var cmd = new SqlCommand(query, connection);
            using var reader = await cmd.ExecuteReaderAsync();
            var lista = new List<(string, string, string)>();
            while (await reader.ReadAsync())
            {
                lista.Add((
                    reader.IsDBNull(0) ? "" : reader.GetString(0),
                    reader.IsDBNull(1) ? "" : reader.GetString(1),
                    reader.IsDBNull(2) ? "" : reader.GetString(2)
                ));
            }
            return lista;
        }

        /// <summary>
        /// Retorna los vuelos futuros (hoy en adelante) de una ruta dada, ordenados por fecha y hora.
        /// </summary>
        public async Task<List<(string NumeroVuelo, string FechaSalida, string HoraSalida)>> ObtenerVuelosFuturosPorRuta(int rutaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT v.NumeroVuelo, CONVERT(VARCHAR(10), v.Fecha, 23) AS FechaSalida,
                       LEFT(CAST(v.HoraSalida AS VARCHAR(8)), 5) AS HoraSalida
                FROM Vuelo v
                WHERE v.RutaID = @rutaId
                  AND v.Fecha >= CAST(GETDATE() AS DATE)
                ORDER BY v.Fecha, v.HoraSalida";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);
            using var reader = await cmd.ExecuteReaderAsync();

            var lista = new List<(string, string, string)>();
            while (await reader.ReadAsync())
            {
                lista.Add((
                    reader.IsDBNull(0) ? "" : reader.GetString(0),
                    reader.IsDBNull(1) ? "" : reader.GetString(1),
                    reader.IsDBNull(2) ? "" : reader.GetString(2)
                ));
            }
            return lista;
        }

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
