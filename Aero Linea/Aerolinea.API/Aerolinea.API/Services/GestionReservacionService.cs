using Aerolinea.API.DTOs;
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
    }
}
