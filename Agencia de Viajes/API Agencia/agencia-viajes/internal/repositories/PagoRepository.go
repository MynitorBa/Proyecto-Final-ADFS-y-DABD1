// # Package repositories
//
// Repositorios de acceso a datos para la agencia de viajes.
// Este paquete centraliza todas las consultas a la base de datos
// utilizadas por los servicios de la aplicacion.
package repositories

import (
	"context"
	"database/sql"
	"math"
)

// PagoRepository
//
// Repositorio encargado de las operaciones relacionadas con el proceso de pago,
// incluyendo la validacion de la reserva, el conteo de detalles por tipo y
// la confirmacion atomica de la reserva junto con la creacion de la factura.
type PagoRepository struct {
	db *sql.DB
}

// NewPagoRepository
//
// Crea e inicializa una nueva instancia de PagoRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *PagoRepository: instancia lista para usar
func NewPagoRepository(db *sql.DB) *PagoRepository {
	return &PagoRepository{db: db}
}

// ObtenerReservaParaPago
//
// Verifica que la reserva pertenezca al usuario indicado y se encuentre
// en estado pendiente (EstadoID = 1), retornando el tipo de reserva y el total.
//
// Parametros:
//   - reservacionID: ID de la reservacion a consultar
//   - usuarioID: ID del usuario propietario de la reservacion
//
// Retorna:
//   - tipoReserva: ID del tipo de reserva (vuelo, hotel, paquete, etc.)
//   - total: monto total acumulado de la reservacion
//   - error: error si la reserva no existe, no pertenece al usuario o no esta pendiente
func (r *PagoRepository) ObtenerReservaParaPago(reservacionID, usuarioID int) (tipoReserva int, total float64, err error) {
	err = r.db.QueryRow(`
		SELECT Tipo_Reserva_ID, Total
		FROM Reservacion
		WHERE ID = ? AND Usuario_ID = ? AND EstadoID = 1`,
		reservacionID, usuarioID).Scan(&tipoReserva, &total)
	return
}

// ContarDetallesPorTipo
//
// Cuenta cuantos detalles de tipo vuelo (1) y tipo hotel (2) tiene una reservacion,
// considerando unicamente los detalles en estado pendiente (Estado_Detalle_ID = 1).
//
// Parametros:
//   - reservacionID: ID de la reservacion a evaluar
//
// Retorna:
//   - vuelos: cantidad de detalles de tipo vuelo pendientes
//   - hoteles: cantidad de detalles de tipo hotel pendientes
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ConfirmarReservaYFacturar
//
// Ejecuta dentro de una transaccion atomica los pasos del proceso de confirmacion:
// aplica el descuento de paquete si corresponde, actualiza el total de la reservacion,
// cambia el estado a confirmada (2), confirma todos sus detalles y crea la factura.
// El descuento es absorbido por la agencia y se aplica unicamente a reservaciones
// de tipo paquete (Tipo_Reserva_ID = 3).
//
// Parametros:
//   - reservacionID: ID de la reservacion a confirmar
//   - total: monto total original de la reservacion antes del descuento
//   - nit: numero de identificacion tributaria del cliente para la factura
//   - codigoPostal: codigo postal del cliente para la factura
//   - porcentajeDescuento: porcentaje de descuento a aplicar (0 si no aplica)
//
// Retorna:
//   - error: error si alguna operacion de la transaccion falla, nil si fue exitosa
//
// Notas:
//   - Si cualquier paso falla, se realiza rollback automatico de toda la transaccion
//   - El descuento se redondea a 2 decimales antes de aplicarse
func (r *PagoRepository) ConfirmarReservaYFacturar(reservacionID int, total float64, nit string, codigoPostal string, porcentajeDescuento float64) error {

	// Aplicar descuento si corresponde
	totalFinal := total
	if porcentajeDescuento > 0 {
		descuento := math.Round(total*(porcentajeDescuento/100)*100) / 100
		totalFinal = math.Round((total-descuento)*100) / 100
	}

	tx, err := r.db.BeginTx(context.Background(), nil)
	if err != nil {
		return err
	}

	// 1. Actualizar el total de la reservacion con el descuento aplicado
	_, err = tx.Exec("UPDATE Reservacion SET EstadoID = 2, Total = ? WHERE ID = ?", totalFinal, reservacionID)
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

	// 3. Crear Factura con el total final ya descontado
	_, err = tx.Exec(`
		INSERT INTO Factura (Reservacion_ID, NIT, Total, Codigo_Postal)
		VALUES (?, ?, ?, ?)`,
		reservacionID, nit, totalFinal, codigoPostal)
	if err != nil {
		tx.Rollback()
		return err
	}

	return tx.Commit()
}