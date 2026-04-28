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
	"time"
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

// DetalleHotelInfo estructura para obtener informacion del detalle de hotel
type DetalleHotelInfo struct {
	ID                int
	ReservacionID     int
	HabitacionID      int
	ProveedorID       int
	FechaCheckIn      string
	FechaCheckOut     string
	CantidadPersonas  int
	PrecioPorNoche    int
	Estado            int
	TipoHabitacion    string
}

// ObtenerDetalleHotelPorID obtiene los datos de un detalle de hotel
func (r *DetalleReservacionRepository) ObtenerDetalleHotelPorID(
	detalleID string, usuarioID int,
) (*DetalleHotelInfo, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var (
		detalle        DetalleHotelInfo
		parametrosJSON string
		precioDecimal  float64
	)
	err = conn.QueryRowContext(context.Background(), `
        SELECT dr.ID, dr.Reservacion_ID, dr.Proveedor_ID,
               dr.Total, dr.Estado_Detalle_ID, dr.Parametros_Json
        FROM Detalles_Reservacion dr
        JOIN Reservacion r ON dr.Reservacion_ID = r.ID
        WHERE dr.ID = ? AND r.Usuario_ID = ? AND dr.Tipo_Detalle_ID = 2
    `, detalleID, usuarioID).
		Scan(&detalle.ID, &detalle.ReservacionID, &detalle.ProveedorID,
			&precioDecimal, &detalle.Estado, &parametrosJSON)

	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		fmt.Printf("[DetalleReservacionRepo] Error en ObtenerDetalleHotelPorID: %v\n", err)
		return nil, err
	}

	// Convertir precio decimal a int
	detalle.PrecioPorNoche = int(precioDecimal)

	// Parsear JSON para obtener datos de la habitación
	if parametrosJSON != "" {
		var params map[string]interface{}
		if err := json.Unmarshal([]byte(parametrosJSON), &params); err == nil {
			if tipoVal, ok := params["tipoHabitacion"]; ok {
				detalle.TipoHabitacion = fmt.Sprintf("%v", tipoVal)
			}
			if habIDVal, ok := params["habitacionId"]; ok {
				detalle.HabitacionID = int(habIDVal.(float64))
			}
		}
	}

	fmt.Printf("[DetalleReservacionRepo] Detalle encontrado: ID=%d, Usuario=%d, Proveedor=%d\n", detalle.ID, detalle.ReservacionID, detalle.ProveedorID)
	return &detalle, nil
}

// ObtenerProveedorYCriterios extrae la información del proveedor y criterios de búsqueda de un detalle
func (r *DetalleReservacionRepository) ObtenerProveedorYCriterios(
	detalleID string, usuarioID int,
) (proveedorID int, urlAPI, tokenEntrada string, criterios map[string]interface{}, err error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return 0, "", "", nil, err
	}
	defer conn.Close()

	var parametrosJSON string
	err = conn.QueryRowContext(context.Background(), `
        SELECT dr.Proveedor_ID, p.URL_API, p.Token_HASH_Entrada, dr.Parametros_Json
        FROM Detalles_Reservacion dr
        JOIN Reservacion r ON dr.Reservacion_ID = r.ID
        JOIN Proveedor p ON dr.Proveedor_ID = p.ID
        WHERE dr.ID = ? AND r.Usuario_ID = ? AND dr.Tipo_Detalle_ID = 2
    `, detalleID, usuarioID).Scan(&proveedorID, &urlAPI, &tokenEntrada, &parametrosJSON)

	if err == sql.ErrNoRows {
		return 0, "", "", nil, fmt.Errorf("detalle no encontrado")
	}
	if err != nil {
		return 0, "", "", nil, err
	}

	// Parsear JSON para obtener criterios de búsqueda
	if parametrosJSON != "" {
		var params map[string]interface{}
		if err := json.Unmarshal([]byte(parametrosJSON), &params); err == nil {
			criterios = params
		}
	}

	return proveedorID, urlAPI, tokenEntrada, criterios, nil
}

