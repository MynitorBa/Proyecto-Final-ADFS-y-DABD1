using System.Text;
using System.Text.Json;
using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio que consulta todos los hoteles aliados activos registrados en la base de datos
    /// y les hace la busqueda dinamicamente usando su propia URL y TokenHASH.
    /// Cada resultado incluye la referencia al aliado de la BD de aerolineas.
    /// </summary>
    public class HotelAliadoService
    {
        private readonly HttpClient _httpClient;
        private readonly HotelAliadoRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de hoteles aliados.
        /// </summary>
        public HotelAliadoService(HotelAliadoRepository repository)
        {
            _httpClient = new HttpClient();
            _repository = repository;
        }

        /// <summary>
        /// Obtiene todos los hoteles aliados activos de la base de datos y consulta
        /// cada uno con su propia URL y token. Agrega todos los resultados en una sola lista.
        /// Cada hotel retornado incluye el ID y nombre del aliado registrado en la BD
        /// para que el frontend sepa de que sistema proviene el resultado.
        /// Si un aliado falla, se omite sin interrumpir la busqueda en los demas.
        /// </summary>
        /// <param name="dto">Criterios de busqueda: ciudad, pais, fechas y personas.</param>
        /// <returns>Lista combinada de hoteles de todos los aliados activos.</returns>
        public async Task<List<HotelAliadoDTO>> BuscarHoteles(BusquedaHotelesDTO dto)
        {
            var hotelesActivos = await _repository.ObtenerHotelesActivos();

            if (!hotelesActivos.Any())
                return new List<HotelAliadoDTO>();

            var body = new
            {
                ciudad = dto.Ciudad,
                pais = dto.Pais,
                fechaCheckIn = dto.FechaCheckIn,
                fechaCheckOut = dto.FechaCheckOut,
                cantidadPersonas = dto.CantidadPersonas
            };

            var resultados = new List<HotelAliadoDTO>();

            foreach (var aliado in hotelesActivos)
            {
                try
                {
                    var content = new StringContent(
                        JsonSerializer.Serialize(body),
                        Encoding.UTF8,
                        "application/json"
                    );

                    // Cada aliado tiene su propio token y URL registrados en la BD
                    var request = new HttpRequestMessage(
                        HttpMethod.Post,
                        $"{aliado.Url}/aerolinea/busqueda"
                    );
                    request.Headers.Add("X-Aerolinea-Token", aliado.TokenHash);
                    request.Content = content;

                    var response = await _httpClient.SendAsync(request);

                    if (!response.IsSuccessStatusCode) continue;

                    var json = await response.Content.ReadAsStringAsync();

                    var hoteles = JsonSerializer.Deserialize<List<JsonElement>>(json,
                        new JsonSerializerOptions { PropertyNameCaseInsensitive = true });

                    if (hoteles == null) continue;

                    // Mapea cada hotel e inyecta el ID y nombre del aliado de la BD
                    resultados.AddRange(hoteles.Select(h => new HotelAliadoDTO
                    {
                        Id = h.GetProperty("id").GetInt32(),
                        Nombre = h.GetProperty("nombre").GetString(),
                        Direccion = h.TryGetProperty("direccion", out var dir) ? dir.GetString() : null,
                        Ciudad = h.TryGetProperty("ciudad", out var ciu) ? ciu.GetString() : null,
                        Pais = h.TryGetProperty("pais", out var pais) ? pais.GetString() : null,
                        Descripcion = h.TryGetProperty("descripcion", out var desc) ? desc.GetString() : null,
                        Rating = h.TryGetProperty("rating", out var rat) ? rat.GetDouble() : 0,
                        AliadoId = aliado.Id,     // viene de HotelAliado en la BD de aerolineas
                        AliadoNombre = aliado.Nombre  // viene de HotelAliado en la BD de aerolineas
                    }));
                }
                catch
                {
                    // Si un aliado falla, continua con los demas
                    continue;
                }
            }

            return resultados;
        }
    }
}