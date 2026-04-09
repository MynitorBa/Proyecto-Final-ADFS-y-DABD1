# DTO


# Package dto

Contiene los Data Transfer Objects utilizados para la comunicacion entre la capa
de transporte y la capa de servicio de la agencia de viajes.
































## TYPES

```go

type ActualizarCatalogoResponse struct {
	Proveedor  string `json:"proveedor"`  // Nombre del proveedor cuyo catalogo fue actualizado
	Insertados int    `json:"insertados"` // Cantidad de registros nuevos insertados en la base de datos
	Mensaje    string `json:"mensaje"`    // Mensaje descriptivo del resultado de la operacion
}
    ActualizarCatalogoResponse

    Representa la respuesta retornada al cliente tras ejecutar la actualizacion
    del catalogo de rutas o habitaciones de un proveedor.

type AgregarDetalleHotelRequest struct {
	ReservacionID int                   `json:"reservacionId"` // ID de la reservacion a la que se agrega el detalle
	ProveedorID   int                   `json:"proveedorId"`   // ID del proveedor hotelero
	Habitaciones  []SeleccionHabitacion `json:"habitaciones"`  // Lista de habitaciones seleccionadas con fechas y personas
    AgregarDetalleHotelRequest

    Representa la solicitud para agregar una o varias habitaciones seleccionadas
    como detalle de hotel dentro de una reservacion existente.

type AgregarDetalleVueloRequest struct {
	ReservacionID int              `json:"reservacion_id"` // ID de la reservacion a la que se agrega el detalle
	ProveedorID   int              `json:"proveedor_id"`   // ID del proveedor aerolinea
	Vuelos        []SeleccionVuelo `json:"vuelos"`         // Lista de vuelos seleccionados con clase y cantidad de pasajeros
    AgregarDetalleVueloRequest

    Representa la solicitud para agregar uno o varios vuelos seleccionados como
    detalle dentro de una reservacion existente.

type AgregarPasajerosVueloAerolineaBody struct {
	ReservacionID int                `json:"reservacionId"` // ID de la reservacion en el sistema de la aerolinea
	Pasajeros     []PasajeroVueloDTO `json:"pasajeros"`     // Lista de pasajeros con sus datos personales
    AgregarPasajerosVueloAerolineaBody

    Representa el cuerpo de la solicitud que se envia directamente a la API de
    la aerolinea para registrar los datos de pasajeros en la reservacion del
    proveedor.

type AgregarPasajerosVueloRequest struct {
	ReservacionID int                `json:"reservacion_id"` // ID de la reservacion en la base de datos de la agencia
	ProveedorID   int                `json:"proveedor_id"`   // ID del proveedor aerolinea
	Pasajeros     []PasajeroVueloDTO `json:"pasajeros"`      // Lista de pasajeros con sus datos personales
    AgregarPasajerosVueloRequest

    Representa la solicitud recibida por el endpoint de la agencia para agregar
    los datos de pasajeros a los boletos de un vuelo reservado.

type AsientoVueloDetalleDTO struct {
	VueloID            int                `json:"vueloId"`            // ID unico del vuelo
	NumeroVuelo        string             `json:"numeroVuelo"`        // Numero identificador del vuelo
	CapacidadPasajeros int                `json:"capacidadPasajeros"` // Capacidad total de pasajeros
	Columnas           []string           `json:"columnas"`           // Letras de columnas disponibles (ej. ["A","B","C"])
	FilasEjecutiva     int                `json:"filasEjecutiva"`     // Numero de filas de clase ejecutiva
	TotalFilas         int                `json:"totalFilas"`         // Total de filas en el avion
	AsientosOcupados   []string           `json:"asientosOcupados"`   // Lista de codigos de asientos ya ocupados
	BoletosAgencia     []BoletoAsientoDTO `json:"boletosAgencia"`     // Boletos pertenecientes a la agencia en este vuelo
    AsientoVueloDetalleDTO

    Representa la informacion detallada del mapa de asientos de un vuelo
    individual, incluyendo ocupacion y boletos de la agencia.

type AsientosVueloResponse []AsientoVueloDetalleDTO
    AsientosVueloResponse

    Representa la respuesta completa de consulta de asientos, siendo una lista
    de detalles por cada vuelo de la reservacion.

type BoletoAsientoDTO struct {
	BoletoID int    `json:"boletoId"` // ID unico del boleto
	NoBoleto string `json:"noBoleto"` // Numero de boleto legible
	Asiento  string `json:"asiento"`  // Codigo de asiento asignado (ej. "12A")
	ClaseID  int    `json:"claseId"`  // ID de la clase de servicio
	Clase    string `json:"clase"`    // Nombre de la clase de servicio (ej. "Economica")
    BoletoAsientoDTO

    Representa los datos de un boleto individual con su asiento y clase
    asignados dentro de un vuelo.

type BusquedaHotelesRequest struct {
	Ciudad           string `json:"ciudad"`           // Ciudad donde se busca el hotel
	Pais             string `json:"pais"`             // Pais donde se busca el hotel
	FechaCheckIn     string `json:"fechaCheckIn"`     // Fecha de entrada en formato ISO 8601
	FechaCheckOut    string `json:"fechaCheckOut"`    // Fecha de salida en formato ISO 8601
	CantidadPersonas int    `json:"cantidadPersonas"` // Numero de personas que se hospedan
    BusquedaHotelesRequest

    Representa los criterios de busqueda enviados por el cliente para consultar
    hoteles disponibles en todos los proveedores hoteleros. Es compartido por
    las rutas de busqueda individual y general.

type BusquedaHotelesResponse struct {
	Proveedor   string      `json:"proveedor"`       // Nombre del proveedor hotelero
	ProveedorID int         `json:"proveedor_id"`    // ID del proveedor hotelero
	Datos       interface{} `json:"datos"`           // Datos crudos de hoteles devueltos por el proveedor
	Error       string      `json:"error,omitempty"` // Mensaje de error si la consulta al proveedor fallo
    BusquedaHotelesResponse

    Representa la respuesta de busqueda de hoteles para un proveedor especifico,
    encapsulando los datos crudos devueltos por su API o el error ocurrido.

type BusquedaVuelosRequest struct {
	Origen            string `json:"origen"`            // Ciudad de origen del vuelo
	OrigenPais        string `json:"origenPais"`        // Pais de origen del vuelo
	Destino           string `json:"destino"`           // Ciudad de destino del vuelo
	DestinoPais       string `json:"destinoPais"`       // Pais de destino del vuelo
	Fecha             string `json:"fecha"`             // Fecha del vuelo en formato ISO 8601
	CantidadPasajeros int    `json:"cantidadPasajeros"` // Numero de pasajeros requeridos
    BusquedaVuelosRequest

    vuelos disponibles en todos los proveedores aerolinea. Es compartido por las
    rutas de busqueda individual y general.

type BusquedaVuelosResponse struct {
	Proveedor   string      `json:"proveedor"`       // Nombre del proveedor aerolinea
	ProveedorID int         `json:"proveedor_id"`    // ID del proveedor aerolinea
	Datos       interface{} `json:"datos"`           // Datos crudos de vuelos devueltos por el proveedor
    BusquedaVuelosResponse

    Representa la respuesta de busqueda de vuelos para un proveedor especifico,

type CambiarAsientoAerolineaBody struct {
	NuevoAsiento string `json:"nuevoAsiento"` // Codigo del nuevo asiento a asignar
    CambiarAsientoAerolineaBody

    la aerolinea para actualizar el asiento de un pasajero.

type CambiarAsientoVueloRequest struct {
	ReservacionID int    `json:"reservacion_id"` // ID de la reservacion en la base de datos de la agencia
	ProveedorID   int    `json:"proveedor_id"`   // ID del proveedor aerolinea
	BoletoID      int    `json:"boleto_id"`      // ID del boleto al que se le cambia el asiento
	NuevoAsiento  string `json:"nuevo_asiento"`  // Codigo del nuevo asiento solicitado
    CambiarAsientoVueloRequest

    Representa la solicitud para cambiar el asiento asignado a un boleto dentro
    de una reservacion de vuelo.

type CancelarReservacionRequest struct {
	Motivo string `json:"motivo"` // Motivo de la cancelacion descrito por el usuario
    CancelarReservacionRequest

    Representa la solicitud para cancelar una reservacion, incluyendo el motivo
    de la cancelacion proporcionado por el usuario.

type CrearProveedorRequest struct {
	Nombre             string  `json:"nombre" binding:"required"`            // Nombre comercial del proveedor
	TipoProveedorID    int     `json:"tipo_proveedor_id" binding:"required"` // ID del tipo de proveedor (aerolinea u hotel)
	URLAPI             string  `json:"url_api"`                              // URL base de la API del proveedor
	UsuarioID          int     `json:"usuario_id" binding:"required"`        // ID del usuario vinculado; debe tener rol 3
	PorcentajeGanancia float64 `json:"porcentaje_ganancia"`                  // Porcentaje de ganancia aplicado sobre el precio base
    CrearProveedorRequest

    Representa los datos necesarios para registrar un nuevo proveedor en el
    sistema de la agencia. El campo UsuarioID debe corresponder a un usuario con
    rol 3 (administrador de proveedor).

type CrearProveedorResponse struct {
	ID                 int     `json:"id"`                  // ID generado para el nuevo proveedor
	Nombre             string  `json:"nombre"`              // Nombre comercial del proveedor
	TipoProveedorID    int     `json:"tipo_proveedor_id"`   // ID del tipo de proveedor registrado
	URLAPI             string  `json:"url_api"`             // URL base de la API del proveedor
	UsuarioID          int     `json:"usuario_id"`          // ID del usuario vinculado al proveedor
	EstadoID           int     `json:"estado_id"`           // ID del estado inicial del proveedor
	PorcentajeGanancia float64 `json:"porcentaje_ganancia"` // Porcentaje de ganancia configurado
    CrearProveedorResponse

    Representa los datos del proveedor recien creado que se retornan al cliente
    tras el registro exitoso en el sistema.

type CrearReservacionRequest struct {
	TipoReservaID int `json:"tipo_reserva_id"` // ID del tipo de reserva (1=Aerolinea, 2=Hotel, 3=Paquete)
    CrearReservacionRequest

    Representa la solicitud para crear una nueva reservacion, indicando el tipo
    de reserva que el usuario desea iniciar.

type CrearReservacionResponse struct {
	ID              int    `json:"id"`               // ID unico generado para la reservacion
	NoReservacion   string `json:"no_reservacion"`   // Numero de reservacion legible para el usuario
	EstadoID        int    `json:"estado_id"`        // ID del estado inicial de la reservacion
	Estado          string `json:"estado"`           // Nombre del estado inicial de la reservacion
	TipoReservaID   int    `json:"tipo_reserva_id"`  // ID del tipo de reserva creado
	FechaExpiracion string `json:"fecha_expiracion"` // Fecha y hora de expiracion de la reservacion
	FechaCreacion   string `json:"fecha_creacion"`   // Fecha y hora de creacion de la reservacion
    CrearReservacionResponse

    Representa la respuesta retornada al cliente tras crear exitosamente una
    nueva reservacion en el sistema.

type DetalleCompletoResponse struct {
	ID                 int         `json:"id"`                   // ID unico del detalle
	TipoDetalleID      int         `json:"tipo_detalle_id"`      // Tipo de detalle: 1=Vuelo, 2=Hotel
	IDReservaProveedor string      `json:"id_reserva_proveedor"` // ID de la reserva en el sistema del proveedor
	Total              float64     `json:"total"`                // Monto total del detalle
	EstadoDetalleID    int         `json:"estado_detalle_id"`    // ID del estado del detalle
	ParametrosJson     interface{} `json:"parametros_json"`      // Parametros adicionales almacenados como JSON
	DataProveedor      interface{} `json:"data_proveedor"`       // Respuesta cruda retornada por el proveedor externo
    DetalleCompletoResponse

    Representa un detalle de reservacion enriquecido con los datos locales de la
    agencia mas la respuesta cruda obtenida del proveedor externo.

type DetalleProveedor struct {
	IDReservaProveedor string // ID de la reserva en el sistema del proveedor externo
	ProveedorID        int    // ID del proveedor en la base de datos de la agencia
	URLAPI             string // URL base de la API del proveedor
	TokenEntrada       string // Token de autenticacion para acceder a la API del proveedor
	TipoDetalleID      int    // ID del tipo de detalle (1=Vuelo, 2=Hotel)
    DetalleProveedor

    Representa la informacion de un detalle de reservacion vinculado a un
    proveedor externo, incluyendo los datos de acceso a su API.

type DetalleResumenResponse struct {
    DetalleResumenResponse

    Representa los datos de un detalle de reservacion guardados localmente en la
    base de datos de la agencia, sin datos del proveedor.

type FilaReservacionDetalle struct {
	ReservacionID      int     // ID unico de la reservacion
	NoReservacion      string  // Numero de reservacion
	TipoReservaID      int     // Tipo de reserva
	EstadoID           int     // Estado de la reservacion
	Total              float64 // Monto total de la reservacion
	FechaCreacion      string  // Fecha de creacion de la reservacion
	FechaExpiracion    *string // Fecha de expiracion (puede ser nula)
	DetalleID          int     // ID del detalle de reservacion
	TipoDetalleID      int     // Tipo del detalle (1=Vuelo, 2=Hotel)
	IDReservaProveedor string  // ID de la reserva en el proveedor
	DetalleTotal       float64 // Monto total del detalle
	EstadoDetalleID    int     // Estado del detalle
	ParametrosJson     string  // Parametros del detalle en formato JSON crudo
	ProveedorID        int     // ID del proveedor asociado al detalle
	URLAPI             string  // URL base de la API del proveedor
	TokenEntrada       string  // Token de autenticacion del proveedor
    FilaReservacionDetalle

    Representa una fila plana del resultado de una consulta SQL que une datos de
    reservacion con sus detalles y la informacion del proveedor, utilizada para
    construir las respuestas estructuradas.

type HandshakeResponse struct {
	Mensaje     string `json:"mensaje"`      // Mensaje descriptivo del resultado del handshake
	TokenSalida string `json:"token_salida"` // Token de autenticacion devuelto por la aerolinea para sesiones posteriores
    HandshakeResponse

    Representa la respuesta recibida del proveedor externo durante el proceso de
    handshake de autenticacion, que incluye el token de sesion devuelto por la
    aerolinea.

type HotelProveedorDTO struct {
	ID     int    `json:"id"`     // ID unico del hotel en el sistema del proveedor
	Nombre string `json:"nombre"` // Nombre comercial del hotel
	Ciudad string `json:"ciudad"` // Ciudad donde se encuentra el hotel
	Pais   string `json:"pais"`   // Pais donde se encuentra el hotel
    HotelProveedorDTO

    Representa la informacion basica de un hotel registrado en el catalogo de un
    proveedor hotelero externo.

type LoginRequest struct {
	Login      string `json:"login"`      // Correo electronico o nombre de usuario
	Contrasena string `json:"contrasena"` // Contrasena del usuario
    LoginRequest

    Representa las credenciales enviadas por el usuario para iniciar sesion en
    el sistema de la agencia.

type LoginResponse struct {
	ID       int    `json:"id"`       // ID unico del usuario en la base de datos
	Nombre   string `json:"nombre"`   // Nombre del usuario
	Apellido string `json:"apellido"` // Apellido del usuario
	Correo   string `json:"correo"`   // Correo electronico del usuario
	Username string `json:"username"` // Nombre de usuario unico
	RolID    int    `json:"rol_id"`   // ID del rol asignado al usuario
    LoginResponse

    Representa los datos del usuario autenticado que se retornan al cliente tras
    un inicio de sesion exitoso.

type ObtenerAsientosVueloRequest struct {
	ReservacionID int `json:"reservacion_id"` // ID de la reservacion en la base de datos de la agencia
	ProveedorID   int `json:"proveedor_id"`   // ID del proveedor aerolinea
    ObtenerAsientosVueloRequest

    Representa la solicitud para consultar los asientos disponibles de un vuelo
    asociado a una reservacion especifica.

type PagoProveedorBody struct {
	Nit          string `json:"nit"`          // Numero de identificacion tributaria del cliente
	CodigoPostal string `json:"codigoPostal"` // Codigo postal de facturacion
    PagoProveedorBody

    Representa el cuerpo de la solicitud que se envia al proveedor externo para
    procesar el pago de una reservacion en su sistema.

type PagoReservacionRequest struct {
	ReservacionID int    `json:"reservacion_id" binding:"required"` // ID de la reservacion a pagar
	TarjetaNumero string `json:"tarjeta_numero" binding:"required"` // Numero completo de la tarjeta de credito o debito
	TarjetaCVV    string `json:"tarjeta_cvv" binding:"required"`    // Codigo de verificacion de la tarjeta
	TarjetaMes    string `json:"tarjeta_mes" binding:"required"`    // Mes de vencimiento de la tarjeta (formato MM)
	TarjetaAnio   string `json:"tarjeta_anio" binding:"required"`   // Anio de vencimiento de la tarjeta (formato AAAA)
	Nit           string `json:"nit" binding:"required"`            // Numero de identificacion tributaria del cliente
	CodigoPostal  string `json:"codigo_postal" binding:"required"`  // Codigo postal de facturacion
    PagoReservacionRequest

    Representa la solicitud de pago enviada por el cliente para completar
    una reservacion, incluyendo los datos de la tarjeta y la informacion de
    facturacion.

type PasajeroVueloDTO struct {
	BoletoID  int    `json:"boletoId"`  // ID del boleto devuelto por la aerolinea
	Nombre    string `json:"nombre"`    // Nombre del pasajero
	Apellido  string `json:"apellido"`  // Apellido del pasajero
	Pasaporte string `json:"pasaporte"` // Numero de pasaporte (solo digitos)
	Telefono  string `json:"telefono"`  // Numero de telefono de contacto
	Pais      string `json:"pais"`      // Pais de residencia del pasajero
	Ciudad    string `json:"ciudad"`    // Ciudad de residencia del pasajero
    PasajeroVueloDTO

    Representa los datos personales de un pasajero asociado a un boleto
    especifico dentro de una reservacion de vuelo.

type ProveedorCatalogo struct {
	ProveedorID        int     // ID unico del proveedor en la base de datos de la agencia
	Nombre             string  // Nombre comercial del proveedor
	URLApi             string  // URL base de la API del proveedor
	TokenEntrada       string  // Token de autenticacion para acceder a la API del proveedor
	PorcentajeGanancia float64 // Porcentaje de ganancia aplicado al precio base del proveedor
    ProveedorCatalogo

    Representa los datos de un proveedor obtenidos del catalogo interno de la
    agencia, utilizados para realizar consultas a su API externa.

type ProveedorIdentidad struct {
	ID              int    `json:"id"`                // ID unico del proveedor en la base de datos de la agencia
	Nombre          string `json:"nombre"`            // Nombre comercial del proveedor
	TipoProveedorID int    `json:"tipo_proveedor_id"` // ID del tipo de proveedor (aerolinea u hotel)
    ProveedorIdentidad

    Representa los datos de identificacion minimos de un proveedor, utilizados
    para reconocer y clasificar al proveedor en operaciones internas sin cargar
    todos sus datos completos.

type ProveedorPuedeCancelarResponse struct {
	PuedeCancelar bool   `json:"puedeCancelar"` // Indica si el proveedor permite la cancelacion
	Razon         string `json:"razon"`         // Mensaje explicativo del proveedor
    ProveedorPuedeCancelarResponse

    Representa la respuesta que entrega el proveedor externo al consultar si una
    reserva puede ser cancelada en su sistema.

type RegistroUsuarioRequest struct {
	Nombre          string   `json:"nombre"`           // Nombre del usuario
	Apellido        string   `json:"apellido"`         // Apellido del usuario
	Correo          string   `json:"correo"`           // Correo electronico del usuario
	Username        string   `json:"username"`         // Nombre de usuario unico
	Contrasena      string   `json:"contrasena"`       // Contrasena de acceso al sistema
	Pasaporte       string   `json:"pasaporte"`        // Numero de pasaporte del usuario
	Telefono        string   `json:"telefono"`         // Numero de telefono de contacto
	FechaNacimiento string   `json:"fecha_nacimiento"` // Fecha de nacimiento en formato ISO 8601
	Ciudad          string   `json:"ciudad"`           // Ciudad de residencia del usuario
	Pais            string   `json:"pais"`             // Pais de residencia del usuario
	Nacionalidades  []string `json:"nacionalidades"`   // Lista de nacionalidades del usuario
    RegistroUsuarioRequest

    Representa los datos enviados por un nuevo usuario para crear una cuenta en
    el sistema de la agencia de viajes.

type ReservacionConDetalles struct {
	ID       int                // ID unico de la reservacion
	Detalles []DetalleProveedor // Lista de detalles asociados a proveedores externos
    ReservacionConDetalles

    Representa una reservacion junto con la lista de sus detalles de
    proveedores, utilizado internamente para operaciones de negocio.

type ReservacionDetalladaResponse struct {
	ID              int                       `json:"id"`               // ID unico de la reservacion
	NoReservacion   string                    `json:"no_reservacion"`   // Numero de reservacion legible para el usuario
	TipoReserva     int                       `json:"tipo_reserva"`     // Tipo de reserva: 1=Aerolinea, 2=Hotel, 3=Paquete
	EstadoID        int                       `json:"estado_id"`        // ID del estado actual de la reservacion
	Total           float64                   `json:"total"`            // Monto total de la reservacion
	FechaCreacion   string                    `json:"fecha_creacion"`   // Fecha y hora de creacion de la reservacion
	FechaExpiracion *string                   `json:"fecha_expiracion"` // Fecha y hora de expiracion (puede ser nula)
	Detalles        []DetalleCompletoResponse `json:"detalles"`         // Lista de detalles con datos locales y del proveedor
    ReservacionDetalladaResponse

    Representa la informacion completa de una reservacion combinando los datos
    locales de la agencia con las respuestas de los proveedores externos.

type ReservacionResumenResponse struct {
	ID              int                      `json:"id"`               // ID unico de la reservacion
	NoReservacion   string                   `json:"no_reservacion"`   // Numero de reservacion legible para el usuario
	TipoReserva     int                      `json:"tipo_reserva"`     // Tipo de reserva: 1=Aerolinea, 2=Hotel, 3=Paquete
	EstadoID        int                      `json:"estado_id"`        // ID del estado actual de la reservacion
	Total           float64                  `json:"total"`            // Monto total de la reservacion
	FechaCreacion   string                   `json:"fecha_creacion"`   // Fecha y hora de creacion de la reservacion
	FechaExpiracion *string                  `json:"fecha_expiracion"` // Fecha y hora de expiracion (puede ser nula)
	Detalles        []DetalleResumenResponse `json:"detalles"`         // Lista de detalles asociados a la reservacion
    ReservacionResumenResponse

    Representa el resumen de una reservacion con sus datos locales, utilizado
    para listar las reservaciones de un usuario sin consultar informacion
    adicional a los proveedores externos.

type ReservacionValidada struct {
	ID            int // ID unico de la reservacion
	EstadoID      int // ID del estado actual de la reservacion
	TipoReservaID int // ID del tipo de reservacion (1=Aerolinea, 2=Hotel, 3=Paquete)
	UsuarioID     int // ID del usuario propietario de la reservacion
    ReservacionValidada

    Representa los datos minimos de una reservacion ya verificada que se usan
    internamente para validar operaciones posteriores.

type RutaProveedorDTO struct {
	ID            int    `json:"id"`            // ID unico de la ruta en el sistema del proveedor
	CiudadOrigen  string `json:"ciudadOrigen"`  // Ciudad de origen del vuelo
	PaisOrigen    string `json:"paisOrigen"`    // Pais de origen del vuelo
	CiudadDestino string `json:"ciudadDestino"` // Ciudad de destino del vuelo
	PaisDestino   string `json:"paisDestino"`   // Pais de destino del vuelo
	Duracion      int    `json:"duracion"`      // Duracion del vuelo en minutos
    RutaProveedorDTO

    Representa la informacion de una ruta de vuelo devuelta por el proveedor
    aerolinea, incluyendo origen, destino y duracion.

type SeleccionHabitacion struct {
	HabitacionID     int    `json:"habitacionId"`     // ID de la habitacion en el catalogo del proveedor
	CantidadPersonas int    `json:"cantidadPersonas"` // Numero de personas que ocuparan la habitacion
    SeleccionHabitacion

    Representa una habitacion especifica elegida por el usuario, con las fechas
    de estancia y la cantidad de personas.

type SeleccionVuelo struct {
	VueloId           int `json:"vueloId"`           // ID del vuelo en el catalogo del proveedor
	ClaseId           int `json:"claseId"`           // ID de la clase de servicio seleccionada
	CantidadPasajeros int `json:"cantidadPasajeros"` // Numero de pasajeros para este vuelo
    SeleccionVuelo

    Representa un vuelo especifico elegido por el usuario, junto con la clase de
    servicio y la cantidad de pasajeros.

type UsuarioResumen struct {
	RolID    int    `json:"rolId"`    // ID del rol asignado al usuario
	Rol      string `json:"rol"`      // Nombre del rol asignado al usuario
    UsuarioResumen

    Representa los datos basicos de un usuario retornados por el panel de
    administracion para gestion de roles y asignacion de WebService.

type ValidacionUsuarioResponse struct {
	Correo    bool `json:"correo"`    // Indica si el correo ya esta registrado en el sistema
	Pasaporte bool `json:"pasaporte"` // Indica si el pasaporte ya esta registrado en el sistema
	Username  bool `json:"username"`  // Indica si el nombre de usuario ya esta registrado en el sistema
    ValidacionUsuarioResponse

    Representa el resultado de la validacion de unicidad de los datos de un
    usuario durante el proceso de registro, indicando si el correo, pasaporte o
    username ya existen en el sistema.

type VerificarCancelacionResponse struct {
	PuedeCancelar bool                       `json:"puede_cancelar"` // Indica si la reservacion completa puede cancelarse
	Detalles      []VerificarDetalleResponse `json:"detalles"`       // Lista de verificaciones por detalle de reservacion
    VerificarCancelacionResponse

    Representa la respuesta de verificacion previa a la cancelacion, indicando
    si la reservacion puede cancelarse y el detalle por cada componente.

type VerificarDetalleResponse struct {
	TipoDetalleID      int    `json:"tipo_detalle_id"`      // ID del tipo de detalle (1=Vuelo, 2=Hotel)
	IDReservaProveedor string `json:"id_reserva_proveedor"` // ID de la reserva en el sistema del proveedor
	PuedeCancelar      bool   `json:"puede_cancelar"`       // Indica si este detalle puede cancelarse
	Razon              string `json:"razon"`                // Razon del resultado de la verificacion
    VerificarDetalleResponse

    Representa el resultado de verificacion de cancelacion para un detalle
    especifico de la reservacion (vuelo u hotel).

```
