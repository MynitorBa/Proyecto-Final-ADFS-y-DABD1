# Dtos

## AgenciaDTO

> DTO que representa los datos de una agencia para transferencia entre capas. Incluye el nombre del estado resuelto mediante join con la tabla ESTADO.

```java
public int getId()
```

Retorna el identificador unico de la agencia.

- **Returns** - ID de la agencia.

---

```java
public String getNombre()
```

Retorna el nombre comercial de la agencia.

- **Returns** - nombre de la agencia.

---

```java
public String getCorreo()
```

Retorna el correo electronico de la agencia.

- **Returns** - correo de la agencia.

---

```java
public int getUsuarioWebisId()
```

Retorna el ID del usuario webservice asociado a la agencia.

- **Returns** - ID del usuario webservice.

---

```java
public double getPorcentajeDescuento()
```

Retorna el porcentaje de descuento aplicado a las reservaciones de la agencia.

- **Returns** - porcentaje de descuento.

---

```java
public int getEstadoId()
```

Retorna el ID del estado actual de la agencia.

- **Returns** - ID del estado.

---

```java
public String getEstado()
```

Retorna el nombre del estado actual resuelto desde la tabla ESTADO.

- **Returns** - nombre del estado.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la agencia.

- **Param** `id` - ID de la agencia.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre comercial de la agencia.

- **Param** `nombre` - nombre de la agencia.

---

```java
public void setCorreo(String correo)
```

Asigna el correo electronico de la agencia.

- **Param** `correo` - correo de la agencia.

---

```java
public void setUsuarioWebisId(int usuarioWebisId)
```

Asigna el ID del usuario webservice asociado a la agencia.

- **Param** `usuarioWebisId` - ID del usuario webservice.

---

```java
public void setPorcentajeDescuento(double porcentajeDescuento)
```

Asigna el porcentaje de descuento aplicado a las reservaciones de la agencia.

- **Param** `porcentajeDescuento` - porcentaje de descuento.

---

```java
public void setEstadoId(int estadoId)
```

Asigna el ID del estado actual de la agencia.

- **Param** `estadoId` - ID del estado.

---

```java
public void setEstado(String estado)
```

Asigna el nombre del estado actual resuelto desde la tabla ESTADO.

- **Param** `estado` - nombre del estado.

---

## AgenciaIdentidad

> DTO que representa la identidad basica de una agencia externa. Se usa para identificar la agencia durante el proceso de handshake.

```java
public int getId()
```

Retorna el identificador unico de la agencia.

- **Returns** - ID de la agencia.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la agencia.

- **Param** `id` - ID de la agencia.

---

```java
public String getNombre()
```

Retorna el nombre comercial de la agencia.

- **Returns** - nombre de la agencia.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre comercial de la agencia.

- **Param** `nombre` - nombre de la agencia.

---

```java
public String getUrlAgencia()
```

Retorna la URL base del sistema de la agencia externa.

- **Returns** - URL de la agencia.

---

```java
public void setUrlAgencia(String urlAgencia)
```

Asigna la URL base del sistema de la agencia externa.

- **Param** `urlAgencia` - URL de la agencia.

---

## AgregarAmenidadRequestDTO

> DTO con los datos necesarios para agregar o actualizar una amenidad en un hotel.

```java
public int getAmenidadId()
```

Retorna el ID de la amenidad del catalogo a asociar al hotel.

- **Returns** - ID de la amenidad.

---

```java
public String getDescripcion()
```

Retorna la descripcion personalizada de la amenidad para ese hotel.

- **Returns** - descripcion de la amenidad.

---

```java
public void setAmenidadId(int amenidadId)
```

Asigna el ID de la amenidad del catalogo a asociar al hotel.

- **Param** `amenidadId` - ID de la amenidad.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion personalizada de la amenidad para ese hotel.

- **Param** `descripcion` - descripcion de la amenidad.

---

## AmenidadDTO

> DTO que representa una amenidad del catalogo del sistema. Contiene el identificador y nombre de cada una de las amenidades disponibles.

```java
public AmenidadDTO(int id, String nombre)
```

Constructor para crear una amenidad con todos sus datos.

- **Param** `id` - identificador unico de la amenidad.
- **Param** `nombre` - nombre descriptivo de la amenidad.

---

```java
public int getId()
```

Retorna el identificador unico de la amenidad.

- **Returns** - ID de la amenidad.

---

```java
public String getNombre()
```

Retorna el nombre descriptivo de la amenidad.

- **Returns** - nombre de la amenidad.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la amenidad.

- **Param** `id` - ID de la amenidad.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre descriptivo de la amenidad.

- **Param** `nombre` - nombre de la amenidad.

---

## AmenidadHotelDTO

> DTO que representa una amenidad asignada a un hotel especifico. Incluye los IDs de sus imagenes para consultarlas via GET /imagenes/amenidad/{id}.

```java
public int getHotelAmenidadId()
```

Retorna el ID de la relacion entre el hotel y la amenidad.

- **Returns** - ID de la relacion hotel-amenidad.

---

```java
public void setHotelAmenidadId(int hotelAmenidadId)
```

Asigna el ID de la relacion entre el hotel y la amenidad.

- **Param** `hotelAmenidadId` - ID de la relacion hotel-amenidad.

---

```java
public int getAmenidadId()
```

Retorna el ID de la amenidad en el catalogo del sistema.

- **Returns** - ID de la amenidad.

---

```java
public void setAmenidadId(int amenidadId)
```

Asigna el ID de la amenidad en el catalogo del sistema.

- **Param** `amenidadId` - ID de la amenidad.

---

```java
public String getNombre()
```

Retorna el nombre de la amenidad.

- **Returns** - nombre de la amenidad.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre de la amenidad.

- **Param** `nombre` - nombre de la amenidad.

---

```java
public String getDescripcion()
```

Retorna la descripcion personalizada de la amenidad para este hotel.

- **Returns** - descripcion de la amenidad.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion personalizada de la amenidad para este hotel.

- **Param** `descripcion` - descripcion de la amenidad.

---

```java
public List<Integer> getImagenesIds()
```

Retorna los IDs de imagenes asociadas a esta amenidad.

- **Returns** - lista de IDs de imagenes.

---

```java
public void setImagenesIds(List<Integer> imagenesIds)
```

Asigna los IDs de imagenes asociadas a esta amenidad.

- **Param** `imagenesIds` - lista de IDs de imagenes.

---

## BusquedaRequestDTO

> DTO con los criterios de busqueda de habitaciones disponibles. Las fechas deben enviarse en formato YYYY-MM-DD.

```java
public String getPais()
```

Retorna el pais destino de la busqueda.

- **Returns** - nombre del pais.

---

```java
public void setPais(String pais)
```

Asigna el pais destino de la busqueda.

- **Param** `pais` - nombre del pais.

---

```java
public String getCiudad()
```

Retorna la ciudad destino de la busqueda.

- **Returns** - nombre de la ciudad.

---

```java
public void setCiudad(String ciudad)
```

Asigna la ciudad destino de la busqueda.

- **Param** `ciudad` - nombre de la ciudad.

---

```java
public String getFechaCheckIn()
```

Retorna la fecha de entrada en formato YYYY-MM-DD.

- **Returns** - fecha de check-in.

---

```java
public void setFechaCheckIn(String fechaCheckIn)
```

Asigna la fecha de entrada en formato YYYY-MM-DD.

- **Param** `fechaCheckIn` - fecha de check-in.

---

```java
public String getFechaCheckOut()
```

Retorna la fecha de salida en formato YYYY-MM-DD.

- **Returns** - fecha de check-out.

---

```java
public void setFechaCheckOut(String fechaCheckOut)
```

Asigna la fecha de salida en formato YYYY-MM-DD.

- **Param** `fechaCheckOut` - fecha de check-out.

---

```java
public int getCantidadPersonas()
```

Retorna el numero de personas para filtrar habitaciones con capacidad suficiente.

- **Returns** - cantidad de personas.

---

```java
public void setCantidadPersonas(int cantidadPersonas)
```

Asigna el numero de personas para filtrar habitaciones con capacidad suficiente.

- **Param** `cantidadPersonas` - cantidad de personas.

---

## CambiarContrasenaRequestDTO

> DTO con los datos necesarios para cambiar la contrasena de un usuario autenticado.

```java
public String getContrasenaActual()
```

Retorna la contrasena actual del usuario para verificar su identidad.

- **Returns** - contrasena actual.

---

```java
public void setContrasenaActual(String contrasenaActual)
```

Asigna la contrasena actual del usuario para verificar su identidad.

- **Param** `contrasenaActual` - contrasena actual.

---

```java
public String getContrasenaNueva()
```

Retorna la nueva contrasena que reemplazara a la actual.

- **Returns** - nueva contrasena.

---

```java
public void setContrasenaNueva(String contrasenaNueva)
```

Asigna la nueva contrasena que reemplazara a la actual.

- **Param** `contrasenaNueva` - nueva contrasena.

---

## CambiarRolRequestDTO

> DTO con el nuevo rol a asignar a un usuario desde el panel de administracion.

```java
public int getRolId()
```

Retorna el ID del rol a asignar al usuario.

- **Returns** - ID del rol.

---

```java
public void setRolId(int rolId)
```

Asigna el ID del rol a asignar al usuario.

- **Param** `rolId` - ID del rol.

---

## CambiarTelefonoRequestDTO

> DTO con el nuevo numero de telefono a asignar al usuario autenticado.

```java
public String getTelefono()
```

Retorna el nuevo numero de telefono del usuario.

- **Returns** - numero de telefono.

---

```java
public void setTelefono(String telefono)
```

Asigna el nuevo numero de telefono del usuario.

- **Param** `telefono` - numero de telefono.

---

## CancelacionRequestDTO

> DTO con el motivo de cancelacion de una reservacion.

```java
public String getMotivoCancelacion()
```

Retorna el motivo por el que se cancela la reservacion.

- **Returns** - motivo de cancelacion.

---

```java
public void setMotivoCancelacion(String motivoCancelacion)
```

Asigna el motivo por el que se cancela la reservacion.

- **Param** `motivoCancelacion` - motivo de cancelacion.

---

## CiudadDTO

> DTO que representa una ciudad del catalogo del sistema. Incluye el nombre del pais al que pertenece resuelto mediante join.

```java
public int getId()
```

Retorna el identificador unico de la ciudad.

- **Returns** - ID de la ciudad.

---

```java
public String getNombre()
```

Retorna el nombre de la ciudad.

- **Returns** - nombre de la ciudad.

---

```java
public int getPaisId()
```

Retorna el ID del pais al que pertenece la ciudad.

- **Returns** - ID del pais.

---

```java
public String getPaisNombre()
```

Retorna el nombre del pais al que pertenece la ciudad.

- **Returns** - nombre del pais.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la ciudad.

- **Param** `id` - ID de la ciudad.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre de la ciudad.

- **Param** `nombre` - nombre de la ciudad.

---

```java
public void setPaisId(int paisId)
```

Asigna el ID del pais al que pertenece la ciudad.

- **Param** `paisId` - ID del pais.

---

```java
public void setPaisNombre(String paisNombre)
```

Asigna el nombre del pais al que pertenece la ciudad.

- **Param** `paisNombre` - nombre del pais.

---

## ComentarioRequestDTO

