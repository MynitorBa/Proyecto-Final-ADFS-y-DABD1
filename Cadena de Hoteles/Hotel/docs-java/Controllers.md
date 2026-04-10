# Controllers

## AdminBusquedaController

> Controller para el modulo de reportes de busquedas del panel de administracion. Expone endpoints para listar busquedas con filtros, obtener un resumen estadistico y exportar el reporte por correo. Solo accesible para usuarios con rol de administrador.

```java
public void registerRoutes(Javalin app)
```

Registra las rutas del modulo de reportes de busquedas en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## AerolineaAdminController

> Controller que registra las rutas HTTP de aerolineas aliadas para el panel de administracion. Expone endpoints exclusivamente para el rol Administrador (rol 2). Tambien expone el endpoint de usuarios webservice libres, usado al crear entidades.

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas de administracion de aerolineas en la aplicacion Javalin. Todas las rutas requieren rol Administrador (rol 2).

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## AerolineaWebserviceController

> Controller que registra las rutas HTTP relacionadas con aerolineas aliadas para el portal webservice. Expone endpoints exclusivamente para el rol Webservice (rol 3).

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas de aerolineas aliadas para el webservice en la aplicacion Javalin. Todas las rutas requieren rol Webservice (rol 3).

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## AgenciaController

> Controller que registra las rutas HTTP relacionadas con agencias. Expone endpoints para el rol Webservice (rol 3) y para el rol Administrador (rol 2).

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas de agencias en la aplicacion Javalin. Las rutas bajo /webservice requieren rol 3 y las de /admin requieren rol 2.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## AuthController

> Controller que maneja la autenticacion de usuarios. Expone endpoints para login y logout usando cookies HttpOnly como mecanismo de sesion.

```java
public void registerRoutes(Javalin app)
```

Registra las rutas de autenticacion en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## BusquedaAerolineaController

> Controller que expone el endpoint de busqueda para aerolineas aliadas. Las peticiones se autentican mediante el header X-Aerolinea-Token.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de busqueda de aerolineas en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## BusquedaAgenciaController

> Controller que expone el endpoint de busqueda para agencias externas. Las peticiones se autentican mediante el header X-Agencia-Token.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de busqueda de agencias en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## BusquedaController

> Controller que expone el endpoint publico de busqueda de vuelos. Si el usuario tiene sesion activa, asocia el usuarioId a la busqueda; de lo contrario la procesa de forma anonima.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de busqueda en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## CancelacionAgenciaController

> Controller que expone los endpoints de cancelacion de reservaciones para agencias externas. Todas las rutas requieren autenticacion mediante el header X-Agencia-Token.

```java
public void registerRoutes(Javalin app)
```

Registra las rutas de cancelacion de agencias en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## CancelacionController

> Controller que expone el endpoint de cancelacion de reservaciones para usuarios autenticados. Requiere sesion activa; el usuarioId se obtiene del contexto inyectado por el middleware JWT.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de cancelacion en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## ComentarioController

> Controller que gestiona los endpoints relacionados con comentarios de hoteles. Expone rutas publicas, rutas protegidas por sesion de usuario y rutas para agencias externas.

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas de comentarios en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## DestinosController

> Controller que expone el endpoint publico de destinos disponibles. No requiere autenticacion; retorna hoteles activos con sus imagenes.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de destinos en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## DownsController

> Controller que gestiona las valoraciones negativas (downs) sobre comentarios. Todas las rutas requieren sesion activa; el usuarioId se obtiene del contexto JWT.

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas de downs en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## EmailReservacionController

> Controller que gestiona el envio de correos electronicos relacionados con reservaciones, formulario de contacto y suscripciones al boletin informativo.

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas de correo en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## HandshakeAerolineaController

> Controller que expone el endpoint publico de handshake para aerolineas aliadas. Permite a una aerolinea externa autenticarse ante el sistema hotelero presentando su URL y un token de entrada, y recibir un token de sesion para sus comunicaciones posteriores. El endpoint no requiere autenticacion previa ya que es el primer punto de contacto.

