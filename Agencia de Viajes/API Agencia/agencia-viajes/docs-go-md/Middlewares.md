# Middlewares


# Package middlewares

Contiene los middlewares HTTP de la aplicacion Movent. Provee funciones de
verificacion de autenticacion JWT, control de roles y validacion de tokens de
proveedor para proteger las rutas de la API REST.






## FUNCTIONS

```go

func AuthRequerido() gin.HandlerFunc
    AuthRequerido

    Retorna un middleware de Gin que verifica que la solicitud incluya una
    cookie de sesion con un JWT valido. Si la cookie no existe o el token
    es invalido o esta expirado, la solicitud es rechazada con HTTP 401.
    Si la autenticacion es exitosa, inyecta los datos del usuario (usuario_id,
    username, rol_id) en el contexto de Gin para que los controladores puedan
    usarlos.

    Retorna:
      - gin.HandlerFunc: funcion de middleware lista para usar con router.Use

func ProveedorRequerido(db *sql.DB) gin.HandlerFunc
    ProveedorRequerido

    Retorna un middleware de Gin que valida el token de proveedor enviado en
    el encabezado HTTP X-Proveedor-Token. Busca el proveedor correspondiente
    en la base de datos mediante el repositorio. Si el token esta ausente,
    no se encuentra o produce un error, rechaza la solicitud con HTTP 401 o 500
    segun corresponda. Si es valido, inyecta proveedor_id, proveedor_nombre y
    proveedor_tipo en el contexto de Gin.

    Parametros:
      - db: conexion activa a la base de datos para consultar el proveedor


func RolRequerido(roles ...int) gin.HandlerFunc
    RolRequerido

    Retorna un middleware de Gin que verifica que el usuario autenticado tenga
    uno de los roles permitidos indicados como argumentos. Lee el rol del
    contexto de Gin (previamente inyectado por AuthRequerido) y compara con los
    roles aceptados. Si el rol no coincide con ninguno, rechaza la solicitud con
    HTTP 403.

      - roles: uno o mas identificadores de rol que tienen acceso permitido


    Notas:
      - Debe usarse siempre despues de AuthRequerido en la cadena de middlewares

```
