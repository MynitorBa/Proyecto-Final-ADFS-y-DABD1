# Services

## AdminVueloService

> Servicio de administracion de vuelos. Contiene la logica de negocio para crear, cancelar y consultar vuelos, asi como verificar disponibilidad de aviones y tripulantes.

```csharp
public async Task<int> CrearVuelo(CrearVueloAdminDTO dto)
```

Crea un nuevo vuelo aplicando validaciones sobre numero de vuelo, aeropuertos, avion, fecha, horario, cantidad de boletos, precios y existencia de ruta entre los aeropuertos. Retorna el ID del vuelo creado.

---

```csharp
public async Task<List<VueloHistorialDTO>> ObtenerHistorialVuelos()
```

Retorna el historial completo de vuelos registrados en el sistema, incluyendo vuelos pasados, activos y cancelados.

---

```csharp
public async Task<bool> CancelarVuelo(int vueloId)
```

Cancela un vuelo existente dado su identificador. Valida que el ID sea mayor a cero antes de proceder con la cancelacion en el repositorio.

---

```csharp
public async Task<HashSet<int>> ObtenerAvionesOcupados(
```

Retorna el conjunto de IDs de aviones que ya tienen un vuelo programado para la fecha, hora de salida y aeropuerto de origen indicados. Permite filtrar aviones no disponibles al momento de crear un vuelo nuevo.

---

```csharp
public async Task<HashSet<int>> ObtenerTripulantesOcupados(DateTime fecha, TimeSpan horaSalida)
```

Retorna el conjunto de IDs de tripulantes que ya estan asignados a algun vuelo en la fecha y hora de salida indicadas. Permite evitar conflictos de asignacion.

---

## AeropuertoService

> Servicio de aeropuertos. Gestiona la logica de negocio para consultar, crear, actualizar y eliminar aeropuertos, incluyendo manejo de imagenes y fechas disponibles.

```csharp
public async Task<List<AeropuertoDTO>> ObtenerAeropuertos()
```

Retorna la lista completa de aeropuertos registrados en el sistema.

---

```csharp
public async Task<AeropuertoDTO?> ObtenerPorId(int id)
```

Busca y retorna un aeropuerto por su identificador unico. Retorna null si no existe.

---

```csharp
public async Task<List<DateTime>> ObtenerFechasDisponibles()
```

Retorna todas las fechas para las que existe al menos un vuelo programado, sin importar la ruta.

---

```csharp
public async Task<List<DateTime>> ObtenerFechasDisponiblesPorRuta(
```

Retorna las fechas disponibles con vuelos para una ruta especifica definida por aeropuerto de origen, destino, cantidad de personas y clase de vuelo.

---

```csharp
public async Task<AeropuertoDTO?> Crear(CrearAeropuertoDTO crearAeropuertoDTO)
```

Crea un nuevo aeropuerto a partir del DTO recibido. Resuelve o crea el pais, ciudad y zona horaria correspondientes. Si ya existe un aeropuerto con el mismo codigo IATA, lo actualiza en lugar de crear uno nuevo para evitar duplicados.

---

```csharp
public async Task<bool> Actualizar(int id, CrearAeropuertoDTO actualizarAeropuertoDto)
```

Actualiza los datos de un aeropuerto existente. Verifica que no existan duplicados de nombre o codigo IATA con otros aeropuertos, y resuelve la zona horaria indicada. Si se proporciona imagen nueva, la guarda junto con los demas cambios.

---

```csharp
public async Task<bool> Eliminar(int id)
```

Elimina el aeropuerto con el identificador indicado del sistema.

---

```csharp
public async Task GuardarImagen(int aeropuertoId, string imagenBase64)
```

Guarda o reemplaza la imagen en formato Base64 asociada al aeropuerto indicado.

---

```csharp
public async Task EliminarImagen(int aeropuertoId)
```

Elimina la imagen asociada al aeropuerto indicado.

---

## AgenciaService

> Servicio de agencias. Gestiona la logica de negocio para crear, consultar y administrar agencias de viaje, incluyendo asignacion de usuarios webservice, descuentos, estados y URL publica de la agencia.

```csharp
public async Task<AgenciaResponseDTO> CrearAgencia(CrearAgenciaDTO dto)
```

