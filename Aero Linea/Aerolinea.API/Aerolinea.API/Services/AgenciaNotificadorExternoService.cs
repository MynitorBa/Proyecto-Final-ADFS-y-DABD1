using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Services
{
    public class AgenciaNotificadorExternoService
    {
        private readonly DbConnectionFactory _db;
        private readonly IHttpClientFactory _http;
        private readonly ILogger<AgenciaNotificadorExternoService> _logger;

        public AgenciaNotificadorExternoService(
            DbConnectionFactory db,
            IHttpClientFactory http,
            ILogger<AgenciaNotificadorExternoService> logger)
        {
            _db = db;
            _http = http;
            _logger = logger;
        }

        // Resultado que devuelve la llamada a la agencia
        public class ResultadoNotificacion
        {
            public bool EsReservaDeAgencia { get; set; }
            public string NombreAgencia { get; set; } = "";
            public bool Enviado { get; set; }
            public int HttpStatus { get; set; }
            public string RespuestaAgencia { get; set; } = "";
            public string? Error { get; set; }
        }

        public async Task<ResultadoNotificacion> NotificarCancelacionAsync(int reservacionId, string motivo)
        {
            var resultado = new ResultadoNotificacion();

            try
            {
                const string sql = @"
                    SELECT  a.ID,
                            a.Nombre,
                            ISNULL(a.Token_HASH_Salida, '') AS Token_HASH_Salida,
                            ISNULL(a.URL_Agencia,      '') AS URL_Agencia
                    FROM    Agencia     a
                    INNER JOIN Reservacion r ON r.UsuarioID = a.UsuarioWebID
                    WHERE   r.ID = @ResId";

                int agenciaId;
                string nombre, token, url;

                using (var conn = _db.CreateConnection())
                {
                    await conn.OpenAsync();
                    using var cmd = new SqlCommand(sql, conn);
                    cmd.Parameters.AddWithValue("@ResId", reservacionId);
                    using var dr = await cmd.ExecuteReaderAsync();

                    if (!await dr.ReadAsync())
                        return resultado; // No es de agencia

                    agenciaId = Convert.ToInt32(dr["ID"]);
                    nombre = dr["Nombre"].ToString()!;
                    token = dr["Token_HASH_Salida"].ToString()!;
                    url = dr["URL_Agencia"].ToString()!;
                    resultado.EsReservaDeAgencia = true;
                    resultado.NombreAgencia = nombre;
                }

                if (string.IsNullOrWhiteSpace(url))
                {
                    resultado.Error = $"Agencia '{nombre}' no tiene URL_Agencia configurada.";
                    _logger.LogWarning("[AgenciaNotificador] {Error}", resultado.Error);
                    return resultado;
                }

                if (string.IsNullOrWhiteSpace(token))
                {
                    resultado.Error = $"Agencia '{nombre}' no tiene Token_HASH_Salida configurado.";
                    _logger.LogWarning("[AgenciaNotificador] {Error}", resultado.Error);
                    return resultado;
                }

                var endpoint = $"{url.TrimEnd('/')}/api/proveedores-ext/detalles/{reservacionId}/cancelar";

                var request = new HttpRequestMessage(HttpMethod.Post, endpoint);
                request.Headers.Add("X-Agencia-Token", token);
                request.Content = new StringContent(
                    System.Text.Json.JsonSerializer.Serialize(new { mensaje = motivo }),
                    System.Text.Encoding.UTF8,
                    "application/json");

                _logger.LogInformation("[AgenciaNotificador] POST {Endpoint}", endpoint);

                var response = await _http.CreateClient().SendAsync(request);
                var body = await response.Content.ReadAsStringAsync();

                resultado.Enviado = true;
                resultado.HttpStatus = (int)response.StatusCode;
                resultado.RespuestaAgencia = body;

                if (response.IsSuccessStatusCode)
                    _logger.LogInformation(
                        "[AgenciaNotificador] HTTP {Status} — {Body}", resultado.HttpStatus, body);
                else
                    _logger.LogWarning(
                        "[AgenciaNotificador] HTTP {Status} — {Body}", resultado.HttpStatus, body);
            }
            catch (Exception ex)
            {
                resultado.Error = ex.Message;
                _logger.LogError(ex, "[AgenciaNotificador] Error. ReservacionId={ResId}", reservacionId);
            }

            return resultado;
        }
    }
}