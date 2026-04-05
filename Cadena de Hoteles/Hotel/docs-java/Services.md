# Services

## AdminBusquedaService

> Service para el modulo de busquedas del panel de administracion. Maneja listado paginado, resumen para dashboard y exportacion por correo.

```java
public Map<String, Object> listar(
```

Retorna una pagina de busquedas aplicando los filtros dados.

- **Param** `destino` - nombre del destino a filtrar, o null para ignorar.
- **Param** `usuarioAgencia` - nombre de usuario o agencia a filtrar, o null para ignorar.
- **Param** `tipo` - "web", "rest" o "todos".
- **Param** `fechaDesdeStr` - fecha de inicio en formato ISO (yyyy-MM-dd), o null.
- **Param** `fechaHastaStr` - fecha de fin en formato ISO (yyyy-MM-dd), o null.
- **Param** `pagina` - numero de pagina (base 1).
- **Param** `porPagina` - cantidad de registros por pagina.
- **Returns** - mapa con "busquedas" (lista) y "total" (int).

---

```java
public Map<String, Object> resumen()
```

Retorna un resumen de busquedas para el dashboard de admin. Incluye totales por tipo, busquedas por dia y top destinos.

- **Returns** - mapa con totalWeb, totalRest, porDia y topDestinos.

---

```java
public void exportar(String email, String destino, String usuarioAgencia,
```

Genera un reporte HTML con las busquedas filtradas y lo envia por correo.

- **Param** `email` - destinatario del reporte.
- **Param** `destino` - filtro de destino, o null.
- **Param** `usuarioAgencia` - filtro de usuario o agencia, o null.
- **Param** `tipo` - "web", "rest" o "todos".
- **Param** `fechaDesdeStr` - fecha de inicio, o null.
- **Param** `fechaHastaStr` - fecha de fin, o null.

---

## AdminReservacionService

> Service para la gestion de reservaciones desde el panel de administracion. Permite listar todas las reservaciones y cancelarlas con validacion de estado.

```java
public List<Map<String, Object>> listarTodas()
```

Retorna todas las reservaciones registradas en el sistema.

- **Returns** - lista de mapas con los datos de cada reservacion.

---

```java
public void cancelarReservacion(int reservacionId, String motivo)
```

Cancela una reservacion si su estado actual lo permite. Solo se pueden cancelar reservaciones en estado Pendiente (1) o Confirmada (2).

- **Param** `reservacionId` - ID de la reservacion a cancelar.
- **Param** `motivo` - razon de la cancelacion.
- **Throws** `IllegalArgumentException` - si la reservacion no existe o su estado no permite cancelacion.

---

## AgenciaService

> Service para la gestion de agencias de viaje. Cubre operaciones del webservice (usuario dueno) y del panel de administracion.

```java
public List<AgenciaDTO> listarPorUsuario(int usuarioId)
```

Retorna las agencias asociadas a un usuario especifico.

- **Param** `usuarioId` - ID del usuario dueno de las agencias.
- **Returns** - lista de agencias del usuario.

---

```java
public AgenciaDTO crear(int usuarioId, CrearAgenciaRequestDTO req)
```

Crea una nueva agencia vinculada al usuario dado.

- **Param** `usuarioId` - ID del usuario que crea la agencia.
- **Param** `req` - datos de la nueva agencia.
- **Returns** - DTO con los datos de la agencia creada.

---

```java
public void cambiarEstado(int agenciaId, int usuarioId, int nuevoEstadoId)
```

Cambia el estado de una agencia entre Activo (1) y Cerrado (2).

- **Param** `agenciaId` - ID de la agencia a modificar.
- **Param** `usuarioId` - ID del usuario dueno de la agencia.
- **Param** `nuevoEstadoId` - nuevo estado: 1 para Activo, 2 para Cerrado.
- **Throws** `IllegalArgumentException` - si el estado no es 1 ni 2.

---

```java
public void eliminar(int agenciaId, int usuarioId)
```

Elimina una agencia del usuario dado.

- **Param** `agenciaId` - ID de la agencia a eliminar.
- **Param** `usuarioId` - ID del usuario dueno de la agencia.

---

```java
public List<AgenciaDTO> listarTodas()
```

Retorna todas las agencias registradas en el sistema. Solo disponible para administradores.

