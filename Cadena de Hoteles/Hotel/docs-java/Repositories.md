# Repositories

## AdminBusquedaRepository

> Repository para consultas del modulo de reportes de busquedas en el panel de administracion. Soporta filtros por destino, usuario o agencia, tipo de busqueda y rango de fechas.

```java
public List<Map<String, Object>> listar(
```

Retorna una pagina de busquedas aplicando los filtros indicados. Todos los parametros de filtro son opcionales; si son null o vacios se ignoran.

- **Param** `destino` - nombre de ciudad a filtrar con LIKE, o null para no filtrar.
- **Param** `usuarioAgencia` - username del usuario o nombre de la agencia a filtrar, o null.
- **Param** `tipoBusquedaId` - 1 para busquedas web, 2 para busquedas REST, null para todas.
- **Param** `fechaDesde` - fecha minima de la busqueda, o null para no filtrar.
- **Param** `fechaHasta` - fecha maxima de la busqueda, o null para no filtrar.
- **Param** `offset` - numero de filas a saltar para la paginacion.
- **Param** `porPagina` - cantidad de filas a retornar por pagina.
- **Returns** - lista de mapas con los datos de cada busqueda encontrada.

---

```java
public int contar(String destino, String usuarioAgencia, Integer tipoBusquedaId,
```

Cuenta el total de busquedas que coinciden con los filtros indicados. Se usa para calcular el total de paginas en la paginacion.

- **Param** `destino` - nombre de ciudad a filtrar con LIKE, o null para no filtrar.
- **Param** `usuarioAgencia` - username del usuario o nombre de la agencia a filtrar, o null.
- **Param** `tipoBusquedaId` - 1 para busquedas web, 2 para busquedas REST, null para todas.
- **Param** `fechaDesde` - fecha minima de la busqueda, o null para no filtrar.
- **Param** `fechaHasta` - fecha maxima de la busqueda, o null para no filtrar.
- **Returns** - total de busquedas que coinciden con los filtros.

---

```java
public int contarPorTipo(Integer tipoBusquedaId)
```

Cuenta el total de busquedas filtradas por tipo. Si tipoBusquedaId es null retorna el total sin filtro de tipo.

- **Param** `tipoBusquedaId` - 1 para web, 2 para REST, null para todas.
- **Returns** - total de busquedas del tipo indicado.

---

```java
public List<Map<String, Object>> busquedasPorDia()
```

Retorna el conteo de busquedas agrupado por dia para los ultimos 30 dias. Cada elemento del resultado contiene la fecha y el total de busquedas de ese dia.

- **Returns** - lista de mapas con los campos "dia" (YYYY-MM-DD) y "total".

---

```java
public List<Map<String, Object>> topDestinos()
```

Retorna los 10 destinos mas buscados ordenados por frecuencia descendente. Cada elemento contiene el nombre de la ciudad y el total de busquedas.

- **Returns** - lista de mapas con los campos "nombre" y "total".

---

```java
public List<Map<String, Object>> exportar(
```

Retorna todas las busquedas que coinciden con los filtros sin paginacion. Se usa para generar el reporte completo a exportar por correo.

- **Param** `destino` - nombre de ciudad a filtrar con LIKE, o null para no filtrar.
- **Param** `usuarioAgencia` - username del usuario o nombre de la agencia a filtrar, o null.
- **Param** `tipoBusquedaId` - 1 para busquedas web, 2 para busquedas REST, null para todas.
- **Param** `fechaDesde` - fecha minima de la busqueda, o null para no filtrar.
- **Param** `fechaHasta` - fecha maxima de la busqueda, o null para no filtrar.
- **Returns** - lista de mapas con los datos de cada busqueda encontrada.

---

## AdminReservacionRepository

> Repository para la gestion de reservaciones desde el panel de administracion. Maneja la consulta de todas las reservaciones y la logica de cancelacion a nivel de base de datos.

```java
public List<Map<String, Object>> listarTodas()
```

Retorna todas las reservaciones registradas en el sistema con sus datos completos. Usa subqueries escalares para garantizar una sola fila por reservacion, evitando duplicados cuando una reservacion tiene habitaciones en distintos hoteles.

- **Returns** - lista de mapas con los datos de cada reservacion, ordenadas por fecha de creacion descendente.

---

```java
public Object[] obtenerReservacion(int reservacionId)
```

Busca una reservacion por su ID y retorna sus datos basicos de estado.

- **Param** `reservacionId` - ID de la reservacion a buscar.
- **Returns** - arreglo con {ID, EstadoID, Estado} o null si no existe.

---

```java
public void cancelarReservacion(int reservacionId, String motivo)
```

Actualiza el estado de una reservacion a Cancelada (EstadoID = 4) y registra el motivo. Si el motivo es nulo o vacio, se guarda un texto por defecto.

- **Param** `reservacionId` - ID de la reservacion a cancelar.
- **Param** `motivo` - razon de la cancelacion ingresada por el administrador.

---

## AgenciaRepository

> Repository para la gestion de agencias de viaje. Cubre operaciones de consulta, creacion, edicion, cambio de estado y eliminacion, tanto para el panel de administracion como para usuarios webservice.

```java
public List<AgenciaDTO> listarTodas()
```

Retorna todas las agencias registradas en el sistema, ordenadas por ID.

- **Returns** - lista de AgenciaDTO con todas las agencias.

---

```java
public List<AgenciaDTO> listarPorUsuario(int usuarioId)
```

Retorna las agencias asociadas a un usuario webservice especifico.

- **Param** `usuarioId` - ID del usuario webservice propietario de las agencias.
- **Returns** - lista de AgenciaDTO pertenecientes al usuario.

---

```java
public AgenciaDTO crear(int usuarioId, CrearAgenciaRequestDTO req)
```

Crea una nueva agencia vinculada al usuario webservice indicado. Valida que los campos obligatorios esten presentes y que el usuario no tenga ya una agencia registrada, ya que solo se permite una por usuario.

- **Param** `usuarioId` - ID del usuario webservice que sera propietario de la agencia.
- **Param** `req` - datos de la nueva agencia (nombre y correo).
- **Returns** - AgenciaDTO con los datos de la agencia recien creada.
- **Throws** `IllegalArgumentException` - si el nombre o correo estan vacios, o si el usuario ya tiene una agencia.

---

```java
public void editar(int agenciaId, EditarAgenciaRequestDTO req)
```

Actualiza los datos de una agencia existente desde el panel de administracion. Valida nombre, correo, porcentaje de descuento y estado antes de aplicar los cambios.

- **Param** `agenciaId` - ID de la agencia a editar.
- **Param** `req` - datos actualizados de la agencia.
- **Throws** `IllegalArgumentException` - si algun campo es invalido o el estado no corresponde a Activa o Inactiva.

---

```java
public void cambiarEstado(int agenciaId, int usuarioId, int nuevoEstadoId)
```

Cambia el estado de una agencia verificando que pertenezca al usuario webservice indicado.

- **Param** `agenciaId` - ID de la agencia a modificar.
- **Param** `usuarioId` - ID del usuario webservice propietario de la agencia.
- **Param** `nuevoEstadoId` - nuevo estado a asignar.
- **Throws** `IllegalArgumentException` - si la agencia no existe o no pertenece al usuario.

---

```java
public void eliminar(int agenciaId, int usuarioId)
```

Elimina una agencia verificando que pertenezca al usuario webservice indicado.

