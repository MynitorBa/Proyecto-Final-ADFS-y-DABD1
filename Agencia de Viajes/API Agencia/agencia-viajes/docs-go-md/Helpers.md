# Helpers


# Package helpers

Provee funciones auxiliares reutilizables para tareas comunes de la aplicacion
Movent: generacion de tokens, hashing de contrasenas, manejo de sesiones JWT,
envio de correos electronicos y generacion de documentos PDF.










## FUNCTIONS

```go

func BuildHTMLBienvenida(nombre, apellido, username, correo, telefono, fechaNacimiento, ciudad, pais string, nacionalidades []string) string
    BuildHTMLBienvenida

    Genera el HTML completo del correo de bienvenida que se envia al usuario
    tras completar el registro. Incluye una tabla con los datos de la cuenta
    recien creada.

    Parametros:
      - nombre: nombre del usuario registrado
      - apellido: apellido del usuario registrado
      - username: nombre de usuario elegido
      - correo: direccion de correo electronico registrada
      - telefono: numero de telefono del usuario
      - fechaNacimiento: fecha de nacimiento en formato string
      - ciudad: nombre de la ciudad del usuario
      - pais: nombre del pais del usuario
      - nacionalidades: slice con los nombres de las nacionalidades del usuario

    Retorna:
      - string: documento HTML completo listo para enviar como cuerpo de correo

func BuildHTMLEmail(data ReservacionPDFData) string
    BuildHTMLEmail

    Genera el HTML completo del correo de confirmacion de reservacion.
    Incluye tarjetas para boletos de vuelo y habitaciones de hotel con todos los
    detalles de la reservacion en formato de tabla.

      - data: struct ReservacionPDFData con todos los datos de la reservacion


func CheckPassword(password, hash string) bool
    CheckPassword

    Compara una contrasena en texto plano contra su hash bcrypt almacenado.
    Retorna verdadero si coinciden, falso en cualquier otro caso.

      - password: contrasena en texto plano ingresada por el usuario
      - hash: hash bcrypt almacenado en la base de datos

      - bool: true si la contrasena es valida, false si no coincide

func EnviarBienvenida(correo, nombre, apellido, username, telefono, fechaNacimiento, ciudad, pais string, nacionalidades []string) error
    EnviarBienvenida

    Construye el correo HTML de bienvenida con los datos del usuario recien
    registrado y lo envia a su direccion de correo electronico.

      - correo: direccion de correo del nuevo usuario
      - nombre: nombre del usuario
      - apellido: apellido del usuario

      - error: error si la construccion del HTML o el envio SMTP falla

func EnviarEmailConPDF(destinatario, asunto, htmlBody string, pdfBytes []byte, nombreArchivo string) error
    EnviarEmailConPDF

    Construye y envia un correo electronico multipart con cuerpo HTML y un
    archivo PDF adjunto. Ambas partes se codifican en base64. La conexion SMTP
    se establece con STARTTLS y autenticacion PLAIN.

      - destinatario: direccion de correo del receptor
      - asunto: linea de asunto del mensaje
      - htmlBody: contenido HTML del cuerpo del correo
      - pdfBytes: bytes del archivo PDF a adjuntar
      - nombreArchivo: nombre con el que se adjunta el PDF

      - error: error si la configuracion SMTP es incompleta o el envio falla

func EnviarEmailHTML(destinatario, asunto, htmlBody string) error
    EnviarEmailHTML

    Construye y envia un correo electronico con cuerpo HTML puro, sin archivos
    adjuntos. El cuerpo se codifica en base64 antes del envio. Se usa para
    notificaciones simples como la bienvenida.



func GenerarPDFReservacion(data ReservacionPDFData) ([]byte, error)
    GenerarPDFReservacion

    Genera el documento PDF de comprobante de reservacion a partir de los datos
    proporcionados. Construye el layout en dos columnas con un header fijo,
    banda de resumen, datos generales, boletos o habitaciones, total y
    condiciones. Retorna los bytes del PDF.

      - data: struct ReservacionPDFData con toda la informacion de la
        reservacion

      - []byte: contenido binario del PDF generado
      - error: error si la generacion o escritura del PDF falla

func GenerarToken(usuarioID int, username string, rolID int) (string, error)
    GenerarToken

    Crea y firma un JWT HS256 con los datos del usuario autenticado. El token
    tiene una vigencia de 24 horas a partir del momento de su emision y se firma
    con la clave JWT_SECRET del entorno.

      - usuarioID: identificador unico del usuario en la base de datos
      - username: nombre de usuario para incluir en los claims
      - rolID: identificador del rol del usuario para control de acceso

      - string: token JWT firmado listo para enviar al cliente
      - error: error si la firma del token falla

func GenerarTokenHash() (string, error)
    GenerarTokenHash

    Genera un token aleatorio seguro de 32 bytes y retorna su hash SHA-256
    codificado en hexadecimal. Se usa para crear tokens de autenticacion o
    identificacion de proveedores.

      - string: cadena hexadecimal de 64 caracteres con el hash SHA-256
      - error: error si la lectura de bytes aleatorios falla

func HashPassword(password string) (string, error)
    HashPassword

    Genera el hash bcrypt de la contrasena en texto plano proporcionada,
    usando el costo por defecto de la libreria bcrypt.

      - password: contrasena en texto plano a hashear

      - string: hash bcrypt resultante
      - error: error si la generacion del hash falla

```

