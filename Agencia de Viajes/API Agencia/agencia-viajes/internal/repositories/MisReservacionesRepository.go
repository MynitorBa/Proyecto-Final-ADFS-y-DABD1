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

// MisReservacionesRepository
//
// Repositorio que gestiona la consulta del historial de reservaciones de un usuario,
// retornando tanto los datos de la reservacion como sus detalles y la informacion
// del proveedor asociado a cada uno.
type MisReservacionesRepository struct {
	db *sql.DB
}

// NewMisReservacionesRepository
//
// Crea e inicializa una nueva instancia de MisReservacionesRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *MisReservacionesRepository: instancia lista para usar
func NewMisReservacionesRepository(db *sql.DB) *MisReservacionesRepository {
	return &MisReservacionesRepository{db: db}
}

// ObtenerReservacionesDeUsuario
//
// Recupera todas las reservaciones de un usuario junto con sus detalles y
// los datos del proveedor de cada detalle. Los resultados se ordenan por
// fecha de creacion descendente.
//
// Parametros:
//   - usuarioID: ID del usuario cuyas reservaciones se desean consultar
//
// Retorna:
//   - []dto.FilaReservacionDetalle: filas planas con datos de reservacion, detalle y proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerReservacionPorID
//
// Recupera una reservacion especifica junto con todos sus detalles y los datos
// del proveedor, verificando que la reservacion pertenezca al usuario indicado.
//
// Parametros:
//   - reservacionID: ID de la reservacion a consultar
//   - usuarioID: ID del usuario propietario de la reservacion
//
// Retorna:
//   - []dto.FilaReservacionDetalle: filas planas con datos de reservacion, detalle y proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// escanearFilas
//
// Funcion auxiliar compartida que itera sobre las filas de un resultado SQL
// y las convierte en una lista de dto.FilaReservacionDetalle.
//
// Parametros:
//   - rows: cursor de resultados de la consulta SQL
//
// Retorna:
//   - []dto.FilaReservacionDetalle: lista de filas mapeadas
//   - error: error de escaneo, nil si la operacion fue exitosa
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
