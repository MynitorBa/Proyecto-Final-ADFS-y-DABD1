using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class HandshakeService
    {
        private readonly AgenciaRepository _agenciaRepository;

        public HandshakeService(AgenciaRepository agenciaRepository)
        {
            _agenciaRepository = agenciaRepository;
        }

        public async Task<HandshakeResponseDTO> ProcesarHandshake(HandshakeRequestDTO dto)
        {
            // 1. Buscar agencia por su URL
            int? agenciaId = await _agenciaRepository.ObtenerAgenciaIdPorURL(dto.UrlAgencia);
            if (agenciaId == null)
                throw new Exception("No se encontró ninguna agencia registrada con esa URL.");

            // 2. Generar token de salida
            string tokenSalida = TokenHelper.GenerarTokenHash();

            // 3. Guardar ambos tokens
            bool guardado = await _agenciaRepository.GuardarTokens(
                agenciaId.Value,
                dto.TokenEntrada,
                tokenSalida
            );

            if (!guardado)
                throw new Exception("No se pudieron guardar los tokens.");

            return new HandshakeResponseDTO { TokenSalida = tokenSalida };
        }
    }
}