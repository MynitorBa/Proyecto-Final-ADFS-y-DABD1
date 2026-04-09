# Controllers

## AdminVuelosController

> Controlador de administracion de vuelos. Expone endpoints REST para crear, cancelar y consultar vuelos desde el panel de administrador. Todos los endpoints requieren rol Administrador.

```csharp
public async Task<IActionResult> CrearVuelo([FromBody] CrearVueloAdminDTO dto)
```

Crea un nuevo vuelo con los datos provistos por el administrador. Traduce errores de base de datos a mensajes legibles para el usuario.

---

```csharp
public async Task<IActionResult> ObtenerHistorialVuelos()
```

Retorna el historial completo de vuelos registrados en el sistema, incluyendo vuelos pasados, activos y cancelados.

---

```csharp
public async Task<IActionResult> CancelarVuelo(int id)
```

Cancela un vuelo activo por su identificador. Si el vuelo ya esta cancelado o no existe, retorna un error 404.

---

```csharp
public async Task<IActionResult> AvionesOcupados(
```

Devuelve los identificadores de aviones que ya tienen vuelo asignado en la fecha y hora indicadas. Permite filtrar adicionalmente por aeropuerto de origen. Se usa en el formulario de creacion de vuelo para deshabilitar aviones no disponibles.

---

```csharp
public async Task<IActionResult> TripulantesOcupados(
```

Devuelve los identificadores de tripulantes que ya tienen vuelo asignado en la fecha y hora indicadas. Se usa en el formulario de creacion de vuelo para deshabilitar tripulantes no disponibles.

---

## AeropuertosController

> Controlador de aeropuertos. Expone endpoints REST para consultar, crear, actualizar y eliminar aeropuertos, asi como gestionar sus imagenes. Los endpoints de lectura son publicos; los de escritura requieren rol Administrador.

```csharp
public async Task<IActionResult> ObtenerAeropuertos()
```

Retorna la lista completa de aeropuertos registrados. Endpoint publico, utilizado en el buscador de vuelos para poblar los selectores de origen y destino.

---

```csharp
public async Task<IActionResult> ObtenerPorId(int id)
```

Retorna los datos de un aeropuerto especifico por su identificador. Devuelve 404 si el aeropuerto no existe.

---

```csharp
public async Task<IActionResult> ObtenerFechasDisponibles(
```

Retorna las fechas con vuelos disponibles para una ruta determinada, filtrando por cantidad de pasajeros y clase. Se usa en el calendario del buscador de vuelos.

---

```csharp
public async Task<IActionResult> Crear([FromBody] CrearAeropuertoDTO crearAeropuertoDTO)
```

Crea un nuevo aeropuerto con los datos del DTO. Requiere rol Administrador. Retorna 409 si ya existe un aeropuerto con el mismo codigo IATA.

---

```csharp
public async Task<IActionResult> Actualizar(int id, [FromBody] CrearAeropuertoDTO actualizarAeropuertoDto)
```

Actualiza los datos de un aeropuerto existente. Requiere rol Administrador. Retorna 404 si el aeropuerto no existe y 409 si hay conflicto con el codigo IATA.

---

```csharp
public async Task<IActionResult> Eliminar(int id)
```

Elimina un aeropuerto por su identificador. Requiere rol Administrador. Retorna 404 si el aeropuerto no existe.

---

```csharp
public async Task<IActionResult> SubirImagen(int id, [FromBody] SubirImagenDTO dto)
```

Sube o reemplaza la imagen de un aeropuerto enviada como cadena Base64. Requiere rol Administrador. Retorna 404 si el aeropuerto no existe.

---

```csharp
public async Task<IActionResult> EliminarImagen(int id)
```

Elimina la imagen asociada a un aeropuerto. Requiere rol Administrador. Retorna 404 si el aeropuerto no existe.

---

## AgenciaController

> Controlador de agencias de viaje. Expone endpoints para que el administrador gestione agencias y para que usuarios Webservice consulten y registren su propia agencia.

```csharp
public async Task<IActionResult> CrearAgencia([FromBody] CrearAgenciaDTO dto)
```

Crea una nueva agencia vinculada a un usuario Webservice. Requiere rol Administrador. Verifica que el usuario no tenga ya una agencia ni un hotel aliado asignados.

---

```csharp
public async Task<IActionResult> ObtenerTodasAdmin()
```