> DTO con los datos necesarios para publicar un comentario o respuesta en un hotel. Si comentarioPadreId es null se trata de un comentario raiz; si tiene valor es una respuesta. La resena solo aplica para comentarios raiz, no para respuestas.

```java
public int getHotelId()
```

Retorna el ID del hotel sobre el que se publica el comentario.

- **Returns** - ID del hotel.

---

```java
public void setHotelId(int hotelId)
```

Asigna el ID del hotel sobre el que se publica el comentario.

- **Param** `hotelId` - ID del hotel.

---

```java
public Integer getComentarioPadreId()
```

Retorna el ID del comentario padre si es una respuesta, null si es un comentario raiz.

- **Returns** - ID del comentario padre, o null.

---

```java
public void setComentarioPadreId(Integer comentarioPadreId)
```

Asigna el ID del comentario padre si es una respuesta, null si es un comentario raiz.

- **Param** `comentarioPadreId` - ID del comentario padre, o null.

---

```java
public Integer getResena()
```

Retorna la puntuacion de la resena del hotel, null si el comentario es una respuesta.

- **Returns** - puntuacion de la resena, o null.

---

```java
public void setResena(Integer resena)
```

Asigna la puntuacion de la resena del hotel, null si el comentario es una respuesta.

- **Param** `resena` - puntuacion de la resena, o null.

---

```java
public String getContenido()
```

Retorna el texto del comentario o respuesta.

- **Returns** - contenido del comentario.

---

```java
public void setContenido(String contenido)
```

Asigna el texto del comentario o respuesta.

- **Param** `contenido` - contenido del comentario.

---

## ComentarioResponseDTO

> DTO con los datos completos de un comentario para retornar al cliente. Incluye informacion del autor, contenido, resena y total de downs recibidos.

```java
public class ComentarioResponseDTO
```

DTO con los datos completos de un comentario para retornar al cliente. Incluye informacion del autor, contenido, resena y total de downs recibidos.

---

```java
public int getId()
```

Retorna el identificador unico del comentario.

- **Returns** - ID del comentario.

---

```java
public void setId(int id)
```

Asigna el identificador unico del comentario.

- **Param** `id` - ID del comentario.

---

```java
public int getUsuarioId()
```

Retorna el ID del usuario que publico el comentario.

- **Returns** - ID del usuario.

---

```java
public void setUsuarioId(int usuarioId)
```

Asigna el ID del usuario que publico el comentario.

- **Param** `usuarioId` - ID del usuario.

---

```java
public String getUsername()
```

Retorna el nombre de usuario del autor del comentario.

- **Returns** - username del autor.

---

```java
public void setUsername(String username)
```

Asigna el nombre de usuario del autor del comentario.

- **Param** `username` - username del autor.

---

```java
public int getHotelId()
```

Retorna el ID del hotel al que pertenece el comentario.

- **Returns** - ID del hotel.

---

```java
public void setHotelId(int hotelId)
```

Asigna el ID del hotel al que pertenece el comentario.

- **Param** `hotelId` - ID del hotel.

---

```java
public Integer getComentarioPadreId()
```

Retorna el ID del comentario padre si es una respuesta, null si es un comentario raiz.

- **Returns** - ID del comentario padre, o null.

---

```java
public void setComentarioPadreId(Integer comentarioPadreId)
```

Asigna el ID del comentario padre si es una respuesta, null si es un comentario raiz.

- **Param** `comentarioPadreId` - ID del comentario padre, o null.

---

```java
public Integer getResena()
```

Retorna la puntuacion de la resena, null si el comentario es una respuesta.

- **Returns** - puntuacion de la resena, o null.

---

```java
public void setResena(Integer resena)
```

Asigna la puntuacion de la resena, null si el comentario es una respuesta.

- **Param** `resena` - puntuacion de la resena, o null.

---

```java
public String getContenido()
```

Retorna el texto del comentario o respuesta.

- **Returns** - contenido del comentario.

---

```java
public void setContenido(String contenido)
```

Asigna el texto del comentario o respuesta.

- **Param** `contenido` - contenido del comentario.

---

```java
public String getFecha()
```

Retorna la fecha de publicacion del comentario.

- **Returns** - fecha del comentario.

---

```java
public void setFecha(String fecha)
```

Asigna la fecha de publicacion del comentario.

- **Param** `fecha` - fecha del comentario.

---

```java
public int getDowns()
```

Retorna la cantidad total de downs recibidos por el comentario.

- **Returns** - total de downs.

---

```java
public void setDowns(int downs)
```

Asigna la cantidad total de downs recibidos por el comentario.

- **Param** `downs` - total de downs.

---

## CrearAgenciaRequestDTO

> DTO con los datos necesarios para crear una nueva agencia. El porcentaje de descuento siempre inicia en 0% y solo el administrador puede modificarlo.

```java
public String getNombre()
```

Retorna el nombre comercial de la nueva agencia.

- **Returns** - nombre de la agencia.

---

```java
public String getCorreo()
```

Retorna el correo electronico de la nueva agencia.

- **Returns** - correo de la agencia.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre comercial de la nueva agencia.

- **Param** `nombre` - nombre de la agencia.

---

```java
public void setCorreo(String correo)
```

Asigna el correo electronico de la nueva agencia.

- **Param** `correo` - correo de la agencia.

---

## CrearHabitacionRequestDTO

> DTO con los datos necesarios para crear una nueva habitacion en un hotel.

```java
public int getHotelId()
```

Retorna el ID del hotel al que pertenece la habitacion.

- **Returns** - ID del hotel.

---

```java
public void setHotelId(int hotelId)
```

Asigna el ID del hotel al que pertenece la habitacion.

- **Param** `hotelId` - ID del hotel.

---

```java
public int getTipoHabitacionId()
```

Retorna el ID del tipo de habitacion segun el catalogo del sistema.

- **Returns** - ID del tipo de habitacion.

---

```java
public void setTipoHabitacionId(int tipoHabitacionId)
```

Asigna el ID del tipo de habitacion segun el catalogo del sistema.

- **Param** `tipoHabitacionId` - ID del tipo de habitacion.

---

```java
public String getDescripcion()
```

Retorna la descripcion detallada de la habitacion.

- **Returns** - descripcion de la habitacion.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion detallada de la habitacion.

- **Param** `descripcion` - descripcion de la habitacion.

---

```java
public int getEstadoId()
```

Retorna el ID del estado inicial de la habitacion.

- **Returns** - ID del estado.

---

```java
public void setEstadoId(int estadoId)
```

Asigna el ID del estado inicial de la habitacion.

- **Param** `estadoId` - ID del estado.

---

## CrearHotelRequestDTO

> DTO con los datos necesarios para crear un nuevo hotel en el sistema. Si la ciudad o el pais indicados no existen, el servicio los crea automaticamente.

```java
public String getNombre()
```

Retorna el nombre del hotel.

- **Returns** - nombre del hotel.

---

```java
public String getDireccion()
```

Retorna la direccion fisica del hotel.

- **Returns** - direccion del hotel.

---

```java
public String getDescripcion()
```

Retorna la descripcion general del hotel.

- **Returns** - descripcion del hotel.

---

```java
public double getRating()
```

Retorna la calificacion inicial del hotel.

- **Returns** - rating del hotel.

---

```java
public int getEstadoId()
```

Retorna el ID del estado inicial del hotel.

- **Returns** - ID del estado.

---

```java
public String getCiudad()
```

Retorna el nombre de la ciudad donde se ubica el hotel.

- **Returns** - nombre de la ciudad.

---

```java
public String getPaisNombre()
```

Retorna el nombre del pais donde se ubica el hotel.

- **Returns** - nombre del pais.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre del hotel.

- **Param** `nombre` - nombre del hotel.

---

```java
public void setDireccion(String direccion)
```

Asigna la direccion fisica del hotel.

- **Param** `direccion` - direccion del hotel.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion general del hotel.

- **Param** `descripcion` - descripcion del hotel.

---

```java
public void setRating(double rating)
```

Asigna la calificacion inicial del hotel.

- **Param** `rating` - rating del hotel.

---

```java
public void setEstadoId(int estadoId)
```

Asigna el ID del estado inicial del hotel.

- **Param** `estadoId` - ID del estado.

---

```java
public void setCiudad(String ciudad)
```

Asigna el nombre de la ciudad donde se ubica el hotel.

- **Param** `ciudad` - nombre de la ciudad.

---

```java
public void setPaisNombre(String paisNombre)
```

Asigna el nombre del pais donde se ubica el hotel.

- **Param** `paisNombre` - nombre del pais.

---

## DownRequestDTO

> DTO con el valor de un down aplicado a un comentario. El valor debe ser 1 para down positivo o -1 para down negativo.

```java
public int getValor()
```

Retorna el valor del down, que puede ser 1 o -1.

- **Returns** - valor del down.

---

```java
public void setValor(int valor)
```

Asigna el valor del down, que puede ser 1 o -1.

- **Param** `valor` - valor del down.

---

## DownResponseDTO

> DTO con los datos completos de un down para retornar al cliente. Incluye informacion del comentario valorado y el hotel al que pertenece.

```java
public class DownResponseDTO
```

DTO con los datos completos de un down para retornar al cliente. Incluye informacion del comentario valorado y el hotel al que pertenece.

---

```java
public int getId()
```

Retorna el identificador unico del down.

- **Returns** - ID del down.

---

```java
public void setId(int id)
```

Asigna el identificador unico del down.

- **Param** `id` - ID del down.

---

```java
public int getComentarioId()
```

Retorna el ID del comentario sobre el que se aplico el down.

- **Returns** - ID del comentario.

---

```java
public void setComentarioId(int comentarioId)
```

Asigna el ID del comentario sobre el que se aplico el down.

- **Param** `comentarioId` - ID del comentario.

---

```java
public int getValor()
```

Retorna el valor del down, que puede ser 1 o -1.

- **Returns** - valor del down.

---

```java
public void setValor(int valor)
```

Asigna el valor del down, que puede ser 1 o -1.

- **Param** `valor` - valor del down.

---

```java
public String getFecha()
```

Retorna la fecha en que se registro el down.

- **Returns** - fecha del down.

---

```java
public void setFecha(String fecha)
```

Asigna la fecha en que se registro el down.

- **Param** `fecha` - fecha del down.

---

```java
public int getHotelId()
```

Retorna el ID del hotel al que pertenece el comentario valorado.

- **Returns** - ID del hotel.

---

```java
public void setHotelId(int hotelId)
```

Asigna el ID del hotel al que pertenece el comentario valorado.

- **Param** `hotelId` - ID del hotel.

---

```java
public String getContenidoComentario()
```

Retorna el texto del comentario sobre el que se aplico el down.

- **Returns** - contenido del comentario.

---

```java
public void setContenidoComentario(String contenidoComentario)
```

Asigna el texto del comentario sobre el que se aplico el down.

- **Param** `contenidoComentario` - contenido del comentario.

---

## EditarAgenciaRequestDTO

> DTO con los datos editables de una agencia desde el panel de administracion. Permite modificar nombre, correo, porcentaje de descuento y estado.

```java
public String getNombre()
```

Retorna el nombre comercial actualizado de la agencia.

- **Returns** - nombre de la agencia.

---

```java
public String getCorreo()
```

Retorna el correo electronico actualizado de la agencia.

- **Returns** - correo de la agencia.

---

```java
public double getPorcentajeDescuento()
```

