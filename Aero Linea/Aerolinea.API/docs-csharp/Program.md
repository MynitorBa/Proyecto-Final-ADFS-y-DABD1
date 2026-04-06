# Program

## Program.cs

> Punto de entrada de la aplicacion. Configura el servidor, registra middleware, controllers y servicios.

```csharp
var builder = WebApplication.CreateBuilder(args);
```

Inicializa el WebApplication builder con la configuracion del host.

```csharp
builder.Services.AddControllers();
```

Registra los controllers de la API para que ASP.NET los descubra automaticamente.

```csharp
builder.Services.AddSingleton<DbConnectionFactory>();
```

Conexion a Oracle como Singleton: una sola instancia para toda la aplicacion.

```csharp
builder.Services.AddScoped<PaisRepository>();
```

Repositorios geograficos - Paises: consulta y creacion.

```csharp
builder.Services.AddScoped<CiudadRepository>();
```

Repositorios geograficos - Ciudades vinculadas a un pais.

```csharp
builder.Services.AddScoped<NacionalidadRepository>();
```

Repositorios geograficos - Nacionalidades para asignacion a usuarios.

```csharp
builder.Services.AddScoped<UsuarioRepository>();
```

Repositorios de usuarios - Registro, consulta y validacion de credenciales.

```csharp
builder.Services.AddScoped<AeropuertoRepository>();
```

Repositorios de vuelos - Aeropuertos con codigos IATA.

```csharp
builder.Services.AddScoped<VueloRepository>();
```

Repositorios de vuelos - Busqueda, disponibilidad e itinerarios.

```csharp
builder.Services.AddScoped<ReservacionRepository>();
```

Repositorios de reservaciones - Creacion, consulta y cancelacion.

```csharp
builder.Services.AddScoped<ComentarioRepository>();
```

Repositorios sociales - Comentarios de usuarios sobre vuelos.

```csharp
builder.Services.AddScoped<DownRepository>();
```

Repositorios sociales - Votos positivos y negativos en comentarios.

```csharp
builder.Services.AddScoped<AvionRepository>();
```

Repositorios de flota - Aviones disponibles y capacidad.

```csharp
builder.Services.AddScoped<TripulacionRepository>();
```

Repositorios de flota - Tripulacion asignada a cada vuelo.

```csharp
builder.Services.AddScoped<GestionReservacionRepository>();
```

Repositorios administrativos - Gestion de reservaciones desde el panel admin.

```csharp
builder.Services.AddScoped<AdminVueloRepository>();
```

Repositorios administrativos - Administracion de vuelos desde el panel admin.

```csharp
builder.Services.AddScoped<VueloAdminInternoRepository>();
```

Repositorios administrativos - Operaciones administrativas avanzadas de vuelos.

```csharp
builder.Services.AddScoped<PerfilRepository>();
```

Repositorios de usuarios - Datos personales y preferencias del usuario.

```csharp
builder.Services.AddScoped<FacturaRepository>();
```

Repositorios de pagos - Facturas generadas por reservaciones confirmadas.

```csharp
builder.Services.AddScoped<MetricasRepository>();
```

Repositorios administrativos - Estadisticas de vuelos, usuarios y reservaciones.

```csharp
builder.Services.AddScoped<RutaRepository>();
```

Repositorios de vuelos - Rutas aereas entre aeropuertos.

```csharp
builder.Services.AddScoped<AsientoRepository>();
```

Repositorios de vuelos - Disponibilidad y asignacion de asientos.

```csharp
builder.Services.AddScoped<AgenciaRepository>();
```

Repositorios de agencias - Agencias registradas en el sistema.

```csharp
builder.Services.AddScoped<RutaAgenciaRepository>();
```

Repositorios de agencias - Rutas disponibles para agencias.

```csharp
builder.Services.AddHealthChecks();
```

Health check para monitoreo del estado del servidor.

```csharp
builder.Services.AddScoped<AgenciaAuthMiddleware>();
```

Middleware de autenticacion para validar tokens de agencias externas.

```csharp
builder.Services.AddScoped<ReservacionAgenciaRepository>();
```

Repositorios de agencias - Reservaciones creadas por agencias.

```csharp
builder.Services.AddScoped<AsientoAgenciaRepository>();
```

Repositorios de agencias - Asientos para operaciones de agencias.

```csharp
builder.Services.AddScoped<ConfirmarReservacionAgenciaRepository>();
```

Repositorios de agencias - Confirmacion de reservaciones de agencias externas.

```csharp
builder.Services.AddScoped<NacionalidadService>();
```

Servicios de usuarios - Consulta y asignacion de nacionalidades.

```csharp
builder.Services.AddScoped<UsuarioService>();
```

Servicios de usuarios - Registro, validacion de campos unicos y perfil.

```csharp
builder.Services.AddScoped<AuthService>();
```

Servicios de autenticacion - Login con cookies y verificacion de credenciales.

```csharp
builder.Services.AddScoped<AeropuertoService>();
```

Servicios de vuelos - Busqueda por codigo IATA y listado de aeropuertos.

```csharp
builder.Services.AddScoped<VueloService>();
```

Servicios de vuelos - Busqueda con escalas, disponibilidad y precios.

```csharp
builder.Services.AddScoped<ReservacionService>();
```

Servicios de reservaciones - Creacion, consulta por usuario y cancelacion.

