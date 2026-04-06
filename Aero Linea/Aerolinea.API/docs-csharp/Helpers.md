# Helpers

## AgenciaAuthMiddleware

> Representa la identidad de una agencia externa autenticada. Contiene los datos basicos que se propagan en el contexto HTTP tras validar el token.

```csharp
public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
```

Ejecuta la validacion del token de agencia antes de que el action del controller sea invocado. Extrae el token del header X-Agencia-Token, consulta la base de datos para verificarlo y almacena los datos de la agencia en HttpContext.Items si es valido. Retorna 401 si el token esta ausente o no corresponde a ninguna agencia registrada.

---

## CustomAssemblyLoadContext

> Contexto de carga de ensamblados personalizado utilizado para cargar librerias nativas en tiempo de ejecucion de forma aislada, principalmente para la generacion de PDFs. Hereda de AssemblyLoadContext para permitir la carga de DLLs no administradas por ruta absoluta.

```csharp
public IntPtr LoadUnmanagedLibrary(string absolutePath)
```

Carga una libreria nativa no administrada desde la ruta absoluta indicada y retorna un handle a la misma.

---

## EmailHelper

> Clase estatica de utilidad para el envio de correos electronicos mediante SMTP. Encapsula la configuracion del servidor de correo y expone metodos para enviar mensajes HTML simples, con copia oculta, y para escapar texto en HTML.

```csharp
public static async Task Enviar(string destinatario, string asunto, string cuerpoHtml)
```

Envia un correo electronico con cuerpo HTML al destinatario especificado. Utiliza el servidor SMTP configurado con SSL en el puerto 587.

---

```csharp
public static async Task EnviarConCopia(string destinatario, string asunto, string cuerpoHtml, string copiaOculta)
```

Envia un correo electronico con cuerpo HTML al destinatario e incluye una copia oculta (BCC) a la direccion indicada, si esta no esta vacia.

---

```csharp
public static string Esc(string texto)
```

Escapa los caracteres especiales de HTML en el texto recibido para evitar inyeccion de etiquetas al incrustar contenido dinamico en plantillas HTML. Retorna cadena vacia si el texto es nulo o vacio.

---

## EmailTemplates

> Clase estatica que centraliza todas las plantillas HTML de correos electronicos enviados por la aplicacion. Cada metodo genera y retorna el HTML completo listo para ser usado como cuerpo del mensaje en EmailHelper.

```csharp
public static string CorreoBienvenida(
```

Genera el HTML del correo de bienvenida que se envia al usuario inmediatamente despues de crear su cuenta en el sistema. Incluye los datos personales registrados, credenciales de acceso y una nota de seguridad sobre el manejo de la contrasena.

---

```csharp
public static string CorreoReservacion(ReservacionDetalleDTO reservacion)
```

Genera el HTML del correo de detalle de reservacion que se envia al usuario con el comprobante completo. Incluye la tabla de boletos, datos de pasajeros si los hay, el total de la reservacion y los terminos y condiciones del viaje.

---

```csharp
public static string CorreoContacto(string nombre, string correo, string asunto, string mensaje)
```

Genera el HTML del correo de notificacion que se envia al administrador cuando un usuario envia un mensaje a traves del formulario de contacto. Incluye el nombre, correo, asunto y cuerpo del mensaje del remitente, junto con un boton para responder directamente.

---

```csharp
public static string CorreoNewsletter(string correo)
```

Genera el HTML del correo de notificacion al administrador cuando un nuevo usuario se suscribe al boletin informativo de Broom AirLine. Muestra el correo del suscriptor y un boton para contactarlo directamente.

---

```csharp
public static string CorreoConfirmacion(string nombreUsuario, string noReservacion, decimal total)
```

Genera el HTML del correo de confirmacion de reservacion que se envia al usuario una vez que el pago y la confirmacion han sido procesados exitosamente. Muestra el numero de reservacion y el total cobrado.

---

```csharp
public static string CorreoCancelacion(string nombreUsuario, string noReservacion)
```

Genera el HTML del correo de aviso de cancelacion que se envia al usuario cuando su reservacion ha sido cancelada, ya sea por el propio usuario o por el sistema. Indica el numero de reservacion afectado y datos de contacto.

---

## PasswordHasher

> Clase estatica de utilidad para el manejo seguro de contrasenas mediante BCrypt. Provee metodos para generar el hash de una contrasena en texto plano y para verificar si una contrasena ingresada coincide con su hash almacenado.