Crea una nueva agencia asignando el usuario webservice indicado en el DTO. Verifica que el usuario exista, tenga rol WebService y no tenga ya una agencia ni un hotel aliado asignados, ya que solo puede tener una entidad activa. Uso exclusivo del administrador.

---

```csharp
public async Task<AgenciaResponseDTO> CrearAgenciaWebservice(int usuarioId, CrearAgenciaWebserviceDTO dto)
```

Permite que un usuario con rol Webservice registre su propia agencia. Verifica que no tenga ya una agencia ni un hotel aliado registrado. El ID del usuario se toma de la sesion activa.

---

```csharp
public async Task<MiAgenciaDTO?> ObtenerMiAgencia(int usuarioId)
```

Retorna la informacion de la agencia asociada al usuario Webservice autenticado. Retorna null si el usuario aun no tiene ninguna agencia registrada.

---

```csharp
public async Task<List<AgenciaAdminDTO>> ObtenerTodasAdmin()
```

Retorna la lista completa de agencias registradas en el sistema, incluyendo datos ampliados para uso administrativo.

---

```csharp
public async Task<List<UsuarioWebserviceDTO>> ObtenerWebserviceSinAgencia()
```

Retorna la lista de usuarios con rol Webservice que no tienen ninguna entidad asignada (ni agencia ni hotel aliado). Util para los selectores del panel admin.

---

```csharp
public async Task AsignarUsuario(int agenciaId, int usuarioId)
```

Asigna un usuario Webservice a una agencia existente. Verifica que el usuario exista, tenga rol Webservice y no este ya asignado a ninguna otra entidad.

---

```csharp
public async Task ActualizarDescuento(int agenciaId, decimal descuento)
```

Actualiza el porcentaje de descuento de una agencia. El valor debe estar entre 0 y 100. Este descuento se aplica sobre el precio de los vuelos al buscar.

---

```csharp
public async Task ActualizarEstado(int agenciaId, int estadoId)
```

Actualiza el estado de una agencia. El valor del estado debe ser un ID valido entre 1 y 3 segun el catalogo de estados de agencia.

---

```csharp
public async Task ActualizarUrl(int agenciaId, string urlAgencia)
```

Actualiza la URL publica de una agencia desde el panel de administracion. La URL no puede estar vacia.

---

## AsientoAgenciaService

> Servicio de asientos para agencias. Gestiona la consulta y cambio de asientos en reservaciones realizadas por una agencia de viaje.

```csharp
public async Task<List<AsientosVueloAgenciaDTO>> ObtenerAsientosPorReservacion(int reservacionId, int agenciaId)
```

Retorna la lista de asientos asignados en una reservacion especifica perteneciente a la agencia indicada.

---

```csharp
public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int agenciaId)
```

Cambia el asiento de un boleto perteneciente a una reservacion de la agencia. Valida que el nuevo numero de asiento no sea vacio y lo convierte a mayusculas.

---

## AsientoService

> Servicio de asientos para usuarios registrados. Gestiona la consulta del mapa de asientos de un vuelo y el cambio de asiento en boletos pertenecientes al usuario autenticado.

```csharp
public async Task<AsientosVueloDTO> ObtenerAsientosVuelo(int vueloId, int usuarioId)
```

Retorna el mapa de asientos de un vuelo especifico, indicando cuales estan disponibles y cuales ya han sido reservados por el usuario autenticado.

---

```csharp
public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int usuarioId)
```

Cambia el asiento asignado a un boleto del usuario. Valida que el nuevo asiento no sea vacio y lo normaliza a mayusculas antes de persistirlo.

---

## AuthService

> Servicio de autenticacion. Verifica las credenciales del usuario contra la base de datos y retorna la informacion de sesion si la autenticacion es exitosa.

```csharp
public async Task<LoginResponseDto?> Login(LoginRequestDto request)
```

Autentica al usuario usando correo o nombre de usuario y contrasena. Verifica el hash de contrasena con BCrypt. Retorna los datos de sesion incluyendo rol del usuario, o null si las credenciales son incorrectas.

---

## AvionService

> Servicio de aviones. Gestiona la logica de negocio para registrar, consultar, actualizar y eliminar aviones de la flota, incluyendo el manejo de imagenes.

```csharp
public async Task<List<AvionDTO>> ObtenerTodos()
```

Retorna la lista completa de aviones registrados en el sistema, incluyendo marca, modelo, capacidad e imagen en Base64.

---

