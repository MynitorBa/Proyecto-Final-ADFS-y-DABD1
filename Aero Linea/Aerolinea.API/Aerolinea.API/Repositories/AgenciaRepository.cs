using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de agencias. Gestiona la creacion, consulta, actualizacion
    /// y autenticacion de agencias de viaje, incluyendo la administracion de
    /// tokens, descuentos, estados y usuarios webservice asociados.
    /// </summary>
    public class AgenciaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;
        public SqlConnection CrearConexion() => _connectionFactory.CreateConnection();


        public AgenciaRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // Verifica rol del usuario
        /// <summary>
        /// Consulta el RolID del usuario especificado. Retorna 0 si el usuario no existe.
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

        // Verifica si el usuario webservice ya tiene agencia
        /// <summary>
        /// Verifica si el usuario webservice dado ya tiene una agencia asignada.
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
        /// Crea una nueva agencia en la base de datos con estado Activo.
        /// Retorna el DTO con los datos de la agencia creada incluyendo su ID generado.
        /// </summary>
        public async Task<AgenciaResponseDTO> CrearAgencia(CrearAgenciaDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            const int estadoActivo = 1;

            var query = @"
                INSERT INTO Agencia
                    (Nombre, Correo, UsuarioWebID, PorcentajeDescuento, EstadoAgenciaID, Token_HASH_Entrada, Token_HASH_Salida)
                OUTPUT INSERTED.ID
                VALUES
                    (@Nombre, @Correo, @UsuarioWebID, @PorcentajeDescuento, @EstadoAgenciaID, '', '')";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Nombre", dto.Nombre);
            command.Parameters.AddWithValue("@Correo", dto.Correo);
            command.Parameters.AddWithValue("@UsuarioWebID", dto.UsuarioWebID);
            command.Parameters.AddWithValue("@PorcentajeDescuento", dto.PorcentajeDescuento);
            command.Parameters.AddWithValue("@EstadoAgenciaID", estadoActivo);

            int nuevoId = (int)await command.ExecuteScalarAsync();

            return new AgenciaResponseDTO
            {
                ID = nuevoId,
                Nombre = dto.Nombre,
                Correo = dto.Correo,
                UsuarioWebID = dto.UsuarioWebID,
                PorcentajeDescuento = dto.PorcentajeDescuento,
                EstadoAgenciaID = estadoActivo
            };
        }

        /// <summary>
        /// Actualiza los tokens de autenticacion (entrada y salida) de una agencia.
        /// Retorna true si se actualizo al menos una fila.
        /// </summary>
        public async Task<bool> GuardarTokens(int agenciaId, string tokenEntrada, string tokenSalida)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
        UPDATE Agencia
        SET Token_HASH_Entrada = @TokenEntrada,
            Token_HASH_Salida  = @TokenSalida
        WHERE ID = @AgenciaId";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@TokenEntrada", tokenEntrada);
            command.Parameters.AddWithValue("@TokenSalida", tokenSalida);
            command.Parameters.AddWithValue("@AgenciaId", agenciaId);

            return await command.ExecuteNonQueryAsync() > 0;
        }


        /// <summary>
        /// Busca el ID de la agencia a partir de su token de entrada.
        /// Retorna null si no se encuentra ninguna agencia con ese token.
        /// </summary>
        public async Task<int?> ObtenerAgenciaIdPorTokenEntrada(string tokenEntrada)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT ID FROM Agencia WHERE Token_HASH_Entrada = @Token", connection);
            command.Parameters.AddWithValue("@Token", tokenEntrada);

            var result = await command.ExecuteScalarAsync();
            return result == null ? null : (int?)Convert.ToInt32(result);
        }

        /// <summary>
        /// Busca el ID de la agencia a partir de su URL. Retorna null si no existe.
        /// </summary>
        public async Task<int?> ObtenerAgenciaIdPorURL(string urlAgencia)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT ID FROM Agencia WHERE URL_Agencia = @URL", connection);
            command.Parameters.AddWithValue("@URL", urlAgencia);

            var result = await command.ExecuteScalarAsync();
            return result == null ? null : (int?)Convert.ToInt32(result);
        }

        /// <summary>
        /// Obtiene la identidad basica de una agencia (ID, nombre y URL) a partir de
        /// su token de autenticacion de entrada. Retorna null si no existe.
        /// </summary>
        public async Task<AgenciaIdentidad?> ObtenerAgenciaPorToken(string token)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(@"
        SELECT ID, Nombre, URL_Agencia
        FROM Agencia
        WHERE Token_HASH_Entrada = @Token", connection);
            command.Parameters.AddWithValue("@Token", token);

            using var reader = await command.ExecuteReaderAsync();
            if (await reader.ReadAsync())
            {
                return new AgenciaIdentidad
                {
                    ID = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    URLAgencia = reader.GetString(2)
                };
            }
            return null;
        }

        /// <summary>
        /// Retorna el porcentaje de descuento configurado para la agencia indicada.
        /// Retorna 0 si no se encuentra la agencia.
        /// </summary>
        public async Task<decimal> ObtenerDescuento(int agenciaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT PorcentajeDescuento FROM Agencia WHERE ID = @Id", connection);
            command.Parameters.AddWithValue("@Id", agenciaId);

            var result = await command.ExecuteScalarAsync();
            return result == null ? 0 : Convert.ToDecimal(result);
        }

        // Devuelve la agencia asociada a un usuario Webservice, o null si no tiene ninguna.
        /// <summary>
        /// Retorna los datos de la agencia del usuario webservice indicado.
        /// Retorna null si el usuario no tiene ninguna agencia asignada.
        /// </summary>
        public async Task<MiAgenciaDTO?> ObtenerAgenciaPorUsuarioId(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(@"
                SELECT ID, Nombre, Correo, PorcentajeDescuento, EstadoAgenciaID
                FROM Agencia
                WHERE UsuarioWebID = @UsuarioId", connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);

            using var reader = await command.ExecuteReaderAsync();
            if (await reader.ReadAsync())
            {
                return new MiAgenciaDTO
                {
                    ID = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Correo = reader.GetString(2),
                    PorcentajeDescuento = reader.GetDecimal(3),
                    EstadoAgenciaID = reader.GetInt32(4)
                };
            }
            return null;
        }

        // ── Admin: listado completo con datos del usuario asignado ────────────
        /// <summary>
        /// Retorna el listado completo de agencias con los datos del usuario webservice
        /// asignado. Destinado al uso exclusivo del panel de administracion.
        /// </summary>
        public async Task<List<AgenciaAdminDTO>> ObtenerTodasAdmin()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var lista = new List<AgenciaAdminDTO>();
            using var command = new SqlCommand(@"
                SELECT a.ID, a.Nombre, a.Correo, a.UsuarioWebID,
                       u.Nombre  AS UsuarioNombre,
                       u.Username,
                       a.PorcentajeDescuento, a.EstadoAgenciaID
                FROM Agencia a
                LEFT JOIN Usuario u ON a.UsuarioWebID = u.Id
                ORDER BY a.ID", connection);

            using var reader = await command.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                lista.Add(new AgenciaAdminDTO
                {
                    ID = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Correo = reader.GetString(2),
                    UsuarioWebID = reader.IsDBNull(3) ? null : reader.GetInt32(3),
                    UsuarioWebNombre = reader.IsDBNull(4) ? null : reader.GetString(4),
                    UsuarioWebUsername = reader.IsDBNull(5) ? null : reader.GetString(5),
                    PorcentajeDescuento = reader.GetDecimal(6),
                    EstadoAgenciaID = reader.GetInt32(7)
                });
            }
            return lista;
        }

        // ── Admin: usuarios Webservice que aún no tienen agencia ─────────────
        /// <summary>
        /// Retorna la lista de usuarios con rol Webservice que todavia no tienen
        /// una agencia asignada. Se usa en el panel de administracion para asignar agencias.
        /// </summary>
        public async Task<List<UsuarioWebserviceDTO>> ObtenerWebserviceSinAgencia()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var lista = new List<UsuarioWebserviceDTO>();
            using var command = new SqlCommand(@"
                SELECT u.Id, u.Nombre, u.Username, u.Correo
                FROM Usuario u
                WHERE u.RolID = 3
                  AND NOT EXISTS (
                      SELECT 1 FROM Agencia a WHERE a.UsuarioWebID = u.Id
                  )
                ORDER BY u.Nombre", connection);

            using var reader = await command.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                lista.Add(new UsuarioWebserviceDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Username = reader.IsDBNull(2) ? "" : reader.GetString(2),
                    Correo = reader.GetString(3)
                });
            }
            return lista;
        }

        // ── Admin: asignar o reasignar usuario a una agencia ─────────────────
        /// <summary>
        /// Asigna o reasigna un usuario webservice a una agencia existente.
        /// Retorna true si la actualizacion fue exitosa.
        /// </summary>
        public async Task<bool> AsignarUsuarioAAgencia(int agenciaId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE Agencia SET UsuarioWebID = @UsuarioId WHERE ID = @AgenciaId", connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);
            command.Parameters.AddWithValue("@AgenciaId", agenciaId);
            return await command.ExecuteNonQueryAsync() > 0;
        }

        // ── Admin: actualizar descuento ───────────────────────────────────────
        /// <summary>
        /// Actualiza el porcentaje de descuento de una agencia.
        /// Retorna true si la actualizacion fue exitosa.
        /// </summary>
        public async Task<bool> ActualizarDescuento(int agenciaId, decimal descuento)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE Agencia SET PorcentajeDescuento = @Descuento WHERE ID = @AgenciaId", connection);
            command.Parameters.AddWithValue("@Descuento", descuento);
            command.Parameters.AddWithValue("@AgenciaId", agenciaId);
            return await command.ExecuteNonQueryAsync() > 0;
        }

        // ── Admin: actualizar estado ──────────────────────────────────────────
        /// <summary>
        /// Actualiza el estado de una agencia (activa, suspendida, etc.).
        /// Retorna true si la actualizacion fue exitosa.
        /// </summary>
        public async Task<bool> ActualizarEstado(int agenciaId, int estadoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE Agencia SET EstadoAgenciaID = @EstadoId WHERE ID = @AgenciaId", connection);
            command.Parameters.AddWithValue("@EstadoId", estadoId);
            command.Parameters.AddWithValue("@AgenciaId", agenciaId);
            return await command.ExecuteNonQueryAsync() > 0;
        }
    }
}
