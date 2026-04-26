using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de aviones. Gestiona el CRUD completo de aviones e imagenes
    /// asociadas. Permite consultar la flota disponible para la asignacion de vuelos.
    /// </summary>
    public class AvionRepository : IAvionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public AvionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Retorna la lista completa de aviones con su imagen asociada, ordenados
        /// por marca y modelo. Si incluirInactivos es false, solo retorna los activos.
        /// </summary>
        public async Task<List<Avion>> ObtenerTodos(bool incluirInactivos = false)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // LEFT JOIN para incluir la imagen si existe
            var query = @"
                SELECT a.ID, a.Modelo, a.Marca, a.CapacidadPasajeros,
                       ia.Imagen, a.Activo
                FROM Avion a
                LEFT JOIN ImagenAvion ia ON ia.AvionID = a.ID
                WHERE (@IncluirInactivos = 1 OR a.Activo = 1)
                ORDER BY a.Marca, a.Modelo";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@IncluirInactivos", incluirInactivos ? 1 : 0);
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
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4),
                    Activo = reader.GetBoolean(5)
                });
            }

            return aviones;
        }

        /// <summary>
        /// Cambia el estado activo/inactivo de un avion (soft-delete).
        /// Retorna true si se modifico al menos una fila.
        /// </summary>
        public async Task<bool> CambiarEstado(int id, bool activo)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE Avion SET Activo = @Activo WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);
            command.Parameters.AddWithValue("@Activo", activo);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        /// <summary>
        /// Retorna un avion especifico con su imagen. Retorna null si no existe el ID dado.
        /// </summary>
        public async Task<Avion?> ObtenerPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT a.ID, a.Modelo, a.Marca, a.CapacidadPasajeros,
                       ia.Imagen, a.Activo
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
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4),
                    Activo = reader.GetBoolean(5)
                };
            }

            return null;
        }

        /// <summary>
        /// Inserta un nuevo avion en la base de datos y retorna el ID generado.
        /// </summary>
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

        /// <summary>
        /// Actualiza el modelo, marca y capacidad de un avion existente.
        /// Retorna true si se modifico al menos una fila.
        /// </summary>
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

        /// <summary>
        /// Elimina un avion y su imagen asociada de la base de datos.
        /// Retorna true si la eliminacion fue exitosa.
        /// </summary>
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

        /// <summary>
        /// Guarda o actualiza la imagen en Base64 de un avion. Si ya existe un registro
        /// de imagen lo actualiza; si no, lo inserta.
        /// </summary>
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

        /// <summary>
        /// Elimina la imagen asociada a un avion segun su ID.
        /// </summary>
        public async Task EliminarImagen(int avionId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "DELETE FROM ImagenAvion WHERE AvionID = @AvionID";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@AvionID", avionId);
            await command.ExecuteNonQueryAsync();
        }

        /// <summary>
        /// Verifica si el avion tiene vuelos activos programados a futuro.
        /// Retorna el total de vuelos futuros activos y los numeros de vuelo
        /// que salen en menos de 48 horas (para bloquear desactivacion/eliminacion).
        /// </summary>
        public async Task<(int totalFuturos, List<string> numeros48h)> VerificarVuelosActivos(int avionId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT v.NumeroVuelo,
                       v.Fecha
                FROM   Vuelo v
                WHERE  v.AvionID   = @AvionId
                  AND  v.EstadoID  = 1
                  AND  v.Fecha    >= CAST(GETDATE() AS DATE)";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@AvionId", avionId);
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
        /// Retorna la lista detallada de vuelos activos futuros asignados a un avion.
        /// Incluye datos de ruta y horas restantes calculados en SQL.
        /// Usado para mostrar al admin qué vuelos se verán afectados al desactivar el avion.
        /// </summary>
        public async Task<List<VueloActivoInfoDTO>> ObtenerVuelosActivosDetallados(int avionId)
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
                FROM   Vuelo v
                INNER JOIN Ruta        r  ON r.ID  = v.RutaID
                INNER JOIN Aeropuerto ao  ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad  ON ad.ID = r.DestinoID
                WHERE  v.AvionID  = @AvionId
                  AND  v.EstadoID = 1
                  AND  v.Fecha   >= CAST(GETDATE() AS DATE)
                ORDER BY v.Fecha, v.HoraSalida";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@AvionId", avionId);
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
        /// Retorna la imagen en Base64 de un avion. Retorna null si no tiene imagen.
        /// </summary>
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
