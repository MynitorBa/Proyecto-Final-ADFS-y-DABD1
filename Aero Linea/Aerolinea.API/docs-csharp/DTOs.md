# DTOs

## AeropuertoDTO

> DTO de lectura que expone la informacion publica de un aeropuerto. Incluye codigo IATA, ciudad, pais, imagen opcional y zona horaria configurada.

## AgregarPasajerosDTO

> DTO para asignar los datos de los pasajeros a los boletos de una reservacion existente. Contiene el identificador de la reservacion y la lista de pasajeros con sus datos personales.

## AsientosVueloAgenciaDTO

> DTO de respuesta que muestra el mapa de asientos de un vuelo para una agencia. Incluye configuracion del avion, asientos ocupados y boletos reservados por la agencia.

## AvionDTO

> DTO de lectura que expone la informacion de una aeronave registrada en la flota. Incluye marca, modelo, capacidad, nombre completo e imagen opcional en Base64.

## BuscarVueloAgenciaDTO

> DTO de peticion para que una agencia busque vuelos disponibles. Permite filtrar por origen, destino, fecha, cantidad de pasajeros, clase y rango de precios.

## BuscarVueloDTO

> DTO de peticion para buscar vuelos disponibles desde el portal web. Permite filtrar por aeropuerto de origen y destino, fecha, cantidad de pasajeros, clase y rango de precios.

## CambiarAsientoRequestDTO

> DTO de peticion para cambiar el asiento asignado a un boleto de la reservacion activa. Contiene unicamente el nuevo asiento solicitado por el usuario.

## CambiarRolDTO

> DTO de peticion para cambiar el rol de un usuario desde el panel de administracion. Contiene el identificador del usuario y el identificador del nuevo rol a asignar.

## CancelarReservacionDTO

> DTO de peticion para cancelar una reservacion activa. El campo Motivo es opcional y puede enviarse como null.

## CiudadDTO

> DTO de lectura que expone la informacion de una ciudad junto con su pais de pertenencia. Incluye nombre completo con formato "Ciudad, Pais" para uso en listas desplegables.

## ComprarReservacionDTO

> DTO de peticion para confirmar la compra de una reservacion pendiente. Contiene datos de facturacion y datos de tarjeta para validacion de formato. Los datos de la tarjeta nunca se persisten en el sistema.

## ConfirmacionAgenciaDTO

> DTO de peticion para que una agencia confirme y pague una reservacion pendiente. Contiene los datos de facturacion requeridos para generar la factura.

## CrearAeropuerto

> DTO para registrar o actualizar un aeropuerto desde el panel de administracion. Requiere nombre, codigo IATA, ciudad y pais. Acepta imagen en Base64 y zona horaria IANA de forma opcional.

## CrearAgenciaDTO

> DTO para que un administrador cree una nueva agencia y la asigne a un usuario Webservice. Incluye nombre, correo, ID del usuario web, porcentaje de descuento y URL publica.

## CrearAvionDTO

> DTO para registrar o actualizar una aeronave en el sistema. Requiere marca, modelo y capacidad de pasajeros. Acepta imagen en Base64 de forma opcional.

## CrearComentarioDTO

> DTO de peticion para publicar un nuevo comentario o resena sobre una ruta especifica. Contiene identificador de la ruta, calificacion por estrellas y texto del comentario.

## CrearReservacionDTO

> DTO de peticion para crear una nueva reservacion de vuelo. El UsuarioId se obtiene de la cookie de sesion en el controller. Contiene la lista de vuelos seleccionados con clase y cantidad de pasajeros.

## CrearTripulanteDTO

> DTO para registrar o actualizar un miembro de la tripulacion en el sistema. Requiere nombre, apellido y rol asignado. Acepta imagen en Base64 de forma opcional.

## CrearUsuarioDTO

> DTO de peticion para registrar un nuevo usuario en el sistema. Contiene credenciales de acceso, datos personales, ubicacion geografica y lista de nacionalidades. El rol por defecto es usuario regular (RolID = 2).

## CrearVueloAdminDTO

> DTO para la creacion de un nuevo vuelo desde el panel de administracion. Contiene numero de vuelo, aeropuertos, avion, fecha, hora de salida local, disponibilidad y precios por clase, y los tripulantes asignados.

## FacturaDTO

> DTO de lectura que expone los datos de una factura generada al confirmar una reservacion. Incluye identificador, fecha, datos fiscales y monto total de la transaccion.

## HandshakeRequestDTO

> DTO de peticion para iniciar el proceso de autenticacion por handshake con una agencia externa. Contiene el token de entrada proporcionado por la agencia y su URL de origen.

## HotelesAliados

> DTO con la informacion completa de un hotel disponible retornado al frontend. Incluye los datos del hotel de la API externa y la referencia al aliado registrado en la base de datos de aerolineas.

## LoginRequestDto

> DTO de peticion para autenticar a un usuario en el sistema. Acepta correo electronico o username junto con la contrasena del usuario.

## MetricasDTO

> DTO que agrupa la cantidad total de busquedas realizadas en un dia especifico. Utilizado en graficas y reportes del panel de metricas administrativo.

## NacionalidadDto

> DTO de lectura que expone el identificador y nombre de una nacionalidad del catalogo. Utilizado en listas desplegables durante el registro de usuarios.

## PerfilDTO

> DTO de lectura con la informacion del perfil del usuario autenticado. Incluye datos personales, credenciales de contacto y ubicacion geografica.

## ReservacionCreadaDTO

> DTO de respuesta devuelto al crear una nueva reservacion exitosamente. Incluye identificador, numero de reservacion, expiracion, total y lista de boletos asignados.

## ReservacionDetalleDTO

> DTO de lectura con el detalle completo de una reservacion. Incluye estado, usuario propietario, factura asociada, lista de boletos y datos de cancelacion si aplica.

## ResultadoBusquedaDTO

> DTO de respuesta que agrupa los resultados de una busqueda de vuelos. Separa los vuelos directos disponibles de los itinerarios con escala.

## RutaAgenciaDTO

> DTO de lectura que expone la informacion de una ruta disponible para agencias. Incluye ciudad y pais de origen y destino, ademas de la duracion estimada en minutos.

## RutaDTO

> DTO para mostrar una ruta en el panel de administracion. Incluye aeropuertos, zonas horarias y duracion editable.

## SubirImagenDTO

> DTO para subir una imagen codificada en Base64 de forma independiente a cualquier entidad. Se utiliza en endpoints de carga de imagenes para aeropuertos, aviones y tripulantes.

## TokenHotelDTO

> DTO con los datos necesarios para solicitar un token de alianza a un hotel.

## TripulanteDTO

> DTO de lectura que expone la informacion de un tripulante registrado en el sistema. Incluye nombre completo, rol de tripulacion e imagen opcional en Base64.

## UsuarioInfoDTO

> DTO de lectura con la informacion completa de un usuario para uso administrativo. Incluye datos personales, ubicacion, rol asignado y lista de nacionalidades registradas.

## VotarComentarioDTO

> DTO de peticion para registrar o actualizar el voto de un usuario sobre un comentario. El campo Valor debe ser 1 para voto positivo o -1 para voto negativo.

## VueloDetalleDTO

> DTO de lectura con el detalle completo de un vuelo para mostrar en resultados de busqueda y paneles de administracion. Incluye estado, avion, aeropuertos de origen y destino, precios, disponibilidad por clase y lista de tripulantes asignados.

## VueloHistorialDTO

> DTO de lectura que expone el historial de un vuelo para reportes administrativos. Incluye numero de vuelo, ruta, horarios, estado, capacidad, boletos vendidos y precios.
