package repositories

import (
	"agencia-viajes/internal/dto"
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

// Obtener reservaciones pendientes de un usuario con sus detalles de proveedor
func (r *ReservacionRepository) ObtenerPendientesConDetalles(usuarioID int) ([]dto.ReservacionConDetalles, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT DISTINCT r.ID
		FROM Reservacion r
		WHERE r.Usuario_ID = ? AND r.EstadoID = 1
	`, usuarioID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var reservaciones []dto.ReservacionConDetalles
	for rows.Next() {
		var res dto.ReservacionConDetalles
		if err := rows.Scan(&res.ID); err != nil {
			return nil, err
		}
		reservaciones = append(reservaciones, res)
	}

	for i, res := range reservaciones {
		detalles, err := r.obtenerDetallesProveedor(conn, res.ID)
		if err != nil {
			return nil, err
		}
		reservaciones[i].Detalles = detalles
	}

	return reservaciones, nil
}

func (r *ReservacionRepository) obtenerDetallesProveedor(conn *sql.Conn, reservacionID int) ([]dto.DetalleProveedor, error) {
	rows, err := conn.QueryContext(context.Background(), `
		SELECT dr.ID_Reserva_Proveedor, p.ID, p.URL_API, p.Token_HASH_Entrada
		FROM Detalles_Reservacion dr
		JOIN Proveedor p ON dr.Proveedor_ID = p.ID
		WHERE dr.Reservacion_ID = ?
	`, reservacionID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var detalles []dto.DetalleProveedor
	for rows.Next() {
		var d dto.DetalleProveedor
		if err := rows.Scan(&d.IDReservaProveedor, &d.ProveedorID, &d.URLAPI, &d.TokenEntrada); err != nil {
			return nil, err
		}
		detalles = append(detalles, d)
	}
	return detalles, nil
}

func (r *ReservacionRepository) ExpirarReservacion(reservacionID int) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	const estadoExpirada = 4
	const estadoPendiente = 1

	_, err = conn.ExecContext(context.Background(), `
        UPDATE Reservacion SET EstadoID = ? 
        WHERE ID = ? AND EstadoID = ?`,
		estadoExpirada, reservacionID, estadoPendiente,
	)
	return err
}

func (r *ReservacionRepository) ExpirarDetalles(reservacionID int) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	const estadoCancelado = 3
	_, err = conn.ExecContext(context.Background(), `
		UPDATE Detalles_Reservacion SET Estado_Detalle_ID = ? WHERE Reservacion_ID = ?`,
		estadoCancelado, reservacionID,
	)
	return err
}

func (r *ReservacionRepository) ObtenerIDsPendientesExpirados() ([]int, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT ID FROM Reservacion
		WHERE EstadoID = 1 AND Fecha_Expiracion < NOW()
	`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var ids []int
	for rows.Next() {
		var id int
		if err := rows.Scan(&id); err != nil {
			return nil, err
		}
		ids = append(ids, id)
	}
	return ids, nil
}

func (r *ReservacionRepository) ObtenerDetallesDeReservacion(reservacionID int) ([]dto.DetalleProveedor, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT dr.ID_Reserva_Proveedor, p.ID, p.URL_API, p.Token_HASH_Entrada
		FROM Detalles_Reservacion dr
		JOIN Proveedor p ON dr.Proveedor_ID = p.ID
		WHERE dr.Reservacion_ID = ?
	`, reservacionID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var detalles []dto.DetalleProveedor
	for rows.Next() {
		var d dto.DetalleProveedor
		if err := rows.Scan(&d.IDReservaProveedor, &d.ProveedorID, &d.URLAPI, &d.TokenEntrada); err != nil {
			return nil, err
		}
		detalles = append(detalles, d)
	}
	return detalles, nil
}