// ObtenerHabitacionesElegibles obtiene habitaciones elegibles para cambio
// Parsea el Parametros_Json del detalle para obtener las habitaciones del proveedor
func (r *DetalleReservacionRepository) ObtenerHabitacionesElegibles(
	detalle *DetalleHotelInfo,
) ([]dto.DetalleHabitacionElegibleDTO, error) {
	// Para obtener alternativas reales, se necesitaría llamar a la API del proveedor.
	// Por ahora, retornamos una lista vacía ya que el JSON almacenado solo tiene
	// la habitacion actualmente reservada, no las alternativas disponibles.

	return []dto.DetalleHabitacionElegibleDTO{}, nil
}

// VerificarHabitacionElegible verifica si una habitacion es elegible para cambio
func (r *DetalleReservacionRepository) VerificarHabitacionElegible(
	detalle *DetalleHotelInfo, nuevaHabitacionID int,
) (bool, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return false, err
	}
	defer conn.Close()

	var count int
	err = conn.QueryRowContext(context.Background(), `
        SELECT COUNT(*) FROM Habitacion h
        WHERE h.ID = ?
          AND h.Proveedor_ID = ?
          AND h.Tipo = ?
          AND h.Precio_PorNoche = ?
          AND h.Activo = 1
    `, nuevaHabitacionID, detalle.ProveedorID, detalle.TipoHabitacion, detalle.PrecioPorNoche).Scan(&count)

	if err != nil {
		return false, err
	}
	return count > 0, nil
}

// ActualizarHabitacionDetalle actualiza la habitacion de un detalle
func (r *DetalleReservacionRepository) ActualizarHabitacionDetalle(
	detalleID string, nuevaHabitacionID int,
) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(), `
        UPDATE Detalles_Reservacion
        SET Habitacion_ID = ?
        WHERE ID = ?
    `, nuevaHabitacionID, detalleID)

	return err
}

// ActualizarPasajero actualiza los datos de un pasajero
// (nombre, apellido, pasaporte, nacionalidad, fecha de nacimiento)
func (r *DetalleReservacionRepository) ActualizarPasajero(
	pasajeroID int, nombre, apellido, numPasaporte, nacionalidad, fechaNac string,
) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	_, err = conn.ExecContext(context.Background(), `
		UPDATE Pasajeros
		SET Nombre = ?, Apellido = ?, Numero_Pasaporte = ?, Nacionalidad = ?, Fecha_Nacimiento = ?
		WHERE ID = ?
	`, nombre, apellido, numPasaporte, nacionalidad, fechaNac, pasajeroID)

	return err
}

// ObtenerDetalleVueloParaEditar obtiene el detalle de vuelo de una reservación
// para propósitos de edición (verificar disponibilidad, actualizar fechas)
func (r *DetalleReservacionRepository) ObtenerDetalleVueloParaEditar(reservacionID int) (map[string]interface{}, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var detalleJSON string
	var proveedorID int

	err = conn.QueryRowContext(context.Background(), `
		SELECT dr.Parametros_Json, dr.Proveedor_ID
		FROM Detalles_Reservacion dr
		WHERE dr.Reservacion_ID = ? AND dr.Tipo_Detalle_ID = 1
		LIMIT 1
	`, reservacionID).Scan(&detalleJSON, &proveedorID)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil // No hay detalle de vuelo
		}
		return nil, err
	}

	var detalle map[string]interface{}
	err = json.Unmarshal([]byte(detalleJSON), &detalle)
	if err != nil {
		return nil, err
	}

	detalle["proveedor_id"] = proveedorID
	return detalle, nil
}

// ActualizarFechasVuelo actualiza las fechas de ida y retorno en un detalle de vuelo
// Almacena la información en el JSON del detalle
func (r *DetalleReservacionRepository) ActualizarFechasVuelo(reservacionID int, fechaIda, fechaRetorno string) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	// Obtener el detalle actual
	var detalleJSON string
	err = conn.QueryRowContext(context.Background(), `
		SELECT dr.Detalle_JSON
		FROM Detalles_Reservacion dr
		WHERE dr.Reservacion_ID = ? AND dr.Tipo_Detalle = 1
	`, reservacionID).Scan(&detalleJSON)

	if err != nil {
		return err
	}

	// Parsear y actualizar fechas
	var detalle map[string]interface{}
	err = json.Unmarshal([]byte(detalleJSON), &detalle)
	if err != nil {
		return err
	}

	if fechaIda != "" {
		detalle["fechaIda"] = fechaIda
	}
	if fechaRetorno != "" {
		detalle["fechaRetorno"] = fechaRetorno
	}

	// Serializar de vuelta a JSON
	detalleActualizadoJSON, err := json.Marshal(detalle)
	if err != nil {
		return err
	}

	// Actualizar en BD
	_, err = conn.ExecContext(context.Background(), `
		UPDATE Detalles_Reservacion
		SET Detalle_JSON = ?
		WHERE Reservacion_ID = ? AND Tipo_Detalle = 1
	`, string(detalleActualizadoJSON), reservacionID)

	return err
}

