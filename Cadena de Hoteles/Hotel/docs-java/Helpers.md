# Helpers

## AgenciaAuthMiddleware

> Middleware de autenticacion para rutas protegidas de agencias externas. Valida el token enviado en el header X-Agencia-Token e inyecta la identidad de la agencia en el contexto de la peticion.

```java
public static boolean verificar(Context ctx)
```

Verifica el token de agencia en el header de la peticion. Si el token es valido, inyecta el ID, nombre y URL de la agencia como atributos del contexto para que los controllers puedan usarlos. Debe llamarse al inicio de cada ruta protegida de agencia. false si el token falta o no corresponde a ninguna agencia, en cuyo caso ya se escribe la respuesta 401 en el contexto.

- **Param** `ctx` - contexto de la peticion HTTP de Javalin.
- **Returns** - true si el token es valido y la agencia fue identificada;

---

## AuthMiddleware

> Middleware de autenticacion global para la aplicacion Javalin. Intercepta todas las peticiones entrantes y valida el token JWT antes de permitir el acceso a rutas protegidas.

```java
public static void registrar(Javalin app)
```

Registra el middleware de autenticacion en la instancia de Javalin. Se ejecuta antes de cada peticion. Si la ruta es publica o pertenece al prefijo /agencia/, la deja pasar sin validar. De lo contrario, exige un cookie auth_token valido y extrae los claims del usuario para inyectarlos en el contexto.

- **Param** `app` - instancia de Javalin donde se registra el middleware.

---

## CamposDuplicadosException

> Excepcion lanzada cuando uno o mas campos unicos ya existen en el sistema durante el registro de un usuario (username, correo o pasaporte duplicados). Incluye el detalle de cuales campos especificamente estan duplicados.

```java
public CamposDuplicadosException(UsuarioValidacionResponseDTO detalle)
```

Crea la excepcion con el detalle de los campos duplicados encontrados.

- **Param** `detalle` - DTO con flags indicando cuales campos ya existen en el sistema.

---

```java
public UsuarioValidacionResponseDTO getDetalle()
```

Retorna el detalle de los campos duplicados que causaron la excepcion.

- **Returns** - DTO con el estado de validacion de username, correo y pasaporte.

---

## CombinacionHelper

> Helper para calcular combinaciones de habitaciones que cubren un numero de personas. Genera particiones numericas usando las capacidades disponibles en stock, limitando a un maximo de 3 habitaciones por combinacion.

```java
public static List<List<Integer>> calcular(int n, Map<Integer, Integer> stockPorCapacidad)
```

Genera las mejores combinaciones de habitaciones para alojar a N personas. Usa las capacidades disponibles en stock para formar particiones validas, descarta la combinacion de una sola habitacion exacta (ya cubierta por busqueda individual) y retorna las 3 mejores priorizando capacidades mas grandes.

- **Param** `n` - cantidad de personas a alojar.
- **Param** `stockPorCapacidad` - mapa de capacidad -> cantidad de habitaciones disponibles de esa capacidad.
- **Returns** - lista de hasta 3 combinaciones, donde cada combinacion es una lista de capacidades que suman N.

---

## CredencialesInvalidasException

> Excepcion lanzada cuando las credenciales proporcionadas por el usuario no coinciden con las registradas en el sistema durante el inicio de sesion o el cambio de contrasena.

## EmailHelper

> Helper para el envio de correos electronicos via SMTP usando Gmail. Configura la sesion con autenticacion y TLS, y expone un metodo estatico para enviar mensajes en formato HTML.

```java
public static void enviar(String destinatario, String asunto, String cuerpoHtml)
```

Envia un correo electronico en formato HTML al destinatario indicado.

- **Param** `destinatario` - direccion de correo del receptor.
- **Param** `asunto` - asunto del mensaje.
- **Param** `cuerpoHtml` - contenido del mensaje en formato HTML.
- **Throws** `RuntimeException` - si ocurre un error durante el envio del correo.

---

## JwtHelper

> Helper para la generacion y validacion de tokens JWT. Maneja la firma con HMAC-SHA, la extraccion de claims y la verificacion de tokens en las peticiones autenticadas. El secreto se lee de la variable de entorno JWT_SECRET y tiene una duracion de 8 horas por token.

```java
public static String generarToken(int usuarioId, String username, int rolId)
```

Genera un token JWT firmado con los datos de identidad del usuario. El token incluye el ID como subject y username y rolId como claims adicionales.

