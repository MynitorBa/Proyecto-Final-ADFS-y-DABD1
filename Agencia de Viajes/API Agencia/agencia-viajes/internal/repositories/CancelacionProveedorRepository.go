package repositories

import (
	"context"
	"database/sql"
	"fmt"
)

// CancelacionProveedorRepository
//
// Repositorio que gestiona las operaciones de base de datos necesarias
// cuando un proveedor externo inicia la cancelacion de un detalle de reservacion.
// Es distinto de CancelacionRepository (que gestiona cancelaciones iniciadas
// por el usuario) porque aqui el flujo es diferente: el estado destino de la
// reservacion padre depende del tipo de reserva y de si quedan otros detalles activos.
type CancelacionProveedorRepository struct {
	db *sql.DB
}

// NewCancelacionProveedorRepository
//
// Crea e inicializa una nueva instancia del repositorio.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *CancelacionProveedorRepository: instancia lista para usar
func NewCancelacionProveedorRepository(db *sql.DB) *CancelacionProveedorRepository {
	return &CancelacionProveedorRepository{db: db}
}

// ObtenerDetalleParaCancelarPorProveedor
//
// Verifica que el detalle de reservacion exista y pertenezca al proveedor
// autenticado. La busqueda se hace por ID_Reserva_Proveedor + Proveedor_ID,
// garantizando que un proveedor solo pueda operar sobre sus propios detalles.
//
// Parametros:
//   - idReservaProveedor: ID de la reserva en el sistema del proveedor (string)
//   - proveedorID:        ID del proveedor autenticado via middleware
//
// Retorna:
//   - detalleID:       ID interno del detalle en detalles_reservacion
//   - reservacionID:   ID de la reservacion padre
//   - estadoDetalleID: estado actual del detalle (1=Pendiente, 2=Confirmado, 3=Cancelado)
//   - error: si el detalle no existe, no pertenece al proveedor o falla la BD
func (r *CancelacionProveedorRepository) ObtenerDetalleParaCancelarPorProveedor(
	idReservaProveedor string, proveedorID int,
) (detalleID int, reservacionID int, estadoDetalleID int, err error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, 0, 0, err
	}
	defer conn.Close()

	err = conn.QueryRowContext(context.Background(), `
		SELECT ID, Reservacion_ID, Estado_Detalle_ID
		FROM detalles_reservacion
		WHERE ID_Reserva_Proveedor = ? AND Proveedor_ID = ?
	`, idReservaProveedor, proveedorID).Scan(&detalleID, &reservacionID, &estadoDetalleID)

	if err == sql.ErrNoRows {
		return 0, 0, 0, fmt.Errorf("detalle no encontrado o no pertenece al proveedor")
	}
	return
}

