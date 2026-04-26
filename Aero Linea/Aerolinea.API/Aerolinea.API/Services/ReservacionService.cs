using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de reservaciones para usuarios registrados. Gestiona la creacion de reservaciones
    /// y la asignacion de pasajeros, validando que el usuario este autenticado
    /// y que los datos de pasaporte sean correctos antes de persistirlos.
    /// </summary>
    public class ReservacionService : IReservacionService
    {
        private readonly IReservacionRepository _repository;
        private readonly LogReservacionRepository _logRepository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de reservaciones.
        /// </summary>
        public ReservacionService(IReservacionRepository repository,
                           LogReservacionRepository logRepository)
        {
            _repository = repository;
            _logRepository = logRepository;
        }

        /// <summary>
        /// Crea una nueva reservacion para el usuario autenticado. Requiere que el usuarioId
        /// no sea nulo; de lo contrario lanza una excepcion indicando que debe iniciar sesion.
        /// </summary>
        public async Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto,
                                                          int? usuarioId, string? ip, string? userAgent)
        {
            try
            {
                if (usuarioId == null)
                    throw new Exception("Debes iniciar sesion para crear una reservacion.");

                var reservacion = await _repository.CrearReservacion(usuarioId.Value, dto.Vuelos);

                await _logRepository.Registrar(
                    LogReservacionRepository.TipoReservacionExitosa,
                    reservacion.ReservacionId,
                    usuarioId,
                    null,
                    (decimal?)reservacion.Total,
                    true,
                    ip,
                    userAgent,
                    null
                );

                return reservacion;
            }
            catch (Exception e) when (e.Message == "Debes iniciar sesion para crear una reservacion.")
            {
                await _logRepository.Registrar(
                    LogReservacionRepository.TipoReservacionFallida,
                    null, null, null, null, false, ip, userAgent, e.Message
                );
                throw;
            }
            catch (Exception e)
            {
                await _logRepository.Registrar(
                    LogReservacionRepository.TipoReservacionErrorInterno,
                    null, null, null, null, false, ip, userAgent, e.Message
                );
                throw;
            }
        }

        /// <summary>
        /// Agrega la lista de pasajeros a una reservacion existente. Valida que cada pasajero
        /// tenga numero de pasaporte y que este contenga unicamente digitos numericos.
        /// </summary>
        public async Task AgregarPasajeros(AgregarPasajerosDTO dto, string? ip, string? userAgent)
        {
            try
            {
                foreach (var pasajero in dto.Pasajeros)
                {
                    if (string.IsNullOrWhiteSpace(pasajero.Pasaporte))
                        throw new Exception("El numero de pasaporte es obligatorio.");
                    if (!pasajero.Pasaporte.All(char.IsDigit))
                        throw new Exception($"El pasaporte de {pasajero.Nombre} {pasajero.Apellido} debe contener solo numeros.");
                }

                await _repository.AgregarPasajerosAReservacion(dto.ReservacionId, dto.Pasajeros);

                await _logRepository.Registrar(
                    LogReservacionRepository.TipoPasajerosAgregados,
                    dto.ReservacionId, null, null, null, true, ip, userAgent, null
                );
            }
            catch (Exception e)
            {
                await _logRepository.Registrar(
                    LogReservacionRepository.TipoPasajerosError,
                    dto.ReservacionId, null, null, null, false, ip, userAgent, e.Message
                );
                throw;
            }
        }
    }
}