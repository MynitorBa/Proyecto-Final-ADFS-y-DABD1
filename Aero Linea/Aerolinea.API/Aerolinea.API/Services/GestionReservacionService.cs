using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de gestion de reservaciones. Permite a los usuarios consultar, cancelar
    /// y obtener resumenes de sus reservaciones, ademas de enviar comprobantes por correo
    /// electronico. Tambien expone metodos de apoyo para el flujo de agencias.
    /// </summary>
    public class GestionReservacionService
    {
        private readonly GestionReservacionRepository _repository;
        private readonly EmailHelper _emailHelper;
        private readonly ILogger<GestionReservacionService> _logger;
        private readonly LogReservacionRepository _logRepository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de gestion de reservaciones,
        /// el helper de correo y el logger.
        /// </summary>
        public GestionReservacionService(
            GestionReservacionRepository repository,
            EmailHelper emailHelper,
            ILogger<GestionReservacionService> logger,
            LogReservacionRepository logRepository)
                {
                    _repository = repository;
                    _emailHelper = emailHelper;
                    _logger = logger;
                    _logRepository = logRepository;
        }


        /// <summary>
        /// Retorna la lista de todas las reservaciones del usuario autenticado,
        /// incluyendo detalle de vuelos, pasajeros y estado de cada reservacion.
        /// </summary>
        public async Task<List<ReservacionDetalleDTO>> ObtenerMisReservaciones(int usuarioId)
        {
            return await _repository.ObtenerReservacionesPorUsuario(usuarioId);
        }

        /// <summary>
        /// Retorna el detalle completo de una reservacion especifica perteneciente al usuario.
        /// Lanza excepcion si la reservacion no existe o no pertenece al usuario indicado.
        /// </summary>
        public async Task<ReservacionDetalleDTO> ObtenerDetalleReservacion(int reservacionId, int usuarioId)
        {
            var reservacion = await _repository.ObtenerReservacionPorId(reservacionId, usuarioId);
            if (reservacion == null)
                throw new Exception("Reservación no encontrada o no tienes acceso a ella.");
            return reservacion;
        }

        /// <summary>
        /// Retorna un resumen estadistico de las reservaciones del usuario, incluyendo
        /// totales por estado y monto acumulado de compras.
        /// </summary>
        public async Task<ResumenReservacionesDTO> ObtenerResumen(int usuarioId)
        {
            return await _repository.ObtenerResumenReservaciones(usuarioId);
        }

        /// <summary>
        /// Cancela una reservacion activa del usuario. Registra el motivo de cancelacion
        /// y libera los asientos y boletos asociados segun la logica del repositorio.
        /// Tras cancelar exitosamente envia un correo de aviso al usuario (best-effort:
        /// si el envio falla se registra en el log pero NO se revierte la cancelacion).
        /// </summary>
        public async Task CancelarReservacion(int reservacionId, int usuarioId,
                                        string motivo, string? ip, string? userAgent,
                                        bool esAgencia = false)
        {
            try
            {
                var (noReservacion, nombreUsuario, emailUsuario) =
                    await _repository.CancelarReservacion(reservacionId, usuarioId, motivo);

                await _logRepository.Registrar(
                    esAgencia
                        ? LogReservacionRepository.TipoCancelacionAgenciaExitosa
                        : LogReservacionRepository.TipoCancelacionExitosa,
                    reservacionId,
                    usuarioId,
                    null,
                    null,
                    true,
                    ip,
                    userAgent,
                    motivo
                );

                if (!string.IsNullOrEmpty(emailUsuario))
                {
                    try
                    {
                        _logger.LogInformation(
                            "Enviando correo de cancelacion para reservacion {NoReservacion} a {Email}",
                            noReservacion, emailUsuario);

                        // Intentar obtener detalle completo para enriquecer el correo
                        ReservacionDetalleDTO? detalle = null;
                        try { detalle = await _repository.ObtenerReservacionPorId(reservacionId, usuarioId); }
                        catch { /* Si falla, se envía el correo sin detalle de vuelos */ }

                        string html = EmailTemplates.CorreoCancelacion(nombreUsuario, noReservacion, detalle, motivo);

                        await _emailHelper.Enviar(
                            emailUsuario,
                            $"Broom AirLine - Reservacion {noReservacion} Cancelada",
                            html);

                        _logger.LogInformation(
                            "Correo de cancelacion enviado exitosamente para reservacion {NoReservacion}",
                            noReservacion);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex,
                            "Error al enviar correo de cancelacion para reservacion {NoReservacion} a {Email}",
                            noReservacion, emailUsuario);
                    }
                }
            }
            catch (Exception e)
            {
                await _logRepository.Registrar(
                    esAgencia
                        ? LogReservacionRepository.TipoCancelacionAgenciaFallida
                        : LogReservacionRepository.TipoCancelacionFallida,
                    reservacionId,
                    usuarioId,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.Message
                );
                throw;
            }
        }

        /// <summary>
        /// Envia al correo del usuario un comprobante en formato HTML con el detalle
        /// completo de su reservacion. El envio se realiza de forma asincrona mediante EmailHelper.
        /// </summary>
        public async Task EnviarComprobanteEmail(int reservacionId, int usuarioId)
        {
            var reservacion = await ObtenerDetalleReservacion(reservacionId, usuarioId);

            string html = EmailTemplates.CorreoReservacion(reservacion);

            await _emailHelper.Enviar(
                reservacion.UsuarioEmail,
                $"Comprobante de Reservacion {reservacion.NoReservacion} — Broom AirLine",
                html
            );
        }


        //agencias
        /// <summary>
        /// Retorna el ID del usuario Webservice asociado a la agencia indicada.
        /// Utilizado para determinar el responsable de reservaciones hechas por agencias.
        /// </summary>
        public async Task<int> ObtenerUsuarioWebIdDeAgencia(int agenciaId)
        {
            return await _repository.ObtenerUsuarioWebIdDeAgencia(agenciaId);
        }

        /// <summary>
        /// Verifica si una reservacion puede ser cancelada por el usuario en el momento actual,
        /// considerando politicas de tiempo minimo antes del vuelo y el estado actual de la reservacion.
        /// </summary>
        public async Task<PuedeCancelarDTO> PuedeCancelar(int reservacionId, int usuarioId)
        {
            return await _repository.PuedeCancelar(reservacionId, usuarioId);
        }

        /// <summary>
        /// Edita los datos del pasajero de un boleto propio. Solo permitido si el vuelo
        /// sale en mas de 24 horas y la reservacion esta en estado Pendiente o Confirmada.
        /// </summary>
        public async Task EditarDatosPasajero(int boletoId, int usuarioId, EditarDatosPasajeroDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Nombre))
                throw new ArgumentException("El nombre es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Apellido))
                throw new ArgumentException("El apellido es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Pasaporte))
                throw new ArgumentException("El pasaporte es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Telefono))
                throw new ArgumentException("El teléfono es obligatorio.");

            await _repository.EditarDatosPasajero(boletoId, usuarioId, dto);
        }

        /// <summary>
        /// Retorna la lista de vuelos elegibles para cambiar la reservacion del usuario.
        /// </summary>
        public async Task<List<VueloElegibleDTO>> ObtenerVuelosElegibles(int reservacionId, int usuarioId)
            => await _repository.ObtenerVuelosElegibles(reservacionId, usuarioId);

        /// <summary>
        /// Ejecuta el cambio de vuelo y envia correo de confirmacion al usuario.
        /// </summary>
        public async Task CambiarVuelo(int reservacionId, int nuevoVueloId, int usuarioId)
        {
            var (noRes, noVuelo, fecha, hora, nombre, email, total) =
                await _repository.EjecutarCambioVuelo(reservacionId, nuevoVueloId, usuarioId);

            if (!string.IsNullOrWhiteSpace(email))
            {
                try
                {
                    // Obtener detalle del vuelo para la ruta del correo
                    var detalle = await _repository.ObtenerReservacionPorId(reservacionId, usuarioId);
                    string origenCod = detalle?.Boletos?.FirstOrDefault()?.OrigenCodigo ?? "";
                    string origenCiu = detalle?.Boletos?.FirstOrDefault()?.OrigenCiudad ?? "";
                    string destCod   = detalle?.Boletos?.FirstOrDefault()?.DestinoCodigo ?? "";
                    string destCiu   = detalle?.Boletos?.FirstOrDefault()?.DestinoCiudad ?? "";

                    string html = EmailTemplates.CorreoCambioVuelo(
                        nombre, noRes, origenCod, origenCiu, destCod, destCiu,
                        noVuelo, fecha, hora, total);

                    await _emailHelper.Enviar(email,
                        $"Broom AirLine — Tu reservación {noRes} fue reprogramada",
                        html);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error al enviar correo de cambio de vuelo para reservacion {Id}", reservacionId);
                }
            }
        }
    }
}
