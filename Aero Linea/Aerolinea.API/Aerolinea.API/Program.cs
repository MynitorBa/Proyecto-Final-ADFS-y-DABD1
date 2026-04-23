using Aerolinea.API.Data;
using Aerolinea.API.Helpers;
using Aerolinea.API.Models.Config;
using Aerolinea.API.Repositories;
using Aerolinea.API.Services;
// using DinkToPdf;
// using DinkToPdf.Contracts;
using Microsoft.AspNetCore.Authentication.Cookies;
using QuestPDF.Infrastructure;
// using System.Runtime.InteropServices;

/// <summary>
/// Inicializa el WebApplication builder con la configuracion del host.
/// </summary>
var builder = WebApplication.CreateBuilder(args);

/// <summary>
/// Registra los controllers de la API para que ASP.NET los descubra automaticamente.
/// </summary>
builder.Services.AddControllers();

/// <summary>
/// Conexion a SQL Server como Singleton: una sola instancia para toda la aplicacion.
/// </summary>
builder.Services.AddSingleton<DbConnectionFactory>();

/// <summary>
/// Repositorios geograficos - Paises: consulta y creacion.
/// </summary>
builder.Services.AddScoped<PaisRepository>();

/// <summary>
/// Repositorios geograficos - Ciudades vinculadas a un pais.
/// </summary>
builder.Services.AddScoped<CiudadRepository>();

/// <summary>
/// Repositorios geograficos - Nacionalidades para asignacion a usuarios.
/// </summary>
builder.Services.AddScoped<NacionalidadRepository>();

/// <summary>
/// Repositorios de usuarios - Registro, consulta y validacion de credenciales.
/// </summary>
builder.Services.AddScoped<IUsuarioRepository, UsuarioRepository>();
builder.Services.AddScoped<UsuarioRepository>();

/// <summary>
/// Repositorios de vuelos - Aeropuertos con codigos IATA.
/// </summary>
builder.Services.AddScoped<AeropuertoRepository>();

/// <summary>
/// Repositorios de vuelos - Busqueda, disponibilidad e itinerarios.
/// </summary>
builder.Services.AddScoped<VueloRepository>();

/// <summary>
/// Repositorios de reservaciones - Creacion, consulta y cancelacion.
/// </summary>
builder.Services.AddScoped<IReservacionRepository, ReservacionRepository>();
builder.Services.AddScoped<ReservacionRepository>();

/// <summary>
/// Repositorios sociales - Comentarios de usuarios sobre vuelos.
/// </summary>
builder.Services.AddScoped<IComentarioRepository, ComentarioRepository>();
builder.Services.AddScoped<ComentarioRepository>();

/// <summary>
/// Repositorios sociales - Votos positivos y negativos en comentarios.
/// </summary>
builder.Services.AddScoped<DownRepository>();

/// <summary>
/// Repositorios de flota - Aviones disponibles y capacidad.
/// </summary>
builder.Services.AddScoped<IAvionRepository, AvionRepository>();
builder.Services.AddScoped<AvionRepository>();

/// <summary>
/// Repositorios de flota - Tripulacion asignada a cada vuelo.
/// </summary>
builder.Services.AddScoped<ITripulacionRepository, TripulacionRepository>();
builder.Services.AddScoped<TripulacionRepository>();

/// <summary>
/// Repositorios administrativos - Gestion de reservaciones desde el panel admin.
/// </summary>
builder.Services.AddScoped<GestionReservacionRepository>();

/// <summary>
/// Repositorios administrativos - Administracion de vuelos desde el panel admin.
/// </summary>
builder.Services.AddScoped<AdminVueloRepository>();

/// <summary>
/// Repositorios administrativos - Operaciones administrativas avanzadas de vuelos.
/// </summary>
builder.Services.AddScoped<VueloAdminInternoRepository>();

/// <summary>
/// Repositorios de usuarios - Datos personales y preferencias del usuario.
/// </summary>
builder.Services.AddScoped<PerfilRepository>();

/// <summary>
/// Repositorios de pagos - Facturas generadas por reservaciones confirmadas.
/// </summary>
builder.Services.AddScoped<FacturaRepository>();