- **Returns** - lista completa de agencias.

---

```java
public void editar(int agenciaId, EditarAgenciaRequestDTO req)
```

Edita los datos de una agencia existente. Solo disponible para administradores.

- **Param** `agenciaId` - ID de la agencia a editar.
- **Param** `req` - datos actualizados de la agencia.

---

## AuthService

> Service de autenticacion de usuarios. Valida credenciales y genera el token JWT para la sesion.

```java
public record LoginResultado(String token, LoginResponseDTO respuesta)
```

Agrupa el token JWT y los datos de respuesta tras un login exitoso.

- **Param** `token` - JWT generado para la sesion.
- **Param** `respuesta` - datos del usuario autenticado.

---

## BusquedaAgenciaService

> Service de busqueda de hoteles para agencias de viaje. Aplica el descuento de la agencia a los precios y calcula combinaciones de habitaciones para grupos grandes.

```java
public List<HotelResultadoDTO> buscar(BusquedaRequestDTO request, int usuarioId)
```

Busca hoteles disponibles para una agencia autenticada por sesion de usuario. Valida que el usuario tenga una agencia activa, obtiene su descuento, guarda la busqueda y retorna los hoteles con precios ya descontados.

- **Param** `request` - criterios de busqueda: ciudad, pais, fechas y cantidad de personas.
- **Param** `usuarioId` - ID del usuario dueno de la agencia.
- **Returns** - lista de hoteles con tipos de habitacion, amenidades y combinaciones disponibles.
- **Throws** `IllegalArgumentException` - si el usuario no tiene agencia activa o la ciudad no existe.

---

```java
public List<HotelResultadoDTO> buscarPorToken(BusquedaRequestDTO request, String token)
```

Busca hoteles disponibles para una agencia autenticada por token de API. Funciona igual que buscar() pero valida la agencia con un token en lugar de sesion de usuario. La busqueda se guarda sin vincular a ningun usuario.

- **Param** `request` - criterios de busqueda: ciudad, pais, fechas y cantidad de personas.
- **Param** `token` - token de acceso de la agencia.
- **Returns** - lista de hoteles con tipos de habitacion, amenidades y combinaciones disponibles.
- **Throws** `IllegalArgumentException` - si el token es invalido, la agencia no esta activa o la ciudad no existe.

---

## BusquedaService

> Service de busqueda de hoteles para usuarios web. Localiza hoteles por ciudad, guarda el registro de busqueda y calcula combinaciones de habitaciones para grupos grandes.

```java
public List<HotelResultadoDTO> buscar(BusquedaRequestDTO request, Integer usuarioId)
```

Busca hoteles disponibles segun los criterios del request. Valida que la ciudad exista, guarda la busqueda y enriquece cada hotel con imagenes, amenidades, tipos de habitacion y combinaciones.

- **Param** `request` - criterios: ciudad, pais, fechas y cantidad de personas.
- **Param** `usuarioId` - ID del usuario que realiza la busqueda, puede ser null si no hay sesion.
- **Returns** - lista de hoteles con toda la informacion necesaria para mostrar resultados.
- **Throws** `IllegalArgumentException` - si la ciudad no existe en la base de datos.

---

## CancelacionService

> Service para cancelacion de reservaciones. Maneja cancelaciones de usuarios web y de agencias, validando estado y la regla de las 24 horas antes del check-in.

```java
public void cancelarReservacion(int reservacionId, int usuarioId, String motivoCancelacion)
```

Cancela una reservacion de un usuario web. Verifica que la reservacion exista, pertenezca al usuario, tenga un estado valido y cumpla la regla de las 24 horas. o faltan menos de 24 horas para el check-in.

- **Param** `reservacionId` - ID de la reservacion a cancelar.
- **Param** `usuarioId` - ID del usuario dueno de la reservacion.
- **Param** `motivoCancelacion` - razon de la cancelacion.
- **Throws** `IllegalArgumentException` - si la reservacion no existe, el estado no lo permite

---

```java
public PuedeCancelarDTO puedeCancelar(int reservacionId, int agenciaId)
```

Verifica si una reservacion de agencia puede cancelarse sin ejecutar la cancelacion. Util para que el frontend consulte antes de mostrar el boton de cancelar.

