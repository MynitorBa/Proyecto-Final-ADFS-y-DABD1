using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de confirmacion de reservaciones para agencias. Gestiona la logica de negocio
    /// para confirmar una reservacion pendiente de una agencia, validando los datos fiscales
    /// requeridos antes de proceder con el pago y emision de boletos.
    /// </summary>
    public class ConfirmarReservacionAgenciaService
    {
        private readonly ConfirmarReservacionAgenciaRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de confirmacion de reservaciones de agencia.
        /// </summary>
        public ConfirmarReservacionAgenciaService(ConfirmarReservacionAgenciaRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Confirma una reservacion pendiente de una agencia aplicando los datos fiscales del DTO.
        /// Valida que el NIT y el codigo postal no esten vacios antes de delegar al repositorio.
        /// Retorna los datos de confirmacion con la informacion de la factura generada.
        /// </summary>
        public async Task<ConfirmacionAgenciaDTO> ConfirmarReservacion(
            int reservacionId,
            int agenciaId,
            ConfirmarReservacionAgenciaDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.NIT))
                throw new Exception("El NIT es requerido. Si no tienes, ingresa 'CF'.");

            if (string.IsNullOrWhiteSpace(dto.CodigoPostal))
                throw new Exception("El código postal es requerido.");

            return await _repository.ConfirmarReservacion(reservacionId, agenciaId, dto);
        }
    }
}