// ObtenerDetalleHotelParaEditar obtiene el detalle de hotel de una reservación
// para propósitos de edición (verificar disponibilidad, actualizar fechas)
func (r *DetalleReservacionRepository) ObtenerDetalleHotelParaEditar(reservacionID int) (map[string]interface{}, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return nil, err
	}
	defer conn.Close()

	var detalleJSON string
	var proveedorID int
	var idReservaProveedor string

	err = conn.QueryRowContext(context.Background(), `
		SELECT dr.Parametros_Json, dr.Proveedor_ID, dr.ID_Reserva_Proveedor
		FROM Detalles_Reservacion dr
		WHERE dr.Reservacion_ID = ? AND dr.Tipo_Detalle_ID = 2
		LIMIT 1
	`, reservacionID).Scan(&detalleJSON, &proveedorID, &idReservaProveedor)

	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil // No hay detalle de hotel
		}
		return nil, err
	}

	var detalle map[string]interface{}
	err = json.Unmarshal([]byte(detalleJSON), &detalle)
	if err != nil {
		return nil, err
	}

	detalle["proveedor_id"] = proveedorID
	detalle["id_reserva_proveedor"] = idReservaProveedor
	return detalle, nil
}

// ActualizarFechasHotel actualiza las fechas de check-in y check-out en un detalle de hotel
// Almacena la información en el JSON del detalle
func (r *DetalleReservacionRepository) ActualizarFechasHotel(reservacionID int, fechaCheckIn, fechaCheckOut string) error {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return err
	}
	defer conn.Close()

	// Obtener el detalle actual
	var detalleJSON string
	err = conn.QueryRowContext(context.Background(), `
		SELECT dr.Detalle_JSON
		FROM Detalles_Reservacion dr
		WHERE dr.Reservacion_ID = ? AND dr.Tipo_Detalle = 2
	`, reservacionID).Scan(&detalleJSON)

	if err != nil {
		return err
	}

	// Parsear y actualizar fechas
	var detalle map[string]interface{}
	err = json.Unmarshal([]byte(detalleJSON), &detalle)
	if err != nil {
		return err
	}

	// Actualizar fechas en respuestaHotel si existe
	if respuesta, ok := detalle["respuestaHotel"].(map[string]interface{}); ok {
		if fechaCheckIn != "" {
			respuesta["fechaCheckIn"] = fechaCheckIn
		}
		if fechaCheckOut != "" {
			respuesta["fechaCheckOut"] = fechaCheckOut
		}
	}

	// Serializar de vuelta a JSON
	detalleActualizadoJSON, err := json.Marshal(detalle)
	if err != nil {
		return err
	}

	// Actualizar SOLO el JSON, sin tocar el Total
	_, err = conn.ExecContext(context.Background(), `
		UPDATE Detalles_Reservacion
		SET Detalle_JSON = ?
		WHERE Reservacion_ID = ? AND Tipo_Detalle = 2
	`, string(detalleActualizadoJSON), reservacionID)

	return err
}