Retorna la lista completa de agencias registradas en el sistema. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> ObtenerWebserviceDisponibles()
```

Retorna los usuarios con rol Webservice que no tienen ninguna entidad asignada (ni agencia ni hotel aliado). Se usa para poblar los selectores de asignacion tanto en el panel de agencias como en el de hoteles.

---

```csharp
public async Task<IActionResult> AsignarUsuario(int id, [FromBody] AsignarUsuarioAgenciaDTO dto)
```

Asigna un usuario Webservice a una agencia existente. Requiere rol Administrador. El usuario no puede tener ya una agencia ni un hotel aliado registrado.

---

```csharp
public async Task<IActionResult> ActualizarDescuento(int id, [FromBody] ActualizarDescuentoDTO dto)
```

Actualiza el porcentaje de descuento aplicado a las reservaciones de una agencia. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> ActualizarEstado(int id, [FromBody] ActualizarEstadoAgenciaDTO dto)
```

Actualiza el estado de una agencia (activa, suspendida, etc.). Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> ActualizarUrl(int id, [FromBody] ActualizarUrlAgenciaDTO dto)
```

Actualiza la URL publica de una agencia desde el panel de administracion. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> ObtenerMiAgencia()
```

Retorna los datos de la agencia asociada al usuario Webservice autenticado. Si el usuario no tiene agencia registrada, retorna tieneAgencia = false. Solo accesible para usuarios con rol Webservice (rolId = 3).

---

```csharp
public async Task<IActionResult> CrearMiAgencia([FromBody] CrearAgenciaWebserviceDTO dto)
```

Permite a un usuario Webservice autenticado crear su propia agencia por primera vez. Solo accesible para usuarios con rol Webservice (rolId = 3).

---

## AsientoAgenciaController

> Controlador de gestion de asientos para agencias de viaje. Permite a una agencia autenticada consultar y cambiar los asientos de los boletos dentro de sus reservaciones. Todos los endpoints requieren autenticacion de agencia mediante AgenciaAuthMiddleware.

```csharp
public async Task<IActionResult> ObtenerAsientosPorReservacion(int reservacionId)
```

Retorna los asientos asignados a cada boleto de una reservacion especifica de la agencia. Verifica que la reservacion pertenezca a la agencia autenticada antes de retornar datos.

---

```csharp
public async Task<IActionResult> CambiarAsiento(int boletoId, [FromBody] CambiarAsientoAgenciaRequestDTO dto)
```

Cambia el asiento asignado a un boleto especifico de una reservacion de la agencia. Verifica que el boleto pertenezca a la agencia autenticada antes de aplicar el cambio.

---

## AsientoController

> Controlador de gestion de asientos para usuarios autenticados. Permite al usuario consultar la disponibilidad de asientos en un vuelo y cambiar el asiento de su boleto. Todos los endpoints requieren sesion activa.

```csharp
public async Task<IActionResult> ObtenerAsientosVuelo(int vueloId)
```

Devuelve la lista de asientos del vuelo indicado, marcando cuales estan ocupados y cual es el asiento actualmente asignado al usuario autenticado. Se usa para renderizar el mapa de asientos en el flujo de reservacion.

---

```csharp
public async Task<IActionResult> CambiarAsiento(int boletoId, [FromBody] CambiarAsientoRequestDTO dto)
```

Cambia el asiento asignado al boleto indicado. Verifica que el boleto pertenezca al usuario autenticado y que el nuevo asiento este disponible antes de aplicar el cambio.

---

## AuthController

> Controlador de autenticacion. Gestiona el inicio de sesion, la consulta de sesion activa y el cierre de sesion mediante cookies cifradas de ASP.NET Core.

```csharp
public async Task<IActionResult> Login(LoginRequestDto request)
```

---

```csharp
public IActionResult ObtenerSesion()
```

Retorna los datos del usuario autenticado extraidos de la cookie de sesion activa, incluyendo id, nombre, correo, id de rol y nombre de rol. Requiere sesion activa.

---

```csharp
public async Task<IActionResult> Logout()
```

Cierra la sesion del usuario eliminando la cookie de autenticacion del navegador. Requiere sesion activa.

---

## AvionesController

> Controlador de aviones. Expone endpoints REST para consultar, crear, actualizar, eliminar aviones y gestionar sus imagenes. Los endpoints de lectura son publicos; los de escritura requieren rol Administrador.

```csharp
public async Task<ActionResult<List<AvionDTO>>> ObtenerTodos()
```