Retorna el porcentaje de descuento a aplicar en reservaciones de la agencia.

- **Returns** - porcentaje de descuento.

---

```java
public int getEstadoId()
```

Retorna el ID del nuevo estado de la agencia.

- **Returns** - ID del estado.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre comercial actualizado de la agencia.

- **Param** `nombre` - nombre de la agencia.

---

```java
public void setCorreo(String correo)
```

Asigna el correo electronico actualizado de la agencia.

- **Param** `correo` - correo de la agencia.

---

```java
public void setPorcentajeDescuento(double porcentajeDescuento)
```

Asigna el porcentaje de descuento a aplicar en reservaciones de la agencia.

- **Param** `porcentajeDescuento` - porcentaje de descuento.

---

```java
public void setEstadoId(int estadoId)
```

Asigna el ID del nuevo estado de la agencia.

- **Param** `estadoId` - ID del estado.

---

## EditarHabitacionRequestDTO

> DTO con los datos editables de una habitacion desde el panel de administracion.

```java
public int getTipoHabitacionId()
```

Retorna el ID del tipo de habitacion segun el catalogo del sistema.

- **Returns** - ID del tipo de habitacion.

---

```java
public String getNumeroHabitacion()
```

Retorna el numero o identificador de la habitacion dentro del hotel.

- **Returns** - numero de la habitacion.

---

```java
public String getDescripcion()
```

Retorna la descripcion actualizada de la habitacion.

- **Returns** - descripcion de la habitacion.

---

```java
public int getEstadoId()
```

Retorna el ID del nuevo estado de la habitacion.

- **Returns** - ID del estado.

---

```java
public void setTipoHabitacionId(int tipoHabitacionId)
```

Asigna el ID del tipo de habitacion segun el catalogo del sistema.

- **Param** `tipoHabitacionId` - ID del tipo de habitacion.

---

```java
public void setNumeroHabitacion(String numeroHabitacion)
```

Asigna el numero o identificador de la habitacion dentro del hotel.

- **Param** `numeroHabitacion` - numero de la habitacion.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion actualizada de la habitacion.

- **Param** `descripcion` - descripcion de la habitacion.

---

```java
public void setEstadoId(int estadoId)
```

Asigna el ID del nuevo estado de la habitacion.

- **Param** `estadoId` - ID del estado.

---

## EditarHotelRequestDTO

> DTO con los datos editables de un hotel desde el panel de administracion.

```java
public String getNombre()
```

Retorna el nombre actualizado del hotel.

- **Returns** - nombre del hotel.

---

```java
public String getDireccion()
```

Retorna la direccion fisica actualizada del hotel.

- **Returns** - direccion del hotel.

---

```java
public String getDescripcion()
```

Retorna la descripcion actualizada del hotel.

- **Returns** - descripcion del hotel.

---

```java
public double getRating()
```

Retorna la calificacion actualizada del hotel.

- **Returns** - rating del hotel.

---

```java
public int getEstadoId()
```

Retorna el ID del nuevo estado del hotel.

- **Returns** - ID del estado.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre actualizado del hotel.

- **Param** `nombre` - nombre del hotel.

---

```java
public void setDireccion(String direccion)
```

Asigna la direccion fisica actualizada del hotel.

- **Param** `direccion` - direccion del hotel.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion actualizada del hotel.

- **Param** `descripcion` - descripcion del hotel.

---

```java
public void setRating(double rating)
```

Asigna la calificacion actualizada del hotel.

- **Param** `rating` - calificacion del hotel.

---

```java
public void setEstadoId(int estadoId)
```

Asigna el ID del nuevo estado del hotel.

- **Param** `estadoId` - ID del estado.

---

## HabitacionAdminDTO

> DTO con los datos completos de una habitacion para el panel de administracion. Incluye campos propios de la habitacion y campos resueltos mediante join con TipoHabitacion.

```java
public int getId()
```

Retorna el identificador unico de la habitacion.

- **Returns** - ID de la habitacion.

---

```java
public int getHotelId()
```

Retorna el ID del hotel al que pertenece la habitacion.

- **Returns** - ID del hotel.

---

```java
public int getTipoHabitacionId()
```

Retorna el ID del tipo de habitacion en el catalogo.

- **Returns** - ID del tipo de habitacion.

---

```java
public String getTipoHabitacion()
```

Retorna el nombre del tipo de habitacion resuelto desde TipoHabitacion.

- **Returns** - nombre del tipo de habitacion.

---

```java
public String getNumeroHabitacion()
```

Retorna el numero o identificador de la habitacion dentro del hotel.

- **Returns** - numero de la habitacion.

---

```java
public String getTipoCama()
```

Retorna el tipo de cama de la habitacion resuelto desde TipoHabitacion.

- **Returns** - tipo de cama.

---

```java
public double getPrecioPorPersona()
```

Retorna el precio adicional por persona extra sobre la capacidad base.

- **Returns** - precio por persona adicional.

---

```java
public double getPrecioPorNoche()
```

Retorna el precio base por noche de la habitacion.

- **Returns** - precio por noche.

---

```java
public int getCapacidadMaxima()
```

Retorna la capacidad maxima de personas admitidas en la habitacion.

- **Returns** - capacidad maxima.

---

```java
public double getMetrosCuadrados()
```

Retorna la superficie de la habitacion en metros cuadrados.

- **Returns** - metros cuadrados de la habitacion.

---

```java
public String getDescripcion()
```

Retorna la descripcion detallada de la habitacion.

- **Returns** - descripcion de la habitacion.

---

```java
public int getEstadoId()
```

Retorna el ID del estado actual de la habitacion.

- **Returns** - ID del estado.

---

```java
public String getEstado()
```

Retorna el nombre del estado actual de la habitacion.

- **Returns** - nombre del estado.

---

```java
public List<Integer> getImagenesIds()
```

Retorna los IDs de imagenes asociadas a la habitacion.

- **Returns** - lista de IDs de imagenes.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la habitacion.

- **Param** `id` - ID de la habitacion.

---

```java
public void setHotelId(int hotelId)
```

Asigna el ID del hotel al que pertenece la habitacion.

- **Param** `hotelId` - ID del hotel.

---

```java
public void setTipoHabitacionId(int tipoHabitacionId)
```

Asigna el ID del tipo de habitacion en el catalogo.

- **Param** `tipoHabitacionId` - ID del tipo de habitacion.

---

```java
public void setTipoHabitacion(String tipoHabitacion)
```

Asigna el nombre del tipo de habitacion resuelto desde TipoHabitacion.

- **Param** `tipoHabitacion` - nombre del tipo de habitacion.

---

```java
public void setNumeroHabitacion(String numeroHabitacion)
```

Asigna el numero o identificador de la habitacion dentro del hotel.

- **Param** `numeroHabitacion` - numero de la habitacion.

---

```java
public void setTipoCama(String tipoCama)
```

Asigna el tipo de cama de la habitacion resuelto desde TipoHabitacion.

- **Param** `tipoCama` - tipo de cama.

---

```java
public void setPrecioPorPersona(double precioPorPersona)
```

Asigna el precio adicional por persona extra sobre la capacidad base.

- **Param** `precioPorPersona` - precio por persona adicional.

---

```java
public void setPrecioPorNoche(double precioPorNoche)
```

Asigna el precio base por noche de la habitacion.

- **Param** `precioPorNoche` - precio por noche.

---

```java
public void setCapacidadMaxima(int capacidadMaxima)
```

Asigna la capacidad maxima de personas admitidas en la habitacion.

- **Param** `capacidadMaxima` - capacidad maxima.

---

```java
public void setMetrosCuadrados(double metrosCuadrados)
```

Asigna la superficie de la habitacion en metros cuadrados.

- **Param** `metrosCuadrados` - metros cuadrados de la habitacion.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion detallada de la habitacion.

- **Param** `descripcion` - descripcion de la habitacion.

---

```java
public void setEstadoId(int estadoId)
```

Asigna el ID del estado actual de la habitacion.

- **Param** `estadoId` - ID del estado.

---

```java
public void setEstado(String estado)
```

Asigna el nombre del estado actual de la habitacion.

- **Param** `estado` - nombre del estado.

---

```java
public void setImagenesIds(List<Integer> imagenesIds)
```

Asigna los IDs de imagenes asociadas a la habitacion.

- **Param** `imagenesIds` - lista de IDs de imagenes.

---

## HabitacionAgenciaDTO

> DTO que extiende HabitacionDTO con los precios ajustados por el descuento negociado para la agencia solicitante.

```java
public double getPorcentajeDescuento()
```

Retorna el porcentaje de descuento aplicado a los precios de la habitacion.

- **Returns** - porcentaje de descuento.

---

```java
public void setPorcentajeDescuento(double porcentajeDescuento)
```

Asigna el porcentaje de descuento aplicado a los precios de la habitacion.

- **Param** `porcentajeDescuento` - porcentaje de descuento.

---

```java
public double getPrecioPorNocheConDescuento()
```

Retorna el precio por noche luego de aplicar el descuento de la agencia.

- **Returns** - precio por noche con descuento.

---

```java
public void setPrecioPorNocheConDescuento(double precioPorNocheConDescuento)
```

Asigna el precio por noche luego de aplicar el descuento de la agencia.

- **Param** `precioPorNocheConDescuento` - precio por noche con descuento.

---

```java
public double getPrecioPorPersonaConDescuento()
```

Retorna el precio por persona luego de aplicar el descuento de la agencia.

- **Returns** - precio por persona con descuento.

---

```java
public void setPrecioPorPersonaConDescuento(double precioPorPersonaConDescuento)
```

Asigna el precio por persona luego de aplicar el descuento de la agencia.

- **Param** `precioPorPersonaConDescuento` - precio por persona con descuento.

---

## HabitacionAgenciaResponseDTO

> DTO con el desglose de precios de una habitacion calculado para una agencia. Incluye el total a pagar considerando noches y personas extra.

```java
public class HabitacionAgenciaResponseDTO
```

DTO con el desglose de precios de una habitacion calculado para una agencia. Incluye el total a pagar considerando noches y personas extra.

---

```java
public int getHabitacionId()
```

Retorna el ID de la habitacion cotizada.

- **Returns** - ID de la habitacion.

---

```java
public void setHabitacionId(int habitacionId)
```

Asigna el ID de la habitacion cotizada.

- **Param** `habitacionId` - ID de la habitacion.

---

```java
public double getPrecioPorNoche()
```

Retorna el precio base por noche aplicado a la reservacion.

- **Returns** - precio por noche.

---

```java
public void setPrecioPorNoche(double precioPorNoche)
```

Asigna el precio base por noche aplicado a la reservacion.

- **Param** `precioPorNoche` - precio por noche.

---

```java
public double getPrecioPorPersona()
```

Retorna el precio adicional por persona extra sobre la capacidad base.

- **Returns** - precio por persona adicional.

---

```java
public void setPrecioPorPersona(double precioPorPersona)
```

Asigna el precio adicional por persona extra sobre la capacidad base.

- **Param** `precioPorPersona` - precio por persona adicional.

---

```java
public int getPersonasExtra()
```

Retorna la cantidad de personas extra sobre la capacidad base de la habitacion.

- **Returns** - personas extra.

---

```java
public void setPersonasExtra(int personasExtra)
```

Asigna la cantidad de personas extra sobre la capacidad base de la habitacion.

