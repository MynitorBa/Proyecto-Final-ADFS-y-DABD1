# Services


# Package services

Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
con proveedores externos (aerolineas y hoteleras).








Contiene los servicios de negocio de la agencia de viajes, incluyendo
procesamiento de pagos, reservaciones, proveedores y usuarios.






























## CONSTANTS

```go

const (
	TipoAerolinea = 1
	TipoHotelera  = 2
)
	TipoDetalleVuelo = 1
	TipoDetalleHotel = 2

	TipoReservaAerolinea = 1
	TipoReservaHotelera  = 2
	TipoReservaPaquete   = 3

```

## VARIABLES

```go

var (
	ErrUsuarioNoEncontrado = errors.New("usuario no encontrado")
	ErrContrasenaInvalida  = errors.New("contrase├▒a inv├ílida")
var ErrCredencialesInvalidas = errors.New("credenciales inv├ílidas")

```

## TYPES

```go

type AsientoVueloService struct {
	// Has unexported fields.
}
    AsientoVueloService

    Servicio encargado de gestionar los asientos de vuelo asociados a
    reservaciones de aerolinea. Permite consultar los asientos disponibles y
    realizar cambios de asiento para boletos especificos de una reservacion.

func NewAsientoVueloService(db *sql.DB) *AsientoVueloService
    NewAsientoVueloService

    Crea e inicializa una nueva instancia de AsientoVueloService con su
    repositorio de detalle de reservacion.

    Parametros:
      - db: conexion activa a la base de datos SQL

    Retorna:
      - *AsientoVueloService: instancia lista para usar

func (s *AsientoVueloService) CambiarAsientoVuelo(
	usuarioID int,
	req dto.CambiarAsientoVueloRequest,
) error
    CambiarAsientoVuelo

    Cambia el asiento asignado a un boleto especifico de una reservacion de
    vuelo. Valida que el boleto pertenezca a la reservacion del usuario antes de
    enviar la solicitud de cambio al proveedor aerolinea.

      - usuarioID: identificador del usuario dueno de la reservacion
      - req: datos del cambio incluyendo ReservacionID, ProveedorID, BoletoID y
        NuevoAsiento

      - error: si el boleto no pertenece a la reservacion o falla la API del
        proveedor

func (s *AsientoVueloService) ObtenerAsientosVuelo(
	req dto.ObtenerAsientosVueloRequest,
) (*dto.AsientosVueloResponse, error)
    ObtenerAsientosVuelo

    Consulta los asientos de vuelo disponibles para una reservacion especifica,
    obteniendo primero los datos de conexion del proveedor aerolinea desde la BD
    y luego llamando a su API externa.

      - req: datos de la solicitud incluyendo ReservacionID y ProveedorID

      - *dto.AsientosVueloResponse: lista de vuelos con sus boletos y asientos
      - error: si la reservacion no existe, no pertenece al usuario o falla la
        API del proveedor

type BusquedaService struct {
    BusquedaService

    Servicio encargado de realizar busquedas de vuelos y hoteles consultando
    los proveedores externos registrados en el catalogo. Aplica el margen de
    ganancia configurado por proveedor sobre los precios retornados.

func NewBusquedaService(db *sql.DB) *BusquedaService
    NewBusquedaService

    Crea e inicializa una nueva instancia de BusquedaService con su repositorio
    de busqueda.


      - *BusquedaService: instancia lista para usar

func (s *BusquedaService) BuscarHoteles(req dto.BusquedaHotelesRequest) ([]dto.BusquedaHotelesResponse, error)
    BuscarHoteles

    Busca hoteles disponibles en una ciudad consultando todos los proveedores
    hoteleras registrados para esa ubicacion en el catalogo. Resuelve el ID de
    ciudad, obtiene la lista de proveedores de tipo hotelera y llama a cada
    uno de forma individual. Si un proveedor falla, se incluye su error en la
    respuesta sin interrumpir las demas consultas.

      - req: datos de busqueda incluyendo ciudad, pais y demas filtros de
        hospedaje

      - []dto.BusquedaHotelesResponse: lista de respuestas por proveedor,
        con datos o error
      - error: si falla la resolucion de ciudad o la consulta de proveedores en
        BD

func (s *BusquedaService) BuscarVuelos(req dto.BusquedaVuelosRequest) ([]dto.BusquedaVuelosResponse, error)
    BuscarVuelos

    Busca vuelos disponibles entre dos ciudades consultando todos los
    proveedores aerolineas registrados para esa ruta en el catalogo. Resuelve
    los IDs de ciudad para origen y destino, obtiene la lista de proveedores
    activos y llama a cada uno de forma individual. Si un proveedor falla,
    se incluye su error en la respuesta sin interrumpir las demas consultas.

      - req: datos de busqueda incluyendo ciudad/pais de origen, destino y demas
        filtros

      - []dto.BusquedaVuelosResponse: lista de respuestas por proveedor,
      - error: si falla la resolucion de ciudades o la consulta de proveedores
        en BD

type CancelacionService struct {
    CancelacionService

    Servicio encargado de gestionar el proceso de cancelacion de reservaciones.
    Implementa un flujo de dos pasos: primero verifica si todos los proveedores
    involucrados permiten la cancelacion, y luego ejecuta la cancelacion en cada
    proveedor y en la base de datos local de forma atomica (todo o nada).

func NewCancelacionService(repo *repositories.CancelacionRepository) *CancelacionService
    NewCancelacionService

    Crea e inicializa una nueva instancia de CancelacionService con el
    repositorio de cancelacion proporcionado.

      - repo: repositorio de cancelacion ya inicializado

      - *CancelacionService: instancia lista para usar

func (s *CancelacionService) CancelarReservacion(reservacionID, usuarioID int, motivo string) error
    CancelarReservacion

    Paso 2 del flujo de cancelacion. Ejecuta la cancelacion completa de una
    reservacion. Primero verifica el estado y re-valida con cada proveedor
    (rollback logico), luego cancela en cada proveedor externo y finalmente
    actualiza el estado en la base de datos local. Si algun proveedor rechaza la
    cancelacion, el proceso se detiene sin cancelar nada.

      - reservacionID: identificador de la reservacion a cancelar
      - motivo: descripcion del motivo de cancelacion

      - error: si la reservacion no es cancelable, algun proveedor rechaza o
        falla la BD

func (s *CancelacionService) VerificarCancelacion(reservacionID, usuarioID int) (*dto.VerificarCancelacionResponse, error)
    VerificarCancelacion

    Paso 1 del flujo de cancelacion. Verifica si una reservacion puede ser
    cancelada consultando el estado local y luego preguntando a cada proveedor
    externo involucrado. Si cualquier proveedor rechaza la cancelacion,
    el resultado global indica que no es posible cancelar.

      - reservacionID: identificador de la reservacion a verificar

      - *dto.VerificarCancelacionResponse: resultado con flag global y detalle
        por proveedor

type CatalogoService struct {
    CatalogoService

    Servicio encargado de sincronizar el catalogo local de rutas y ubicaciones
    con la informacion provista por los proveedores externos registrados.
    Soporta proveedores de tipo aerolinea (rutas origen-destino) y hotelera
    (hoteles por ciudad), gestionando la creacion de ubicaciones nuevas mediante
    el UbicacionService.

func NewCatalogoService(db *sql.DB, ubicacionService *UbicacionService) *CatalogoService
    NewCatalogoService

    Crea e inicializa una nueva instancia de CatalogoService con su repositorio
    y el servicio de ubicaciones requerido.

      - ubicacionService: servicio para obtener o crear ciudades/paises en BD

      - *CatalogoService: instancia lista para usar

func (s *CatalogoService) ActualizarCatalogo() ([]dto.ActualizarCatalogoResponse, error)
    ActualizarCatalogo

    Actualiza el catalogo completo de la agencia iterando sobre todos
    los proveedores activos registrados en BD. Por cada proveedor llama a
    actualizarProveedor y acumula el resultado o el error en la respuesta.
    Un fallo en un proveedor no detiene el proceso para los demas.

      - []dto.ActualizarCatalogoResponse: lista de resultados por proveedor con
        conteo de insertados
      - error: si falla la consulta de proveedores activos en BD

type ComentarioService struct {
    ComentarioService

    Servicio encargado de obtener comentarios de vuelos y hoteles consultando
    directamente las APIs de los proveedores externos registrados.

func NewComentarioService(proveedorRepo *repositories.ProveedorRepository) *ComentarioService
    NewComentarioService

    Crea e inicializa una nueva instancia de ComentarioService con su
    repositorio de proveedores.

      - proveedorRepo: repositorio de proveedores para obtener URL y token de
        acceso

      - *ComentarioService: instancia inicializada del servicio de comentarios

func (s *ComentarioService) ObtenerComentariosHotel(proveedorID, hotelID int) (interface{}, error)
    ObtenerComentariosHotel

    Obtiene los comentarios de un hotel consultando la API del proveedor
    identificado por su ID. Construye la URL del endpoint de comentarios de
    hotel y delega la llamada HTTP al metodo interno llamarProveedor.

      - proveedorID: identificador del proveedor hotelero en la base de datos
      - hotelID: identificador del hotel en el sistema del proveedor

      - interface{}: respuesta JSON deserializada del proveedor
      - error: error si el proveedor no existe o la llamada HTTP falla

func (s *ComentarioService) ObtenerComentariosVuelo(proveedorID, rutaID int) (interface{}, error)
    ObtenerComentariosVuelo

    Obtiene los comentarios de una ruta de vuelo consultando la API del
    proveedor identificado por su ID. Construye la URL del endpoint de
    comentarios de aerolinea y delega la llamada HTTP al metodo interno
    llamarProveedor.

      - proveedorID: identificador del proveedor de aerolinea en la base de
        datos
      - rutaID: identificador de la ruta de vuelo en el sistema del proveedor


type DetalleReservacionService struct {
    DetalleReservacionService

    Servicio encargado de agregar detalles de vuelo y hotel a reservaciones
    existentes en estado pendiente. Coordina la reserva con el proveedor
    externo, aplica el margen de ganancia configurado, almacena el detalle
    en BD y recalcula el total de la reservacion. Tambien gestiona el alta de
    pasajeros en el sistema de la aerolinea.

func NewDetalleReservacionService(db *sql.DB) *DetalleReservacionService
    NewDetalleReservacionService

    Crea e inicializa una nueva instancia de DetalleReservacionService con su


      - *DetalleReservacionService: instancia lista para usar

func (s *DetalleReservacionService) AgregarDetalleHotel(usuarioID int, req dto.AgregarDetalleHotelRequest) (interface{}, error)
    AgregarDetalleHotel

    Agrega un detalle de hotel a una reservacion existente. Valida que la
    reservacion pertenezca al usuario, este en estado pendiente y no sea
    exclusivamente de tipo aerea. Llama al proveedor hotelera para crear la
    reserva, calcula el precio con ganancia por noche (con soporte para personas
    extra) y guarda el detalle en BD, actualizando el total.

      - req: datos del detalle incluyendo ReservacionID, ProveedorID y lista de
        habitaciones

      - interface{}: mapa con mensaje, IDs, total base, total con ganancia y
        detalle del proveedor
      - error: si la reservacion no existe, no es valida, falla el proveedor o
        la BD

func (s *DetalleReservacionService) AgregarDetalleVuelo(usuarioID int, req dto.AgregarDetalleVueloRequest) (interface{}, error)
    AgregarDetalleVuelo

    Agrega un detalle de vuelo a una reservacion existente. Valida que la
    exclusivamente de tipo hotelera. Llama al proveedor aerolinea para crear la
    reserva, calcula el precio con ganancia por boleto y guarda el detalle en
    BD, actualizando el total de la reservacion.

        vuelos


func (s *DetalleReservacionService) AgregarPasajerosVuelo(
	req dto.AgregarPasajerosVueloRequest,
    AgregarPasajerosVuelo

    Registra los datos de los pasajeros en el sistema de la aerolinea para una
    reservacion existente. Valida que cada pasajero tenga un numero de pasaporte
    con solo digitos antes de enviar la solicitud al proveedor.

      - req: datos de la solicitud incluyendo ReservacionID, ProveedorID y lista
        de pasajeros

      - error: si el pasaporte es invalido, falla la obtencion del detalle o
        falla el proveedor

type EmailReservacionService struct {
    EmailReservacionService

    Servicio encargado de generar el PDF de confirmacion de una reservacion y
    enviarlo al correo electronico del usuario titular.

func NewEmailReservacionService(
	misSvc *MisReservacionesService,
	pdfSvc *PdfReservacionService,
	usuRepo *repositories.UsuarioRepository,
) *EmailReservacionService
    NewEmailReservacionService

    Crea e inicializa una nueva instancia de EmailReservacionService con sus
    dependencias.

      - misSvc: servicio de mis reservaciones para obtener el detalle completo
        desde proveedores
      - pdfSvc: servicio de PDF para generar el archivo adjunto
      - usuRepo: repositorio de usuarios para obtener nombre y correo cuando no
        los provee el proveedor

      - *EmailReservacionService: instancia inicializada del servicio de envio
        de correos

func (s *EmailReservacionService) EnviarConfirmacion(reservacionID, usuarioID int) error
    EnviarConfirmacion

    Ejecuta el flujo completo de envio del correo de confirmacion: obtiene el
    detalle de la reservacion desde los proveedores, prepara los datos del PDF,
    completa nombre y correo del usuario desde la base de datos si es necesario
    (caso reservas de hotel puro), genera el PDF como adjunto, construye el
    cuerpo HTML del correo y lo envia al destinatario.

      - reservacionID: identificador de la reservacion a confirmar
      - usuarioID: identificador del usuario propietario de la reservacion

      - error: error si la reservacion no existe, no se puede determinar el
        correo destinatario, falla la generacion del PDF o el envio del correo

type ExpiracionService struct {
    ExpiracionService

    Servicio encargado de expirar automaticamente las reservaciones pendientes
    que han superado su tiempo limite. Ejecuta una revision periodica en segundo
    plano cada minuto y tambien permite expirar manualmente las reservaciones
    pendientes de un usuario especifico. Notifica a cada proveedor externo antes
    de actualizar el estado en la base de datos local.

func NewExpiracionService(db *sql.DB) *ExpiracionService
    NewExpiracionService

    Crea e inicializa una nueva instancia de ExpiracionService con su
    repositorio de reservaciones y el canal de control para detener el proceso
    en segundo plano.


      - *ExpiracionService: instancia lista para usar, aun no iniciada

func (s *ExpiracionService) Detener()
    Detener

    Detiene el proceso de expiracion en segundo plano cerrando el canal de
    control. Debe llamarse al apagar la aplicacion para liberar la goroutine.

func (s *ExpiracionService) ExpirarReservacionesDeUsuario(usuarioID int) error
    ExpirarReservacionesDeUsuario

    Expira todas las reservaciones pendientes de un usuario especifico que hayan
    superado su tiempo limite. Itera sobre las reservaciones obtenidas y llama
    a expirarUna para cada una, registrando en log los errores individuales sin
    interrumpir el proceso.

      - usuarioID: identificador del usuario cuyas reservaciones se deben
        revisar

      - error: si falla la consulta de reservaciones pendientes del usuario en

func (s *ExpiracionService) Iniciar()
    Iniciar

    Lanza en segundo plano una goroutine que revisa y expira reservaciones
    pendientes vencidas cada minuto. El proceso puede detenerse llamando a
    Detener.

    Notas:
      - Registra en log cada revision completada o error ocurrido

type HandshakeHoteleraService struct {
    HandshakeHoteleraService

    Servicio encargado de gestionar el proceso de handshake de autenticacion con
    proveedores de tipo hotelera. Genera un token de entrada para la agencia,
    lo envia a la hotelera y almacena ambos tokens (entrada y salida) en BD para
    su uso en comunicaciones posteriores.

func NewHandshakeHoteleraService(db *sql.DB, cfg *config.Config) *HandshakeHoteleraService
    NewHandshakeHoteleraService

    Crea e inicializa una nueva instancia de HandshakeHoteleraService con su
    repositorio de proveedores y la URL publica de la agencia.

      - cfg: configuracion de la aplicacion que contiene la URL del servidor

      - *HandshakeHoteleraService: instancia lista para usar

func (s *HandshakeHoteleraService) IniciarHandshake(proveedorID int) (string, error)
    IniciarHandshake

    Ejecuta el flujo completo de handshake con una hotelera proveedora.
    Obtiene la URL del proveedor, genera un token de entrada para la agencia,
    lo envia a la hotelera junto con la URL de la agencia, recibe el token de
    salida del proveedor y guarda ambos tokens en BD.

      - proveedorID: identificador del proveedor hotelera con quien hacer
        handshake

      - string: token de salida recibido del proveedor hotelera
      - error: si el proveedor no existe, no tiene URL, falla la generacion del
        token, falla la comunicacion con la hotelera o falla el guardado en BD

type HandshakeService struct {
    HandshakeService

    proveedores de tipo aerolinea. Genera un token de entrada para la agencia,
    lo envia a la aerolinea y almacena ambos tokens (entrada y salida) en BD
    para su uso en comunicaciones posteriores.

func NewHandshakeService(db *sql.DB, cfg *config.Config) *HandshakeService
    NewHandshakeService

    Crea e inicializa una nueva instancia de HandshakeService con su repositorio
    de proveedores y la URL publica de la agencia.


      - *HandshakeService: instancia lista para usar

func (s *HandshakeService) IniciarHandshake(proveedorID int) (string, error)

    Ejecuta el flujo completo de handshake con una aerolinea proveedora.
    lo envia a la aerolinea junto con la URL de la agencia, recibe el token de

      - proveedorID: identificador del proveedor aerolinea con quien hacer

      - string: token de salida recibido del proveedor aerolinea
        token, falla la comunicacion con la aerolinea o falla el guardado en BD

type LoginService struct {
    LoginService

    Servicio encargado de gestionar la autenticacion de usuarios en la agencia
    de viajes. Valida las credenciales ingresadas contra la base de datos y
    retorna la informacion del usuario autenticado para generar la sesion.

func NewLoginService(db *sql.DB) *LoginService
    NewLoginService

    Crea e inicializa una nueva instancia de LoginService con su repositorio de
    login.


      - *LoginService: instancia lista para usar

func (s *LoginService) Login(req dto.LoginRequest) (dto.LoginResponse, error)
    Login

    Autentica a un usuario verificando sus credenciales contra la base de datos.
    Busca al usuario por username o correo electronico y verifica la contrasena
    usando hashing seguro. Si las credenciales son invalidas retorna un error
    generico para evitar revelar si el usuario existe o no.

      - req: datos de login con campo Login (username o correo) y Contrasena

      - dto.LoginResponse: datos del usuario autenticado (ID, nombre, apellido,
        correo, username, rol)
      - error: ErrCredencialesInvalidas si el usuario no existe o la contrasena
        es incorrecta

type MisReservacionesService struct {
    MisReservacionesService

    Servicio encargado de consultar y presentar las reservaciones de un usuario.
    Ofrece dos modos de consulta: un listado resumido con datos locales
    unicamente, y un detalle completo que enriquece cada reservacion consultando
    en tiempo real a los proveedores externos involucrados (aerolineas y
    hoteleras).

func NewMisReservacionesService(repo *repositories.MisReservacionesRepository) *MisReservacionesService
    NewMisReservacionesService

    Crea e inicializa una nueva instancia de MisReservacionesService con el
    repositorio de reservaciones proporcionado.

      - repo: repositorio de mis reservaciones ya inicializado

      - *MisReservacionesService: instancia lista para usar

func (s *MisReservacionesService) ListarReservaciones(usuarioID int) ([]dto.ReservacionResumenResponse, error)
    ListarReservaciones

    Retorna el listado resumido de todas las reservaciones del usuario con sus
    detalles basicos. Agrupa las filas retornadas por la BD en reservaciones,
    organizando los detalles de cada una en orden de insercion.

      - usuarioID: identificador del usuario cuyas reservaciones se desean
        listar

      - []dto.ReservacionResumenResponse: lista de reservaciones con detalles
        resumidos
      - error: si falla la consulta de reservaciones en BD

func (s *MisReservacionesService) ObtenerDetalle(reservacionID, usuarioID int) (*dto.ReservacionDetalladaResponse, error)
    ObtenerDetalle

    Retorna el detalle completo de una reservacion especifica del usuario,
    enriqueciendo cada detalle con la informacion en tiempo real obtenida del
    proveedor externo correspondiente. Si la consulta a un proveedor falla,
    el campo DataProveedor incluye el mensaje de error en lugar de los datos.

      - reservacionID: identificador de la reservacion a consultar

      - *dto.ReservacionDetalladaResponse: reservacion con detalles completos y
        datos de proveedores
      - error: si la reservacion no existe o no pertenece al usuario

type PagoService struct {
    PagoService

    Servicio encargado de gestionar el procesamiento de pagos de reservaciones,
    incluyendo validacion de tarjetas, verificacion de integridad de detalles y
    notificacion a proveedores externos.

func NewPagoService(repo *repositories.PagoRepository, rr *repositories.ReservacionRepository) *PagoService
    NewPagoService

    Crea e inicializa una nueva instancia de PagoService con sus dependencias.

      - repo: repositorio de pagos para operaciones en base de datos
      - rr: repositorio de reservaciones para consultar detalles

      - *PagoService: instancia inicializada del servicio de pagos

func (s *PagoService) ProcesarPago(usuarioID int, req dto.PagoReservacionRequest) error
    ProcesarPago

    Ejecuta el flujo completo de pago de una reservacion: valida los datos de
    la tarjeta, verifica que la reserva pertenezca al usuario y este pendiente,
    valida la integridad de los detalles segun el tipo de reserva, notifica a
    cada proveedor externo y finalmente confirma la reserva en la base de datos.

      - usuarioID: identificador del usuario que realiza el pago
      - req: datos del pago incluyendo numero de tarjeta, CVV, NIT y codigo
        postal

      - error: error si la tarjeta es invalida, la reserva no existe o ya fue
        pagada, si los detalles no cumplen la estructura del tipo de reserva,
        o si algun proveedor rechaza el pago

type PdfReservacionService struct {
    PdfReservacionService

    Servicio encargado de generar el PDF de una reservacion combinando los datos
    obtenidos desde los proveedores externos con la informacion del usuario
    almacenada en la base de datos propia.

func NewPdfReservacionService(
) *PdfReservacionService
    NewPdfReservacionService

    Crea e inicializa una nueva instancia de PdfReservacionService con sus

      - usuRepo: repositorio de usuarios para consultar nombre y correo cuando
        no los provee la aerolinea

      - *PdfReservacionService: instancia inicializada del servicio de
        generacion de PDF

func (s *PdfReservacionService) GenerarPDF(reservacionID, usuarioID int) ([]byte, error)
    GenerarPDF

    Obtiene el detalle completo de una reservacion desde los proveedores,
    lo mapea a la estructura de datos del PDF, completa el nombre y correo del
    usuario desde la base de datos si no fueron provistos por el proveedor (caso
    reservas de hotel puro), y genera los bytes del archivo PDF.

      - reservacionID: identificador de la reservacion a convertir en PDF

      - []byte: bytes del PDF generado
      - error: error si la reservacion no existe, falla la serializacion,
        el mapeo de datos o la generacion del PDF

type PerfilService struct {
    PerfilService

    Servicio encargado de gestionar las operaciones sobre el perfil del usuario,
    incluyendo la consulta de datos, actualizacion de telefono y cambio de
    contrasena.

func NewPerfilService(db *sql.DB) *PerfilService
    NewPerfilService

    Crea e inicializa una nueva instancia de PerfilService con su repositorio.


      - *PerfilService: instancia inicializada del servicio de perfil

func (s *PerfilService) ActualizarTelefono(usuarioID int, telefono string) error
    ActualizarTelefono

    Actualiza el numero de telefono del usuario identificado por su ID.

      - usuarioID: identificador del usuario a actualizar
      - telefono: nuevo numero de telefono a asignar

      - error: error si falla la actualizacion en la base de datos

func (s *PerfilService) CambiarContrasena(usuarioID int, nueva string) error
    CambiarContrasena

    Genera el hash bcrypt de la nueva contrasena y lo persiste en la base de
    datos para el usuario indicado.

      - usuarioID: identificador del usuario que cambia su contrasena
      - nueva: nueva contrasena en texto plano que sera hasheada antes de
        guardarse

      - error: error si falla la generacion del hash o la actualizacion en la
        base de datos

func (s *PerfilService) ObtenerHash(usuarioID int) (string, error)
    ObtenerHash

    Obtiene el hash de la contrasena actual del usuario para verificacion previa
    antes de permitir el cambio de contrasena.

      - usuarioID: identificador del usuario cuyo hash se desea obtener

      - string: hash bcrypt de la contrasena actual del usuario
      - error: error si el usuario no existe o falla la consulta a la base de

func (s *PerfilService) ObtenerPerfil(usuarioID int) (map[string]interface{}, error)
    ObtenerPerfil

    Recupera los datos del perfil del usuario identificado por su ID.

      - usuarioID: identificador del usuario cuyo perfil se desea obtener

      - map[string]interface{}: mapa con los campos del perfil del usuario

type ProveedorService struct {
    ProveedorService

    Servicio encargado de la logica de negocio relacionada con la creacion y
    gestion de proveedores externos registrados en el sistema.

func NewProveedorService(db *sql.DB) *ProveedorService
    NewProveedorService

    Crea e inicializa una nueva instancia de ProveedorService con su
    repositorio.


      - *ProveedorService: instancia inicializada del servicio de proveedores

func (s *ProveedorService) CrearProveedor(req dto.CrearProveedorRequest) (dto.CrearProveedorResponse, error)
    CrearProveedor

    Ejecuta las validaciones de negocio necesarias antes de registrar un nuevo
    proveedor: verifica que el usuario exista y tenga rol webservice (rol 3),
    que dicho usuario no tenga ya un proveedor asignado, y que el tipo de
    proveedor indicado sea valido. Si todas las validaciones pasan, delega la
    creacion al repositorio.

      - req: datos del proveedor a crear, incluyendo el ID de usuario y tipo de

      - dto.CrearProveedorResponse: datos del proveedor creado incluyendo token
        generado
      - error: error si el usuario no existe, no tiene el rol correcto, ya tiene
        un proveedor asignado o el tipo de proveedor no existe

type ReservacionService struct {
    ReservacionService

    Servicio responsable de la creacion de nuevas reservaciones de viaje.
    Coordina la expiracion de reservaciones pendientes anteriores del usuario y
    la generacion de numeros de reservacion unicos con fecha de expiracion.

func NewReservacionService(db *sql.DB, expiracionService *ExpiracionService) *ReservacionService
    NewReservacionService

    Crea e inicializa una nueva instancia de ReservacionService con sus

      - expiracionService: servicio que gestiona la expiracion de reservaciones
        pendientes

      - *ReservacionService: instancia inicializada del servicio de
        reservaciones

func (s *ReservacionService) CrearReservacion(usuarioID, tipoReservaID int) (dto.CrearReservacionResponse, error)
    CrearReservacion

    Crea una nueva reservacion para el usuario especificado. Primero expira
    cualquier reservacion pendiente anterior del mismo usuario, luego genera
    un numero de reservacion unico de 8 caracteres en mayusculas y calcula una
    fecha de expiracion de 10 minutos desde el momento de la creacion.

      - usuarioID: identificador del usuario que crea la reservacion
      - tipoReservaID: tipo de reserva (1=Aerolinea, 2=Hotel, 3=Paquete)

      - dto.CrearReservacionResponse: datos de la reservacion creada incluyendo
        numero, estado y fechas
      - error: error si falla la expiracion de reservaciones anteriores o la
        creacion en base de datos

type UbicacionResult struct {
	Pais   models.Pais   `json:"pais"`
	Ciudad models.Ciudad `json:"ciudad"`
    UbicacionResult

    Estructura que agrupa el resultado de una busqueda o creacion de ubicacion,
    conteniendo el pais y la ciudad resueltos.

type UbicacionService struct {
    UbicacionService

    Servicio encargado de resolver ubicaciones geograficas (paises, ciudades y
    nacionalidades), creandolas en la base de datos si no existen previamente.

func NewUbicacionService(db *sql.DB) *UbicacionService
    NewUbicacionService

    Crea e inicializa una nueva instancia de UbicacionService con su


      - *UbicacionService: instancia inicializada del servicio de ubicaciones

func (s *UbicacionService) ObtenerOCrearNacionalidades(nombres []string) ([]models.Nacionalidad, error)
    ObtenerOCrearNacionalidades

    Procesa un slice de nombres de nacionalidades, buscando cada una en la
    base de datos y creandola si no existe. Ignora entradas vacias o que solo
    contengan espacios. Recibe un slice porque el usuario puede tener multiples
    nacionalidades.

      - nombres: slice de nombres de nacionalidades a resolver

      - []models.Nacionalidad: slice con las nacionalidades resueltas
      - error: error si falla la consulta o insercion de alguna nacionalidad en

func (s *UbicacionService) ObtenerOCrearUbicacion(nombreCiudad, nombrePais string) (UbicacionResult, error)
    ObtenerOCrearUbicacion

    Busca en la base de datos el pais y la ciudad indicados por nombre.
    Si alguno no existe, lo crea automaticamente. Limpia espacios en blanco de
    los nombres antes de procesarlos.

      - nombreCiudad: nombre de la ciudad a buscar o crear
      - nombrePais: nombre del pais al que pertenece la ciudad

      - UbicacionResult: struct con el pais y la ciudad resueltos
      - error: error si falla la consulta o insercion en base de datos

type UsuarioService struct {
    UsuarioService

    Servicio encargado de la logica de negocio para el registro y validacion de
    usuarios, incluyendo la resolucion de ubicaciones y nacionalidades mediante
    el servicio de ubicacion.

func NewUsuarioService(db *sql.DB, ubicacionService *UbicacionService) *UsuarioService
    NewUsuarioService

    Crea e inicializa una nueva instancia de UsuarioService con sus

      - ubicacionService: servicio de ubicaciones para resolver pais, ciudad y
        nacionalidades

      - *UsuarioService: instancia inicializada del servicio de usuarios

func (s *UsuarioService) Registrar(req dto.RegistroUsuarioRequest) (dto.ValidacionUsuarioResponse, error)
    Registrar

    Ejecuta el flujo completo de registro de un nuevo usuario: valida que
    correo, pasaporte y username no esten duplicados, resuelve la ubicacion
    geografica (pais y ciudad), resuelve las nacionalidades, crea el usuario
    en la base de datos con estado y rol por defecto (1), y le asigna sus
    nacionalidades. Si hay duplicados en los datos unicos, retorna la validacion
    sin registrar al usuario.

      - req: datos completos del usuario a registrar

      - dto.ValidacionUsuarioResponse: resultado de la validacion de datos
        unicos
      - error: error si falla la validacion, la resolucion de ubicacion o la
        insercion en base de datos

func (s *UsuarioService) ValidarDatosUnicos(req dto.RegistroUsuarioRequest) (dto.ValidacionUsuarioResponse, error)
    ValidarDatosUnicos

    Verifica si el correo, pasaporte o username del request ya existen en la
    base de datos. Devuelve un struct de validacion indicando cuales campos ya
    estan en uso.

      - req: datos del usuario a registrar, incluyendo correo, pasaporte y
        username

      - dto.ValidacionUsuarioResponse: flags indicando que campos ya existen en
        la base de datos
      - error: error si falla alguna consulta a la base de datos

```
