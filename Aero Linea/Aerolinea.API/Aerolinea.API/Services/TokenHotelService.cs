using System.Text;
using System.Text.Json;
using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio que solicita un token de alianza a un hotel aliado especifico.
    /// Busca el hotel en la BD de aerolineas por su ID, llama a su endpoint
    /// de generacion de token y retorna el resultado al frontend.
    /// </summary>
    public class TokenHotelService
    {
        private readonly HttpClient _httpClient;
        private readonly HotelAliadoRepository _repository;

        // Host que reemplaza 'localhost' cuando el backend corre dentro de Docker.
        // Se lee de HANDSHAKE_HOST_OVERRIDE (ej: "host.docker.internal").
        private static readonly string? _hostOverride =
            Environment.GetEnvironmentVariable("HANDSHAKE_HOST_OVERRIDE");

        /// <summary>
        /// Inicializa el servicio con el repositorio de hoteles aliados.
        /// </summary>
        public TokenHotelService(HotelAliadoRepository repository)
        {
            _httpClient = new HttpClient();
            _repository = repository;
        }

        /// <summary>
        /// Si HANDSHAKE_HOST_OVERRIDE esta definido, reemplaza 'localhost' en la URL
        /// por el override para que las llamadas salgan del contenedor Docker hacia el host.
        /// </summary>
        private static string AplicarHostOverride(string url)
        {
            if (string.IsNullOrEmpty(_hostOverride)) return url;
            return url.Replace("localhost", _hostOverride);
        }

        /// <summary>
        /// Busca el hotel aliado por su ID en la BD, llama a su endpoint
        /// POST /aerolinea/token con el TokenHASH y retorna el token generado
        /// junto con la URL de redireccion para el usuario.
        /// </summary>
        public async Task<TokenHotelResponseDTO> SolicitarToken(int aliadoId, TokenHotelRequestDTO dto)
        {
            var aliado = await _repository.ObtenerHotelActivoPorId(aliadoId);
            if (aliado == null)
                throw new Exception("Hotel aliado no encontrado o no activo");

            var body = new
            {
                ciudad = dto.Ciudad,
                pais = dto.Pais
            };

            var content = new StringContent(
                JsonSerializer.Serialize(body),
                Encoding.UTF8,
                "application/json"
            );

            // Aplica el override para salir del contenedor si es necesario
            var urlDestino = AplicarHostOverride(aliado.Url);

            var request = new HttpRequestMessage(
                HttpMethod.Post,
                $"{urlDestino}/aerolinea/token"
            );
            request.Headers.Add("X-Aerolinea-Token", aliado.TokenHash);
            request.Content = content;

            var response = await _httpClient.SendAsync(request);

            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception($"Error al solicitar token al hotel aliado: {error}");
            }

            var json = await response.Content.ReadAsStringAsync();

            var resultado = JsonSerializer.Deserialize<TokenHotelResponseDTO>(json,
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true });

            return resultado;
        }
    }
}