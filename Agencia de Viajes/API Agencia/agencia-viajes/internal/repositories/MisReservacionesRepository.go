package repositories

import (
	"agencia-viajes/internal/dto"
	"context"
	"database/sql"
)

type MisReservacionesRepository struct {
	db *sql.DB
}

func NewMisReservacionesRepository(db *sql.DB) *MisReservacionesRepository {
	return &MisReservacionesRepository{db: db}
}

// ObtenerReservacionesDeUsuario — trae todas las reservaciones con sus detalles locales
func (r *MisReservacionesRepository) ObtenerReservacionesDeUsuario(usuarioID int) ([]dto.FilaReservacionDetalle, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT 
			r.ID, r.No_Reservacion, r.Tipo_Reserva_ID, r.EstadoID,
			r.Total, r.Fecha_Creacion, r.Fecha_Expiracion,
			dr.ID, dr.Tipo_Detalle_ID, dr.ID_Reserva_Proveedor,
			dr.Total, dr.Estado_Detalle_ID, dr.Parametros_Json,
			p.ID, p.URL_API, p.Token_HASH_Entrada
		FROM Reservacion r
		JOIN Detalles_Reservacion dr ON dr.Reservacion_ID = r.ID
		JOIN Proveedor p             ON p.ID = dr.Proveedor_ID
		WHERE r.Usuario_ID = ?
		ORDER BY r.Fecha_Creacion DESC, r.ID, dr.ID
	`, usuarioID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	return escanearFilas(rows)
}

// ObtenerReservacionPorID — trae una reservación específica verificando que sea del usuario
func (r *MisReservacionesRepository) ObtenerReservacionPorID(reservacionID, usuarioID int) ([]dto.FilaReservacionDetalle, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	rows, err := conn.QueryContext(context.Background(), `
		SELECT 
			r.ID, r.No_Reservacion, r.Tipo_Reserva_ID, r.EstadoID,
			r.Total, r.Fecha_Creacion, r.Fecha_Expiracion,
			dr.ID, dr.Tipo_Detalle_ID, dr.ID_Reserva_Proveedor,
			dr.Total, dr.Estado_Detalle_ID, dr.Parametros_Json,
			p.ID, p.URL_API, p.Token_HASH_Entrada
		FROM Reservacion r
		JOIN Detalles_Reservacion dr ON dr.Reservacion_ID = r.ID
		JOIN Proveedor p             ON p.ID = dr.Proveedor_ID
		WHERE r.ID = ? AND r.Usuario_ID = ?
		ORDER BY dr.ID
	`, reservacionID, usuarioID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	return escanearFilas(rows)
}

// escanearFilas — helper compartido para leer las filas del resultado
func escanearFilas(rows *sql.Rows) ([]dto.FilaReservacionDetalle, error) {
	var filas []dto.FilaReservacionDetalle
	for rows.Next() {
		var f dto.FilaReservacionDetalle
		err := rows.Scan(
			&f.ReservacionID, &f.NoReservacion, &f.TipoReservaID, &f.EstadoID,
			&f.Total, &f.FechaCreacion, &f.FechaExpiracion,
			&f.DetalleID, &f.TipoDetalleID, &f.IDReservaProveedor,
			&f.DetalleTotal, &f.EstadoDetalleID, &f.ParametrosJson,
			&f.ProveedorID, &f.URLAPI, &f.TokenEntrada,
		)
		if err != nil {
			return nil, err
		}
		filas = append(filas, f)
	}
	return filas, nil
}