- **Param** `reservacionId` - ID de la reservacion a evaluar.
- **Param** `agenciaId` - ID de la agencia duena de la reservacion.
- **Returns** - DTO con el resultado (puede o no cancelar) y un mensaje explicativo.

---

```java
public void cancelarReservacionAgencia(int reservacionId, int agenciaId, String motivo)
```

Cancela una reservacion perteneciente a una agencia. Aplica las mismas validaciones de estado y plazo que cancelarReservacion. o faltan menos de 24 horas para el check-in.

- **Param** `reservacionId` - ID de la reservacion a cancelar.
- **Param** `agenciaId` - ID de la agencia duena de la reservacion.
- **Param** `motivo` - razon de la cancelacion.
- **Throws** `IllegalArgumentException` - si la reservacion no existe, el estado no lo permite

---

## ComentarioService

> Service para la gestion de comentarios y resenas de hoteles. Diferencia entre comentarios de hotel (con resena) y respuestas a comentarios (sin resena).

```java
public ComentarioResponseDTO agregarComentario(ComentarioRequestDTO request, int usuarioId)
```

Agrega un comentario o respuesta segun el contenido del request. Si tiene comentarioPadreId es una respuesta, si no es un comentario de hotel con resena. Valida contenido, resena y que el usuario no haya comentado ya en ese hotel. o el usuario ya tiene resena en ese hotel.

- **Param** `request` - datos del comentario: contenido, resena, hotelId y comentarioPadreId opcional.
- **Param** `usuarioId` - ID del usuario que escribe el comentario.
- **Returns** - DTO con los datos del comentario recien creado.
- **Throws** `IllegalArgumentException` - si el contenido es invalido, la resena esta fuera de rango

---

```java
public List<ComentarioResponseDTO> obtenerComentariosPorUsuario(int usuarioId)
```

Retorna todos los comentarios escritos por un usuario especifico.

- **Param** `usuarioId` - ID del usuario.
- **Returns** - lista de comentarios del usuario.

---

```java
public List<ComentarioResponseDTO> obtenerComentariosPorHotel(int hotelId)
```

Retorna todos los comentarios de un hotel especifico.

- **Param** `hotelId` - ID del hotel.
- **Returns** - lista de comentarios del hotel.

---

## DestinosService

> Service para obtener el listado de destinos disponibles. Retorna todos los hoteles con sus imagenes para la pagina de destinos.

```java
public List<HotelResultadoDTO> obtenerDestinos()
```

Obtiene todos los hoteles disponibles como destinos y les asigna sus imagenes.

- **Returns** - lista de hoteles con sus IDs de imagenes cargados.

---

## DownsService

> Service para la gestion de downs (votos) en comentarios. Un down puede ser positivo (1) o negativo (-1) y cada usuario solo puede tener un down por comentario.

```java
public void agregarDown(int comentarioId, int usuarioId, int valor)
```

Agrega un down de un usuario a un comentario. Valida que el valor sea 1 o -1, que el comentario exista y que el usuario no haya votado ya en ese comentario. o el usuario ya tiene un down en ese comentario.

- **Param** `comentarioId` - ID del comentario a votar.
- **Param** `usuarioId` - ID del usuario que vota.
- **Param** `valor` - 1 para voto positivo, -1 para voto negativo.
- **Throws** `IllegalArgumentException` - si el valor es invalido, el comentario no existe

---

```java
public void eliminarDown(int comentarioId, int usuarioId)
```

Elimina el down de un usuario en un comentario y ajusta el contador.

- **Param** `comentarioId` - ID del comentario.
- **Param** `usuarioId` - ID del usuario cuyo down se va a eliminar.
- **Throws** `IllegalArgumentException` - si el usuario no tiene down en ese comentario.

---

```java
public void actualizarDown(int comentarioId, int usuarioId, int nuevoValor)
```

Cambia el valor del down existente de un usuario en un comentario. Elimina el voto anterior e inserta el nuevo ajustando el contador en ambos pasos. o el nuevo valor es igual al actual.

- **Param** `comentarioId` - ID del comentario.
- **Param** `usuarioId` - ID del usuario que actualiza su voto.
- **Param** `nuevoValor` - nuevo valor: 1 o -1.
- **Throws** `IllegalArgumentException` - si el valor es invalido, el usuario no tiene down