- **Param** `agenciaId` - ID de la agencia a eliminar.
- **Param** `usuarioId` - ID del usuario webservice propietario de la agencia.
- **Throws** `IllegalArgumentException` - si la agencia no existe o no pertenece al usuario.

---

```java
public Integer obtenerAgenciaIdPorURL(String urlAgencia)
```

Busca el ID de una agencia a partir de su URL registrada.

- **Param** `urlAgencia` - URL unica asociada a la agencia.
- **Returns** - ID de la agencia, o null si no se encuentra ninguna con esa URL.

---

```java
public boolean guardarTokens(int agenciaId, String tokenEntrada, String tokenSalida)
```

Guarda los tokens de entrada y salida asociados a una agencia.

- **Param** `agenciaId` - ID de la agencia a actualizar.
- **Param** `tokenEntrada` - hash del token de entrada.
- **Param** `tokenSalida` - hash del token de salida.
- **Returns** - true si se actualizo al menos un registro, false si no se encontro la agencia.

---

```java
public AgenciaIdentidad obtenerAgenciaPorToken(String token)
```

Busca una agencia por su token de entrada y retorna su informacion de identidad.

- **Param** `token` - hash del token de entrada a buscar.
- **Returns** - AgenciaIdentidad con los datos basicos de la agencia, o null si no existe.

---

## AuthRepository

> Repository para la autenticacion de usuarios. Maneja la busqueda de credenciales en la base de datos durante el proceso de login.

```java
public Usuario buscarPorIdentificador(String identificador)
```

Busca un usuario por su username o correo electronico. El identificador se compara contra ambos campos en una sola consulta.

- **Param** `identificador` - username o correo del usuario a buscar.
- **Returns** - instancia de Usuario con sus credenciales, o null si no existe.

---

## BusquedaAgenciaRepository

> Repository para la busqueda de hoteles y habitaciones disponibles desde el canal de agencias. Maneja consultas de disponibilidad, descuentos, imagenes y registro de busquedas.

```java
public Double obtenerDescuentoAgencia(int usuarioId)
```

Retorna el porcentaje de descuento de la agencia activa asociada a un usuario webservice.

- **Param** `usuarioId` - ID del usuario webservice vinculado a la agencia.
- **Returns** - porcentaje de descuento como Double, o null si no tiene agencia activa.

---

```java
public Integer buscarCiudadId(String nombreCiudad, String nombrePais)
```

Busca el ID de una ciudad comparando nombre de ciudad y nombre de pais sin distincion de mayusculas.

- **Param** `nombreCiudad` - nombre de la ciudad a buscar.
- **Param** `nombrePais` - nombre del pais al que pertenece la ciudad.
- **Returns** - ID de la ciudad si existe, o null si no se encuentra.

---

```java
public void guardarBusqueda(int ciudadId, Date fechaCheckIn, Date fechaCheckOut,
```

Registra una busqueda realizada por un usuario webservice autenticado (TipoBusquedaID = 2).

- **Param** `ciudadId` - ID de la ciudad consultada.
- **Param** `fechaCheckIn` - fecha de entrada solicitada.
- **Param** `fechaCheckOut` - fecha de salida solicitada.
- **Param** `cantidadPersonas` - numero de personas para la busqueda.
- **Param** `usuarioId` - ID del usuario webservice que realiza la busqueda.

---

```java
public List<HotelResultadoDTO> buscarHotelesPorCiudad(int ciudadId)
```

Retorna los hoteles activos ubicados en una ciudad especifica.

- **Param** `ciudadId` - ID de la ciudad donde se buscan los hoteles.
- **Returns** - lista de HotelResultadoDTO con los hoteles activos encontrados.

---

```java
public List<Integer> buscarImagenesHotel(int hotelId)
```

Retorna los IDs de las imagenes asociadas a un hotel.

- **Param** `hotelId` - ID del hotel del que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes del hotel.

---

```java
public List<AmenidadHotelDTO> buscarAmenidadesHotel(int hotelId)
```

Retorna las amenidades registradas para un hotel junto con su descripcion y nombre.

- **Param** `hotelId` - ID del hotel del que se quieren obtener las amenidades.
- **Returns** - lista de AmenidadHotelDTO con los datos de cada amenidad.

---

```java
public List<Integer> buscarImagenesAmenidad(int hotelAmenidadId)
```

Retorna los IDs de las imagenes asociadas a una amenidad de hotel.

- **Param** `hotelAmenidadId` - ID del registro HotelAmenidad del que se buscan las imagenes.
- **Returns** - lista de IDs de imagenes de la amenidad.

---

```java
public List<TipoHabitacionResultadoDTO> buscarTiposHabitacionDisponibles(
```

Retorna los tipos de habitacion disponibles en un hotel para un rango de fechas y capacidad minima. Excluye habitaciones con reservaciones activas (Pendiente o Confirmada) que se traslapen con el rango solicitado. El precio y tipo de cama se obtienen desde TipoHabitacion, no desde Habitacion.

- **Param** `hotelId` - ID del hotel donde se busca disponibilidad.
- **Param** `capacidadMinima` - capacidad minima requerida por tipo de habitacion.
- **Param** `fechaCheckIn` - fecha de entrada solicitada.
- **Param** `fechaCheckOut` - fecha de salida solicitada.
- **Returns** - lista de TipoHabitacionResultadoDTO con los tipos de habitacion disponibles.

---

```java
public List<HabitacionResumenDTO> buscarHabitacionesResumenPorTipo(
```

Retorna las habitaciones concretas disponibles de un tipo especifico en un hotel para un rango de fechas. Excluye habitaciones con reservaciones activas que se traslapen con el rango solicitado.

- **Param** `hotelId` - ID del hotel donde se busca disponibilidad.
- **Param** `tipoHabitacionId` - ID del tipo de habitacion requerido.
- **Param** `fechaCheckIn` - fecha de entrada solicitada.
- **Param** `fechaCheckOut` - fecha de salida solicitada.
- **Returns** - lista de HabitacionResumenDTO con el ID y numero de cada habitacion disponible.

---

```java
public List<Integer> buscarImagenesHabitacion(int habitacionId)
```

Retorna los IDs de las imagenes asociadas a una habitacion especifica.

- **Param** `habitacionId` - ID de la habitacion de la que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes de la habitacion.

---

```java
public void guardarBusquedaSinUsuario(int ciudadId, Date fechaCheckIn, Date fechaCheckOut,
```

Registra una busqueda anonima sin usuario autenticado (TipoBusquedaID = 2).

- **Param** `ciudadId` - ID de la ciudad consultada.
- **Param** `fechaCheckIn` - fecha de entrada solicitada.
- **Param** `fechaCheckOut` - fecha de salida solicitada.
- **Param** `cantidadPersonas` - numero de personas para la busqueda.

---

```java
public Double obtenerDescuentoAgenciaPorToken(String token)
```

Retorna el porcentaje de descuento de la agencia activa identificada por su token de entrada.

- **Param** `token` - hash del token de entrada asociado a la agencia.
- **Returns** - porcentaje de descuento como Double, o null si no se encuentra una agencia activa con ese token.

---

## BusquedaRepository

> Repository para la busqueda de hoteles y habitaciones disponibles desde el canal web de usuarios. Maneja consultas de disponibilidad, amenidades, imagenes y registro de busquedas.

```java
public Integer buscarCiudadId(String nombreCiudad, String nombrePais)
```

Busca el ID de una ciudad comparando nombre de ciudad y nombre de pais sin distincion de mayusculas.