/// <summary>
/// Repositorios administrativos - Estadisticas de vuelos, usuarios y reservaciones.
/// </summary>
builder.Services.AddScoped<MetricasRepository>();

/// <summary>
/// Repositorios de vuelos - Rutas aereas entre aeropuertos.
/// </summary>
builder.Services.AddScoped<IRutaRepository, RutaRepository>();
builder.Services.AddScoped<RutaRepository>();

/// <summary>
/// Repositorios de vuelos - Disponibilidad y asignacion de asientos.
/// </summary>
builder.Services.AddScoped<AsientoRepository>();

/// <summary>
/// Repositorios de agencias - Agencias registradas en el sistema.
/// </summary>
builder.Services.AddScoped<AgenciaRepository>();

/// <summary>
/// Repositorios de agencias - Rutas disponibles para agencias.
/// </summary>
builder.Services.AddScoped<RutaAgenciaRepository>();

/// <summary>
/// Repositorios de hotel - Hoteles aliados registrados en el sistema.
/// </summary>
builder.Services.AddScoped<HotelAliadoRepository>();

/// <summary>
/// Servicio de hotel - Obtener token de descuentos al redireccionar.
/// </summary>
builder.Services.AddScoped<TokenHotelService>();

/// <summary>
/// Health check para monitoreo del estado del servidor.
/// </summary>
builder.Services.AddHealthChecks();

/// <summary>
/// Middleware de autenticacion para validar tokens de agencias externas.
/// </summary>
builder.Services.AddScoped<AgenciaAuthMiddleware>();

/// <summary>
/// Repositorios de agencias - Reservaciones creadas por agencias.
/// </summary>
builder.Services.AddScoped<ReservacionAgenciaRepository>();

/// <summary>
/// Repositorios de agencias - Asientos para operaciones de agencias.
/// </summary>
builder.Services.AddScoped<AsientoAgenciaRepository>();

/// <summary>
/// Repositorios de agencias - Confirmacion de reservaciones de agencias externas.
/// </summary>
builder.Services.AddScoped<ConfirmarReservacionAgenciaRepository>();

/// <summary>
/// Servicios de usuarios - Consulta y asignacion de nacionalidades.
/// </summary>
builder.Services.AddScoped<NacionalidadService>();

/// <summary>
/// Servicios de usuarios - Registro, validacion de campos unicos y perfil.
/// </summary>
builder.Services.AddScoped<IUsuarioService, UsuarioService>();
builder.Services.AddScoped<UsuarioService>();

/// <summary>
/// Servicios de autenticacion - Login con cookies y verificacion de credenciales.
/// </summary>
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<AuthService>();

/// <summary>
/// Servicios de vuelos - Busqueda por codigo IATA y listado de aeropuertos.
/// </summary>
builder.Services.AddScoped<AeropuertoService>();

/// <summary>
/// Servicios de vuelos - Busqueda con escalas, disponibilidad y precios.
/// </summary>
builder.Services.AddScoped<IVueloRepository, VueloRepository>();
builder.Services.AddScoped<IVueloService, VueloService>();
builder.Services.AddScoped<VueloService>();

/// <summary>
/// Servicios de reservaciones - Creacion, consulta por usuario y cancelacion.
/// </summary>
builder.Services.AddScoped<IReservacionService, ReservacionService>();
builder.Services.AddScoped<ReservacionService>();

/// <summary>
/// Servicios sociales - Agregar, listar y validar resenas de vuelos.
/// </summary>
builder.Services.AddScoped<IComentarioService, ComentarioService>();
builder.Services.AddScoped<ComentarioService>();

/// <summary>
/// Servicios sociales - Agregar, actualizar y eliminar votos en comentarios.
/// </summary>
builder.Services.AddScoped<DownService>();

/// <summary>
/// Servicios de flota - Gestion de aviones y capacidad.
/// </summary>
builder.Services.AddScoped<IAvionService, AvionService>();
builder.Services.AddScoped<AvionService>();

/// <summary>
/// Servicios de flota - Asignacion y consulta de tripulacion por vuelo.
/// </summary>
builder.Services.AddScoped<ITripulacionService, TripulacionService>();
builder.Services.AddScoped<TripulacionService>();

