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
        /// genera un token de salida y guarda ambos tokens (entrada y salida) en la base de datos.
        /// Retorna el token de salida que la agencia debe usar en solicitudes posteriores.
        /// </summary>
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
