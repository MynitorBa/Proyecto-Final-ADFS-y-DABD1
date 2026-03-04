using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class RutaService
    {
        private readonly RutaRepository _repository;

        public RutaService(RutaRepository repository)
        {
            _repository = repository;
        }

        // ─────────────────────────────────────────────────────────────────
        //  LISTAR
        // ─────────────────────────────────────────────────────────────────
        public async Task<List<RutaDTO>> ObtenerTodas()
            => await _repository.ObtenerTodas();

        // ─────────────────────────────────────────────────────────────────
        //  ACTUALIZAR DURACIÓN
        // ─────────────────────────────────────────────────────────────────
        public async Task<bool> ActualizarDuracion(int id, int duracionMinutos)
        {
            if (duracionMinutos <= 0)
                throw new ArgumentException("La duración debe ser mayor a 0 minutos");
            if (duracionMinutos > 10000)
                throw new ArgumentException("La duración no puede exceder 10,000 minutos (~7 días)");

            return await _repository.ActualizarDuracion(id, duracionMinutos);
        }

        // ─────────────────────────────────────────────────────────────────
        //  CALCULAR HORA DE LLEGADA (con zonas horarias)
        // ─────────────────────────────────────────────────────────────────
        public async Task<CalculoLlegadaResponseDTO> CalcularLlegada(CalculoLlegadaRequestDTO request)
        {
            if (request.AeropuertoOrigenId <= 0 || request.AeropuertoDestinoId <= 0)
                throw new ArgumentException("IDs de aeropuerto inválidos");

            if (!DateOnly.TryParse(request.FechaSalida, out var fechaSalidaDate))
                throw new ArgumentException("Formato de fecha inválido (esperado: yyyy-MM-dd)");

            if (!TimeSpan.TryParse(request.HoraSalida, out var horaSalida))
                throw new ArgumentException("Formato de hora inválido (esperado: HH:mm)");

            var (duracion, tzOrigenId, tzDestinoId) = await _repository.ObtenerInfoRuta(
                request.AeropuertoOrigenId, request.AeropuertoDestinoId);  // nunca null — usa (120,null,null) como default

            var fechaSalida = fechaSalidaDate.ToDateTime(TimeOnly.MinValue);

            var (horaLlegada, fechaLlegada, usoZonas, nota) =
                CalcularLlegadaConZonas(fechaSalida, horaSalida, duracion, tzOrigenId, tzDestinoId);

            return new CalculoLlegadaResponseDTO
            {
                HoraLlegada = horaLlegada.ToString(@"HH\:mm"),
                FechaLlegada = fechaLlegada.ToString("yyyy-MM-dd"),
                DuracionMinutos = duracion,
                ZonaHorariaOrigen = tzOrigenId,
                ZonaHorariaDestino = tzDestinoId,
                UsoZonasHorarias = usoZonas,
                Nota = nota
            };
        }

        // ─────────────────────────────────────────────────────────────────
        //  LÓGICA INTERNA DE CÁLCULO DE LLEGADA
        // ─────────────────────────────────────────────────────────────────
        /// <summary>
        /// Calcula la hora y fecha de llegada local en el aeropuerto de destino.
        /// Toma en cuenta las zonas horarias si están disponibles.
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

            // Intentar conversión con zonas horarias IANA
            if (!string.IsNullOrWhiteSpace(tzOrigenId) && !string.IsNullOrWhiteSpace(tzDestinoId))
            {
                try
                {
                    var tzOrigen = TimeZoneInfo.FindSystemTimeZoneById(tzOrigenId);
                    var tzDestino = TimeZoneInfo.FindSystemTimeZoneById(tzDestinoId);

                    // Salida en hora local origen → UTC
                    var departureUtc = TimeZoneInfo.ConvertTimeToUtc(departureDateTimeLocal, tzOrigen);
                    // Agregar duración del vuelo
                    var arrivalUtc = departureUtc.AddMinutes(duracionMinutos);
                    // Convertir llegada UTC → hora local destino
                    var arrivalLocal = TimeZoneInfo.ConvertTimeFromUtc(arrivalUtc, tzDestino);

                    var offsetOrigen = tzOrigen.GetUtcOffset(departureDateTimeLocal);
                    var offsetDestino = tzDestino.GetUtcOffset(arrivalLocal);
                    var diffHoras = (offsetDestino - offsetOrigen).TotalHours;

                    string nota = diffHoras == 0
                        ? $"Misma zona horaria. Vuelo de {duracionMinutos} min."
                        : $"Diferencia de zona horaria: {(diffHoras > 0 ? "+" : "")}{diffHoras:0.##}h respecto al origen. " +
                          $"Vuelo de {duracionMinutos} min.";

                    return (arrivalLocal.TimeOfDay, arrivalLocal.Date, true, nota);
                }
                catch (TimeZoneNotFoundException ex)
                {
                    // Zona horaria no reconocida — fallback sin conversión
                    var notaError = $"Zona horaria no reconocida ({ex.Message}). Se usó cálculo sin conversión.";
                    var fallback = departureDateTimeLocal.AddMinutes(duracionMinutos);
                    return (fallback.TimeOfDay, fallback.Date, false, notaError);
                }
            }

            // Sin zonas horarias: suma directa (hora local sin conversión)
            var arrival = departureDateTimeLocal.AddMinutes(duracionMinutos);
            string notaSinTz = "Zonas horarias no configuradas. El tiempo de llegada es hora local estimada sin conversión de zona.";
            return (arrival.TimeOfDay, arrival.Date, false, notaSinTz);
        }
        // ── Verificar si existe ruta ──────────────────────────────────────
        public async Task<bool> ExisteRuta(int origenId, int destinoId)
            => await _repository.ExisteRuta(origenId, destinoId);

        // ── Crear ruta manualmente desde el panel admin ───────────────────
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

    }
}