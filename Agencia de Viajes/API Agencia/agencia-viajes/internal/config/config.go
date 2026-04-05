// # Package config
//
// Gestiona la configuracion de la aplicacion Movent.
// Lee las variables de entorno necesarias para la conexion a la base
// de datos y el servidor HTTP, con soporte para archivo .env mediante
// la libreria godotenv.
package config

import (
	"log"
	"os"

	"github.com/joho/godotenv"
)

// Config
//
// Contiene todos los parametros de configuracion necesarios para
// iniciar la aplicacion: credenciales de la base de datos MySQL,
// puerto del servidor HTTP y URL base del servidor.
type Config struct {
	DBHost     string
	DBPort     string
	DBUser     string
	DBPassword string
	DBName     string
	ServerPort string
	ServerURL  string
}

// Load
//
// Carga la configuracion desde variables de entorno. Intenta leer
// un archivo .env en el directorio de trabajo; si no existe, utiliza
// directamente las variables del sistema operativo.
//
// Retorna:
//   - *Config: puntero al struct poblado con los valores del entorno
//
// Notas:
//   - Si el archivo .env no se encuentra solo se registra un aviso
//     en el log; la funcion no falla por esa causa
func Load() *Config {
	err := godotenv.Load()
	if err != nil {
		log.Println("No se encontró .env, usando variables del sistema")
	}

	return &Config{
		DBHost:     os.Getenv("DB_HOST"),
		DBPort:     os.Getenv("DB_PORT"),
		DBUser:     os.Getenv("DB_USER"),
		DBPassword: os.Getenv("DB_PASSWORD"),
		DBName:     os.Getenv("DB_NAME"),
		ServerPort: os.Getenv("SERVER_PORT"),
		ServerURL:  os.Getenv("SERVER_URL"),
	}
}