```csharp
public async Task<AvionDTO?> ObtenerPorId(int id)
```

Busca y retorna un avion por su identificador unico. Retorna null si el avion no existe en el sistema.

---

```csharp
public async Task<AvionDTO> Crear(CrearAvionDTO crearAvionDto)
```

Crea un nuevo avion en el sistema a partir del DTO recibido. Si se incluye imagen en Base64, la guarda de manera independiente en el repositorio. Retorna el DTO del avion recien creado con su ID asignado.

---

```csharp
public async Task<bool> Actualizar(int id, CrearAvionDTO actualizarAvionDto)
```

Actualiza los datos de un avion existente. Si se proporciona una nueva imagen en Base64, tambien la actualiza en el repositorio.

---

```csharp
public async Task<bool> Eliminar(int id)
```

Elimina el avion con el identificador indicado del sistema.

---

```csharp
public async Task GuardarImagen(int avionId, string imagenBase64)
```

Guarda o reemplaza la imagen en formato Base64 asociada al avion indicado.

---

```csharp
public async Task EliminarImagen(int avionId)
```

Elimina la imagen asociada al avion indicado.

---

## Busquedatemporalservice 

> Servicio de busquedas temporales en memoria. Almacena el contexto de busqueda de vuelos de forma temporal usando un diccionario concurrente para soportar multiples solicitudes simultaneas. Las entradas expiradas (mayores a 1 hora) se eliminan automaticamente en cada escritura.

```csharp
public string GuardarBusqueda(BusquedaVuelo busqueda)
```

Guarda el contexto de una busqueda de vuelo en memoria y retorna su identificador unico. Antes de guardar, elimina todas las busquedas con mas de una hora de antiguedad.

---

## ComentarioService

> Servicio de comentarios. Gestiona la logica de negocio para crear comentarios con calificacion sobre rutas, publicar respuestas a comentarios y consultar comentarios con informacion de votos por usuario.

```csharp
public async Task<ComentarioDTO> CrearComentarioRuta(int usuarioId, CrearComentarioRutaDTO dto)
```

Crea un comentario con calificacion de estrellas sobre una ruta especifica. Valida que la cantidad de estrellas este entre 1 y 5 y que el contenido no este vacio ni supere los 500 caracteres.

---

```csharp
public async Task<ComentarioDTO> CrearRespuesta(int usuarioId, CrearRespuestaDTO dto)
```

Crea una respuesta a un comentario existente. Valida que el contenido no este vacio y no supere los 500 caracteres.

---

```csharp
public async Task<List<ComentarioConVotoDTO>> ObtenerTodosConVoto(int usuarioId)
```

Retorna todos los comentarios del sistema incluyendo el voto que el usuario autenticado ha emitido sobre cada uno, si lo hay.

---

```csharp
public async Task<List<ComentarioDTO>> ObtenerComentariosPorUsuario(int usuarioId)
```

Retorna todos los comentarios publicados por un usuario especifico.

---

```csharp
public async Task<List<ComentarioConVotoDTO>> ObtenerComentariosRutaConVoto(int rutaId, int usuarioId)
```

Retorna los comentarios de una ruta especifica incluyendo el estado del voto del usuario autenticado en cada comentario.

---

```csharp
public async Task<List<ComentarioDTO>> ObtenerComentariosPorRuta(int rutaId)
```

Retorna todos los comentarios asociados a una ruta especifica sin informacion de votos.

---

## ConfirmarReservacionAgenciaService

> Servicio de confirmacion de reservaciones para agencias. Gestiona la logica de negocio para confirmar una reservacion pendiente de una agencia, validando los datos fiscales requeridos antes de proceder con el pago y emision de boletos.

```csharp
public async Task<ConfirmacionAgenciaDTO> ConfirmarReservacion(
```

Confirma una reservacion pendiente de una agencia aplicando los datos fiscales del DTO. Valida que el NIT y el codigo postal no esten vacios antes de delegar al repositorio. Retorna los datos de confirmacion con la informacion de la factura generada.

---

## DownService

> Servicio de votos en comentarios. Gestiona la logica de negocio para emitir, quitar y consultar votos (upvote/downvote) de los usuarios sobre comentarios de rutas.

```csharp
public async Task<ResultadoVotoDTO> VotarComentario(int usuarioId, VotarComentarioDTO dto)
```

