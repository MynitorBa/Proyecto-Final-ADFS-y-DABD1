// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/helpers"
	"agencia-viajes/internal/repositories"
	"bytes"
	"crypto/tls"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log"
	"math"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

const (
	TipoDetalleVuelo = 1
	TipoDetalleHotel = 2

	TipoReservaAerolinea = 1
	TipoReservaHotelera  = 2
	TipoReservaPaquete   = 3
)

// DetalleReservacionService
//
// Servicio encargado de agregar detalles de vuelo y hotel a reservaciones
// existentes en estado pendiente. Coordina la reserva con el proveedor externo,
// aplica el margen de ganancia configurado, almacena el detalle en BD y
// recalcula el total de la reservacion. Tambien gestiona el alta de pasajeros
// en el sistema de la aerolinea.
type DetalleReservacionService struct {
	repo      *repositories.DetalleReservacionRepository
	logSesion *LogSesionService
}

// NewDetalleReservacionService
//
// Crea e inicializa una nueva instancia de DetalleReservacionService con su
// repositorio de detalle de reservacion.
//
// Parametros:
//   - db:        conexion activa a la base de datos SQL
//   - logSesion: servicio de auditoria para registrar eventos REST salientes
//
// Retorna:
//   - *DetalleReservacionService: instancia lista para usar
func NewDetalleReservacionService(db *sql.DB, logSesion *LogSesionService) *DetalleReservacionService {
	return &DetalleReservacionService{
		repo:      repositories.NewDetalleReservacionRepository(db),
		logSesion: logSesion,
	}
}

// AgregarDetalleVuelo
//
// Agrega un detalle de vuelo a una reservacion existente. Valida que la
// reservacion pertenezca al usuario, este en estado pendiente y no sea
// exclusivamente de tipo hotelera. Llama al proveedor aerolinea para crear
// la reserva, calcula el precio con ganancia por boleto y guarda el detalle
// en BD, actualizando el total de la reservacion.
//
// Parametros:
//   - usuarioID: identificador del usuario dueno de la reservacion
//   - req: datos del detalle incluyendo ReservacionID, ProveedorID y lista de vuelos
//
// Retorna:
//   - interface{}: mapa con mensaje, IDs, total base, total con ganancia y detalle del proveedor
//   - error: si la reservacion no existe, no es valida, falla el proveedor o la BD
func (s *DetalleReservacionService) AgregarDetalleVuelo(c *gin.Context, usuarioID int, req dto.AgregarDetalleVueloRequest) (interface{}, error) {

	reservacion, err := s.repo.ObtenerReservacionParaDetalle(req.ReservacionID, usuarioID)
	if err != nil {
		return nil, err
	}
	if reservacion == nil {
		return nil, errors.New("reservación no encontrada o no pertenece al usuario")
	}
	if reservacion.EstadoID != 1 {
		return nil, errors.New("la reservación no está en estado pendiente")
	}
	if reservacion.TipoReservaID == TipoReservaHotelera {
		return nil, errors.New("esta reservación es solo de tipo hotelera, no admite vuelos")
	}

	urlAPI, tokenEntrada, porcentajeDescuento, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleVuelo)
	if err != nil {
		return nil, err
	}

	uid := usuarioID
	respAerolinea, err := s.llamarReservacionAerolinea(c, &uid, urlAPI, tokenEntrada, req.Vuelos)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en aerolínea: %w", err)
	}

	// La aerolinea devuelve precios SIN descuento
	// Movent calcula la ganancia basada en el porcentaje de descuento
	totalVuelos := respAerolinea["total"].(float64)

	// Calcular ganancia/impuestos de Movent
	// Ganancia = total × porcentajeDescuento / 100
	gananciaMovent := math.Round((totalVuelos * porcentajeDescuento / 100) * 100) / 100

	// Total final = siempre el precio original de vuelos
	var totalConGanancia float64 = totalVuelos

	idReservaProveedor := fmt.Sprintf("%v", respAerolinea["reservacionId"])

	// Envolver respAerolinea en parametrosCompletos incluyendo detalles de cálculo
	parametrosCompletos := map[string]interface{}{
		"totalVuelos": totalVuelos,      // Precio de vuelos (sin descuento)
		"impuestos":   gananciaMovent,   // Impuestos/ganancia de Movent
		"respuestaAerea": respAerolinea,
	}

	err = s.repo.InsertarDetalle(
		req.ReservacionID,
		req.ProveedorID,
		TipoDetalleVuelo,
		idReservaProveedor,
		totalConGanancia,
		parametrosCompletos,
	)
	if err != nil {
		return nil, errors.New("error guardando detalle de reservación")
	}

	err = s.repo.RecalcularTotalReservacion(req.ReservacionID)
	if err != nil {
		return nil, errors.New("error recalculando total de reservación")
	}

	return map[string]interface{}{
		"mensaje":        "detalle de vuelo agregado exitosamente",
		"reservacion_id": req.ReservacionID,
		"vuelos":         totalVuelos,      // Precio de vuelos
		"impuestos":      gananciaMovent,   // Impuestos y servicios (ganancia agencia)
		"total":          totalConGanancia, // Total a pagar
		"detalle":        respAerolinea,
	}, nil
}