- **Param** `personasExtra` - personas extra.

---

```java
public int getNoches()
```

Retorna el numero de noches de la estancia.

- **Returns** - cantidad de noches.

---

```java
public void setNoches(int noches)
```

Asigna el numero de noches de la estancia.

- **Param** `noches` - cantidad de noches.

---

```java
public double getTotal()
```

Retorna el monto total a pagar considerando noches, personas extra y descuento de agencia.

- **Returns** - total a pagar.

---

```java
public void setTotal(double total)
```

Asigna el monto total a pagar considerando noches, personas extra y descuento de agencia.

- **Param** `total` - monto total a pagar.

---

## HabitacionDTO

> DTO con los datos de una habitacion para mostrar en resultados de busqueda y detalle de hotel. Los IDs de imagenes se usan para consultarlas via GET /imagenes/habitacion/{id}.

```java
public int getId()
```

Retorna el identificador unico de la habitacion.

- **Returns** - ID de la habitacion.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la habitacion.

- **Param** `id` - ID de la habitacion.

---

```java
public String getTipoHabitacion()
```

Retorna el nombre del tipo de habitacion.

- **Returns** - tipo de habitacion.

---

```java
public void setTipoHabitacion(String tipoHabitacion)
```

Asigna el nombre del tipo de habitacion.

- **Param** `tipoHabitacion` - tipo de habitacion.

---

```java
public double getPrecioPorPersona()
```

Retorna el precio adicional por persona extra sobre la capacidad base.

- **Returns** - precio por persona adicional.

---

```java
public void setPrecioPorPersona(double precioPorPersona)
```

Asigna el precio adicional por persona extra sobre la capacidad base.

- **Param** `precioPorPersona` - precio por persona adicional.

---

```java
public double getPrecioPorNoche()
```

Retorna el precio base por noche de la habitacion.

- **Returns** - precio por noche.

---

```java
public void setPrecioPorNoche(double precioPorNoche)
```

Asigna el precio base por noche de la habitacion.

- **Param** `precioPorNoche` - precio por noche.

---

```java
public int getCapacidadMaxima()
```

Retorna la capacidad maxima de personas admitidas en la habitacion.

- **Returns** - capacidad maxima.

---

```java
public void setCapacidadMaxima(int capacidadMaxima)
```

Asigna la capacidad maxima de personas admitidas en la habitacion.

- **Param** `capacidadMaxima` - capacidad maxima.

---

```java
public String getTipoCama()
```

Retorna el tipo de cama disponible en la habitacion.

- **Returns** - tipo de cama.

---

```java
public void setTipoCama(String tipoCama)
```

Asigna el tipo de cama disponible en la habitacion.

- **Param** `tipoCama` - tipo de cama.

---

```java
public double getMetrosCuadrados()
```

Retorna la superficie de la habitacion en metros cuadrados.

- **Returns** - metros cuadrados de la habitacion.

---

```java
public void setMetrosCuadrados(double metrosCuadrados)
```

Asigna la superficie de la habitacion en metros cuadrados.

- **Param** `metrosCuadrados` - metros cuadrados de la habitacion.

---

```java
public String getDescripcion()
```

Retorna la descripcion detallada de la habitacion.

- **Returns** - descripcion de la habitacion.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion detallada de la habitacion.

- **Param** `descripcion` - descripcion de la habitacion.

---

```java
public String getEstado()
```

Retorna el nombre del estado actual de la habitacion.

- **Returns** - nombre del estado.

---

```java
public void setEstado(String estado)
```

Asigna el nombre del estado actual de la habitacion.

- **Param** `estado` - nombre del estado.

---

```java
public List<Integer> getImagenesIds()
```

Retorna los IDs de imagenes asociadas a la habitacion.

- **Returns** - lista de IDs de imagenes.

---

```java
public void setImagenesIds(List<Integer> imagenesIds)
```

Asigna los IDs de imagenes asociadas a la habitacion.

- **Param** `imagenesIds` - lista de IDs de imagenes.

---

## HabitacionReservaRequestDTO

> DTO con los datos de una habitacion dentro de una solicitud de reservacion. Las fechas deben enviarse en formato YYYY-MM-DD.

```java
public int getHabitacionId()
```

Retorna el ID de la habitacion a reservar.

- **Returns** - ID de la habitacion.

---

```java
public void setHabitacionId(int habitacionId)
```

Asigna el ID de la habitacion a reservar.

- **Param** `habitacionId` - ID de la habitacion.

---

```java
public int getCantidadPersonas()
```

Retorna el numero de personas que ocuparan la habitacion.

- **Returns** - cantidad de personas.

---

```java
public void setCantidadPersonas(int cantidadPersonas)
```

Asigna el numero de personas que ocuparan la habitacion.

- **Param** `cantidadPersonas` - cantidad de personas.

---

```java
public String getFechaCheckIn()
```

Retorna la fecha de entrada en formato YYYY-MM-DD.

- **Returns** - fecha de check-in.

---

```java
public void setFechaCheckIn(String fechaCheckIn)
```

Asigna la fecha de entrada en formato YYYY-MM-DD.

- **Param** `fechaCheckIn` - fecha de check-in.

---

```java
public String getFechaCheckOut()
```

Retorna la fecha de salida en formato YYYY-MM-DD.

- **Returns** - fecha de check-out.

---

```java
public void setFechaCheckOut(String fechaCheckOut)
```

Asigna la fecha de salida en formato YYYY-MM-DD.

- **Param** `fechaCheckOut` - fecha de check-out.

---

## HabitacionResumenDTO

> DTO con los datos minimos de una habitacion para usarse en listados y resumenes.

```java
public int getId()
```

Retorna el identificador unico de la habitacion.

- **Returns** - ID de la habitacion.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la habitacion.

- **Param** `id` - ID de la habitacion.

---

```java
public String getNumeroHabitacion()
```

Retorna el numero o identificador de la habitacion dentro del hotel.

- **Returns** - numero de la habitacion.

---

```java
public void setNumeroHabitacion(String numeroHabitacion)
```

Asigna el numero o identificador de la habitacion dentro del hotel.

- **Param** `numeroHabitacion` - numero de la habitacion.

---

## HandshakeRequestDTO

> DTO con los datos enviados por una agencia externa para iniciar el proceso de handshake. Los campos se mapean desde snake_case del JSON mediante anotaciones de Jackson.

```java
public String getTokenEntrada()
```

Retorna el token de entrada proporcionado por la agencia para autenticarse.

- **Returns** - token de entrada.

---

```java
public void setTokenEntrada(String tokenEntrada)
```

Asigna el token de entrada proporcionado por la agencia para autenticarse.

- **Param** `tokenEntrada` - token de entrada.

---

```java
public String getUrlAgencia()
```

Retorna la URL base del sistema de la agencia externa.

- **Returns** - URL de la agencia.

---

```java
public void setUrlAgencia(String urlAgencia)
```

Asigna la URL base del sistema de la agencia externa.

- **Param** `urlAgencia` - URL de la agencia.

---

## HandshakeResponseDTO

> DTO con la respuesta del servidor al proceso de handshake de una agencia externa. Contiene el token de salida que la agencia debera usar en peticiones posteriores.

```java
public class HandshakeResponseDTO
```

DTO con la respuesta del servidor al proceso de handshake de una agencia externa. Contiene el token de salida que la agencia debera usar en peticiones posteriores.

---

```java
public HandshakeResponseDTO(String tokenSalida)
```

Constructor que inicializa la respuesta con el token generado por el servidor.

- **Param** `tokenSalida` - token de salida que la agencia usara para autenticarse en siguientes peticiones.

---

```java
public String getTokenSalida()
```

Retorna el token de salida generado por el servidor para la agencia.

- **Returns** - token de salida.

---

```java
public void setTokenSalida(String tokenSalida)
```

Asigna el token de salida generado por el servidor para la agencia.

- **Param** `tokenSalida` - token de salida.

---

## HotelAdminDTO

> DTO con los datos completos de un hotel para el panel de administracion. Incluye informacion de ubicacion, estado, cantidad de habitaciones e imagenes asociadas.

```java
public int getId()
```

Retorna el identificador unico del hotel.

- **Returns** - ID del hotel.

---

```java
public String getNombre()
```

Retorna el nombre del hotel.

- **Returns** - nombre del hotel.

---

```java
public String getDireccion()
```

Retorna la direccion fisica del hotel.

- **Returns** - direccion del hotel.

---

```java
public String getDescripcion()
```

Retorna la descripcion general del hotel.

- **Returns** - descripcion del hotel.

---

```java
public double getRating()
```

Retorna la calificacion promedio del hotel.

- **Returns** - rating del hotel.

---

```java
public int getEstadoId()
```

Retorna el ID del estado actual del hotel.

- **Returns** - ID del estado.

---

```java
public String getEstado()
```

Retorna el nombre del estado actual del hotel.

- **Returns** - nombre del estado.

---

```java
public String getCiudad()
```

Retorna la ciudad donde se ubica el hotel.

- **Returns** - nombre de la ciudad.

---

```java
public String getPais()
```

Retorna el pais donde se ubica el hotel.

- **Returns** - nombre del pais.

---

```java
public int getCantidadHabitaciones()
```

Retorna el total de habitaciones registradas en el hotel.

- **Returns** - cantidad de habitaciones.

---

```java
public List<Integer> getImagenesIds()
```

Retorna los IDs de imagenes asociadas al hotel.

- **Returns** - lista de IDs de imagenes.

---

```java
public void setId(int id)
```

Asigna el identificador unico del hotel.

- **Param** `id` - ID del hotel.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre del hotel.

- **Param** `nombre` - nombre del hotel.

---

```java
public void setDireccion(String direccion)
```

Asigna la direccion fisica del hotel.

- **Param** `direccion` - direccion del hotel.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion general del hotel.

- **Param** `descripcion` - descripcion del hotel.

---

```java
public void setRating(double rating)
```

Asigna la calificacion promedio del hotel.

- **Param** `rating` - calificacion del hotel.

---

```java
public void setEstadoId(int estadoId)
```

Asigna el ID del estado actual del hotel.

- **Param** `estadoId` - ID del estado.

---

```java
public void setEstado(String estado)
```

Asigna el nombre del estado actual del hotel.

- **Param** `estado` - nombre del estado.

---

```java
public void setCiudad(String ciudad)
```

Asigna la ciudad donde se ubica el hotel.

- **Param** `ciudad` - nombre de la ciudad.

---

```java
public void setPais(String pais)
```

Asigna el pais donde se ubica el hotel.

- **Param** `pais` - nombre del pais.

---

```java
public void setCantidadHabitaciones(int cantidadHabitaciones)
```

Asigna el total de habitaciones registradas en el hotel.

- **Param** `cantidadHabitaciones` - total de habitaciones.

---

```java
public void setImagenesIds(List<Integer> imagenesIds)
```

Asigna los IDs de imagenes asociadas al hotel.

- **Param** `imagenesIds` - lista de IDs de imagenes.

---

## HotelAgenciaDTO

> DTO con los datos basicos de un hotel para mostrar en el catalogo de agencias externas.

```java
public int getId()
```

Retorna el identificador unico del hotel.

- **Returns** - ID del hotel.

---

```java
public void setId(int id)
```

Asigna el identificador unico del hotel.

- **Param** `id` - ID del hotel.

---

```java
public String getNombre()
```

