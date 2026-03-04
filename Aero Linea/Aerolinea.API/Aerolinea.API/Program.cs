using Aerolinea.API.Data;
using Aerolinea.API.Repositories;
using Aerolinea.API.Services;
using DinkToPdf;
using DinkToPdf.Contracts;
using Microsoft.AspNetCore.Authentication.Cookies;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddSingleton<DbConnectionFactory>();

// Repositorios
builder.Services.AddScoped<PaisRepository>();
builder.Services.AddScoped<CiudadRepository>();
builder.Services.AddScoped<NacionalidadRepository>();
builder.Services.AddScoped<UsuarioRepository>();
builder.Services.AddScoped<AeropuertoRepository>();
builder.Services.AddScoped<VueloRepository>();
builder.Services.AddScoped<ReservacionRepository>();
builder.Services.AddScoped<ComentarioRepository>();
builder.Services.AddScoped<DownRepository>();
builder.Services.AddScoped<AvionRepository>();
builder.Services.AddScoped<TripulacionRepository>();
builder.Services.AddScoped<GestionReservacionRepository>();
builder.Services.AddScoped<AdminVueloRepository>();
builder.Services.AddScoped<VueloAdminInternoRepository>();
builder.Services.AddScoped<PerfilRepository>();
builder.Services.AddScoped<FacturaRepository>();
builder.Services.AddScoped<MetricasRepository>();


// Servicios
builder.Services.AddScoped<NacionalidadService>();
builder.Services.AddScoped<UsuarioService>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<AeropuertoService>();
builder.Services.AddScoped<VueloService>();
builder.Services.AddScoped<ReservacionService>();
builder.Services.AddScoped<ComentarioService>();
builder.Services.AddScoped<DownService>();
builder.Services.AddScoped<AvionService>();
builder.Services.AddScoped<TripulacionService>();
builder.Services.AddScoped<GestionReservacionService>();
builder.Services.AddScoped<AdminVueloService>();
builder.Services.AddScoped<PerfilService>();
builder.Services.AddScoped<FacturaService>();
builder.Services.AddScoped<MetricasService>();


builder.Services.AddSingleton(typeof(IConverter), new SynchronizedConverter(new PdfTools()));
builder.Services.AddScoped<PdfService>();

// Servicio de búsquedas temporales (Singleton)
builder.Services.AddSingleton<BusquedaTemporalService>();

// *** BACKGROUND SERVICE PARA LIMPIAR RESERVAS EXPIRADAS ***
builder.Services.AddHostedService<ReservasCleanupService>();

// *** AUTENTICACIÓN POR COOKIES (sesión de 8 horas) ***
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.Cookie.Name = "aerolinea_session";
        options.Cookie.HttpOnly = true;                      // No accesible desde JS, más seguro
        options.Cookie.SameSite = SameSiteMode.None;         // Necesario para Svelte en origen distinto
        options.Cookie.SecurePolicy = CookieSecurePolicy.Always; // Requerido cuando SameSite=None
        options.ExpireTimeSpan = TimeSpan.FromHours(8);
        options.SlidingExpiration = true;                    // Renueva las 8h con cada request
        // En lugar de redirigir a /login (comportamiento MVC), devolvemos 401/403 para APIs
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

builder.Services.AddAuthorization();

// *** CORS para Svelte - WithCredentials requiere origen explícito ***
builder.Services.AddCors(options =>
{
    options.AddPolicy("FrontendPolicy", policy =>
    {
        policy.WithOrigins(
                "http://localhost:5173",   // Vite dev server (Svelte por defecto)
                "http://localhost:4173",   // Vite preview
                "http://localhost:3000"    // Por si usan otro puerto
              )
              .AllowAnyMethod()
              .AllowAnyHeader()
              .AllowCredentials();         // Imprescindible para que la cookie viaje con fetch
    });
});

var app = builder.Build();

// *** ORDEN DEL PIPELINE - el orden importa ***
app.UseCors("FrontendPolicy");           // CORS siempre primero
// app.UseHttpsRedirection();
app.UseAuthentication();                 // Primero autenticación
app.UseAuthorization();                  // Luego autorización
app.MapControllers();
app.Run();