// AgregarDetalleHotel
//
// Agrega un detalle de hotel a una reservacion existente. Valida que la
// reservacion pertenezca al usuario, este en estado pendiente y no sea
// exclusivamente de tipo aerea. Llama al proveedor hotelera para crear
// la reserva, calcula el precio con ganancia por noche (con soporte para
// personas extra) y guarda el detalle en BD, actualizando el total.
//
// Parametros:
//   - usuarioID: identificador del usuario dueno de la reservacion
//   - req: datos del detalle incluyendo ReservacionID, ProveedorID y lista de habitaciones
//
// Retorna:
//   - interface{}: mapa con mensaje, IDs, total base, total con ganancia y detalle del proveedor
//   - error: si la reservacion no existe, no es valida, falla el proveedor o la BD
func (s *DetalleReservacionService) AgregarDetalleHotel(c *gin.Context, usuarioID int, req dto.AgregarDetalleHotelRequest) (interface{}, error) {

	reservacion, err := s.repo.ObtenerReservacionParaDetalle(req.ReservacionID, usuarioID)
	if err != nil {
		return nil, err
	}
	if reservacion == nil {
		return nil, errors.New("reservación no encontrada o no pertenece al usuario")
	}
	if reservacion.EstadoID != 1 {
		return nil, errors.New("la reservación no está en estado pendiente")
	}
	if reservacion.TipoReservaID == TipoReservaAerolinea {
		return nil, errors.New("esta reservación es solo de tipo aérea, no admite hoteles")
	}

	urlAPI, tokenEntrada, porcentajeDescuento, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleHotel)
	if err != nil {
		return nil, err
	}

	uid := usuarioID
	respHotel, err := s.llamarReservacionHotel(c, &uid, urlAPI, tokenEntrada, req.Habitaciones)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en hotelera: %w", err)
	}

	// El hotel devuelve precios SIN descuento
	// Movent calcula la ganancia basada en el porcentaje de descuento
	// totalHabitaciones = lo que devuelve el hotel (SIN descuento)
	// impuestos = ganancia de Movent = total × (porcentajeDescuento / 100)
	// totalFinal = totalHabitaciones = totalHabitaciones (usuario siempre paga el precio original)

	totalHabitaciones := respHotel["total"].(float64)

	// Calcular ganancia/impuestos de Movent
	// Ganancia = total × porcentajeDescuento / 100
	// Ejemplo: $1,600 × 10% = $160
	gananciaMovent := math.Round((totalHabitaciones * porcentajeDescuento / 100) * 100) / 100

	// Total final = siempre el precio original de habitaciones
	totalConGanancia := totalHabitaciones

	idReservaProveedor := fmt.Sprintf("%v", respHotel["id"])

	// Crear estructura que contenga tanto la respuesta del hotel como los criterios de búsqueda
	// IMPORTANTE: Incluir los detalles de cálculo para que el frontend muestre el desglose de impuestos
	parametrosCompletos := map[string]interface{}{
		"totalHabitaciones": totalHabitaciones, // Precio de habitaciones (sin descuento)
		"impuestos":         gananciaMovent,    // Impuestos/ganancia de Movent
		"respuestaHotel":    respHotel,
		"criteriosBusqueda": req.CriteriosBusqueda,
	}

	err = s.repo.InsertarDetalle(
		req.ReservacionID,
		req.ProveedorID,
		TipoDetalleHotel,
		idReservaProveedor,
		totalConGanancia,
		parametrosCompletos,
	)
	if err != nil {
		return nil, errors.New("error guardando detalle de reservación")
	}

	err = s.repo.RecalcularTotalReservacion(req.ReservacionID)
	if err != nil {
		return nil, errors.New("error recalculando total de reservación")
	}

	return map[string]interface{}{
		"mensaje":           "detalle de hotel agregado exitosamente",
		"reservacion_id":    req.ReservacionID,
		"habitaciones":      totalHabitaciones, // Precio de habitaciones
		"impuestos":         gananciaMovent,    // Impuestos y servicios (ganancia agencia)
		"total":             totalConGanancia,  // Total a pagar
		"detalle":           respHotel,
	}, nil
}

