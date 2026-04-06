# Models

## Aeropuerto

> Entidad que representa un aeropuerto registrado en el sistema. Contiene codigo IATA, nombre, ciudad asociada y zona horaria opcional.

## Avion

> Entidad que representa una aeronave registrada en la flota de la aerolinea. Almacena marca, modelo, capacidad de pasajeros e imagen opcional en Base64.

## Boleto

> Entidad que representa un boleto de avion asignado a un vuelo especifico. Contiene numero de boleto, asiento, precio, clase, estado y referencias a la reservacion y datos del pasajero.

## BusquedaVuelo

> Entidad que registra una busqueda de vuelo realizada por un usuario o agencia. Almacena origen, destino, fechas, cantidad de pasajeros y tipo de viaje para fines de analisis y metricas del sistema.

## Ciudad

> Entidad que representa una ciudad del catalogo geografico del sistema. Pertenece a un pais y sirve como referencia para aeropuertos y usuarios.

## Comentario

> Entidad que representa un comentario o resena publicado por un usuario sobre una ruta. Incluye calificacion por estrellas, contenido textual, conteo de votos negativos y fecha.

## DatosPasajero

> Entidad que almacena la informacion personal de un pasajero asociado a un boleto. Incluye nombre, apellido, numero de pasaporte, telefono y ubicacion geografica.

## Down

> Entidad que registra el voto de un usuario sobre un comentario. El campo Valor indica el tipo de voto: 1 para positivo y -1 para negativo. Garantiza que cada usuario solo pueda votar una vez por comentario.

## Guardarbusquedadto

> DTO utilizado para persistir los parametros de una busqueda de vuelo realizada por el usuario. Contiene origen, destino, fechas, numero de pasajeros y tipo de viaje.

## Loginresponsedto

> DTO de respuesta devuelto al cliente despues de un inicio de sesion exitoso. Contiene identificacion del usuario, nombre, correo y datos del rol asignado.

## Nacionalidad

> Entidad que representa una nacionalidad disponible en el catalogo del sistema. Se utiliza para asociar una o mas nacionalidades a un usuario registrado.

## Pais

> Entidad que representa un pais del catalogo geografico del sistema. Sirve como referencia para ciudades, aeropuertos y datos de usuario.

## RegisterConstraint

> DTO que indica que campos unicos ya existen en el sistema durante el proceso de registro. Se utiliza para validar disponibilidad de correo, username y pasaporte antes de crear el usuario.

## Reservacion

> Entidad que representa una reservacion de vuelo realizada por un usuario. Contiene numero unico de reserva, fechas de creacion y expiracion, total a pagar y estado actual del proceso de reserva.

## RolTripulacion

> Entidad que define un rol o cargo dentro de la tripulacion de un vuelo. Ejemplos de cargos: Piloto, Copiloto, Auxiliar de vuelo.

## Tripulante

> Entidad que representa a un miembro de la tripulacion registrado en el sistema. Contiene nombre, apellido, rol asignado e imagen opcional en Base64.

## Usuario

> Entidad principal que representa a un usuario registrado en el sistema. Almacena credenciales de acceso, datos personales, ubicacion geografica y el rol asignado que determina los permisos dentro de la plataforma.

## UsuarioNacionalidad

> Entidad de relacion que vincula a un usuario con una o mas nacionalidades. Permite que un usuario tenga multiples nacionalidades registradas en el sistema.

## Vuelo

> Entidad que representa un vuelo programado en el sistema. Contiene numero de vuelo, fecha, horarios de salida y llegada, avion asignado, ruta, disponibilidad de boletos por clase y precios de cada clase.
