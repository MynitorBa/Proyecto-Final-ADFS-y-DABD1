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
)

// ReservacionRepository
//
// Repositorio que gestiona el ciclo de vida de las reservaciones en la base de datos,
// incluyendo creacion, consulta de pendientes, expiracion masiva e individual,
// y recuperacion de detalles asociados a cada reservacion.
type ReservacionRepository struct {
	db *sql.DB
}

// NewReservacionRepository
//
// Crea e inicializa una nueva instancia de ReservacionRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *ReservacionRepository: instancia lista para usar
func NewReservacionRepository(db *sql.DB) *ReservacionRepository {
	return &ReservacionRepository{db: db}
}

// CrearReservacion
//
// Inserta una nueva reservacion en la base de datos con estado pendiente (1)
// y total inicial de cero.
//
// Parametros:
//   - usuarioID: ID del usuario que realiza la reservacion
//   - tipoReservaID: tipo de reserva (vuelo, hotel, paquete, etc.)
//   - noReservacion: numero unico de reservacion generado por el servicio
//   - fechaExpiracion: fecha y hora limite para confirmar la reservacion
//
// Retorna:
//   - int: ID autogenerado de la nueva reservacion
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ExpirarReservacionesPendientes
//
// Actualiza a estado expirado (4) todas las reservaciones que se encuentren
// en estado pendiente (1) y cuya fecha de expiracion ya haya pasado.
//
// Parametros:
//   - (ninguno)
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerPendientesConDetalles
//
// Recupera todas las reservaciones pendientes de un usuario junto con los detalles
// y datos del proveedor asociados a cada una.
//
// Parametros:
//   - usuarioID: ID del usuario cuyas reservaciones pendientes se desean consultar
//
// Retorna:
//   - []dto.ReservacionConDetalles: lista de reservaciones con sus detalles de proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// obtenerDetallesProveedor
//
// Funcion interna que recupera los detalles de proveedor de una reservacion
// usando una conexion SQL ya abierta.
//
// Parametros:
//   - conn: conexion SQL activa reutilizada desde el metodo llamador
//   - reservacionID: ID de la reservacion cuyos detalles se desean obtener
//
// Retorna:
//   - []dto.DetalleProveedor: lista de detalles con informacion del proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ReservacionRepository) obtenerDetallesProveedor(conn *sql.Conn, reservacionID int) ([]dto.DetalleProveedor, error) {
	rows, err := conn.QueryContext(context.Background(), `
		SELECT dr.ID_Reserva_Proveedor, p.ID, p.URL_API, p.Token_HASH_Entrada, dr.Tipo_Detalle_ID
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
		if err := rows.Scan(&d.IDReservaProveedor, &d.ProveedorID, &d.URLAPI, &d.TokenEntrada, &d.TipoDetalleID); err != nil {
			return nil, err
		}
		detalles = append(detalles, d)
	}
	return detalles, nil
}

// ExpirarReservacion
//
// Marca una reservacion especifica como expirada (estado 4) siempre que
// actualmente se encuentre en estado pendiente (1).
//
// Parametros:
//   - reservacionID: ID de la reservacion a expirar
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ExpirarDetalles
//
// Marca como cancelados (estado 3) todos los detalles de una reservacion,
// utilizado durante el proceso de expiracion de la reservacion padre.
//
// Parametros:
//   - reservacionID: ID de la reservacion cuyos detalles se deben cancelar
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerIDsPendientesExpirados
//
// Consulta los IDs de todas las reservaciones que esten en estado pendiente (1)
// y cuya fecha de expiracion ya haya pasado. Usado por el scheduler de expiracion.
//
// Parametros:
//   - (ninguno)
//
// Retorna:
//   - []int: lista de IDs de reservaciones pendientes expiradas
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerDetallesDeReservacion
//
// Recupera todos los detalles de una reservacion junto con los datos de conexion
// del proveedor asociado a cada detalle.
//
// Parametros:
//   - reservacionID: ID de la reservacion a consultar
//
// Retorna:
//   - []dto.DetalleProveedor: lista de detalles con informacion del proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
func (r *ReservacionRepository) ObtenerDetallesDeReservacion(reservacionID int) ([]dto.DetalleProveedor, error) {
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
