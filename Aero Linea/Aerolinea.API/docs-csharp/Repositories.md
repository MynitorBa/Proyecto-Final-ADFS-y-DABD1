# Repositories

## AdminVueloRepository

> Repositorio de administracion de vuelos. Gestiona la creacion, cancelacion, historial y disponibilidad de aviones y tripulacion en la base de datos.

```csharp
public async Task<int> CrearVuelo(CrearVueloAdminDTO dto)
```

Crea un nuevo vuelo en la base de datos dentro de una transaccion. Verifica o crea la ruta, calcula la hora de llegada segun zonas horarias, valida la capacidad del avion y asigna la tripulacion indicada. Retorna el ID del vuelo creado.

---

```csharp
public async Task<List<VueloHistorialDTO>> ObtenerHistorialVuelos()
```

Obtiene el historial completo de vuelos con informacion de ruta, avion, estado, boletos vendidos y zonas horarias. Los resultados se ordenan por fecha descendente.

---

```csharp
public async Task<bool> CancelarVuelo(int vueloId)
```

Cancela un vuelo activo o en curso. Marca el vuelo con estado Cancelado, cancela todos los boletos activos y actualiza las reservaciones relacionadas al estado Cancelado dentro de una transaccion atomica.

---

```csharp
public async Task<HashSet<int>> ObtenerAvionesOcupados(
```

Devuelve los IDs de aviones que no estan disponibles para un vuelo nuevo. Un avion no esta disponible si: a) Todavia esta en vuelo al momento de la nueva salida. b) Aterrizo en el mismo aeropuerto de origen hace menos de 24 horas. c) Aterrizo en un aeropuerto diferente hace menos de 48 horas. FechaLlegada + HoraLlegada define el momento real de aterrizaje.

---

```csharp
public async Task<HashSet<int>> ObtenerTripulantesOcupados(DateTime fecha, TimeSpan horaSalida)
```

Devuelve los IDs de tripulantes que no estan disponibles para la fecha y hora indicada. Un tripulante no esta disponible si: a) Tiene un vuelo asignado el mismo dia con estado activo. b) Su vuelo mas reciente finaliza menos de 24 horas antes de la salida solicitada. FechaLlegada + HoraLlegada define el momento real de fin del vuelo.

---

## AeropuertoRepository

> Repositorio de aeropuertos. Gestiona consultas, creacion, actualizacion, eliminacion e imagenes de aeropuertos, asi como la resolucion de zonas horarias, ciudades y paises relacionados.

```csharp
public async Task<List<AeropuertoDTO>> ObtenerTodos()
```

Retorna la lista completa de aeropuertos con ciudad, pais, imagen y zona horaria IANA. Ordenados alfabeticamente por nombre.

---

```csharp
public async Task<AeropuertoDTO?> ObtenerPorId(int id)
```

Retorna un aeropuerto especifico con todos sus datos de ciudad, pais, imagen y zona horaria. Retorna null si no existe el ID dado.

---

```csharp
public async Task<string?> VerificarDuplicado(string nombre, string codigo, int? excludeId = null)
```

Verifica si ya existe un aeropuerto con el mismo nombre o codigo IATA. Retorna el nombre del campo duplicado ('nombre' o 'codigo') o null si no hay conflicto. El parametro excludeId permite ignorar el propio registro al editar.

---

```csharp
public async Task<int?> ObtenerIdPorCodigo(string codigo)
```

Busca el ID de un aeropuerto por su codigo IATA. Retorna null si no existe.

---

```csharp
public async Task<int> Crear(Aeropuerto aeropuerto)
```

Inserta un nuevo aeropuerto en la base de datos. Si la columna ZonaHorariaID ya existe en el esquema, la incluye en la insercion. Retorna el ID generado.

---

```csharp
public async Task<bool> Actualizar(Aeropuerto aeropuerto)
```

Actualiza los datos de un aeropuerto existente. Incluye ZonaHorariaID si la columna existe en el esquema actual. Retorna true si se afecto al menos una fila.

---

```csharp
public async Task<bool> Eliminar(int id)
```

Elimina un aeropuerto y su imagen asociada de la base de datos. Retorna true si se elimino correctamente.

---

```csharp
public async Task<int?> ObtenerOCrearZonaHoraria(string? nombreIana)
```

Busca una zona horaria IANA en el catalogo por nombre. Si no existe la crea. Retorna null si el nombre proporcionado es nulo o vacio.

---

```csharp
public async Task GuardarImagen(int aeropuertoId, string imagenBase64)
```

Guarda o actualiza la imagen en Base64 de un aeropuerto. Usa un patron UPSERT: actualiza si ya existe registro, inserta si no.

---

```csharp
public async Task EliminarImagen(int aeropuertoId)
```

