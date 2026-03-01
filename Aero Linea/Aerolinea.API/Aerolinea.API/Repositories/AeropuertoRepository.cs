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
                    a.ID, a.Nombre, a.Codigo,
                    c.Nombre AS Ciudad, p.Nombre AS Pais,
                    ia.Imagen
                FROM Aeropuerto a
                INNER JOIN Ciudad c ON a.CiudadID = c.ID
                INNER JOIN Pais p ON c.PaisID = p.ID
                LEFT JOIN ImagenAeropuerto ia ON ia.AeropuertoID = a.ID
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
                    ImagenBase64 = reader.IsDBNull(5) ? null : reader.GetString(5)
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
                    ia.Imagen
                FROM Aeropuerto a
                INNER JOIN Ciudad c ON a.CiudadID = c.ID
                INNER JOIN Pais p ON c.PaisID = p.ID
                LEFT JOIN ImagenAeropuerto ia ON ia.AeropuertoID = a.ID
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
                    ImagenBase64 = reader.IsDBNull(5) ? null : reader.GetString(5)
                };
            }

            return null;
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
                fechas.Add(reader.GetDateTime(0));

            return fechas;
        }

        public async Task<List<DateTime>> ObtenerFechasConVuelosPorRuta(
            int? origenId,
            int? destinoId,
            int cantidadPersonas = 1,
            int? claseId = null)
        {
            var fechas = new List<DateTime>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string filtroDisponibilidad = claseId == 1
                ? "AND v.BoletosTurista   >= @cantidadPersonas"
                : claseId == 2
                    ? "AND v.BoletosEjecutivo >= @cantidadPersonas"
                    : "AND (v.BoletosTurista >= @cantidadPersonas OR v.BoletosEjecutivo >= @cantidadPersonas)";

            string query = $@"
                SELECT DISTINCT v.Fecha 
                FROM Vuelo v
                INNER JOIN Ruta r ON v.RutaID = r.ID
                INNER JOIN Estado e ON v.EstadoID = e.ID
                WHERE v.Fecha >= CAST(GETDATE() AS DATE)
                  AND e.Estatus = 'A tiempo'
                  {filtroDisponibilidad}";

            if (origenId.HasValue)
                query += " AND r.OrigenID = @origenId";

            if (destinoId.HasValue)
                query += " AND r.DestinoID = @destinoId";

            query += " ORDER BY v.Fecha";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@cantidadPersonas", cantidadPersonas);

            if (origenId.HasValue)
                command.Parameters.AddWithValue("@origenId", origenId.Value);

            if (destinoId.HasValue)
                command.Parameters.AddWithValue("@destinoId", destinoId.Value);

            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
                fechas.Add(reader.GetDateTime(0));

            return fechas;
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

        public async Task<bool> Eliminar(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Primero eliminar imagen si existe
            var deleteImagen = "DELETE FROM ImagenAeropuerto WHERE AeropuertoID = @Id";
            using var cmdImagen = new SqlCommand(deleteImagen, connection);
            cmdImagen.Parameters.AddWithValue("@Id", id);
            await cmdImagen.ExecuteNonQueryAsync();

            // Luego eliminar el aeropuerto
            var query = "DELETE FROM Aeropuerto WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        // ===== IMAGEN =====

        public async Task GuardarImagen(int aeropuertoId, string imagenBase64)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // UPSERT: si ya existe la actualizamos, si no la insertamos
            var upsert = @"
                IF EXISTS (SELECT 1 FROM ImagenAeropuerto WHERE AeropuertoID = @AeropuertoID)
                    UPDATE ImagenAeropuerto SET Imagen = @Imagen WHERE AeropuertoID = @AeropuertoID
                ELSE
                    INSERT INTO ImagenAeropuerto (ID, AeropuertoID, Imagen) VALUES (@AeropuertoID, @AeropuertoID, @Imagen)";

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

            var nuevoPaisId = await commandCrear.ExecuteScalarAsync();
            return Convert.ToInt32(nuevoPaisId);
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

            var nuevaCiudadId = await commandCrear.ExecuteScalarAsync();
            return Convert.ToInt32(nuevaCiudadId);
        }
    }
}