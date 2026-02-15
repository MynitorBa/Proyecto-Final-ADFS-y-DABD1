using Aerolinea.API.Data;
using Aerolinea.API.Repositories;
using Aerolinea.API.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddSingleton<DbConnectionFactory>();

// Repositories
builder.Services.AddScoped<PaisRepository>();
builder.Services.AddScoped<CiudadRepository>();
builder.Services.AddScoped<NacionalidadRepository>();
builder.Services.AddScoped<UsuarioRepository>();
builder.Services.AddScoped<AeropuertoRepository>();
builder.Services.AddScoped<VueloRepository>();

// Services
builder.Services.AddScoped<NacionalidadService>();
builder.Services.AddScoped<UsuarioService>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<AeropuertoService>();
builder.Services.AddScoped<VueloService>();

// Servicio de búsquedas temporales (Singleton)
builder.Services.AddSingleton<BusquedaTemporalService>();

// CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("FrontendPolicy", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

app.UseCors("FrontendPolicy");
app.UseAuthorization();
app.MapControllers();
app.Run();