---

```java
public List<DownResponseDTO> obtenerDownsDeUsuario(int usuarioId)
```

Retorna todos los downs registrados por un usuario.

- **Param** `usuarioId` - ID del usuario.
- **Returns** - lista de downs del usuario.

---

```java
public List<DownResponseDTO> obtenerDownsDeUsuarioPorHotel(int usuarioId, int hotelId)
```

Retorna los downs de un usuario filtrados por hotel. Util para saber como voto el usuario en los comentarios de un hotel especifico.

- **Param** `usuarioId` - ID del usuario.
- **Param** `hotelId` - ID del hotel a filtrar.
- **Returns** - lista de downs del usuario en comentarios de ese hotel.

---

## EmailReservacionService

> Service para enviar el resumen de una reservacion por correo electronico. Construye un HTML con los detalles de la reservacion y lo envia al correo del usuario.

```java
public void enviarCorreoReservacion(int reservacionId, int usuarioId)
```

Envia el correo de confirmacion de una reservacion al usuario. Valida que la reservacion pertenezca al usuario, obtiene su correo y construye el HTML con los detalles y factura antes de enviarlo. no se encuentra el correo o la reservacion no tiene detalles.

- **Param** `reservacionId` - ID de la reservacion a enviar.
- **Param** `usuarioId` - ID del usuario destinatario.
- **Throws** `IllegalArgumentException` - si la reservacion no pertenece al usuario,

---

## ExpiracionService

> Servicio en segundo plano que expira reservaciones pendientes vencidas. Corre en un hilo separado cada minuto desde que arranca el servidor.

```java
public void iniciar()
```

Arranca el hilo programado que revisa y expira reservaciones cada minuto. El primer ciclo inicia un minuto despues de llamar a este metodo.

---

```java
public void detener()
```

Detiene el hilo del scheduler al apagar el servidor. Se llama desde el ShutdownHook registrado en Main.

---

## HandshakeService

> Service para el proceso de handshake entre el sistema y una agencia externa. Valida la URL de la agencia, genera un token de salida y persiste ambos tokens.

```java
public HandshakeResponseDTO procesarHandshake(HandshakeRequestDTO dto)
```

Procesa el handshake de una agencia externa. Busca la agencia por su URL, genera un token de respuesta y guarda el token de entrada y el de salida en la base de datos.

- **Param** `dto` - datos del handshake con la URL de la agencia y el token de entrada.
- **Returns** - DTO con el token de salida generado para que la agencia lo use en requests futuros.
- **Throws** `IllegalArgumentException` - si no existe una agencia con esa URL o si los tokens no se pudieron guardar.

---

## HotelAgenciaService

> Service para exponer informacion de hoteles al modulo de agencias.

```java
public List<HotelAgenciaDTO> obtenerHotelesParaAgencia()
```

Retorna la lista de hoteles disponibles para ser consultados por agencias.

- **Returns** - lista de hoteles con los datos relevantes para el contexto de agencia.

---

## HotelService

> Service principal para la gestion de hoteles desde el panel de administracion. Cubre catalogos, hoteles, amenidades, habitaciones, imagenes y metricas.

```java
public List<AmenidadDTO> listarAmenidades()
```

Retorna todas las amenidades disponibles en el catalogo.

- **Returns** - lista de amenidades.

---

```java
public List<PaisDTO> listarPaises()
```

Retorna todos los paises registrados en el sistema.

- **Returns** - lista de paises.

---

```java
public List<CiudadDTO> listarCiudades()
```

Retorna todas las ciudades registradas en el sistema.

- **Returns** - lista de ciudades.

---

```java
public Map<String, Object> crearAmenidad(String nombre)
```

Crea una nueva amenidad en el catalogo.

- **Param** `nombre` - nombre de la amenidad, no puede ser nulo ni vacio.
- **Returns** - mapa con el ID, nombre y mensaje de confirmacion.
- **Throws** `IllegalArgumentException` - si el nombre es nulo o vacio.

---

```java
public List<HotelAdminDTO> listarTodos()
```

Retorna todos los hoteles con su cantidad de habitaciones e IDs de imagenes.

- **Returns** - lista de hoteles con datos enriquecidos para el panel de admin.

