using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de hoteles aliados. Gestiona la consulta de hoteles activos para
    /// la busqueda dinamica, y la creacion, consulta y administracion de hoteles
    /// registrados tanto por usuarios Webservice como por el administrador del sistema.
    /// Incluye el guardado del token de sesion resultante del handshake con la aerolinea.
    /// </summary>
    public class HotelAliadoRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public HotelAliadoRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Retorna todos los hoteles aliados activos con su URL y TokenHASH
        /// para que el service pueda consultarlos dinamicamente.
        /// </summary>
        public async Task<List<HotelAliadoConexionDTO>> ObtenerHotelesActivos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT h.ID, h.Nombre, h.URL, h.TokenHASH
                FROM HotelAliado h
                JOIN EstadoAliado e ON h.EstadoID = e.ID
                WHERE LOWER(TRIM(e.Estado)) = 'activo'";

            var hoteles = new List<HotelAliadoConexionDTO>();

            using var cmd = new SqlCommand(query, connection);
            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                hoteles.Add(new HotelAliadoConexionDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Url = reader.GetString(2),
                    TokenHash = reader.GetString(3)
                });
            }

            return hoteles;
        }

        /// <summary>
        /// Retorna un hotel aliado activo por su ID.
        /// Retorna null si no existe o no esta activo.
        /// Se usa para busquedas de disponibilidad donde el estado importa.
        /// </summary>
        /// <param name="id">ID del registro HotelAliado a buscar.</param>
        public async Task<HotelAliadoConexionDTO> ObtenerHotelActivoPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT h.ID, h.Nombre, h.URL, h.TokenHASH
                FROM HotelAliado h
                JOIN EstadoAliado e ON h.EstadoID = e.ID
                WHERE h.ID = @id
                AND LOWER(TRIM(e.Estado)) = 'activo'";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@id", id);

            using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync()) return null;

            return new HotelAliadoConexionDTO
            {
                Id = reader.GetInt32(0),
                Nombre = reader.GetString(1),
                Url = reader.GetString(2),
                TokenHash = reader.GetString(3)
            };
        }

        // ── Handshake ─────────────────────────────────────────────────────────

        /// <summary>
        /// Retorna los datos basicos de un hotel aliado por su ID sin filtrar por estado.
        /// Se usa exclusivamente para el proceso de handshake: el admin debe poder
        /// hacer handshake independientemente del estado actual del hotel, siempre
        /// que el hotel tenga una URL de API configurada.
        /// Retorna null si el ID no existe en la tabla.
        /// </summary>
        /// <param name="id">ID del registro HotelAliado a buscar.</param>
        public async Task<HotelAliadoConexionDTO?> ObtenerHotelParaHandshake(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Sin JOIN a EstadoAliado: el handshake debe funcionar para cualquier
            // hotel registrado que tenga URL configurada, sin importar su estado actual
            using var cmd = new SqlCommand(
                "SELECT ID, Nombre, URL, TokenHASH FROM HotelAliado WHERE ID = @id",
                connection);
            cmd.Parameters.AddWithValue("@id", id);

            using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync()) return null;

            return new HotelAliadoConexionDTO
            {
                Id = reader.GetInt32(0),
                Nombre = reader.GetString(1),
                Url = reader.IsDBNull(2) ? string.Empty : reader.GetString(2),
                TokenHash = reader.IsDBNull(3) ? string.Empty : reader.GetString(3)
            };
        }

        /// <summary>
        /// Actualiza el TokenHASH de un hotel aliado con el token de sesion
        /// recibido tras completar el handshake. Este token se usa para autenticar
        /// las llamadas posteriores de la aerolinea hacia el hotel.
        /// Retorna true si la actualizacion fue exitosa.
        /// </summary>
        public async Task<bool> GuardarTokenHash(int hotelId, string tokenHash)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE HotelAliado SET TokenHASH = @TokenHash WHERE ID = @HotelId",
                connection);
            command.Parameters.AddWithValue("@TokenHash", tokenHash);
            command.Parameters.AddWithValue("@HotelId", hotelId);
            return await command.ExecuteNonQueryAsync() > 0;
        }

        // ── CRUD general ──────────────────────────────────────────────────────

        /// <summary>
        /// Verifica si el usuario webservice dado ya tiene un hotel aliado registrado.
        /// Retorna true si existe al menos un hotel para ese usuario.
        /// </summary>
        public async Task<bool> UsuarioYaTieneHotelAliado(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT COUNT(*) FROM HotelAliado WHERE UsuarioWEBIs = @Id", connection);
            command.Parameters.AddWithValue("@Id", usuarioId);

            return (int)await command.ExecuteScalarAsync() > 0;
        }

        /// <summary>
        /// Verifica si el usuario webservice dado ya tiene una agencia de viaje registrada.
        /// Se usa para impedir que un usuario tenga tanto un hotel como una agencia.
        /// Retorna true si existe al menos una agencia para ese usuario.
        /// </summary>
        public async Task<bool> UsuarioYaTieneAgencia(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT COUNT(*) FROM Agencia WHERE UsuarioWebID = @Id", connection);
            command.Parameters.AddWithValue("@Id", usuarioId);

            return (int)await command.ExecuteScalarAsync() > 0;
        }

        /// <summary>
        /// Consulta el RolID del usuario especificado. Retorna 0 si el usuario no existe.
        /// Se usa para validar que el usuario a asignar tenga rol Webservice.
        /// </summary>
        public async Task<int> ObtenerRolUsuario(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT RolID FROM Usuario WHERE Id = @Id", connection);
            command.Parameters.AddWithValue("@Id", usuarioId);

            var result = await command.ExecuteScalarAsync();
            return result == null ? 0 : (int)result;
        }

        /// <summary>
        /// Inserta un nuevo hotel aliado vinculado al usuario Webservice indicado.
        /// El EstadoID se establece en 1 (activo) por defecto. El TokenHASH se deja
        /// vacio ya que se genera automaticamente al establecer la conexion con la aerolinea.
        /// Retorna el DTO con los datos del hotel recien creado.
        /// </summary>
        public async Task<MiHotelDTO> CrearHotelWebservice(int usuarioId, CrearHotelWebserviceDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            const int estadoActivo = 1;

            var query = @"
        INSERT INTO HotelAliado (Nombre, UsuarioWEBIs, EstadoID, TokenHASH, URL, URLParaUsuario, URLHomeAliado)
        OUTPUT INSERTED.ID
        VALUES (@Nombre, @UsuarioWEBIs, @EstadoID, '', @URL, @URLParaUsuario, @URLHomeAliado)";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Nombre", dto.Nombre);
            command.Parameters.AddWithValue("@UsuarioWEBIs", usuarioId);
            command.Parameters.AddWithValue("@EstadoID", estadoActivo);
            command.Parameters.AddWithValue("@URL", dto.Url);
            command.Parameters.AddWithValue("@URLParaUsuario", dto.UrlParaUsuario);
            command.Parameters.AddWithValue("@URLHomeAliado", (object?)dto.UrlHomeAliado ?? DBNull.Value);

            int nuevoId = (int)await command.ExecuteScalarAsync();

            return new MiHotelDTO
            {
                ID = nuevoId,
                Nombre = dto.Nombre,
                EstadoID = estadoActivo,
                Url = dto.Url,
                UrlParaUsuario = dto.UrlParaUsuario,
                UrlHomeAliado = dto.UrlHomeAliado
            };
        }

        /// <summary>
        /// Retorna los datos del hotel aliado del usuario webservice indicado.
        /// No incluye el TokenHASH ya que es informacion interna del sistema.
        /// Retorna null si el usuario no tiene ningun hotel registrado.
        /// </summary>
        public async Task<MiHotelDTO?> ObtenerHotelPorUsuarioId(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(@"
        SELECT ID, Nombre, EstadoID, URL, URLParaUsuario, URLHomeAliado
        FROM HotelAliado
        WHERE UsuarioWEBIs = @UsuarioId", connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);

            using var reader = await command.ExecuteReaderAsync();
            if (await reader.ReadAsync())
            {
                return new MiHotelDTO
                {
                    ID = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    EstadoID = reader.GetInt32(2),
                    Url = reader.IsDBNull(3) ? string.Empty : reader.GetString(3),
                    UrlParaUsuario = reader.IsDBNull(4) ? string.Empty : reader.GetString(4),
                    UrlHomeAliado = reader.IsDBNull(5) ? null : reader.GetString(5)
                };
            }
            return null;
        }

        /// <summary>
        /// Retorna el listado completo de hoteles aliados con los datos del usuario
        /// Webservice asignado. No incluye TokenHASH. Destinado al panel de administracion.
        /// </summary>
        public async Task<List<HotelAdminDTO>> ObtenerTodosAdmin()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var lista = new List<HotelAdminDTO>();

            using var command = new SqlCommand(@"
    SELECT h.ID, h.Nombre, h.EstadoID, h.URL, h.URLParaUsuario, h.URLHomeAliado,
           h.UsuarioWEBIs,
           u.Nombre   AS UsuarioNombre,
           u.Username AS UsuarioUsername
    FROM HotelAliado h
    LEFT JOIN Usuario u ON h.UsuarioWEBIs = u.Id
    ORDER BY h.ID", connection);

            using var reader = await command.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                lista.Add(new HotelAdminDTO
                {
                    ID = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    EstadoID = reader.GetInt32(2),
                    Url = reader.IsDBNull(3) ? string.Empty : reader.GetString(3),
                    UrlParaUsuario = reader.IsDBNull(4) ? string.Empty : reader.GetString(4),
                    UrlHomeAliado = reader.IsDBNull(5) ? null : reader.GetString(5),   
                    UsuarioWEBIs = reader.IsDBNull(6) ? null : reader.GetInt32(6),
                    UsuarioNombre = reader.IsDBNull(7) ? null : reader.GetString(7),
                    UsuarioUsername = reader.IsDBNull(8) ? null : reader.GetString(8)
                });
            }
            return lista;
        }

        /// <summary>
        /// Crea un nuevo hotel aliado desde el panel de administracion, asignandolo
        /// directamente al usuario Webservice indicado. El TokenHASH se deja vacio.
        /// Retorna el DTO completo del hotel creado con el ID generado.
        /// </summary>
        public async Task<HotelAdminDTO> CrearHotelAdmin(CrearHotelAdminDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            const int estadoActivo = 1;

            var query = @"
        INSERT INTO HotelAliado (Nombre, UsuarioWEBIs, EstadoID, TokenHASH, URL, URLParaUsuario, URLHomeAliado)
        OUTPUT INSERTED.ID
        VALUES (@Nombre, @UsuarioWEBIs, @EstadoID, '', @URL, @URLParaUsuario, @URLHomeAliado)";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Nombre", dto.Nombre);
            command.Parameters.AddWithValue("@UsuarioWEBIs", dto.UsuarioWEBIs);
            command.Parameters.AddWithValue("@EstadoID", estadoActivo);
            command.Parameters.AddWithValue("@URL", dto.Url);
            command.Parameters.AddWithValue("@URLParaUsuario", dto.UrlParaUsuario);
            command.Parameters.AddWithValue("@URLHomeAliado", (object?)dto.UrlHomeAliado ?? DBNull.Value);

            int nuevoId = (int)await command.ExecuteScalarAsync();

            return new HotelAdminDTO
            {
                ID = nuevoId,
                Nombre = dto.Nombre,
                EstadoID = estadoActivo,
                Url = dto.Url,
                UrlParaUsuario = dto.UrlParaUsuario,
                UrlHomeAliado = dto.UrlHomeAliado,
                UsuarioWEBIs = dto.UsuarioWEBIs
            };
        }

        /// <summary>
        /// Actualiza el EstadoID de un hotel aliado.
        /// Retorna true si la actualizacion fue exitosa.
        /// </summary>
        public async Task<bool> ActualizarEstado(int hotelId, int estadoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE HotelAliado SET EstadoID = @EstadoId WHERE ID = @HotelId", connection);
            command.Parameters.AddWithValue("@EstadoId", estadoId);
            command.Parameters.AddWithValue("@HotelId", hotelId);
            return await command.ExecuteNonQueryAsync() > 0;
        }

        /// <summary>
        /// Asigna o reasigna un usuario Webservice a un hotel aliado existente.
        /// Retorna true si la actualizacion fue exitosa.
        /// </summary>
        public async Task<bool> AsignarUsuario(int hotelId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE HotelAliado SET UsuarioWEBIs = @UsuarioId WHERE ID = @HotelId", connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);
            command.Parameters.AddWithValue("@HotelId", hotelId);
            return await command.ExecuteNonQueryAsync() > 0;
        }

        /// <summary>
        /// Actualiza la URL de la API y la URL publica para usuarios de un hotel aliado.
        /// Retorna true si la actualizacion fue exitosa.
        /// </summary>
        public async Task<bool> ActualizarUrls(int hotelId, string url, string urlParaUsuario, string? urlHomeAliado)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(@"
        UPDATE HotelAliado
        SET URL = @Url, URLParaUsuario = @UrlParaUsuario, URLHomeAliado = @UrlHomeAliado
        WHERE ID = @HotelId", connection);
            command.Parameters.AddWithValue("@Url", url);
            command.Parameters.AddWithValue("@UrlParaUsuario", urlParaUsuario);
            command.Parameters.AddWithValue("@UrlHomeAliado", (object?)urlHomeAliado ?? DBNull.Value);
            command.Parameters.AddWithValue("@HotelId", hotelId);
            return await command.ExecuteNonQueryAsync() > 0;
        }


        /// <summary>
        /// Retorna el nombre y la URL Home de todos los hoteles aliados activos.
        /// Destinado a la recomendación de hoteles para el usuario final.
        /// Solo incluye hoteles que tienen URLHomeAliado configurada.
        /// </summary>
        public async Task<List<HotelHomeDTO>> ObtenerHotelesHome()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var lista = new List<HotelHomeDTO>();

            using var command = new SqlCommand(@"
        SELECT h.ID, h.Nombre, h.URLHomeAliado
        FROM HotelAliado h
        JOIN EstadoAliado e ON h.EstadoID = e.ID
        WHERE LOWER(TRIM(e.Estado)) = 'activo'
          AND h.URLHomeAliado IS NOT NULL", connection);

            using var reader = await command.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                lista.Add(new HotelHomeDTO
                {
                    ID = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    UrlHomeAliado = reader.GetString(2)
                });
            }
            return lista;
        }
    }
}