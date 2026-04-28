using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de handshake entre la aerolinea y agencias externas. Gestiona el intercambio
    /// de tokens de autenticacion para establecer una sesion segura con una agencia registrada
    /// identificada por su URL.
    /// </summary>
    public class HandshakeService
    {
        private readonly AgenciaRepository _agenciaRepository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de agencias.
        /// </summary>
        public HandshakeService(AgenciaRepository agenciaRepository)
        {
            _agenciaRepository = agenciaRepository;
        }

        /// <summary>
        /// Procesa la solicitud de handshake de una agencia externa. Busca la agencia por su URL,
        /// genera un token de salida, guarda ambos tokens y retorna el porcentaje de descuento
        /// configurado para la agencia.
        /// </summary>
        public async Task<HandshakeResponseDTO> ProcesarHandshake(HandshakeRequestDTO dto)
        {
            // 1. Buscar agencia por su URL, obteniendo ID y porcentaje
            var agencia = await _agenciaRepository.ObtenerAgenciaConPorcentajePorURL(dto.UrlAgencia);
            if (agencia == null)
                throw new Exception("No se encontró ninguna agencia registrada con esa URL.");

            // 2. Generar token de salida
            string tokenSalida = TokenHelper.GenerarTokenHash();

            // 3. Guardar ambos tokens
            bool guardado = await _agenciaRepository.GuardarTokens(
                agencia.ID,
                dto.TokenEntrada,
                tokenSalida
            );

            if (!guardado)
                throw new Exception("No se pudieron guardar los tokens.");

            return new HandshakeResponseDTO
            {
                TokenSalida = tokenSalida,
                PorcentajeGanancia = agencia.PorcentajeDescuento
            };
        }
    }
}
