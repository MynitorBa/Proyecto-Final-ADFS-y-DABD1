package database

import (
	"agencia-viajes/internal/config"
	"database/sql"
	"fmt"
	"log"

	_ "github.com/go-sql-driver/mysql"
)

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