Elimina la imagen asociada a un aeropuerto segun su ID.

---

```csharp
public async Task<List<DateTime>> ObtenerFechasConVuelos()
```

Retorna las fechas futuras que tienen al menos un vuelo programado, ordenadas de forma ascendente. Se usa para resaltar dias disponibles en el calendario.

---

```csharp
public async Task<List<DateTime>> ObtenerFechasConVuelosPorRuta(
```

Retorna las fechas con vuelos disponibles para una ruta especifica, considerando cantidad de pasajeros, clase y numero maximo de escalas. Utiliza BFS por capas para incluir fechas de itinerarios con conexiones, igual que VueloRepository.

---

```csharp
public async Task<List<CiudadDTO>> ObtenerCiudades()
```

Retorna la lista de ciudades con su pais, ordenadas por pais y ciudad. Incluye un campo NombreCompleto con el formato "Ciudad, Pais".

---

```csharp
public async Task<int> ObtenerOCrearPais(string nombrePais)
```

Busca un pais por nombre. Si no existe lo crea y retorna su ID. Se usa al registrar aeropuertos con paises nuevos.

---

```csharp
public async Task<int> ObtenerOCrearCiudad(string nombreCiudad, int paisId)
```

Busca una ciudad por nombre y pais. Si no existe la crea y retorna su ID. Se usa al registrar aeropuertos con ciudades nuevas.

---

```csharp
public async Task<int> ObtenerIdPorCiudad(int ciudadId, SqlConnection connection)
```

Obtiene el ID del primer aeropuerto registrado para una ciudad dada. Lanza una excepcion si la ciudad no tiene aeropuertos configurados.

---

## AgenciaRepository

> Repositorio de agencias. Gestiona la creacion, consulta, actualizacion y autenticacion de agencias de viaje, incluyendo la administracion de tokens, descuentos, estados y usuarios webservice asociados.

```csharp
public async Task<int> ObtenerRolUsuario(int usuarioId)
```

Consulta el RolID del usuario especificado. Retorna 0 si el usuario no existe.

---

```csharp
public async Task<bool> UsuarioYaTieneAgencia(int usuarioId)
```

Verifica si el usuario webservice dado ya tiene una agencia asignada. Retorna true si existe al menos una agencia para ese usuario.

---

```csharp
public async Task<AgenciaResponseDTO> CrearAgencia(CrearAgenciaDTO dto)
```

Crea una nueva agencia en la base de datos con estado Activo. Retorna el DTO con los datos de la agencia creada incluyendo su ID generado.

---

```csharp
public async Task<bool> GuardarTokens(int agenciaId, string tokenEntrada, string tokenSalida)
```

Actualiza los tokens de autenticacion (entrada y salida) de una agencia. Retorna true si se actualizo al menos una fila.

---

```csharp
public async Task<int?> ObtenerAgenciaIdPorTokenEntrada(string tokenEntrada)
```

Busca el ID de la agencia a partir de su token de entrada. Retorna null si no se encuentra ninguna agencia con ese token.

---

```csharp
public async Task<int?> ObtenerAgenciaIdPorURL(string urlAgencia)
```

Busca el ID de la agencia a partir de su URL. Retorna null si no existe.

---

```csharp
public async Task<AgenciaIdentidad?> ObtenerAgenciaPorToken(string token)
```

Obtiene la identidad basica de una agencia (ID, nombre y URL) a partir de su token de autenticacion de entrada. Retorna null si no existe.

---

```csharp
public async Task<decimal> ObtenerDescuento(int agenciaId)
```

Retorna el porcentaje de descuento configurado para la agencia indicada. Retorna 0 si no se encuentra la agencia.

---

```csharp
public async Task<MiAgenciaDTO?> ObtenerAgenciaPorUsuarioId(int usuarioId)
```

Retorna los datos de la agencia del usuario webservice indicado. Retorna null si el usuario no tiene ninguna agencia asignada.

---

```csharp
public async Task<List<AgenciaAdminDTO>> ObtenerTodasAdmin()
```

Retorna el listado completo de agencias con los datos del usuario webservice asignado. Destinado al uso exclusivo del panel de administracion.

---

```csharp
public async Task<List<UsuarioWebserviceDTO>> ObtenerWebserviceSinAgencia()
```

Retorna la lista de usuarios con rol Webservice que todavia no tienen una agencia asignada. Se usa en el panel de administracion para asignar agencias.

---

```csharp
public async Task<bool> AsignarUsuarioAAgencia(int agenciaId, int usuarioId)
```

Asigna o reasigna un usuario webservice a una agencia existente. Retorna true si la actualizacion fue exitosa.

---

```csharp
public async Task<bool> ActualizarDescuento(int agenciaId, decimal descuento)
```