- **Param** `nombreCiudad` - nombre de la ciudad a buscar.
- **Param** `nombrePais` - nombre del pais al que pertenece la ciudad.
- **Returns** - ID de la ciudad si existe, o null si no se encuentra.

---

```java
public void guardarBusqueda(int ciudadId, Date fechaCheckIn, Date fechaCheckOut,
```

Registra una busqueda de disponibilidad realizada por un usuario autenticado o anonimo. El TipoBusquedaID se asigna como 1 si hay sesion activa, o null si la busqueda es anonima.

- **Param** `ciudadId` - ID de la ciudad consultada.
- **Param** `fechaCheckIn` - fecha de entrada solicitada.
- **Param** `fechaCheckOut` - fecha de salida solicitada.
- **Param** `cantidadPersonas` - numero de personas para la busqueda.
- **Param** `usuarioId` - ID del usuario autenticado, o null si no hay sesion.

---

```java
public List<HotelResultadoDTO> buscarHotelesPorCiudad(int ciudadId)
```

Retorna los hoteles activos ubicados en una ciudad especifica.

- **Param** `ciudadId` - ID de la ciudad donde se buscan los hoteles.
- **Returns** - lista de HotelResultadoDTO con los hoteles activos encontrados.

---

```java
public List<Integer> buscarImagenesHotel(int hotelId)
```

Retorna los IDs de las imagenes asociadas a un hotel.

- **Param** `hotelId` - ID del hotel del que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes del hotel.

---

```java
public List<AmenidadHotelDTO> buscarAmenidadesHotel(int hotelId)
```

Retorna las amenidades registradas para un hotel junto con su descripcion y nombre.

- **Param** `hotelId` - ID del hotel del que se quieren obtener las amenidades.
- **Returns** - lista de AmenidadHotelDTO con los datos de cada amenidad.

---

```java
public List<Integer> buscarImagenesAmenidad(int hotelAmenidadId)
```

Retorna los IDs de las imagenes asociadas a una amenidad de hotel.

- **Param** `hotelAmenidadId` - ID del registro HotelAmenidad del que se buscan las imagenes.
- **Returns** - lista de IDs de imagenes de la amenidad.

---

```java
public List<TipoHabitacionResultadoDTO> buscarTiposHabitacionDisponibles(
```

Retorna los tipos de habitacion disponibles en un hotel para un rango de fechas y capacidad minima. Excluye habitaciones con reservaciones activas (Pendiente o Confirmada) que se traslapen con el rango solicitado.

- **Param** `hotelId` - ID del hotel donde se busca disponibilidad.
- **Param** `capacidadMinima` - capacidad minima requerida por tipo de habitacion.
- **Param** `fechaCheckIn` - fecha de entrada solicitada.
- **Param** `fechaCheckOut` - fecha de salida solicitada.
- **Returns** - lista de TipoHabitacionResultadoDTO con los tipos de habitacion disponibles.

---

```java
public List<HabitacionResumenDTO> buscarHabitacionesResumenPorTipo(
```

Retorna las habitaciones concretas disponibles de un tipo especifico en un hotel para un rango de fechas. Excluye habitaciones con reservaciones activas que se traslapen con el rango solicitado.

- **Param** `hotelId` - ID del hotel donde se busca disponibilidad.
- **Param** `tipoHabitacionId` - ID del tipo de habitacion requerido.
- **Param** `fechaCheckIn` - fecha de entrada solicitada.
- **Param** `fechaCheckOut` - fecha de salida solicitada.
- **Returns** - lista de HabitacionResumenDTO con el ID y numero de cada habitacion disponible.

---

```java
public List<Integer> buscarImagenesHabitacion(int habitacionId)
```

Retorna los IDs de las imagenes asociadas a una habitacion especifica.

- **Param** `habitacionId` - ID de la habitacion de la que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes de la habitacion.

---

## CancelacionRepository

> Repository para la cancelacion de reservaciones, tanto de usuarios directos como de agencias. Maneja la consulta de estado previo a la cancelacion y la actualizacion del registro.

```java
public Object[] obtenerReservacionParaCancelar(int reservacionId, int usuarioId)
```

Busca una reservacion verificando que pertenezca al usuario indicado, para validar antes de cancelar.

- **Param** `reservacionId` - ID de la reservacion a consultar.
- **Param** `usuarioId` - ID del usuario propietario de la reservacion.
- **Returns** - arreglo con {ID, EstadoID, Estado} o null si no existe o no pertenece al usuario.

---

```java
public java.sql.Date obtenerFechaCheckInMasReciente(int reservacionId)
```

Retorna la fecha de check-in mas proxima entre todas las habitaciones de una reservacion. Se usa para validar si la cancelacion esta dentro del plazo permitido.

- **Param** `reservacionId` - ID de la reservacion de la que se quiere obtener la fecha.
- **Returns** - fecha de check-in mas reciente, o null si no se encuentran detalles.

---

```java
public void cancelarReservacion(int reservacionId, String motivoCancelacion)
```

Actualiza el estado de una reservacion a Cancelada (EstadoID = 4) y registra el motivo y la fecha.

- **Param** `reservacionId` - ID de la reservacion a cancelar.
- **Param** `motivoCancelacion` - razon de la cancelacion ingresada por el usuario.

---

```java
public Object[] obtenerReservacionAgenciaParaCancelar(int reservacionId, int agenciaId)
```

Busca una reservacion verificando que pertenezca a una agencia especifica, para validar antes de cancelar. La relacion se resuelve a traves del usuario webservice vinculado a la agencia.

- **Param** `reservacionId` - ID de la reservacion a consultar.
- **Param** `agenciaId` - ID de la agencia propietaria de la reservacion.
- **Returns** - arreglo con {ID, EstadoID, Estado} o null si no existe o no pertenece a la agencia.

---

## CiudadRepository

> Repository para la gestion de ciudades. Permite buscar o crear ciudades de forma segura ante condiciones de carrera entre hilos.

```java
public int buscarOCrearPorNombre(String nombre, int paisId)
```

Busca una ciudad por nombre y pais, y la crea si no existe. Si dos hilos intentan insertar la misma ciudad simultaneamente y se produce un conflicto de clave unica (ORA-00001), reintenta la busqueda antes de propagar el error.

- **Param** `nombre` - nombre de la ciudad a buscar o crear.
- **Param** `paisId` - ID del pais al que pertenece la ciudad.
- **Returns** - ID de la ciudad existente o recien creada.
- **Throws** `RuntimeException` - si ocurre un error de base de datos distinto a una colision de clave unica.

---

## ComentarioRepository

> Repository para la gestion de comentarios y resenas de hoteles. Cubre la creacion de comentarios, validacion de resenas previas y actualizacion del rating del hotel.

```java
public boolean existeComentarioConResena(int usuarioId, int hotelId)
```

Verifica si un usuario ya tiene un comentario con resena registrado para un hotel especifico.

- **Param** `usuarioId` - ID del usuario a verificar.
- **Param** `hotelId` - ID del hotel a verificar.
- **Returns** - true si ya existe un comentario con resena del usuario en ese hotel, false en caso contrario.

---

```java
public int crearComentario(int usuarioId, int hotelId, Integer comentarioPadreId,
```

Inserta un nuevo comentario en la base de datos y retorna el ID generado. El comentario puede ser una resena principal o una respuesta a otro comentario si se indica el padre.

