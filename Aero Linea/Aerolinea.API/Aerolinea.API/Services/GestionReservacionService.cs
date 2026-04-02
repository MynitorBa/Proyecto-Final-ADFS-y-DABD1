using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class GestionReservacionService
    {
        private readonly GestionReservacionRepository _repository;

        public GestionReservacionService(GestionReservacionRepository repository)
        {
            _repository = repository;
        }

        public async Task<List<ReservacionDetalleDTO>> ObtenerMisReservaciones(int usuarioId)
        {
            return await _repository.ObtenerReservacionesPorUsuario(usuarioId);
        }

        public async Task<ReservacionDetalleDTO> ObtenerDetalleReservacion(int reservacionId, int usuarioId)
        {
            var reservacion = await _repository.ObtenerReservacionPorId(reservacionId, usuarioId);
            if (reservacion == null)
                throw new Exception("Reservación no encontrada o no tienes acceso a ella.");
            return reservacion;
        }

        public async Task<ResumenReservacionesDTO> ObtenerResumen(int usuarioId)
        {
            return await _repository.ObtenerResumenReservaciones(usuarioId);
        }

        public async Task CancelarReservacion(int reservacionId, int usuarioId, string motivo)
        {
            await _repository.CancelarReservacion(reservacionId, usuarioId, motivo);
        }

        public async Task EnviarComprobanteEmail(int reservacionId, int usuarioId)
        {
            var reservacion = await ObtenerDetalleReservacion(reservacionId, usuarioId);

            string html = EmailTemplates.CorreoReservacion(reservacion);

            await EmailHelper.Enviar(
                reservacion.UsuarioEmail,
                $"Comprobante de Reservacion {reservacion.NoReservacion} — Broom AirLine",
                html
            );
        }





        //agencias
        public async Task<int> ObtenerUsuarioWebIdDeAgencia(int agenciaId)
        {
            return await _repository.ObtenerUsuarioWebIdDeAgencia(agenciaId);
        }
        public async Task<PuedeCancelarDTO> PuedeCancelar(int reservacionId, int usuarioId)
        {
            return await _repository.PuedeCancelar(reservacionId, usuarioId);
        }
    }
}
