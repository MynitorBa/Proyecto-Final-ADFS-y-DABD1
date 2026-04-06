using BCrypt.Net;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Clase estatica de utilidad para el manejo seguro de contrasenas mediante BCrypt.
    /// Provee metodos para generar el hash de una contrasena en texto plano
    /// y para verificar si una contrasena ingresada coincide con su hash almacenado.
    /// </summary>
    public static class PasswordHasher
    {
        /// <summary>
        /// Genera un hash seguro de la contrasena proporcionada usando el algoritmo BCrypt.
        /// El hash resultante incluye el salt embebido y es apto para almacenarse en la base de datos.
        /// </summary>
        public static string Hash(string password)
        {
            return BCrypt.Net.BCrypt.HashPassword(password);
        }

        /// <summary>
        /// Verifica si la contrasena en texto plano coincide con el hash previamente almacenado.
        /// Retorna true si la contrasena es correcta, false en caso contrario.
        /// </summary>
        public static bool Verify(string password, string hash)
        {
            return BCrypt.Net.BCrypt.Verify(password, hash);
        }
    }
}
