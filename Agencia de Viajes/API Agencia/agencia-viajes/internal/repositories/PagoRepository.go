package repositories

import (
	"context"
	"database/sql"
)

type PagoRepository struct {
	db *sql.DB
}

func NewPagoRepository(db *sql.DB) *PagoRepository {
	return &PagoRepository{db: db}
}

// ObtenerDetallesParaPago verifica que la reserva sea del usuario y esté pendiente
func (r *PagoRepository) ObtenerReservaParaPago(reservacionID, usuarioID int) (tipoReserva int, total float64, err error) {
	err = r.db.QueryRow(`
		SELECT Tipo_Reserva_ID, Total 
		FROM Reservacion 
		WHERE ID = ? AND Usuario_ID = ? AND EstadoID = 1`,
		reservacionID, usuarioID).Scan(&tipoReserva, &total)
	return
}

// ContarDetallesPorTipo cuenta cuántos detalles de cada tipo tiene la reserva
func (r *PagoRepository) ContarDetallesPorTipo(reservacionID int) (vuelos int, hoteles int, err error) {
	query := `
		SELECT 
			SUM(CASE WHEN Tipo_Detalle_ID = 1 THEN 1 ELSE 0 END) as vuelos,
			SUM(CASE WHEN Tipo_Detalle_ID = 2 THEN 1 ELSE 0 END) as hoteles
		FROM Detalles_Reservacion 
		WHERE Reservacion_ID = ? AND Estado_Detalle_ID = 1`

	err = r.db.QueryRow(query, reservacionID).Scan(&vuelos, &hoteles)
	return
}

// ConfirmarReservaYFacturar realiza el cambio de estado y crea la factura en una transacción
func (r *PagoRepository) ConfirmarReservaYFacturar(reservacionID int, total float64, nit string, codigoPostal string) error {

	tx, err := r.db.BeginTx(context.Background(), nil)
	if err != nil {
		return err
	}

	// 1. Cambiar estado a Confirmada (2)
	_, err = tx.Exec("UPDATE Reservacion SET EstadoID = 2 WHERE ID = ?", reservacionID)
	if err != nil {
		tx.Rollback()
		return err
	}

	// 2. Cambiar estado de detalles a Confirmado (2)
	_, err = tx.Exec("UPDATE Detalles_Reservacion SET Estado_Detalle_ID = 2 WHERE Reservacion_ID = ?", reservacionID)
	if err != nil {
		tx.Rollback()
		return err
	}

	// 3. Crear Factura (Ajusta los nombres de columnas según tu tabla Factura)
	_, err = tx.Exec(`
		INSERT INTO Factura (Reservacion_ID, NIT, Total, Codigo_Postal) 
		VALUES (?, ?, ?, ?)`,
		reservacionID, nit, total, codigoPostal)
	if err != nil {
		tx.Rollback()
		return err
	}

	return tx.Commit()
}