Actualiza el porcentaje de descuento de una agencia. Retorna true si la actualizacion fue exitosa.

---

```csharp
public async Task<bool> ActualizarEstado(int agenciaId, int estadoId)
```

Actualiza el estado de una agencia (activa, suspendida, etc.). Retorna true si la actualizacion fue exitosa.

---

## AsientoAgenciaRepository

> Repositorio de asientos para agencias de viaje. Permite consultar el mapa de asientos de una reservacion y cambiar asientos de boletos dentro del contexto de una agencia autenticada.

```csharp
public async Task<List<AsientosVueloAgenciaDTO>> ObtenerAsientosPorReservacion(int reservacionId, int agenciaId)
```

Retorna el mapa de asientos de todos los vuelos de una reservacion de agencia. Incluye asientos ocupados, boletos propios de la agencia y el layout dinamico calculado segun la cantidad de boletos por clase en cada vuelo. Lanza una excepcion si la reservacion no pertenece a la agencia.

---

```csharp
public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int agenciaId)
```

Cambia el asiento de un boleto perteneciente a una reservacion de agencia. Verifica propiedad, validez del asiento en el layout del vuelo, disponibilidad con bloqueo pesimista y regla de clase antes de aplicar el cambio.

---

## AsientoRepository

> Repositorio de asientos para usuarios. Gestiona la consulta del mapa de asientos de un vuelo y el cambio de asiento de un boleto pendiente, verificando propiedad, disponibilidad con bloqueo pesimista y reglas de clase.

```csharp
public async Task<AsientosVueloDTO> ObtenerAsientosVuelo(int vueloId, int usuarioId)
```

Retorna el mapa completo de asientos de un vuelo para un usuario especifico. Calcula el layout dinamico por clase, identifica los asientos ocupados por otros pasajeros y lista los boletos del usuario en su reservacion activa.

---

```csharp
public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int usuarioId)
```

Cambia el asiento de un boleto del usuario autenticado. Verifica que el boleto pertenezca al usuario, que la reservacion este pendiente y no expirada, que el asiento sea valido en el layout del vuelo, que este disponible con bloqueo pesimista y que corresponda a la clase del boleto.

---

## AvionRepository

> Repositorio de aviones. Gestiona el CRUD completo de aviones e imagenes asociadas. Permite consultar la flota disponible para la asignacion de vuelos.

```csharp
public async Task<List<Avion>> ObtenerTodos()
```

Retorna la lista completa de aviones con su imagen asociada, ordenados por marca y modelo.

---

```csharp
public async Task<Avion?> ObtenerPorId(int id)
```

Retorna un avion especifico con su imagen. Retorna null si no existe el ID dado.

---

```csharp
public async Task<int> Crear(Avion avion)
```

Inserta un nuevo avion en la base de datos y retorna el ID generado.

---

```csharp
public async Task<bool> Actualizar(Avion avion)
```

Actualiza el modelo, marca y capacidad de un avion existente. Retorna true si se modifico al menos una fila.

---

```csharp
public async Task<bool> Eliminar(int id)
```

Elimina un avion y su imagen asociada de la base de datos. Retorna true si la eliminacion fue exitosa.

---

```csharp
public async Task GuardarImagen(int avionId, string imagenBase64)
```

Guarda o actualiza la imagen en Base64 de un avion. Si ya existe un registro de imagen lo actualiza; si no, lo inserta.

---

```csharp
public async Task EliminarImagen(int avionId)
```

Elimina la imagen asociada a un avion segun su ID.

---

```csharp
public async Task<string?> ObtenerImagen(int avionId)
```

Retorna la imagen en Base64 de un avion. Retorna null si no tiene imagen.

---

## CiudadRepository

> Repositorio de ciudades. Permite buscar o crear ciudades asociadas a un pais dentro de la base de datos, reutilizando registros existentes cuando sea posible.

```csharp
public async Task<int> ObtenerOCrearId(string nombre, int paisId, SqlConnection connection, SqlTransaction transaction = null)
```

Busca una ciudad por nombre y pais. Si no existe la crea y retorna su ID. Acepta una conexion y transaccion opcionales para participar en operaciones mayores.

---

## ComentarioRepository

> Repositorio de comentarios y resenas. Gestiona la creacion de resenas de ruta, respuestas a comentarios, consultas por ruta o usuario, y la inclusion del voto del usuario autenticado en los resultados.

```csharp
public async Task<ComentarioDTO> CrearComentarioRuta(int usuarioId, CrearComentarioRutaDTO dto)
```

Crea una resena con calificacion de estrellas para una ruta. Verifica que la ruta exista, que el usuario haya viajado en ella con una reservacion completada y que no haya dejado ya una resena en esa ruta. Retorna el DTO del comentario creado.

---

