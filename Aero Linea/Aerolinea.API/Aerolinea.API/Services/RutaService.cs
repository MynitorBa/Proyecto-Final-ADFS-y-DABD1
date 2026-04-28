using Aerolinea.API.Helpers;
using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de rutas aereas. Gestiona la logica de negocio para consultar, crear
    /// y actualizar rutas entre aeropuertos, incluyendo el calculo de hora de llegada
    /// con conversion de zonas horarias IANA y Windows.
    /// </summary>
    public class RutaService : IRutaService
    {
        private readonly IRutaRepository _repository;
        private readonly EmailHelper _emailHelper;
        private readonly ILogger<RutaService> _logger;

        /// <summary>
        /// Inicializa el servicio con el repositorio de rutas, el helper de correo y el logger.
        /// </summary>
        public RutaService(IRutaRepository repository, EmailHelper emailHelper, ILogger<RutaService> logger)
        {
            _repository  = repository;
            _emailHelper = emailHelper;
            _logger      = logger;
        }

        /// <summary>
        /// Retorna la lista completa de rutas registradas en el sistema.
        /// </summary>
        public async Task<List<RutaDTO>> ObtenerTodas()
            => await _repository.ObtenerTodas();

        /// <summary>
        /// Actualiza la duracion estimada en minutos de una ruta existente.
        /// Valida que el valor sea mayor a 0 y no supere los 10,000 minutos.
        /// </summary>
        public async Task<bool> ActualizarDuracion(int id, int duracionMinutos)
        {
            if (duracionMinutos <= 0)
                throw new ArgumentException("La duración debe ser mayor a 0 minutos");
            if (duracionMinutos > 10000)
                throw new ArgumentException("La duración no puede exceder 10,000 minutos (~7 días)");

            return await _repository.ActualizarDuracion(id, duracionMinutos);
        }

        /// <summary>
        /// Calcula la hora y fecha de llegada estimadas dado el aeropuerto de origen, destino,
        /// fecha y hora de salida. Aplica conversion de zonas horarias si ambos aeropuertos
        /// tienen zona configurada; de lo contrario usa calculo directo sin conversion.
        /// </summary>
        public async Task<CalculoLlegadaResponseDTO> CalcularLlegada(CalculoLlegadaRequestDTO request)
        {
            if (request.AeropuertoOrigenId <= 0 || request.AeropuertoDestinoId <= 0)
                throw new ArgumentException("IDs de aeropuerto inválidos");

            // ── Fix: usar DateTime.TryParse en lugar de DateOnly.TryParse
            // DateOnly puede fallar con ciertos locales en Windows
            if (!DateTime.TryParse(request.FechaSalida, out var fechaSalidaDateTime))
                throw new ArgumentException($"Formato de fecha inválido: '{request.FechaSalida}' (esperado: yyyy-MM-dd)");

            if (!TimeSpan.TryParse(request.HoraSalida, out var horaSalida))
                throw new ArgumentException($"Formato de hora inválido: '{request.HoraSalida}' (esperado: HH:mm)");

            var (duracion, tzOrigenId, tzDestinoId) = await _repository.ObtenerInfoRuta(
                request.AeropuertoOrigenId, request.AeropuertoDestinoId);

            var (horaLlegada, fechaLlegada, usoZonas, nota) =
                CalcularLlegadaConZonas(fechaSalidaDateTime, horaSalida, duracion, tzOrigenId, tzDestinoId);

            return new CalculoLlegadaResponseDTO
            {
                HoraLlegada = horaLlegada.ToString(@"hh\:mm"),
                FechaLlegada = fechaLlegada.ToString("yyyy-MM-dd"),
                DuracionMinutos = duracion,
                ZonaHorariaOrigen = tzOrigenId,
                ZonaHorariaDestino = tzDestinoId,
                UsoZonasHorarias = usoZonas,
                Nota = nota
            };
        }

        /// <summary>
        /// Calcula la hora y fecha de llegada local en destino aplicando la duracion del vuelo
        /// y la diferencia de zonas horarias entre origen y destino. Si alguna zona no esta
        /// configurada o no puede resolverse, realiza el calculo sin conversion e incluye una
        /// nota explicativa en el resultado.
        /// </summary>
        public static (TimeSpan horaLlegada, DateTime fechaLlegada, bool usoZonas, string nota)
            CalcularLlegadaConZonas(
                DateTime fechaSalida,
                TimeSpan horaSalida,
                int duracionMinutos,
                string? tzOrigenId,
                string? tzDestinoId)
        {
            var departureDateTimeLocal = new DateTime(
                fechaSalida.Year, fechaSalida.Month, fechaSalida.Day,
                horaSalida.Hours, horaSalida.Minutes, horaSalida.Seconds,
                DateTimeKind.Unspecified);

            if (!string.IsNullOrWhiteSpace(tzOrigenId) && !string.IsNullOrWhiteSpace(tzDestinoId))
            {
                try
                {
                    // En Windows, .NET 6+ intenta resolver IDs IANA automáticamente.
                    // Si falla, cae al fallback sin conversión.
                    TimeZoneInfo tzOrigen = FindTimezone(tzOrigenId);
                    TimeZoneInfo tzDestino = FindTimezone(tzDestinoId);

                    var departureUtc = TimeZoneInfo.ConvertTimeToUtc(departureDateTimeLocal, tzOrigen);
                    var arrivalUtc = departureUtc.AddMinutes(duracionMinutos);
                    var arrivalLocal = TimeZoneInfo.ConvertTimeFromUtc(arrivalUtc, tzDestino);

                    var offsetOrigen = tzOrigen.GetUtcOffset(departureDateTimeLocal);
                    var offsetDestino = tzDestino.GetUtcOffset(arrivalLocal);
                    var diffHoras = (offsetDestino - offsetOrigen).TotalHours;

                    string nota = diffHoras == 0
                        ? $"Misma zona horaria. Vuelo de {duracionMinutos} min."
                        : $"Diferencia de zona horaria: {(diffHoras > 0 ? "+" : "")}{diffHoras:0.##}h. Vuelo de {duracionMinutos} min.";

                    return (arrivalLocal.TimeOfDay, arrivalLocal.Date, true, nota);
                }
                catch (Exception ex)
                {
                    // TimeZoneNotFoundException u otro error — fallback sin conversión
                    var notaError = $"Zona horaria no reconocida ({ex.Message.Split('.')[0]}). Cálculo sin conversión.";
                    var fallback = departureDateTimeLocal.AddMinutes(duracionMinutos);
                    return (fallback.TimeOfDay, fallback.Date, false, notaError);
                }
            }

            // Sin zonas horarias configuradas
            var arrival = departureDateTimeLocal.AddMinutes(duracionMinutos);
            string notaSinTz = "Zonas horarias no configuradas. Hora de llegada estimada sin conversión.";
            return (arrival.TimeOfDay, arrival.Date, false, notaSinTz);
        }

        /// <summary>
        /// Intenta resolver el timezone por ID IANA y, si falla,
        /// convierte de IANA a Windows ID para compatibilidad.
        /// </summary>
        private static TimeZoneInfo FindTimezone(string tzId)
        {
            // Intento directo (funciona en Linux y en .NET 6+ con ICU en Windows)
            try { return TimeZoneInfo.FindSystemTimeZoneById(tzId); }
            catch { /* continúa con conversión */ }

            // Conversión IANA → Windows ID (para Windows sin ICU)
            if (TimeZoneInfo.TryConvertIanaIdToWindowsId(tzId, out var windowsId))
                return TimeZoneInfo.FindSystemTimeZoneById(windowsId);

            throw new TimeZoneNotFoundException($"No se pudo resolver la zona horaria: {tzId}");
        }

        /// <summary>
        /// Verifica si ya existe una ruta registrada entre los aeropuertos de origen y destino indicados.
        /// </summary>
        public async Task<bool> ExisteRuta(int origenId, int destinoId)
            => await _repository.ExisteRuta(origenId, destinoId);

        /// <summary>
        /// Crea una nueva ruta entre dos aeropuertos con la duracion estimada en minutos.
        /// Valida que origen y destino sean distintos, que la duracion sea valida y que
        /// la ruta no exista previamente. Retorna una tupla con el resultado, el ID y un mensaje.
        /// </summary>
        public async Task<(bool creada, int rutaId, string mensaje)> CrearRuta(
            int origenId, int destinoId, int duracionEstimada)
        {
            if (origenId == destinoId)
                return (false, 0, "El aeropuerto de origen y destino no pueden ser iguales.");

            if (duracionEstimada <= 0 || duracionEstimada > 10000)
                return (false, 0, "La duración debe estar entre 1 y 10,000 minutos.");

            bool existe = await _repository.ExisteRuta(origenId, destinoId);
            if (existe)
                return (false, 0, "Ya existe una ruta entre estos aeropuertos.");

            var rutaId = await _repository.CrearRuta(origenId, destinoId, duracionEstimada);
            return (true, rutaId, "Ruta creada correctamente.");
        }

        /// <summary>
        /// Reactiva una ruta que fue desactivada previamente, permitiendo que vuelva a aceptar vuelos.
        /// Tras activar, notifica a las agencias registradas si la ruta tiene vuelos futuros (best-effort).
        /// </summary>
        public async Task<(bool ok, string mensaje)> ActivarRuta(int id)
        {
            bool activada = await _repository.ActivarRuta(id);
            if (!activada)
                return (false, "Ruta no encontrada.");

            // Notificar agencias (best-effort: si falla no se revierte la activacion)
            try
            {
                var (origenCodigo, origenCiudad, destinoCodigo, destinoCiudad) =
                    await _repository.ObtenerDescripcionRuta(id);

                var vuelosFuturos = await _repository.ObtenerVuelosFuturosPorRuta(id);
                var agencias = await _repository.ObtenerEmailsAgencias();

                foreach (var (email, nombreContacto, nombreAgencia) in agencias)
                {
                    try
                    {
                        string html = EmailTemplates.CorreoRutaActivada(
                            nombreContacto, nombreAgencia,
                            origenCodigo, origenCiudad,
                            destinoCodigo, destinoCiudad,
                            vuelosFuturos);

                        await _emailHelper.Enviar(
                            email,
                            $"Broom AirLine — Ruta {origenCodigo} → {destinoCodigo} reactivada",
                            html);

                        _logger.LogInformation(
                            "Notificacion de ruta activada enviada a agencia {Agencia} ({Email})",
                            nombreAgencia, email);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex,
                            "Error al notificar ruta activada a agencia {Agencia} ({Email})",
                            nombreAgencia, email);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al obtener datos para notificar ruta activada {RutaId}", id);
            }

            return (true, "Ruta activada correctamente.");
        }

        /// <summary>
        /// Desactiva una ruta solo si no tiene reservaciones activas (Pendiente o Confirmada)
        /// en ninguno de sus vuelos. Una ruta desactivada no acepta nuevos vuelos.
        /// Tras desactivar, notifica a todas las agencias registradas por correo (best-effort).
        /// Requiere que la columna Activo exista en la tabla Ruta.
        /// </summary>
        public async Task<(bool ok, string mensaje)> DesactivarRuta(int id)
        {
            bool tieneActivas = await _repository.TieneReservacionesActivas(id);
            if (tieneActivas)
                return (false, "No se puede desactivar: la ruta tiene reservaciones activas (Pendiente o Confirmada). Cancélalas primero.");

            // Obtener descripcion de la ruta ANTES de desactivar (para incluirla en el correo)
            var (origenCodigo, origenCiudad, destinoCodigo, destinoCiudad) =
                await _repository.ObtenerDescripcionRuta(id);

            bool desactivada = await _repository.DesactivarRuta(id);
            if (!desactivada)
                return (false, "Ruta no encontrada.");

            // Notificar a todas las agencias (best-effort: si falla el envio no se revierte la desactivacion)
            try
            {
                var agencias = await _repository.ObtenerEmailsAgencias();
                foreach (var (email, nombreContacto, nombreAgencia) in agencias)
                {
                    try
                    {
                        string html = EmailTemplates.CorreoRutaDesactivada(
                            nombreContacto, nombreAgencia,
                            origenCodigo, origenCiudad,
                            destinoCodigo, destinoCiudad);

                        await _emailHelper.Enviar(
                            email,
                            $"Broom AirLine — Ruta {origenCodigo} → {destinoCodigo} desactivada",
                            html);

                        _logger.LogInformation(
                            "Notificacion de ruta desactivada enviada a agencia {Agencia} ({Email})",
                            nombreAgencia, email);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex,
                            "Error al notificar ruta desactivada a agencia {Agencia} ({Email})",
                            nombreAgencia, email);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error al obtener agencias para notificar ruta desactivada {RutaId}", id);
            }

            return (true, "Ruta desactivada correctamente.");
        }
    }
}
