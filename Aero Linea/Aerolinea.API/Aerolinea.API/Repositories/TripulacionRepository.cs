using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
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
        /// Si incluirInactivos es false, solo retorna los activos.
        /// </summary>
        public async Task<List<Tripulante>> ObtenerTodos(bool incluirInactivos = false)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // La columna Imagen está directamente en MiembroTripulacion
            var query = @"
                SELECT ID, Nombre, Apellido, RolID, Imagen, Activo
                FROM MiembroTripulacion
                WHERE (@IncluirInactivos = 1 OR Activo = 1)
                ORDER BY ID";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@IncluirInactivos", incluirInactivos ? 1 : 0);
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
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4),
                    Activo = reader.GetBoolean(5)
                });
            }

            return tripulantes;
        }

        /// <summary>
        /// Cambia el estado activo/inactivo de un tripulante (soft-delete).
        /// Retorna true si se modifico al menos una fila.
        /// </summary>
        public async Task<bool> CambiarEstado(int id, bool activo)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE MiembroTripulacion SET Activo = @Activo WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);
            command.Parameters.AddWithValue("@Activo", activo);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
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
        /// Verifica si el tripulante tiene vuelos activos asignados a futuro.
        /// Retorna el total de vuelos futuros activos y los numeros de vuelo
        /// que salen en menos de 48 horas (para bloquear desactivacion/eliminacion).
        /// </summary>
        public async Task<(int totalFuturos, List<string> numeros48h)> VerificarVuelosAsignados(int tripulanteId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT v.NumeroVuelo,
                       v.Fecha
                FROM   EquipoPivote ep
                INNER JOIN Vuelo v ON v.ID = ep.VueloID
                WHERE  ep.MiembroTripulacionID = @TripulanteId
                  AND  v.EstadoID  = 1
                  AND  v.Fecha    >= CAST(GETDATE() AS DATE)";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@TripulanteId", tripulanteId);
            using var reader = await command.ExecuteReaderAsync();

            var numeros48h   = new List<string>();
            int totalFuturos = 0;
            var limite48h    = DateTime.Now.AddHours(48).Date;

            while (await reader.ReadAsync())
            {
                totalFuturos++;
                var fecha = reader.GetDateTime(1).Date;
                if (fecha <= limite48h)
                    numeros48h.Add(reader.GetString(0));
            }

            return (totalFuturos, numeros48h);
        }

        /// <summary>
        /// Retorna la lista detallada de vuelos activos futuros a los que el tripulante esta asignado.
        /// Incluye datos de ruta y horas restantes calculados en SQL.
        /// Usado para el modal de confirmacion de desactivacion.
        /// </summary>
        public async Task<List<VueloActivoInfoDTO>> ObtenerVuelosAsignadosDetallados(int tripulanteId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT v.ID,
                       v.NumeroVuelo,
                       ao.Codigo AS Origen,
                       ad.Codigo AS Destino,
                       CONVERT(VARCHAR(10), v.Fecha, 120)                         AS Fecha,
                       CONVERT(VARCHAR(8),  v.HoraSalida, 108)                    AS HoraSalida,
                       CAST(DATEDIFF(MINUTE, GETDATE(),
                           DATEADD(SECOND, DATEDIFF(SECOND, 0, v.HoraSalida),
                                   CAST(v.Fecha AS DATETIME))) AS FLOAT) / 60.0   AS HorasRestantes
                FROM   EquipoPivote ep
                INNER JOIN Vuelo      v  ON v.ID  = ep.VueloID
                INNER JOIN Ruta       r  ON r.ID  = v.RutaID
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                WHERE  ep.MiembroTripulacionID = @TripulanteId
                  AND  v.EstadoID = 1
                  AND  v.Fecha   >= CAST(GETDATE() AS DATE)
                ORDER BY v.Fecha, v.HoraSalida";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@TripulanteId", tripulanteId);
            using var reader = await command.ExecuteReaderAsync();

            var result = new List<VueloActivoInfoDTO>();
            while (await reader.ReadAsync())
            {
                result.Add(new VueloActivoInfoDTO(
                    Id:             reader.GetInt32(0),
                    NumeroVuelo:    reader.GetString(1),
                    Origen:         reader.GetString(2),
                    Destino:        reader.GetString(3),
                    Fecha:          reader.GetString(4),
                    HoraSalida:     reader.GetString(5),
                    HorasRestantes: Convert.ToDouble(reader[6])
                ));
            }

            return result;
        }

        /// <summary>
        /// Elimina al tripulante de EquipoPivote para los vuelos indicados.
        /// Se usa al desactivar un tripulante con vuelos futuros (>48h) para liberar esos vuelos.
        /// Retorna el numero de filas eliminadas.
        /// </summary>
        public async Task<int> DesasignarDeFuturosVuelos(int tripulanteId, IEnumerable<int> vueloIds)
        {
            var ids = string.Join(",", vueloIds);
            if (string.IsNullOrEmpty(ids)) return 0;

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = $@"
                DELETE FROM EquipoPivote
                WHERE MiembroTripulacionID = @TripulanteId
                  AND VueloID IN ({ids})";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@TripulanteId", tripulanteId);
            return await command.ExecuteNonQueryAsync();
        }

        /// <summary>
        /// Retorna la lista de tripulantes actualmente asignados a un vuelo especifico,
        /// incluyendo su rol, para mostrar la composicion actual en el modal de reemplazo.
        /// </summary>
        public async Task<List<Tripulante>> ObtenerEquipoVuelo(int vueloId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT mt.ID, mt.Nombre, mt.Apellido, mt.RolID
                FROM   EquipoPivote ep
                INNER JOIN MiembroTripulacion mt ON mt.ID = ep.MiembroTripulacionID
                WHERE  ep.VueloID = @VueloId
                ORDER  BY mt.RolID, mt.ID";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@VueloId", vueloId);
            using var reader = await command.ExecuteReaderAsync();

            var result = new List<Tripulante>();
            while (await reader.ReadAsync())
            {
                result.Add(new Tripulante
                {
                    Id       = reader.GetInt32(0),
                    Nombre   = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID    = reader.GetInt32(3)
                });
            }
            return result;
        }

        /// <summary>
        /// Asigna los tripulantes indicados al vuelo, ignorando los que ya esten asignados.
        /// Se usa para agregar reemplazos al desactivar un tripulante con vuelos futuros.
        /// </summary>
        public async Task AsignarTripulantesAVuelo(int vueloId, IEnumerable<int> tripulanteIds)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            foreach (var tripId in tripulanteIds)
            {
                var check = "SELECT COUNT(1) FROM EquipoPivote WHERE VueloID = @V AND MiembroTripulacionID = @T";
                using var cmdCheck = new SqlCommand(check, connection);
                cmdCheck.Parameters.AddWithValue("@V", vueloId);
                cmdCheck.Parameters.AddWithValue("@T", tripId);
                var existe = Convert.ToInt32(await cmdCheck.ExecuteScalarAsync()) > 0;

                if (!existe)
                {
                    var insert = "INSERT INTO EquipoPivote (VueloID, MiembroTripulacionID) VALUES (@V, @T)";
                    using var cmdIns = new SqlCommand(insert, connection);
                    cmdIns.Parameters.AddWithValue("@V", vueloId);
                    cmdIns.Parameters.AddWithValue("@T", tripId);
                    await cmdIns.ExecuteNonQueryAsync();
                }
            }
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
