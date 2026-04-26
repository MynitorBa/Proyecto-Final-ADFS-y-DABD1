using Aerolinea.API.Data;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio en segundo plano que envia cada semana un correo con la oferta
    /// de vuelo mas barata (directo y con escala) a todos los usuarios que
    /// activaron "Deseo recibir ofertas". La oferta se personaliza segun el pais
    /// de origen registrado por el usuario en el momento del registro.
    /// </summary>
    public class OfertasSemanalService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<OfertasSemanalService> _logger;
        private DateTime _ultimoEnvio = DateTime.MinValue;

        public OfertasSemanalService(
            IServiceProvider serviceProvider,
            ILogger<OfertasSemanalService> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("Servicio de ofertas semanales iniciado.");

            // Esperar 2 minutos al inicio para que el resto de la app cargue
            await Task.Delay(TimeSpan.FromMinutes(2), stoppingToken);

            while (!stoppingToken.IsCancellationRequested)
            {
                // Ejecutar si han pasado 7 dias desde el ultimo envio
                if (DateTime.UtcNow - _ultimoEnvio >= TimeSpan.FromDays(7))
                {
                    try
                    {
                        await EnviarOfertas();
                        _ultimoEnvio = DateTime.UtcNow;
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError(ex, "Error al enviar ofertas semanales.");
                    }
                }

                await Task.Delay(TimeSpan.FromHours(1), stoppingToken);
            }
        }

        private async Task EnviarOfertas()
        {
            using var scope = _serviceProvider.CreateScope();
            var usuarioRepo = scope.ServiceProvider.GetRequiredService<UsuarioRepository>();
            var emailHelper = scope.ServiceProvider.GetRequiredService<EmailHelper>();
            var dbFactory   = scope.ServiceProvider.GetRequiredService<DbConnectionFactory>();

            var usuarios = await usuarioRepo.ObtenerUsuariosParaOfertas();
            if (usuarios.Count == 0)
            {
                _logger.LogInformation("No hay usuarios suscritos a ofertas semanales.");
                return;
            }

            _logger.LogInformation("Enviando ofertas semanales a {Count} usuario(s).", usuarios.Count);

            foreach (var (id, nombre, correo, pais) in usuarios)
            {
                try
                {
                    var directo  = await BuscarVueloMasBarato(dbFactory, pais, conEscala: false);
                    var conEscala = await BuscarVueloMasBarato(dbFactory, pais, conEscala: true);

                    if (directo == null && conEscala == null)
                    {
                        _logger.LogWarning("No se encontraron vuelos disponibles para enviar oferta a {Correo}.", correo);
                        continue;
                    }

                    string html = EmailTemplates.CorreoOfertaSemanal(nombre, pais, directo, conEscala);
                    await emailHelper.Enviar(correo, "Broom AirLine — Tu oferta de vuelo de esta semana", html);
                    _logger.LogInformation("Oferta semanal enviada a {Correo}.", correo);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error al enviar oferta a {Correo}.", correo);
                }
            }
        }

        /// <summary>
        /// Busca el vuelo mas barato (directo o con una escala) desde aeropuertos
        /// en el pais del usuario. Si no hay vuelos desde ese pais, busca el mas
        /// barato a nivel global como fallback.
        /// </summary>
        private async Task<OfertaVueloInfo?> BuscarVueloMasBarato(
            DbConnectionFactory dbFactory, string pais, bool conEscala)
        {
            using var connection = dbFactory.CreateConnection();
            await connection.OpenAsync();

            if (conEscala)
            {
                // Vuelo mas barato con exactamente una escala (dos segmentos encadenados)
                var sql = @"
                    SELECT TOP 1
                        v1.NumeroVuelo, v1.Fecha, v1.HoraSalida, v1.HoraLlegada, v1.PrecioTurista,
                        v2.NumeroVuelo, v2.Fecha, v2.HoraSalida, v2.HoraLlegada, v2.PrecioTurista,
                        (v1.PrecioTurista + v2.PrecioTurista) as TotalPrecio,
                        ao.Codigo, co.Nombre, aesc.Codigo, cesc.Nombre, ad2.Codigo, cd2.Nombre
                    FROM Vuelo v1
                    INNER JOIN Ruta r1        ON v1.RutaID    = r1.ID
                    INNER JOIN Aeropuerto ao  ON r1.OrigenID  = ao.ID
                    INNER JOIN Ciudad     co  ON ao.CiudadID  = co.ID
                    INNER JOIN Pais       po  ON co.PaisID    = po.ID
                    INNER JOIN Aeropuerto aesc ON r1.DestinoID = aesc.ID
                    INNER JOIN Ciudad     cesc ON aesc.CiudadID = cesc.ID
                    INNER JOIN Ruta       r2   ON r2.OrigenID  = aesc.ID
                    INNER JOIN Vuelo      v2   ON v2.RutaID    = r2.ID
                    INNER JOIN Aeropuerto ad2  ON r2.DestinoID = ad2.ID
                    INNER JOIN Ciudad     cd2  ON ad2.CiudadID = cd2.ID
                    WHERE v1.Fecha > GETDATE()
                      AND v2.Fecha > GETDATE()
                      AND (v2.Fecha > v1.Fecha OR (v2.Fecha = v1.Fecha AND v2.HoraSalida > v1.HoraLlegada))
                      AND v1.BoletosTurista > 0
                      AND v2.BoletosTurista > 0
                      AND (@Pais = '' OR po.Nombre LIKE '%' + @Pais + '%')
                    ORDER BY TotalPrecio ASC";

                using var cmd = new SqlCommand(sql, connection);
                cmd.Parameters.AddWithValue("@Pais", pais ?? "");
                using var reader = await cmd.ExecuteReaderAsync();
                if (await reader.ReadAsync())
                {
                    return new OfertaVueloInfo
                    {
                        NumeroVuelo    = reader.IsDBNull(0)  ? "" : reader.GetString(0),
                        Fecha          = reader.IsDBNull(1)  ? DateTime.MinValue : reader.GetDateTime(1),
                        HoraSalida     = reader.IsDBNull(2)  ? TimeSpan.Zero : reader.GetTimeSpan(2),
                        HoraLlegada    = reader.IsDBNull(3)  ? TimeSpan.Zero : reader.GetTimeSpan(3),
                        PrecioTurista  = reader.IsDBNull(4)  ? 0 : reader.GetDecimal(4),
                        NumeroVuelo2   = reader.IsDBNull(5)  ? null : reader.GetString(5),
                        Fecha2         = reader.IsDBNull(6)  ? null : reader.GetDateTime(6),
                        HoraSalida2    = reader.IsDBNull(7)  ? null : reader.GetTimeSpan(7),
                        HoraLlegada2   = reader.IsDBNull(8)  ? null : reader.GetTimeSpan(8),
                        PrecioTurista2 = reader.IsDBNull(9)  ? null : reader.GetDecimal(9),
                        TotalPrecio    = reader.IsDBNull(10) ? 0 : reader.GetDecimal(10),
                        OrigenCodigo   = reader.IsDBNull(11) ? "" : reader.GetString(11),
                        OrigenCiudad   = reader.IsDBNull(12) ? "" : reader.GetString(12),
                        EscalaCodigo   = reader.IsDBNull(13) ? null : reader.GetString(13),
                        EscalaCiudad   = reader.IsDBNull(14) ? null : reader.GetString(14),
                        DestinoCodigo  = reader.IsDBNull(15) ? "" : reader.GetString(15),
                        DestinoCiudad  = reader.IsDBNull(16) ? "" : reader.GetString(16),
                        EsConEscala    = true
                    };
                }
                return null;
            }
            else
            {
                // Vuelo directo mas barato
                var sql = @"
                    SELECT TOP 1
                        v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada, v.PrecioTurista,
                        ao.Codigo, co.Nombre, ad.Codigo, cd.Nombre
                    FROM Vuelo v
                    INNER JOIN Ruta       r  ON v.RutaID   = r.ID
                    INNER JOIN Aeropuerto ao ON r.OrigenID = ao.ID
                    INNER JOIN Ciudad     co ON ao.CiudadID = co.ID
                    INNER JOIN Pais       po ON co.PaisID   = po.ID
                    INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                    INNER JOIN Ciudad     cd ON ad.CiudadID = cd.ID
                    WHERE v.Fecha > GETDATE()
                      AND v.BoletosTurista > 0
                      AND (@Pais = '' OR po.Nombre LIKE '%' + @Pais + '%')
                    ORDER BY v.PrecioTurista ASC";

                using var cmd = new SqlCommand(sql, connection);
                cmd.Parameters.AddWithValue("@Pais", pais ?? "");
                using var reader = await cmd.ExecuteReaderAsync();
                if (await reader.ReadAsync())
                {
                    return new OfertaVueloInfo
                    {
                        NumeroVuelo   = reader.IsDBNull(0) ? "" : reader.GetString(0),
                        Fecha         = reader.IsDBNull(1) ? DateTime.MinValue : reader.GetDateTime(1),
                        HoraSalida    = reader.IsDBNull(2) ? TimeSpan.Zero : reader.GetTimeSpan(2),
                        HoraLlegada   = reader.IsDBNull(3) ? TimeSpan.Zero : reader.GetTimeSpan(3),
                        PrecioTurista = reader.IsDBNull(4) ? 0 : reader.GetDecimal(4),
                        TotalPrecio   = reader.IsDBNull(4) ? 0 : reader.GetDecimal(4),
                        OrigenCodigo  = reader.IsDBNull(5) ? "" : reader.GetString(5),
                        OrigenCiudad  = reader.IsDBNull(6) ? "" : reader.GetString(6),
                        DestinoCodigo = reader.IsDBNull(7) ? "" : reader.GetString(7),
                        DestinoCiudad = reader.IsDBNull(8) ? "" : reader.GetString(8),
                        EsConEscala   = false
                    };
                }
                return null;
            }
        }
    }

    /// <summary>
    /// Datos de un vuelo (directo o con escala) para incluir en el correo de oferta semanal.
    /// </summary>
    public class OfertaVueloInfo
    {
        public string NumeroVuelo   { get; set; } = "";
        public DateTime Fecha       { get; set; }
        public TimeSpan HoraSalida  { get; set; }
        public TimeSpan HoraLlegada { get; set; }
        public decimal PrecioTurista { get; set; }

        // Segundo segmento (solo cuando EsConEscala = true)
        public string? NumeroVuelo2   { get; set; }
        public DateTime? Fecha2       { get; set; }
        public TimeSpan? HoraSalida2  { get; set; }
        public TimeSpan? HoraLlegada2 { get; set; }
        public decimal? PrecioTurista2 { get; set; }

        public decimal  TotalPrecio   { get; set; }
        public string   OrigenCodigo  { get; set; } = "";
        public string   OrigenCiudad  { get; set; } = "";
        public string?  EscalaCodigo  { get; set; }
        public string?  EscalaCiudad  { get; set; }
        public string   DestinoCodigo { get; set; } = "";
        public string   DestinoCiudad { get; set; } = "";
        public bool     EsConEscala   { get; set; }
    }
}
