using System.Security.Cryptography;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Clase estatica de utilidad para la generacion de tokens seguros y unicos.
    /// Utiliza el generador de numeros aleatorios criptograficos del sistema
    /// para producir tokens con alta entropia aptos para autenticacion de agencias,
    /// restablecimiento de contrasena u otros flujos que requieran tokens opacos.
    /// </summary>
    public static class TokenHelper
    {
        /// <summary>
        /// Genera un token unico y seguro de 64 caracteres hexadecimales en minusculas.
        /// Internamente obtiene 32 bytes aleatorios criptograficamente seguros,
        /// les aplica SHA-256 y convierte el resultado a cadena hexadecimal.
        /// </summary>
        public static string GenerarTokenHash()
        {
            var bytes = RandomNumberGenerator.GetBytes(32);
            var hash = SHA256.HashData(bytes);
            return Convert.ToHexString(hash).ToLower();
        }
    }
}