// VerificarTraslapeHotel
//
// Verifica si las nuevas fechas de check-in/checkout de un hotel se solapan
// con otras reservaciones activas del mismo hotel. Retorna true si hay traslape
// (conflicto), false si las fechas son válidas.
//
// La verificación se hace contra todas las reservaciones CONFIRMADAS (EstadoID=2)
// o COMPLETADAS (EstadoID=4) del mismo hotel en el mismo proveedor.
//
// Parámetros:
//   - proveedorID: ID del hotel/proveedor
//   - fechaCheckIn: fecha de entrada en formato YYYY-MM-DD
//   - fechaCheckOut: fecha de salida en formato YYYY-MM-DD
//   - excludeReservacionID: ID de la reservación actual (excluir de validación)
//
// Retorna:
//   - bool: true si hay traslape, false si no hay conflictos
//   - error: error de base de datos, nil si la operación fue exitosa
func (r *DetalleReservacionRepository) VerificarTraslapeHotel(proveedorID int, fechaCheckIn, fechaCheckOut string, excludeReservacionID int) (bool, error) {
	conn, err := r.db.Conn(context.Background())
	if err != nil {
		return false, err
	}
	defer conn.Close()

	// Buscar reservaciones del mismo hotel que se solapan con las fechas
	// Dos periodos se solapan si: checkin_nuevo < checkout_existente AND checkout_nuevo > checkin_existente
	var count int
	err = conn.QueryRowContext(context.Background(), `
		SELECT COUNT(*) FROM Detalles_Reservacion dr
		INNER JOIN Reservacion res ON dr.Reservacion_ID = res.ID
		WHERE dr.Proveedor_ID = ?
		  AND res.ID != ?
		  AND res.EstadoID IN (2, 4)
		  AND dr.Tipo_Detalle_ID = 2
		  AND JSON_EXTRACT(dr.Parametros_Json, '$.respuestaHotel.fechaCheckIn') < ?
		  AND JSON_EXTRACT(dr.Parametros_Json, '$.respuestaHotel.fechaCheckOut') > ?
	`, proveedorID, excludeReservacionID, fechaCheckOut, fechaCheckIn).Scan(&count)

	if err != nil {
		return false, err
	}

	return count > 0, nil
}

// VerificarDuracionIgual
//
// Verifica que la duración (número de noches) de la nueva reservación
// sea la misma que la original. Esto asegura que no se extienda o acorte
// la estadía sin cambiar el precio.
//
// Parámetros:
//   - fechaCheckInActual: fecha de check-in actual en formato YYYY-MM-DD
//   - fechaCheckOutActual: fecha de check-out actual en formato YYYY-MM-DD
//   - fechaCheckInNueva: fecha de check-in nueva en formato YYYY-MM-DD
//   - fechaCheckOutNueva: fecha de check-out nueva en formato YYYY-MM-DD
//
// Retorna:
//   - bool: true si las duraciones son iguales, false si son diferentes
func VerificarDuracionIgual(fechaCheckInActual, fechaCheckOutActual, fechaCheckInNueva, fechaCheckOutNueva string) bool {
	// Parsear fechas actuales
	checkinActual := parseFecha(fechaCheckInActual)
	checkoutActual := parseFecha(fechaCheckOutActual)
	duracionActual := checkoutActual.Sub(checkinActual).Hours() / 24

	// Parsear fechas nuevas
	checkinNueva := parseFecha(fechaCheckInNueva)
	checkoutNueva := parseFecha(fechaCheckOutNueva)
	duracionNueva := checkoutNueva.Sub(checkinNueva).Hours() / 24

	// Comparar duraciones (con tolerancia de 0.1 para evitar errores de precisión)
	return duracionActual > duracionNueva-0.1 && duracionActual < duracionNueva+0.1
}