Registra o actualiza el voto de un usuario sobre un comentario. El valor del voto debe ser 1 para upvote o -1 para downvote. Retorna el nuevo conteo de votos positivos y negativos del comentario.

---

```csharp
public async Task<ResultadoVotoDTO> QuitarVoto(int usuarioId, int comentarioId)
```

Elimina el voto que el usuario habia emitido sobre un comentario especifico. Retorna el nuevo conteo de votos del comentario tras la eliminacion.

---

```csharp
public async Task<int?> ObtenerVotoUsuario(int usuarioId, int comentarioId)
```

Retorna el valor del voto que el usuario ha emitido sobre un comentario (1, -1 o null). Retorna null si el usuario no ha votado en ese comentario.

---

## FacturaService

> Servicio de facturacion. Gestiona la logica de negocio para procesar la compra de una reservacion, validando datos fiscales y el formato de tarjeta de pago antes de generar la factura y emitir los boletos.

```csharp
public async Task<CompraRealizadaDTO> ComprarReservacion(
```

Procesa la compra de una reservacion existente. Valida que el NIT y codigo postal esten presentes, verifica el formato de la tarjeta de pago mediante TarjetaHelper (sin almacenar datos de la tarjeta) y delega la creacion de factura y boletos al repositorio.

---

## GestionReservacionService

> Servicio de gestion de reservaciones. Permite a los usuarios consultar, cancelar y obtener resumenes de sus reservaciones, ademas de enviar comprobantes por correo electronico. Tambien expone metodos de apoyo para el flujo de agencias.

```csharp
public async Task<List<ReservacionDetalleDTO>> ObtenerMisReservaciones(int usuarioId)
```

Retorna la lista de todas las reservaciones del usuario autenticado, incluyendo detalle de vuelos, pasajeros y estado de cada reservacion.

---

```csharp
public async Task<ReservacionDetalleDTO> ObtenerDetalleReservacion(int reservacionId, int usuarioId)
```

Retorna el detalle completo de una reservacion especifica perteneciente al usuario. Lanza excepcion si la reservacion no existe o no pertenece al usuario indicado.

---

```csharp
public async Task<ResumenReservacionesDTO> ObtenerResumen(int usuarioId)
```

Retorna un resumen estadistico de las reservaciones del usuario, incluyendo totales por estado y monto acumulado de compras.

---

```csharp
public async Task CancelarReservacion(int reservacionId, int usuarioId, string motivo)
```

Cancela una reservacion activa del usuario. Registra el motivo de cancelacion y libera los asientos y boletos asociados segun la logica del repositorio.

---

```csharp
public async Task EnviarComprobanteEmail(int reservacionId, int usuarioId)
```

Envia al correo del usuario un comprobante en formato HTML con el detalle completo de su reservacion. El envio se realiza de forma asincrona mediante EmailHelper.

---

```csharp
public async Task<int> ObtenerUsuarioWebIdDeAgencia(int agenciaId)
```

Retorna el ID del usuario Webservice asociado a la agencia indicada. Utilizado para determinar el responsable de reservaciones hechas por agencias.

---

```csharp
public async Task<PuedeCancelarDTO> PuedeCancelar(int reservacionId, int usuarioId)
```

Verifica si una reservacion puede ser cancelada por el usuario en el momento actual, considerando politicas de tiempo minimo antes del vuelo y el estado actual de la reservacion.

---

## HandshakeHotelService

> Servicio encargado de iniciar el proceso de handshake de autenticacion con un hotel aliado externo. Genera un token de entrada que identifica a la aerolinea, lo envia al hotel junto con la URL publica de la aerolinea y almacena el token de sesion resultante en la columna TokenHASH del registro HotelAliado. Cuando el backend corre dentro de Docker y la URL del hotel contiene 'localhost', se sustituye ese host por el valor de HANDSHAKE_HOST_OVERRIDE (host.docker.internal) para poder alcanzar el servidor Java corriendo en la maquina anfitriona.

```csharp
public async Task<string> IniciarHandshake(int hotelId)
```

Ejecuta el flujo completo de handshake con un hotel aliado. Obtiene la URL del hotel desde la base de datos sin filtrar por estado, aplica la sustitucion de host si es necesaria para entornos Docker, genera un token de entrada, lo envia al hotel, recibe el token de sesion y lo guarda en HotelAliado.TokenHASH. Si el hotel no existe, no tiene URL configurada, falla la llamada HTTP o falla el guardado en base de datos.