---

```java
public Map<String, Object> crearHotel(CrearHotelRequestDTO req)
```

Crea un nuevo hotel, buscando o creando el pais y ciudad si no existen.

- **Param** `req` - datos del hotel: nombre, rating, estado, ciudad y pais.
- **Returns** - mapa con el ID del hotel creado y mensaje de confirmacion.
- **Throws** `IllegalArgumentException` - si algun campo obligatorio es invalido.

---

```java
public void editarHotel(int hotelId, EditarHotelRequestDTO req)
```

Edita los datos de un hotel existente.

- **Param** `hotelId` - ID del hotel a editar.
- **Param** `req` - datos actualizados del hotel.
- **Throws** `IllegalArgumentException` - si el hotel no existe o los datos son invalidos.

---

```java
public void eliminarHotel(int hotelId)
```

Elimina un hotel del sistema.

- **Param** `hotelId` - ID del hotel a eliminar.
- **Throws** `IllegalArgumentException` - si el hotel no existe.

---

```java
public List<HotelAmenidadDTO> listarAmenidadesHotel(int hotelId)
```

Retorna las amenidades de un hotel con sus IDs de imagenes.

- **Param** `hotelId` - ID del hotel.
- **Returns** - lista de amenidades del hotel.
- **Throws** `IllegalArgumentException` - si el hotel no existe.

---

```java
public Map<String, Object> agregarAmenidadHotel(int hotelId, AgregarAmenidadRequestDTO req)
```

Agrega una amenidad del catalogo a un hotel. Valida que el hotel exista y que no tenga ya esa amenidad asignada. o el hotel ya tiene esa amenidad.

- **Param** `hotelId` - ID del hotel.
- **Param** `req` - datos con el ID de amenidad y descripcion opcional.
- **Returns** - mapa con el ID del registro creado y mensaje de confirmacion.
- **Throws** `IllegalArgumentException` - si el hotel no existe, la amenidad es invalida

---

```java
public void actualizarAmenidadHotel(int hotelAmenidadId, AgregarAmenidadRequestDTO req)
```

Actualiza la descripcion de una amenidad asignada a un hotel.

- **Param** `hotelAmenidadId` - ID del registro hotel-amenidad.
- **Param** `req` - datos con la nueva descripcion.

---

```java
public void eliminarAmenidadHotel(int hotelAmenidadId)
```

Elimina una amenidad asignada a un hotel.

- **Param** `hotelAmenidadId` - ID del registro hotel-amenidad a eliminar.

---

```java
public Map<String, Object> agregarImagenAmenidad(int hotelAmenidadId, String base64)
```

Agrega una imagen a una amenidad de hotel decodificando el base64 recibido.

- **Param** `hotelAmenidadId` - ID del registro hotel-amenidad.
- **Param** `base64` - imagen codificada en base64, con o sin prefijo data URI.
- **Returns** - mapa con el ID de la imagen creada y mensaje de confirmacion.

---

```java
public void eliminarImagenAmenidad(int imagenId)
```

Elimina una imagen de amenidad por su ID.

- **Param** `imagenId` - ID de la imagen a eliminar.

---

```java
public Map<String, Object> agregarImagenHotel(int hotelId, String base64)
```

Agrega una imagen al hotel decodificando el base64 recibido.

- **Param** `hotelId` - ID del hotel.
- **Param** `base64` - imagen codificada en base64.
- **Returns** - mapa con el ID de la imagen creada y mensaje de confirmacion.
- **Throws** `IllegalArgumentException` - si el hotel no existe.

---

```java
public void eliminarImagenHotel(int imagenId)
```

Elimina una imagen de hotel por su ID.

- **Param** `imagenId` - ID de la imagen a eliminar.

---

```java
public List<HabitacionAdminDTO> listarHabitaciones(int hotelId)
```

Retorna las habitaciones de un hotel con sus IDs de imagenes.

- **Param** `hotelId` - ID del hotel.
- **Returns** - lista de habitaciones con datos para el panel de admin.
- **Throws** `IllegalArgumentException` - si el hotel no existe.

---

```java
public Map<String, Object> crearHabitacion(CrearHabitacionRequestDTO req)
```

Crea una nueva habitacion en un hotel. El numero de habitacion se auto-asigna en el repositorio.

