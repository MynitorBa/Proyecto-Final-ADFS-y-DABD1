using Aerolinea.API.Data;
using Aerolinea.API.Repositories;
using Aerolinea.API.Services;

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

// Servicios
builder.Services.AddScoped<NacionalidadService>();
builder.Services.AddScoped<UsuarioService>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<AeropuertoService>();
builder.Services.AddScoped<VueloService>();
builder.Services.AddScoped<ReservacionService>();

// *** BACKGROUND SERVICE PARA LIMPIAR RESERVAS EXPIRADAS ***
builder.Services.AddHostedService<ReservasCleanupService>();

// CORS abierto para desarrollo local
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

// Configure the HTTP request pipeline.
app.UseCors("FrontendPolicy");
// app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();
app.Run();