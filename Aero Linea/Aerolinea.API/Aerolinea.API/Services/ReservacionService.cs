using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class ReservacionService
    {
        private readonly ReservacionRepository _repository;

        public ReservacionService(ReservacionRepository repository)
        {
            _repository = repository;
        }

        public async Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto, int? usuarioId)
        {
            if (usuarioId == null)
                throw new Exception("Debes iniciar sesión para crear una reservación.");

            return await _repository.CrearReservacion(usuarioId.Value, dto.Vuelos);
        }

        public async Task AgregarPasajeros(AgregarPasajerosDTO dto)
        {
            // Validar que el pasaporte de cada pasajero contenga solo números
            foreach (var pasajero in dto.Pasajeros)
            {
                if (string.IsNullOrWhiteSpace(pasajero.Pasaporte))
                    throw new Exception("El número de pasaporte es obligatorio.");

                if (!pasajero.Pasaporte.All(char.IsDigit))
                    throw new Exception($"El pasaporte de {pasajero.Nombre} {pasajero.Apellido} debe contener solo números.");
            }

            await _repository.AgregarPasajerosAReservacion(dto.ReservacionId, dto.Pasajeros);
        }
    }
}