Retorna la lista completa de aviones registrados. Endpoint publico, utilizado para poblar selectores en el formulario de creacion de vuelos del panel de admin.

---

```csharp
public async Task<ActionResult<AvionDTO>> ObtenerPorId(int id)
```

Retorna los datos de un avion especifico por su identificador. Devuelve 404 si el avion no existe.

---

```csharp
public async Task<ActionResult<AvionDTO>> Crear([FromBody] CrearAvionDTO crearAvionDto)
```

Crea un nuevo avion con los datos del DTO. Requiere rol Administrador.

---

```csharp
public async Task<ActionResult> Actualizar(int id, [FromBody] CrearAvionDTO actualizarAvionDto)
```

Actualiza los datos de un avion existente. Requiere rol Administrador. Retorna 404 si el avion no existe.

---

```csharp
public async Task<ActionResult> Eliminar(int id)
```

Elimina un avion por su identificador. Requiere rol Administrador. Retorna 404 si el avion no existe.

---

```csharp
public async Task<ActionResult> SubirImagen(int id, [FromBody] SubirImagenDTO dto)
```

Sube o reemplaza la imagen de un avion enviada como cadena Base64. Requiere rol Administrador. Retorna 404 si el avion no existe.

---

```csharp
public async Task<ActionResult> EliminarImagen(int id)
```

Elimina la imagen asociada a un avion. Requiere rol Administrador. Retorna 404 si el avion no existe.

---

## Busquedacontroller 

> Controlador de busquedas temporales de vuelos. Permite guardar los parametros de una busqueda en memoria y recuperarlos por identificador, facilitando la navegacion entre pasos del flujo de reservacion sin depender de la URL ni del estado del frontend.

```csharp
public IActionResult GuardarBusqueda([FromBody] GuardarBusquedaDto dto)
```

Guarda los parametros de una busqueda de vuelos (origen, destino, fechas, pasajeros) en memoria y retorna un identificador unico para recuperarlos posteriormente.

---

```csharp
public IActionResult ObtenerBusqueda(string id)
```

Recupera los parametros de una busqueda previamente guardada por su identificador. Retorna 404 si la busqueda no existe o ha expirado.

---

## ComentariosController

> Controlador de comentarios y respuestas sobre rutas. Permite a los usuarios autenticados publicar comentarios en rutas, responder comentarios existentes y consultar los comentarios con su estado de voto. Las agencias tambien pueden consultar comentarios de rutas.

```csharp
public async Task<IActionResult> CrearComentarioRuta([FromBody] CrearComentarioRutaDTO dto)
```

Publica un nuevo comentario sobre una ruta especifica. Requiere sesion activa. El comentario queda vinculado al usuario autenticado y a la ruta indicada en el DTO.

---

```csharp
public async Task<IActionResult> CrearRespuesta([FromBody] CrearRespuestaDTO dto)
```

Publica una respuesta a un comentario existente. Requiere sesion activa. La respuesta queda vinculada al comentario padre indicado en el DTO.

---

```csharp
public async Task<IActionResult> ObtenerTodosConVoto()
```

Retorna todos los comentarios del sistema incluyendo el estado de voto del usuario autenticado en cada comentario. Requiere sesion activa.

---

```csharp
public async Task<IActionResult> ObtenerMisComentarios()
```

Retorna los comentarios publicados por el usuario autenticado. Requiere sesion activa.

---

```csharp
public async Task<IActionResult> ObtenerComentariosRutaConVoto(int rutaId)
```

Retorna los comentarios de una ruta especifica incluyendo el estado de voto del usuario autenticado en cada comentario. Requiere sesion activa.

---

```csharp
public async Task<IActionResult> ObtenerComentariosPorRuta(int rutaId)
```

Retorna los comentarios de una ruta especifica. Endpoint publico, no requiere autenticacion.

---

```csharp
public async Task<IActionResult> ObtenerComentariosRutaAgencia(int rutaId)
```

Retorna los comentarios de una ruta para consumo de agencias. Requiere autenticacion de agencia mediante AgenciaAuthMiddleware.

---

## ConfirmarReservacionAgenciaController

> Controlador para confirmar reservaciones de agencias de viaje. Expone el endpoint de confirmacion que finaliza el proceso de compra de una reservacion pendiente creada por una agencia autenticada.

```csharp
public async Task<IActionResult> ConfirmarReservacion(
```

Confirma y finaliza el pago de una reservacion existente de la agencia. Verifica que la reservacion pertenezca a la agencia autenticada antes de procesar la confirmacion.

