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
        WHERE ID = ? AND Tipo_Proveedor_ID = ? 
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

// ObtenerDetalleAerolineaPorProveedor devuelve el ID de reserva en la aerolínea
// para un detalle de tipo vuelo (Tipo_Detalle_ID = 1) que esté pendiente,
// verificando que la reservación pertenezca al usuario.
func (r *DetalleReservacionRepository) ObtenerDetalleAerolineaPorProveedor(
	reservacionID, usuarioID, proveedorID int,
) (idReservaProveedor string, urlAPI string, tokenEntrada string, err error) {

	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return "", "", "", err
	}
	defer conn.Close()

	err = conn.QueryRowContext(context.Background(), `
        SELECT dr.ID_Reserva_Proveedor, p.URL_API, p.Token_HASH_Entrada
        FROM Detalles_Reservacion dr
        JOIN Reservacion r        ON dr.Reservacion_ID = r.ID
        JOIN Proveedor   p        ON dr.Proveedor_ID   = p.ID
        WHERE dr.Reservacion_ID   = ?
          AND r.Usuario_ID        = ?
          AND dr.Proveedor_ID     = ?
          AND dr.Tipo_Detalle_ID  = 1
          AND dr.Estado_Detalle_ID = 1
          AND r.EstadoID          = 1
    `, reservacionID, usuarioID, proveedorID).
		Scan(&idReservaProveedor, &urlAPI, &tokenEntrada)

	if err == sql.ErrNoRows {
		return "", "", "", fmt.Errorf(
			"no se encontró un detalle de vuelo pendiente para esa reservación y proveedor, " +
				"o la reservación no pertenece al usuario",
		)
	}
	return
}
