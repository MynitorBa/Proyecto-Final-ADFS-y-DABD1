using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de facturacion. Gestiona la logica de negocio para procesar la compra
    /// de una reservacion, validando datos fiscales y el formato de tarjeta de pago
    /// antes de generar la factura y emitir los boletos.
    /// </summary>
    public class FacturaService
    {
        private readonly FacturaRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de facturas.
        /// </summary>
        public FacturaService(FacturaRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Procesa la compra de una reservacion existente. Valida que el NIT y codigo postal
        /// esten presentes, verifica el formato de la tarjeta de pago mediante TarjetaHelper
        /// (sin almacenar datos de la tarjeta) y delega la creacion de factura y boletos al repositorio.
        /// </summary>
        public async Task<CompraRealizadaDTO> ComprarReservacion(
            int reservacionId,
            int usuarioId,
            ComprarReservacionDTO dto)
        {
            // ── Validar datos de factura ──────────────────────────────────────
            if (string.IsNullOrWhiteSpace(dto.NIT))
                throw new Exception("El NIT es requerido. Si no tienes, ingresa 'CF'.");

            if (string.IsNullOrWhiteSpace(dto.CodigoPostal))
                throw new Exception("El código postal es requerido.");

            // ── Validar formato de tarjeta (nunca se guarda) ─────────────────
            TarjetaHelper.ValidarFormato(
                dto.NumeroTarjeta,
                dto.NombreTitular,
                dto.FechaExpiracion,
                dto.CVV);

            // ── Delegar al repositorio ────────────────────────────────────────
            return await _repository.ComprarReservacion(reservacionId, usuarioId, dto);
        }
    }
}