```csharp
public async Task<ComentarioDTO> CrearRespuesta(int usuarioId, CrearRespuestaDTO dto)
```

Crea una respuesta a un comentario existente. Verifica que el comentario padre exista y obtiene la ruta asociada para vincular la respuesta. Las respuestas no incluyen calificacion de estrellas. Retorna el DTO del comentario creado.

---

```csharp
public async Task<List<ComentarioDTO>> ObtenerComentariosPorRuta(int rutaId)
```

Retorna todos los comentarios y respuestas de una ruta especifica, agrupados para que las respuestas aparezcan junto a su comentario padre, ordenados cronologicamente.

---

```csharp
public async Task<List<ComentarioConVotoDTO>> ObtenerTodosConVoto(int usuarioId)
```

Retorna todos los comentarios del sistema incluyendo el voto del usuario autenticado en cada comentario. Ordenados por fecha descendente.

---

```csharp
public async Task<List<ComentarioDTO>> ObtenerComentariosPorUsuario(int usuarioId)
```

Retorna todos los comentarios realizados por un usuario especifico, ordenados por fecha descendente. Incluye resenas y respuestas.

---

```csharp
public async Task<List<ComentarioConVotoDTO>> ObtenerComentariosRutaConVoto(int rutaId, int usuarioId)
```

Retorna los comentarios de una ruta incluyendo el voto del usuario autenticado en cada comentario. Agrupa respuestas junto a su padre y ordena cronologicamente.

---

## ConfirmarReservacionAgenciaRepository

> Repositorio de confirmacion de reservaciones para agencias. Gestiona el proceso transaccional de confirmar una reservacion pendiente: validaciones, creacion de factura y actualizacion de estados de boletos y reservacion.

```csharp
public async Task<ConfirmacionAgenciaDTO> ConfirmarReservacion(
```

Confirma una reservacion pendiente de una agencia. Verifica pertenencia a la agencia, estado y expiracion de la reservacion, que todos los boletos tengan pasajero asignado, crea la factura y actualiza estados de boletos y reservacion dentro de una transaccion atomica. Retorna el DTO con los datos de la confirmacion realizada.

---

## DownRepository

> Repositorio de votos (downs) en comentarios. Permite votar a favor o en contra de un comentario, cambiar un voto existente, quitarlo y consultar el voto actual de un usuario en un comentario especifico.

```csharp
public async Task<ResultadoVotoDTO> VotarComentario(int usuarioId, VotarComentarioDTO dto)
```

Registra o actualiza el voto de un usuario en un comentario. Si es nuevo crea el registro; si ya voto igual lanza excepcion; si cambio de voto actualiza el registro. Actualiza el contador de downs en el comentario. Retorna el resultado con el nuevo conteo y la accion realizada.

---

```csharp
public async Task<ResultadoVotoDTO> QuitarVoto(int usuarioId, int comentarioId)
```

Elimina el voto de un usuario en un comentario y actualiza el contador de downs. Lanza excepcion si el usuario no habia votado en ese comentario. Retorna el resultado con el nuevo conteo y la accion 'voto_eliminado'.

---

```csharp
public async Task<int?> ObtenerVotoUsuario(int usuarioId, int comentarioId)
```

Retorna el valor del voto del usuario en el comentario indicado. Retorna null si el usuario no ha votado en ese comentario.

---

## FacturaRepository

> Repositorio de facturacion. Gestiona el proceso transaccional de compra de una reservacion por parte de un usuario: validaciones, creacion de factura y actualizacion de estados de boletos y reservacion.

```csharp
public async Task<CompraRealizadaDTO> ComprarReservacion(
```

Procesa el pago de una reservacion pendiente del usuario autenticado. Verifica propiedad, estado y expiracion de la reservacion, que todos los boletos tengan pasajero asignado, crea la factura y actualiza estados de boletos y reservacion dentro de una transaccion atomica. Retorna el DTO con los datos de la compra realizada.

---

## GestionReservacionRepository

> Repositorio de gestion de reservaciones para usuarios. Permite consultar el historial de reservaciones, obtener el detalle de una reservacion con sus boletos y pasajeros, cancelar reservaciones y verificar si se puede cancelar segun las reglas de tiempo antes del vuelo.

```csharp
public async Task<List<ReservacionDetalleDTO>> ObtenerReservacionesPorUsuario(int usuarioId)
```

Retorna el historial completo de reservaciones de un usuario con sus boletos asociados. Incluye informacion de vuelo, ruta, avion y datos del pasajero por cada boleto. Ordenadas por fecha de creacion descendente.

---

```csharp
public async Task<ReservacionDetalleDTO> ObtenerReservacionPorId(int reservacionId, int usuarioId)
```