## TYPES

```go

type BoletoPDF struct {
	NoBoleto       string
	NumeroVuelo    string
	Clase          string
	NoAsiento      string
	OrigenCodigo   string
	OrigenCiudad   string
	DestinoCodigo  string
	DestinoCiudad  string
	HoraSalida     string
	HoraLlegada    string
	FechaVuelo     string
	AvionMarca     string
	AvionModelo    string
	Precio         float64
	EstadoBoleto   string
	PasajeroNombre string
}
    BoletoPDF

    Representa los datos de un boleto de vuelo para incluir en el PDF o correo
    de confirmacion de reservacion.

type Claims struct {
	UsuarioID int    `json:"usuario_id"`
	Username  string `json:"username"`
	RolID     int    `json:"rol_id"`
	jwt.RegisteredClaims
    Claims

    Estructura de los claims personalizados incluidos en el JWT de sesion.
    Extiende jwt.RegisteredClaims con los datos de identidad del usuario
    necesarios para autorizacion en los middlewares y controladores.

func VerificarToken(tokenStr string) (*Claims, error)
    VerificarToken

    Parsea y valida un JWT firmado con HS256. Verifica que el metodo de firma
    sea HMAC, que la firma sea correcta y que el token no haya expirado,
    retornando los claims si todo es valido.

      - tokenStr: cadena JWT recibida desde el cliente

      - *Claims: puntero a los claims extraidos del token valido
      - error: error si el metodo de firma es invalido, la firma no coincide,
        el token esta expirado o los claims no son validos

type HabitacionPDF struct {
	NombreHotel      string
	TipoHabitacion   string
	TipoCama         string
	NumeroHabitacion string
	FechaCheckIn     string
	FechaCheckOut    string
	CantidadPersonas int
	TotalDetalle     float64
	Estado           string
    HabitacionPDF

    Representa los datos de una habitacion de hotel para incluir en el PDF o
    correo de confirmacion de reservacion.

type ReservacionPDFData struct {
	NoReservacion string
	EstadoReserva string
	FechaCreacion string
	Total         float64
	TipoReserva   int
	UsuarioNombre string
	UsuarioEmail  string
	Boletos       []BoletoPDF
	Habitaciones  []HabitacionPDF
    ReservacionPDFData

    Agrupa todos los datos necesarios para generar el PDF y el correo HTML de
    una reservacion. Contiene informacion del encabezado, del usuario titular y
    las listas de boletos y habitaciones.

type SMTPConfig struct {
	Host     string
	Port     string
	User     string
	Password string
	From     string
    SMTPConfig

    Contiene los parametros de conexion al servidor de correo SMTP. Se pobla
    desde variables de entorno mediante GetSMTPConfig.

func GetSMTPConfig() SMTPConfig
    GetSMTPConfig

    Lee las variables de entorno SMTP y retorna un SMTPConfig listo para usar.
    Si alguna variable no esta definida se usa el valor por defecto indicado
    (host: smtp.gmail.com, puerto: 587).

      - SMTPConfig: struct con los datos de conexion SMTP

```