- **Param** `req` - datos de la habitacion: hotelId, tipo, descripcion y estado.
- **Returns** - mapa con el ID de la habitacion creada y mensaje de confirmacion.
- **Throws** `IllegalArgumentException` - si el hotel no existe, el tipo es invalido o el estado no es valido.

---

```java
public void editarHabitacion(int habitacionId, EditarHabitacionRequestDTO req)
```

Edita los datos de una habitacion existente.

- **Param** `habitacionId` - ID de la habitacion a editar.
- **Param** `req` - datos actualizados de la habitacion.
- **Throws** `IllegalArgumentException` - si la habitacion no existe, el tipo es invalido o el estado no es valido.

---

```java
public void eliminarHabitacion(int habitacionId)
```

Elimina una habitacion del sistema.

- **Param** `habitacionId` - ID de la habitacion a eliminar.
- **Throws** `IllegalArgumentException` - si la habitacion no existe.

---

```java
public Map<String, Object> agregarImagenHabitacion(int habitacionId, String base64)
```

Agrega una imagen a una habitacion decodificando el base64 recibido.

- **Param** `habitacionId` - ID de la habitacion.
- **Param** `base64` - imagen codificada en base64.
- **Returns** - mapa con el ID de la imagen creada y mensaje de confirmacion.
- **Throws** `IllegalArgumentException` - si la habitacion no existe.

---

```java
public void eliminarImagenHabitacion(int imagenId)
```

Elimina una imagen de habitacion por su ID.

- **Param** `imagenId` - ID de la imagen a eliminar.

---

```java
public List<Map<String, Object>> listarTodasReservaciones()
```

Retorna todas las reservaciones del sistema para el panel de admin.

- **Returns** - lista de reservaciones con sus datos principales.

---

```java
public Map<String, Object> obtenerMetricas()
```

Retorna metricas generales del sistema: reservaciones, ingresos, ocupacion, etc.

- **Returns** - mapa con los valores de cada metrica.

---

## ImagenService

> Service para recuperar imagenes almacenadas en la base de datos. Sirve los bytes de imagenes de hoteles, habitaciones y amenidades.

```java
public byte[] obtenerImagenHotel(int id)
```

Retorna los bytes de una imagen de hotel.

- **Param** `id` - ID de la imagen.
- **Returns** - array de bytes de la imagen.

---

```java
public byte[] obtenerImagenHabitacion(int id)
```

Retorna los bytes de una imagen de habitacion.

- **Param** `id` - ID de la imagen.
- **Returns** - array de bytes de la imagen.

---

```java
public byte[] obtenerImagenAmenidad(int id)
```

Retorna los bytes de una imagen de amenidad.

- **Param** `id` - ID de la imagen.
- **Returns** - array de bytes de la imagen.

---

## PagoAgenciaService

> Service para procesar pagos de reservaciones realizadas por agencias. Valida los datos de facturacion, confirma la reservacion y genera la factura.

```java
public PagoResponseDTO procesarPago(int reservacionId, int agenciaId, PagoAgenciaRequestDTO request)
```

Procesa el pago de una reservacion de agencia. Valida NIT y codigo postal, verifica que la reservacion pertenezca a la agencia y este en estado Pendiente, luego la confirma y genera la factura. o su estado no permite el pago.

- **Param** `reservacionId` - ID de la reservacion a pagar.
- **Param** `agenciaId` - ID de la agencia duena de la reservacion.
- **Param** `request` - datos de facturacion: NIT y codigo postal.
- **Returns** - DTO con los datos de la factura generada.
- **Throws** `IllegalArgumentException` - si los datos son invalidos, la reservacion no existe

---

## PagoService

> Service para procesar pagos de reservaciones de usuarios web. Valida la reservacion, simula la verificacion de tarjeta y genera la factura.

```java
public PagoResponseDTO procesarPago(int reservacionId, int usuarioId, PagoRequestDTO request)
```

Procesa el pago de una reservacion de usuario web. Verifica que la reservacion exista y este pendiente, valida la tarjeta en memoria, confirma la reservacion y genera la factura. o su estado no permite el pago.

