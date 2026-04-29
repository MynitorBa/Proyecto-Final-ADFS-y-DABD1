using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;
using System.Net.Http.Json;
using System.Text.Json;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio encargado de iniciar el proceso de handshake de autenticacion
    /// con un hotel aliado externo. Genera un token de entrada que identifica a la
    /// aerolinea, lo envia al hotel junto con la URL publica de la aerolinea y almacena
    /// el token de sesion resultante en la columna TokenHASH del registro HotelAliado.
    ///
    /// Cuando el backend corre dentro de Docker y la URL del hotel contiene 'localhost',
    /// se sustituye ese host por el valor de HANDSHAKE_HOST_OVERRIDE (host.docker.internal)
    /// para poder alcanzar el servidor Java corriendo en la maquina anfitriona.
    /// </summary>
    public class HandshakeHotelService
    {
        private readonly HotelAliadoRepository _repo;
        private readonly IConfiguration _config;
        private readonly IHttpClientFactory _httpFactory;

        /// <summary>
        /// Inicializa el servicio con el repositorio de hoteles, la configuracion
        /// del servidor y la fabrica de clientes HTTP para llamadas salientes.
        /// </summary>
        public HandshakeHotelService(
            HotelAliadoRepository repo,
            IConfiguration config,
            IHttpClientFactory httpFactory)
        {
            _repo = repo;
            _config = config;
            _httpFactory = httpFactory;
        }

        /// <summary>
        /// Ejecuta el flujo completo de handshake con un hotel aliado.
        /// Obtiene la URL del hotel desde la base de datos sin filtrar por estado,
        /// aplica la sustitucion de host si es necesaria para entornos Docker,
        /// genera un token de entrada, lo envia al hotel, recibe el token de sesion
        /// y lo guarda en HotelAliado.TokenHASH.
        /// </summary>
        /// <param name="hotelId">ID del registro HotelAliado con quien iniciar el handshake.</param>
        /// <returns>El token de sesion recibido del hotel aliado.</returns>
        /// <exception cref="Exception">
        /// Si el hotel no existe, no tiene URL configurada, falla la llamada HTTP
        /// o falla el guardado en base de datos.
        /// </exception>
        public async Task<string> IniciarHandshake(int hotelId)
        {
            // 1. Obtener el hotel sin filtro de estado para que el handshake funcione
            //    incluso si el hotel tiene estado Inactivo o Suspendido
            var hotel = await _repo.ObtenerHotelParaHandshake(hotelId);
            if (hotel == null)
                throw new Exception($"No se encontro ningun hotel aliado con ID {hotelId}.");

            if (string.IsNullOrWhiteSpace(hotel.Url))
                throw new Exception(
                    $"El hotel '{hotel.Nombre}' no tiene URL de API configurada. " +
                    "Edita las URLs del hotel antes de hacer handshake.");

            // 2. Aplicar sustitucion de host para entornos Docker.
            //    Si la URL almacenada tiene 'localhost' y estamos dentro de un contenedor,
            //    la reemplazamos por host.docker.internal para alcanzar el host real.
            string urlHotel = AplicarHostOverride(hotel.Url);

            // 3. Generar el token de entrada que identifica a esta aerolinea ante el hotel
            string tokenEntrada = TokenHelper.GenerarTokenHash();

            // 4. Obtener la URL publica de la aerolinea para enviarsela al hotel.
            //    El hotel la usara para buscar el registro AerolineaAliado en su BD.
            string urlAerolinea = ObtenerUrlAerolinea();

            // 5. Llamar al endpoint de handshake del hotel y recibir el token de sesion
            string tokenSalida = await LlamarHandshakeHotel(urlHotel, tokenEntrada, urlAerolinea);

            // 6. Guardar el token de sesion en TokenHASH para autenticar futuras llamadas
            bool guardado = await _repo.GuardarTokenHash(hotelId, tokenSalida);
            if (!guardado)
                throw new Exception(
                    "El token fue generado pero no se pudo guardar en la base de datos. " +
                    "Verifique que el ID del hotel sea correcto.");

            return tokenSalida;
        }

        /// <summary>
        /// Sustituye 'localhost' en la URL por el valor de HANDSHAKE_HOST_OVERRIDE
        /// cuando este configurado. Permite que el contenedor Docker alcance servicios
        /// corriendo directamente en la maquina anfitriona (ej: servidor Java del hotel).
        /// Si HANDSHAKE_HOST_OVERRIDE no esta definido o la URL no contiene 'localhost',
        /// retorna la URL original sin modificar.
        /// </summary>
        /// <param name="url">URL original del hotel aliado almacenada en la base de datos.</param>
        /// <returns>URL con el host sustituido si aplica, o la URL original.</returns>
        private string AplicarHostOverride(string url)
        {
            var hostOverride = _config["HANDSHAKE_HOST_OVERRIDE"];
            if (!string.IsNullOrWhiteSpace(hostOverride) && url.Contains("localhost"))
            {
                // Sustituye todas las ocurrencias de localhost para cubrir casos
                // con puerto explicito (ej: http://localhost:7000 -> http://host.docker.internal:7000)
                return url.Replace("localhost", hostOverride);
            }
            return url;
        }

        /// <summary>
        /// Obtiene la URL publica de la aerolinea leyendo en orden:
        /// 1. La clave "ServerURL" de appsettings.json o variables de entorno
        /// 2. La variable de entorno ASPNETCORE_URLS (primer origen)
        /// 3. La clave "urls" de configuracion
        /// El valor obtenido se envia al hotel para que identifique a la aerolinea en su BD.
        /// </summary>
        private string ObtenerUrlAerolinea()
        {
            // Intento 1: clave explicita en appsettings.json o variables de entorno
            var serverUrl = _config["ServerURL"];
            if (!string.IsNullOrWhiteSpace(serverUrl))
                return serverUrl.TrimEnd('/');

            // Intento 2: ASPNETCORE_URLS usa el primer origen definido
            var aspnetUrls = _config["ASPNETCORE_URLS"]
                ?? Environment.GetEnvironmentVariable("ASPNETCORE_URLS");
            if (!string.IsNullOrWhiteSpace(aspnetUrls))
                return aspnetUrls.Split(';')[0].TrimEnd('/');

            // Intento 3: Urls configuradas en el host builder
            var urls = _config["urls"];
            if (!string.IsNullOrWhiteSpace(urls))
                return urls.Split(';')[0].TrimEnd('/');

            throw new Exception(
                "No se pudo determinar la URL de la aerolinea. " +
                "Agrega 'ServerURL': 'http://localhost:PUERTO' en appsettings.json " +
                "donde PUERTO es el puerto en que corre este servidor.");
        }

        /// <summary>
        /// Realiza la llamada HTTP POST al endpoint de handshake del hotel aliado.
        /// Envia el token de entrada de la aerolinea y su URL publica para que el hotel
        /// identifique y registre a la aerolinea en su base de datos.
        /// Retorna el token de salida que el hotel genera para autenticar llamadas futuras.
        /// </summary>
        /// <param name="urlHotel">URL del hotel ya con host override aplicado si corresponde.</param>
        /// <param name="tokenEntrada">Token generado por la aerolinea para identificarse.</param>
        /// <param name="urlAerolinea">URL publica de la aerolinea para que el hotel la registre.</param>
        /// <returns>Token de salida asignado por el hotel.</returns>
        private async Task<string> LlamarHandshakeHotel(
            string urlHotel, string tokenEntrada, string urlAerolinea)
        {
            var client = _httpFactory.CreateClient();

            // Timeout generoso para dar tiempo a la JVM de responder
            client.Timeout = TimeSpan.FromSeconds(15);

            // El campo se llama "url_agencia" por compatibilidad con el HandshakeRequestDTO
            // del sistema hotelero que reutiliza el mismo DTO del handshake de agencias
            var payload = new
            {
                token_entrada = tokenEntrada,
                url_agencia = urlAerolinea
            };

            Console.WriteLine($"[HANDSHAKE HOTEL] Llamando a: {urlHotel}/api/aerolineas/handshake");
            Console.WriteLine($"[HANDSHAKE HOTEL] url_aerolinea enviada: {urlAerolinea}");

            HttpResponseMessage response;
            try
            {
                response = await client.PostAsJsonAsync(
                    $"{urlHotel}/api/aerolineas/handshake", payload);
            }
            catch (HttpRequestException ex)
            {
                throw new Exception(
                    $"No se pudo conectar con el hotel en '{urlHotel}'. " +
                    $"Verifica que el servidor Java este corriendo. Detalle: {ex.Message}");
            }
            catch (TaskCanceledException)
            {
                throw new Exception(
                    $"El hotel en '{urlHotel}' no respondio en 15 segundos. " +
                    "Verifica que el servidor Java este activo.");
            }

            if (!response.IsSuccessStatusCode)
            {
                var errorBody = await response.Content.ReadAsStringAsync();
                throw new Exception(
                    $"El hotel respondio con error {(int)response.StatusCode}. " +
                    $"Respuesta: {errorBody}");
            }

            var resultado = await response.Content.ReadFromJsonAsync<Dictionary<string, JsonElement>>();
            if (resultado == null || !resultado.TryGetValue("token_salida", out JsonElement tokenElement))
            {
                throw new Exception(
                    "El hotel no devolvio un token_salida valido en la respuesta. " +
                    "Verifica que el endpoint /api/aerolineas/handshake este implementado.");
            }

            string? tokenSalida = tokenElement.GetString();
            if (string.IsNullOrEmpty(tokenSalida))
            {
                throw new Exception(
                    "El hotel no devolvio un token_salida valido en la respuesta. " +
                    "Verifica que el endpoint /api/aerolineas/handshake este implementado.");
            }

            return tokenSalida;
        }
    }
}