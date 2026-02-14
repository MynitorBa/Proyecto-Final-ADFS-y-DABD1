using Aerolinea.API.Data;
using Aerolinea.API.Repositories;
using Aerolinea.API.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddSingleton<DbConnectionFactory>();
builder.Services.AddScoped<UsuarioRepository>();
builder.Services.AddScoped<UsuarioService>();
builder.Services.AddScoped<NacionalidadRepository>();
builder.Services.AddScoped<NacionalidadService>();
<<<<<<< HEAD

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
=======
builder.Services.AddScoped<AuthService>();


>>>>>>> 754cf0650aa2d0cbe8f8a1ecfb8c4c1684b727ba

var app = builder.Build();

// Configure the HTTP request pipeline.
app.UseCors("FrontendPolicy");

// app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();