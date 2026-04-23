using Aerolinea.API.Data;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de tripulacion. Gestiona el CRUD completo de miembros de tripulacion
    /// y sus imagenes almacenadas directamente en la tabla MiembroTripulacion.
    /// Tambien permite consultar los roles de tripulacion disponibles.
    /// </summary>
    public class TripulacionRepository : ITripulacionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public TripulacionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Retorna la lista de todos los miembros de tripulacion ordenados por ID,
        /// incluyendo su imagen en Base64 si esta disponible.
        /// </summary>
        public async Task<List<Tripulante>> ObtenerTodos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // La columna Imagen está directamente en MiembroTripulacion
            var query = @"
                SELECT ID, Nombre, Apellido, RolID, Imagen
                FROM MiembroTripulacion
                ORDER BY ID";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var tripulantes = new List<Tripulante>();
            while (await reader.ReadAsync())
            {
                tripulantes.Add(new Tripulante
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID = reader.GetInt32(3),
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4)
                });
            }

            return tripulantes;
        }

        /// <summary>
        /// Retorna el miembro de tripulacion con el ID indicado.
        /// Retorna null si no existe ningun tripulante con ese ID.
        /// </summary>
        public async Task<Tripulante?> ObtenerPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT ID, Nombre, Apellido, RolID, Imagen
                FROM MiembroTripulacion
                WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new Tripulante
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID = reader.GetInt32(3),
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4)
                };
            }

            return null;
        }

        /// <summary>
        /// Retorna el nombre del cargo asociado al rol de tripulacion indicado.
        /// Retorna null si el rol no existe.
        /// </summary>
        public async Task<string?> ObtenerNombreRol(int rolId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "SELECT Cargo FROM RolTripulacion WHERE ID = @RolId";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@RolId", rolId);

            var result = await command.ExecuteScalarAsync();
            return result?.ToString();
        }

        /// <summary>
        /// Inserta un nuevo miembro de tripulacion en la base de datos incluyendo
        /// su imagen en Base64 si se proporciona. Retorna el ID generado.
        /// </summary>
        public async Task<int> Crear(Tripulante tripulante)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                INSERT INTO MiembroTripulacion (Nombre, Apellido, RolID, Imagen)
                VALUES (@Nombre, @Apellido, @RolID, @Imagen);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Nombre", tripulante.Nombre);
            command.Parameters.AddWithValue("@Apellido", tripulante.Apellido);
            command.Parameters.AddWithValue("@RolID", tripulante.RolID);
            command.Parameters.AddWithValue("@Imagen", (object?)tripulante.ImagenBase64 ?? DBNull.Value);

            var nuevoId = await command.ExecuteScalarAsync();
            return Convert.ToInt32(nuevoId);
        }

        /// <summary>
        /// Actualiza los datos de un miembro de tripulacion existente. Si se proporciona
        /// una nueva imagen la incluye en la actualizacion; de lo contrario conserva
        /// la imagen anterior sin modificarla. Retorna true si se actualizo al menos una fila.
        /// </summary>
        public async Task<bool> Actualizar(Tripulante tripulante)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Solo actualiza la imagen si se proporciona una nueva
            string query;
            if (tripulante.ImagenBase64 != null)
            {
                query = @"
                    UPDATE MiembroTripulacion
                    SET Nombre = @Nombre, Apellido = @Apellido, RolID = @RolID, Imagen = @Imagen
                    WHERE ID = @Id";
            }
            else
            {
                query = @"
                    UPDATE MiembroTripulacion
                    SET Nombre = @Nombre, Apellido = @Apellido, RolID = @RolID
                    WHERE ID = @Id";
            }

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", tripulante.Id);
            command.Parameters.AddWithValue("@Nombre", tripulante.Nombre);
            command.Parameters.AddWithValue("@Apellido", tripulante.Apellido);
            command.Parameters.AddWithValue("@RolID", tripulante.RolID);

            if (tripulante.ImagenBase64 != null)
                command.Parameters.AddWithValue("@Imagen", tripulante.ImagenBase64);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        /// <summary>
        /// Elimina el miembro de tripulacion con el ID indicado.
        /// Retorna true si se elimino al menos una fila.
        /// </summary>
        public async Task<bool> Eliminar(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "DELETE FROM MiembroTripulacion WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        /// <summary>
        /// Guarda o reemplaza la imagen en Base64 del tripulante indicado
        /// actualizando directamente la columna Imagen de MiembroTripulacion.
        /// </summary>
        public async Task GuardarImagen(int tripulanteId, string imagenBase64)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE MiembroTripulacion SET Imagen = @Imagen WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", tripulanteId);
            command.Parameters.AddWithValue("@Imagen", imagenBase64);
            await command.ExecuteNonQueryAsync();
        }

        /// <summary>
        /// Elimina la imagen del tripulante indicado estableciendo NULL en la columna Imagen.
        /// </summary>
        public async Task EliminarImagen(int tripulanteId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE MiembroTripulacion SET Imagen = NULL WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", tripulanteId);
            await command.ExecuteNonQueryAsync();
        }

        /// <summary>
        /// Retorna la lista de todos los roles de tripulacion disponibles ordenados por ID.
        /// </summary>
        public async Task<List<RolTripulacion>> ObtenerRoles()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "SELECT ID, Cargo FROM RolTripulacion ORDER BY ID";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var roles = new List<RolTripulacion>();
            while (await reader.ReadAsync())
            {
                roles.Add(new RolTripulacion
                {
                    Id = reader.GetInt32(0),
                    Cargo = reader.GetString(1)
                });
            }

            return roles;
        }
    }
}