- **Param** `usuarioId` - ID del usuario que publica el comentario.
- **Param** `hotelId` - ID del hotel al que pertenece el comentario.
- **Param** `comentarioPadreId` - ID del comentario padre si es una respuesta, o null si es raiz.
- **Param** `resena` - puntuacion de la resena (1-5), o null si es solo un comentario.
- **Param** `contenido` - texto del comentario.
- **Returns** - ID del comentario recien insertado.

---

```java
public void actualizarRatingHotel(int hotelId)
```

Recalcula y actualiza el rating de un hotel en base al promedio de sus resenas activas.

- **Param** `hotelId` - ID del hotel cuyo rating se debe actualizar.

---

```java
public ComentarioResponseDTO obtenerComentario(int comentarioId)
```

Retorna un comentario especifico con los datos del usuario que lo publico.

- **Param** `comentarioId` - ID del comentario a buscar.
- **Returns** - ComentarioResponseDTO con los datos del comentario, o null si no existe.

---

```java
public List<ComentarioResponseDTO> obtenerComentariosPorUsuario(int usuarioId)
```

Retorna todos los comentarios publicados por un usuario, ordenados por fecha descendente.

- **Param** `usuarioId` - ID del usuario del que se quieren obtener los comentarios.
- **Returns** - lista de ComentarioResponseDTO con los comentarios del usuario.

---

```java
public List<ComentarioResponseDTO> obtenerComentariosPorHotel(int hotelId)
```

Retorna todos los comentarios registrados para un hotel, ordenados por fecha descendente.

- **Param** `hotelId` - ID del hotel del que se quieren obtener los comentarios.
- **Returns** - lista de ComentarioResponseDTO con los comentarios del hotel.

---

## DestinosRepository

> Repository para la consulta de destinos turisticos disponibles. Provee acceso a los hoteles activos y sus imagenes para la vista de destinos.

```java
public List<HotelResultadoDTO> obtenerTodosLosHoteles()
```

Retorna todos los hoteles activos del sistema ordenados por pais, ciudad y nombre.

- **Returns** - lista de HotelResultadoDTO con los datos de cada hotel activo.

---

```java
public List<Integer> obtenerImagenesHotel(int hotelId)
```

Retorna los IDs de las imagenes asociadas a un hotel especifico.

- **Param** `hotelId` - ID del hotel del que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes del hotel.

---

## DownsRepository

> Repository para la gestion de downs (votos negativos) sobre comentarios de hoteles. Permite consultar, registrar, eliminar y contabilizar downs por usuario y comentario.

```java
public List<DownResponseDTO> obtenerDownsDeUsuario(int usuarioId)
```

Retorna todos los downs registrados por un usuario, ordenados por fecha descendente.

- **Param** `usuarioId` - ID del usuario del que se quieren obtener los downs.
- **Returns** - lista de DownResponseDTO con los datos de cada down del usuario.

---

```java
public List<DownResponseDTO> obtenerDownsDeUsuarioPorHotel(int usuarioId, int hotelId)
```

Retorna los downs de un usuario filtrados por un hotel especifico, ordenados por fecha descendente.

- **Param** `usuarioId` - ID del usuario del que se quieren obtener los downs.
- **Param** `hotelId` - ID del hotel por el que se filtra.
- **Returns** - lista de DownResponseDTO con los downs del usuario en ese hotel.

---

```java
public Integer obtenerValorDown(int usuarioId, int comentarioId)
```

Retorna el valor del down que un usuario tiene registrado sobre un comentario especifico. Se usa para verificar si ya existe un down antes de insertar o eliminar.

- **Param** `usuarioId` - ID del usuario a verificar.
- **Param** `comentarioId` - ID del comentario a verificar.
- **Returns** - valor del down existente, o null si el usuario no ha marcado ese comentario.

---

```java
public void insertarDown(int usuarioId, int comentarioId, int valor)
```

Registra un nuevo down de un usuario sobre un comentario.

- **Param** `usuarioId` - ID del usuario que registra el down.
- **Param** `comentarioId` - ID del comentario sobre el que se aplica el down.
- **Param** `valor` - valor del down a registrar.

---

```java
public void eliminarDown(int usuarioId, int comentarioId)
```

Elimina el down que un usuario tiene registrado sobre un comentario.

- **Param** `usuarioId` - ID del usuario cuyo down se eliminara.
- **Param** `comentarioId` - ID del comentario del que se quita el down.

---

```java
public void actualizarContadorDown(int comentarioId, int delta)
```

Incrementa o decrementa el contador de downs de un comentario segun el delta indicado. Se usa para mantener sincronizado el campo Downs del comentario tras insertar o eliminar un down.

- **Param** `comentarioId` - ID del comentario cuyo contador se actualiza.
- **Param** `delta` - valor a sumar al contador (puede ser negativo para restar).

---

## HotelAgenciaRepository

> Repository para la consulta de hoteles disponibles desde el canal de agencias. Provee el listado de hoteles activos con su ciudad y pais para uso en reservaciones de agencia.

```java
public List<HotelAgenciaDTO> listarHotelesParaAgencia()
```

Retorna todos los hoteles activos con su informacion de ubicacion, ordenados por ID.

- **Returns** - lista de HotelAgenciaDTO con el ID, nombre, ciudad y pais de cada hotel activo.

---

## HotelRepository

> Repository para la gestion completa de hoteles desde el panel de administracion. Cubre amenidades, habitaciones, imagenes, reservaciones y metricas del sistema.

```java
public List<AmenidadDTO> listarAmenidades()
```

Retorna el catalogo completo de amenidades disponibles, ordenadas por ID.

- **Returns** - lista de AmenidadDTO con el ID y nombre de cada amenidad.

---

```java
public int crearAmenidad(String nombre)
```

Crea una nueva amenidad en el catalogo y retorna el ID generado.

- **Param** `nombre` - nombre de la amenidad a crear.
- **Returns** - ID de la amenidad recien insertada.

---

```java
public List<HotelAdminDTO> listarTodos()
```

Retorna todos los hoteles registrados con su estado y ubicacion, ordenados por ID.

- **Returns** - lista de HotelAdminDTO con los datos completos de cada hotel.

---

```java
public int crearHotel(String nombre, String direccion, String descripcion,
```

Inserta un nuevo hotel en la base de datos y retorna el ID generado.

- **Param** `nombre` - nombre del hotel.
- **Param** `direccion` - direccion fisica del hotel.
- **Param** `descripcion` - descripcion general del hotel.
- **Param** `rating` - calificacion inicial del hotel.
- **Param** `estadoId` - ID del estado inicial del hotel.
- **Param** `ciudadId` - ID de la ciudad donde se ubica el hotel.
- **Returns** - ID del hotel recien insertado.

---

```java
public void actualizarHotel(int hotelId, String nombre, String direccion,
```

Actualiza los datos principales de un hotel existente.

- **Param** `hotelId` - ID del hotel a actualizar.
- **Param** `nombre` - nuevo nombre del hotel.
- **Param** `direccion` - nueva direccion del hotel.
- **Param** `descripcion` - nueva descripcion del hotel.
- **Param** `rating` - nuevo rating del hotel.
- **Param** `estadoId` - nuevo ID de estado del hotel.

---

```java
public void eliminarHotel(int hotelId)
```

Elimina un hotel y todos sus registros dependientes en cascada. Borra en orden: imagenes de habitaciones, habitaciones, imagenes de amenidades, amenidades del hotel, imagenes del hotel y finalmente el hotel.

- **Param** `hotelId` - ID del hotel a eliminar.

---

