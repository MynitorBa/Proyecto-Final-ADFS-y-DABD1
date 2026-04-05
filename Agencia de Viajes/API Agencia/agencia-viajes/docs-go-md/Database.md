# Database


# Package database

Provee la conexion a la base de datos MySQL de Movent. Expone una unica funcion
Connect que construye el DSN a partir de la configuracion de la aplicacion
y retorna una instancia verificada de *sql.DB lista para ser usada por los
repositorios.


## FUNCTIONS

```go

func Connect(cfg *config.Config) *sql.DB
    Connect

    Establece y verifica la conexion a MySQL usando la configuracion provista.
    Construye el DSN con los campos del struct Config, abre la conexion y
    ejecuta un Ping para confirmar disponibilidad antes de retornar.

    Parametros:
      - cfg: puntero a la configuracion con credenciales y host de la BD

    Retorna:
      - *sql.DB: instancia de la conexion lista para usar

    Notas:
      - Termina el proceso con log.Fatal si la apertura de la conexion falla
      - Termina el proceso con log.Fatal si el Ping a la base de datos falla

```
