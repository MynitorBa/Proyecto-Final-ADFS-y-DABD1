package repositories

import (
	"context"
	"database/sql"
)

type ReservacionRepository struct {
	db *sql.DB
}

func NewReservacionRepository(db *sql.DB) *ReservacionRepository {
	return &ReservacionRepository{db: db}
}

func (r *ReservacionRepository) CrearReservacion(
	usuarioID int,
	tipoReservaID int,
	noReservacion string,
	fechaExpiracion string,
) (int, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, err
	}
	defer conn.Close()

	const estadoPendiente = 1

	result, err := conn.ExecContext(context.Background(), `
		INSERT INTO Reservacion 
			(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Expiracion, Fecha_Creacion, Tipo_Reserva_ID)
		VALUES (?, 0, ?, ?, ?, NOW(), ?)`,
		noReservacion, estadoPendiente, usuarioID, fechaExpiracion, tipoReservaID,
	)
	if err != nil {
		return 0, err
	}

	id, _ := result.LastInsertId()
	return int(id), nil
}

func (r *ReservacionRepository) ExpirarReservacionesPendientes() error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	const estadoPendiente = 1
	const estadoExpirada = 4

	_, err = conn.ExecContext(context.Background(), `
		UPDATE Reservacion
		SET EstadoID = ?
		WHERE EstadoID = ?
		  AND Fecha_Expiracion < NOW()`,
		estadoExpirada, estadoPendiente,
	)
	return err
}