```java
public void registerRoutes(Javalin app)
```

Registra las rutas publicas del handshake de aerolineas en el servidor Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## HotelAgenciaController

> Controller que expone el endpoint de consulta de hoteles para agencias externas. Requiere autenticacion mediante el header X-Agencia-Token.

```java
public void registrarRutas(Javalin app)
```

Registra la ruta de hoteles para agencias en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## HotelController

> Controller que centraliza la administracion de hoteles, habitaciones, amenidades, imagenes, reservaciones y metricas del sistema. Todas las rutas requieren rol Administrador (rol 2).

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas administrativas en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## ImagenController

> Controller que expone los endpoints publicos de descarga de imagenes. Sirve imagenes de hoteles, habitaciones y amenidades en formato JPEG.

```java
public void registerRoutes(Javalin app)
```

Registra las rutas de imagenes en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## PagoAgenciaController

> Controller que gestiona el procesamiento de pagos de reservaciones realizadas por agencias externas. Requiere autenticacion mediante el header X-Agencia-Token.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de pago de agencias en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## PagoController

> Controller que gestiona el procesamiento de pagos de reservaciones para usuarios autenticados. Requiere sesion activa; el usuarioId se obtiene del contexto inyectado por el middleware JWT.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de pago en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## PdfReservacionController

> Controller que gestiona la descarga del comprobante PDF de una reservacion. Requiere sesion activa; el usuarioId se obtiene del contexto inyectado por el middleware JWT.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de descarga de PDF en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## ReservacionAgenciaController

> Controller que gestiona las reservaciones realizadas por agencias externas. Todas las rutas requieren autenticacion mediante el header X-Agencia-Token.

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas de reservaciones de agencias en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## ReservacionController

> Controller que gestiona las reservaciones de usuarios autenticados. Requiere sesion activa; el usuarioId se obtiene del contexto inyectado por el middleware JWT.

```java
public void registerRoutes(Javalin app)
```

Registra las rutas de reservaciones en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---

## SesionController

> Controller que expone el endpoint de consulta del estado de sesion actual. Es publico para que el frontend pueda verificar la sesion sin importar si hay token o no.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de sesion en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## TokenAerolineaController

> Controller que expone el endpoint de generacion de tokens de alianza. Solo aerolineas autenticadas mediante X-Aerolinea-Token pueden acceder.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de generacion de tokens en la aplicacion Javalin. <p>Endpoint: POST /aerolinea/token</p> <p>Header requerido: X-Aerolinea-Token</p> <p>Body esperado: { "ciudad": "Paris", "pais": "Francia" }</p> <p>Respuesta exitosa 201: token generado, URL de redireccion y fecha de expiracion.</p>

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## TokenValidacionController

> Controller que expone el endpoint de validacion de tokens de alianza. Requiere sesion activa del usuario; el token de alianza se recibe como query parameter en la URL.

```java
public void registerRoutes(Javalin app)
```

Registra la ruta de validacion de tokens en la aplicacion Javalin. <p>Endpoint: GET /alianza/validar?token=uuid</p> <p>Requiere JWT activo en el header Authorization.</p> <p>Respuesta exitosa 200: ciudad, pais, porcentaje de descuento y fecha de expiracion.</p> <p>Respuesta 400: si el token no existe, ya fue usado o expiro.</p>

- **Param** `app` - instancia de Javalin donde se registra la ruta.

---

## UsuarioController

> Controller que gestiona las operaciones sobre usuarios del sistema. Expone rutas publicas de registro y validacion, rutas privadas para el perfil del usuario autenticado, y rutas administrativas exclusivas para rol 2.

```java
public void registerRoutes(Javalin app)
```

Registra todas las rutas de usuarios en la aplicacion Javalin.

- **Param** `app` - instancia de Javalin donde se registran las rutas.

---