- **Param** `reservacionId` - ID de la reservacion a pagar.
- **Param** `usuarioId` - ID del usuario dueno de la reservacion.
- **Param** `request` - datos de pago: tarjeta, NIT y codigo postal.
- **Returns** - DTO con los datos de la factura generada.
- **Throws** `IllegalArgumentException` - si la reservacion no existe, no pertenece al usuario

---

## PdfReservacionService

> Service para la generacion de PDFs de reservaciones. Valida que la reservacion pertenezca al usuario y delega la construccion del PDF a PdfHelper.

```java
public byte[] generarPdf(int reservacionId, int usuarioId)
```

Genera el PDF de una reservacion para un usuario especifico. Verifica que la reservacion pertenezca al usuario, obtiene los detalles y la factura, y construye el PDF con esos datos. o no tiene detalles registrados.

- **Param** `reservacionId` - ID de la reservacion a exportar.
- **Param** `usuarioId` - ID del usuario dueno de la reservacion.
- **Returns** - array de bytes del PDF generado.
- **Throws** `IllegalArgumentException` - si la reservacion no pertenece al usuario

---

## ReservacionAgenciaService

> Service para la gestion de reservaciones realizadas por agencias. Maneja la creacion con descuento aplicado, consulta de reservaciones y expiracion manual de reservaciones pendientes.

```java
public ReservacionAgenciaResponseDTO crearReservacion(ReservacionRequestDTO request, int agenciaId)
```

Crea una nueva reservacion para una agencia aplicando su descuento. Valida disponibilidad de cada habitacion, calcula totales con el descuento de la agencia, genera el numero de reservacion y persiste los detalles. La reservacion expira automaticamente en 15 minutos si no se paga. las fechas son invalidas o alguna habitacion no esta disponible.

- **Param** `request` - datos de la reservacion: lista de habitaciones con fechas y personas.
- **Param** `agenciaId` - ID de la agencia que realiza la reservacion.
- **Returns** - DTO con los datos de la reservacion creada y el desglose por habitacion.
- **Throws** `IllegalArgumentException` - si la agencia no esta activa, no hay habitaciones,

---

```java
public List<ReservacionDetalleDTO> obtenerReservaciones(int agenciaId)
```

Retorna todas las reservaciones asociadas a una agencia.

- **Param** `agenciaId` - ID de la agencia.
- **Returns** - lista de reservaciones con sus detalles.

---

```java
public void expirarReservacion(int reservacionId, int agenciaId)
```

Expira manualmente una reservacion pendiente de una agencia. Solo aplica si la reservacion pertenece a la agencia y esta en estado Pendiente. o no esta en estado pendiente.

- **Param** `reservacionId` - ID de la reservacion a expirar.
- **Param** `agenciaId` - ID de la agencia duena de la reservacion.
- **Throws** `IllegalArgumentException` - si la reservacion no existe, no pertenece a la agencia

---

```java
public List<ReservacionDetalleDTO> obtenerDetalleReservacion(int reservacionId, int agenciaId)
```

Retorna el detalle completo de una reservacion de agencia con imagenes incluidas.

- **Param** `reservacionId` - ID de la reservacion.
- **Param** `agenciaId` - ID de la agencia duena de la reservacion.
- **Returns** - lista de DTOs con detalles de habitaciones, hotel e imagenes.
- **Throws** `IllegalArgumentException` - si la reservacion no existe o no pertenece a la agencia.

---

## ReservacionService

> Service para la gestion de reservaciones de habitaciones. Maneja la creacion de reservaciones con validacion de fechas, disponibilidad y calculo de precios, incluyendo cargos por personas extra.

```java
public ReservacionResponseDTO crearReservacion(ReservacionRequestDTO request, int usuarioId)
```

Crea una nueva reservacion para el usuario indicado. Valida fechas, disponibilidad de habitaciones y capacidad maxima antes de persistir. El numero de reservacion se genera automaticamente con el prefijo MIKU-. La reservacion queda en estado pendiente y expira en 10 minutos si no se confirma. hay traslape de disponibilidad, o se excede la capacidad permitida.

- **Param** `request` - datos de la reservacion, incluyendo la lista de habitaciones solicitadas.
- **Param** `usuarioId` - ID del usuario que realiza la reservacion.
- **Returns** - DTO con los datos de la reservacion creada.
- **Throws** `IllegalArgumentException` - si no se incluyen habitaciones, las fechas son invalidas,

