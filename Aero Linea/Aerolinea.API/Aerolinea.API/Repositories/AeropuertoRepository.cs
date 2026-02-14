using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AeropuertoRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public AeropuertoRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<List<AeropuertoDTO>> ObtenerTodos()
        {
            var aeropuertos = new List<AeropuertoDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    a.ID,
                    a.Nombre,
                    a.Codigo,
                    c.Nombre AS Ciudad,
                    p.Nombre AS Pais
                FROM Aeropuerto a
                INNER JOIN Ciudad c ON a.CiudadID = c.ID
                INNER JOIN Pais p ON c.PaisID = p.ID
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
                    Pais = reader.GetString(4)
                });
            }

            return aeropuertos;
        }

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
            {
                fechas.Add(reader.GetDateTime(0));
            }

            return fechas;
        }

        public async Task<List<DateTime>> ObtenerFechasConVuelosPorRuta(int? origenId, int? destinoId)
        {
            var fechas = new List<DateTime>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT DISTINCT v.Fecha 
                FROM Vuelo v
                INNER JOIN Ruta r ON v.RutaID = r.ID
                WHERE v.Fecha >= CAST(GETDATE() AS DATE)";

            if (origenId.HasValue)
            {
                query += " AND r.OrigenID = @origenId";
            }

            if (destinoId.HasValue)
            {
                query += " AND r.DestinoID = @destinoId";
            }

            query += " ORDER BY v.Fecha";

            using var command = new SqlCommand(query, connection);

            if (origenId.HasValue)
            {
                command.Parameters.AddWithValue("@origenId", origenId.Value);
            }

            if (destinoId.HasValue)
            {
                command.Parameters.AddWithValue("@destinoId", destinoId.Value);
            }

            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                fechas.Add(reader.GetDateTime(0));
            }

            return fechas;
        }
    }
}