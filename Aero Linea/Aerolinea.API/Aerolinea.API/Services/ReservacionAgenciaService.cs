using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de reservaciones para agencias. Gestiona la logica de negocio para que
    /// una agencia pueda crear reservaciones con descuento, expirarlas manualmente
    /// y agregar pasajeros con sus datos de pasaporte.
    /// </summary>
    public class ReservacionAgenciaService
    {
        private readonly ReservacionAgenciaRepository _repository;
        private readonly AgenciaRepository _agenciaRepository;
        private readonly LogReservacionRepository _logRepository;

        /// <summary>
        /// Inicializa el servicio con los repositorios de reservacion de agencia y de agencias.
        /// </summary>
        public ReservacionAgenciaService(
        ReservacionAgenciaRepository repository,
        AgenciaRepository agenciaRepository,
        LogReservacionRepository logRepository)
        {
            _repository = repository;
            _agenciaRepository = agenciaRepository;
            _logRepository = logRepository;
        }

        /// <summary>
        /// Crea una nueva reservacion para la agencia indicada. Obtiene el porcentaje de descuento
        /// configurado para la agencia y lo aplica al momento de crear la reservacion en el repositorio.
        /// </summary>
        public async Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto,
                                                          int agenciaId, string? ip, string? userAgent)
        {
            try
            {
                decimal descuento = await _agenciaRepository.ObtenerDescuento(agenciaId);
                var reservacion = await _repository.CrearReservacion(dto.Vuelos, descuento, agenciaId);

                await _logRepository.Registrar(
                    LogReservacionRepository.TipoAgenciaExitosa,
                    reservacion.ReservacionId,
                    null,
                    agenciaId,
                    (decimal?)reservacion.Total,
                    true,
                    ip,
                    userAgent,
                    null
                );

                return reservacion;
            }
            catch (Exception e)
            {
                await _logRepository.Registrar(
                    LogReservacionRepository.TipoAgenciaError,
                    null, null, agenciaId, null, false, ip, userAgent, e.Message
                );
                throw;
            }
        }

        /// <summary>
        /// Expira manualmente una reservacion pendiente de la agencia. Verifica que la reservacion
        /// pertenezca a la agencia y que este en estado pendiente antes de proceder.
        /// </summary>
        public async Task ExpirarReservacion(int reservacionId, int agenciaId, string? ip, string? userAgent)
        {
            bool valida = await _repository.PerteneceAAgenciaYEstaPendiente(reservacionId, agenciaId);

            if (!valida)
            {
                await _logRepository.Registrar(
                    LogReservacionRepository.TipoAgenciaFallida,
                    reservacionId, null, agenciaId, null, false, ip, userAgent,
                    "Reservacion no encontrada, no pertenece a esta agencia o no esta pendiente"
                );
                throw new Exception("La reservación no existe, no pertenece a esta agencia, o no está en estado pendiente.");
            }

            await _repository.ExpirarReservacion(reservacionId);

            await _logRepository.Registrar(
                LogReservacionRepository.TipoAgenciaExpirada,
                reservacionId, null, agenciaId, null, true, ip, userAgent, null
            );
        }

        /// <summary>
        /// Agrega la lista de pasajeros a una reservacion existente de la agencia.
        /// Valida que cada pasajero tenga numero de pasaporte y que este contenga solo digitos.
        /// </summary>
        public async Task AgregarPasajeros(AgregarPasajerosDTO dto, int agenciaId, string? ip, string? userAgent)
        {
            try
            {
                foreach (var pasajero in dto.Pasajeros)
                {
                    if (string.IsNullOrWhiteSpace(pasajero.Pasaporte))
                        throw new Exception("El número de pasaporte es obligatorio.");
                    if (!pasajero.Pasaporte.All(char.IsDigit))
                        throw new Exception($"El pasaporte de {pasajero.Nombre} {pasajero.Apellido} debe contener solo números.");
                }

                await _repository.AgregarPasajerosAReservacion(dto.ReservacionId, dto.Pasajeros, agenciaId);

                await _logRepository.Registrar(
                    LogReservacionRepository.TipoPasajerosAgenciaAgregados,
                    dto.ReservacionId, null, agenciaId, null, true, ip, userAgent, null
                );
            }
            catch (Exception e)
            {
                await _logRepository.Registrar(
                    LogReservacionRepository.TipoPasajerosAgenciaError,
                    dto.ReservacionId, null, agenciaId, null, false, ip, userAgent, e.Message
                );
                throw;
            }
        }
    }
}