Retorna el nombre del hotel.

- **Returns** - nombre del hotel.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre del hotel.

- **Param** `nombre` - nombre del hotel.

---

```java
public String getCiudad()
```

Retorna la ciudad donde se ubica el hotel.

- **Returns** - nombre de la ciudad.

---

```java
public void setCiudad(String ciudad)
```

Asigna la ciudad donde se ubica el hotel.

- **Param** `ciudad` - nombre de la ciudad.

---

```java
public String getPais()
```

Retorna el pais donde se ubica el hotel.

- **Returns** - nombre del pais.

---

```java
public void setPais(String pais)
```

Asigna el pais donde se ubica el hotel.

- **Param** `pais` - nombre del pais.

---

## HotelAmenidadDTO

> DTO que representa una amenidad asignada a un hotel especifico. Incluye la descripcion personalizada y los IDs de imagenes de la tabla ImagenHotelAmenidad.

```java
public int getId()
```

Retorna el ID del registro en la tabla HotelAmenidad.

- **Returns** - ID del registro.

---

```java
public int getHotelId()
```

Retorna el ID del hotel al que pertenece la amenidad.

- **Returns** - ID del hotel.

---

```java
public int getAmenidadId()
```

Retorna el ID de la amenidad en el catalogo del sistema.

- **Returns** - ID de la amenidad.

---

```java
public String getAmenidadNombre()
```

Retorna el nombre de la amenidad resuelto desde el catalogo.

- **Returns** - nombre de la amenidad.

---

```java
public String getDescripcion()
```

Retorna la descripcion personalizada de la amenidad para este hotel.

- **Returns** - descripcion de la amenidad.

---

```java
public List<Integer> getImagenesIds()
```

Retorna los IDs de imagenes asociadas a esta amenidad en el hotel.

- **Returns** - lista de IDs de imagenes.

---

```java
public void setId(int id)
```

Asigna el ID del registro en la tabla HotelAmenidad.

- **Param** `id` - ID del registro.

---

```java
public void setHotelId(int hotelId)
```

Asigna el ID del hotel al que pertenece la amenidad.

- **Param** `hotelId` - ID del hotel.

---

```java
public void setAmenidadId(int amenidadId)
```

Asigna el ID de la amenidad en el catalogo del sistema.

- **Param** `amenidadId` - ID de la amenidad.

---

```java
public void setAmenidadNombre(String amenidadNombre)
```

Asigna el nombre de la amenidad resuelto desde el catalogo.

- **Param** `amenidadNombre` - nombre de la amenidad.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion personalizada de la amenidad para este hotel.

- **Param** `descripcion` - descripcion de la amenidad.

---

```java
public void setImagenesIds(List<Integer> imagenesIds)
```

Asigna los IDs de imagenes asociadas a esta amenidad en el hotel.

- **Param** `imagenesIds` - lista de IDs de imagenes.

---

## HotelResultadoDTO

> DTO con los datos completos de un hotel como resultado de una busqueda. Incluye amenidades, tipos de habitacion disponibles, combinaciones numericas y tipos de habitacion agrupados por capacidad para facilitar la seleccion al usuario.

```java
public int getId()
```

Retorna el identificador unico del hotel.

- **Returns** - ID del hotel.

---

```java
public void setId(int id)
```

Asigna el identificador unico del hotel.

- **Param** `id` - ID del hotel.

---

```java
public String getNombre()
```

Retorna el nombre del hotel.

- **Returns** - nombre del hotel.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre del hotel.

- **Param** `nombre` - nombre del hotel.

---

```java
public String getDireccion()
```

Retorna la direccion fisica del hotel.

- **Returns** - direccion del hotel.

---

```java
public void setDireccion(String direccion)
```

Asigna la direccion fisica del hotel.

- **Param** `direccion` - direccion del hotel.

---

```java
public String getCiudad()
```

Retorna la ciudad donde se ubica el hotel.

- **Returns** - nombre de la ciudad.

---

```java
public void setCiudad(String ciudad)
```

Asigna la ciudad donde se ubica el hotel.

- **Param** `ciudad` - nombre de la ciudad.

---

```java
public String getPais()
```

Retorna el pais donde se ubica el hotel.

- **Returns** - nombre del pais.

---

```java
public void setPais(String pais)
```

Asigna el pais donde se ubica el hotel.

- **Param** `pais` - nombre del pais.

---

```java
public String getDescripcion()
```

Retorna la descripcion general del hotel.

- **Returns** - descripcion del hotel.

---

```java
public void setDescripcion(String descripcion)
```

Asigna la descripcion general del hotel.

- **Param** `descripcion` - descripcion del hotel.

---

```java
public double getRating()
```

Retorna la calificacion promedio del hotel.

- **Returns** - rating del hotel.

---

```java
public void setRating(double rating)
```

Asigna la calificacion promedio del hotel.

- **Param** `rating` - calificacion del hotel.

---

```java
public String getEstado()
```

Retorna el estado actual del hotel.

- **Returns** - nombre del estado.

---

```java
public void setEstado(String estado)
```

Asigna el estado actual del hotel.

- **Param** `estado` - nombre del estado.

---

```java
public List<Integer> getImagenesIds()
```

Retorna los IDs de las imagenes asociadas al hotel.

- **Returns** - lista de IDs de imagenes.

---

```java
public void setImagenesIds(List<Integer> imagenesIds)
```

Asigna los IDs de las imagenes asociadas al hotel.

- **Param** `imagenesIds` - lista de IDs de imagenes.

---

```java
public List<AmenidadHotelDTO> getAmenidades()
```

Retorna las amenidades asignadas al hotel.

- **Returns** - lista de amenidades con sus descripciones e imagenes.

---

```java
public void setAmenidades(List<AmenidadHotelDTO> amenidades)
```

Asigna las amenidades del hotel.

- **Param** `amenidades` - lista de amenidades con sus descripciones e imagenes.

---

```java
public List<TipoHabitacionResultadoDTO> getTiposHabitacion()
```

Retorna los tipos de habitacion disponibles en el hotel para la busqueda.

- **Returns** - lista de tipos de habitacion.

---

```java
public void setTiposHabitacion(List<TipoHabitacionResultadoDTO> tiposHabitacion)
```

Asigna los tipos de habitacion disponibles en el hotel.

- **Param** `tiposHabitacion` - lista de tipos de habitacion.

---

```java
public List<List<Integer>> getCombinacionesNumericas()
```

Retorna las combinaciones numericas de habitaciones que cubren la cantidad de personas solicitada.

- **Returns** - lista de combinaciones, donde cada combinacion es una lista de capacidades.

---

```java
public void setCombinacionesNumericas(List<List<Integer>> combinacionesNumericas)
```

Asigna las combinaciones numericas de habitaciones.

- **Param** `combinacionesNumericas` - lista de combinaciones de capacidades.

---

```java
public Map<Integer, List<TipoHabitacionResultadoDTO>> getTiposHabitacionPorCapacidad()
```

Retorna los tipos de habitacion agrupados por capacidad maxima. Util para mostrar al usuario las opciones segun el numero de personas por habitacion.

- **Returns** - mapa donde la clave es la capacidad y el valor es la lista de tipos de habitacion.

---

```java
public void setTiposHabitacionPorCapacidad(Map<Integer, List<TipoHabitacionResultadoDTO>> tiposHabitacionPorCapacidad)
```

Asigna los tipos de habitacion agrupados por capacidad maxima.

- **Param** `tiposHabitacionPorCapacidad` - mapa de capacidad a lista de tipos de habitacion.

---

## LoginRequestDTO

> DTO con los datos necesarios para autenticar a un usuario en el sistema. El identificador puede ser el username o el correo electronico.

```java
public String getIdentificador()
```

Retorna el identificador del usuario, que puede ser su username o correo.

- **Returns** - username o correo del usuario.

---

```java
public void setIdentificador(String identificador)
```

Asigna el identificador del usuario, que puede ser su username o correo.

- **Param** `identificador` - username o correo del usuario.

---

```java
public String getContrasena()
```

Retorna la contrasena del usuario.

- **Returns** - contrasena del usuario.

---

```java
public void setContrasena(String contrasena)
```

Asigna la contrasena del usuario.

- **Param** `contrasena` - contrasena del usuario.

---

## LoginResponseDTO

> DTO con los datos retornados al cliente tras una autenticacion exitosa. Incluye el mensaje de confirmacion, el username y el rol del usuario autenticado.

```java
public class LoginResponseDTO
```

DTO con los datos retornados al cliente tras una autenticacion exitosa. Incluye el mensaje de confirmacion, el username y el rol del usuario autenticado.

---

```java
public LoginResponseDTO(String mensaje, String username, int rolId)
```

Constructor que inicializa la respuesta de login con todos sus datos.

- **Param** `mensaje` - mensaje de confirmacion de la autenticacion.
- **Param** `username` - nombre de usuario autenticado.
- **Param** `rolId` - ID del rol asignado al usuario.

---

```java
public String getMensaje()
```

Retorna el mensaje de confirmacion de la autenticacion.

- **Returns** - mensaje de confirmacion.

---

```java
public String getUsername()
```

Retorna el nombre de usuario autenticado.

- **Returns** - username del usuario.

---

```java
public int getRolId()
```

Retorna el ID del rol asignado al usuario autenticado.

- **Returns** - ID del rol.

---

## PagoAgenciaRequestDTO

> DTO con los datos de facturacion necesarios para procesar el pago de una agencia.

```java
public String getNit()
```

Retorna el NIT de la agencia para la facturacion.

- **Returns** - NIT de la agencia.

---

```java
public void setNit(String nit)
```

Asigna el NIT de la agencia para la facturacion.

- **Param** `nit` - NIT de la agencia.

---

```java
public String getCodigoPostal()
```

Retorna el codigo postal de la agencia para la facturacion.

- **Returns** - codigo postal de la agencia.

---

```java
public void setCodigoPostal(String codigoPostal)
```

Asigna el codigo postal de la agencia para la facturacion.

- **Param** `codigoPostal` - codigo postal de la agencia.

---

## PagoRequestDTO

> DTO con los datos necesarios para procesar el pago de una reservacion. Incluye datos de facturacion y datos de tarjeta. Los datos de tarjeta solo se validan durante el proceso y nunca se almacenan en el sistema.

```java
public String getNit()
```

Retorna el NIT del cliente para la facturacion.

- **Returns** - NIT del cliente.

---

```java
public void setNit(String nit)
```

Asigna el NIT del cliente para la facturacion.

- **Param** `nit` - NIT del cliente.

---

```java
public String getCodigoPostal()
```

Retorna el codigo postal del cliente para la facturacion.

- **Returns** - codigo postal del cliente.

---

```java
public void setCodigoPostal(String codigoPostal)
```

Asigna el codigo postal del cliente para la facturacion.

- **Param** `codigoPostal` - codigo postal del cliente.

---

```java
public String getNumeroTarjeta()
```

Retorna el numero de tarjeta del cliente.

- **Returns** - numero de tarjeta.

---

```java
public void setNumeroTarjeta(String numeroTarjeta)
```

Asigna el numero de tarjeta del cliente.

- **Param** `numeroTarjeta` - numero de tarjeta.

---

```java
public String getNombreTitular()
```

Retorna el nombre del titular de la tarjeta.

- **Returns** - nombre del titular.

---