- **Param** `hotelId` - ID del registro HotelAliado con quien iniciar el handshake.
- **Returns** - El token de sesion recibido del hotel aliado.
- `@throws Exception`

---

## HandshakeService

> Servicio de handshake entre la aerolinea y agencias externas. Gestiona el intercambio de tokens de autenticacion para establecer una sesion segura con una agencia registrada identificada por su URL.

```csharp
public async Task<HandshakeResponseDTO> ProcesarHandshake(HandshakeRequestDTO dto)
```

Procesa la solicitud de handshake de una agencia externa. Busca la agencia por su URL, genera un token de salida y guarda ambos tokens (entrada y salida) en la base de datos. Retorna el token de salida que la agencia debe usar en solicitudes posteriores.

---

## HotelAliadoService

> Servicio de hoteles aliados. Gestiona la busqueda dinamica de hoteles consultando cada aliado activo con su propia URL y TokenHASH, el registro y consulta de hoteles por usuarios Webservice, y la administracion completa desde el panel de administracion.

```csharp
public async Task<List<HotelAliadoDTO>> BuscarHoteles(BusquedaHotelesDTO dto)
```

Obtiene todos los hoteles aliados activos de la base de datos y consulta cada uno con su propia URL y token. Agrega todos los resultados en una sola lista. Si un aliado falla, se omite sin interrumpir la busqueda en los demas.

- **Param** `dto` - Criterios de busqueda: ciudad, pais, fechas y personas.
- **Returns** - Lista combinada de hoteles de todos los aliados activos.

---

```csharp
public async Task<MiHotelDTO> CrearHotelWebservice(int usuarioId, CrearHotelWebserviceDTO dto)
```

Permite que un usuario con rol Webservice registre su propio hotel aliado. Verifica que no tenga ya un hotel ni una agencia registrada. El ID del usuario se toma de la sesion activa.

---

```csharp
public async Task<MiHotelDTO?> ObtenerMiHotel(int usuarioId)
```

Retorna la informacion del hotel aliado asociado al usuario Webservice autenticado. Retorna null si el usuario aun no tiene ningun hotel registrado.

---

```csharp
public async Task<List<HotelAdminDTO>> ObtenerTodosAdmin()
```

Retorna la lista completa de hoteles aliados con datos del usuario asignado. Destinado al uso exclusivo del panel de administracion.

---

```csharp
public async Task<HotelAdminDTO> CrearHotelAdmin(CrearHotelAdminDTO dto)
```

Crea un nuevo hotel aliado desde el panel de administracion, asignandolo al usuario Webservice indicado. Verifica que el usuario exista, tenga rol Webservice y no tenga ya ninguna otra entidad asignada (agencia o hotel).

---

```csharp
public async Task ActualizarEstado(int hotelId, int estadoId)
```

Actualiza el estado de un hotel aliado. El EstadoId debe ser un valor valido del catalogo EstadoAliado (1=Activo, 2=Inactivo, 3=Suspendido).

---

```csharp
public async Task AsignarUsuario(int hotelId, int usuarioId)
```

Asigna un usuario Webservice a un hotel aliado existente. Verifica que el usuario exista, tenga rol Webservice y no este ya vinculado a ninguna entidad.

---

```csharp
public async Task ActualizarUrls(int hotelId, string url, string urlParaUsuario)
```

Actualiza la URL de la API y la URL publica de un hotel aliado. Ninguna de las dos URLs puede estar vacia.

---

## MetricasService

> Servicio de metricas del sistema. Provee datos estadisticos sobre busquedas de vuelos, rutas mas solicitadas y distribucion por tipo, con soporte para filtros de fecha. Tambien permite exportar el listado completo sin paginacion.

```csharp
public async Task<MetricasResumenDTO> ObtenerResumen(string? fechaDesde, string? fechaHasta)
```

Retorna un resumen de las metricas del sistema en el rango de fechas indicado. Incluye totales de busquedas, usuarios activos y reservaciones generadas.

---

```csharp
public async Task<List<BusquedasPorDiaDTO>> ObtenerBusquedasPorDia(
```

Retorna la cantidad de busquedas realizadas por dia en el rango de fechas indicado. Util para graficar la evolucion de la demanda a lo largo del tiempo.

---

```csharp
public async Task<List<RutaMasBuscadaDTO>> ObtenerRutasMasBuscadas(
```