/// <summary>
/// Servicios administrativos - Gestion de reservaciones para el panel admin.
/// </summary>
builder.Services.AddScoped<GestionReservacionService>();

/// <summary>
/// Servicios administrativos - Crear, editar y cancelar vuelos.
/// </summary>
builder.Services.AddScoped<AdminVueloService>();

/// <summary>
/// Servicios de usuarios - Consulta y actualizacion de datos personales del perfil.
/// </summary>
builder.Services.AddScoped<PerfilService>();

/// <summary>
/// Servicios de pagos - Generacion de facturas. PDF deshabilitado temporalmente
/// mientras la libreria nativa wkhtmltopdf no este disponible en el entorno.
/// </summary>
builder.Services.AddScoped<PdfService>();

/// <summary>
/// Servicios de pagos - Generacion y consulta de facturas por reservacion.
/// </summary>
builder.Services.AddScoped<FacturaService>();

/// <summary>
/// Servicios administrativos - Estadisticas del sistema para el dashboard.
/// </summary>
builder.Services.AddScoped<MetricasService>();

/// <summary>
/// Servicios de vuelos - Consulta y administracion de rutas aereas.
/// </summary>
builder.Services.AddScoped<IRutaService, RutaService>();
builder.Services.AddScoped<RutaService>();

/// <summary>
/// Servicios de vuelos - Seleccion y disponibilidad de asientos por vuelo.
/// </summary>
builder.Services.AddScoped<AsientoService>();

/// <summary>
/// Servicios de agencias - Registro, handshake y gestion de estado de agencias.
/// </summary>
builder.Services.AddScoped<AgenciaService>();

/// <summary>
/// Servicios de agencias - Intercambio de tokens con agencias externas (handshake).
/// </summary>
builder.Services.AddScoped<HandshakeService>();

/// <summary>
/// Servicios de agencias - Validacion de tokens de agencias en cada request.
/// </summary>
builder.Services.AddScoped<AgenciaAuthMiddleware>();

/// <summary>
/// Servicios de agencias - Rutas disponibles para agencias via REST.
/// </summary>
builder.Services.AddScoped<RutaAgenciaService>();

/// <summary>
/// Servicios de agencias - Busqueda de vuelos para agencias via REST.
/// </summary>
builder.Services.AddScoped<VueloAgenciaService>();

/// <summary>
/// Servicios de agencias - Crear y consultar reservaciones via REST.
/// </summary>
builder.Services.AddScoped<ReservacionAgenciaService>();

/// <summary>
/// Servicios de agencias - Disponibilidad de asientos via REST.
/// </summary>
builder.Services.AddScoped<AsientoAgenciaService>();

/// <summary>
/// Servicios de agencias - Confirmar reservaciones creadas por agencias externas.
/// </summary>
builder.Services.AddScoped<ConfirmarReservacionAgenciaService>();

/// <summary>
/// Servicios de Hotel Aliado - Consulta y agregacion de resultados de hoteles aliados.
/// </summary>
builder.Services.AddScoped<HotelAliadoService>();

/// <summary>
/// IHttpClientFactory - Habilita llamadas HTTP salientes hacia sistemas externos
/// (hotel aliado). Requerido por HandshakeHotelService para el proceso de handshake.
/// </summary>
builder.Services.AddHttpClient();

/// <summary>
/// Servicios de Hotel Aliado - Inicia el handshake de autenticacion con un hotel aliado
/// y guarda el token de sesion resultante en HotelAliado.TokenHASH.
/// </summary>
builder.Services.AddScoped<HandshakeHotelService>();

/// <summary>
/// Admin Reservaciones - Repositorio, notificador de agencia y servicio para gestion
/// administrativa de reservaciones agrupadas por vuelo. El notificador llama al sistema
/// externo de la agencia via POST /api/proveedores-ext/detalles/{id}/cancelar cuando
/// el admin cancela una reservacion creada por un usuario webservice de agencia.
/// </summary>
builder.Services.AddScoped<AdminReservacionesRepository>();
builder.Services.AddScoped<AgenciaNotificadorExternoService>();
builder.Services.AddScoped<AdminReservacionesService>();

