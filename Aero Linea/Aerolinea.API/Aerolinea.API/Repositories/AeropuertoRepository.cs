using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
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

        public async Task<AeropuertoDTO?> ObtenerPorId(int id)
        {
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
                    Pais = reader.GetString(4)
                };
            }

            return null;
        }

        public async Task<int> Crear(Aeropuerto aeropuerto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                INSERT INTO Aeropuerto (Nombre, Codigo, CiudadID)
                VALUES (@Nombre, @Codigo, @CiudadID);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Nombre", aeropuerto.Nombre);
            command.Parameters.AddWithValue("@Codigo", aeropuerto.Codigo);
            command.Parameters.AddWithValue("@CiudadID", aeropuerto.CiudadId);

            var nuevoId = await command.ExecuteScalarAsync();
            return Convert.ToInt32(nuevoId);
        }

        public async Task<bool> Actualizar(Aeropuerto aeropuerto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                UPDATE Aeropuerto 
                SET Nombre = @Nombre,
                    Codigo = @Codigo,
                    CiudadID = @CiudadID
                WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", aeropuerto.Id);
            command.Parameters.AddWithValue("@Nombre", aeropuerto.Nombre);
            command.Parameters.AddWithValue("@Codigo", aeropuerto.Codigo);
            command.Parameters.AddWithValue("@CiudadID", aeropuerto.CiudadId);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        public async Task<List<CiudadDTO>> ObtenerCiudades()
        {
            var ciudades = new List<CiudadDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    c.ID,
                    c.Nombre,
                    c.PaisID,
                    p.Nombre AS NombrePais
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

            // Buscar si el país ya existe (case-insensitive)
            var queryBuscar = "SELECT ID FROM Pais WHERE LOWER(Nombre) = LOWER(@Nombre)";
            using var commandBuscar = new SqlCommand(queryBuscar, connection);
            commandBuscar.Parameters.AddWithValue("@Nombre", nombrePais.Trim());

            var paisId = await commandBuscar.ExecuteScalarAsync();

            if (paisId != null)
            {
                return Convert.ToInt32(paisId);
            }

            // Si no existe, crear el país
            var queryCrear = @"
                INSERT INTO Pais (Nombre)
                VALUES (@Nombre);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var commandCrear = new SqlCommand(queryCrear, connection);
            commandCrear.Parameters.AddWithValue("@Nombre", nombrePais.Trim());

            var nuevoPaisId = await commandCrear.ExecuteScalarAsync();
            return Convert.ToInt32(nuevoPaisId);
        }

        public async Task<int> ObtenerOCrearCiudad(string nombreCiudad, int paisId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Buscar si la ciudad ya existe en ese país (case-insensitive)
            var queryBuscar = @"
                SELECT ID 
                FROM Ciudad 
                WHERE LOWER(Nombre) = LOWER(@Nombre) 
                AND PaisID = @PaisID";

            using var commandBuscar = new SqlCommand(queryBuscar, connection);
            commandBuscar.Parameters.AddWithValue("@Nombre", nombreCiudad.Trim());
            commandBuscar.Parameters.AddWithValue("@PaisID", paisId);

            var ciudadId = await commandBuscar.ExecuteScalarAsync();

            if (ciudadId != null)
            {
                return Convert.ToInt32(ciudadId);
            }

            // Si no existe, crear la ciudad
            var queryCrear = @"
                INSERT INTO Ciudad (Nombre, PaisID)
                VALUES (@Nombre, @PaisID);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var commandCrear = new SqlCommand(queryCrear, connection);
            commandCrear.Parameters.AddWithValue("@Nombre", nombreCiudad.Trim());
            commandCrear.Parameters.AddWithValue("@PaisID", paisId);

            var nuevaCiudadId = await commandCrear.ExecuteScalarAsync();
            return Convert.ToInt32(nuevaCiudadId);
        }
    }
}