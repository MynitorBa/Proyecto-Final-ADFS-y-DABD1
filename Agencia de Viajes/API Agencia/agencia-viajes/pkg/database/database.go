// # Package database
//
// Provee la conexion a la base de datos MySQL de Movent.
// Expone una unica funcion Connect que construye el DSN a partir
// de la configuracion de la aplicacion y retorna una instancia
// verificada de *sql.DB lista para ser usada por los repositorios.
package database

import (
	"agencia-viajes/internal/config"
	"database/sql"
	"fmt"
	"log"

	_ "github.com/go-sql-driver/mysql"
)

// Connect
//
// Establece y verifica la conexion a MySQL usando la configuracion provista.
// Construye el DSN con los campos del struct Config, abre la conexion y
// ejecuta un Ping para confirmar disponibilidad antes de retornar.
//
// Parametros:
//   - cfg: puntero a la configuracion con credenciales y host de la BD
//
// Retorna:
//   - *sql.DB: instancia de la conexion lista para usar
//
// Notas:
//   - Termina el proceso con log.Fatal si la apertura de la conexion falla
//   - Termina el proceso con log.Fatal si el Ping a la base de datos falla
func Connect(cfg *config.Config) *sql.DB {
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?parseTime=true",
		cfg.DBUser,
		cfg.DBPassword,
		cfg.DBHost,
		cfg.DBPort,
		cfg.DBName,
	)

	db, err := sql.Open("mysql", dsn)
	if err != nil {
		log.Fatal("Error al abrir la conexión: ", err)
	}

	if err := db.Ping(); err != nil {
		log.Fatal("Error al conectar con la BD: ", err)
	}

	log.Println("Conexión a MySQL exitosa")
	return db
}