```csharp
public static string Hash(string password)
```

Genera un hash seguro de la contrasena proporcionada usando el algoritmo BCrypt. El hash resultante incluye el salt embebido y es apto para almacenarse en la base de datos.

---

```csharp
public static bool Verify(string password, string hash)
```

Verifica si la contrasena en texto plano coincide con el hash previamente almacenado. Retorna true si la contrasena es correcta, false en caso contrario.

---

## PdfHtmlHelper

> Clase estatica que genera el HTML utilizado para producir el comprobante de reservacion en formato PDF. Construye un documento HTML completo con el detalle de boletos, datos del pasajero, informacion fiscal si aplica, subtotales por vuelo y los terminos y condiciones del servicio.

```csharp
public static string GenerarComprobante(ReservacionDetalleDTO reservacion)
```

Genera el HTML completo del comprobante de reservacion optimizado para impresion en formato A5 horizontal. Incluye encabezado con numero y estado de la reservacion, tabla de boletos con subtotales por vuelo, seccion de datos de pasajeros, datos fiscales si la reservacion tiene factura asociada, y pie de pagina institucional.

---

## SessionHelper

> Clase estatica de utilidad para acceder a los datos del usuario autenticado almacenados en los claims del JWT. Centraliza los nombres de los claims y expone metodos para leer el ID, rol, nombre y correo del usuario activo sin necesidad de manipular strings de claims directamente en los controllers.

```csharp
public static int? GetUsuarioId(HttpContext context)
```

Retorna el ID del usuario autenticado extraido del claim JWT. Retorna null si el usuario no tiene sesion activa o el claim no existe.

---

```csharp
public static int? GetRolId(HttpContext context)
```

Retorna el ID del rol del usuario autenticado extraido del claim JWT. Retorna null si el usuario no tiene sesion activa o el claim no existe.

---

```csharp
public static string? GetRolNombre(HttpContext context)
```

Retorna el nombre del rol del usuario autenticado segun el claim de rol estandar de ASP.NET. Retorna null si no hay sesion activa.

---

```csharp
public static string? GetNombre(HttpContext context)
```

Retorna el nombre completo del usuario autenticado segun el claim de nombre estandar. Retorna null si no hay sesion activa.

---

```csharp
public static string? GetCorreo(HttpContext context)
```

Retorna el correo electronico del usuario autenticado segun el claim de email estandar. Retorna null si no hay sesion activa.

---

```csharp
public static bool EstaAutenticado(HttpContext context)
```

Indica si el contexto HTTP actual corresponde a un usuario con sesion autenticada. Retorna true si el usuario tiene una identidad valida y autenticada.

---

```csharp
public static bool TieneRol(HttpContext context, string rolNombre)
```

Verifica si el usuario autenticado posee el rol indicado segun los claims del JWT. Retorna true si el usuario pertenece al rol especificado.

---

## TarjetaHelper

> Clase estatica de utilidad para la validacion y deteccion de tarjetas de credito o debito. Realiza comprobaciones de formato superficiales sobre los datos de la tarjeta sin realizar ningun cargo ni verificacion con entidades bancarias externas.

```csharp
public static void ValidarFormato(
```

Valida el formato de los datos de una tarjeta de pago: numero de 16 digitos, nombre del titular con caracteres validos, fecha de expiracion en formato MM/YY no vencida, y CVV de 3 o 4 digitos. Lanza una excepcion con mensaje descriptivo si alguno de los campos no cumple el formato esperado. No verifica si la tarjeta es real ni realiza cargos.

---

```csharp
public static string DetectarTipo(string numeroTarjeta)
```

Detecta el tipo de red de la tarjeta (Visa, Mastercard, American Express, Discover) a partir del prefijo del numero de tarjeta. Solo tiene caracter informativo y no garantiza que la tarjeta pertenezca realmente a esa red.

---

## TokenHelper

> Clase estatica de utilidad para la generacion de tokens seguros y unicos. Utiliza el generador de numeros aleatorios criptograficos del sistema para producir tokens con alta entropia aptos para autenticacion de agencias, restablecimiento de contrasena u otros flujos que requieran tokens opacos.

```csharp
public static string GenerarTokenHash()
```

Genera un token unico y seguro de 64 caracteres hexadecimales en minusculas. Internamente obtiene 32 bytes aleatorios criptograficamente seguros, les aplica SHA-256 y convierte el resultado a cadena hexadecimal.

---