```java
public void setNombreTitular(String nombreTitular)
```

Asigna el nombre del titular de la tarjeta.

- **Param** `nombreTitular` - nombre del titular.

---

```java
public String getFechaVencimiento()
```

Retorna la fecha de vencimiento de la tarjeta en formato MM/YY.

- **Returns** - fecha de vencimiento.

---

```java
public void setFechaVencimiento(String fechaVencimiento)
```

Asigna la fecha de vencimiento de la tarjeta en formato MM/YY.

- **Param** `fechaVencimiento` - fecha de vencimiento.

---

```java
public String getCvv()
```

Retorna el CVV de la tarjeta del cliente.

- **Returns** - CVV de la tarjeta.

---

```java
public void setCvv(String cvv)
```

Asigna el CVV de la tarjeta del cliente.

- **Param** `cvv` - CVV de la tarjeta.

---

## PagoResponseDTO

> DTO con los datos de confirmacion retornados al cliente tras procesar un pago. Incluye los datos de la factura generada y el total cobrado.

```java
public class PagoResponseDTO
```

DTO con los datos de confirmacion retornados al cliente tras procesar un pago. Incluye los datos de la factura generada y el total cobrado.

---

```java
public int getFacturaId()
```

Retorna el identificador unico de la factura generada.

- **Returns** - ID de la factura.

---

```java
public void setFacturaId(int facturaId)
```

Asigna el identificador unico de la factura generada.

- **Param** `facturaId` - ID de la factura.

---

```java
public String getNoReservacion()
```

Retorna el numero de reservacion asociado al pago.

- **Returns** - numero de reservacion.

---

```java
public void setNoReservacion(String noReservacion)
```

Asigna el numero de reservacion asociado al pago.

- **Param** `noReservacion` - numero de reservacion.

---

```java
public String getEstado()
```

Retorna el estado actual de la reservacion tras el pago.

- **Returns** - nombre del estado.

---

```java
public void setEstado(String estado)
```

Asigna el estado actual de la reservacion tras el pago.

- **Param** `estado` - nombre del estado.

---

```java
public String getFecha()
```

Retorna la fecha en que se registro el pago.

- **Returns** - fecha del pago.

---

```java
public void setFecha(String fecha)
```

Asigna la fecha en que se registro el pago.

- **Param** `fecha` - fecha del pago.

---

```java
public String getNit()
```

Retorna el NIT del cliente usado en la facturacion.

- **Returns** - NIT del cliente.

---

```java
public void setNit(String nit)
```

Asigna el NIT del cliente usado en la facturacion.

- **Param** `nit` - NIT del cliente.

---

```java
public String getCodigoPostal()
```

Retorna el codigo postal del cliente usado en la facturacion.

- **Returns** - codigo postal del cliente.

---

```java
public void setCodigoPostal(String codigoPostal)
```

Asigna el codigo postal del cliente usado en la facturacion.

- **Param** `codigoPostal` - codigo postal del cliente.

---

```java
public double getTotal()
```

Retorna el monto total cobrado en el pago.

- **Returns** - total del pago.

---

```java
public void setTotal(double total)
```

Asigna el monto total cobrado en el pago.

- **Param** `total` - total del pago.

---

## PaisDTO

> DTO que representa un pais del catalogo del sistema.

```java
public PaisDTO(int id, String nombre)
```

Constructor para crear un pais con todos sus datos.

- **Param** `id` - identificador unico del pais.
- **Param** `nombre` - nombre del pais.

---

```java
public int getId()
```

Retorna el identificador unico del pais.

- **Returns** - ID del pais.

---

```java
public String getNombre()
```

Retorna el nombre del pais.

- **Returns** - nombre del pais.

---

```java
public void setId(int id)
```

Asigna el identificador unico del pais.

- **Param** `id` - ID del pais.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre del pais.

- **Param** `nombre` - nombre del pais.

---

## PuedeCancelarDTO

> DTO con el resultado de verificar si una reservacion puede ser cancelada. Incluye un indicador booleano y la razon en caso de que no sea posible cancelar.

```java
public PuedeCancelarDTO(boolean puedeCancelar, String razon)
```

Constructor que inicializa el resultado de la verificacion de cancelacion.

- **Param** `puedeCancelar` - indica si la reservacion puede cancelarse.
- **Param** `razon` - razon por la que no puede cancelarse, null si si puede.

---

```java
public boolean isPuedeCancelar()
```

Retorna si la reservacion puede ser cancelada.

- **Returns** - true si puede cancelarse, false en caso contrario.

---

```java
public String getRazon()
```

Retorna la razon por la que la reservacion no puede cancelarse.

- **Returns** - razon de la restriccion, o null si la cancelacion es posible.

---

## ReservacionAgenciaResponseDTO

> DTO con los datos de una reservacion realizada por una agencia externa. Incluye el desglose de habitaciones con sus precios calculados.

```java
public class ReservacionAgenciaResponseDTO
```

DTO con los datos de una reservacion realizada por una agencia externa. Incluye el desglose de habitaciones con sus precios calculados.

---

```java
public int getId()
```

Retorna el identificador unico de la reservacion.

- **Returns** - ID de la reservacion.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la reservacion.

- **Param** `id` - ID de la reservacion.

---

```java
public String getNoReservacion()
```

Retorna el numero de reservacion generado por el sistema.

- **Returns** - numero de reservacion.

---

```java
public void setNoReservacion(String noReservacion)
```

Asigna el numero de reservacion generado por el sistema.

- **Param** `noReservacion` - numero de reservacion.

---

```java
public double getTotal()
```

Retorna el monto total de la reservacion.

- **Returns** - total de la reservacion.

---

```java
public void setTotal(double total)
```

Asigna el monto total de la reservacion.

- **Param** `total` - total de la reservacion.

---

```java
public String getFechaCreacion()
```

Retorna la fecha en que se creo la reservacion.

- **Returns** - fecha de creacion.

---

```java
public void setFechaCreacion(String fechaCreacion)
```

Asigna la fecha en que se creo la reservacion.

- **Param** `fechaCreacion` - fecha de creacion.

---

```java
public String getFechaExpiracion()
```

Retorna la fecha en que expira la reservacion si no es pagada.

- **Returns** - fecha de expiracion.

---

```java
public void setFechaExpiracion(String fechaExpiracion)
```

Asigna la fecha en que expira la reservacion si no es pagada.

- **Param** `fechaExpiracion` - fecha de expiracion.

---

```java
public String getEstado()
```

Retorna el nombre del estado actual de la reservacion.

- **Returns** - nombre del estado.

---

```java
public void setEstado(String estado)
```

Asigna el nombre del estado actual de la reservacion.

- **Param** `estado` - nombre del estado.

---

```java
public List<HabitacionAgenciaResponseDTO> getHabitaciones()
```

Retorna el desglose de habitaciones incluidas en la reservacion.

- **Returns** - lista de habitaciones con sus precios calculados.

---

```java
public void setHabitaciones(List<HabitacionAgenciaResponseDTO> habitaciones)
```

Asigna el desglose de habitaciones incluidas en la reservacion.

- **Param** `habitaciones` - lista de habitaciones con sus precios calculados.

---

## ReservacionDetalleDTO

> DTO con los datos completos de una reservacion y su detalle de habitacion. Incluye informacion del hotel, fechas, estado y IDs de imagenes asociadas.

```java
public int getId()
```

Retorna el identificador unico de la reservacion.

- **Returns** - ID de la reservacion.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la reservacion.

- **Param** `id` - ID de la reservacion.

---

```java
public String getNoReservacion()
```

Retorna el numero de reservacion generado por el sistema.

- **Returns** - numero de reservacion.

---

```java
public void setNoReservacion(String noReservacion)
```

Asigna el numero de reservacion generado por el sistema.

- **Param** `noReservacion` - numero de reservacion.

---

```java
public double getTotal()
```

Retorna el monto total de la reservacion.

- **Returns** - total de la reservacion.

---

```java
public void setTotal(double total)
```

Asigna el monto total de la reservacion.

- **Param** `total` - total de la reservacion.

---

```java
public String getEstado()
```

Retorna el nombre del estado actual de la reservacion.

- **Returns** - nombre del estado.

---

```java
public void setEstado(String estado)
```

Asigna el nombre del estado actual de la reservacion.

- **Param** `estado` - nombre del estado.

---

```java
public String getFechaCreacion()
```

Retorna la fecha en que se creo la reservacion.

- **Returns** - fecha de creacion.

---

```java
public void setFechaCreacion(String fechaCreacion)
```

Asigna la fecha en que se creo la reservacion.

- **Param** `fechaCreacion` - fecha de creacion.

---

```java
public String getFechaExpiracion()
```

Retorna la fecha en que expira la reservacion si no es pagada.

- **Returns** - fecha de expiracion.

---

```java
public void setFechaExpiracion(String fechaExpiracion)
```

Asigna la fecha en que expira la reservacion si no es pagada.

- **Param** `fechaExpiracion` - fecha de expiracion.

---

```java
public String getFechaCancelacion()
```

Retorna la fecha en que se cancelo la reservacion.

- **Returns** - fecha de cancelacion, o null si no fue cancelada.

---

```java
public void setFechaCancelacion(String fechaCancelacion)
```

Asigna la fecha en que se cancelo la reservacion.

- **Param** `fechaCancelacion` - fecha de cancelacion.

---

```java
public String getMotivoCancelacion()
```

Retorna el motivo por el que se cancelo la reservacion.

- **Returns** - motivo de cancelacion, o null si no fue cancelada.

---

```java
public void setMotivoCancelacion(String motivoCancelacion)
```

Asigna el motivo por el que se cancelo la reservacion.

- **Param** `motivoCancelacion` - motivo de cancelacion.

---

```java
public int getDetalleId()
```

Retorna el identificador unico del detalle de la reservacion.

- **Returns** - ID del detalle.

---

```java
public void setDetalleId(int detalleId)
```

Asigna el identificador unico del detalle de la reservacion.

- **Param** `detalleId` - ID del detalle.

---

```java
public int getHabitacionId()
```

Retorna el ID de la habitacion reservada.

- **Returns** - ID de la habitacion.

---

```java
public void setHabitacionId(int habitacionId)
```

Asigna el ID de la habitacion reservada.

- **Param** `habitacionId` - ID de la habitacion.

---

```java
public String getNumeroHabitacion()
```

Retorna el numero o identificador de la habitacion dentro del hotel.

- **Returns** - numero de la habitacion.

---

```java
public void setNumeroHabitacion(String numeroHabitacion)
```

Asigna el numero o identificador de la habitacion dentro del hotel.

- **Param** `numeroHabitacion` - numero de la habitacion.

---

```java
public String getFechaCheckIn()
```

Retorna la fecha de entrada en la habitacion.

- **Returns** - fecha de check-in.

---

```java
public void setFechaCheckIn(String fechaCheckIn)
```

Asigna la fecha de entrada en la habitacion.

- **Param** `fechaCheckIn` - fecha de check-in.

---

```java
public String getFechaCheckOut()
```

Retorna la fecha de salida de la habitacion.

- **Returns** - fecha de check-out.

---

```java
public void setFechaCheckOut(String fechaCheckOut)
```

Asigna la fecha de salida de la habitacion.

- **Param** `fechaCheckOut` - fecha de check-out.

---

```java
public int getCantidadPersonas()
```

Retorna la cantidad de personas que ocupan la habitacion.

