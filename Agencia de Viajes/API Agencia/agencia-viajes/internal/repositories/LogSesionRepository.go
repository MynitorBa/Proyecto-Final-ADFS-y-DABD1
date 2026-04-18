// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"agencia-viajes/internal/models"
	"context"
	"database/sql"
)

// LogSesionRepository
//
// Repositorio encargado de las operaciones de escritura sobre la tabla
// log_sesion. Permite insertar registros de eventos de autenticacion
// (login y registro) para auditoria y trazabilidad.
type LogSesionRepository struct {
	db *sql.DB
}

// NewLogSesionRepository
//
// Crea e inicializa una nueva instancia de LogSesionRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *LogSesionRepository: instancia lista para usar
func NewLogSesionRepository(db *sql.DB) *LogSesionRepository {
	return &LogSesionRepository{db: db}
}

// Insertar
//
// Inserta un nuevo registro de evento de sesion en la tabla log_sesion.
// El campo Fecha no se incluye en el INSERT; MySQL aplica el valor
// DEFAULT current_timestamp() automaticamente.
//
// Parametros:
//   - entry: struct LogSesion con los datos del evento a registrar
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *LogSesionRepository) Insertar(entry models.LogSesion) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(), `
		INSERT INTO log_sesion
			(Tipo_Evento_ID, Usuario_ID, Login_Intentado, Exitoso, IP_Origen, User_Agent, Mensaje)
		VALUES (?, ?, ?, ?, ?, ?, ?)`,
		entry.TipoEventoID,
		entry.UsuarioID,
		entry.LoginIntentado,
		entry.Exitoso,
		entry.IPOrigen,
		entry.UserAgent,
		entry.Mensaje,
	)
	return err
}
