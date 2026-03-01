using Aerolinea.API.Data;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AvionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public AvionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<List<Avion>> ObtenerTodos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // LEFT JOIN para incluir la imagen si existe
            var query = @"
                SELECT a.ID, a.Modelo, a.Marca, a.CapacidadPasajeros,
                       ia.Imagen
                FROM Avion a
                LEFT JOIN ImagenAvion ia ON ia.AvionID = a.ID
                ORDER BY a.Marca, a.Modelo";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var aviones = new List<Avion>();
            while (await reader.ReadAsync())
            {
                aviones.Add(new Avion
                {
                    Id = reader.GetInt32(0),
                    Modelo = reader.GetString(1),
                    Marca = reader.GetString(2),
                    CapacidadPasajeros = reader.GetInt32(3),
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4)
                });
            }

            return aviones;
        }

        public async Task<Avion?> ObtenerPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT a.ID, a.Modelo, a.Marca, a.CapacidadPasajeros,
                       ia.Imagen
                FROM Avion a
                LEFT JOIN ImagenAvion ia ON ia.AvionID = a.ID
                WHERE a.ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new Avion
                {
                    Id = reader.GetInt32(0),
                    Modelo = reader.GetString(1),
                    Marca = reader.GetString(2),
                    CapacidadPasajeros = reader.GetInt32(3),
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4)
                };
            }

            return null;
        }

        public async Task<int> Crear(Avion avion)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                INSERT INTO Avion (Modelo, Marca, CapacidadPasajeros)
                VALUES (@Modelo, @Marca, @CapacidadPasajeros);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Modelo", avion.Modelo);
            command.Parameters.AddWithValue("@Marca", avion.Marca);
            command.Parameters.AddWithValue("@CapacidadPasajeros", avion.CapacidadPasajeros);

            var nuevoId = await command.ExecuteScalarAsync();
            return Convert.ToInt32(nuevoId);
        }

        public async Task<bool> Actualizar(Avion avion)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                UPDATE Avion 
                SET Modelo = @Modelo,
                    Marca = @Marca,
                    CapacidadPasajeros = @CapacidadPasajeros
                WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", avion.Id);
            command.Parameters.AddWithValue("@Modelo", avion.Modelo);
            command.Parameters.AddWithValue("@Marca", avion.Marca);
            command.Parameters.AddWithValue("@CapacidadPasajeros", avion.CapacidadPasajeros);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        public async Task<bool> Eliminar(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Primero eliminar imagen si existe
            var deleteImagen = "DELETE FROM ImagenAvion WHERE AvionID = @Id";
            using var cmdImagen = new SqlCommand(deleteImagen, connection);
            cmdImagen.Parameters.AddWithValue("@Id", id);
            await cmdImagen.ExecuteNonQueryAsync();

            // Luego eliminar el avión
            var query = "DELETE FROM Avion WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        // ===== IMAGEN =====

        public async Task GuardarImagen(int avionId, string imagenBase64)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // UPSERT: si ya existe imagen para este avión la actualizamos, si no la insertamos
            var upsert = @"
                IF EXISTS (SELECT 1 FROM ImagenAvion WHERE AvionID = @AvionID)
                    UPDATE ImagenAvion SET Imagen = @Imagen WHERE AvionID = @AvionID
                ELSE
                    INSERT INTO ImagenAvion (ID, AvionID, Imagen) VALUES (@AvionID, @AvionID, @Imagen)";

            using var command = new SqlCommand(upsert, connection);
            command.Parameters.AddWithValue("@AvionID", avionId);
            command.Parameters.AddWithValue("@Imagen", imagenBase64);

            await command.ExecuteNonQueryAsync();
        }

        public async Task EliminarImagen(int avionId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "DELETE FROM ImagenAvion WHERE AvionID = @AvionID";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@AvionID", avionId);
            await command.ExecuteNonQueryAsync();
        }

        public async Task<string?> ObtenerImagen(int avionId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "SELECT Imagen FROM ImagenAvion WHERE AvionID = @AvionID";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@AvionID", avionId);

            var result = await command.ExecuteScalarAsync();
            return result?.ToString();
        }
    }
}