Retorna el listado de rutas con mayor cantidad de busquedas en el rango de fechas, filtrado opcionalmente por tipo de busqueda (directo, con escala, etc.).

---

```csharp
public async Task<List<BusquedasPorTipoDTO>> ObtenerBusquedasPorTipo(
```

Retorna la distribucion de busquedas agrupadas por tipo en el rango de fechas indicado. Permite identificar que tipo de viaje es mas demandado por los usuarios.

---

```csharp
public async Task<ListadoBusquedasDTO> ObtenerListado(MetricasFiltroDTO filtro)
```

Retorna un listado paginado de busquedas aplicando los filtros del objeto de filtro recibido. Incluye informacion de usuario, fechas y parametros de cada busqueda.

---

```csharp
public async Task<ListadoBusquedasDTO> ObtenerListadoCompleto(MetricasFiltroDTO filtro)
```

Retorna el listado completo de busquedas sin paginacion, aplicando solo los filtros de fecha, tipo y usuario. Pensado para exportacion de datos en reportes.

---

## NacionalidadService

> Servicio de nacionalidades. Expone la logica de negocio para consultar el catalogo de nacionalidades disponibles en el sistema.

```csharp
public async Task<List<NacionalidadDto>> ObtenerTodas()
```

Retorna la lista completa de nacionalidades disponibles mapeadas a su DTO, incluyendo ID y nombre de cada una.

---

## PdfService

> Servicio de generacion de documentos PDF. Convierte contenido HTML a bytes de PDF. Temporalmente retorna un array vacio mientras la libreria nativa wkhtmltopdf no este disponible en el entorno de desarrollo.

```csharp
public byte[] GenerarPdf(string html)
```

Genera un archivo PDF a partir del contenido HTML recibido. Retorna un array vacio si la libreria nativa no esta disponible.

---

## PerfilService

> Servicio de perfil de usuario. Gestiona la consulta de datos personales, la actualizacion del numero de telefono y el cambio de contrasena con verificacion del hash actual almacenado.

```csharp
public async Task<PerfilDTO?> ObtenerPerfil(int usuarioId)
```

Retorna los datos del perfil del usuario autenticado. Retorna null si el usuario no existe en el sistema.

---

```csharp
public async Task<(bool exito, string mensaje)> ActualizarTelefono(int usuarioId, string telefono)
```

Actualiza el numero de telefono del usuario. Valida que el campo no este vacio. Retorna una tupla con un indicador de exito y un mensaje descriptivo del resultado.

---

```csharp
public async Task<(bool exito, string mensaje)> CambiarContrasena(int usuarioId, CambiarContrasenaDTO dto)
```

Cambia la contrasena del usuario tras verificar que la contrasena actual sea correcta y que la nueva tenga al menos 8 caracteres. Hashea la nueva contrasena antes de guardarla. Retorna una tupla con un indicador de exito y un mensaje descriptivo del resultado.

---

## ReservacionAgenciaService

> Servicio de reservaciones para agencias. Gestiona la logica de negocio para que una agencia pueda crear reservaciones con descuento, expirarlas manualmente y agregar pasajeros con sus datos de pasaporte.

```csharp
public async Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto, int agenciaId)
```

Crea una nueva reservacion para la agencia indicada. Obtiene el porcentaje de descuento configurado para la agencia y lo aplica al momento de crear la reservacion en el repositorio.

---

```csharp
public async Task ExpirarReservacion(int reservacionId, int agenciaId)
```

Expira manualmente una reservacion pendiente de la agencia. Verifica que la reservacion pertenezca a la agencia y que este en estado pendiente antes de proceder.

---

```csharp
public async Task AgregarPasajeros(AgregarPasajerosDTO dto, int agenciaId)
```

Agrega la lista de pasajeros a una reservacion existente de la agencia. Valida que cada pasajero tenga numero de pasaporte y que este contenga solo digitos.

---

## ReservacionService

> Servicio de reservaciones para usuarios registrados. Gestiona la creacion de reservaciones y la asignacion de pasajeros, validando que el usuario este autenticado y que los datos de pasaporte sean correctos antes de persistirlos.

```csharp
public async Task<ReservacionCreadaDTO> CrearReservacion(CrearReservacionDTO dto, int? usuarioId)
```