---

```java
public List<ReservacionDetalleDTO> obtenerReservaciones(int usuarioId)
```

Obtiene todas las reservaciones de un usuario con sus imagenes asociadas. Por cada reservacion se cargan las imagenes del hotel y de la habitacion correspondiente.

- **Param** `usuarioId` - ID del usuario del que se quieren obtener las reservaciones.
- **Returns** - lista de DTOs con el detalle de cada reservacion e imagenes incluidas.

---

## SesionService

> Service para la gestion de la sesion activa del usuario. Construye el estado de sesion a partir de los datos del token JWT ya validado por el middleware.

```java
public SesionDTO obtenerSesion(int usuarioId, String username, int rolId)
```

Construye el DTO de sesion para un usuario autenticado. Los datos del usuario vienen del token ya validado por el middleware, y se complementa con el nombre del rol consultado en base de datos.

- **Param** `usuarioId` - ID del usuario autenticado.
- **Param** `username` - nombre de usuario extraido del token.
- **Param** `rolId` - ID del rol del usuario.
- **Returns** - DTO con los datos de sesion y autenticado en true.

---

## UsuarioService

> Service para la gestion de usuarios del sistema. Cubre el registro, validacion de campos unicos, consulta de perfil, actualizacion de datos personales y operaciones administrativas de rol.

```java
public UsuarioValidacionResponseDTO validarDisponibilidad(UsuarioValidacionRequestDTO request)
```

Verifica si el username, correo o pasaporte ya estan registrados en el sistema.

- **Param** `request` - DTO con los campos a validar.
- **Returns** - DTO indicando cuales campos ya existen.

---

```java
public int registrarUsuario(UsuarioValidacionRequestDTO request)
```

Registra un nuevo usuario en el sistema. Valida que no existan campos duplicados, hashea la contrasena, resuelve pais y ciudad (creandolos si no existen), asigna nacionalidades y envia un correo de bienvenida. El correo no bloquea el registro si falla.

- **Param** `request` - datos del usuario a registrar.
- **Returns** - ID del usuario recien creado.
- **Throws** `CamposDuplicadosException` - si el username, correo o pasaporte ya estan en uso.

---

```java
public UsuarioPerfilResponseDTO obtenerPerfil(int usuarioId)
```

Retorna el perfil completo de un usuario incluyendo sus nacionalidades.

- **Param** `usuarioId` - ID del usuario a consultar.
- **Returns** - DTO con los datos del perfil y lista de nacionalidades.
- **Throws** `RuntimeException` - si el usuario no existe.

---

```java
public void cambiarTelefono(int usuarioId, String nuevoTelefono)
```

Actualiza el numero de telefono de un usuario.

- **Param** `usuarioId` - ID del usuario a actualizar.
- **Param** `nuevoTelefono` - nuevo numero de telefono; no puede ser nulo ni vacio.
- **Throws** `IllegalArgumentException` - si el telefono es nulo o esta en blanco.

---

```java
public void cambiarContrasena(int usuarioId, String contrasenaActual, String contrasenaNueva)
```

Cambia la contrasena de un usuario previa verificacion de la contrasena actual.

- **Param** `usuarioId` - ID del usuario.
- **Param** `contrasenaActual` - contrasena actual en texto plano para verificacion.
- **Param** `contrasenaNueva` - nueva contrasena en texto plano que sera hasheada.
- **Throws** `CredencialesInvalidasException` - si la contrasena actual no coincide.

---

```java
public List<UsuarioAdminDTO> listarTodosUsuarios()
```

Retorna todos los usuarios registrados junto con su rol asignado. Uso exclusivo del panel de administracion.

- **Returns** - lista de DTOs con los datos de cada usuario y su rol.

---

```java
public void cambiarRol(int usuarioId, int nuevoRolId)
```

Cambia el rol de un usuario. Solo se aceptan los roles definidos en el sistema.

- **Param** `usuarioId` - ID del usuario al que se le cambiara el rol.
- **Param** `nuevoRolId` - ID del nuevo rol: 1 (Usuario), 2 (Administrador) o 3 (Webservice).
- **Throws** `IllegalArgumentException` - si el ID de rol no corresponde a ninguno de los roles validos.

---