---

## EmailController

> Controlador de correos electronicos. Gestiona el envio de comprobantes de reservacion, mensajes de contacto desde el formulario publico y suscripciones al boletin informativo.

```csharp
public async Task<IActionResult> EnviarCorreoReservacion(int id)
```

Envia al correo del usuario el comprobante de una reservacion especifica. Solo accesible para roles Administrador (1) y Cliente (2). Verifica que la reservacion pertenezca al usuario autenticado antes de enviar el correo.

---

```csharp
public async Task<IActionResult> Contacto([FromBody] ContactoDTO dto)
```

Recibe un mensaje del formulario de contacto publico y lo reenvÃ­a al correo del administrador. Requiere nombre, correo y mensaje como campos obligatorios.

---

```csharp
public async Task<IActionResult> Newsletter([FromBody] NewsletterDTO dto)
```

Registra una suscripcion al boletin informativo y notifica al administrador por correo. Endpoint publico, valida que el correo tenga formato valido antes de procesar.

---

## FacturaController

> Controlador de facturacion y compra de reservaciones. Expone el endpoint que procesa el pago de una reservacion pendiente, genera la factura y confirma los boletos del usuario.

```csharp
public async Task<IActionResult> ComprarReservacion(int id, [FromBody] ComprarReservacionDTO dto)
```

Procesa la compra de una reservacion pendiente validando el metodo de pago y generando la factura correspondiente. Requiere sesion activa. Solo el propietario de la reservacion puede realizar la compra.

---

## HandshakeController

> Controlador de autenticacion de agencias externas (handshake). Permite a una agencia externa autenticarse contra la API mediante un token de entrada y recibir un token de sesion para usar en las solicitudes posteriores a los endpoints de agencia.

```csharp
public async Task<IActionResult> ProcesarHandshake([FromBody] HandshakeRequestDTO dto)
```

Recibe las credenciales de una agencia externa (URL y token de entrada), las valida contra la base de datos y retorna un token de sesion si la autenticacion es exitosa.

---

## HandshakeHotelController

> Controlador que inicia el proceso de handshake entre la aerolinea y un hotel aliado. Genera un token de entrada, lo envia al hotel y guarda el token de sesion resultante en la base de datos. Solo accesible por administradores autenticados.

```csharp
public async Task<IActionResult> IniciarHandshake(int hotelId)
```

Inicia el handshake de autenticacion con el hotel aliado identificado por hotelId. Genera un token de entrada, lo envia al endpoint /api/aerolineas/handshake del hotel y guarda el token de sesion recibido en HotelAliado.TokenHASH. Retorna el token de sesion resultante si el proceso fue exitoso.

---

## HealthController

> Controlador de estado de la API. Expone un endpoint publico para verificar que el servicio esta en linea y obtener informacion basica del ambiente y la hora del servidor. Utilizado por herramientas de monitoreo y orquestacion de contenedores.

```csharp
public IActionResult Get()
```

Retorna el estado actual de la API, el ambiente de ejecucion (Development, Production, etc.) y la hora UTC del servidor. Endpoint publico, no requiere autenticacion.

---

## HotelAliadoController

> Controlador de hoteles aliados. Expone el endpoint de busqueda dinamica de hoteles, los endpoints para que usuarios Webservice consulten y registren su propio hotel, y los endpoints de administracion completa para el panel de administrador.

```csharp
public async Task<IActionResult> BuscarHoteles([FromBody] BusquedaHotelesDTO dto)
```

Busca hoteles disponibles en la ciudad destino del pasajero consultando la API de cada hotel aliado activo de forma dinamica.

---

```csharp
public async Task<IActionResult> ObtenerMiHotel()
```

Retorna los datos del hotel aliado asociado al usuario Webservice autenticado. Si el usuario no tiene hotel registrado, retorna tieneHotel = false. Solo accesible para usuarios con rol Webservice (rolId = 3).

---

```csharp
public async Task<IActionResult> CrearMiHotel([FromBody] CrearHotelWebserviceDTO dto)
```

Permite a un usuario Webservice autenticado registrar su propio hotel aliado. Un usuario Webservice solo puede tener un hotel o una agencia, nunca ambos. Solo accesible para usuarios con rol Webservice (rolId = 3).

---

```csharp
public async Task<IActionResult> ObtenerTodosAdmin()
```