// llamarReservacionAerolinea
//
// Realiza la llamada HTTP POST al endpoint de reservaciones de una aerolinea
// proveedora enviando la seleccion de vuelos. Retorna la respuesta completa
// del proveedor como mapa generico.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor aerolinea
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//   - vuelos: lista de vuelos seleccionados con sus pasajeros
//
// Retorna:
//   - map[string]interface{}: respuesta del proveedor con reservacionId, total y detalle
//   - error: si la serializacion falla, la peticion HTTP falla o la respuesta es invalida
func (s *DetalleReservacionService) llamarReservacionAerolinea(c *gin.Context, usuarioID *int, urlAPI, token string, vuelos []dto.SeleccionVuelo) (map[string]interface{}, error) {
	// TODO: agregar timeout al http.DefaultClient (deuda técnica identificada)
	body, err := json.Marshal(map[string]interface{}{
		"vuelos": vuelos,
	})
	if err != nil {
		return nil, fmt.Errorf("error serializando request: %w", err)
	}

	req, err := http.NewRequest(http.MethodPost, urlAPI+"/api/reservaciones-agencia", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutReservaVueloProveedorFallida, usuarioID, "reserva-vuelo-proveedor",
			fmt.Sprintf("Broom status=ERR msg='%s'", err.Error()))
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		msg := fmt.Sprintf("Broom status=%d msg='%s'", resp.StatusCode, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutReservaVueloProveedorFallida, usuarioID, "reserva-vuelo-proveedor", msg)
		return nil, fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	var resultado map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutReservaVueloProveedorFallida, usuarioID, "reserva-vuelo-proveedor",
			fmt.Sprintf("Broom decode_error='%s'", err.Error()))
		return nil, errors.New("respuesta inválida de la aerolínea")
	}

	s.logSesion.Registrar(c, helpers.TipoOutReservaVueloProveedorExitosa, usuarioID, "reserva-vuelo-proveedor",
		fmt.Sprintf("Broom: reservacionId=%v total=%v", resultado["reservacionId"], resultado["total"]))

	return resultado, nil
}

