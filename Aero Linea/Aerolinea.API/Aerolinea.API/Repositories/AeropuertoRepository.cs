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
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string filtroDisponibilidad = claseId == 1
                ? "AND v.BoletosTurista   >= @cantidadPersonas"
                : claseId == 2
                    ? "AND v.BoletosEjecutivo >= @cantidadPersonas"
                    : "AND (v.BoletosTurista >= @cantidadPersonas OR v.BoletosEjecutivo >= @cantidadPersonas)";

            // ── 1. Fechas con vuelos DIRECTOS ─────────────────────────────
            string queryDirectos = $@"
                SELECT DISTINCT v.Fecha
                FROM Vuelo v
                INNER JOIN Ruta   r ON r.ID = v.RutaID
                INNER JOIN Estado e ON e.ID = v.EstadoID
                WHERE v.Fecha  >= CAST(GETDATE() AS DATE)
                  AND e.Estatus = 'A tiempo'
                  {filtroDisponibilidad}";

            if (origenId.HasValue) queryDirectos += " AND r.OrigenID  = @origenId";
            if (destinoId.HasValue) queryDirectos += " AND r.DestinoID = @destinoId";

            var fechas = new HashSet<DateTime>();

            using (var cmd = new SqlCommand(queryDirectos, connection))
            {
                cmd.Parameters.AddWithValue("@cantidadPersonas", cantidadPersonas);
                if (origenId.HasValue) cmd.Parameters.AddWithValue("@origenId", origenId.Value);
                if (destinoId.HasValue) cmd.Parameters.AddWithValue("@destinoId", destinoId.Value);

                using var reader = await cmd.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                    fechas.Add(reader.GetDateTime(0));
            }

            // ── 2. Fechas con vuelos CON ESCALA ───────────────────────────
            // Solo aplica si vienen ambos IDs
            if (origenId.HasValue && destinoId.HasValue)
            {
                // Buscamos el Fecha del tramo 1 donde exista al menos un par
                // tramo1 (origen→X) + tramo2 (X→destino) con escala válida (60-720 min)
                // y disponibilidad en ambos tramos.
                // La columna FechaLlegada del tramo 1 se usa para calcular la escala real.
                string campoDispo1 = claseId == 1 ? "t1.BoletosTurista"
                                   : claseId == 2 ? "t1.BoletosEjecutivo"
                                   : "GREATEST(t1.BoletosTurista, t1.BoletosEjecutivo)";

                string campoDispo2 = claseId == 1 ? "t2.BoletosTurista"
                                   : claseId == 2 ? "t2.BoletosEjecutivo"
                                   : "GREATEST(t2.BoletosTurista, t2.BoletosEjecutivo)";

                string queryEscalas = @"
                    SELECT DISTINCT t1.Fecha
                    FROM Vuelo t1
                    INNER JOIN Ruta   r1 ON r1.ID = t1.RutaID
                    INNER JOIN Estado e1 ON e1.ID = t1.EstadoID
                    INNER JOIN Vuelo  t2 ON t2.RutaID IN (
                                               SELECT ID FROM Ruta
                                               WHERE OrigenID  = r1.DestinoID
                                                 AND DestinoID = @destinoId)
                    INNER JOIN Estado e2 ON e2.ID = t2.EstadoID
                    WHERE t1.Fecha    >= CAST(GETDATE() AS DATE)
                      AND r1.OrigenID  = @origenId
                      AND r1.DestinoID != @destinoId
                      AND e1.Estatus   = 'A tiempo'
                      AND e2.Estatus   = 'A tiempo'
                      -- Tramo 2 sale el mismo día o el día siguiente del tramo 1
                      AND t2.Fecha BETWEEN t1.Fecha AND DATEADD(DAY, 1, t1.Fecha)
                      -- Escala entre 60 y 720 minutos usando FechaLlegada real del tramo 1
                      AND DATEDIFF(MINUTE,
                              CAST(t1.FechaLlegada AS DATETIME) + CAST(t1.HoraLlegada AS DATETIME),
                              CAST(t2.Fecha        AS DATETIME) + CAST(t2.HoraSalida  AS DATETIME)
                          ) BETWEEN 60 AND 720
                      -- Disponibilidad en tramo 1
                      AND (t1.BoletosTurista   >= @cantidadPersonas
                           OR t1.BoletosEjecutivo >= @cantidadPersonas)
                      -- Disponibilidad en tramo 2
                      AND (t2.BoletosTurista   >= @cantidadPersonas
                           OR t2.BoletosEjecutivo >= @cantidadPersonas)";

                using var cmd = new SqlCommand(queryEscalas, connection);
                cmd.Parameters.AddWithValue("@origenId", origenId.Value);
                cmd.Parameters.AddWithValue("@destinoId", destinoId.Value);
                cmd.Parameters.AddWithValue("@cantidadPersonas", cantidadPersonas);

                using var reader = await cmd.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                    fechas.Add(reader.GetDateTime(0));
            }

            return fechas.OrderBy(f => f).ToList();
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

            return Convert.ToInt32(await command.ExecuteScalarAsync());
        }

        public async Task<bool> Actualizar(Aeropuerto aeropuerto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                UPDATE Aeropuerto 
                SET Nombre   = @Nombre,
                    Codigo   = @Codigo,
                    CiudadID = @CiudadID
                WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", aeropuerto.Id);
            command.Parameters.AddWithValue("@Nombre", aeropuerto.Nombre);
            command.Parameters.AddWithValue("@Codigo", aeropuerto.Codigo);
            command.Parameters.AddWithValue("@CiudadID", aeropuerto.CiudadId);

            return await command.ExecuteNonQueryAsync() > 0;
        }

        public async Task<bool> Eliminar(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var deleteImagen = "DELETE FROM ImagenAeropuerto WHERE AeropuertoID = @Id";
            using var cmdImagen = new SqlCommand(deleteImagen, connection);
            cmdImagen.Parameters.AddWithValue("@Id", id);
            await cmdImagen.ExecuteNonQueryAsync();

            var query = "DELETE FROM Aeropuerto WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            return await command.ExecuteNonQueryAsync() > 0;
        }

        // ── IMAGEN ────────────────────────────────────────────────────────

        public async Task GuardarImagen(int aeropuertoId, string imagenBase64)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var upsert = @"
                IF EXISTS (SELECT 1 FROM ImagenAeropuerto WHERE AeropuertoID = @AeropuertoID)
                    UPDATE ImagenAeropuerto SET Imagen = @Imagen WHERE AeropuertoID = @AeropuertoID
                ELSE
                    INSERT INTO ImagenAeropuerto (ID, AeropuertoID, Imagen)
                    VALUES (@AeropuertoID, @AeropuertoID, @Imagen)";

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

        // ── CIUDADES / PAÍSES ─────────────────────────────────────────────

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

            return Convert.ToInt32(await commandCrear.ExecuteScalarAsync());
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

            return Convert.ToInt32(await commandCrear.ExecuteScalarAsync());
        }
    }
}