Crea una nueva reservacion para el usuario autenticado. Requiere que el usuarioId no sea nulo; de lo contrario lanza una excepcion indicando que debe iniciar sesion.

---

```csharp
public async Task AgregarPasajeros(AgregarPasajerosDTO dto)
```

Agrega la lista de pasajeros a una reservacion existente. Valida que cada pasajero tenga numero de pasaporte y que este contenga unicamente digitos numericos.

---

## ReservasCleanupService

> Servicio en segundo plano para el mantenimiento automatico de reservaciones y vuelos. Se ejecuta como BackgroundService y realiza tres tareas periodicas: liberar reservas expiradas, completar reservaciones cuyo vuelo ya paso, y actualizar el estado de los vuelos (en transcurso y finalizados) cada 30 ciclos de ejecucion (30 minutos).

```csharp
protected override async Task ExecuteAsync(CancellationToken stoppingToken)
```

Ejecuta el servicio en segundo plano de forma continua. Libera reservas expiradas, completa reservaciones cuyo vuelo ya paso y actualiza el estado de los vuelos cada 30 ciclos (30 minutos).

---

## RutaAgenciaService

> Servicio de rutas para agencias. Provee acceso al catalogo completo de rutas disponibles que las agencias pueden usar al buscar y reservar vuelos.

```csharp
public async Task<List<RutaAgenciaDTO>> ObtenerTodasLasRutas()
```

Retorna la lista completa de rutas disponibles en el sistema con los datos necesarios para que una agencia realice busquedas de vuelos.

---

## RutaService

> Servicio de rutas aereas. Gestiona la logica de negocio para consultar, crear y actualizar rutas entre aeropuertos, incluyendo el calculo de hora de llegada con conversion de zonas horarias IANA y Windows.

```csharp
public async Task<List<RutaDTO>> ObtenerTodas()
```

Retorna la lista completa de rutas registradas en el sistema.

---

```csharp
public async Task<bool> ActualizarDuracion(int id, int duracionMinutos)
```

Actualiza la duracion estimada en minutos de una ruta existente. Valida que el valor sea mayor a 0 y no supere los 10,000 minutos.

---

```csharp
public async Task<CalculoLlegadaResponseDTO> CalcularLlegada(CalculoLlegadaRequestDTO request)
```

Calcula la hora y fecha de llegada estimadas dado el aeropuerto de origen, destino, fecha y hora de salida. Aplica conversion de zonas horarias si ambos aeropuertos tienen zona configurada; de lo contrario usa calculo directo sin conversion.

---

```csharp
public async Task<bool> ExisteRuta(int origenId, int destinoId)
```

Verifica si ya existe una ruta registrada entre los aeropuertos de origen y destino indicados.

---

```csharp
public async Task<(bool creada, int rutaId, string mensaje)> CrearRuta(
```

Crea una nueva ruta entre dos aeropuertos con la duracion estimada en minutos. Valida que origen y destino sean distintos, que la duracion sea valida y que la ruta no exista previamente. Retorna una tupla con el resultado, el ID y un mensaje.

---

## TokenHotelService

> Servicio que solicita un token de alianza a un hotel aliado especifico. Busca el hotel en la BD de aerolineas por su ID, llama a su endpoint de generacion de token y retorna el resultado al frontend.

```csharp
public async Task<TokenHotelResponseDTO> SolicitarToken(int aliadoId, TokenHotelRequestDTO dto)
```

Busca el hotel aliado por su ID en la BD, llama a su endpoint POST /aerolinea/token con el TokenHASH y retorna el token generado junto con la URL de redireccion para el usuario.

- **Param** `aliadoId` - ID del registro HotelAliado en la BD de aerolineas.
- **Param** `dto` - Ciudad y pais destino del pasajero.
- **Returns** - TokenHotelResponseDTO con token, URL y fecha de expiracion.
- **Throws** `Exception` - Si el aliado no existe, no esta activo o el hotel responde con error.

---

## TripulacionService

> Servicio de tripulacion. Gestiona la logica de negocio para registrar, consultar, actualizar y eliminar tripulantes de vuelo, incluyendo la asignacion de roles, manejo de imagenes y consulta del catalogo de roles disponibles.

```csharp
public async Task<List<TripulanteDTO>> ObtenerTodos()
```

Retorna la lista completa de tripulantes registrados en el sistema. Por cada tripulante resuelve el nombre del rol mediante una consulta adicional al repositorio y construye el DTO con el nombre completo concatenado.

