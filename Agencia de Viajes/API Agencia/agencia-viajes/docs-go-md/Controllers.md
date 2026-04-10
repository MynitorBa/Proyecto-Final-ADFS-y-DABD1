# Controllers


# Package controllers

Controladores HTTP de la API de Movent. Cada controlador agrupa los handlers
relacionados a un recurso o dominio especifico de la aplicacion.


Controladores HTTP de la agencia de viajes. Cada controlador recibe solicitudes
de Gin, delega la logica de negocio al servicio correspondiente y devuelve la
respuesta JSON al cliente.








































## FUNCTIONS

```go

func EnviarContacto(c *gin.Context)
    EnviarContacto

    Procesa el formulario de contacto enviado desde el sitio web. Valida que el
    mensaje tenga al menos 10 caracteres, construye un correo HTML con los datos
    del remitente y lo envia a la bandeja de entrada de soporte de MOVENT.

    Parametros:
      - c: contexto de Gin con la solicitud HTTP

    Retorna:
      - HTTP 200 OK: JSON con mensaje de confirmacion al visitante
      - HTTP 400 Bad Request: si faltan campos requeridos o el mensaje es muy
        corto
      - HTTP 500 Internal Server Error: si ocurre un error al enviar el correo

    Notas:
      - Si el asunto llega vacio se usa el valor por defecto "Consulta desde el
        sitio web"
      - El correo se envia a la cuenta configurada en el SMTP (cfg.User)

```

## TYPES