Retorna la lista completa de hoteles aliados con datos del usuario asignado. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> CrearHotelAdmin([FromBody] CrearHotelAdminDTO dto)
```

Crea un nuevo hotel aliado y lo vincula al usuario Webservice indicado. Verifica que el usuario no tenga ya ninguna otra entidad asignada. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> ActualizarEstado(int id, [FromBody] ActualizarEstadoHotelDTO dto)
```

Actualiza el estado de un hotel aliado segun el catalogo EstadoAliado. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> AsignarUsuario(int id, [FromBody] AsignarUsuarioHotelDTO dto)
```

Asigna un usuario Webservice a un hotel aliado existente. El usuario no puede tener ya ninguna otra entidad asignada. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> ActualizarUrls(int id, [FromBody] ActualizarUrlHotelDTO dto)
```

Actualiza la URL de la API y la URL publica para usuarios de un hotel aliado. Requiere rol Administrador.

---

## MetricasController

> Controlador de metricas y analiticos del sistema. Expone endpoints para que el administrador consulte resumenes, graficas de busquedas por dia, rutas mas buscadas, busquedas por tipo de canal, listados con filtros paginados y exportacion de reportes por correo. Todos los endpoints requieren rol Administrador.

```csharp
public async Task<IActionResult> ObtenerResumen(
```

Retorna un resumen de metricas clave del sistema (totales, conversiones, etc.) para el rango de fechas indicado. Si no se especifican fechas se usa el periodo completo.

---

```csharp
public async Task<IActionResult> BusquedasPorDia(
```

Retorna la cantidad de busquedas realizadas por dia dentro del rango de fechas indicado. Se usa para renderizar la grafica de linea en el panel de analiticos del administrador.

---

```csharp
public async Task<IActionResult> RutasMasBuscadas(
```

Retorna las rutas origen-destino mas frecuentes en el periodo indicado, con opcion de filtrar por tipo de canal (Web o REST). Se usa para la grafica de barras del panel.

---

```csharp
public async Task<IActionResult> BusquedasPorTipo(
```

Retorna el desglose de busquedas por tipo de canal (Web vs REST) en el periodo indicado. Se usa para la grafica de dona del panel de analiticos.

---

```csharp
public async Task<IActionResult> ObtenerListado([FromBody] MetricasFiltroDTO filtro)
```

Retorna un listado paginado de registros de busqueda con los filtros especificados en el cuerpo de la solicitud (fechas, tipo de canal, usuario y tamano de pagina).

---

```csharp
public async Task<IActionResult> ExportarPorCorreo([FromBody] ExportarMetricasDTO dto)
```

Genera un reporte HTML con todos los registros de busqueda segun los filtros indicados y lo envia por correo electronico a la direccion especificada. No aplica paginacion al exportar, incluye hasta 9999 registros.

---

## MisReservacionesController

> Controlador de reservaciones del usuario autenticado. Permite consultar el listado y detalle de sus reservaciones, descargar o enviar comprobantes, obtener un resumen estadistico y cancelar reservaciones activas. Todos los endpoints requieren sesion activa.

```csharp
public async Task<IActionResult> ObtenerMisReservaciones()
```

Retorna el listado de todas las reservaciones del usuario autenticado, incluyendo estado, vuelos y monto total de cada una.

---

```csharp
public async Task<IActionResult> ObtenerDetalleReservacion(int reservacionId)
```

Retorna el detalle completo de una reservacion especifica del usuario autenticado, incluyendo boletos, pasajeros, vuelos y datos de facturacion.

---

```csharp
public async Task<IActionResult> DescargarComprobante(int reservacionId)
```

Genera y retorna el comprobante de una reservacion como HTML para que el usuario lo abra en una nueva pestana e imprima como PDF desde el navegador. No requiere la libreria nativa wkhtmltopdf.

---

```csharp
public async Task<IActionResult> ObtenerResumen()
```

Retorna un resumen estadistico de las reservaciones del usuario autenticado, como totales por estado, monto gastado y proximos vuelos.

---

```csharp
public async Task<IActionResult> CancelarReservacion(int reservacionId, [FromBody] CancelarReservacionDTO dto)
```

Cancela una reservacion activa del usuario autenticado. El motivo de cancelacion es opcional. Solo se pueden cancelar reservaciones pendientes o confirmadas.

---

```csharp
public async Task<IActionResult> EnviarComprobanteEmail(int reservacionId)
```

Envia el comprobante de una reservacion al correo electronico registrado del usuario. Genera el HTML del comprobante y lo adjunta al correo antes de enviarlo.

