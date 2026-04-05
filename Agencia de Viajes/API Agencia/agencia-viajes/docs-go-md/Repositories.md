# Repositories


# Package repositories

Repositorios de acceso a datos para la agencia de viajes. Este paquete
centraliza todas las consultas a la base de datos utilizadas por los servicios
de la aplicacion.
























## TYPES

```go

type BusquedaRepository struct {
	// Has unexported fields.
}
    BusquedaRepository

    Repositorio encargado de las consultas de busqueda de ciudades y proveedores
    disponibles segun origen, destino y tipo de servicio.

func NewBusquedaRepository(db *sql.DB) *BusquedaRepository
    NewBusquedaRepository

    Crea e inicializa una nueva instancia de BusquedaRepository.

    Parametros:
      - db: conexion activa a la base de datos

    Retorna:
      - *BusquedaRepository: instancia lista para usar

func (r *BusquedaRepository) BuscarCiudadID(ciudad, pais string) (*int, error)
    BuscarCiudadID

    Consulta el ID de una ciudad a partir de su nombre y el nombre del pais al
    que pertenece. La comparacion se realiza ignorando mayusculas y espacios.

      - ciudad: nombre de la ciudad a buscar
      - pais: nombre del pais al que pertenece la ciudad

      - *int: puntero al ID de la ciudad encontrada, nil si no existe
      - error: error de base de datos, nil si la operacion fue exitosa

func (r *BusquedaRepository) ObtenerAerolineasPorRuta(
	ciudadOrigenID, ciudadDestinoID int,
) ([]dto.ProveedorCatalogo, error)
    ObtenerAerolineasPorRuta

    Consulta los proveedores de tipo aerolinea (Tipo_Catalogo_ID = 1) activos
    que operan desde una ciudad de origen determinada.

      - ciudadOrigenID: ID de la ciudad de origen del vuelo
      - ciudadDestinoID: ID de la ciudad de destino (recibido pero no usado en
        el filtro SQL)

      - []dto.ProveedorCatalogo: lista de aerolineas disponibles para la ruta

    Notas:
      - El filtro por ciudad de destino no se aplica en la consulta actual

func (r *BusquedaRepository) ObtenerProveedoresPorOrigenYTipo(
	ciudadOrigenID, tipoCatalogoID int,
    ObtenerProveedoresPorOrigenYTipo

    Consulta los proveedores activos que operan desde una ciudad de origen dada
    y que pertenecen a un tipo de catalogo especifico (por ejemplo, hotelero o
    aereo).

      - ciudadOrigenID: ID de la ciudad de origen
      - tipoCatalogoID: ID del tipo de catalogo (1=aerolinea, 2=hotelera)

      - []dto.ProveedorCatalogo: lista de proveedores que cumplen el criterio

type CancelacionRepository struct {
    CancelacionRepository

    Repositorio que gestiona las operaciones de cancelacion de reservaciones,
    incluyendo la verificacion de pertenencia al usuario y la actualizacion del
    estado en base de datos mediante transacciones.

func NewCancelacionRepository(db *sql.DB) *CancelacionRepository
    NewCancelacionRepository

    Crea e inicializa una nueva instancia de CancelacionRepository.


      - *CancelacionRepository: instancia lista para usar

func (r *CancelacionRepository) CancelarReservacion(reservacionID int, motivo string) error
    CancelarReservacion

    Marca la reservacion y todos sus detalles activos como cancelados (estado 3)
    dentro de una transaccion atomica. Ademas registra la fecha de cancelacion y
    el motivo proporcionado.

      - reservacionID: ID de la reservacion a cancelar
      - motivo: descripcion del motivo de cancelacion

      - error: error si alguna operacion de la transaccion falla, nil si fue
        exitosa

      - Si cualquier paso falla, se realiza rollback automatico

func (r *CancelacionRepository) ObtenerDetallesParaCancelar(reservacionID int) ([]dto.DetalleProveedor, error)
    ObtenerDetallesParaCancelar

    Recupera los detalles de una reservacion junto con los datos del proveedor
    asociado a cada detalle. Solo incluye detalles con estado pendiente (1) o
    confirmado (2).

      - reservacionID: ID de la reservacion cuyos detalles se desean obtener

      - []dto.DetalleProveedor: lista de detalles con informacion del proveedor

func (r *CancelacionRepository) ObtenerReservacionParaCancelar(reservacionID, usuarioID int) (estadoID int, err error)
    ObtenerReservacionParaCancelar

    Verifica que una reservacion exista, pertenezca al usuario indicado y
    retorna su estado actual para validar si es cancelable.

      - reservacionID: ID de la reservacion a consultar
      - usuarioID: ID del usuario que solicita la cancelacion

      - estadoID: identificador del estado actual de la reservacion
      - error: error si la reservacion no existe, no pertenece al usuario o
        falla la consulta

type CatalogoRepository struct {
    CatalogoRepository

    Repositorio encargado de las operaciones sobre el catalogo de proveedores,
    incluyendo consulta de tipos, datos de conexion, sincronizacion y
    actualizacion de rutas disponibles por proveedor.

func NewCatalogoRepository(db *sql.DB) *CatalogoRepository
    NewCatalogoRepository

    Crea e inicializa una nueva instancia de CatalogoRepository.


      - *CatalogoRepository: instancia lista para usar

func (r *CatalogoRepository) EliminarCatalogoPorProveedor(proveedorID int) error
    EliminarCatalogoPorProveedor

    Elimina todas las entradas del catalogo asociadas a un proveedor especifico.
    Utilizado antes de sincronizar el catalogo con datos actualizados del
    webservice.

      - proveedorID: ID del proveedor cuyo catalogo se desea eliminar


func (r *CatalogoRepository) InsertarEntrada(
	ciudadOrigenID int,
	ciudadDestinoID *int,
	tipoCatalogoID int,
	proveedorID int,
) error
    InsertarEntrada

    Registra una nueva entrada en el catalogo del proveedor, asociando una
    ciudad de origen, opcionalmente una ciudad de destino, el tipo de catalogo y
    el proveedor.

      - ciudadOrigenID: ID de la ciudad de origen del servicio
      - ciudadDestinoID: puntero al ID de la ciudad de destino; puede ser nil
        para servicios hoteleros
      - proveedorID: ID del proveedor al que pertenece esta entrada


      - Si ciudadDestinoID es nil, se inserta NULL en la columna
        Ciudad_Destino_ID

func (r *CatalogoRepository) ObtenerDatosConexion(proveedorID int) (urlAPI, tokenEntrada string, err error)
    ObtenerDatosConexion

    Recupera la URL de la API y el token de entrada del proveedor especificado,
    necesarios para realizar llamadas al webservice externo.

      - proveedorID: ID del proveedor a consultar

      - urlAPI: URL base del API del proveedor
      - tokenEntrada: token de autenticacion para las peticiones al proveedor

func (r *CatalogoRepository) ObtenerProveedoresActivos() ([]int, error)
    ObtenerProveedoresActivos

    Recupera los IDs de todos los proveedores cuyo estado es activo (EstadoID =
    1).

      - (ninguno)

      - []int: lista de IDs de proveedores activos

func (r *CatalogoRepository) ObtenerTipoProveedor(proveedorID int) (int, error)
    ObtenerTipoProveedor

    Consulta el tipo de proveedor asociado a un ID dado. El tipo determina la
    categoria del servicio (1=aerolinea, 2=hotelera).


      - int: ID del tipo de proveedor

type DetalleReservacionRepository struct {
    DetalleReservacionRepository

    Repositorio que gestiona las operaciones sobre los detalles de reservacion,
    incluyendo la insercion de nuevos detalles, consulta de datos de proveedor,
    actualizacion de totales y validacion de vuelos pendientes.

func NewDetalleReservacionRepository(db *sql.DB) *DetalleReservacionRepository
    NewDetalleReservacionRepository

    Crea e inicializa una nueva instancia de DetalleReservacionRepository.


      - *DetalleReservacionRepository: instancia lista para usar

func (r *DetalleReservacionRepository) ActualizarTotalReservacion(reservacionID int, montoAgregar float64) error
    ActualizarTotalReservacion

    Incrementa el total acumulado de una reservacion sumando el monto indicado.
    Se utiliza cada vez que se agrega un nuevo detalle a la reservacion.

      - reservacionID: ID de la reservacion a actualizar
      - montoAgregar: monto que se suma al total existente


func (r *DetalleReservacionRepository) InsertarDetalle(
	reservacionID, proveedorID, tipoDetalleID int,
	idReservaProveedor string,
	total float64,
	parametrosJson interface{},
    InsertarDetalle

    Inserta un nuevo registro en la tabla Detalles_Reservacion con estado
    pendiente (1). Los parametros adicionales de la reserva se serializan a JSON
    antes de persistirse.

      - reservacionID: ID de la reservacion padre
      - proveedorID: ID del proveedor que gestiona este detalle
      - tipoDetalleID: tipo de servicio reservado (1=vuelo, 2=hotel)
      - idReservaProveedor: identificador de la reserva en el sistema del
        proveedor externo
      - total: monto total del detalle con el margen de ganancia aplicado
      - parametrosJson: estructura con los parametros de la reserva a serializar

      - error: error de serializacion o base de datos, nil si la operacion fue

func (r *DetalleReservacionRepository) ObtenerDatosProveedor(proveedorID int) (urlAPI, tokenEntrada string, porcentajeGanancia float64, err error)
    ObtenerDatosProveedor

    Recupera la URL del API, el token de entrada y el porcentaje de ganancia
    configurado para un proveedor especifico.


      - tokenEntrada: token de autenticacion para peticiones al proveedor
      - porcentajeGanancia: margen de ganancia configurado para el proveedor

func (r *DetalleReservacionRepository) ObtenerDatosProveedorPorTipo(
	proveedorID, tipoDetalleID int,
) (urlAPI, tokenEntrada string, porcentajeGanancia float64, err error)
    ObtenerDatosProveedorPorTipo

    Recupera los datos de conexion de un proveedor validando ademas que coincida
    con el tipo de detalle solicitado (1=aerolinea, 2=hotelera).

      - tipoDetalleID: ID del tipo de proveedor esperado

      - porcentajeGanancia: margen de ganancia configurado
      - error: error si el proveedor no existe o no corresponde al tipo,
        nil si fue exitosa

func (r *DetalleReservacionRepository) ObtenerDetalleAerolineaPorProveedor(
	reservacionID, usuarioID, proveedorID int,
) (idReservaProveedor string, urlAPI string, tokenEntrada string, err error)
    ObtenerDetalleAerolineaPorProveedor

    Busca el identificador de reserva en el proveedor aerolinea para un detalle
    de tipo vuelo (Tipo_Detalle_ID = 1) que este pendiente, validando que la
    reservacion pertenezca al usuario y este en estado activo.

      - usuarioID: ID del usuario propietario de la reservacion
      - proveedorID: ID del proveedor aerolinea involucrado

        proveedor
      - tokenEntrada: token de autenticacion del proveedor
      - error: error si no se encuentra el detalle o la reservacion no pertenece
        al usuario

func (r *DetalleReservacionRepository) ObtenerReservacionParaDetalle(reservacionID, usuarioID int) (*dto.ReservacionValidada, error)
    ObtenerReservacionParaDetalle

    Verifica que una reservacion exista y pertenezca al usuario indicado,
    retornando sus datos basicos para validacion previa a la insercion de
    detalles.


      - *dto.ReservacionValidada: datos basicos de la reservacion, nil si no
        existe

func (r *DetalleReservacionRepository) RecalcularTotalReservacion(reservacionID int) error
    RecalcularTotalReservacion

    Recalcula y actualiza el total de una reservacion sumando el total de todos
    sus detalles que se encuentren en estado pendiente (Estado_Detalle_ID = 1).

      - reservacionID: ID de la reservacion a recalcular


      - Utiliza COALESCE para retornar 0 si no hay detalles activos

type LoginRepository struct {
    LoginRepository

    Repositorio encargado de las consultas necesarias para el proceso de
    autenticacion de usuarios, permitiendo buscar por nombre de usuario o por
    correo electronico.

func NewLoginRepository(db *sql.DB) *LoginRepository
    NewLoginRepository

    Crea e inicializa una nueva instancia de LoginRepository.


      - *LoginRepository: instancia lista para usar

func (r *LoginRepository) ObtenerPorUsernameOCorreo(login string) (models.Usuario, error)
    ObtenerPorUsernameOCorreo

    Busca un usuario en la base de datos comparando el valor recibido contra el
    campo Username y el campo Correo. Retorna el modelo completo del usuario si
    se encuentra una coincidencia, o un modelo vacio si no existe.

      - login: valor a buscar, puede ser el username o el correo del usuario

      - models.Usuario: datos completos del usuario encontrado, vacio si no

      - Si no se encuentra el usuario se retorna un struct vacio sin error

type MisReservacionesRepository struct {
    MisReservacionesRepository

    Repositorio que gestiona la consulta del historial de reservaciones de un
    usuario, retornando tanto los datos de la reservacion como sus detalles y la
    informacion del proveedor asociado a cada uno.

func NewMisReservacionesRepository(db *sql.DB) *MisReservacionesRepository
    NewMisReservacionesRepository

    Crea e inicializa una nueva instancia de MisReservacionesRepository.


      - *MisReservacionesRepository: instancia lista para usar

func (r *MisReservacionesRepository) ObtenerReservacionPorID(reservacionID, usuarioID int) ([]dto.FilaReservacionDetalle, error)
    ObtenerReservacionPorID

    Recupera una reservacion especifica junto con todos sus detalles y los
    datos del proveedor, verificando que la reservacion pertenezca al usuario
    indicado.


      - []dto.FilaReservacionDetalle: filas planas con datos de reservacion,
        detalle y proveedor

func (r *MisReservacionesRepository) ObtenerReservacionesDeUsuario(usuarioID int) ([]dto.FilaReservacionDetalle, error)
    ObtenerReservacionesDeUsuario

    Recupera todas las reservaciones de un usuario junto con sus detalles y los
    datos del proveedor de cada detalle. Los resultados se ordenan por fecha de
    creacion descendente.

      - usuarioID: ID del usuario cuyas reservaciones se desean consultar


type PagoRepository struct {
    PagoRepository

    Repositorio encargado de las operaciones relacionadas con el proceso de
    pago, incluyendo la validacion de la reserva, el conteo de detalles por tipo
    y la confirmacion atomica de la reserva junto con la creacion de la factura.

func NewPagoRepository(db *sql.DB) *PagoRepository
    NewPagoRepository

    Crea e inicializa una nueva instancia de PagoRepository.


      - *PagoRepository: instancia lista para usar

func (r *PagoRepository) ConfirmarReservaYFacturar(reservacionID int, total float64, nit string, codigoPostal string) error
    ConfirmarReservaYFacturar

    Ejecuta dentro de una transaccion atomica los tres pasos del proceso de
    confirmacion: cambia el estado de la reservacion a confirmada (2), cambia el
    estado de todos sus detalles a confirmados (2) y crea el registro de factura
    asociado.

      - reservacionID: ID de la reservacion a confirmar
      - total: monto total a registrar en la factura
      - nit: numero de identificacion tributaria del cliente para la factura
      - codigoPostal: codigo postal del cliente para la factura


      - Si cualquier paso falla, se realiza rollback automatico de toda la
        transaccion

func (r *PagoRepository) ContarDetallesPorTipo(reservacionID int) (vuelos int, hoteles int, err error)
    ContarDetallesPorTipo

    Cuenta cuantos detalles de tipo vuelo (1) y tipo hotel (2) tiene una
    reservacion, considerando unicamente los detalles en estado pendiente
    (Estado_Detalle_ID = 1).

      - reservacionID: ID de la reservacion a evaluar

      - vuelos: cantidad de detalles de tipo vuelo pendientes
      - hoteles: cantidad de detalles de tipo hotel pendientes

func (r *PagoRepository) ObtenerReservaParaPago(reservacionID, usuarioID int) (tipoReserva int, total float64, err error)
    ObtenerReservaParaPago

    Verifica que la reserva pertenezca al usuario indicado y se encuentre en
    estado pendiente (EstadoID = 1), retornando el tipo de reserva y el total.


      - tipoReserva: ID del tipo de reserva (vuelo, hotel, paquete, etc.)
      - total: monto total acumulado de la reservacion
      - error: error si la reserva no existe, no pertenece al usuario o no esta
        pendiente

type PerfilRepository struct {
    PerfilRepository

    Repositorio encargado de las operaciones de consulta y actualizacion del
    perfil del usuario autenticado, incluyendo datos personales, ubicacion,
    nacionalidades y gestion de contrasena.

func NewPerfilRepository(db *sql.DB) *PerfilRepository
    NewPerfilRepository

    Crea e inicializa una nueva instancia de PerfilRepository.


      - *PerfilRepository: instancia lista para usar

func (r *PerfilRepository) ActualizarContrasena(usuarioID int, hash string) error
    ActualizarContrasena

    Actualiza el hash de la contrasena del usuario en la base de datos. Debe
    recibir el hash ya procesado, no la contrasena en texto plano.

      - usuarioID: ID del usuario cuya contrasena se desea actualizar
      - hash: nuevo hash bcrypt de la contrasena a persistir


func (r *PerfilRepository) ActualizarTelefono(usuarioID int, telefono string) error
    ActualizarTelefono

    Actualiza el numero de telefono del usuario identificado por su ID.

      - usuarioID: ID del usuario a actualizar
      - telefono: nuevo numero de telefono a registrar


func (r *PerfilRepository) ObtenerHash(usuarioID int) (string, error)
    ObtenerHash

    Recupera el hash de la contrasena actual del usuario, utilizado para validar
    la contrasena anterior antes de permitir un cambio.

      - usuarioID: ID del usuario cuyo hash se desea obtener

      - string: hash bcrypt de la contrasena actual

func (r *PerfilRepository) ObtenerPerfil(usuarioID int) (map[string]interface{}, error)
    ObtenerPerfil

    Recupera todos los datos del perfil del usuario incluyendo nombre, apellido,
    correo, username, pasaporte, telefono, fecha de nacimiento, ciudad, pais y
    la lista de nacionalidades asociadas.

      - usuarioID: ID del usuario cuyo perfil se desea consultar

      - map[string]interface{}: mapa con todos los campos del perfil del usuario

      - Ciudad y pais pueden ser NULL si el usuario no tiene ciudad asignada
      - La lista de nacionalidades puede estar vacia si no se asignaron al
        registrarse

type ProveedorRepository struct {
    ProveedorRepository

    Repositorio encargado de las operaciones sobre la entidad Proveedor,
    incluyendo creacion, validacion de roles y existencia, almacenamiento de
    tokens y consulta de datos de conexion por distintos criterios.

func NewProveedorRepository(db *sql.DB) *ProveedorRepository
    NewProveedorRepository

    Crea e inicializa una nueva instancia de ProveedorRepository.


      - *ProveedorRepository: instancia lista para usar

func (r *ProveedorRepository) CrearProveedor(req dto.CrearProveedorRequest) (dto.CrearProveedorResponse, error)
    CrearProveedor

    Inserta un nuevo proveedor en la base de datos con estado activo (EstadoID =
    1) y tokens vacios que seran generados y guardados posteriormente.

      - req: DTO con los datos necesarios para crear el proveedor

      - dto.CrearProveedorResponse: datos del proveedor recien creado incluyendo
        su ID

func (r *ProveedorRepository) ExisteTipoProveedor(tipoID int) (bool, error)
    ExisteTipoProveedor

    Verifica si existe un registro en la tabla Tipo_Proveedor con el ID

      - tipoID: ID del tipo de proveedor a validar

      - bool: true si el tipo existe, false en caso contrario

func (r *ProveedorRepository) GuardarTokens(proveedorID int, tokenEntrada, tokenSalida string) error
    GuardarTokens

    Actualiza los tokens de entrada y salida de un proveedor existente.
    Se utiliza luego de la creacion del proveedor para persistir los hashes
    generados.

      - proveedorID: ID del proveedor al que se asignan los tokens
      - tokenEntrada: hash del token de entrada para autenticar peticiones
        entrantes
      - tokenSalida: hash del token de salida para autenticar peticiones
        salientes


func (r *ProveedorRepository) ObtenerProveedorPorID(proveedorID int) (*dto.DetalleProveedor, error)
    ObtenerProveedorPorID

    Recupera la URL del API y el token de entrada de un proveedor especifico
    verificando que se encuentre en estado activo (EstadoID = 1).


      - *dto.DetalleProveedor: datos de conexion del proveedor encontrado
      - error: error si el proveedor no existe o esta inactivo, nil si fue

func (r *ProveedorRepository) ObtenerProveedorPorTipo(tipoProveedorID int) (*dto.DetalleProveedor, error)
    ObtenerProveedorPorTipo

    Recupera la URL del API y el token de entrada del primer proveedor activo
    que corresponda al tipo indicado. Retorna error si no hay proveedores
    activos de ese tipo.

      - tipoProveedorID: ID del tipo de proveedor a buscar (1=aerolinea,
        2=hotelera)

      - error: error si no hay proveedor activo del tipo indicado o falla la
        consulta

func (r *ProveedorRepository) ObtenerProveedorPorToken(token string) (*dto.ProveedorIdentidad, error)
    ObtenerProveedorPorToken

    Busca un proveedor utilizando su token de entrada (Token_HASH_Entrada).
    Retorna nil si no se encuentra ningun proveedor con ese token.

      - token: hash del token de entrada a buscar

      - *dto.ProveedorIdentidad: datos de identidad del proveedor encontrado,
        nil si no existe

func (r *ProveedorRepository) ObtenerRolUsuario(usuarioID int) (int, error)
    ObtenerRolUsuario

    Consulta el RolID asignado a un usuario especifico. Retorna 0 si el usuario
    no existe en la base de datos.

      - usuarioID: ID del usuario a consultar

      - int: ID del rol del usuario, 0 si no existe

func (r *ProveedorRepository) ObtenerURLAPI(proveedorID int) (string, error)
    ObtenerURLAPI

    Recupera la URL del API de un proveedor especifico para realizar llamadas al
    webservice externo.


      - string: URL del API del proveedor

func (r *ProveedorRepository) UsuarioYaTieneProveedor(usuarioID int) (bool, error)
    UsuarioYaTieneProveedor

    Verifica si un usuario webservice ya tiene un proveedor registrado y
    asociado a su cuenta, para evitar duplicidad.

      - usuarioID: ID del usuario a verificar

      - bool: true si ya existe un proveedor asociado, false en caso contrario

type ReservacionRepository struct {
    ReservacionRepository

    Repositorio que gestiona el ciclo de vida de las reservaciones en la base
    de datos, incluyendo creacion, consulta de pendientes, expiracion masiva e
    individual, y recuperacion de detalles asociados a cada reservacion.

func NewReservacionRepository(db *sql.DB) *ReservacionRepository
    NewReservacionRepository

    Crea e inicializa una nueva instancia de ReservacionRepository.


      - *ReservacionRepository: instancia lista para usar

func (r *ReservacionRepository) CrearReservacion(
	usuarioID int,
	tipoReservaID int,
	noReservacion string,
	fechaExpiracion string,
) (int, error)
    CrearReservacion

    Inserta una nueva reservacion en la base de datos con estado pendiente (1) y
    total inicial de cero.

      - usuarioID: ID del usuario que realiza la reservacion
      - tipoReservaID: tipo de reserva (vuelo, hotel, paquete, etc.)
      - noReservacion: numero unico de reservacion generado por el servicio
      - fechaExpiracion: fecha y hora limite para confirmar la reservacion

      - int: ID autogenerado de la nueva reservacion

func (r *ReservacionRepository) ExpirarDetalles(reservacionID int) error
    ExpirarDetalles

    Marca como cancelados (estado 3) todos los detalles de una reservacion,
    utilizado durante el proceso de expiracion de la reservacion padre.

      - reservacionID: ID de la reservacion cuyos detalles se deben cancelar


func (r *ReservacionRepository) ExpirarReservacion(reservacionID int) error
    ExpirarReservacion

    Marca una reservacion especifica como expirada (estado 4) siempre que
    actualmente se encuentre en estado pendiente (1).

      - reservacionID: ID de la reservacion a expirar


func (r *ReservacionRepository) ExpirarReservacionesPendientes() error
    ExpirarReservacionesPendientes

    Actualiza a estado expirado (4) todas las reservaciones que se encuentren en
    estado pendiente (1) y cuya fecha de expiracion ya haya pasado.



func (r *ReservacionRepository) ObtenerDetallesDeReservacion(reservacionID int) ([]dto.DetalleProveedor, error)
    ObtenerDetallesDeReservacion

    Recupera todos los detalles de una reservacion junto con los datos de
    conexion del proveedor asociado a cada detalle.



func (r *ReservacionRepository) ObtenerIDsPendientesExpirados() ([]int, error)
    ObtenerIDsPendientesExpirados

    Consulta los IDs de todas las reservaciones que esten en estado pendiente
    (1) y cuya fecha de expiracion ya haya pasado. Usado por el scheduler de
    expiracion.


      - []int: lista de IDs de reservaciones pendientes expiradas

func (r *ReservacionRepository) ObtenerPendientesConDetalles(usuarioID int) ([]dto.ReservacionConDetalles, error)
    ObtenerPendientesConDetalles

    Recupera todas las reservaciones pendientes de un usuario junto con los
    detalles y datos del proveedor asociados a cada una.

      - usuarioID: ID del usuario cuyas reservaciones pendientes se desean
        consultar

      - []dto.ReservacionConDetalles: lista de reservaciones con sus detalles de

type UbicacionRepository struct {
    UbicacionRepository

    Repositorio encargado de la gestion de entidades geograficas como paises,
    ciudades y nacionalidades. Implementa el patron buscar-o-crear para
    garantizar que no se dupliquen registros existentes en la base de datos.

func NewUbicacionRepository(db *sql.DB) *UbicacionRepository
    NewUbicacionRepository

    Crea e inicializa una nueva instancia de UbicacionRepository.


      - *UbicacionRepository: instancia lista para usar

func (r *UbicacionRepository) BuscarOCrearCiudad(nombre string, paisID int) (models.Ciudad, error)
    BuscarOCrearCiudad

    Busca una ciudad por nombre y pais en la base de datos. Si no existe,
    la inserta y retorna el registro recien creado con su ID autogenerado.

      - nombre: nombre de la ciudad a buscar o crear
      - paisID: ID del pais al que pertenece la ciudad

      - models.Ciudad: entidad de la ciudad encontrada o creada

func (r *UbicacionRepository) BuscarOCrearNacionalidad(nombre string) (models.Nacionalidad, error)
    BuscarOCrearNacionalidad

    Busca una nacionalidad por nombre en la base de datos. Si no existe,

      - nombre: nombre de la nacionalidad a buscar o crear

      - models.Nacionalidad: entidad de la nacionalidad encontrada o creada

func (r *UbicacionRepository) BuscarOCrearPais(nombre string) (models.Pais, error)
    BuscarOCrearPais

    Busca un pais por nombre en la base de datos. Si no existe, lo inserta y
    retorna el registro recien creado con su ID autogenerado.

      - nombre: nombre del pais a buscar o crear

      - models.Pais: entidad del pais encontrado o creado

type UsuarioRepository struct {
    UsuarioRepository

    Repositorio encargado de las operaciones sobre la entidad Usuario,
    incluyendo validacion de unicidad de campos, creacion de nuevos usuarios,
    asignacion de nacionalidades y consulta de datos de contacto.

func NewUsuarioRepository(db *sql.DB) *UsuarioRepository
    NewUsuarioRepository

    Crea e inicializa una nueva instancia de UsuarioRepository.


      - *UsuarioRepository: instancia lista para usar

func (r *UsuarioRepository) AsignarNacionalidades(usuarioID int, nacionalidadIDs []int) error
    AsignarNacionalidades

    Inserta las asociaciones entre un usuario y sus nacionalidades en la
    tabla UsuarioNacionalidad. Itera sobre la lista de IDs de nacionalidad
    proporcionada.

      - usuarioID: ID del usuario al que se le asignan las nacionalidades
      - nacionalidadIDs: lista de IDs de nacionalidades a asociar

      - error: error de base de datos si alguna insercion falla, nil si todas
        fueron exitosas

func (r *UsuarioRepository) CrearUsuario(req dto.RegistroUsuarioRequest, ciudadID, rolID, estadoID int) (int, error)
    CrearUsuario

    Inserta un nuevo usuario en la base de datos. La contrasena se hashea antes
    de persistirse utilizando el helper de seguridad.

      - req: DTO con los datos del formulario de registro del usuario
      - ciudadID: ID de la ciudad de residencia del usuario
      - rolID: ID del rol asignado al nuevo usuario
      - estadoID: ID del estado inicial del usuario (activo, pendiente, etc.)

      - int: ID autogenerado del usuario recien creado
      - error: error de hasheo o de base de datos, nil si la operacion fue

func (r *UsuarioRepository) ExisteCorreo(correo string) (bool, error)
    ExisteCorreo

    Verifica si ya existe un usuario registrado con el correo electronico

      - correo: correo electronico a verificar

      - bool: true si el correo ya esta en uso, false en caso contrario

func (r *UsuarioRepository) ExistePasaporte(pasaporte string) (bool, error)
    ExistePasaporte

    Verifica si ya existe un usuario registrado con el numero de pasaporte

      - pasaporte: numero de pasaporte a verificar

      - bool: true si el pasaporte ya esta en uso, false en caso contrario

func (r *UsuarioRepository) ExisteUsername(username string) (bool, error)
    ExisteUsername

    Verifica si ya existe un usuario registrado con el nombre de usuario

      - username: nombre de usuario a verificar

      - bool: true si el username ya esta en uso, false en caso contrario

func (r *UsuarioRepository) ObtenerNombreYEmail(usuarioID int) (nombre, email string, err error)
    ObtenerNombreYEmail

    Recupera el nombre completo y el correo electronico de un usuario por su ID.
    El nombre completo se construye concatenando Nombre y Apellido en Go para
    evitar problemas de concatenacion en distintos motores de base de datos.


      - nombre: nombre completo del usuario (Nombre + Apellido)
      - email: correo electronico del usuario

```
