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
builder.Services.AddScoped<AuthService>();



var app = builder.Build();

// Configure the HTTP request pipeline.

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();