Retorna el detalle completo de una reservacion especifica del usuario, incluyendo boletos con datos de vuelo, ruta, pasajeros y la factura si existe. Retorna null si la reservacion no existe o no pertenece al usuario.

---

```csharp
public async Task<ResumenReservacionesDTO> ObtenerResumenReservaciones(int usuarioId)
```

Retorna un resumen estadistico de las reservaciones del usuario: totales por estado (pendiente, confirmada, cancelada, expirada, completada) y el monto total gastado en reservaciones confirmadas y completadas.

---

```csharp
public async Task CancelarReservacion(int reservacionId, int usuarioId, string motivo)
```

Cancela una reservacion pendiente o confirmada del usuario. Valida que la reservacion pertenezca al usuario, que este en estado cancelable, y para reservaciones confirmadas que falten mas de 24 horas para el vuelo. Libera los boletos, devuelve disponibilidad a los vuelos y actualiza el estado de la reservacion con el motivo de cancelacion.

---

```csharp
public async Task<int> ObtenerUsuarioWebIdDeAgencia(int agenciaId)
```

Obtiene el ID del usuario webservice asociado a la agencia indicada. Se usa para operaciones de gestion que requieren conocer el propietario de la agencia.

---

```csharp
public async Task<PuedeCancelarDTO> PuedeCancelar(int reservacionId, int usuarioId)
```

Verifica si una reservacion puede ser cancelada por el usuario. Evalua el estado actual y, para reservaciones confirmadas, si faltan mas de 24 horas para el vuelo. Retorna un DTO con el resultado y el motivo de la decision.

---

## MetricasRepository

> Repositorio de metricas del sistema. Provee datos estadisticos sobre busquedas de vuelos, rutas mas buscadas, distribucion por tipo de acceso (Web/REST), ingresos reales desde facturas y listados paginados para exportacion o analisis.

```csharp
public async Task<List<BusquedasPorDiaDTO>> ObtenerBusquedasPorDia(
```

Retorna el conteo de busquedas agrupadas por dia dentro del rango de fechas indicado. Si no se indica rango se usan los ultimos 30 dias. Ordenado ascendente.

---

```csharp
public async Task<List<RutaMasBuscadaDTO>> ObtenerRutasMasBuscadas(
```

Retorna las 10 rutas mas buscadas en el rango de fechas indicado. Opcionalmente filtra por tipo de acceso (Web o REST). Incluye codigos y nombres de ciudad de origen y destino.

---

```csharp
public async Task<List<BusquedasPorTipoDTO>> ObtenerBusquedasPorTipo(
```

Retorna la cantidad de busquedas agrupadas por tipo (Web y REST) en el rango de fechas indicado. Se usa para graficar la proporcion de cada canal.

---

```csharp
public async Task<(IngresosKpiDTO kpi, List<DistribucionClaseDTO> dist)> ObtenerIngresos(
```

Calcula los ingresos reales del periodo a partir de las facturas emitidas. Retorna un KPI con totales y ticket promedio, y una distribucion de ingresos y boletos por clase (Turista y Ejecutiva). Excluye reservaciones canceladas.

---

```csharp
public async Task<MetricasResumenDTO> ObtenerResumen(DateTime? desde, DateTime? hasta)
```

Construye el resumen general de metricas del sistema combinando busquedas por dia, rutas mas buscadas, distribucion por tipo y datos de ingresos para el rango de fechas indicado.

---

```csharp
public async Task<ListadoBusquedasDTO> ObtenerListado(MetricasFiltroDTO filtro)
```

Retorna un listado paginado de busquedas con filtros por fecha, tipo de acceso y usuario. Incluye conteo total para calcular paginas. Se usa para la tabla de detalle del panel de metricas y para exportaciones.

---

## NacionalidadRepository

> Repositorio de nacionalidades. Permite consultar el catalogo completo y buscar o crear registros de nacionalidad segun su nombre.

```csharp
public async Task<List<Nacionalidad>> ObtenerTodas()
```

Retorna la lista completa de nacionalidades ordenadas alfabeticamente.

---

```csharp
public async Task<int> ObtenerOCrearId(string nombre, SqlConnection connection)
```

Busca una nacionalidad por nombre usando la conexion dada. Si no existe la crea y retorna su ID. Se usa al registrar usuarios con nacionalidades nuevas.

---

## PaisRepository

> Repositorio de paises. Permite buscar o crear registros de pais por nombre, reutilizando entradas existentes para mantener la integridad referencial.

```csharp
public async Task<int> ObtenerOCrearId(string nombre, SqlConnection connection, SqlTransaction transaction = null)
```

Busca un pais por nombre. Si no existe lo crea y retorna su ID. Acepta una conexion y transaccion opcionales para participar en transacciones externas.

---

## PerfilRepository

> Repositorio de perfil de usuario. Permite consultar y actualizar datos personales del usuario autenticado, incluyendo telefono y contrasena.