// ObtenerDatosCorreoReservacion
//
// Recupera el correo electronico, nombre, apellido y numero de reservacion
// del usuario dueno de una reservacion. Se usa para enviar la notificacion
// de cancelacion por proveedor tras completar la transaccion.
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
func (r *CancelacionProveedorRepository) ObtenerDatosCorreoReservacion(
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

// determinarEstadoDestino
//
// Decide a que estado debe pasar la reservacion padre tras cancelar un detalle.
// La regla de negocio es:
//
//   - Tipo 1 (Aerolinea) o Tipo 2 (Hotelera): reservacion de un solo proveedor,
//     no hay otros detalles posibles → Cancelada (3) directamente.
//
//   - Tipo 3 (Paquete): puede tener vuelo + hotel. Se cuentan los detalles
//     restantes con estado activo (Pendiente=1 o Confirmado=2) excluyendo el
//     detalle que se acaba de cancelar. Si quedan activos → Retenida (7).
//     Si no queda ninguno → Cancelada (3).
//
// Esta consulta se ejecuta DENTRO de la transaccion para leer el estado
// consistente antes de aplicar el UPDATE sobre la reservacion.
//
// Parametros:
//   - tx:            transaccion activa
//   - reservacionID: ID de la reservacion padre
//   - detalleID:     ID del detalle que se esta cancelando (se excluye del conteo)
//   - tipoReservaID: tipo de la reservacion (1=Aerolinea, 2=Hotelera, 3=Paquete)
//
// Retorna:
//   - estadoDestino: 3 (Cancelada) o 7 (Retenida)
//   - error: si falla la consulta de BD
func determinarEstadoDestino(
	tx *sql.Tx,
	reservacionID, detalleID, tipoReservaID int,
) (estadoDestino int, err error) {

	// Tipos 1 y 2: reservacion de proveedor unico, no hay nada mas que revisar
	if tipoReservaID == 1 || tipoReservaID == 2 {
		return 3, nil // Cancelada directamente
	}

	// Tipo 3 (Paquete): contar cuantos detalles siguen activos excluyendo
	// el que se esta cancelando en este momento
	var detallesActivosRestantes int
	err = tx.QueryRowContext(context.Background(), `
		SELECT COUNT(*)
		FROM detalles_reservacion
		WHERE Reservacion_ID = ?
		  AND ID != ?
		  AND Estado_Detalle_ID IN (1, 2)
	`, reservacionID, detalleID).Scan(&detallesActivosRestantes)
	if err != nil {
		return 0, err
	}

	if detallesActivosRestantes > 0 {
		return 7, nil // Retenida: aun hay componentes activos
	}
	return 3, nil // Cancelada: no queda ningun componente activo
}

// CancelarDetalleYActualizarReservacion
//
// Ejecuta en una sola transaccion atomica:
//  1. Obtiene el Tipo_Reserva_ID de la reservacion padre
//  2. Cancela el detalle indicado (Estado_Detalle_ID = 3)
//  3. Determina si la reservacion queda Cancelada (3) o Retenida (7)
//     segun el tipo de reserva y los detalles activos restantes
//  4. Actualiza el estado de la reservacion padre (incluye estado 7=Retenida
//     para que un paquete parcialmente cancelado pueda quedar Cancelado completo)
//  5. Inserta la notificacion con el mensaje del proveedor
//
// Toda la logica ocurre dentro de la transaccion para garantizar
// consistencia: el conteo de detalles activos refleja el estado real
// en el momento exacto de la cancelacion.
//
// Parametros:
//   - detalleID:        ID interno del detalle a cancelar
//   - reservacionID:    ID de la reservacion padre
//   - mensajeProveedor: mensaje explicativo enviado por el proveedor
//
// Retorna:
//   - estadoDestino: estado final aplicado a la reservacion (3=Cancelada, 7=Retenida)
//   - error: si alguna operacion de la transaccion falla (con rollback automatico)
func (r *CancelacionProveedorRepository) CancelarDetalleYActualizarReservacion(
	detalleID, reservacionID int,
	mensajeProveedor string,
) (estadoDestino int, err error) {
	tx, err := r.db.BeginTx(context.Background(), nil)
	if err != nil {
		return 0, err
	}

	// 1. Leer el tipo de reservacion dentro de la transaccion
	var tipoReservaID int
	err = tx.QueryRowContext(context.Background(), `
		SELECT Tipo_Reserva_ID FROM reservacion WHERE ID = ?
	`, reservacionID).Scan(&tipoReservaID)
	if err != nil {
		tx.Rollback()
		return 0, err
	}

	// 2. Cancelar el detalle especifico (estado 3 = Cancelado)
	_, err = tx.Exec(`
		UPDATE detalles_reservacion
		SET Estado_Detalle_ID = 3
		WHERE ID = ?
	`, detalleID)
	if err != nil {
		tx.Rollback()
		return 0, err
	}

	// 3. Determinar estado destino de la reservacion segun tipo y detalles restantes
	//    Se evalua DESPUES de cancelar el detalle para que el conteo sea correcto
	estadoDestino, err = determinarEstadoDestino(tx, reservacionID, detalleID, tipoReservaID)
	if err != nil {
		tx.Rollback()
		return 0, err
	}

	// 4. Actualizar el estado de la reservacion padre.
	//    Se incluye EstadoID = 7 (Retenida) para que si el segundo detalle de un
	//    paquete tambien se cancela, la reservacion pase de Retenida a Cancelada.
	_, err = tx.Exec(`
		UPDATE reservacion
		SET EstadoID = ?
		WHERE ID = ? AND EstadoID IN (1, 2, 7)
	`, estadoDestino, reservacionID)
	if err != nil {
		tx.Rollback()
		return 0, err
	}

	// 5. Insertar notificacion vinculada a la reservacion y al detalle cancelado
	//    Tipo_Notificacion_ID = 3 corresponde a "Cancelacion por Proveedor"
	_, err = tx.Exec(`
		INSERT INTO notificaciones
			(Reservacion_ID, Detalle_Reservacion_ID, Mensaje_Proveedor, Boleano_Leido, Tipo_Notificacion_ID)
		VALUES (?, ?, ?, 0, 3)
	`, reservacionID, detalleID, mensajeProveedor)
	if err != nil {
		tx.Rollback()
		return 0, err
	}

	return estadoDestino, tx.Commit()
}
