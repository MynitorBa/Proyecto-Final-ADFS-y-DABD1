using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AgenciaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

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
    }
}