```java
public boolean existe(int hotelId)
```

Verifica si un hotel existe en la base de datos.

- **Param** `hotelId` - ID del hotel a verificar.
- **Returns** - true si el hotel existe, false en caso contrario.

---

```java
public int contarHabitaciones(int hotelId)
```

Retorna la cantidad de habitaciones registradas para un hotel.

- **Param** `hotelId` - ID del hotel a consultar.
- **Returns** - numero de habitaciones del hotel, o 0 si no tiene ninguna.

---

```java
public List<HotelAmenidadDTO> listarAmenidadesHotel(int hotelId)
```

Retorna las amenidades asignadas a un hotel con su descripcion y nombre, ordenadas por ID.

- **Param** `hotelId` - ID del hotel del que se quieren obtener las amenidades.
- **Returns** - lista de HotelAmenidadDTO con los datos de cada amenidad del hotel.

---

```java
public boolean tieneAmenidad(int hotelId, int amenidadId)
```

Verifica si un hotel ya tiene asignada una amenidad especifica.

- **Param** `hotelId` - ID del hotel a verificar.
- **Param** `amenidadId` - ID de la amenidad a verificar.
- **Returns** - true si la amenidad ya esta asignada al hotel, false en caso contrario.

---

```java
public int agregarAmenidadHotel(int hotelId, int amenidadId, String descripcion)
```

Asigna una amenidad a un hotel con una descripcion personalizada y retorna el ID generado.

- **Param** `hotelId` - ID del hotel al que se agrega la amenidad.
- **Param** `amenidadId` - ID de la amenidad a agregar.
- **Param** `descripcion` - descripcion particular de la amenidad en ese hotel.
- **Returns** - ID del registro HotelAmenidad recien insertado.

---

```java
public void actualizarAmenidadHotel(int hotelAmenidadId, String descripcion)
```

Actualiza la descripcion de una amenidad asignada a un hotel.

- **Param** `hotelAmenidadId` - ID del registro HotelAmenidad a actualizar.
- **Param** `descripcion` - nueva descripcion de la amenidad.

---

```java
public void eliminarAmenidadHotel(int hotelAmenidadId)
```

Elimina una amenidad de un hotel junto con todas sus imagenes asociadas.

- **Param** `hotelAmenidadId` - ID del registro HotelAmenidad a eliminar.

---

```java
public List<Integer> obtenerImagenesAmenidadIds(int hotelAmenidadId)
```

Retorna los IDs de las imagenes asociadas a una amenidad de hotel, ordenados por ID.

- **Param** `hotelAmenidadId` - ID del registro HotelAmenidad del que se buscan las imagenes.
- **Returns** - lista de IDs de imagenes de la amenidad.

---

```java
public int agregarImagenAmenidad(int hotelAmenidadId, byte[] imagen)
```

Agrega una imagen a una amenidad de hotel y retorna el ID generado.

- **Param** `hotelAmenidadId` - ID del registro HotelAmenidad al que se agrega la imagen.
- **Param** `imagen` - bytes de la imagen a guardar.
- **Returns** - ID de la imagen recien insertada.

---

```java
public void eliminarImagenAmenidad(int imagenId)
```

Elimina una imagen de amenidad de hotel por su ID.

- **Param** `imagenId` - ID de la imagen a eliminar.

---

```java
public List<Integer> obtenerImagenesIds(int hotelId)
```

Retorna los IDs de las imagenes asociadas a un hotel, ordenados por ID.

- **Param** `hotelId` - ID del hotel del que se quieren obtener los IDs de imagenes.
- **Returns** - lista de IDs de imagenes del hotel.

---

```java
public int agregarImagenHotel(int hotelId, byte[] imagen)
```

Agrega una imagen a un hotel y retorna el ID generado.

- **Param** `hotelId` - ID del hotel al que se agrega la imagen.
- **Param** `imagen` - bytes de la imagen a guardar.
- **Returns** - ID de la imagen recien insertada.

---

```java
public void eliminarImagenHotel(int imagenId)
```

Elimina una imagen de hotel por su ID.

- **Param** `imagenId` - ID de la imagen a eliminar.

---

```java
public List<HabitacionAdminDTO> listarHabitacionesPorHotel(int hotelId)
```

Retorna todas las habitaciones de un hotel con sus datos de tipo, cama, precio y estado.

- **Param** `hotelId` - ID del hotel del que se quieren listar las habitaciones.
- **Returns** - lista de HabitacionAdminDTO con los datos completos de cada habitacion.

---

```java
public int crearHabitacion(int hotelId, int tipoHabitacionId,
```

Crea una nueva habitacion en un hotel y auto-asigna el numero correlativo basado en la cantidad de habitaciones ya existentes en ese hotel.

- **Param** `hotelId` - ID del hotel donde se crea la habitacion.
- **Param** `tipoHabitacionId` - ID del tipo de habitacion a asignar.
- **Param** `descripcion` - descripcion particular de la habitacion.
- **Param** `estadoId` - ID del estado inicial de la habitacion.
- **Returns** - ID de la habitacion recien insertada.

---

```java
public boolean existeHabitacion(int habitacionId)
```

Verifica si una habitacion existe en la base de datos.

- **Param** `habitacionId` - ID de la habitacion a verificar.
- **Returns** - true si la habitacion existe, false en caso contrario.

---

```java
public void actualizarHabitacion(int habitacionId, int tipoHabitacionId,
```

Actualiza los datos de una habitacion existente.

- **Param** `habitacionId` - ID de la habitacion a actualizar.
- **Param** `tipoHabitacionId` - nuevo ID del tipo de habitacion.
- **Param** `numeroHabitacion` - nuevo numero de habitacion.
- **Param** `descripcion` - nueva descripcion de la habitacion.
- **Param** `estadoId` - nuevo ID de estado de la habitacion.

---

```java
public void eliminarHabitacion(int habitacionId)
```

Elimina una habitacion y todas sus imagenes asociadas.

- **Param** `habitacionId` - ID de la habitacion a eliminar.

---

```java
public List<Integer> obtenerImagenesHabitacionIds(int habitacionId)
```

Retorna los IDs de las imagenes asociadas a una habitacion, ordenados por ID.

- **Param** `habitacionId` - ID de la habitacion de la que se quieren obtener los IDs de imagenes.
- **Returns** - lista de IDs de imagenes de la habitacion.

---

```java
public int agregarImagenHabitacion(int habitacionId, byte[] imagen)
```

Agrega una imagen a una habitacion y retorna el ID generado.

- **Param** `habitacionId` - ID de la habitacion a la que se agrega la imagen.
- **Param** `imagen` - bytes de la imagen a guardar.
- **Returns** - ID de la imagen recien insertada.

---

```java
public void eliminarImagenHabitacion(int imagenId)
```

Elimina una imagen de habitacion por su ID.

- **Param** `imagenId` - ID de la imagen a eliminar.

---

```java
public List<java.util.Map<String, Object>> listarTodasReservaciones()
```

Retorna todas las reservaciones del sistema con datos del usuario, hotel y fechas, ordenadas por fecha de creacion descendente.

- **Returns** - lista de mapas con los datos resumidos de cada reservacion.

---

```java
public java.util.Map<String, Object> obtenerMetricas()
```

Retorna un mapa con las metricas generales del sistema para el panel de administracion. Incluye totales de usuarios, hoteles, reservaciones por estado e ingresos confirmados. ingresosTotales, reservasPorEstado y hotesTotales.