```csharp
public async Task<PerfilDTO?> ObtenerPerfil(int usuarioId)
```

Retorna el perfil completo del usuario incluyendo nombre, apellido, correo, telefono, pasaporte, fecha de nacimiento, ciudad y pais. Retorna null si el usuario no existe.

---

```csharp
public async Task<bool> ActualizarTelefono(int usuarioId, string telefono)
```

Actualiza el numero de telefono del usuario indicado. Retorna true si la actualizacion afecto al menos una fila.

---

```csharp
public async Task<string?> ObtenerHashContrasena(int usuarioId)
```

Retorna el hash de la contrasena almacenada para el usuario indicado. Retorna null si el usuario no existe.

---

```csharp
public async Task<bool> ActualizarContrasena(int usuarioId, string nuevoHash)
```

Actualiza el hash de contrasena del usuario con el nuevo valor proporcionado. Retorna true si la actualizacion afecto al menos una fila.

---

## ReservacionAgenciaRepository

> Repositorio de reservaciones para agencias. Gestiona la creacion de reservaciones con descuento aplicado, la expiracion manual de reservaciones pendientes y la asignacion de pasajeros a boletos de una reservacion existente.

```csharp
public async Task<ReservacionCreadaDTO> CrearReservacion(List<SeleccionVueloDTO> vuelos, decimal descuento, int agenciaId)
```

Crea una reservacion para la agencia indicada aplicando el descuento configurado. Verifica disponibilidad de boletos con UPDLOCK/ROWLOCK, descuenta asientos del vuelo, asigna asientos secuenciales y retorna el DTO con todos los boletos reservados. La transaccion usa nivel Serializable para evitar condiciones de carrera.

---

```csharp
public async Task ExpirarReservacion(int reservacionId)
```

Marca como expirada una reservacion pendiente de la agencia. Libera los boletos reservados devolviendo disponibilidad al vuelo correspondiente y cambia el estado de la reservacion a expirado (4). Lanza excepcion si la reservacion no esta pendiente.

---

```csharp
public async Task<bool> PerteneceAAgenciaYEstaPendiente(int reservacionId, int agenciaId)
```

Verifica si una reservacion pertenece a la agencia indicada y se encuentra en estado pendiente (1). Retorna true si se cumplen ambas condiciones.

---

```csharp
public async Task AgregarPasajerosAReservacion(int reservacionId, List<DatosPasajeroDTO> pasajeros, int agenciaId)
```

Asigna o actualiza los datos de pasajero para cada boleto de una reservacion de agencia. Verifica que la reservacion pertenezca a la agencia, este pendiente y no haya expirado. Crea o actualiza registros en DatosPasajero y los vincula al boleto correspondiente.

---

## ReservacionRepository

> Repositorio de reservaciones para usuarios. Gestiona la creacion de reservaciones con asignacion automatica de asientos, la liberacion de reservaciones expiradas, la asignacion de pasajeros a boletos, la confirmacion y el completado automatico de reservaciones cuyos vuelos ya aterrizaron.

```csharp
public async Task<ReservacionCreadaDTO> CrearReservacion(int? usuarioId, List<SeleccionVueloDTO> vuelos)
```

Crea una nueva reservacion para el usuario indicado. Si el usuario ya tiene reservaciones pendientes, las expira liberando sus asientos antes de continuar. Verifica disponibilidad con UPDLOCK/ROWLOCK, descuenta boletos del vuelo, asigna asientos secuenciales y retorna el DTO con todos los boletos reservados. La transaccion usa nivel Serializable para evitar condiciones de carrera.

---

```csharp
public async Task<int> LiberarReservasExpiradas()
```

Busca todas las reservaciones en estado pendiente cuya fecha de expiracion ya paso, libera sus boletos devolviendo disponibilidad a los vuelos y cambia el estado de cada reservacion a expirado (4). Retorna la cantidad de reservaciones procesadas.

---

```csharp
public async Task AgregarPasajerosAReservacion(int reservacionId, List<DatosPasajeroDTO> pasajeros)
```

Asigna o actualiza los datos de pasajero para cada boleto de una reservacion de usuario. Verifica que la reservacion exista, este pendiente y no haya expirado. Crea o actualiza registros en DatosPasajero y los vincula al boleto correspondiente.

---

```csharp
public async Task ConfirmarReservacion(int reservacionId)
```

Confirma una reservacion pendiente cambiando el estado de todos sus boletos a Vendido (3) y el estado de la reservacion a Confirmada (2). Verifica que todos los boletos tengan pasajero asignado antes de confirmar.

---

```csharp
public async Task<int> CompletarReservaciones()
```