---

## NacionalidadesController

> Controlador de nacionalidades. Expone un endpoint publico para obtener el catalogo de nacionalidades disponibles, utilizado en el formulario de registro de usuarios y en el ingreso de datos de pasajeros durante la reservacion.

```csharp
public async Task<IActionResult> Get()
```

Retorna el listado completo de nacionalidades registradas en el sistema. Endpoint publico, no requiere autenticacion.

---

## PdfController

> Controlador de generacion de comprobantes en HTML para impresion como PDF. Genera el HTML del comprobante de una reservacion y lo retorna como contenido de texto para que el frontend lo abra en una nueva pestana e imprima desde el navegador.

```csharp
public async Task<IActionResult> ObtenerComprobante(int id)
```

Retorna el HTML formateado del comprobante de una reservacion para que el usuario lo imprima como PDF desde el navegador. Solo accesible para roles Administrador (1) y Cliente (2). Verifica que la reservacion pertenezca al usuario autenticado.

---

## PerfilController

> Controlador de perfil de usuario. Permite al usuario autenticado consultar sus datos, actualizar su numero de telefono y cambiar su contrasena. Aplica verificacion de propiedad para garantizar que cada usuario solo pueda modificar su propio perfil. Todos los endpoints requieren sesion activa.

```csharp
public async Task<IActionResult> ObtenerPerfil(int usuarioId)
```

Retorna los datos del perfil de un usuario especifico. Solo el propio usuario puede consultar su perfil; retorna 403 si el id de ruta no coincide con la sesion.

---

```csharp
public async Task<IActionResult> ActualizarTelefono(
```

Actualiza el numero de telefono del usuario especificado. Solo el propio usuario puede modificar su telefono; retorna 403 si el id de ruta no coincide con la sesion.

---

```csharp
public async Task<IActionResult> CambiarContrasena(
```

Cambia la contrasena del usuario especificado tras validar la contrasena actual. Solo el propio usuario puede cambiar su contrasena; retorna 403 si el id de ruta no coincide con la sesion activa.

---

## ReservacionAgenciaController

> Controlador para la creacion y gestion inicial de reservaciones por parte de agencias de viaje. Permite a una agencia autenticada crear reservaciones, agregar datos de pasajeros y expirar reservaciones pendientes. Todos los endpoints requieren autenticacion de agencia.

```csharp
public async Task<IActionResult> CrearReservacion([FromBody] CrearReservacionDTO dto)
```

Crea una nueva reservacion en estado pendiente para la agencia autenticada. Retorna el detalle de la reservacion creada incluyendo los boletos generados.

---

```csharp
public async Task<IActionResult> ExpirarReservacion(int id)
```

Marca una reservacion pendiente como expirada. Se usa cuando la agencia no completa el flujo de confirmacion dentro del tiempo permitido.

---

```csharp
public async Task<IActionResult> AgregarPasajeros([FromBody] AgregarPasajerosDTO dto)
```

Guarda los datos de los pasajeros asociados a una reservacion de la agencia. Debe llamarse antes de confirmar la reservacion para registrar la informacion requerida de cada boleto.

---

## ReservacionesAgenciaController

> Controlador de gestion de reservaciones existentes para agencias de viaje. Permite a una agencia autenticada consultar el detalle de una reservacion, cancelarla y verificar si es posible cancelarla. Todos los endpoints requieren autenticacion de agencia.

```csharp
public async Task<IActionResult> ObtenerDetalle(int reservacionId)
```

Retorna el detalle completo de una reservacion especifica de la agencia autenticada, incluyendo boletos, pasajeros, vuelos y estado actual.

---

```csharp
public async Task<IActionResult> CancelarReservacion(int reservacionId, [FromBody] CancelarReservacionDTO dto)
```

Cancela una reservacion activa de la agencia autenticada. El motivo de cancelacion es opcional. Solo se pueden cancelar reservaciones que aun no hayan sido completadas.

---

```csharp
public async Task<IActionResult> PuedeCancelar(int reservacionId)
```

Verifica si una reservacion de la agencia puede ser cancelada segun las reglas de negocio (estado actual, tiempo antes del vuelo, etc.). Retorna un objeto con el resultado de la validacion.

---

## ReservacionesController

> Controlador de reservaciones para usuarios del portal web. Expone los endpoints para crear una reservacion y agregar los datos de los pasajeros asociados. La creacion de reservacion requiere sesion activa; el alta de pasajeros tambien.

