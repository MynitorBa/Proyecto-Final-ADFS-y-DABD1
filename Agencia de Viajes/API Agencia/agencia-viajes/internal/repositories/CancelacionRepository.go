package repositories

import (
	"agencia-viajes/internal/dto"
	"context"
	"database/sql"
	"fmt"
)

type CancelacionRepository struct {
	db *sql.DB
}

func NewCancelacionRepository(db *sql.DB) *CancelacionRepository {
	return &CancelacionRepository{db: db}
}

// ObtenerReservacionParaCancelar — verifica que existe, pertenece al usuario y está en estado cancelable
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

// ObtenerDetallesParaCancelar — trae los detalles con datos del proveedor
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

// CancelarReservacion — marca la reservación y sus detalles como cancelados en transacción
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
