using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class FacturaService
    {
        private readonly FacturaRepository _repository;

        public FacturaService(FacturaRepository repository)
        {
            _repository = repository;
        }

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