```csharp
public async Task<IActionResult> CrearReservacion([FromBody] CrearReservacionDTO dto)
```

Crea una nueva reservacion en estado pendiente para el usuario autenticado. Asigna automaticamente los boletos y asientos segun el vuelo y la clase seleccionados.

---

```csharp
public async Task<IActionResult> AgregarPasajeros(int id, [FromBody] List<DatosPasajeroDTO> pasajeros)
```

Registra o actualiza los datos de los pasajeros de una reservacion existente. Debe llamarse antes de confirmar la compra para asociar la informacion de cada pasajero al boleto correspondiente.

---

## RutaAgenciaController

> Controlador de rutas para agencias de viaje externas. Expone el listado de rutas disponibles a agencias autenticadas mediante AgenciaAuthMiddleware, para que puedan construir sus propios buscadores de vuelos.

```csharp
public async Task<IActionResult> ObtenerRutas()
```

Retorna el listado completo de rutas con vuelos disponibles para consumo de agencias. Requiere autenticacion de agencia mediante token en la solicitud.

---

## RutasController

> Controlador de rutas de vuelo. Expone endpoints para que el administrador consulte, cree y actualice rutas origen-destino, asi como calcule el tiempo estimado de llegada teniendo en cuenta las zonas horarias de los aeropuertos involucrados. Todos los endpoints requieren rol Administrador.

```csharp
public async Task<IActionResult> ObtenerTodas()
```

Retorna el listado completo de rutas registradas en el sistema. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> ActualizarDuracion(int id, [FromBody] EditarDuracionRutaDTO dto)
```

Actualiza la duracion estimada de vuelo de una ruta existente. Requiere rol Administrador. Retorna 404 si la ruta no existe y 400 si la duracion proporcionada no es valida.

---

```csharp
public async Task<IActionResult> CalcularLlegada([FromBody] CalculoLlegadaRequestDTO request)
```

Calcula la fecha y hora de llegada estimada a partir del aeropuerto origen, destino, fecha y hora de salida. Considera las zonas horarias de ambos aeropuertos. Retorna null si faltan datos en lugar de retornar error, para no bloquear el formulario del admin. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> CrearRuta([FromBody] CrearRutaDTO dto)
```