// --- wkhtmltopdf comentado: DLL nativa no disponible en este entorno ---
// var architectureFolder = RuntimeInformation.IsOSPlatform(OSPlatform.Linux) ? "linux-x64" : "win-x64";
// var wkHtmlPath = Path.Combine(Directory.GetCurrentDirectory(), "native", architectureFolder, ...);
// var context = new CustomAssemblyLoadContext();
// context.LoadUnmanagedLibrary(wkHtmlPath);
// builder.Services.AddSingleton(typeof(IConverter), new SynchronizedConverter(new PdfTools()));

/// <summary>
/// Configuracion SMTP leida desde appsettings.json (seccion EmailSettings).
/// En Docker sobreescribir SenderPassword via variable de entorno:
///   EmailSettings__SenderPassword=tu_app_password
/// </summary>
builder.Services.Configure<EmailSettings>(
    builder.Configuration.GetSection("EmailSettings"));

/// <summary>
/// Helper de correo como Singleton: una sola instancia comparte la configuracion SMTP.
/// </summary>
builder.Services.AddSingleton<EmailHelper>();

/// <summary>
/// Almacena busquedas temporales en memoria - no persiste en base de datos.
/// </summary>
builder.Services.AddSingleton<BusquedaTemporalService>();

/// <summary>
/// Background service que limpia reservaciones que excedieron el tiempo de pago.
/// </summary>
builder.Services.AddHostedService<ReservasCleanupService>();

/// <summary>
/// Autenticacion por cookies - sesion de 8 horas con sliding expiration.
/// Retorna 401 si no autenticado y 403 si no tiene permisos.
/// </summary>
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.Cookie.Name = builder.Configuration["COOKIE_NAME"] ?? "aerolinea_session";
        options.Cookie.HttpOnly = true;
        options.Cookie.SameSite = SameSiteMode.Lax;
        options.Cookie.SecurePolicy = CookieSecurePolicy.SameAsRequest;
        options.ExpireTimeSpan = TimeSpan.FromHours(8);
        options.SlidingExpiration = true;
        options.Events.OnRedirectToLogin = ctx =>
        {
            ctx.Response.StatusCode = 401;
            return Task.CompletedTask;
        };
        options.Events.OnRedirectToAccessDenied = ctx =>
        {
            ctx.Response.StatusCode = 403;
            return Task.CompletedTask;
        };
    });

/// <summary>
/// Habilita autorizacion basada en roles: admin, usuario y webservice.
/// </summary>
builder.Services.AddAuthorization();

/// <summary>
/// CORS para permitir peticiones del frontend Svelte.
/// Acepta origenes configurados en appsettings.json o los puertos por defecto de Vite.
/// </summary>
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        var corsOrigins = builder.Configuration
            .GetSection("Cors:Origins")
            .Get<string[]>()
            ?? new[] { "http://localhost:5173", "http://localhost:4173" };

        policy
            .WithOrigins(corsOrigins)
            .AllowAnyHeader()
            .AllowAnyMethod()
            .AllowCredentials();
    });
});

/// <summary>
/// Construye la aplicacion con toda la configuracion registrada.
/// </summary>
var app = builder.Build();

/// <summary>
/// QuestPDF Community License - gratuita para proyectos no comerciales.
/// Debe establecerse antes de cualquier generacion de PDF.
/// </summary>
QuestPDF.Settings.License = LicenseType.Community;

/// <summary>
/// Pipeline de middleware - el orden importa.
/// Primero CORS para que las preflight requests pasen.
/// </summary>
app.UseCors();

/// <summary>
/// Autenticacion: lee la cookie de sesion y valida la identidad del usuario.
/// </summary>
app.UseAuthentication();

/// <summary>
/// Health check endpoint - GET /health retorna 200 OK si el servidor esta activo.
/// </summary>
app.MapHealthChecks("/health");

/// <summary>
/// Autorizacion: valida roles despues de confirmar la identidad.
/// </summary>
app.UseAuthorization();

/// <summary>
/// Mapea todos los controllers de la API descubiertos automaticamente.
/// </summary>
app.MapControllers();

/// <summary>
/// Arranca el servidor y comienza a escuchar peticiones HTTP.
/// </summary>
app.Run();