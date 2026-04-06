using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AgenciaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;
        public SqlConnection CrearConexion() => _connectionFactory.CreateConnection();


        public AgenciaRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // Verifica rol del usuario
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
        public async Task<bool> UsuarioYaTieneAgencia(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT COUNT(*) FROM Agencia WHERE UsuarioWebID = @Id", connection);
            command.Parameters.AddWithValue("@Id", usuarioId);

            return (int)await command.ExecuteScalarAsync() > 0;
        }

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