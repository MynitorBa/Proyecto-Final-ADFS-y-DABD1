using System.Text.Json.Serialization;

namespace Aerolinea.API.DTOs
{
    public class HandshakeRequestDTO
    {
        [JsonPropertyName("token_entrada")]
        public string TokenEntrada { get; set; } = string.Empty;

        [JsonPropertyName("url_agencia")]
        public string UrlAgencia { get; set; } = string.Empty;
    }

    public class HandshakeResponseDTO
    {
        [JsonPropertyName("token_salida")]
        public string TokenSalida { get; set; } = string.Empty;
    }
}