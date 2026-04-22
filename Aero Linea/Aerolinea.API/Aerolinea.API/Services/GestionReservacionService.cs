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
        private readonly GestionReservacionRepository      _repository;
        private readonly EmailHelper                       _emailHelper;
        private readonly ILogger<GestionReservacionService> _logger;

        /// <summary>
        /// Inicializa el servicio con el repositorio de gestion de reservaciones,
        /// el helper de correo y el logger.
        /// </summary>
        public GestionReservacionService(
            GestionReservacionRepository      repository,
            EmailHelper                       emailHelper,
            ILogger<GestionReservacionService> logger)
        {
            _repository  = repository;
            _emailHelper = emailHelper;
            _logger      = logger;
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
        public async Task CancelarReservacion(int reservacionId, int usuarioId, string motivo)
        {
            // El repositorio retorna los datos del usuario necesarios para el correo
            var (noReservacion, nombreUsuario, emailUsuario) =
                await _repository.CancelarReservacion(reservacionId, usuarioId, motivo);

            // Enviar correo de cancelacion (best-effort)
            if (!string.IsNullOrEmpty(emailUsuario))
            {
                try
                {
                    _logger.LogInformation(
                        "Enviando correo de cancelacion para reservacion {NoReservacion} a {Email}",
                        noReservacion, emailUsuario);

                    string html = EmailTemplates.CorreoCancelacion(nombreUsuario, noReservacion);

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
                    // El fallo del correo nunca revierte la cancelacion ya confirmada
                    _logger.LogError(ex,
                        "Error al enviar correo de cancelacion para reservacion {NoReservacion} a {Email}",
                        noReservacion, emailUsuario);
                }
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
    }
}
