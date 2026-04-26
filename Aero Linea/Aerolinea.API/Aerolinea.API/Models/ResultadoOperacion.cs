namespace Aerolinea.API.Models
{
    /// <summary>
    /// Encapsula el resultado de una operacion de negocio con su estado y mensaje.
    /// </summary>
    public class ResultadoOperacion
    {
        public bool Exitoso { get; set; }
        public string Mensaje { get; set; } = string.Empty;
        public object? Data { get; set; }

        public static ResultadoOperacion Ok(string mensaje, object? data = null)
            => new() { Exitoso = true, Mensaje = mensaje, Data = data };

        public static ResultadoOperacion Error(string mensaje)
            => new() { Exitoso = false, Mensaje = mensaje };
    }
}