Crea una nueva ruta entre dos aeropuertos con la duracion estimada indicada. Si la ruta ya existe retorna el mensaje correspondiente. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> ExisteRuta(
```

Verifica si ya existe una ruta entre dos aeropuertos. Se usa en el formulario de creacion de rutas para validar en tiempo real. Requiere rol Administrador.

---

## TokenHotelController

> Controlador que expone el endpoint para solicitar un token de alianza a un hotel aliado especifico. Requiere sesion activa del usuario.

```csharp
public async Task<IActionResult> SolicitarToken(int aliadoId, [FromBody] TokenHotelRequestDTO dto)
```

Solicita un token de alianza al hotel identificado por aliadoId. El hotel genera un token de un solo uso valido por 15 minutos y retorna la URL lista para redirigir al usuario.

---

## TripulacionController

> Controlador de tripulacion. Expone endpoints REST para consultar, crear, actualizar y eliminar tripulantes, asi como gestionar sus imagenes y obtener el catalogo de roles. Los endpoints de lectura son publicos; los de escritura requieren rol Administrador.

```csharp
public async Task<IActionResult> ObtenerTodos()
```

Retorna la lista completa de tripulantes registrados. Endpoint publico, utilizado para poblar selectores en el formulario de creacion de vuelos del panel de admin.

---

```csharp
public async Task<IActionResult> ObtenerPorId(int id)
```

Retorna los datos de un tripulante especifico por su identificador. Devuelve 404 si el tripulante no existe.

---

```csharp
public async Task<IActionResult> ObtenerRoles()
```

Retorna el catalogo de roles de tripulacion disponibles (piloto, copiloto, auxiliar de vuelo, etc.). Endpoint publico.

---

```csharp
public async Task<IActionResult> Crear([FromBody] CrearTripulanteDTO crearTripulanteDTO)
```

Crea un nuevo tripulante con los datos del DTO. Requiere rol Administrador.

---

```csharp
public async Task<IActionResult> Actualizar(int id, [FromBody] CrearTripulanteDTO actualizarTripulanteDto)
```

Actualiza los datos de un tripulante existente. Requiere rol Administrador. Retorna 404 si el tripulante no existe.

---

```csharp
public async Task<IActionResult> Eliminar(int id)
```

Elimina un tripulante por su identificador. Requiere rol Administrador. Retorna 404 si el tripulante no existe.

---

```csharp
public async Task<IActionResult> SubirImagen(int id, [FromBody] SubirImagenDTO dto)
```

Sube o reemplaza la imagen de un tripulante enviada como cadena Base64. Requiere rol Administrador. Retorna 404 si el tripulante no existe.

---

```csharp
public async Task<IActionResult> EliminarImagen(int id)
```

Elimina la imagen asociada a un tripulante. Requiere rol Administrador. Retorna 404 si el tripulante no existe.

---

## UsuariosController

> Controlador de usuarios. Expone endpoints para el registro publico de nuevos usuarios, la validacion de datos en tiempo real durante el registro, el cambio de rol por parte del administrador y la consulta del listado completo de usuarios.

```csharp
public async Task<IActionResult> CrearUsuario([FromBody] CrearUsuarioDTO dto)
```

Crea un nuevo usuario en el sistema tras verificar que el correo, nombre de usuario y pasaporte no esten ya en uso. Endpoint publico, no requiere autenticacion.

---

```csharp
public async Task<IActionResult> VerificarConstraints([FromBody] CrearUsuarioDTO dto)
```

Verifica si el correo, nombre de usuario o pasaporte del DTO ya existen en el sistema. Se usa para validacion en tiempo real mientras el usuario completa el formulario de registro. Endpoint publico, no requiere autenticacion.

---

```csharp
public async Task<IActionResult> CambiarRol([FromBody] CambiarRolDTO dto)
```

Cambia el rol de un usuario existente. Requiere rol Administrador. Retorna el resultado de la operacion con un mensaje descriptivo del cambio realizado.

---

```csharp
public async Task<IActionResult> ObtenerTodos()
```

Retorna el listado completo de usuarios registrados en el sistema. Requiere rol Administrador.

---

## VotosController

> Controlador de votos en comentarios. Permite a los usuarios autenticados emitir votos positivos o negativos en comentarios, quitar votos previamente emitidos y consultar su voto actual sobre un comentario especifico. Todos los endpoints requieren sesion activa.

```csharp
public async Task<IActionResult> VotarComentario([FromBody] VotarComentarioDTO dto)
```

Registra un voto positivo (1) o negativo (-1) del usuario autenticado sobre un comentario. Si el usuario ya habia votado el mismo valor, el voto se anula; si era distinto, se actualiza.

---

```csharp
public async Task<IActionResult> QuitarVoto(int comentarioId)
```

Elimina el voto del usuario autenticado sobre un comentario especifico. Retorna el nuevo conteo de votos del comentario tras la eliminacion.

---

```csharp
public async Task<IActionResult> ObtenerVotoUsuario(int comentarioId)
```

Retorna el voto actual del usuario autenticado sobre un comentario especifico. El valor puede ser 1 (upvote), -1 (downvote) o null si no ha votado.

---

## VueloAgenciaController

> Controlador de busqueda de vuelos para agencias de viaje externas. Expone el endpoint de busqueda que aplica el descuento negociado de la agencia a las tarifas retornadas. Requiere autenticacion de agencia mediante AgenciaAuthMiddleware.

```csharp
public async Task<IActionResult> BuscarVuelos([FromBody] BuscarVueloAgenciaDTO dto)
```

Busca vuelos disponibles segun los criterios del DTO y aplica el descuento de la agencia autenticada a las tarifas retornadas. Requiere autenticacion de agencia.

---

## VuelosController

> Controlador principal de vuelos del portal web. Expone endpoints publicos para buscar vuelos disponibles por criterios de origen, destino, fecha y pasajeros, y para realizar una busqueda general por texto libre. No requiere autenticacion en ningun endpoint.

```csharp
public async Task<IActionResult> BusquedaGeneral([FromQuery] string query)
```

Realiza una busqueda general de vuelos por texto libre (numero de vuelo, ciudad, codigo de aeropuerto, etc.). Requiere al menos 2 caracteres en el termino de busqueda. Endpoint publico, no requiere autenticacion.

---

```csharp
public async Task<IActionResult> BuscarVuelos([FromBody] BuscarVueloDTO dto)
```

Busca vuelos disponibles entre origen y destino para la fecha y cantidad de pasajeros indicados en el DTO. Si el usuario esta autenticado, registra la busqueda para metricas. Endpoint publico, no requiere autenticacion.

---