Busca reservaciones confirmadas (estado 2) cuyos vuelos ya aterrizaron y las marca como completadas (estado 5). Solo completa reservaciones donde todos los vuelos asociados tienen estado 3 (finalizado). Retorna la cantidad de reservaciones completadas.

---

## RutaAgenciaRepository

> Repositorio de rutas para agencias. Proporciona acceso de solo lectura al catalogo completo de rutas disponibles con informacion de ciudad, pais y duracion estimada de cada trayecto.

```csharp
public async Task<List<RutaAgenciaDTO>> ObtenerTodasLasRutas()
```

Retorna la lista completa de rutas con ciudad y pais de origen y destino obtenidos a traves de los aeropuertos asociados a cada ruta.

---

## RutaRepository

> Repositorio de rutas. Permite listar, crear, buscar y actualizar rutas entre aeropuertos. Incluye introspeccion de esquema para soportar instalaciones con o sin la tabla ZonaHoraria, adaptando las consultas segun la estructura disponible.

```csharp
public async Task<List<RutaDTO>> ObtenerTodas()
```

Retorna todas las rutas con informacion de aeropuertos y zonas horarias. Si la tabla ZonaHoraria o la columna ZonaHorariaID no existen en el esquema actual, omite los JOINs correspondientes y retorna null en esos campos.

---

```csharp
public async Task<bool> ActualizarDuracion(int rutaId, int minutos)
```

Actualiza la duracion estimada en minutos de una ruta existente. Retorna true si la actualizacion afecto al menos una fila.

---

```csharp
public async Task<(int duracion, string? tzOrigen, string? tzDestino)> ObtenerInfoRuta(
```

Obtiene la duracion estimada y las zonas horarias de origen y destino para calcular la hora de llegada de un vuelo dado el par de aeropuertos. Si la tabla ZonaHoraria no existe en el esquema, retorna null en las zonas horarias. Retorna (120, null, null) como fallback si la ruta no existe.

---

```csharp
public async Task<bool> ExisteRuta(int origenId, int destinoId)
```

Verifica si existe una ruta directa entre los aeropuertos de origen y destino indicados. Retorna true si existe al menos una ruta con ese par exacto origen-destino.

---

```csharp
public async Task<int> CrearRuta(int origenId, int destinoId, int duracionEstimada = 120)
```

Crea una nueva ruta entre los aeropuertos indicados con la duracion estimada dada. Si ya existe una ruta con el mismo par origen-destino, retorna su ID sin duplicar.

---

## TripulacionRepository

> Repositorio de tripulacion. Gestiona el CRUD completo de miembros de tripulacion y sus imagenes almacenadas directamente en la tabla MiembroTripulacion. Tambien permite consultar los roles de tripulacion disponibles.

```csharp
public async Task<List<Tripulante>> ObtenerTodos()
```

Retorna la lista de todos los miembros de tripulacion ordenados por ID, incluyendo su imagen en Base64 si esta disponible.

---

```csharp
public async Task<Tripulante?> ObtenerPorId(int id)
```

Retorna el miembro de tripulacion con el ID indicado. Retorna null si no existe ningun tripulante con ese ID.

---

```csharp
public async Task<string?> ObtenerNombreRol(int rolId)
```

Retorna el nombre del cargo asociado al rol de tripulacion indicado. Retorna null si el rol no existe.

---

```csharp
public async Task<int> Crear(Tripulante tripulante)
```

Inserta un nuevo miembro de tripulacion en la base de datos incluyendo su imagen en Base64 si se proporciona. Retorna el ID generado.

---

```csharp
public async Task<bool> Actualizar(Tripulante tripulante)
```

Actualiza los datos de un miembro de tripulacion existente. Si se proporciona una nueva imagen la incluye en la actualizacion; de lo contrario conserva la imagen anterior sin modificarla. Retorna true si se actualizo al menos una fila.

---

```csharp
public async Task<bool> Eliminar(int id)
```

Elimina el miembro de tripulacion con el ID indicado. Retorna true si se elimino al menos una fila.

---

```csharp
public async Task GuardarImagen(int tripulanteId, string imagenBase64)
```

Guarda o reemplaza la imagen en Base64 del tripulante indicado actualizando directamente la columna Imagen de MiembroTripulacion.

---

```csharp
public async Task EliminarImagen(int tripulanteId)
```

Elimina la imagen del tripulante indicado estableciendo NULL en la columna Imagen.

---

```csharp
public async Task<List<RolTripulacion>> ObtenerRoles()
```

Retorna la lista de todos los roles de tripulacion disponibles ordenados por ID.

---

## UsuarioRepository

> Repositorio de usuarios. Gestiona la creacion, consulta y actualizacion de usuarios, incluyendo la asignacion de nacionalidades, verificacion de duplicados en registro, busqueda por credenciales para autenticacion y administracion de roles.

