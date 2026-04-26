namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// Datos de un vuelo activo vinculado a un recurso (avion o tripulante).
    /// Usado para validar y advertir al administrador antes de desactivar
    /// un avion o tripulante con vuelos futuros programados.
    /// HorasRestantes < 0 indica que el calculo se realizo en el pasado (raro en prod).
    /// </summary>
    public record VueloActivoInfoDTO(
        int     Id,
        string  NumeroVuelo,
        string  Origen,
        string  Destino,
        string  Fecha,
        string  HoraSalida,
        double  HorasRestantes,
        string? AvionNombre = null
    );
}
