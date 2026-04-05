// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"context"
	"database/sql"
)

// AgenciaConfiguracionRepository
//
// Repositorio que gestiona la lectura de la configuracion global
// de la agencia, incluyendo el porcentaje de descuento aplicado
// a reservaciones de tipo paquete (vuelo + hotel).
type AgenciaConfiguracionRepository struct {
	db *sql.DB
}

// NewAgenciaConfiguracionRepository
//
// Crea e inicializa una nueva instancia de AgenciaConfiguracionRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *AgenciaConfiguracionRepository: instancia lista para usar
func NewAgenciaConfiguracionRepository(db *sql.DB) *AgenciaConfiguracionRepository {
	return &AgenciaConfiguracionRepository{db: db}
}

// ObtenerPorcentajeDescuento
//
// Retorna el porcentaje de descuento configurado para paquetes.
// Lee siempre el primer registro de la tabla Agencia_Configuracion.
// Si la tabla esta vacia o falla la consulta, retorna 0.
//
// Retorna:
//   - float64: porcentaje de descuento (ej. 5.00 = 5%)
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *AgenciaConfiguracionRepository) ObtenerPorcentajeDescuento() (float64, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, err
	}
	defer conn.Close()

	var porcentaje float64
	err = conn.QueryRowContext(context.Background(), `
		SELECT Porcentaje_Descuento
		FROM Agencia_Configuracion
		ORDER BY ID ASC
		LIMIT 1
	`).Scan(&porcentaje)

	if err == sql.ErrNoRows {
		return 0, nil
	}
	return porcentaje, err
}