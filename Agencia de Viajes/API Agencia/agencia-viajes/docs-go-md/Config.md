# Config


# Package config

Gestiona la configuracion de la aplicacion Movent. Lee las variables de entorno
necesarias para la conexion a la base de datos y el servidor HTTP, con soporte
para archivo .env mediante la libreria godotenv.


## TYPES

```go

type Config struct {
	DBHost     string
	DBPort     string
	DBUser     string
	DBPassword string
	DBName     string
	ServerPort string
	ServerURL  string
}
    Config

    Contiene todos los parametros de configuracion necesarios para iniciar la
    aplicacion: credenciales de la base de datos MySQL, puerto del servidor HTTP
    y URL base del servidor.

func Load() *Config
    Load

    Carga la configuracion desde variables de entorno. Intenta leer un archivo
    .env en el directorio de trabajo; si no existe, utiliza directamente las
    variables del sistema operativo.

    Retorna:
      - *Config: puntero al struct poblado con los valores del entorno

    Notas:
      - Si el archivo .env no se encuentra solo se registra un aviso en el log;
        la funcion no falla por esa causa

```