- **Returns** - cantidad de personas.

---

```java
public void setCantidadPersonas(int cantidadPersonas)
```

Asigna la cantidad de personas que ocupan la habitacion.

- **Param** `cantidadPersonas` - cantidad de personas.

---

```java
public double getTotalDetalle()
```

Retorna el subtotal correspondiente a este detalle de habitacion.

- **Returns** - total del detalle.

---

```java
public void setTotalDetalle(double totalDetalle)
```

Asigna el subtotal correspondiente a este detalle de habitacion.

- **Param** `totalDetalle` - total del detalle.

---

```java
public String getDescripcionHabitacion()
```

Retorna la descripcion de la habitacion reservada.

- **Returns** - descripcion de la habitacion.

---

```java
public void setDescripcionHabitacion(String descripcionHabitacion)
```

Asigna la descripcion de la habitacion reservada.

- **Param** `descripcionHabitacion` - descripcion de la habitacion.

---

```java
public String getTipoHabitacion()
```

Retorna el nombre del tipo de habitacion reservada.

- **Returns** - tipo de habitacion.

---

```java
public void setTipoHabitacion(String tipoHabitacion)
```

Asigna el nombre del tipo de habitacion reservada.

- **Param** `tipoHabitacion` - tipo de habitacion.

---

```java
public String getTipoCama()
```

Retorna el tipo de cama de la habitacion reservada.

- **Returns** - tipo de cama.

---

```java
public void setTipoCama(String tipoCama)
```

Asigna el tipo de cama de la habitacion reservada.

- **Param** `tipoCama` - tipo de cama.

---

```java
public int getHotelId()
```

Retorna el ID del hotel al que pertenece la habitacion reservada.

- **Returns** - ID del hotel.

---

```java
public void setHotelId(int hotelId)
```

Asigna el ID del hotel al que pertenece la habitacion reservada.

- **Param** `hotelId` - ID del hotel.

---

```java
public String getNombreHotel()
```

Retorna el nombre del hotel al que pertenece la habitacion reservada.

- **Returns** - nombre del hotel.

---

```java
public void setNombreHotel(String nombreHotel)
```

Asigna el nombre del hotel al que pertenece la habitacion reservada.

- **Param** `nombreHotel` - nombre del hotel.

---

```java
public List<Integer> getImagenesHotelIds()
```

Retorna los IDs de imagenes asociadas al hotel.

- **Returns** - lista de IDs de imagenes del hotel.

---

```java
public void setImagenesHotelIds(List<Integer> imagenesHotelIds)
```

Asigna los IDs de imagenes asociadas al hotel.

- **Param** `imagenesHotelIds` - lista de IDs de imagenes del hotel.

---

```java
public List<Integer> getImagenesHabitacionIds()
```

Retorna los IDs de imagenes asociadas a la habitacion.

- **Returns** - lista de IDs de imagenes de la habitacion.

---

```java
public void setImagenesHabitacionIds(List<Integer> imagenesHabitacionIds)
```

Asigna los IDs de imagenes asociadas a la habitacion.

- **Param** `imagenesHabitacionIds` - lista de IDs de imagenes de la habitacion.

---

## ReservacionRequestDTO

> DTO con los datos necesarios para crear una nueva reservacion. Contiene la lista de habitaciones con sus fechas y cantidad de personas.

```java
public List<HabitacionReservaRequestDTO> getHabitaciones()
```

Retorna la lista de habitaciones incluidas en la solicitud de reservacion.

- **Returns** - lista de habitaciones a reservar.

---

```java
public void setHabitaciones(List<HabitacionReservaRequestDTO> habitaciones)
```

Asigna la lista de habitaciones incluidas en la solicitud de reservacion.

- **Param** `habitaciones` - lista de habitaciones a reservar.

---

## ReservacionResponseDTO

> DTO con los datos basicos de una reservacion retornados al cliente tras su creacion. Incluye el numero de reservacion, total, estado y fechas relevantes.

```java
public class ReservacionResponseDTO
```

DTO con los datos basicos de una reservacion retornados al cliente tras su creacion. Incluye el numero de reservacion, total, estado y fechas relevantes.

---

```java
public int getId()
```

Retorna el identificador unico de la reservacion.

- **Returns** - ID de la reservacion.

---

```java
public void setId(int id)
```

Asigna el identificador unico de la reservacion.

- **Param** `id` - ID de la reservacion.

---

```java
public String getNoReservacion()
```

Retorna el numero de reservacion generado por el sistema.

- **Returns** - numero de reservacion.

---

```java
public void setNoReservacion(String noReservacion)
```

Asigna el numero de reservacion generado por el sistema.

- **Param** `noReservacion` - numero de reservacion.

---

```java
public double getTotal()
```

Retorna el monto total de la reservacion.

- **Returns** - total de la reservacion.

---

```java
public void setTotal(double total)
```

Asigna el monto total de la reservacion.

- **Param** `total` - total de la reservacion.

---

```java
public String getEstado()
```

Retorna el nombre del estado actual de la reservacion.

- **Returns** - nombre del estado.

---

```java
public void setEstado(String estado)
```

Asigna el nombre del estado actual de la reservacion.

- **Param** `estado` - nombre del estado.

---

```java
public String getFechaCreacion()
```

Retorna la fecha en que se creo la reservacion.

- **Returns** - fecha de creacion.

---

```java
public void setFechaCreacion(String fechaCreacion)
```

Asigna la fecha en que se creo la reservacion.

- **Param** `fechaCreacion` - fecha de creacion.

---

```java
public String getFechaExpiracion()
```

Retorna la fecha en que expira la reservacion si no es pagada.

- **Returns** - fecha de expiracion.

---

```java
public void setFechaExpiracion(String fechaExpiracion)
```

Asigna la fecha en que expira la reservacion si no es pagada.

- **Param** `fechaExpiracion` - fecha de expiracion.

---

## SesionDTO

> DTO con los datos de la sesion activa de un usuario autenticado. Se usa para verificar el estado de autenticacion y el rol del usuario.

```java
public int getUsuarioId()
```

Retorna el identificador unico del usuario autenticado.

- **Returns** - ID del usuario.

---

```java
public void setUsuarioId(int usuarioId)
```

Asigna el identificador unico del usuario autenticado.

- **Param** `usuarioId` - ID del usuario.

---

```java
public String getUsername()
```

Retorna el nombre de usuario de la sesion activa.

- **Returns** - username del usuario.

---

```java
public void setUsername(String username)
```

Asigna el nombre de usuario de la sesion activa.

- **Param** `username` - username del usuario.

---

```java
public int getRolId()
```

Retorna el ID del rol asignado al usuario autenticado.

- **Returns** - ID del rol.

---

```java
public void setRolId(int rolId)
```

Asigna el ID del rol asignado al usuario autenticado.

- **Param** `rolId` - ID del rol.

---

```java
public String getRol()
```

Retorna el nombre del rol asignado al usuario autenticado.

- **Returns** - nombre del rol.

---

```java
public void setRol(String rol)
```

Asigna el nombre del rol asignado al usuario autenticado.

- **Param** `rol` - nombre del rol.

---

```java
public boolean isAutenticado()
```

Retorna si el usuario tiene una sesion activa autenticada.

- **Returns** - true si esta autenticado, false en caso contrario.

---

```java
public void setAutenticado(boolean autenticado)
```

Asigna el estado de autenticacion del usuario.

- **Param** `autenticado` - true si esta autenticado, false en caso contrario.

---

## SubirImagenRequestDTO

> DTO con los datos necesarios para subir una imagen al sistema. La imagen debe enviarse codificada en Base64.

```java
public String getBase64()
```

Retorna la imagen codificada en Base64.

- **Returns** - imagen en formato Base64.

---

```java
public void setBase64(String base64)
```

Asigna la imagen codificada en Base64.

- **Param** `base64` - imagen en formato Base64.

---

## TipoHabitacionResultadoDTO

> DTO con los datos de un tipo de habitacion como resultado de una busqueda. Incluye las habitaciones fisicas disponibles de ese tipo para el rango de fechas solicitado.

```java
public int getTipoHabitacionId()
```

Retorna el ID del tipo de habitacion en el catalogo.

- **Returns** - ID del tipo de habitacion.

---

```java
public void setTipoHabitacionId(int tipoHabitacionId)
```

Asigna el ID del tipo de habitacion en el catalogo.

- **Param** `tipoHabitacionId` - ID del tipo de habitacion.

---

```java
public String getTipoHabitacion()
```

Retorna el nombre del tipo de habitacion.

- **Returns** - nombre del tipo de habitacion.

---

```java
public void setTipoHabitacion(String tipoHabitacion)
```

Asigna el nombre del tipo de habitacion.

- **Param** `tipoHabitacion` - nombre del tipo de habitacion.

---

```java
public double getPrecioPorPersona()
```

Retorna el precio adicional por persona extra sobre la capacidad base.

- **Returns** - precio por persona adicional.

---

```java
public void setPrecioPorPersona(double precioPorPersona)
```

Asigna el precio adicional por persona extra sobre la capacidad base.

- **Param** `precioPorPersona` - precio por persona adicional.

---

```java
public double getPrecioPorNoche()
```

Retorna el precio base por noche del tipo de habitacion.

- **Returns** - precio por noche.

---

```java
public void setPrecioPorNoche(double precioPorNoche)
```

Asigna el precio base por noche del tipo de habitacion.

- **Param** `precioPorNoche` - precio por noche.

---

```java
public int getCapacidadMaxima()
```

Retorna la capacidad maxima de personas admitidas en este tipo de habitacion.

- **Returns** - capacidad maxima.

---

```java
public void setCapacidadMaxima(int capacidadMaxima)
```

Asigna la capacidad maxima de personas admitidas en este tipo de habitacion.

- **Param** `capacidadMaxima` - capacidad maxima.

---

```java
public String getTipoCama()
```

Retorna el tipo de cama disponible en este tipo de habitacion.

- **Returns** - tipo de cama.

---

```java
public void setTipoCama(String tipoCama)
```

Asigna el tipo de cama disponible en este tipo de habitacion.

- **Param** `tipoCama` - tipo de cama.

---

```java
public double getMetrosCuadrados()
```

Retorna la superficie en metros cuadrados de este tipo de habitacion.

- **Returns** - metros cuadrados.

---

```java
public void setMetrosCuadrados(double metrosCuadrados)
```

Asigna la superficie en metros cuadrados de este tipo de habitacion.

- **Param** `metrosCuadrados` - metros cuadrados.

---

```java
public List<Integer> getImagenesIds()
```

Retorna los IDs de imagenes asociadas a este tipo de habitacion.

- **Returns** - lista de IDs de imagenes.

---

```java
public void setImagenesIds(List<Integer> imagenesIds)
```

Asigna los IDs de imagenes asociadas a este tipo de habitacion.

- **Param** `imagenesIds` - lista de IDs de imagenes.

---

```java
public List<HabitacionResumenDTO> getHabitacionesDisponibles()
```

Retorna las habitaciones fisicas disponibles de este tipo para el rango de fechas solicitado.

- **Returns** - lista de habitaciones disponibles.

---

```java
public void setHabitacionesDisponibles(List<HabitacionResumenDTO> habitacionesDisponibles)
```

Asigna las habitaciones fisicas disponibles de este tipo para el rango de fechas solicitado.