---

```csharp
public async Task<TripulanteDTO?> ObtenerPorId(int id)
```

Busca y retorna los datos de un tripulante especifico por su ID, incluyendo el nombre del rol resuelto desde el repositorio. Retorna null si el tripulante no existe.

---

```csharp
public async Task<TripulanteDTO> Crear(CrearTripulanteDTO crearTripulanteDTO)
```

Crea un nuevo tripulante a partir del DTO recibido y retorna su DTO con el ID asignado.

---

```csharp
public async Task<bool> Actualizar(int id, CrearTripulanteDTO actualizarTripulanteDto)
```

Actualiza los datos de un tripulante existente usando el ID y el DTO proporcionados.

---

```csharp
public async Task<bool> Eliminar(int id)
```

Elimina el tripulante con el identificador indicado del sistema.

---

```csharp
public async Task GuardarImagen(int tripulanteId, string imagenBase64)
```

Guarda o reemplaza la imagen en formato Base64 del tripulante indicado.

---

```csharp
public async Task EliminarImagen(int tripulanteId)
```

Elimina la imagen asociada al tripulante indicado.

---

```csharp
public async Task<List<RolTripulacion>> ObtenerRoles()
```

Retorna el catalogo completo de roles de tripulacion disponibles en el sistema, como piloto, copiloto, auxiliar de vuelo, etc.

---

## UsuarioService

> Servicio de usuarios. Gestiona el registro de nuevos usuarios con validaciones de formato, la verificacion de campos unicos, el cambio de rol por parte del administrador y la consulta del listado completo de usuarios del sistema.

```csharp
public async Task CrearUsuario(CrearUsuarioDTO dto)
```

Registra un nuevo usuario en el sistema. Valida formato de pasaporte y telefono, resuelve o crea el pais y ciudad correspondientes, hashea la contrasena y asigna el rol de cliente por defecto. Tambien guarda las nacionalidades si se proveen y envia un correo de bienvenida de forma no bloqueante.

---

```csharp
public async Task<RegisterConstraint> VerificarConstraints(CrearUsuarioDTO dto)
```

Verifica si ya existe algun usuario con el mismo correo, nombre de usuario o pasaporte. Retorna un objeto con los campos que generarian conflicto de unicidad.

---

```csharp
public async Task<(bool exito, string mensaje)> CambiarRol(CambiarRolDTO dto)
```

Cambia el rol de un usuario existente. Verifica que tanto el usuario como el rol indicados existan antes de aplicar el cambio. Retorna una tupla con resultado y mensaje.

---

```csharp
public async Task<List<object>> ObtenerTodos()
```

Retorna la lista de todos los usuarios registrados en el sistema como objetos anonimos, incluyendo ID, nombre, apellido, correo, username, ID de rol y nombre del rol.

---

## VueloAgenciaService

> Servicio de busqueda de vuelos para agencias. Resuelve la ciudad y aeropuerto a partir del nombre de pais y ciudad provistos por la agencia, aplica el descuento configurado para la agencia y retorna vuelos directos y con escala con precios ajustados.

```csharp
public async Task<ResultadoBusquedaDTO> BuscarVuelos(BuscarVueloAgenciaDTO dto, int agenciaId)
```

Busca vuelos disponibles para una agencia resolviendo primero los aeropuertos de origen y destino a partir de los nombres de pais y ciudad. Obtiene el descuento de la agencia, busca vuelos directos y con escala, y aplica el factor de descuento a todos los precios antes de retornar los resultados.

---

## VueloService

> Servicio de vuelos para usuarios. Gestiona la busqueda de vuelos directos y con escala, el registro de busquedas para metricas y la aplicacion de filtros de precio en memoria.

```csharp
public async Task<List<VueloDetalleDTO>> BusquedaGeneral(string query)
```

Realiza una busqueda general de vuelos por texto libre. Retorna una lista de vuelos cuyo numero, origen o destino coincidan con la consulta.

---

```csharp
public async Task<ResultadoBusquedaDTO> BuscarVuelos(BuscarVueloDTO dto, int? usuarioId)
```

Busca vuelos disponibles entre dos aeropuertos en una fecha y con los filtros indicados. Registra la busqueda en la base de datos para metricas. Retorna vuelos directos y con escala, ambos filtrados por rango de precio si se especifica.

---
