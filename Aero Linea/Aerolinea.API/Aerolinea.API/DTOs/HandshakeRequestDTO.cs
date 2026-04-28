using System.Text.Json.Serialization;

namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para iniciar el proceso de autenticacion por handshake con una agencia externa.
    /// Contiene el token de entrada proporcionado por la agencia y su URL de origen.
    /// </summary>
    public class HandshakeRequestDTO
    {
        [JsonPropertyName("token_entrada")]
        public string TokenEntrada { get; set; } = string.Empty;

        [JsonPropertyName("url_agencia")]
        public string UrlAgencia { get; set; } = string.Empty;
    }

    /// <summary>
    /// DTO de respuesta devuelto al completar el handshake con una agencia externa.
    /// Contiene el token de salida que la agencia debe utilizar en las siguientes peticiones,
    /// y el porcentaje de ganancia que la agencia tiene configurado.
    /// </summary>
    public class HandshakeResponseDTO
    {
        [JsonPropertyName("token_salida")]
        public string TokenSalida { get; set; } = string.Empty;

        [JsonPropertyName("porcentajeGanancia")]
        public decimal PorcentajeGanancia { get; set; } = 0;
    }
}