- **Returns** - mapa con las claves: totalUsuarios, hotelesActivos, reservasActivas, reservasTotales,

---

## ImagenRepository

> Repository para la recuperacion de imagenes almacenadas en la base de datos. Cubre imagenes de hoteles, habitaciones y amenidades.

```java
public byte[] obtenerImagenHotel(int id)
```

Retorna los bytes de una imagen de hotel por su ID.

- **Param** `id` - ID de la imagen a recuperar.
- **Returns** - arreglo de bytes con la imagen, o null si no existe.

---

```java
public byte[] obtenerImagenHabitacion(int id)
```

Retorna los bytes de una imagen de habitacion por su ID.

- **Param** `id` - ID de la imagen a recuperar.
- **Returns** - arreglo de bytes con la imagen, o null si no existe.

---

```java
public byte[] obtenerImagenAmenidad(int id)
```

Retorna los bytes de una imagen de amenidad de hotel por su ID.

- **Param** `id` - ID de la imagen a recuperar.
- **Returns** - arreglo de bytes con la imagen, o null si no existe.

---

## NacionalidadRepository

> Repository para la gestion de nacionalidades. Permite buscar o crear una nacionalidad de forma idempotente por nombre.

```java
public int buscarOCrearPorNombre(String nombre)
```

Busca una nacionalidad por nombre y la crea si no existe. La comparacion del nombre es case-insensitive.

- **Param** `nombre` - nombre de la nacionalidad a buscar o crear.
- **Returns** - ID de la nacionalidad existente o recien creada.

---

## PagoAgenciaRepository

> Repository para el procesamiento de pagos de reservaciones realizadas por agencias. Maneja la validacion de reservaciones, confirmacion y generacion de facturas.

```java
public Object[] obtenerReservacionParaPago(int reservacionId, int agenciaId)
```

Busca una reservacion verificando que pertenezca a la agencia indicada. Se usa para validar la reservacion antes de procesar el pago.

- **Param** `reservacionId` - ID de la reservacion a consultar.
- **Param** `agenciaId` - ID de la agencia propietaria de la reservacion.
- **Returns** - arreglo con {ID, No_Reservacion, Total, Estado, EstadoID} o null si no existe o no pertenece a la agencia.

---

```java
public void confirmarReservacion(int reservacionId)
```

Confirma una reservacion actualizando su estado a Confirmada (EstadoID = 2) y eliminando la fecha de expiracion.

- **Param** `reservacionId` - ID de la reservacion a confirmar.

---

```java
public int crearFactura(int reservacionId, String nit, String codigoPostal, double total)
```

Crea una factura asociada a una reservacion y retorna el ID generado.

- **Param** `reservacionId` - ID de la reservacion a facturar.
- **Param** `nit` - numero de identificacion tributaria del cliente.
- **Param** `codigoPostal` - codigo postal del cliente.
- **Param** `total` - monto total de la factura.
- **Returns** - ID de la factura recien insertada.

---

```java
public PagoResponseDTO obtenerFactura(int facturaId)
```

Retorna los datos completos de una factura junto con el estado y numero de su reservacion asociada.

- **Param** `facturaId` - ID de la factura a consultar.
- **Returns** - PagoResponseDTO con los datos de la factura, o null si no existe.

---

## PagoRepository

> Repository para el procesamiento de pagos de reservaciones de usuarios directos. Maneja la validacion de reservaciones, confirmacion y generacion de facturas.

```java
public Object[] obtenerReservacionParaPago(int reservacionId, int usuarioId)
```

Busca una reservacion verificando que pertenezca al usuario indicado. Se usa para validar la reservacion antes de procesar el pago.

- **Param** `reservacionId` - ID de la reservacion a consultar.
- **Param** `usuarioId` - ID del usuario propietario de la reservacion.
- **Returns** - arreglo con {ID, No_Reservacion, Total, Estado, EstadoID} o null si no existe o no pertenece al usuario.

---

```java
public void confirmarReservacion(int reservacionId)
```

Confirma una reservacion actualizando su estado a Confirmada (EstadoID = 2) y eliminando la fecha de expiracion.

- **Param** `reservacionId` - ID de la reservacion a confirmar.

---

```java
public int crearFactura(int reservacionId, String nit, String codigoPostal, double total)
```

Crea una factura asociada a una reservacion y retorna el ID generado.

- **Param** `reservacionId` - ID de la reservacion a facturar.
- **Param** `nit` - numero de identificacion tributaria del cliente.
- **Param** `codigoPostal` - codigo postal del cliente.
- **Param** `total` - monto total de la factura.
- **Returns** - ID de la factura recien insertada.

---

```java
public PagoResponseDTO obtenerFactura(int facturaId)
```

Retorna los datos completos de una factura junto con el estado y numero de su reservacion asociada.

- **Param** `facturaId` - ID de la factura a consultar.
- **Returns** - PagoResponseDTO con los datos de la factura, o null si no existe.

---

## PaisRepository

> Repository para la gestion de paises y ciudades. Permite buscar o crear paises de forma segura ante condiciones de carrera, y provee listados de paises y ciudades para uso en formularios del panel de administracion.

```java
public int buscarOCrearPorNombre(String nombre)
```

Busca un pais por nombre y lo crea si no existe. Si dos hilos intentan insertar el mismo pais simultaneamente y se produce un conflicto de clave unica (ORA-00001), reintenta la busqueda antes de propagar el error.

- **Param** `nombre` - nombre del pais a buscar o crear.
- **Returns** - ID del pais existente o recien creado.
- **Throws** `RuntimeException` - si ocurre un error de base de datos distinto a una colision de clave unica.

---

```java
public List<PaisDTO> listarPaises()
```

Retorna todos los paises registrados ordenados alfabeticamente por nombre. Se usa para poblar dropdowns en el panel de administracion.

- **Returns** - lista de PaisDTO con el ID y nombre de cada pais.

---

```java
public List<CiudadDTO> listarCiudades()
```

Retorna todas las ciudades registradas con su pais asociado, ordenadas por pais y nombre de ciudad. Se usa para poblar dropdowns en el panel de administracion.

- **Returns** - lista de CiudadDTO con el ID, nombre, ID de pais y nombre de pais de cada ciudad.

---

## PdfReservacionRepository

> Repository para la generacion de PDFs de reservaciones. Provee los datos necesarios para construir el documento: correo del usuario, validacion de pertenencia, detalles de la reservacion y factura asociada.

```java
public String obtenerCorreoUsuario(int usuarioId)
```

Retorna el correo electronico de un usuario por su ID.

- **Param** `usuarioId` - ID del usuario del que se quiere obtener el correo.
- **Returns** - correo del usuario, o null si no existe.

---

```java
public boolean perteneceAlUsuario(int reservacionId, int usuarioId)
```

Verifica si una reservacion pertenece a un usuario especifico.

- **Param** `reservacionId` - ID de la reservacion a verificar.
- **Param** `usuarioId` - ID del usuario a validar como propietario.
- **Returns** - true si la reservacion pertenece al usuario, false en caso contrario.

---

```java
public List<ReservacionDetalleDTO> obtenerDetalles(int reservacionId)
```

Retorna los detalles completos de una reservacion para la generacion del PDF. Incluye datos de la reservacion, habitaciones, tipo de cama, hotel y fechas de check-in y check-out.

- **Param** `reservacionId` - ID de la reservacion de la que se quieren obtener los detalles.
- **Returns** - lista de ReservacionDetalleDTO con una entrada por cada habitacion reservada.

