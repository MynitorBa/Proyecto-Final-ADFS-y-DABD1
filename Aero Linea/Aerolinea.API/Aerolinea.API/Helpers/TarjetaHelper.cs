using System.Text.RegularExpressions;

namespace Aerolinea.API.Helpers
{
    public static class TarjetaHelper
    {
        /// Valida el formato superficial de los datos de tarjeta.
        /// NO verifica si la tarjeta es real ni hace cobros.
        /// Lanza Exception con mensaje descriptivo si algo falla.

        public static void ValidarFormato(
            string numeroTarjeta,
            string nombreTitular,
            string fechaExpiracion,
            string cvv)
        {
            // ── Número de tarjeta: 16 dígitos, puede venir con espacios o guiones ──
            if (string.IsNullOrWhiteSpace(numeroTarjeta))
                throw new Exception("El número de tarjeta es requerido.");

            string soloDigitosNumero = Regex.Replace(numeroTarjeta, @"[\s\-]", "");
            if (!Regex.IsMatch(soloDigitosNumero, @"^\d{16}$"))
                throw new Exception("El número de tarjeta debe tener 16 dígitos.");

            // ── Nombre del titular: solo letras y espacios, mínimo 3 caracteres ──
            if (string.IsNullOrWhiteSpace(nombreTitular))
                throw new Exception("El nombre del titular es requerido.");

            if (!Regex.IsMatch(nombreTitular.Trim(), @"^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]{3,60}$"))
                throw new Exception("El nombre del titular no es válido.");

            // ── Fecha de expiración: formato MM/YY y no vencida ──
            if (string.IsNullOrWhiteSpace(fechaExpiracion))
                throw new Exception("La fecha de expiración es requerida.");

            if (!Regex.IsMatch(fechaExpiracion, @"^(0[1-9]|1[0-2])\/\d{2}$"))
                throw new Exception("La fecha de expiración debe tener el formato MM/YY.");

            var partes = fechaExpiracion.Split('/');
            int mes = int.Parse(partes[0]);
            int anio = int.Parse(partes[1]) + 2000; // YY → YYYY

            var hoy = DateTime.Now;
            // La tarjeta es válida hasta el último día del mes indicado
            var expiracion = new DateTime(anio, mes, DateTime.DaysInMonth(anio, mes));
            if (expiracion < hoy.Date)
                throw new Exception("La tarjeta está vencida.");

            // ── CVV: 3 o 4 dígitos ──
            if (string.IsNullOrWhiteSpace(cvv))
                throw new Exception("El CVV es requerido.");

            if (!Regex.IsMatch(cvv, @"^\d{3,4}$"))
                throw new Exception("El CVV debe tener 3 o 4 dígitos.");
        }

        /// Devuelve el tipo de tarjeta según el número (solo informativo).
        public static string DetectarTipo(string numeroTarjeta)
        {
            string n = Regex.Replace(numeroTarjeta, @"[\s\-]", "");
            if (Regex.IsMatch(n, @"^4")) return "Visa";
            if (Regex.IsMatch(n, @"^5[1-5]")) return "Mastercard";
            if (Regex.IsMatch(n, @"^3[47]")) return "American Express";
            if (Regex.IsMatch(n, @"^6(?:011|5)")) return "Discover";
            return "Desconocido";
        }
    }
}