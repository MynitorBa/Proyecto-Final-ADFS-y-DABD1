using System.Security.Cryptography;

namespace Aerolinea.API.Helpers
{
    public static class TokenHelper
    {
        public static string GenerarTokenHash()
        {
            var bytes = RandomNumberGenerator.GetBytes(32);
            var hash = SHA256.HashData(bytes);
            return Convert.ToHexString(hash).ToLower();
        }
    }
}