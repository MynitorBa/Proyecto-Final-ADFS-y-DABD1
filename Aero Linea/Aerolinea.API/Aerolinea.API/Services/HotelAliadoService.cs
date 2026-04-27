using System.Text;
using System.Text.Json;
using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de hoteles aliados. Gestiona la busqueda dinamica de hoteles consultando
    /// cada aliado activo con su propia URL y TokenHASH, el registro y consulta de hoteles
    /// por usuarios Webservice, y la administracion completa desde el panel de administracion.
    /// </summary>
    public class HotelAliadoService
    {
        private readonly HttpClient _httpClient;
        private readonly HotelAliadoRepository _repository;

        // Host que reemplaza 'localhost' cuando el backend corre dentro de Docker.
        // Se lee de HANDSHAKE_HOST_OVERRIDE (ej: "host.docker.internal").
        // Si la variable no esta definida, las URLs se usan tal cual (entorno local).
        private static readonly string? _hostOverride =
            Environment.GetEnvironmentVariable("HANDSHAKE_HOST_OVERRIDE");

        /// <summary>
        /// Inicializa el servicio con el repositorio de hoteles aliados.
        /// </summary>
        public HotelAliadoService(HotelAliadoRepository repository)
        {
            _httpClient = new HttpClient();
            _repository = repository;
        }

        /// <summary>
        /// Si HANDSHAKE_HOST_OVERRIDE esta definido, reemplaza el host 'localhost'
        /// en la URL por el valor del override para que las llamadas salgan del
        /// contenedor Docker hacia el host de la maquina anfitriona.
        /// </summary>
        private static string AplicarHostOverride(string url)
        {
            if (string.IsNullOrEmpty(_hostOverride)) return url;
            return url.Replace("localhost", _hostOverride);
        }

        /// <summary>
        /// Obtiene todos los hoteles aliados activos de la base de datos y consulta
        /// cada uno con su propia URL y token. Agrega todos los resultados en una sola lista.
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

                    // Aplica el override de host para que la llamada salga del contenedor
                    var urlDestino = AplicarHostOverride(aliado.Url);

                    var request = new HttpRequestMessage(
                        HttpMethod.Post,
                        $"{urlDestino}/aerolinea/busqueda"
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
                        AliadoId = aliado.Id,
                        AliadoNombre = aliado.Nombre
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

        // Usado por el propio usuario Webservice para registrar su hotel aliado.
        /// <summary>
        /// Permite que un usuario con rol Webservice registre su propio hotel aliado.
        /// Verifica que no tenga ya un hotel ni una agencia registrada.
        /// El ID del usuario se toma de la sesion activa.
        /// </summary>
        public async Task<MiHotelDTO> CrearHotelWebservice(int usuarioId, CrearHotelWebserviceDTO dto)
        {
            bool yaExisteHotel = await _repository.UsuarioYaTieneHotelAliado(usuarioId);
            if (yaExisteHotel)
                throw new Exception("Ya tienes un hotel aliado registrado. Solo se permite uno por cuenta Webservice.");

            // Un usuario Webservice no puede tener hotel Y agencia al mismo tiempo
            bool yaExisteAgencia = await _repository.UsuarioYaTieneAgencia(usuarioId);
            if (yaExisteAgencia)
                throw new Exception("Ya tienes una agencia registrada. Un usuario Webservice solo puede registrar una agencia o un hotel, no ambos.");

            return await _repository.CrearHotelWebservice(usuarioId, dto);
        }

        /// <summary>
        /// Retorna la informacion del hotel aliado asociado al usuario Webservice autenticado.
        /// Retorna null si el usuario aun no tiene ningun hotel registrado.
        /// </summary>
        public async Task<MiHotelDTO?> ObtenerMiHotel(int usuarioId)
        {
            return await _repository.ObtenerHotelPorUsuarioId(usuarioId);
        }

        // ── Admin: obtener todos los hoteles ──────────────────────────────────
        /// <summary>
        /// Retorna la lista completa de hoteles aliados con datos del usuario asignado.
        /// Destinado al uso exclusivo del panel de administracion.
        /// </summary>
        public async Task<List<HotelAdminDTO>> ObtenerTodosAdmin()
            => await _repository.ObtenerTodosAdmin();

        // ── Admin: crear hotel asignando un usuario Webservice ────────────────
        /// <summary>
        /// Crea un nuevo hotel aliado desde el panel de administracion, asignandolo
        /// al usuario Webservice indicado. Verifica que el usuario exista, tenga rol
        /// Webservice y no tenga ya ninguna otra entidad asignada (agencia o hotel).
        /// </summary>
        public async Task<HotelAdminDTO> CrearHotelAdmin(CrearHotelAdminDTO dto)
        {
            int rolId = await _repository.ObtenerRolUsuario(dto.UsuarioWEBIs);
            if (rolId == 0)
                throw new Exception("El usuario no existe.");
            if (rolId != 3)
                throw new Exception("El usuario debe tener rol Webservice.");

            bool yaExisteHotel = await _repository.UsuarioYaTieneHotelAliado(dto.UsuarioWEBIs);
            if (yaExisteHotel)
                throw new Exception("El usuario ya tiene un hotel aliado registrado.");

            bool yaExisteAgencia = await _repository.UsuarioYaTieneAgencia(dto.UsuarioWEBIs);
            if (yaExisteAgencia)
                throw new Exception("El usuario ya tiene una agencia registrada. Un usuario Webservice solo puede tener una entidad.");

            return await _repository.CrearHotelAdmin(dto);
        }

        // ── Admin: actualizar estado ──────────────────────────────────────────
        /// <summary>
        /// Actualiza el estado de un hotel aliado. El EstadoId debe ser un valor
        /// valido del catalogo EstadoAliado (1=Activo, 2=Inactivo, 3=Suspendido).
        /// </summary>
        public async Task ActualizarEstado(int hotelId, int estadoId)
        {
            if (estadoId < 1 || estadoId > 3)
                throw new Exception("Estado no válido.");
            bool ok = await _repository.ActualizarEstado(hotelId, estadoId);
            if (!ok) throw new Exception("No se encontró el hotel indicado.");
        }

        // ── Admin: asignar usuario Webservice ─────────────────────────────────
        /// <summary>
        /// Asigna un usuario Webservice a un hotel aliado existente. Verifica que el
        /// usuario exista, tenga rol Webservice y no este ya vinculado a ninguna entidad.
        /// </summary>
        public async Task AsignarUsuario(int hotelId, int usuarioId)
        {
            int rolId = await _repository.ObtenerRolUsuario(usuarioId);
            if (rolId == 0) throw new Exception("El usuario no existe.");
            if (rolId != 3) throw new Exception("El usuario debe tener rol Webservice.");

            bool tieneHotel = await _repository.UsuarioYaTieneHotelAliado(usuarioId);
            if (tieneHotel) throw new Exception("Ese usuario ya está asignado a otro hotel aliado.");

            bool tieneAgencia = await _repository.UsuarioYaTieneAgencia(usuarioId);
            if (tieneAgencia) throw new Exception("Ese usuario ya tiene una agencia registrada.");

            bool ok = await _repository.AsignarUsuario(hotelId, usuarioId);
            if (!ok) throw new Exception("No se encontró el hotel indicado.");
        }

        // ── Admin: actualizar URLs ────────────────────────────────────────────
        /// <summary>
        /// Actualiza la URL de la API y la URL publica de un hotel aliado.
        /// Ninguna de las dos URLs puede estar vacia.
        /// </summary>
        public async Task ActualizarUrls(int hotelId, string url, string urlParaUsuario, string? urlHomeAliado)
        {
            if (string.IsNullOrWhiteSpace(url))
                throw new Exception("La URL de la API no puede estar vacía.");
            if (string.IsNullOrWhiteSpace(urlParaUsuario))
                throw new Exception("La URL publica no puede estar vacía.");
            // urlHomeAliado es opcional, no se valida como obligatoria

            bool ok = await _repository.ActualizarUrls(hotelId, url.Trim(), urlParaUsuario.Trim(), urlHomeAliado?.Trim());
            if (!ok) throw new Exception("No se encontró el hotel indicado.");
        }

        /// <summary>
        /// Retorna el nombre y URL home de todos los hoteles aliados activos
        /// que tienen URLHomeAliado configurada. Para recomendación al usuario final.
        /// </summary>
        public async Task<List<HotelHomeDTO>> ObtenerHotelesHome()
            => await _repository.ObtenerHotelesHome();
    }
}