using System.Text.RegularExpressions;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de administracion de vuelos. Contiene la logica de negocio para crear,
    /// cancelar y consultar vuelos, asi como verificar disponibilidad de aviones y tripulantes.
    /// Al cancelar un vuelo notifica por correo a todos los pasajeros afectados.
    /// </summary>
    public class AdminVueloService
    {
        private readonly AdminVueloRepository       _adminVueloRepository;
        private readonly RutaRepository             _rutaRepository;
        private readonly EmailHelper                _emailHelper;
        private readonly ILogger<AdminVueloService> _logger;
        private readonly LogReservacionRepository   _logRepo;

        /// <summary>
        /// Inicializa el servicio con los repositorios necesarios para la gestion de vuelos,
        /// rutas, el helper de correo y el logger para registrar errores en notificaciones masivas.
        /// </summary>
        public AdminVueloService(
            AdminVueloRepository       adminVueloRepository,
            RutaRepository             rutaRepository,
            EmailHelper                emailHelper,
            ILogger<AdminVueloService> logger,
            LogReservacionRepository   logRepo)
        {
            _adminVueloRepository = adminVueloRepository;
            _rutaRepository       = rutaRepository;
            _emailHelper          = emailHelper;
            _logger               = logger;
            _logRepo              = logRepo;
        }

        /// <summary>
        /// Crea un nuevo vuelo aplicando validaciones sobre numero de vuelo, aeropuertos, avion,
        /// fecha, horario, cantidad de boletos, precios y existencia de ruta entre los aeropuertos.
        /// Retorna el ID del vuelo creado.
        /// </summary>
        public async Task<int> CrearVuelo(CrearVueloAdminDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.NumeroVuelo))
                throw new ArgumentException("El número de vuelo es obligatorio");

            if (dto.AeropuertoOrigenId <= 0)
                throw new ArgumentException("Debe seleccionar un aeropuerto de origen");

            if (dto.AeropuertoDestinoId <= 0)
                throw new ArgumentException("Debe seleccionar un aeropuerto de destino");

            if (dto.AeropuertoOrigenId == dto.AeropuertoDestinoId)
                throw new ArgumentException("El aeropuerto de origen y destino no pueden ser iguales");

            if (dto.AvionId <= 0)
                throw new ArgumentException("Debe seleccionar un avión");

            if (dto.Fecha < DateTime.Now.Date)
                throw new ArgumentException("La fecha del vuelo no puede ser en el pasado");

            if (!TimeSpan.TryParse(dto.HoraSalida, out _))
                throw new ArgumentException("El formato de hora de salida es inválido");

            // NOTA: HoraLlegada y FechaLlegada ya NO se validan aquí.
            // Son calculadas automáticamente en el repositorio usando:
            //   DuracionEstimada de la Ruta + ZonaHoraria de los aeropuertos.

            if (dto.BoletosTurista < 0)
                throw new ArgumentException("Los boletos de clase turista no pueden ser negativos");

            if (dto.BoletosEjecutivo < 0)
                throw new ArgumentException("Los boletos de clase ejecutiva no pueden ser negativos");

            if (dto.BoletosTurista == 0 && dto.BoletosEjecutivo == 0)
                throw new ArgumentException("Debe asignar al menos un boleto turista o ejecutivo");

            if (dto.PrecioTurista <= 0)
                throw new ArgumentException("El precio de clase turista debe ser mayor a 0");

            if (dto.PrecioEjecutiva <= 0)
                throw new ArgumentException("El precio de clase ejecutiva debe ser mayor a 0");

            // Validar que exista una ruta entre los aeropuertos seleccionados
            bool rutaExiste = await _rutaRepository.ExisteRuta(
                dto.AeropuertoOrigenId, dto.AeropuertoDestinoId);

            if (!rutaExiste)
                throw new InvalidOperationException(
                    "No existe una ruta entre los aeropuertos seleccionados. " +
                    "Ve a 'Gestionar Rutas' y crea la ruta antes de crear el vuelo.");

            return await _adminVueloRepository.CrearVuelo(dto);
        }

        /// <summary>
        /// Retorna el historial completo de vuelos registrados en el sistema,
        /// incluyendo vuelos pasados, activos y cancelados.
        /// </summary>
        public async Task<List<VueloHistorialDTO>> ObtenerHistorialVuelos()
        {
            return await _adminVueloRepository.ObtenerHistorialVuelos();
        }

        /// <summary>
        /// Cancela un vuelo existente dado su identificador. Valida que el ID sea mayor a cero
        /// antes de proceder con la cancelacion en el repositorio.
        /// Antes de cancelar consulta los pasajeros afectados y, tras la cancelacion exitosa,
        /// envia un correo de aviso a cada uno de forma individual (best-effort: si un correo
        /// falla se loguea el error y se continua con los restantes sin revertir la cancelacion).
        /// </summary>
        public async Task<bool> CancelarVuelo(int vueloId, string? ip = null, string? userAgent = null)
        {
            if (vueloId <= 0)
                throw new ArgumentException("ID de vuelo invalido");

            // 1. Obtener lista de pasajeros afectados ANTES de cancelar para tener
            //    los datos de sus reservaciones todavia en estado activo
            var afectados = await _adminVueloRepository.ObtenerAfectadosPorVuelo(vueloId);

            _logger.LogInformation(
                "Cancelando vuelo {VueloId}. Pasajeros afectados: {Total}",
                vueloId, afectados.Count);

            // 2. Ejecutar la cancelacion en la base de datos
            var cancelado = await _adminVueloRepository.CancelarVuelo(vueloId);

            if (!cancelado)
            {
                await _logRepo.Registrar(
                    LogReservacionRepository.TipoCancelacionAdminFallida,
                    null, null, null, null, false, ip, userAgent,
                    $"Vuelo {vueloId} no pudo cancelarse (ya cancelado o no existe)");
                return false;
            }

            // 3. Registrar un log por cada reservacion afectada
            foreach (var afectado in afectados)
            {
                var reservacionId = afectado.ReservacionID > 0 ? (int?)afectado.ReservacionID : null;
                await _logRepo.Registrar(
                    LogReservacionRepository.TipoCancelacionAdminExitosa,
                    reservacionId, null, null, null, true, ip, userAgent,
                    $"Vuelo {vueloId} cancelado por administrador");
            }

            // Si no habia reservaciones activas, registrar igualmente el evento del vuelo
            if (afectados.Count == 0)
            {
                await _logRepo.Registrar(
                    LogReservacionRepository.TipoCancelacionAdminExitosa,
                    null, null, null, null, true, ip, userAgent,
                    $"Vuelo {vueloId} cancelado por administrador (sin reservaciones activas)");
            }

            // 4. Notificar a cada pasajero afectado con try-catch individual
            //    Un fallo de correo no revierte la cancelacion del vuelo
            int enviados  = 0;
            int fallidos  = 0;

            foreach (var afectado in afectados)
            {
                if (string.IsNullOrEmpty(afectado.EmailUsuario))
                    continue;

                try
                {
                    string html = EmailTemplates.CorreoCancelacionVuelo(
                        afectado.NombreUsuario,
                        afectado.NoReservacion,
                        afectado.NumeroVuelo,
                        afectado.OrigenCodigo,
                        afectado.DestinoCodigo,
                        afectado.FechaVuelo);

                    await _emailHelper.Enviar(
                        afectado.EmailUsuario,
                        $"Broom AirLine - Vuelo {afectado.NumeroVuelo} Cancelado",
                        html);

                    enviados++;
                }
                catch (Exception ex)
                {
                    fallidos++;
                    _logger.LogError(ex,
                        "Error al notificar cancelacion de vuelo {VueloId} al usuario {Email} (reservacion {NoReservacion})",
                        vueloId, afectado.EmailUsuario, afectado.NoReservacion);
                }
            }

            _logger.LogInformation(
                "Notificaciones de cancelacion de vuelo {VueloId}: {Enviados} enviadas, {Fallidos} fallidas",
                vueloId, enviados, fallidos);

            return true;
        }

        /// <summary>
        /// Edita un vuelo existente. Solo permitido con mas de 60 dias de anticipacion.
        /// Recalcula la hora de llegada, valida tripulacion y capacidad del avion.
        /// No permite modificar el precio ni el codigo del vuelo.
        /// Envia correo de aviso a todos los pasajeros afectados con el motivo del cambio.
        /// </summary>
        public async Task<int> EditarVuelo(int vueloId, EditarVueloDTO dto)
        {
            // Validar motivo obligatorio
            if (string.IsNullOrWhiteSpace(dto.Motivo))
                throw new ArgumentException("El motivo del cambio es obligatorio.");

            // Obtener vuelo actual
            var vuelo = await _adminVueloRepository.ObtenerVueloPorId(vueloId);
            if (vuelo == null)
                throw new ArgumentException("Vuelo no encontrado");

            // Validar restricción de 60 días
            var fechaSalidaLocal = vuelo.Fecha.Date + vuelo.HoraSalida;
            var diasRestantes = (fechaSalidaLocal - DateTime.Now).TotalDays;
            if (diasRestantes < 60)
                throw new InvalidOperationException(
                    $"No se puede editar: el vuelo sale en {diasRestantes:F0} días. Se requieren mínimo 60 días de anticipación.");

            // Validar tripulación
            if (dto.TripulantesIds.Count != 5)
                throw new ArgumentException("El vuelo requiere exactamente 5 tripulantes");

            // Validar composición por roles
            var tripulantes = await _adminVueloRepository.ObtenerTripulantesPorIds(dto.TripulantesIds);
            int pilotos    = tripulantes.Count(t => t.RolID == 1);
            int copilotos  = tripulantes.Count(t => t.RolID == 2);
            int auxiliares = tripulantes.Count(t => t.RolID == 3);

            if (pilotos < 1)    throw new ArgumentException("Debe asignar al menos 1 Piloto");
            if (copilotos < 1)  throw new ArgumentException("Debe asignar al menos 1 Copiloto");
            if (auxiliares < 3) throw new ArgumentException("Debe asignar al menos 3 Auxiliares de vuelo");

            // Validar que el nuevo avión tenga capacidad suficiente para boletos ya vendidos
            var avion = await _adminVueloRepository.ObtenerAvionPorId(dto.AvionId);
            if (avion == null)
                throw new ArgumentException("Avión no encontrado");

            var boletosVendidos = await _adminVueloRepository.ObtenerBoletosVendidos(vueloId);
            if (avion.CapacidadPasajeros < boletosVendidos)
                throw new ArgumentException(
                    $"El avión '{avion.Marca} {avion.Modelo}' tiene capacidad para {avion.CapacidadPasajeros} pasajeros, " +
                    $"pero ya hay {boletosVendidos} boleto(s) vendido(s) en este vuelo.");

            // Obtener pasajeros afectados ANTES de actualizar (mientras las reservaciones siguen activas)
            var afectados = await _adminVueloRepository.ObtenerAfectadosPorVuelo(vueloId);

            // Recalcular hora de llegada usando la ruta del vuelo
            var (duracion, tzOrigen, tzDestino) = await _adminVueloRepository.ObtenerInfoRutaPorId(vuelo.RutaId);
            var (horaLlegada, fechaLlegada, _, _) = RutaService.CalcularLlegadaConZonas(
                dto.Fecha, TimeSpan.Parse(dto.HoraSalida), duracion, tzOrigen, tzDestino);

            // Actualizar en BD
            await _adminVueloRepository.ActualizarVuelo(vueloId, dto, horaLlegada, fechaLlegada);

            // Notificar a cada pasajero con correo de disculpa (best-effort)
            int enviados = 0;
            int fallidos = 0;

            foreach (var afectado in afectados)
            {
                if (string.IsNullOrEmpty(afectado.EmailUsuario))
                    continue;

                try
                {
                    string html = EmailTemplates.CorreoCambioVuelo(
                        afectado.NombreUsuario,
                        afectado.NoReservacion,
                        afectado.NumeroVuelo,
                        afectado.OrigenCodigo,
                        afectado.DestinoCodigo,
                        afectado.FechaVuelo,
                        dto.Fecha.ToString("yyyy-MM-dd"),
                        dto.HoraSalida,
                        dto.Motivo);

                    await _emailHelper.Enviar(
                        afectado.EmailUsuario,
                        $"Broom AirLine - Cambio en tu vuelo {afectado.NumeroVuelo}",
                        html);

                    enviados++;
                }
                catch (Exception ex)
                {
                    fallidos++;
                    _logger.LogError(ex,
                        "Error al notificar cambio de vuelo {VueloId} al usuario {Email} (reservacion {NoReservacion})",
                        vueloId, afectado.EmailUsuario, afectado.NoReservacion);
                }
            }

            _logger.LogInformation(
                "Notificaciones de cambio de vuelo {VueloId}: {Enviados} enviadas, {Fallidos} fallidas",
                vueloId, enviados, fallidos);

            return vueloId;
        }

        /// <summary>
        /// Retorna el conjunto de IDs de aviones que ya tienen un vuelo programado
        /// para la fecha, hora de salida y aeropuerto de origen indicados.
        /// Permite filtrar aviones no disponibles al momento de crear un vuelo nuevo.
        /// </summary>
        /// <summary>
        /// Cambia el avión asignado a un vuelo. Valida mínimo 48h de anticipación
        /// y que el nuevo avión tenga capacidad suficiente para los boletos ya vendidos.
        /// </summary>
        public async Task CambiarAvion(int vueloId, int nuevoAvionId)
        {
            var vuelo = await _adminVueloRepository.ObtenerVueloPorId(vueloId);
            if (vuelo == null)
                throw new ArgumentException("Vuelo no encontrado");

            var salidaDateTime = vuelo.Fecha.Date + vuelo.HoraSalida;
            var horasRestantes = (salidaDateTime - DateTime.Now).TotalHours;
            if (horasRestantes < 48)
                throw new InvalidOperationException(
                    $"No se puede cambiar el avión: faltan {horasRestantes:F0}h. Se requieren al menos 48h de anticipación.");

            var avion = await _adminVueloRepository.ObtenerAvionPorId(nuevoAvionId);
            if (avion == null)
                throw new ArgumentException("Avión no encontrado");

            var boletosVendidos = await _adminVueloRepository.ObtenerBoletosVendidos(vueloId);
            if (avion.CapacidadPasajeros < boletosVendidos)
                throw new ArgumentException(
                    $"El avión '{avion.Marca} {avion.Modelo}' tiene capacidad para {avion.CapacidadPasajeros} pasajeros, " +
                    $"pero ya hay {boletosVendidos} boleto(s) vendido(s) en este vuelo.");

            await _adminVueloRepository.CambiarAvionVuelo(vueloId, nuevoAvionId);
        }

        public async Task<HashSet<int>> ObtenerAvionesOcupados(
            DateTime fecha, TimeSpan horaSalida, int aeropuertoOrigenId)
            => await _adminVueloRepository.ObtenerAvionesOcupados(fecha, horaSalida, aeropuertoOrigenId);

        /// <summary>
        /// Retorna el conjunto de IDs de tripulantes que ya estan asignados a algun vuelo
        /// en la fecha y hora de salida indicadas. Permite evitar conflictos de asignacion.
        /// </summary>
        public async Task<HashSet<int>> ObtenerTripulantesOcupados(DateTime fecha, TimeSpan horaSalida)
            => await _adminVueloRepository.ObtenerTripulantesOcupados(fecha, horaSalida);

        // ─────────────────────────────────────────────────────────────────
        //  SIGUIENTE NÚMERO DE VUELO
        // ─────────────────────────────────────────────────────────────────
        /// <summary>
        /// Calcula el siguiente numero de secuencia disponible para el prefijo indicado.
        /// Valida que el prefijo sea exactamente 2 letras mayusculas antes de consultar.
        /// </summary>
        public async Task<string> ObtenerSiguienteNumeroVuelo(string prefijo)
        {
            prefijo = prefijo?.Trim().ToUpper() ?? "";
            if (!Regex.IsMatch(prefijo, @"^[A-Z]{4}$"))
                throw new ArgumentException("El prefijo debe tener exactamente 4 letras mayúsculas.");

            return await _adminVueloRepository.ObtenerSiguienteNumeroVuelo(prefijo);
        }

        // ─────────────────────────────────────────────────────────────────
        //  TRIPULANTES DE UN VUELO
        // ─────────────────────────────────────────────────────────────────
        /// <summary>
        /// Retorna la lista de tripulantes asignados a un vuelo concreto.
        /// </summary>
        public async Task<List<TripulanteDTO>> ObtenerTripulantesDelVuelo(int vueloId)
            => await _adminVueloRepository.ObtenerTripulantesDelVuelo(vueloId);
    }
}