```go

type AdminController struct {
	// Has unexported fields.
}
    AdminController

    Controlador que maneja los endpoints del panel de administracion, incluyendo
    gestion de usuarios, roles, proveedores, reservaciones recientes y metricas
    financieras.

func NewAdminController(db *sql.DB) *AdminController
    NewAdminController

    Constructor que retorna una nueva instancia de AdminController con la
    conexion a la base de datos inyectada.

      - db: puntero a la conexion de base de datos SQL

      - *AdminController: puntero a la nueva instancia

func (ctrl *AdminController) ActualizarRol(c *gin.Context)
    ActualizarRol

    Actualiza el rol de un usuario especifico. El ID del usuario se lee desde el
    parametro de URL :id y el nuevo rol desde el body JSON.


      - HTTP 200 OK: JSON con mensaje de confirmacion
      - HTTP 400 Bad Request: si el ID es invalido o el campo rolId no esta
        presente
      - HTTP 500 Internal Server Error: si ocurre un error de conexion o al
        ejecutar el UPDATE

func (ctrl *AdminController) EditarProveedor(c *gin.Context)
    EditarProveedor

    Actualiza el nombre, URL de API y porcentaje de ganancia de un proveedor.
    El ID del proveedor se lee desde el parametro de URL :id.


      - HTTP 400 Bad Request: si el ID es invalido o el body no puede ser
        parseado

func (ctrl *AdminController) ListarProveedores(c *gin.Context)
    ListarProveedores

    Retorna la lista completa de proveedores registrados con su tipo, estado,
    URL de API y porcentaje de ganancia configurado, ordenada por ID ascendente.


      - HTTP 200 OK: JSON con arreglo de proveedores incluyendo campo activo
        derivado del EstadoID
      - HTTP 500 Internal Server Error: si ocurre un error de conexion o
        consulta

func (ctrl *AdminController) ListarUsuarios(c *gin.Context)
    ListarUsuarios

    Retorna la lista completa de usuarios registrados en el sistema junto con su
    rol asignado, ordenada por ID ascendente.


      - HTTP 200 OK: JSON con arreglo de usuarios (id, nombre, apellido, correo,
        fechaRegistro, rolId, rol)

func (ctrl *AdminController) ObtenerMetricas(c *gin.Context)
    ObtenerMetricas

    Retorna el desglose financiero detallado de todas las reservaciones,
    separando el monto cobrado, el costo base y la ganancia por cada
    detalle (vuelo u hotel) segun el porcentaje de ganancia configurado en
    cada proveedor. Tambien incluye un resumen global agrupado por tipo de
    reservacion.


      - HTTP 200 OK: JSON con campos resumen (totales globales y por tipo) y
        reservaciones (lista detallada con desglose financiero individual)

      - El costo base se calcula como: cobrado / (1 + porcentaje/100)
      - La ganancia es la diferencia entre cobrado y base

func (ctrl *AdminController) ReservacionesRecientes(c *gin.Context)
    ReservacionesRecientes

    Retorna las ultimas 10 reservaciones realizadas en la plataforma por
    cualquier usuario, ordenadas por fecha de creacion descendente. Incluye
    nombre del usuario, tipo, total y estado de cada reservacion.


      - HTTP 200 OK: JSON con arreglo de hasta 10 reservaciones recientes

func (ctrl *AdminController) ToggleEstadoProveedor(c *gin.Context)
    ToggleEstadoProveedor

    Activa o desactiva un proveedor segun el valor del campo activo recibido
    en el body. El ID del proveedor se lee desde el parametro de URL :id.
    EstadoID 1 equivale a activo, EstadoID 2 equivale a inactivo.



type AsientoVueloController struct {
    AsientoVueloController

    Controlador encargado de gestionar el mapa de asientos de un vuelo y el
    cambio de asiento de un boleto especifico.

func NewAsientoVueloController(service *services.AsientoVueloService) *AsientoVueloController
    NewAsientoVueloController

    Crea e inicializa un nuevo AsientoVueloController con el servicio recibido.

      - service: instancia del servicio de asientos de vuelo

      - *AsientoVueloController: puntero al controlador creado

func (ctrl *AsientoVueloController) CambiarAsiento(c *gin.Context)
    CambiarAsiento

    Handler HTTP que actualiza el asiento de un boleto especifico dentro de una
    reservacion. Valida la sesion del usuario y delega la operacion al servicio,
    que verifica que el boleto pertenezca a la reserva del usuario.


      - HTTP 200: mensaje de confirmacion indicando que el cambio fue procesado
        por la aerolinea
      - HTTP 400: error si los datos enviados son invalidos o el servicio
        retorna un error
      - HTTP 401: error si el usuario no esta autenticado

func (ctrl *AsientoVueloController) ObtenerAsientos(c *gin.Context)
    ObtenerAsientos

    Handler HTTP que devuelve el mapa de asientos de un vuelo para el usuario
    autenticado. Valida la sesion del usuario y delega la consulta al servicio.


      - HTTP 200: objeto AsientosVueloResponse con el mapa de asientos y boletos
        de la agencia
      - HTTP 400: error si el body JSON es invalido o el servicio retorna un
        error

type BusquedaController struct {
    BusquedaController

    Controlador encargado de gestionar las busquedas de vuelos y hoteles
    disponibles a traves de los proveedores registrados.

func NewBusquedaController(service *services.BusquedaService) *BusquedaController
    NewBusquedaController

    Crea e inicializa un nuevo BusquedaController con el servicio recibido.

      - service: instancia del servicio de busqueda

      - *BusquedaController: puntero al controlador creado

func (ctrl *BusquedaController) BuscarHoteles(c *gin.Context)
    BuscarHoteles

    Handler HTTP que recibe los criterios de busqueda de hoteles y retorna los
    resultados obtenidos de los proveedores hoteleros.


      - HTTP 200: lista de hoteles disponibles que coinciden con los criterios

      - Ruta esperada: POST /busqueda/hoteles

func (ctrl *BusquedaController) BuscarVuelos(c *gin.Context)
    BuscarVuelos

    Handler HTTP que recibe los criterios de busqueda de vuelos y retorna los
    resultados obtenidos de los proveedores de aerolineas.


      - HTTP 200: lista de vuelos disponibles que coinciden con los criterios

      - Ruta esperada: POST /busqueda/vuelos

type CancelacionController struct {
    CancelacionController

    Controlador encargado de gestionar la verificacion y ejecucion de
    cancelaciones de reservaciones realizadas por el usuario.

func NewCancelacionController(s *services.CancelacionService) *CancelacionController
    NewCancelacionController

    Crea e inicializa un nuevo CancelacionController con el servicio recibido.

      - s: instancia del servicio de cancelacion

      - *CancelacionController: puntero al controlador creado

func (ctrl *CancelacionController) Cancelar(c *gin.Context)
    Cancelar

    Handler HTTP que ejecuta la cancelacion de una reservacion existente.
    El motivo de cancelacion es opcional; si no se proporciona se usa una cadena
    vacia.


      - HTTP 200: mensaje confirmando que la reservacion fue cancelada
        exitosamente
      - HTTP 400: error si el parametro de ruta ID no es un entero valido
      - HTTP 422: error si el servicio no puede procesar la cancelacion

      - Ruta esperada: POST /api/reservaciones/:id/cancelar

func (ctrl *CancelacionController) Verificar(c *gin.Context)
    Verificar

    Handler HTTP que verifica si una reservacion puede ser cancelada,
    devolviendo las condiciones y penalizaciones aplicables antes de confirmar
    la cancelacion.


      - HTTP 200: resultado de la verificacion con condiciones de cancelacion
      - HTTP 404: error si la reservacion no existe o no pertenece al usuario

      - Ruta esperada: GET /api/reservaciones/:id/cancelar/verificar

type CatalogoController struct {
    CatalogoController

    Controlador encargado de gestionar la actualizacion del catalogo de
    productos (vuelos, hoteles y paquetes) obtenidos desde los proveedores
    externos.

func NewCatalogoController(service *services.CatalogoService) *CatalogoController
    NewCatalogoController

    Crea e inicializa un nuevo CatalogoController con el servicio recibido.

      - service: instancia del servicio de catalogo

      - *CatalogoController: puntero al controlador creado

func (ctrl *CatalogoController) ActualizarCatalogo(c *gin.Context)
    ActualizarCatalogo

    Handler HTTP que dispara el proceso de actualizacion del catalogo
    consultando los proveedores registrados y sincronizando la informacion en la
    base de datos.


      - HTTP 200: mensaje de proceso completado junto con el detalle de
        resultados por proveedor
      - HTTP 500: error interno si el servicio falla durante la actualizacion

type ComentarioController struct {
    ComentarioController

    Controlador que maneja los endpoints de consulta de comentarios asociados a
    vuelos y hoteles de proveedores registrados.

func NewComentarioController(s *services.ComentarioService) *ComentarioController
    NewComentarioController

    Constructor que retorna una nueva instancia de ComentarioController con el
    servicio de comentarios inyectado.

      - s: puntero al servicio de comentarios

      - *ComentarioController: puntero a la nueva instancia

func (ctrl *ComentarioController) ObtenerComentariosHotel(c *gin.Context)
    ObtenerComentariosHotel

    Retorna la lista de comentarios asociados a un hotel especifico de un
    proveedor dado. Los parametros proveedorId y hotelId se leen desde la URL.


      - HTTP 200 OK: JSON con la lista de comentarios del hotel
      - HTTP 400 Bad Request: si proveedorId o hotelId no son enteros validos
      - HTTP 500 Internal Server Error: si ocurre un error en la capa de
        servicio

func (ctrl *ComentarioController) ObtenerComentariosVuelo(c *gin.Context)
    ObtenerComentariosVuelo

    Retorna la lista de comentarios asociados a una ruta de vuelo especifica de
    un proveedor dado. Los parametros proveedorId y rutaId se leen desde la URL.


      - HTTP 200 OK: JSON con la lista de comentarios del vuelo
      - HTTP 400 Bad Request: si proveedorId o rutaId no son enteros validos

type ConfiguracionController struct {
    ConfiguracionController

    Controlador encargado de exponer la configuracion global de la agencia,
    incluyendo el porcentaje de descuento aplicado a reservaciones de tipo
    paquete.

func NewConfiguracionController(db *sql.DB) *ConfiguracionController
    NewConfiguracionController

    Crea e inicializa un nuevo ConfiguracionController con la conexion a la base
    de datos.

      - db: conexion activa a la base de datos

      - *ConfiguracionController: puntero al controlador creado

func (ctrl *ConfiguracionController) ObtenerDescuento(c *gin.Context)
    ObtenerDescuento

    Handler HTTP publico que retorna el porcentaje de descuento configurado para
    reservaciones de tipo paquete. Si la consulta falla retorna 0.

      - HTTP 200: JSON con el campo porcentaje_descuento

      - Ruta esperada: GET /api/configuracion/descuento

type DetalleReservacionController struct {
    DetalleReservacionController

    Controlador encargado de agregar detalles a una reservacion existente,
    incluyendo vuelos con sus pasajeros y habitaciones de hotel.

func NewDetalleReservacionController(service *services.DetalleReservacionService) *DetalleReservacionController
    NewDetalleReservacionController

    Crea e inicializa un nuevo DetalleReservacionController con el servicio
    recibido.

      - service: instancia del servicio de detalle de reservacion

      - *DetalleReservacionController: puntero al controlador creado

func (ctrl *DetalleReservacionController) AgregarDetalleHotel(c *gin.Context)
    AgregarDetalleHotel

    Handler HTTP que agrega un detalle de hotel a la reservacion del usuario
    autenticado. Deserializa la solicitud y delega la operacion al servicio.


      - HTTP 200: respuesta del servicio con el detalle de hotel registrado

func (ctrl *DetalleReservacionController) AgregarDetalleVuelo(c *gin.Context)
    AgregarDetalleVuelo

    Handler HTTP que agrega un detalle de vuelo a la reservacion del usuario
    autenticado. Valida la sesion y deserializa la solicitud antes de delegar al
    servicio.


      - HTTP 200: respuesta del servicio con el detalle de vuelo registrado

func (ctrl *DetalleReservacionController) AgregarPasajerosVuelo(c *gin.Context)
    AgregarPasajerosVuelo

    Handler HTTP que registra los datos de los pasajeros asociados a un vuelo
    dentro de la reservacion del usuario autenticado.


      - HTTP 200: mensaje confirmando que los datos de pasajeros fueron
        guardados correctamente

type HandshakeController struct {
    HandshakeController

    Controlador encargado de iniciar el proceso de handshake con proveedores
    de aerolineas, obteniendo el token de sesion necesario para consumir sus
    servicios.

func NewHandshakeController(service *services.HandshakeService) *HandshakeController
    NewHandshakeController

    Crea e inicializa un nuevo HandshakeController con el servicio recibido.

      - service: instancia del servicio de handshake de aerolineas

      - *HandshakeController: puntero al controlador creado

func (ctrl *HandshakeController) IniciarHandshake(c *gin.Context)
    IniciarHandshake

    Handler HTTP que inicia el proceso de handshake con un proveedor de
    aerolinea identificado por su ID en la ruta. Si el proceso es exitoso
    retorna el token de salida generado por el proveedor.


      - HTTP 200: mensaje de exito junto con el token de salida del proveedor
      - HTTP 400: error si el parametro de ruta ID no es un entero valido o el
        servicio retorna un error

      - El parametro de ruta :id corresponde al ID del proveedor de aerolinea

type HandshakeHoteleraController struct {
    HandshakeHoteleraController

    hoteleros, obteniendo el token de sesion necesario para consumir sus

func NewHandshakeHoteleraController(service *services.HandshakeHoteleraService) *HandshakeHoteleraController
    NewHandshakeHoteleraController

    Crea e inicializa un nuevo HandshakeHoteleraController con el servicio

      - service: instancia del servicio de handshake hotelero

      - *HandshakeHoteleraController: puntero al controlador creado

func (ctrl *HandshakeHoteleraController) IniciarHandshake(c *gin.Context)

    Handler HTTP que inicia el proceso de handshake con un proveedor hotelero
    identificado por su ID en la ruta. Si el proceso es exitoso retorna el token
    de salida generado por el proveedor.


        hotelero

      - El parametro de ruta :id corresponde al ID del proveedor hotelero

type LoginController struct {
    LoginController

    Controlador encargado de gestionar la autenticacion de usuarios, incluyendo
    el inicio y cierre de sesion mediante JWT almacenado en cookie.

func NewLoginController(service *services.LoginService) *LoginController
    NewLoginController

    Crea e inicializa un nuevo LoginController con el servicio recibido.

      - service: instancia del servicio de login

      - *LoginController: puntero al controlador creado

func (ctrl *LoginController) Login(c *gin.Context)
    Login

    Handler HTTP que autentica al usuario con sus credenciales. Si son validas
    genera un token JWT y lo persiste en una cookie HttpOnly con duracion de 24
    horas, retornando ademas los datos del usuario en el cuerpo de la respuesta.


      - HTTP 200: datos del usuario autenticado y cookie de sesion establecida
      - HTTP 400: error si el body JSON es invalido
      - HTTP 401: error si las credenciales son incorrectas
      - HTTP 500: error interno al generar el token JWT o al procesar el login

func (ctrl *LoginController) Logout(c *gin.Context)
    Logout

    Handler HTTP que cierra la sesion del usuario eliminando la cookie de sesion
    al establecer su tiempo de vida en -1.


      - HTTP 200: mensaje confirmando que la sesion fue cerrada

type MisReservacionesController struct {
    MisReservacionesController

    Controlador encargado de exponer las reservaciones del usuario autenticado,
    tanto en formato de listado resumido como en detalle completo consultando a
    los proveedores externos.

func NewMisReservacionesController(s *services.MisReservacionesService) *MisReservacionesController
    NewMisReservacionesController

    Crea e inicializa un nuevo MisReservacionesController con el servicio

      - s: instancia del servicio de mis reservaciones

      - *MisReservacionesController: puntero al controlador creado

func (ctrl *MisReservacionesController) Detalle(c *gin.Context)
    Detalle

    Handler HTTP que devuelve el detalle completo de una reservacion especifica
    del usuario autenticado, consultando informacion actualizada de los
    proveedores externos.


      - HTTP 200: objeto con el detalle completo de la reservacion

      - Ruta esperada: GET /api/reservaciones/mias/:id

func (ctrl *MisReservacionesController) Listar(c *gin.Context)
    Listar

    Handler HTTP que devuelve todas las reservaciones del usuario autenticado
    con los datos almacenados localmente en la base de datos de la agencia.


      - HTTP 200: lista de reservaciones del usuario
      - HTTP 500: error interno si el servicio falla al consultar las
        reservaciones

      - Ruta esperada: GET /api/reservaciones/mias

type PagoController struct {
    PagoController

    Controlador que maneja los endpoints relacionados al procesamiento de pagos
    de reservaciones existentes en la plataforma.

func NewPagoController(s *services.PagoService) *PagoController
    NewPagoController

    Constructor que retorna una nueva instancia de PagoController con el
    servicio de pagos inyectado.

      - s: puntero al servicio de pagos

      - *PagoController: puntero a la nueva instancia

func (ctrl *PagoController) Pagar(c *gin.Context)
    Pagar

    Procesa el pago de una reservacion existente para el usuario autenticado.
    Lee el ID del usuario desde el contexto de Gin, valida el body del request y
    delega el procesamiento al servicio de pagos.


      - HTTP 200 OK: JSON con mensaje de confirmacion si el pago fue exitoso
      - HTTP 400 Bad Request: si los datos de pago en el body estan incompletos
        o son invalidos
      - HTTP 422 Unprocessable Entity: si el servicio rechaza el pago (fondos
        insuficientes, reservacion no encontrada, etc.)

      - El ID del usuario autenticado se obtiene del contexto sin verificacion
        de existencia

type PerfilController struct {
    PerfilController

    Controlador que maneja los endpoints de consulta y actualizacion del perfil
    del usuario autenticado, incluyendo telefono y contrasena.

func NewPerfilController(service *services.PerfilService) *PerfilController
    NewPerfilController

    Constructor que retorna una nueva instancia de PerfilController con el
    servicio de perfil inyectado.

      - service: puntero al servicio de perfil

      - *PerfilController: puntero a la nueva instancia

func (ctrl *PerfilController) ActualizarTelefono(c *gin.Context)
    ActualizarTelefono

    Actualiza el numero de telefono del usuario autenticado. El nuevo numero se
    lee del campo telefono en el body JSON de la solicitud.


      - HTTP 400 Bad Request: si el campo telefono no esta presente en el body
      - HTTP 500 Internal Server Error: si ocurre un error al actualizar el
        telefono

func (ctrl *PerfilController) CambiarContrasena(c *gin.Context)
    CambiarContrasena

    Cambia la contrasena del usuario autenticado. Verifica que la contrasena
    actual sea correcta, que la nueva y su confirmacion coincidan, y que la
    nueva tenga al menos 8 caracteres antes de persistir el cambio.


      - HTTP 400 Bad Request: si faltan campos, las contrasenas no coinciden o
        la nueva es muy corta
      - HTTP 401 Unauthorized: si la contrasena actual proporcionada es
        incorrecta
      - HTTP 500 Internal Server Error: si ocurre un error al verificar o
        cambiar la contrasena

      - La verificacion de la contrasena actual se realiza comparando el hash
        almacenado

func (ctrl *PerfilController) ObtenerPerfil(c *gin.Context)
    ObtenerPerfil

    Retorna los datos del perfil del usuario actualmente autenticado. El ID del
    usuario se extrae del contexto de Gin inyectado por el middleware.


      - HTTP 200 OK: JSON con los datos del perfil del usuario
      - HTTP 500 Internal Server Error: si ocurre un error al obtener el perfil

type ProveedorController struct {
    ProveedorController

    Controlador encargado de gestionar el alta de proveedores externos
    (aerolineas y hoteles) en el sistema de la agencia.

func NewProveedorController(service *services.ProveedorService) *ProveedorController
    NewProveedorController

    Crea e inicializa un nuevo ProveedorController con el servicio recibido.

      - service: instancia del servicio de proveedor

      - *ProveedorController: puntero al controlador creado

func (ctrl *ProveedorController) CrearProveedor(c *gin.Context)
    CrearProveedor

    Handler HTTP que registra un nuevo proveedor externo en el sistema a partir
    de los datos enviados en el body de la solicitud.


      - HTTP 201: mensaje de confirmacion junto con los datos del proveedor
        creado
        error de validacion

type ReservacionController struct {
    ReservacionController

    Controlador que maneja los endpoints de creacion de reservaciones, descarga
    de PDF y envio de correo de confirmacion al usuario autenticado.

func NewReservacionController(
	service *services.ReservacionService,
	pdfService *services.PdfReservacionService,
	emailService *services.EmailReservacionService,
) *ReservacionController
    NewReservacionController

    Constructor que retorna una nueva instancia de ReservacionController con los
    servicios de reservacion, PDF y correo inyectados.

      - service: servicio principal de reservaciones
      - pdfService: servicio de generacion de PDF
      - emailService: servicio de envio de correo de confirmacion

      - *ReservacionController: puntero a la nueva instancia

func (ctrl *ReservacionController) CrearReservacion(c *gin.Context)
    CrearReservacion

    Crea una nueva reservacion para el usuario autenticado. Valida que el tipo
    de reserva sea 1 (Aerolinea), 2 (Hotelera) o 3 (Paquete) antes de delegar al


      - HTTP 201 Created: JSON con los datos de la reservacion creada
      - HTTP 400 Bad Request: si el body es invalido o el tipo de reserva es
        incorrecto
      - HTTP 401 Unauthorized: si el usuario no esta autenticado

func (ctrl *ReservacionController) DescargarPDF(c *gin.Context)
    DescargarPDF

    Genera y retorna el PDF de una reservacion especifica del usuario
    autenticado. El ID de la reservacion se lee desde el parametro de URL :id.


      - HTTP 200 OK: archivo PDF adjunto con nombre reservacion-{id}.pdf
      - HTTP 400 Bad Request: si el ID de la reservacion no es un entero valido
      - HTTP 404 Not Found: si la reservacion no existe
      - HTTP 500 Internal Server Error: si ocurre un error al generar el PDF

      - El PDF se entrega con Content-Disposition attachment para forzar la
        descarga

func (ctrl *ReservacionController) EnviarCorreo(c *gin.Context)
    EnviarCorreo

    Envia un correo de confirmacion al usuario autenticado para una reservacion
    especifica. El ID de la reservacion se lee desde el parametro de URL :id.


      - HTTP 200 OK: JSON con mensaje de exito al enviar el correo

      - Los errores del servicio de correo se registran en el log del servidor

type SesionController struct{}
    SesionController

    Controlador que maneja los endpoints relacionados a la sesion del usuario
    autenticado.

func NewSesionController() *SesionController
    NewSesionController

    Constructor que retorna una nueva instancia de SesionController.

      - *SesionController: puntero a la nueva instancia

func (ctrl *SesionController) ObtenerSesion(c *gin.Context)
    ObtenerSesion

    Retorna los datos de la sesion del usuario actualmente autenticado,
    extrayendo el ID, nombre de usuario y rol desde el contexto de Gin inyectado
    por el middleware de autenticacion.


      - HTTP 200 OK: JSON con campos usuario_id, username y rol_id

type StatsController struct {
    StatsController

    Controlador que maneja los endpoints de estadisticas generales de la
    plataforma, incluyendo conteos de proveedores, usuarios, reservaciones e
    ingresos totales. Es de acceso publico, no requiere autenticacion.

func NewStatsController(db *sql.DB) *StatsController
    NewStatsController

    Constructor que retorna una nueva instancia de StatsController con la


      - *StatsController: puntero a la nueva instancia

func (ctrl *StatsController) ObtenerStats(c *gin.Context)
    ObtenerStats

    Retorna un resumen estadistico de la plataforma consultando directamente
    la base de datos. Incluye conteos de aerolineas, hoteles, usuarios,
    reservaciones por estado y por tipo, asi como los ingresos totales.


      - HTTP 200 OK: JSON con todas las estadisticas de la plataforma
      - HTTP 500 Internal Server Error: si ocurre un error de conexion a la base
        de datos

      - Este endpoint es publico y no requiere autenticacion
      - Los ingresos totales se calculan sumando el campo Total de
        detalles_reservacion

type UsuarioController struct {
    UsuarioController

    Controlador que maneja los endpoints de gestion de usuarios, incluyendo el
    registro de nuevas cuentas y la consulta del listado completo para el panel
    de administracion.

func NewUsuarioController(service *services.UsuarioService) *UsuarioController
    NewUsuarioController

    Constructor que retorna una nueva instancia de UsuarioController con el
    servicio de usuario inyectado.

      - service: puntero al servicio de usuario

      - *UsuarioController: puntero a la nueva instancia

func (ctrl *UsuarioController) ObtenerTodos(c *gin.Context)
    ObtenerTodos

    Retorna la lista completa de usuarios registrados en el sistema con sus
    datos basicos y rol asignado. Usado por el panel de administracion para
    gestion de roles y asignacion de usuarios WebService a proveedores.


      - HTTP 200 OK: JSON con el listado de usuarios
      - HTTP 500 Internal Server Error: si ocurre un error al consultar la base

func (ctrl *UsuarioController) Registrar(c *gin.Context)
    Registrar

    Registra un nuevo usuario en el sistema a partir de los datos enviados en el
    body de la solicitud. Verifica duplicados de correo, pasaporte y username.
    Si el registro es exitoso, envia un correo de bienvenida en segundo plano
    sin bloquear la respuesta al cliente.


      - HTTP 201 Created: JSON con mensaje de exito si el usuario fue registrado
      - HTTP 400 Bad Request: si el body no puede ser parseado o tiene datos
        invalidos
      - HTTP 409 Conflict: JSON con campos duplicados (correo, pasaporte,
        username)
      - HTTP 500 Internal Server Error: si ocurre un error interno al registrar

      - El correo de bienvenida se envia de forma asincrona (goroutine
        fire-and-forget)
      - Los errores del envio de correo se registran en el log del servidor

type WebserviceController struct {
    WebserviceController

    Controlador que maneja los endpoints del webservice externo, permitiendo a
    proveedores autenticados notificar cambios de estado sobre reservaciones
    mediante un token de proveedor.

func NewWebserviceController(db *sql.DB) *WebserviceController
    NewWebserviceController

    Constructor que retorna una nueva instancia de WebserviceController con la


      - *WebserviceController: puntero a la nueva instancia

func (ctrl *WebserviceController) RecibirNotificacion(c *gin.Context)
    RecibirNotificacion

    Recibe una notificacion de cambio de estado desde un proveedor externo
    autenticado via header X-Proveedor-Token. Busca la reservacion interna
    usando el ID de reserva del proveedor y actualiza su estado en la base
    de datos. Si el nuevo estado es cancelada, guarda el motivo en el campo
    parametros_json del detalle de la reservacion.


      - HTTP 200 OK: JSON con mensaje de confirmacion, reservacion_id y
        nuevo_estado
      - HTTP 400 Bad Request: si el body es invalido, el estado no es
        reconocido, o falta el motivo en una cancelacion
      - HTTP 404 Not Found: si no se encuentra la reservacion para el proveedor
        indicado
        actualizar

      - La identidad del proveedor (proveedor_id) es inyectada por el middleware
        ProveedorRequerido
      - Los estados validos son: cancelada, confirmada, completada, en curso
      - El motivo es obligatorio cuando nuevoEstado es cancelada

```
