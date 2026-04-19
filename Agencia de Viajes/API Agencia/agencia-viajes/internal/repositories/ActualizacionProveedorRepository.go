package repositories

import (
	"context"
	"database/sql"
	"fmt"
)

// ActualizacionProveedorRepository
//
// Repositorio que gestiona la insercion de notificaciones de actualizacion
// enviadas por proveedores externos. A diferencia de la cancelacion, aqui
// no se modifica ningun estado: solo se registra la notificacion y se
// resuelven los datos del usuario para el correo.
type ActualizacionProveedorRepository struct {
	db *sql.DB
}

// NewActualizacionProveedorRepository
//
// Crea e inicializa una nueva instancia del repositorio.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *ActualizacionProveedorRepository: instancia lista para usar
func NewActualizacionProveedorRepository(db *sql.DB) *ActualizacionProveedorRepository {
	return &ActualizacionProveedorRepository{db: db}
}

// ObtenerDetalleParaActualizacion
//
// Verifica que el detalle exista y pertenezca al proveedor autenticado,
// resolviendo al mismo tiempo el ID interno del detalle y el ID de la
// reservacion padre para la notificacion.
//
// Parametros:
//   - idReservaProveedor: ID de la reserva en el sistema del proveedor (string)
//   - proveedorID:        ID del proveedor autenticado via middleware
//
// Retorna:
//   - detalleID:     ID interno del detalle en detalles_reservacion
//   - reservacionID: ID de la reservacion padre
//   - error: si el detalle no existe, no pertenece al proveedor o falla la BD
func (r *ActualizacionProveedorRepository) ObtenerDetalleParaActualizacion(
	idReservaProveedor string, proveedorID int,
) (detalleID int, reservacionID int, err error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, 0, err
	}
	defer conn.Close()

	err = conn.QueryRowContext(context.Background(), `
		SELECT ID, Reservacion_ID
		FROM detalles_reservacion
		WHERE ID_Reserva_Proveedor = ? AND Proveedor_ID = ?
	`, idReservaProveedor, proveedorID).Scan(&detalleID, &reservacionID)

	if err == sql.ErrNoRows {
		return 0, 0, fmt.Errorf("detalle no encontrado o no pertenece al proveedor")
	}
	return
}

// InsertarNotificacionActualizacion
//
// Registra la notificacion de actualizacion en la tabla notificaciones.
// Tipo_Notificacion_ID = 4 corresponde a "Actualizacion por Proveedor".
// No modifica ningun estado de reservacion ni detalle.
//
// Parametros:
//   - reservacionID:    ID de la reservacion padre
//   - detalleID:        ID interno del detalle que origino la actualizacion
//   - mensajeProveedor: mensaje descriptivo enviado por el proveedor
//
// Retorna:
//   - error: si falla la insercion en BD
func (r *ActualizacionProveedorRepository) InsertarNotificacionActualizacion(
	reservacionID, detalleID int,
	mensajeProveedor string,
) error {
	_, err := r.db.ExecContext(context.Background(), `
		INSERT INTO notificaciones
			(Reservacion_ID, Detalle_Reservacion_ID, Mensaje_Proveedor, Boleano_Leido, Tipo_Notificacion_ID)
		VALUES (?, ?, ?, 0, 4)
	`, reservacionID, detalleID, mensajeProveedor)
	return err
}

// ObtenerDatosCorreoActualizacion
//
// Recupera el correo, nombre, apellido y numero de reservacion del usuario
// dueno de la reservacion para enviarle la notificacion de actualizacion.
// Reutiliza el mismo JOIN que en cancelacion para mantener consistencia.
//
// Parametros:
//   - reservacionID: ID de la reservacion de la que se quieren los datos
//
// Retorna:
//   - correo:        correo electronico del usuario
//   - nombre:        nombre del usuario
//   - apellido:      apellido del usuario
//   - noReservacion: numero de reservacion legible (ej: "1B037995")
//   - error: si la reservacion no existe o falla la BD
func (r *ActualizacionProveedorRepository) ObtenerDatosCorreoActualizacion(
	reservacionID int,
) (correo, nombre, apellido, noReservacion string, err error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return
	}
	defer conn.Close()

	err = conn.QueryRowContext(context.Background(), `
		SELECT u.Correo, u.Nombre, u.Apellido, r.No_Reservacion
		FROM reservacion r
		JOIN usuario u ON r.Usuario_ID = u.ID
		WHERE r.ID = ?
	`, reservacionID).Scan(&correo, &nombre, &apellido, &noReservacion)

	if err == sql.ErrNoRows {
		err = fmt.Errorf("reservacion no encontrada: %d", reservacionID)
	}
	return
}