- **Param** `habitacionesDisponibles` - lista de habitaciones disponibles.

---

## UsuarioAdminDTO

> DTO con los datos completos de un usuario para el panel de administracion. Incluye informacion personal, rol asignado y ubicacion geografica.

```java
public int getId()
```

Retorna el identificador unico del usuario.

- **Returns** - ID del usuario.

---

```java
public String getUsername()
```

Retorna el nombre de usuario.

- **Returns** - username del usuario.

---

```java
public String getNombre()
```

Retorna el nombre del usuario.

- **Returns** - nombre del usuario.

---

```java
public String getApellido()
```

Retorna el apellido del usuario.

- **Returns** - apellido del usuario.

---

```java
public String getCorreo()
```

Retorna el correo electronico del usuario.

- **Returns** - correo del usuario.

---

```java
public String getTelefono()
```

Retorna el numero de telefono del usuario.

- **Returns** - telefono del usuario.

---

```java
public String getFechaNacimiento()
```

Retorna la fecha de nacimiento del usuario.

- **Returns** - fecha de nacimiento del usuario.

---

```java
public int getRolId()
```

Retorna el ID del rol asignado al usuario.

- **Returns** - ID del rol.

---

```java
public String getRolNombre()
```

Retorna el nombre del rol asignado al usuario.

- **Returns** - nombre del rol.

---

```java
public String getCiudad()
```

Retorna la ciudad de residencia del usuario.

- **Returns** - nombre de la ciudad.

---

```java
public String getPais()
```

Retorna el pais de residencia del usuario.

- **Returns** - nombre del pais.

---

```java
public void setId(int id)
```

Asigna el identificador unico del usuario.

- **Param** `id` - ID del usuario.

---

```java
public void setUsername(String username)
```

Asigna el nombre de usuario.

- **Param** `username` - username del usuario.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre del usuario.

- **Param** `nombre` - nombre del usuario.

---

```java
public void setApellido(String apellido)
```

Asigna el apellido del usuario.

- **Param** `apellido` - apellido del usuario.

---

```java
public void setCorreo(String correo)
```

Asigna el correo electronico del usuario.

- **Param** `correo` - correo del usuario.

---

```java
public void setTelefono(String telefono)
```

Asigna el numero de telefono del usuario.

- **Param** `telefono` - telefono del usuario.

---

```java
public void setFechaNacimiento(String fechaNacimiento)
```

Asigna la fecha de nacimiento del usuario.

- **Param** `fechaNacimiento` - fecha de nacimiento del usuario.

---

```java
public void setRolId(int rolId)
```

Asigna el ID del rol asignado al usuario.

- **Param** `rolId` - ID del rol.

---

```java
public void setRolNombre(String rolNombre)
```

Asigna el nombre del rol asignado al usuario.

- **Param** `rolNombre` - nombre del rol.

---

```java
public void setCiudad(String ciudad)
```

Asigna la ciudad de residencia del usuario.

- **Param** `ciudad` - nombre de la ciudad.

---

```java
public void setPais(String pais)
```

Asigna el pais de residencia del usuario.

- **Param** `pais` - nombre del pais.

---

## UsuarioPerfilResponseDTO

> DTO con los datos completos del perfil de un usuario autenticado. Incluye informacion personal, ubicacion geografica y nacionalidades registradas.

```java
public class UsuarioPerfilResponseDTO
```

DTO con los datos completos del perfil de un usuario autenticado. Incluye informacion personal, ubicacion geografica y nacionalidades registradas.

---

```java
public int getId()
```

Retorna el identificador unico del usuario.

- **Returns** - ID del usuario.

---

```java
public void setId(int id)
```

Asigna el identificador unico del usuario.

- **Param** `id` - ID del usuario.

---

```java
public String getUsername()
```

Retorna el nombre de usuario.

- **Returns** - username del usuario.

---

```java
public void setUsername(String username)
```

Asigna el nombre de usuario.

- **Param** `username` - username del usuario.

---

```java
public String getCorreo()
```

Retorna el correo electronico del usuario.

- **Returns** - correo del usuario.

---

```java
public void setCorreo(String correo)
```

Asigna el correo electronico del usuario.

- **Param** `correo` - correo del usuario.

---

```java
public String getPasaporte()
```

Retorna el numero de pasaporte del usuario.

- **Returns** - numero de pasaporte.

---

```java
public void setPasaporte(String pasaporte)
```

Asigna el numero de pasaporte del usuario.

- **Param** `pasaporte` - numero de pasaporte.

---

```java
public String getNombre()
```

Retorna el nombre del usuario.

- **Returns** - nombre del usuario.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre del usuario.

- **Param** `nombre` - nombre del usuario.

---

```java
public String getApellido()
```

Retorna el apellido del usuario.

- **Returns** - apellido del usuario.

---

```java
public void setApellido(String apellido)
```

Asigna el apellido del usuario.

- **Param** `apellido` - apellido del usuario.

---

```java
public String getTelefono()
```

Retorna el numero de telefono del usuario.

- **Returns** - telefono del usuario.

---

```java
public void setTelefono(String telefono)
```

Asigna el numero de telefono del usuario.

- **Param** `telefono` - telefono del usuario.

---

```java
public String getFechaNacimiento()
```

Retorna la fecha de nacimiento del usuario.

- **Returns** - fecha de nacimiento.

---

```java
public void setFechaNacimiento(String fechaNacimiento)
```

Asigna la fecha de nacimiento del usuario.

- **Param** `fechaNacimiento` - fecha de nacimiento.

---

```java
public int getRolId()
```

Retorna el ID del rol asignado al usuario.

- **Returns** - ID del rol.

---

```java
public void setRolId(int rolId)
```

Asigna el ID del rol asignado al usuario.

- **Param** `rolId` - ID del rol.

---

```java
public String getPais()
```

Retorna el pais de residencia del usuario.

- **Returns** - nombre del pais.

---

```java
public void setPais(String pais)
```

Asigna el pais de residencia del usuario.

- **Param** `pais` - nombre del pais.

---

```java
public String getCiudad()
```

Retorna la ciudad de residencia del usuario.

- **Returns** - nombre de la ciudad.

---

```java
public void setCiudad(String ciudad)
```

Asigna la ciudad de residencia del usuario.

- **Param** `ciudad` - nombre de la ciudad.

---

```java
public List<String> getNacionalidades()
```

Retorna la lista de nacionalidades registradas del usuario.

- **Returns** - lista de nacionalidades.

---

```java
public void setNacionalidades(List<String> nacionalidades)
```

Asigna la lista de nacionalidades registradas del usuario.

- **Param** `nacionalidades` - lista de nacionalidades.

---

## UsuarioValidacionRequestDTO

> DTO con los datos necesarios para registrar un nuevo usuario en el sistema. El pais y la ciudad se envian como nombres en texto. Si no existen, el servicio los crea. La fecha de nacimiento debe enviarse en formato YYYY-MM-DD.

```java
public String getUsername()
```

Retorna el nombre de usuario elegido para el registro.

- **Returns** - username del usuario.

---

```java
public void setUsername(String username)
```

Asigna el nombre de usuario elegido para el registro.

- **Param** `username` - username del usuario.

---

```java
public String getCorreo()
```

Retorna el correo electronico del usuario.

- **Returns** - correo del usuario.

---

```java
public void setCorreo(String correo)
```

Asigna el correo electronico del usuario.

- **Param** `correo` - correo del usuario.

---

```java
public String getContrasena()
```

Retorna la contrasena del usuario.

- **Returns** - contrasena del usuario.

---

```java
public void setContrasena(String contrasena)
```

Asigna la contrasena del usuario.

- **Param** `contrasena` - contrasena del usuario.

---

```java
public String getPasaporte()
```

Retorna el numero de pasaporte del usuario.

- **Returns** - numero de pasaporte.

---

```java
public void setPasaporte(String pasaporte)
```

Asigna el numero de pasaporte del usuario.

- **Param** `pasaporte` - numero de pasaporte.

---

```java
public String getNombre()
```

Retorna el nombre del usuario.

- **Returns** - nombre del usuario.

---

```java
public void setNombre(String nombre)
```

Asigna el nombre del usuario.

- **Param** `nombre` - nombre del usuario.

---

```java
public String getApellido()
```

Retorna el apellido del usuario.

- **Returns** - apellido del usuario.

---

```java
public void setApellido(String apellido)
```

Asigna el apellido del usuario.

- **Param** `apellido` - apellido del usuario.

---

```java
public String getTelefono()
```

Retorna el numero de telefono del usuario.

- **Returns** - telefono del usuario.

---

```java
public void setTelefono(String telefono)
```

Asigna el numero de telefono del usuario.

- **Param** `telefono` - telefono del usuario.

---

```java
public String getFechaNacimiento()
```

Retorna la fecha de nacimiento del usuario en formato YYYY-MM-DD.

- **Returns** - fecha de nacimiento.

---

```java
public void setFechaNacimiento(String fechaNacimiento)
```

Asigna la fecha de nacimiento del usuario en formato YYYY-MM-DD.

- **Param** `fechaNacimiento` - fecha de nacimiento.

---

```java
public String getPais()
```

Retorna el nombre del pais de residencia del usuario.

- **Returns** - nombre del pais.

---

```java
public void setPais(String pais)
```

Asigna el nombre del pais de residencia del usuario.

- **Param** `pais` - nombre del pais.

---

```java
public String getCiudad()
```

Retorna el nombre de la ciudad de residencia del usuario.

- **Returns** - nombre de la ciudad.

---

```java
public void setCiudad(String ciudad)
```

Asigna el nombre de la ciudad de residencia del usuario.

- **Param** `ciudad` - nombre de la ciudad.

---

```java
public List<String> getNacionalidades()
```

Retorna la lista de nacionalidades del usuario como nombres en texto.

- **Returns** - lista de nacionalidades.

---

```java
public void setNacionalidades(List<String> nacionalidades)
```

Asigna la lista de nacionalidades del usuario como nombres en texto.

- **Param** `nacionalidades` - lista de nacionalidades.

---

## UsuarioValidacionResponseDTO

> DTO con el resultado de validar si un username, correo o pasaporte ya existen en el sistema. Se usa antes del registro para informar al cliente de duplicados.

```java
public class UsuarioValidacionResponseDTO
```

DTO con el resultado de validar si un username, correo o pasaporte ya existen en el sistema. Se usa antes del registro para informar al cliente de duplicados.

---

```java
public UsuarioValidacionResponseDTO(boolean usernameExiste, boolean correoExiste, boolean pasaporteExiste)
```

Constructor que inicializa el resultado de la validacion de duplicados.

- **Param** `usernameExiste` - true si el username ya esta registrado en el sistema.
- **Param** `correoExiste` - true si el correo ya esta registrado en el sistema.
- **Param** `pasaporteExiste` - true si el pasaporte ya esta registrado en el sistema.

---

```java
public boolean isUsernameExiste()
```

Retorna si el username ya existe en el sistema.

- **Returns** - true si el username esta registrado, false en caso contrario.

---

```java
public boolean isCorreoExiste()
```

Retorna si el correo ya existe en el sistema.

- **Returns** - true si el correo esta registrado, false en caso contrario.

---

```java
public boolean isPasaporteExiste()
```

Retorna si el pasaporte ya existe en el sistema.

- **Returns** - true si el pasaporte esta registrado, false en caso contrario.

---