```csharp
builder.Services.AddScoped<ComentarioService>();
```

Servicios sociales - Agregar, listar y validar resenas de vuelos.

```csharp
builder.Services.AddScoped<DownService>();
```

Servicios sociales - Agregar, actualizar y eliminar votos en comentarios.

```csharp
builder.Services.AddScoped<AvionService>();
```

Servicios de flota - Gestion de aviones y capacidad.

```csharp
builder.Services.AddScoped<TripulacionService>();
```

Servicios de flota - Asignacion y consulta de tripulacion por vuelo.

```csharp
builder.Services.AddScoped<GestionReservacionService>();
```

Servicios administrativos - Gestion de reservaciones para el panel admin.

```csharp
builder.Services.AddScoped<AdminVueloService>();
```

Servicios administrativos - Crear, editar y cancelar vuelos.

```csharp
builder.Services.AddScoped<PerfilService>();
```

Servicios de usuarios - Consulta y actualizacion de datos personales del perfil.

```csharp
builder.Services.AddScoped<FacturaService>();
```

Servicios de pagos - Generacion de facturas en PDF con DinkToPdf.

```csharp
builder.Services.AddScoped<MetricasService>();
```

Servicios administrativos - Estadisticas del sistema para el dashboard.

```csharp
builder.Services.AddScoped<RutaService>();
```

Servicios de vuelos - Consulta y administracion de rutas aereas.

```csharp
builder.Services.AddScoped<AsientoService>();
```

Servicios de vuelos - Seleccion y disponibilidad de asientos por vuelo.

```csharp
builder.Services.AddScoped<AgenciaService>();
```

Servicios de agencias - Registro, handshake y gestion de estado de agencias.

```csharp
builder.Services.AddScoped<HandshakeService>();
```

Servicios de agencias - Intercambio de tokens con agencias externas.

```csharp
builder.Services.AddScoped<AgenciaAuthMiddleware>();
```

Servicios de agencias - Validacion de tokens de agencias en cada request.

```csharp
builder.Services.AddScoped<RutaAgenciaService>();
```

Servicios de agencias - Rutas disponibles para agencias via REST.

```csharp
builder.Services.AddScoped<VueloAgenciaService>();
```

Servicios de agencias - Busqueda de vuelos para agencias via REST.

```csharp
builder.Services.AddScoped<ReservacionAgenciaService>();
```

Servicios de agencias - Crear y consultar reservaciones via REST.

```csharp
builder.Services.AddScoped<AsientoAgenciaService>();
```

Servicios de agencias - Disponibilidad de asientos via REST.

```csharp
builder.Services.AddScoped<ConfirmarReservacionAgenciaService>();
```

Servicios de agencias - Confirmar reservaciones creadas por agencias externas.

```csharp
var architectureFolder = RuntimeInformation.IsOSPlatform(OSPlatform.Linux)
```

Carpeta de la libreria nativa segun el sistema operativo: linux-x64 o win-x64.

```csharp
var wkHtmlPath = Path.Combine(
```

Ruta absoluta a la libreria nativa wkhtmltopdf para generacion de PDF.

```csharp
var context = new CustomAssemblyLoadContext();
```

Carga la libreria nativa wkhtmltopdf en un contexto de ensamblaje personalizado.

```csharp
context.LoadUnmanagedLibrary(wkHtmlPath);
```

Carga la libreria nativa en memoria para habilitar la conversion de HTML a PDF.

```csharp
builder.Services.AddSingleton(typeof(IConverter), new SynchronizedConverter(new PdfTools()));
```

Convertidor HTML a PDF como Singleton - una sola instancia, thread-safe.

```csharp
builder.Services.AddScoped<PdfService>();
```

Servicio de generacion de PDF para facturas.

```csharp
builder.Services.AddSingleton<BusquedaTemporalService>();
```

Almacena busquedas temporales en memoria - no persiste en base de datos.

```csharp
builder.Services.AddHostedService<ReservasCleanupService>();
```

Background service que limpia reservaciones que excedieron el tiempo de pago.

```csharp
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
```

Autenticacion por cookies - sesion de 8 horas con sliding expiration. Retorna 401 si no autenticado y 403 si no tiene permisos.

```csharp
builder.Services.AddAuthorization();
```

Habilita autorizacion basada en roles: admin, usuario y webservice.

```csharp
builder.Services.AddCors(options =>
```

CORS para permitir peticiones del frontend Svelte. Acepta origenes configurados en appsettings.json o los puertos por defecto de Vite.

```csharp
var corsOrigins = builder.Configuration
```

Lee los origenes permitidos desde appsettings.json o usa los puertos de Vite por defecto.

```csharp
var app = builder.Build();
```

Construye la aplicacion con toda la configuracion registrada.

```csharp
app.UseCors();
```

Pipeline de middleware - el orden importa. Primero CORS para que las preflight requests pasen.

```csharp
app.UseAuthentication();
```

Autenticacion: lee la cookie de sesion y valida la identidad del usuario.

```csharp
app.MapHealthChecks("/health");
```

Health check endpoint - GET /health retorna 200 OK si el servidor esta activo.

```csharp
app.UseAuthorization();
```

Autorizacion: valida roles despues de confirmar la identidad.

```csharp
app.MapControllers();
```

Mapea todos los controllers de la API descubiertos automaticamente.