```csharp
public async Task<int> CrearUsuario(Usuario usuario)
```

Inserta un nuevo usuario en la base de datos y retorna el ID generado. Los campos opcionales como telefono, fecha de nacimiento y ciudad aceptan null o valor por defecto y se almacenan como DBNull en la base de datos.

---

```csharp
public async Task AgregarNacionalidades(int usuarioId, List<string> nacionalidades)
```

Asocia una lista de nacionalidades a un usuario recien creado. Por cada nacionalidad obtiene o crea el registro en la tabla Nacionalidad y luego inserta el vinculo en UsuarioNacionalidad.

---

```csharp
public async Task<RegisterConstraint> VerificarExistencia(string correo, string username, string pasaporte)
```

Verifica si ya existe un usuario con el correo, username o pasaporte indicados. Retorna un objeto RegisterConstraint con las flags correspondientes a cada campo que ya este en uso, para informar al cliente que datos causan conflicto.

---

```csharp
public async Task<Usuario?> ObtenerPorCorreoOUsername(string correoOUsername)
```

Busca un usuario por correo electronico o username para el proceso de autenticacion. Retorna el objeto Usuario completo con su hash de contrasena y rol, o null si no existe ningun usuario con esas credenciales.

---

```csharp
public async Task<string?> ObtenerNombreRol(int rolId)
```

Retorna el nombre del rol asociado al ID indicado. Retorna null si el rol no existe en la tabla Rol.

---

```csharp
public async Task<bool> ActualizarRol(int usuarioId, int nuevoRolId)
```

Actualiza el rol de un usuario al nuevo rol indicado. Retorna true si la actualizacion afecto al menos una fila.

---

```csharp
public async Task<bool> UsuarioExiste(int usuarioId)
```

Verifica si existe un usuario con el ID indicado. Retorna true si se encuentra al menos un registro con ese ID.

---

```csharp
public async Task<bool> RolExiste(int rolId)
```

Verifica si existe un rol con el ID indicado en la tabla Rol. Retorna true si se encuentra al menos un registro con ese ID.

---

```csharp
public async Task<List<Usuario>> ObtenerTodos()
```

Retorna la lista completa de usuarios registrados en el sistema, ordenados por ID. Incluye todos los campos del perfil pero no incluye las nacionalidades asociadas.

---

## VueloAdminInternoRepository

> Repositorio interno de actualizacion de estados de vuelos. Ejecuta el proceso automatico que transiciona vuelos de 'A tiempo' (1) a 'En transcurso' (2) o 'Finalizado' (3) segun la hora actual en relacion con sus horas de salida y llegada.

```csharp
public async Task<(int enTranscurso, int finalizados)> ActualizarEstadosVuelos()
```

Actualiza el estado de todos los vuelos cuya hora de salida o llegada ya ocurrio. Pasa a estado 2 (en transcurso) los vuelos que ya salieron pero no aterrizaron, y a estado 3 (finalizado) los vuelos cuya hora de llegada ya paso. Retorna una tupla con la cantidad de vuelos pasados a cada estado.

---

## VueloRepository

> Repositorio principal de vuelos. Soporta busqueda por termino libre, busqueda directa por ruta y fecha, y busqueda con escalas mediante BFS por capas. Tambien registra cada busqueda realizada para alimentar las metricas del sistema.

```csharp
public async Task<List<VueloDetalleDTO>> BusquedaGeneral(string query)
```

Realiza una busqueda libre de vuelos activos que coincidan con el termino ingresado contra ciudad, pais, aeropuerto o numero de vuelo. Retorna hasta 50 resultados futuros con disponibilidad, ordenados por fecha y hora de salida.

---

```csharp
public async Task<List<VueloDetalleDTO>> BuscarVuelos(
```

Busca vuelos directos entre dos aeropuertos en una fecha especifica con la cantidad de pasajeros indicada. Permite filtrar por clase (Turista o Ejecutivo). Retorna los vuelos disponibles ordenados por hora de salida, cada uno con su lista de tripulantes asignados.

---

```csharp
public async Task<List<VueloConEscalaDTO>> BuscarVuelosConEscalas(
```

Busca combinaciones de vuelos con escalas entre dos aeropuertos usando BFS por capas. Aplica reglas de escala entre 1h y 12h, limita la duracion total de vuelo al 1.5x de la ruta directa y evita ciclos en aeropuertos intermedios. Admite hasta maxEscalas (default 3) y filtra por clase y disponibilidad. Retorna los resultados ordenados por duracion total incluyendo tiempo de espera.

---

```csharp
public async Task GuardarBusqueda(
```

Registra una busqueda de vuelos en la tabla Busqueda para uso en metricas. Solo inserta el registro si existe una ruta directa entre los aeropuertos indicados.

---