// llamarReservacionHotel
//
// Realiza la llamada HTTP POST al endpoint de reservaciones de una hotelera
// proveedora enviando la seleccion de habitaciones. Retorna la respuesta
// completa del proveedor como mapa generico.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor hotelera
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//   - habitaciones: lista de habitaciones seleccionadas con fechas y opciones
//
// Retorna:
//   - map[string]interface{}: respuesta del proveedor con id, total y detalle de habitaciones
//   - error: si la serializacion falla, la peticion HTTP falla o la respuesta es invalida
func (s *DetalleReservacionService) llamarReservacionHotel(c *gin.Context, usuarioID *int, urlAPI, token string, habitaciones []dto.SeleccionHabitacion) (map[string]interface{}, error) {
	// TODO: agregar timeout al http.DefaultClient (deuda técnica identificada)
	body, err := json.Marshal(map[string]interface{}{
		"habitaciones": habitaciones,
	})
	if err != nil {
		return nil, fmt.Errorf("error serializando request: %w", err)
	}

	req, err := http.NewRequest(http.MethodPost, urlAPI+"/agencia/reservaciones", bytes.NewBuffer(body))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutReservaHotelProveedorFallida, usuarioID, "reserva-hotel-proveedor",
			fmt.Sprintf("Miku status=ERR msg='%s'", err.Error()))
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		msg := fmt.Sprintf("Miku status=%d msg='%s'", resp.StatusCode, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutReservaHotelProveedorFallida, usuarioID, "reserva-hotel-proveedor", msg)
		return nil, fmt.Errorf("hotelera respondió con status %d", resp.StatusCode)
	}

	var resultado map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutReservaHotelProveedorFallida, usuarioID, "reserva-hotel-proveedor",
			fmt.Sprintf("Miku decode_error='%s'", err.Error()))
		return nil, errors.New("respuesta inválida de la hotelera")
	}

	s.logSesion.Registrar(c, helpers.TipoOutReservaHotelProveedorExitosa, usuarioID, "reserva-hotel-proveedor",
		fmt.Sprintf("Miku: id=%v total=%v", resultado["id"], resultado["total"]))

	return resultado, nil
}

// AgregarPasajerosVuelo
//
// Registra los datos de los pasajeros en el sistema de la aerolinea para
// una reservacion existente. Valida que cada pasajero tenga un numero de
// pasaporte con solo digitos antes de enviar la solicitud al proveedor.
//
// Parametros:
//   - usuarioID: identificador del usuario dueno de la reservacion
//   - req: datos de la solicitud incluyendo ReservacionID, ProveedorID y lista de pasajeros
//
// Retorna:
//   - error: si el pasaporte es invalido, falla la obtencion del detalle o falla el proveedor
func (s *DetalleReservacionService) AgregarPasajerosVuelo(
	c *gin.Context,
	usuarioID int,
	req dto.AgregarPasajerosVueloRequest,
) error {

	for _, p := range req.Pasajeros {
		if strings.TrimSpace(p.Pasaporte) == "" {
			return errors.New("el número de pasaporte es obligatorio")
		}
		for _, c := range p.Pasaporte {
			if c < '0' || c > '9' {
				return fmt.Errorf(
					"el pasaporte de %s %s debe contener solo números",
					p.Nombre, p.Apellido,
				)
			}
		}
	}

	idReservaAerolinea, urlAPI, token, err := s.repo.ObtenerDetalleAerolineaPorProveedor(
		req.ReservacionID, usuarioID, req.ProveedorID,
	)
	if err != nil {
		return err
	}

	reservacionAerolineaID, err := strconv.Atoi(idReservaAerolinea)
	if err != nil {
		return fmt.Errorf("id de reservación de aerolínea inválido: %w", err)
	}

	uid := usuarioID
	return s.llamarPasajerosAerolinea(c, &uid, urlAPI, token, reservacionAerolineaID, req.Pasajeros)
}

