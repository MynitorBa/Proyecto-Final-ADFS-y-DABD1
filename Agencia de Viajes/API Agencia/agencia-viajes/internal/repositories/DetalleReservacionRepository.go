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
	"encoding/json"
	"fmt"
)

// DetalleReservacionRepository
//
// Repositorio que gestiona las operaciones sobre los detalles de reservacion,
// incluyendo la insercion de nuevos detalles, consulta de datos de proveedor,
// actualizacion de totales y validacion de vuelos pendientes.
type DetalleReservacionRepository struct {
	db *sql.DB
}

// NewDetalleReservacionRepository
//
// Crea e inicializa una nueva instancia de DetalleReservacionRepository.
//
// Parametros:
//   - db: conexion activa a la base de datos
//
// Retorna:
//   - *DetalleReservacionRepository: instancia lista para usar
func NewDetalleReservacionRepository(db *sql.DB) *DetalleReservacionRepository {
	return &DetalleReservacionRepository{db: db}
}

// ObtenerReservacionParaDetalle
//
// Verifica que una reservacion exista y pertenezca al usuario indicado,
// retornando sus datos basicos para validacion previa a la insercion de detalles.
//
// Parametros:
//   - reservacionID: ID de la reservacion a consultar
//   - usuarioID: ID del usuario propietario de la reservacion
//
// Retorna:
//   - *dto.ReservacionValidada: datos basicos de la reservacion, nil si no existe
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerDatosProveedor
//
// Recupera la URL del API, el token de entrada y el porcentaje de ganancia
// configurado para un proveedor especifico.
//
// Parametros:
//   - proveedorID: ID del proveedor a consultar
//
// Retorna:
//   - urlAPI: URL base del API del proveedor
//   - tokenEntrada: token de autenticacion para peticiones al proveedor
//   - porcentajeGanancia: margen de ganancia configurado para el proveedor
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// InsertarDetalle
//
// Inserta un nuevo registro en la tabla Detalles_Reservacion con estado pendiente (1).
// Los parametros adicionales de la reserva se serializan a JSON antes de persistirse.
//
// Parametros:
//   - reservacionID: ID de la reservacion padre
//   - proveedorID: ID del proveedor que gestiona este detalle
//   - tipoDetalleID: tipo de servicio reservado (1=vuelo, 2=hotel)
//   - idReservaProveedor: identificador de la reserva en el sistema del proveedor externo
//   - total: monto total del detalle con el margen de ganancia aplicado
//   - parametrosJson: estructura con los parametros de la reserva a serializar
//
// Retorna:
//   - error: error de serializacion o base de datos, nil si la operacion fue exitosa
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

// ActualizarTotalReservacion
//
// Incrementa el total acumulado de una reservacion sumando el monto indicado.
// Se utiliza cada vez que se agrega un nuevo detalle a la reservacion.
//
// Parametros:
//   - reservacionID: ID de la reservacion a actualizar
//   - montoAgregar: monto que se suma al total existente
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
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

// ObtenerDatosProveedorPorTipo
//
// Recupera los datos de conexion de un proveedor validando ademas que coincida
// con el tipo de detalle solicitado (1=aerolinea, 2=hotelera).
//
// Parametros:
//   - proveedorID: ID del proveedor a consultar
//   - tipoDetalleID: ID del tipo de proveedor esperado
//
// Retorna:
//   - urlAPI: URL base del API del proveedor
//   - tokenEntrada: token de autenticacion para peticiones al proveedor
//   - porcentajeGanancia: margen de ganancia configurado
//   - error: error si el proveedor no existe o no corresponde al tipo, nil si fue exitosa
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

// RecalcularTotalReservacion
//
// Recalcula y actualiza el total de una reservacion sumando el total de todos
// sus detalles que se encuentren en estado pendiente (Estado_Detalle_ID = 1).
//
// Parametros:
//   - reservacionID: ID de la reservacion a recalcular
//
// Retorna:
//   - error: error de base de datos, nil si la operacion fue exitosa
//
// Notas:
//   - Utiliza COALESCE para retornar 0 si no hay detalles activos
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

// ObtenerDetalleAerolineaPorProveedor
//
// Busca el identificador de reserva en el proveedor aerolinea para un detalle
// de tipo vuelo (Tipo_Detalle_ID = 1) que este pendiente, validando que la
// reservacion pertenezca al usuario y este en estado activo.
//
// Parametros:
//   - reservacionID: ID de la reservacion a consultar
//   - usuarioID: ID del usuario propietario de la reservacion
//   - proveedorID: ID del proveedor aerolinea involucrado
//
// Retorna:
//   - idReservaProveedor: identificador de la reserva en el sistema del proveedor
//   - urlAPI: URL base del API del proveedor
//   - tokenEntrada: token de autenticacion del proveedor
//   - error: error si no se encuentra el detalle o la reservacion no pertenece al usuario
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