// ActualizarFechasHotelAtomico
//
// Actualiza las fechas de check-in y check-out de TODAS las habitaciones
// de un hotel en una única transacción. Si cualquier operación falla,
// se hace rollback de todos los cambios.
//
// Parámetros:
//   - reservacionID: ID de la reservación cuyos detalles se actualizarán
//   - fechaCheckIn: nueva fecha de check-in en formato YYYY-MM-DD
//   - fechaCheckOut: nueva fecha de check-out en formato YYYY-MM-DD
//
// Retorna:
//   - error: nil si la transacción fue exitosa, error si falla
func (r *DetalleReservacionRepository) ActualizarFechasHotelAtomico(reservacionID int, fechaCheckIn, fechaCheckOut string) error {
	tx, err := r.db.BeginTx(context.Background(), nil)
	if err != nil {
		return err
	}

	// 1. Obtener todos los detalles de hotel para esta reservación
	var detalles []struct {
		ID          int
		DetalleJSON string
	}

	rows, err := tx.QueryContext(context.Background(), `
		SELECT ID, Parametros_Json FROM Detalles_Reservacion
		WHERE Reservacion_ID = ? AND Tipo_Detalle_ID = 2
	`, reservacionID)
	if err != nil {
		tx.Rollback()
		return err
	}
	defer rows.Close()

	for rows.Next() {
		var detalle struct {
			ID          int
			DetalleJSON string
		}
		if err := rows.Scan(&detalle.ID, &detalle.DetalleJSON); err != nil {
			tx.Rollback()
			return err
		}
		detalles = append(detalles, detalle)
	}

	// 2. Actualizar cada detalle de hotel
	for _, detalle := range detalles {
		var jsonData map[string]interface{}
		if err := json.Unmarshal([]byte(detalle.DetalleJSON), &jsonData); err != nil {
			tx.Rollback()
			return err
		}

		// Actualizar fechas en respuestaHotel si existe
		if respuesta, ok := jsonData["respuestaHotel"].(map[string]interface{}); ok {
			respuesta["fechaCheckIn"] = fechaCheckIn
			respuesta["fechaCheckOut"] = fechaCheckOut
		}

		// También actualizar en el nivel superior (por si las fechas están allí)
		jsonData["fechaCheckIn"] = fechaCheckIn
		jsonData["fechaCheckOut"] = fechaCheckOut

		// Serializar JSON actualizado
		updatedJSON, err := json.Marshal(jsonData)
		if err != nil {
			tx.Rollback()
			return err
		}

		// Actualizar en BD
		_, err = tx.ExecContext(context.Background(), `
			UPDATE Detalles_Reservacion
			SET Parametros_Json = ?
			WHERE ID = ?
		`, string(updatedJSON), detalle.ID)
		if err != nil {
			tx.Rollback()
			return err
		}
	}

	return tx.Commit()
}

// ActualizarFechasVueloAtomico
//
// Actualiza las fechas de ida y retorno de TODOS los vuelos
// de una reservación en una única transacción. Si cualquier operación falla,
// se hace rollback de todos los cambios.
//
// Parámetros:
//   - reservacionID: ID de la reservación cuyos detalles se actualizarán
//   - fechaIda: nueva fecha de ida en formato YYYY-MM-DD
//   - fechaRetorno: nueva fecha de retorno en formato YYYY-MM-DD
//
// Retorna:
//   - error: nil si la transacción fue exitosa, error si falla
func (r *DetalleReservacionRepository) ActualizarFechasVueloAtomico(reservacionID int, fechaIda, fechaRetorno string) error {
	tx, err := r.db.BeginTx(context.Background(), nil)
	if err != nil {
		return err
	}

	// 1. Obtener todos los detalles de vuelo para esta reservación
	var detalles []struct {
		ID          int
		DetalleJSON string
	}

	rows, err := tx.QueryContext(context.Background(), `
		SELECT ID, Parametros_Json FROM Detalles_Reservacion
		WHERE Reservacion_ID = ? AND Tipo_Detalle_ID = 1
	`, reservacionID)
	if err != nil {
		tx.Rollback()
		return err
	}
	defer rows.Close()

	for rows.Next() {
		var detalle struct {
			ID          int
			DetalleJSON string
		}
		if err := rows.Scan(&detalle.ID, &detalle.DetalleJSON); err != nil {
			tx.Rollback()
			return err
		}
		detalles = append(detalles, detalle)
	}

	// 2. Actualizar cada detalle de vuelo
	for _, detalle := range detalles {
		var jsonData map[string]interface{}
		if err := json.Unmarshal([]byte(detalle.DetalleJSON), &jsonData); err != nil {
			tx.Rollback()
			return err
		}

		// Actualizar fechas
		if fechaIda != "" {
			jsonData["fechaIda"] = fechaIda
		}
		if fechaRetorno != "" {
			jsonData["fechaRetorno"] = fechaRetorno
		}

		// Serializar JSON actualizado
		updatedJSON, err := json.Marshal(jsonData)
		if err != nil {
			tx.Rollback()
			return err
		}

		// Actualizar en BD
		_, err = tx.ExecContext(context.Background(), `
			UPDATE Detalles_Reservacion
			SET Parametros_Json = ?
			WHERE ID = ?
		`, string(updatedJSON), detalle.ID)
		if err != nil {
			tx.Rollback()
			return err
		}
	}

	return tx.Commit()
}

// parseFecha
//
// Función auxiliar para parsear fechas en formato YYYY-MM-DD.
// Retorna time.Time con la fecha parsada.
func parseFecha(fechaStr string) time.Time {
	fecha, _ := time.Parse("2006-01-02", fechaStr)
	return fecha
}