- **Param** `usuarioId` - ID del usuario autenticado.
- **Param** `username` - nombre de usuario.
- **Param** `rolId` - ID del rol del usuario.
- **Returns** - token JWT compacto y firmado.

---

```java
public static Claims verificarToken(String token)
```

Verifica la firma del token y retorna los claims si es valido.

- **Param** `token` - token JWT a verificar.
- **Returns** - claims extraidos del payload del token.
- **Throws** `JwtException` - si el token esta malformado, expirado o la firma no coincide.

---

```java
public static int getUsuarioId(Claims claims)
```

Extrae el ID del usuario desde los claims del token.

- **Param** `claims` - claims obtenidos de un token verificado.
- **Returns** - ID del usuario como entero.

---

```java
public static String getUsername(Claims claims)
```

Extrae el username desde los claims del token.

- **Param** `claims` - claims obtenidos de un token verificado.
- **Returns** - nombre de usuario.

---

```java
public static int getRolId(Claims claims)
```

Extrae el ID de rol desde los claims del token.

- **Param** `claims` - claims obtenidos de un token verificado.
- **Returns** - ID del rol del usuario.

---

```java
public static boolean esValido(String token)
```

Valida un token JWT sin lanzar excepciones. Util para verificar rapidamente si un token es usable antes de procesarlo.

- **Param** `token` - token JWT a validar.
- **Returns** - true si el token es valido y no ha expirado; false en cualquier otro caso.

---

## PasswordHelper

> Helper para el hasheo y verificacion de contrasenas usando BCrypt. Utiliza un factor de costo de 12 rondas para el salt, lo que ofrece un balance adecuado entre seguridad y rendimiento.

```java
public static String hashear(String passwordPlano)
```

Genera el hash BCrypt de una contrasena en texto plano.

- **Param** `passwordPlano` - contrasena en texto plano a hashear.
- **Returns** - hash BCrypt listo para almacenar en base de datos.

---

```java
public static boolean verificar(String passwordPlano, String passwordHasheado)
```

Verifica si una contrasena en texto plano coincide con su hash BCrypt.

- **Param** `passwordPlano` - contrasena en texto plano ingresada por el usuario.
- **Param** `passwordHasheado` - hash BCrypt almacenado en base de datos.
- **Returns** - true si la contrasena coincide con el hash; false en caso contrario.

---

## PdfHelper

> Helper para la generacion de comprobantes y facturas de reservacion en PDF. Construye el documento con iText 7, incluyendo header y footer por pagina, tabla de habitaciones con subtotales, datos fiscales opcionales y bloque de terminos y condiciones.

```java
public static byte[] generarPdfReservacion(List<ReservacionDetalleDTO> detalles, Object[] factura)
```

Genera el PDF completo del comprobante o factura de una reservacion. Si se proporciona el arreglo de factura el documento incluye datos fiscales; de lo contrario se emite como comprobante simple.

- **Param** `detalles` - lista de DTOs con el detalle de cada habitacion.
- **Param** `factura` - arreglo [id, fechaEmision, nit, codigoPostal], o null si es comprobante.
- **Returns** - bytes del PDF generado listos para enviar como respuesta HTTP.

---

```java
public void handleEvent(Event event)
```

---

```java
public void handleEvent(Event event)
```

---

## TarjetaHelper

> Helper para la validacion de datos de tarjeta de credito o debito. Verifica formato del numero, nombre del titular, fecha de vencimiento y CVV antes de procesar cualquier pago.

```java
public static void validar(PagoRequestDTO request)
```

Valida los datos de tarjeta contenidos en el request de pago. Verifica que el numero tenga 16 digitos, el titular no este vacio, la fecha de vencimiento sea valida y no este expirada, y el CVV tenga 3 o 4 digitos. o la tarjeta esta vencida.

- **Param** `request` - DTO con los datos de la tarjeta a validar.
- **Throws** `IllegalArgumentException` - si cualquiera de los campos no cumple el formato esperado

---

## TokenHelper

> Helper para la generacion de tokens de autenticacion seguros. Usa SecureRandom para garantizar aleatoriedad criptografica.

```java
public static String generarTokenHash()
```

Genera un token aleatorio de 64 caracteres en formato hexadecimal. Se usa como token de acceso para agencias externas u otros casos donde se necesite un identificador unico e impredecible.

- **Returns** - string hexadecimal de 64 caracteres generado con SecureRandom.

---
