// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"agencia-viajes/internal/dto"
	"context"
	"database/sql"
	"fmt"
)

// CancelacionRepository
//
// Repositorio que gestiona las operaciones de cancelacion de reservaciones,
// incluyendo la verificacion de pertenencia al usuario y la actualizacion
// del estado en base de datos mediante transacciones.
type CancelacionRepository struct {
	db *sql.DB
}

// NewCancelacionRepository
//
// Crea e inicializa una nueva instancia de CancelacionRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *CancelacionRepository: instancia lista para usar
func NewCancelacionRepository(db *sql.DB) *CancelacionRepository {
	return &CancelacionRepository{db: db}
}

// ObtenerReservacionParaCancelar
//
// Verifica que una reservacion exista, pertenezca al usuario indicado
// y retorna su estado actual para validar si es cancelable.
//
// Parametros:
//   - reservacionID: ID de la reservacion a consultar
//   - usuarioID: ID del usuario que solicita la cancelacion
//
// Retorna:
//   - estadoID: identificador del estado actual de la reservacion
//   - error: error si la reservacion no existe, no pertenece al usuario o falla la consulta
func (r *CancelacionRepository) ObtenerReservacionParaCancelar(reservacionID, usuarioID int) (estadoID int, err error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, err
	}
	defer conn.Close()

	err = conn.QueryRowContext(context.Background(), `
		SELECT EstadoID FROM Reservacion
		WHERE ID = ? AND Usuario_ID = ?
	`, reservacionID, usuarioID).Scan(&estadoID)

	if err == sql.ErrNoRows {
		return 0, fmt.Errorf("reservación no encontrada o no pertenece al usuario")
	}
	return
}

// ObtenerDetallesParaCancelar
//
// Recupera los detalles de una reservacion junto con los datos del proveedor
// asociado a cada detalle. Solo incluye detalles con estado pendiente (1) o confirmado (2).
//
// Parametros:
//   - reservacionID: ID de la reservacion cuyos detalles se desean obtener
//
// Retorna:
//   - []dto.DetalleProveedor: lista de detalles con informacion del proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *CancelacionRepository) ObtenerDetallesParaCancelar(reservacionID int) ([]dto.DetalleProveedor, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT dr.ID_Reserva_Proveedor, p.ID, p.URL_API, p.Token_HASH_Entrada, dr.Tipo_Detalle_ID
		FROM Detalles_Reservacion dr
		JOIN Proveedor p ON dr.Proveedor_ID = p.ID
		WHERE dr.Reservacion_ID = ?
		  AND dr.Estado_Detalle_ID IN (1, 2)
	`, reservacionID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var detalles []dto.DetalleProveedor
	for rows.Next() {
		var d dto.DetalleProveedor
		if err := rows.Scan(&d.IDReservaProveedor, &d.ProveedorID, &d.URLAPI, &d.TokenEntrada, &d.TipoDetalleID); err != nil {
			return nil, err
		}
		detalles = append(detalles, d)
	}
	return detalles, nil
}

// CancelarReservacion
//
// Marca la reservacion y todos sus detalles activos como cancelados (estado 3)
// dentro de una transaccion atomica. Ademas registra la fecha de cancelacion
// y el motivo proporcionado.
//
// Parametros:
//   - reservacionID: ID de la reservacion a cancelar
//   - motivo: descripcion del motivo de cancelacion
//
// Retorna:
//   - error: error si alguna operacion de la transaccion falla, nil si fue exitosa
//
// Notas:
//   - Si cualquier paso falla, se realiza rollback automatico
func (r *CancelacionRepository) CancelarReservacion(reservacionID int, motivo string) error {
	tx, err := r.db.BeginTx(context.Background(), nil)
	if err != nil {
		return err
	}

	// 1. Cancelar detalles (estado 3)
	_, err = tx.Exec(`
		UPDATE Detalles_Reservacion SET Estado_Detalle_ID = 3
		WHERE Reservacion_ID = ? AND Estado_Detalle_ID IN (1, 2)
	`, reservacionID)
	if err != nil {
		tx.Rollback()
		return err
	}

	// 2. Cancelar reservación (estado 3)
	_, err = tx.Exec(`
		UPDATE Reservacion
		SET EstadoID = 3, Fecha_Cancelacion = NOW(), Motivo_Cancelacion = ?
		WHERE ID = ?
	`, motivo, reservacionID)
	if err != nil {
		tx.Rollback()
		return err
	}

	return tx.Commit()
}