---

```java
public Object[] obtenerFactura(int reservacionId)
```

Retorna los datos de la factura asociada a una reservacion, si existe.

- **Param** `reservacionId` - ID de la reservacion de la que se busca la factura.
- **Returns** - arreglo con {ID, Fecha, NIT, Codigo_Postal, Total} o null si no existe factura.

---

## ReservacionAgenciaRepository

> Repository para la gestion de reservaciones realizadas por agencias de viaje. Cubre la creacion, consulta, validacion de disponibilidad y expiracion de reservaciones, asi como la obtencion de datos de agencia y precios de habitaciones.

```java
public int[] obtenerDatosAgencia(int agenciaId)
```

Retorna el ID del usuario webservice y el porcentaje de descuento de una agencia activa.

- **Param** `agenciaId` - ID de la agencia a consultar.
- **Returns** - arreglo con {UsuarioWEBIs_ID, PorcentajeDescuento (entero)} o null si la agencia no existe o no esta activa.

---

```java
public double obtenerDescuentoAgencia(int agenciaId)
```

Retorna el porcentaje de descuento exacto con decimales de una agencia activa.

- **Param** `agenciaId` - ID de la agencia a consultar.
- **Returns** - porcentaje de descuento como double, o 0.0 si la agencia no existe o no esta activa.

---

```java
public double[] obtenerPrecios(int habitacionId)
```

Retorna el precio por noche, precio por persona y capacidad maxima de una habitacion. Los valores se retornan en ese orden: precios[0] = precioPorNoche, precios[1] = precioPorPersona, precios[2] = capacidadMaxima.

- **Param** `habitacionId` - ID de la habitacion a consultar.
- **Returns** - arreglo de doubles con los tres valores de precio y capacidad.
- **Throws** `RuntimeException` - si la habitacion no existe en la base de datos.

---

```java
public boolean existeTraslape(int habitacionId, Date fechaCheckIn, Date fechaCheckOut)
```

Verifica si una habitacion tiene reservaciones activas que se traslapen con el rango de fechas indicado. Solo considera reservaciones en estado Pendiente o Confirmada.

- **Param** `habitacionId` - ID de la habitacion a verificar.
- **Param** `fechaCheckIn` - fecha de entrada solicitada.
- **Param** `fechaCheckOut` - fecha de salida solicitada.
- **Returns** - true si existe al menos un traslape, false si la habitacion esta disponible.

---

```java
public int crearReservacion(String noReservacion, double total, int usuarioWebisId,
```

Crea una nueva reservacion en estado Pendiente (EstadoID = 1) y retorna el ID generado.

- **Param** `noReservacion` - codigo unico de la reservacion.
- **Param** `total` - monto total de la reservacion.
- **Param** `usuarioWebisId` - ID del usuario webservice asociado a la agencia.
- **Param** `fechaCreacion` - fecha y hora de creacion de la reservacion.
- **Param** `fechaExpiracion` - fecha y hora limite para completar el pago.
- **Returns** - ID de la reservacion recien insertada.

---

```java
public boolean perteneceAAgenciaYEstaPendiente(int reservacionId, int agenciaId)
```

Verifica si una reservacion pertenece a una agencia especifica y se encuentra en estado Pendiente.

- **Param** `reservacionId` - ID de la reservacion a verificar.
- **Param** `agenciaId` - ID de la agencia a validar como propietaria.
- **Returns** - true si la reservacion pertenece a la agencia y esta pendiente, false en caso contrario.

---

```java
public void expirarReservacion(int reservacionId)
```

Marca una reservacion como Expirada si actualmente se encuentra en estado Pendiente.

- **Param** `reservacionId` - ID de la reservacion a expirar.

---

```java
public void crearDetalle(int reservacionId, int habitacionId,
```

Inserta un detalle de reservacion con los datos de una habitacion y el rango de fechas solicitado.

- **Param** `reservacionId` - ID de la reservacion a la que pertenece el detalle.
- **Param** `habitacionId` - ID de la habitacion reservada.
- **Param** `fechaCheckIn` - fecha de entrada.
- **Param** `fechaCheckOut` - fecha de salida.
- **Param** `cantidadPersonas` - numero de personas para este detalle.
- **Param** `total` - costo total de este detalle.

---

```java
public Object[] obtenerReservacion(int reservacionId)
```

Retorna los datos resumidos de una reservacion para construir la respuesta al cliente. o null si no existe.

- **Param** `reservacionId` - ID de la reservacion a consultar.
- **Returns** - arreglo con {ID, No_Reservacion, Total, Fecha_Creacion, Fecha_Expiracion, Estado}

---

```java
public List<ReservacionDetalleDTO> obtenerReservacionesDeAgencia(int agenciaId)
```

Retorna todas las reservaciones asociadas a una agencia con el detalle completo de habitaciones y hotel.

- **Param** `agenciaId` - ID de la agencia de la que se quieren obtener las reservaciones.
- **Returns** - lista de ReservacionDetalleDTO con una entrada por cada habitacion reservada.

---

```java
public List<Integer> obtenerImagenesHotel(int hotelId)
```

Retorna los IDs de las imagenes asociadas a un hotel.

- **Param** `hotelId` - ID del hotel del que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes del hotel.

---

```java
public List<Integer> obtenerImagenesHabitacion(int habitacionId)
```

Retorna los IDs de las imagenes asociadas a una habitacion.

- **Param** `habitacionId` - ID de la habitacion de la que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes de la habitacion.

---

```java
public List<ReservacionDetalleDTO> obtenerDetalleReservacionAgencia(int reservacionId, int agenciaId)
```

Retorna el detalle completo de una reservacion especifica verificando que pertenezca a la agencia indicada. Primero obtiene el ID del usuario webservice de la agencia y luego filtra la reservacion por ese usuario. o la reservacion no le pertenece.

- **Param** `reservacionId` - ID de la reservacion a consultar.
- **Param** `agenciaId` - ID de la agencia propietaria de la reservacion.
- **Returns** - lista de ReservacionDetalleDTO con una entrada por cada habitacion, o lista vacia si la agencia no existe

---

## ReservacionRepository

> Repository para el acceso a datos relacionados con reservaciones. Maneja consultas y actualizaciones sobre reservaciones, detalles e imagenes.

```java
public double[] obtenerPrecios(int habitacionId)
```

Retorna los precios y la capacidad maxima de una habitacion segun su tipo.

- **Param** `habitacionId` - ID de la habitacion a consultar.
- **Returns** - arreglo con precio por noche, precio por persona y capacidad maxima.
- **Throws** `RuntimeException` - si la habitacion no existe en la base de datos.

---

```java
public boolean existeTraslape(int habitacionId, Date fechaCheckIn, Date fechaCheckOut)
```

Verifica si existe un traslape de fechas para una habitacion con reservaciones activas. Solo considera reservaciones en estado pendiente o confirmada.

- **Param** `habitacionId` - ID de la habitacion a verificar.
- **Param** `fechaCheckIn` - fecha de entrada de la nueva reservacion.
- **Param** `fechaCheckOut` - fecha de salida de la nueva reservacion.
- **Returns** - true si existe al menos un traslape, false en caso contrario.

---

```java
public void expirarPendientesDeUsuario(int usuarioId, int reservacionIdExcluir)
```

Marca como expiradas todas las reservaciones pendientes de un usuario, exceptuando la reservacion que se acaba de crear o actualizar.

