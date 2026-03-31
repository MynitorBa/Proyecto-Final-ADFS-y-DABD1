package repositories

import (
	"agencia-viajes/internal/dto"
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
)

type DetalleReservacionRepository struct {
	db *sql.DB
}

func NewDetalleReservacionRepository(db *sql.DB) *DetalleReservacionRepository {
	return &DetalleReservacionRepository{db: db}
}

func (r *DetalleReservacionRepository) ObtenerReservacionParaDetalle(reservacionID, usuarioID int) (*dto.ReservacionValidada, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var res dto.ReservacionValidada
	err = conn.QueryRowContext(context.Background(), `
		SELECT ID, EstadoID, Tipo_Reserva_ID, Usuario_ID
		FROM Reservacion
		WHERE ID = ? AND Usuario_ID = ?
	`, reservacionID, usuarioID).Scan(&res.ID, &res.EstadoID, &res.TipoReservaID, &res.UsuarioID)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, err
	}
	return &res, nil
}

func (r *DetalleReservacionRepository) ObtenerDatosProveedor(proveedorID int) (urlAPI, tokenEntrada string, porcentajeGanancia float64, err error) {
	conn, connErr := r.db.Conn(context.Background())
	if connErr != nil {
		return "", "", 0, connErr
	}
	defer conn.Close()

	err = conn.QueryRowContext(context.Background(), `
		SELECT URL_API, Token_HASH_Entrada, Porcentaje_Ganancia
		FROM Proveedor WHERE ID = ?
	`, proveedorID).Scan(&urlAPI, &tokenEntrada, &porcentajeGanancia)
	return
}

func (r *DetalleReservacionRepository) InsertarDetalle(
	reservacionID, proveedorID, tipoDetalleID int,
	idReservaProveedor string,
	total float64,
	parametrosJson interface{},
) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	jsonBytes, err := json.Marshal(parametrosJson)
	if err != nil {
		return fmt.Errorf("error serializando parametros: %w", err)
	}

	const estadoPendiente = 1

	_, err = conn.ExecContext(context.Background(), `
		INSERT INTO Detalles_Reservacion
			(Reservacion_ID, Proveedor_ID, Tipo_Detalle_ID, ID_Reserva_Proveedor, Total, Estado_Detalle_ID, Parametros_Json)
		VALUES (?, ?, ?, ?, ?, ?, ?)`,
		reservacionID, proveedorID, tipoDetalleID, idReservaProveedor, total, estadoPendiente, string(jsonBytes),
	)
	return err
}

func (r *DetalleReservacionRepository) ActualizarTotalReservacion(reservacionID int, montoAgregar float64) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(), `
		UPDATE Reservacion
		SET Total = Total + ?
		WHERE ID = ?`,
		montoAgregar, reservacionID,
	)
	return err
}

func (r *DetalleReservacionRepository) ObtenerDatosProveedorPorTipo(
	proveedorID, tipoDetalleID int,
) (urlAPI, tokenEntrada string, porcentajeGanancia float64, err error) {
	conn, connErr := r.db.Conn(context.Background())
	if connErr != nil {
		return "", "", 0, connErr
	}
	defer conn.Close()

	err = conn.QueryRowContext(context.Background(), `
        SELECT URL_API, Token_HASH_Entrada, Porcentaje_Ganancia
        FROM Proveedor 
        WHERE ID = ? AND Tipo_Detalle_ID = ? 
    `, proveedorID, tipoDetalleID).Scan(&urlAPI, &tokenEntrada, &porcentajeGanancia)

	if err == sql.ErrNoRows {
		return "", "", 0, fmt.Errorf("proveedor no encontrado o no es del tipo requerido")
	}
	return
}

func (r *DetalleReservacionRepository) RecalcularTotalReservacion(reservacionID int) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	// Suma todos los detalles pendientes/activos de la reservación
	_, err = conn.ExecContext(context.Background(), `
        UPDATE Reservacion r
        SET r.Total = (
            SELECT COALESCE(SUM(d.Total), 0)
            FROM Detalles_Reservacion d
            WHERE d.Reservacion_ID = r.ID
            AND d.Estado_Detalle_ID = 1
        )
        WHERE r.ID = ?`,
		reservacionID,
	)
	return err
}