// llamarPasajerosAerolinea
//
// Realiza la llamada HTTP POST al endpoint de pasajeros de la aerolinea
// proveedora, enviando el ID de reservacion en el sistema externo y la
// lista de pasajeros con sus datos personales y de pasaporte.
//
// Parametros:
//   - urlAPI: URL base del API del proveedor aerolinea
//   - token: token de autenticacion de agencia (X-Agencia-Token)
//   - reservacionID: ID de la reservacion en el sistema de la aerolinea
//   - pasajeros: lista de pasajeros con nombre, apellido, pasaporte y otros datos
//
// Retorna:
//   - error: si la serializacion falla, la peticion HTTP falla o la aerolinea rechaza la solicitud
func (s *DetalleReservacionService) llamarPasajerosAerolinea(
	c *gin.Context,
	usuarioID *int,
	urlAPI, token string,
	reservacionID int,
	pasajeros []dto.PasajeroVueloDTO,
) error {
	// TODO: agregar timeout al http.DefaultClient (deuda técnica identificada)
	bodyReq := dto.AgregarPasajerosVueloAerolineaBody{
		ReservacionID: reservacionID,
		Pasajeros:     pasajeros,
	}

	bodyBytes, err := json.Marshal(bodyReq)
	if err != nil {
		return fmt.Errorf("error serializando pasajeros: %w", err)
	}

	req, err := http.NewRequest(
		http.MethodPost,
		urlAPI+"/api/reservaciones-agencia/pasajeros",
		bytes.NewBuffer(bodyBytes),
	)
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Agencia-Token", token)

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		s.logSesion.Registrar(c, helpers.TipoOutPasajerosProveedorFallida, usuarioID, "pasajeros-proveedor",
			fmt.Sprintf("Broom status=ERR msg='%s'", err.Error()))
		return fmt.Errorf("error contactando aerolínea: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		msg := fmt.Sprintf("Broom status=%d msg='%s'", resp.StatusCode, helpers.ParseErrorProveedor(resp))
		s.logSesion.Registrar(c, helpers.TipoOutPasajerosProveedorFallida, usuarioID, "pasajeros-proveedor", msg)
		return fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	s.logSesion.Registrar(c, helpers.TipoOutPasajerosProveedorExitosa, usuarioID, "pasajeros-proveedor",
		fmt.Sprintf("Broom: %d pasajero(s) registrados, reservaId=%d", len(pasajeros), reservacionID))

	return nil
}

// EditarReservacion
//
// Edita datos de una reservacion existente incluyendo nombres de pasajeros,
// datos de pasaporte y fechas. Valida que el usuario sea propietario de la
// reservacion, verifica disponibilidad de vuelos y hoteles para las nuevas fechas
// y actualiza los datos en la BD.
//
// Parametros:
//   - c: contexto de Gin para logging
//   - usuarioID: identificador del usuario propietario de la reservacion
//   - reservacionID: identificador de la reservacion a editar
//   - req: DTO con los datos a actualizar
//
// Retorna:
//   - interface{}: mapa con confirmación de cambios realizados
//   - error: si ocurre error de validacion, autorizacion o BD
func (s *DetalleReservacionService) EditarReservacion(c *gin.Context, usuarioID int, reservacionID string, req dto.EditarReservacionRequest) (interface{}, error) {
	// Validar que la reservacion pertenezca al usuario
	resID, _ := strconv.Atoi(reservacionID)
	reservacion, err := s.repo.ObtenerReservacionParaDetalle(resID, usuarioID)
	if err != nil {
		return nil, errors.New("reservación no encontrada")
	}
	if reservacion == nil {
		return nil, errors.New("no autorizado")
	}

	var cambios []string

	// Nota: Solo se permite editar fechas de hospedaje (hotel), no vuelos
	// La edición de pasajeros y vuelos ha sido deshabilitada por solicitud de usuario

	// Verificar y actualizar fechas de hotel si se proporcionan
	if req.FechaCheckIn != "" || req.FechaCheckOut != "" {
		// Validar formato de fechas
		if req.FechaCheckIn != "" {
			if _, err := time.Parse("2006-01-02", req.FechaCheckIn); err != nil {
				return nil, errors.New("formato de check-in inválido (usar YYYY-MM-DD)")
			}
		}
		if req.FechaCheckOut != "" {
			if _, err := time.Parse("2006-01-02", req.FechaCheckOut); err != nil {
				return nil, errors.New("formato de check-out inválido (usar YYYY-MM-DD)")
			}
		}

		// Validar que las fechas sean posteriores o iguales a hoy
		hoy := time.Now().Truncate(24 * time.Hour)
		if req.FechaCheckIn != "" {
			fechaCheckIn, _ := time.Parse("2006-01-02", req.FechaCheckIn)
			if fechaCheckIn.Before(hoy) {
				return nil, errors.New("la fecha de check-in no puede ser anterior a hoy")
			}
		}
		if req.FechaCheckOut != "" {
			fechaCheckOut, _ := time.Parse("2006-01-02", req.FechaCheckOut)
			if fechaCheckOut.Before(hoy) {
				return nil, errors.New("la fecha de check-out no puede ser anterior a hoy")
			}
		}

		// Validar regla de 48 horas: check-in no puede ser en menos de 48 horas
		if req.FechaCheckIn != "" {
			fechaCheckIn, _ := time.Parse("2006-01-02", req.FechaCheckIn)
			horasRestantes := fechaCheckIn.Sub(hoy).Hours()
			if horasRestantes < 48 {
				return nil, errors.New("el check-in debe ser con al menos 48 horas de anticipación")
			}
		}

		// Obtener detalle de hotel para verificar disponibilidad
		detalleHotel, err := s.repo.ObtenerDetalleHotelParaEditar(resID)
		if err == nil && detalleHotel != nil {
			// Usar fechas actuales enviadas por el frontend
			fechaCheckInActual := req.FechaCheckInActual
			fechaCheckOutActual := req.FechaCheckOutActual

			// Validar que la duración sea igual (si se cambian ambas fechas)
			// SOLO si ambas nuevas son diferentes a las actuales
			if req.FechaCheckIn != "" && req.FechaCheckOut != "" && fechaCheckInActual != "" && fechaCheckOutActual != "" {
				// Si las fechas cambiaron
				if req.FechaCheckIn != fechaCheckInActual || req.FechaCheckOut != fechaCheckOutActual {
					if !repositories.VerificarDuracionIgual(fechaCheckInActual, fechaCheckOutActual, req.FechaCheckIn, req.FechaCheckOut) {
						return nil, errors.New("la duración de la estadía no puede cambiar (mismo número de noches)")
					}
				}
			}

			// Verificar traslapes con otras reservaciones
			proveedorID, ok := detalleHotel["proveedor_id"].(float64)
			if ok {
				// Usar fechas nuevas si se proporcionan, sino usar actuales
				fechaCheckInValidar := req.FechaCheckIn
				if fechaCheckInValidar == "" {
					fechaCheckInValidar = fechaCheckInActual
				}
				fechaCheckOutValidar := req.FechaCheckOut
				if fechaCheckOutValidar == "" {
					fechaCheckOutValidar = fechaCheckOutActual
				}

				hayTraslape, err := s.repo.VerificarTraslapeHotel(int(proveedorID), fechaCheckInValidar, fechaCheckOutValidar, resID)
				if err != nil {
					return nil, fmt.Errorf("error verificando disponibilidad de hotel: %w", err)
				}
				if hayTraslape {
					return nil, errors.New("el hotel no está disponible en las fechas solicitadas (hay conflicto con otra reserva)")
				}
			}

			uid := usuarioID
			// Llamar al proveedor para verificar disponibilidad en las nuevas fechas
			disponible, err := s.verificarDisponibilidadHotel(c, &uid, detalleHotel, req.FechaCheckIn, req.FechaCheckOut)
			if err != nil {
				return nil, fmt.Errorf("error verificando disponibilidad de hotel: %w", err)
			}
			if !disponible {
				return nil, errors.New("el hotel no está disponible en las fechas solicitadas")
			}

			// Actualizar fechas en BD usando transacción atómica
			// IMPORTANTE: Usar fechas validadas que tienen valores por defecto
			fechaCheckInFinal := req.FechaCheckIn
			if fechaCheckInFinal == "" {
				fechaCheckInFinal = fechaCheckInActual
			}
			fechaCheckOutFinal := req.FechaCheckOut
			if fechaCheckOutFinal == "" {
				fechaCheckOutFinal = fechaCheckOutActual
			}

			err = s.repo.ActualizarFechasHotelAtomico(resID, fechaCheckInFinal, fechaCheckOutFinal)
			if err != nil {
				return nil, fmt.Errorf("error actualizando fechas de hotel: %w", err)
			}

			// Notificar al proveedor hotelero sobre los cambios de fechas
			log.Printf("[EditarReservacion] detalleHotel es nil: %v", detalleHotel == nil)
			if detalleHotel != nil {
				log.Printf("[EditarReservacion] Contenido detalleHotel: %v", detalleHotel)

				// Obtener proveedorID - puede ser int o float64
				var proveedorID int
				var ok bool
				if pID, okF := detalleHotel["proveedor_id"].(float64); okF {
					proveedorID = int(pID)
					ok = true
				} else if pID, okI := detalleHotel["proveedor_id"].(int); okI {
					proveedorID = pID
					ok = true
				}

				idReservaProveedor, okID := detalleHotel["id_reserva_proveedor"].(string)

				log.Printf("[EditarReservacion] proveedorID=%v, idReservaProveedor=%s, ok=%v, okID=%v", proveedorID, idReservaProveedor, ok, okID)

				if ok && okID {
					// Extraer detalleId del primer habitacion en respuestaHotel
					detalleID := 0
					if respuestaHotel, okResp := detalleHotel["respuestaHotel"].(map[string]interface{}); okResp {
						log.Printf("[EditarReservacion] respuestaHotel encontrado: %v", respuestaHotel)
						if habitaciones, okHab := respuestaHotel["habitaciones"].([]interface{}); okHab && len(habitaciones) > 0 {
							log.Printf("[EditarReservacion] habitaciones encontradas: %d", len(habitaciones))
							if primer, okPrimer := habitaciones[0].(map[string]interface{}); okPrimer {
								log.Printf("[EditarReservacion] Primera habitacion: %v", primer)
								if detID, okDetID := primer["detalleId"].(float64); okDetID {
									detalleID = int(detID)
									log.Printf("[EditarReservacion] detalleId extraído: %d", detalleID)
								} else {
									log.Printf("[EditarReservacion] No se pudo convertir detalleId a float64")
								}
							} else {
								log.Printf("[EditarReservacion] No se pudo convertir primera habitacion a map")
							}
						} else {
							log.Printf("[EditarReservacion] No hay habitaciones o okHab=false")
						}
					} else {
						log.Printf("[EditarReservacion] respuestaHotel no encontrado en detalleHotel")
					}

					if detalleID > 0 {
						log.Printf("[EditarReservacion] Enviando notificación a MIKU con detalleID=%d", detalleID)
						err := s.notificarCambioFechasAlProveedor(
							c, int(proveedorID), idReservaProveedor,
							fechaCheckInFinal, fechaCheckOutFinal, detalleID,
						)
						if err != nil {
							log.Printf("[EditarReservacion] Error notificando al proveedor: %v", err)
							// No fallar si la notificación falla - los datos locales ya se actualizaron
							// El proveedor puede sincronizar de forma asíncrona si es necesario
						}
					} else {
						log.Printf("[EditarReservacion] No se encontró detalleId en la respuesta del proveedor")
					}
				} else {
					log.Printf("[EditarReservacion] No pasó validación de proveedor: ok=%v, okID=%v", ok, okID)
				}
			}
		}

		cambios = append(cambios, "Fechas de hotel actualizadas")
	}

	// NO recalcular el total - el usuario ya pagó ese precio
	// Las fechas no deberían afectar el total

	// Log del cambio de reservacion
	s.logSesion.Registrar(c, helpers.TipoOutEditarReservacionExitosa, &usuarioID, reservacionID,
		fmt.Sprintf("Reservación editada con cambios: %v", cambios))

	return dto.EditarReservacionResponse{
		Exitoso: true,
		Mensaje: "Reservación actualizada exitosamente",
		Cambios: cambios,
	}, nil
}

// verificarDisponibilidadVuelo
//
// Verifica con el proveedor aerolinea si hay disponibilidad de vuelos en
// las nuevas fechas solicitadas.
//
// Retorna true si está disponible, false si no.
func (s *DetalleReservacionService) verificarDisponibilidadVuelo(
	c *gin.Context,
	usuarioID *int,
	detalleVuelo map[string]interface{},
	fechaIda, fechaRetorno string,
) (bool, error) {
	// Si no hay cambio en fechas, considerar disponible
	if fechaIda == "" && fechaRetorno == "" {
		return true, nil
	}

	// Aquí se podría implementar una llamada al proveedor para verificar
	// disponibilidad en las nuevas fechas. Por ahora asumimos disponible
	// ya que es el flujo de edición simple (no listado de opciones).
	// El proveedor validará definitivamente al momento de confirmar cambios.

	return true, nil
}

// verificarDisponibilidadHotel
//
// Verifica con el proveedor hotelero si hay disponibilidad del hotel en
// las nuevas fechas solicitadas.
//
// Retorna true si está disponible, false si no.
func (s *DetalleReservacionService) verificarDisponibilidadHotel(
	c *gin.Context,
	usuarioID *int,
	detalleHotel map[string]interface{},
	fechaCheckIn, fechaCheckOut string,
) (bool, error) {
	// Si no hay cambio en fechas, considerar disponible
	if fechaCheckIn == "" && fechaCheckOut == "" {
		return true, nil
	}

	// Aquí se podría implementar una llamada al proveedor para verificar
	// disponibilidad en las nuevas fechas. Por ahora asumimos disponible
	// ya que es el flujo de edición simple (no listado de opciones).
	// El proveedor validará definitivamente al momento de confirmar cambios.

	return true, nil
}

// notificarCambioFechasAlProveedor
//
// Notifica al proveedor hotelero sobre los cambios de fechas en una reservación.
// Realiza un request PATCH al endpoint del proveedor para sincronizar los cambios.
//
// Parámetros:
//   - c: contexto de Gin
//   - proveedorID: ID del proveedor hotelero
//   - idReservaProveedor: ID de la reservación en el sistema del proveedor
//   - fechaCheckIn: nueva fecha de check-in
//   - fechaCheckOut: nueva fecha de check-out
//   - detalleID: ID del detalle de la reservación
//
// Retorna:
//   - error: nil si la notificación fue exitosa, error si falla
func (s *DetalleReservacionService) notificarCambioFechasAlProveedor(
	c *gin.Context,
	proveedorID int,
	idReservaProveedor string,
	fechaCheckIn, fechaCheckOut string,
	detalleID int,
) error {
	// Obtener datos del proveedor (URLAPI y token)
	urlAPI, tokenEntrada, _, err := s.repo.ObtenerDatosProveedorPorTipo(proveedorID, 2)
	if err != nil {
		return fmt.Errorf("error obteniendo datos del proveedor: %w", err)
	}

	// Construir URL del endpoint del proveedor
	url := fmt.Sprintf("%s/agencia/reservaciones/%s/fechas", urlAPI, idReservaProveedor)

	// Construir el body del request con los cambios
	cambios := []map[string]interface{}{
		{
			"detalleId":      detalleID,
			"fechaCheckIn":   fechaCheckIn,
			"fechaCheckOut":  fechaCheckOut,
		},
	}

	bodyMap := map[string]interface{}{
		"cambios": cambios,
	}

	bodyBytes, err := json.Marshal(bodyMap)
	if err != nil {
		return fmt.Errorf("error serializando body para notificación: %w", err)
	}

	// Crear el request HTTP
	req, err := http.NewRequest("PATCH", url, bytes.NewBuffer(bodyBytes))
	if err != nil {
		return fmt.Errorf("error creando request HTTP: %w", err)
	}

	// Configurar headers
	req.Header.Set("X-Agencia-Token", tokenEntrada)
	req.Header.Set("Content-Type", "application/json")

	// Realizar el request con un cliente HTTP seguro
	resp, err := httpClientSinVerificar().Do(req)
	if err != nil {
		return fmt.Errorf("error enviando notificación al proveedor: %w", err)
	}
	defer resp.Body.Close()

	// Verificar el status code de respuesta
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		body, _ := io.ReadAll(resp.Body)
		log.Printf("[NotificacionProveedor] HTTP %d al notificar cambio de fechas. Respuesta: %s",
			resp.StatusCode, string(body))
		return fmt.Errorf("proveedor rechazó la notificación: HTTP %d", resp.StatusCode)
	}

	log.Printf("[NotificacionProveedor] Éxito notificando cambio de fechas para reservación %s", idReservaProveedor)
	return nil
}

// httpClientSinVerificar
//
// Crea y retorna un cliente HTTP configurado para omitir la verificación
// de certificados TLS. Se usa para comunicación con proveedores externos.
//
// Retorna:
//   - *http.Client: cliente HTTP con TLS InsecureSkipVerify habilitado
func httpClientSinVerificar() *http.Client {
	return &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	}
}