- **Param** `usuarioId` - ID del usuario dueno de las reservaciones.
- **Param** `reservacionIdExcluir` - ID de la reservacion que no debe expirar.

---

```java
public int crearReservacion(String noReservacion, double total, int usuarioId,
```

Inserta una nueva reservacion en la base de datos con estado inicial pendiente.

- **Param** `noReservacion` - codigo unico que identifica la reservacion.
- **Param** `total` - monto total de la reservacion.
- **Param** `usuarioId` - ID del usuario que realiza la reservacion.
- **Param** `fechaCreacion` - fecha y hora en que se creo la reservacion.
- **Param** `fechaExpiracion` - fecha y hora en que expira la reservacion si no se confirma.
- **Returns** - ID generado por la base de datos para la nueva reservacion.

---

```java
public void crearDetalle(int reservacionId, int habitacionId,
```

Inserta el detalle de una habitacion asociado a una reservacion existente.

- **Param** `reservacionId` - ID de la reservacion a la que pertenece el detalle.
- **Param** `habitacionId` - ID de la habitacion reservada.
- **Param** `fechaCheckIn` - fecha de entrada a la habitacion.
- **Param** `fechaCheckOut` - fecha de salida de la habitacion.
- **Param** `cantidadPersonas` - numero de personas para esta habitacion.
- **Param** `total` - costo total calculado para este detalle.

---

```java
public Object[] obtenerReservacion(int reservacionId)
```

Retorna los datos principales de una reservacion junto con su estado actual.

- **Param** `reservacionId` - ID de la reservacion a consultar.
- **Returns** - arreglo con los campos de la reservacion, o null si no existe.

---

```java
public List<ReservacionDetalleDTO> obtenerReservacionesDeUsuario(int usuarioId)
```

Retorna todas las reservaciones de un usuario con el detalle completo de cada habitacion. Incluye informacion del hotel, tipo de habitacion, cama y fechas de cada detalle.

- **Param** `usuarioId` - ID del usuario cuyas reservaciones se desean consultar.
- **Returns** - lista de DTOs con la informacion consolidada de reservaciones y detalles.

---

```java
public List<Integer> obtenerImagenesHotel(int hotelId)
```

Retorna los IDs de todas las imagenes registradas para un hotel.

- **Param** `hotelId` - ID del hotel del que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes del hotel.

---

```java
public List<Integer> obtenerImagenesHabitacion(int habitacionId)
```

Retorna los IDs de todas las imagenes registradas para una habitacion.

- **Param** `habitacionId` - ID de la habitacion de la que se quieren obtener las imagenes.
- **Returns** - lista de IDs de imagenes de la habitacion.

---

```java
public int expirarReservacionesVencidas()
```

Marca como expiradas todas las reservaciones pendientes cuya fecha de expiracion ya paso.

- **Returns** - numero de filas actualizadas en la base de datos.

---

## SesionRepository

> Repository para el acceso a datos relacionados con sesiones de usuario. Actualmente provee consultas sobre roles del sistema.

```java
public String obtenerNombreRol(int rolId)
```

Retorna el nombre del rol correspondiente a un ID dado. Si el rol no existe, devuelve el valor por defecto "Desconocido".

- **Param** `rolId` - ID del rol a consultar.
- **Returns** - nombre del rol, o "Desconocido" si no se encuentra.

---

## UsuarioNacionalidadRepository

> Repository para gestionar la relacion entre usuarios y sus nacionalidades.

```java
public void asignarNacionalidades(int usuarioId, List<Integer> nacionalidadIds)
```

Inserta en la base de datos las nacionalidades asociadas a un usuario. Ejecuta una insercion por cada nacionalidad recibida en la lista.

- **Param** `usuarioId` - ID del usuario al que se le asignan las nacionalidades.
- **Param** `nacionalidadIds` - lista de IDs de nacionalidades a asociar.

---

## UsuarioRepository

> Repository para el acceso a datos de usuarios. Cubre validaciones de unicidad, creacion, consulta de perfil, gestion de contrasenas y operaciones administrativas de rol.

```java
public boolean existeUsername(String username)
```

Verifica si ya existe un usuario con el username dado.

- **Param** `username` - nombre de usuario a verificar.
- **Returns** - true si el username ya esta en uso, false en caso contrario.

---

```java
public boolean existeCorreo(String correo)
```

Verifica si ya existe un usuario registrado con el correo dado.

- **Param** `correo` - correo electronico a verificar.
- **Returns** - true si el correo ya esta en uso, false en caso contrario.

---

```java
public boolean existePasaporte(String pasaporte)
```

Verifica si ya existe un usuario registrado con el numero de pasaporte dado. Retorna false directamente si el pasaporte es nulo o esta vacio.

- **Param** `pasaporte` - numero de pasaporte a verificar.
- **Returns** - true si el pasaporte ya esta en uso, false en caso contrario.

---

```java
public int crearUsuario(
```

Inserta un nuevo usuario en la base de datos con rol de cliente por defecto.

- **Param** `correo` - correo electronico del usuario.
- **Param** `contrasenaHasheada` - contrasena ya procesada con hash.
- **Param** `pasaporte` - numero de pasaporte del usuario, puede ser nulo.
- **Param** `username` - nombre de usuario unico.
- **Param** `nombre` - nombre de pila del usuario.
- **Param** `apellido` - apellido del usuario.
- **Param** `telefono` - numero de telefono de contacto.
- **Param** `fechaNacimiento` - fecha de nacimiento del usuario.
- **Param** `ciudadId` - ID de la ciudad de residencia del usuario.
- **Returns** - ID generado por la base de datos para el nuevo usuario.

---

```java
public UsuarioPerfilResponseDTO obtenerPerfil(int usuarioId)
```

Retorna el perfil completo de un usuario incluyendo su ciudad y pais.

- **Param** `usuarioId` - ID del usuario a consultar.
- **Returns** - DTO con los datos del perfil, o null si el usuario no existe.

---

```java
public List<String> obtenerNacionalidades(int usuarioId)
```

Retorna los nombres de las nacionalidades registradas para un usuario.

- **Param** `usuarioId` - ID del usuario a consultar.
- **Returns** - lista de nombres de nacionalidades del usuario.

---

```java
public void actualizarTelefono(int usuarioId, String telefono)
```

Actualiza el numero de telefono de un usuario.

- **Param** `usuarioId` - ID del usuario a modificar.
- **Param** `telefono` - nuevo numero de telefono.

---

```java
public String obtenerContrasena(int usuarioId)
```

Retorna la contrasena hasheada almacenada para un usuario.

- **Param** `usuarioId` - ID del usuario a consultar.
- **Returns** - contrasena hasheada, o null si el usuario no existe.

---

```java
public void actualizarContrasena(int usuarioId, String contrasenaHasheada)
```

Actualiza la contrasena de un usuario con el nuevo hash proporcionado.

- **Param** `usuarioId` - ID del usuario a modificar.
- **Param** `contrasenaHasheada` - nueva contrasena ya procesada con hash.

---

```java
public List<UsuarioAdminDTO> listarTodosConRol()
```

Retorna todos los usuarios del sistema con su rol, ciudad y pais asignados.

- **Returns** - lista de DTOs con la informacion administrativa de cada usuario.

---

```java
public void actualizarRol(int usuarioId, int nuevoRolId)
```

Actualiza el rol asignado a un usuario.

- **Param** `usuarioId` - ID del usuario a modificar.
- **Param** `nuevoRolId` - ID del nuevo rol a asignar.

---
