// # Package services
//
// Servicios de negocio de la agencia de viajes. Este paquete contiene la logica
// central para reservaciones, busquedas, autenticacion, catalogos y comunicacion
// con proveedores externos (aerolineas y hoteleras).
package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"bytes"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"math"
	"net/http"
	"strconv"
	"strings"
	"time"
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
	repo *repositories.DetalleReservacionRepository
}

// NewDetalleReservacionService
//
// Crea e inicializa una nueva instancia de DetalleReservacionService con su
// repositorio de detalle de reservacion.
//
// Parametros:
//   - db: conexion activa a la base de datos SQL
//
// Retorna:
//   - *DetalleReservacionService: instancia lista para usar
func NewDetalleReservacionService(db *sql.DB) *DetalleReservacionService {
	return &DetalleReservacionService{
		repo: repositories.NewDetalleReservacionRepository(db),
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
func (s *DetalleReservacionService) AgregarDetalleVuelo(usuarioID int, req dto.AgregarDetalleVueloRequest) (interface{}, error) {

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

	urlAPI, tokenEntrada, porcentajeGanancia, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleVuelo)
	if err != nil {
		return nil, err
	}

	respAerolinea, err := s.llamarReservacionAerolinea(urlAPI, tokenEntrada, req.Vuelos)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en aerolínea: %w", err)
	}

	totalBase := respAerolinea["total"].(float64)

	var totalPasajeros int
	for _, v := range req.Vuelos {
		if v.CantidadPasajeros > 0 {
			totalPasajeros += v.CantidadPasajeros
		}
	}

	var totalConGanancia float64
	if totalPasajeros > 0 {
		precioBaseBoleto := totalBase / float64(totalPasajeros)
		precioBoletoConGanancia := math.Round(precioBaseBoleto*(1+porcentajeGanancia/100)*100) / 100
		totalConGanancia = math.Round(precioBoletoConGanancia*float64(totalPasajeros)*100) / 100
	} else {
		totalConGanancia = math.Round(totalBase*(1+porcentajeGanancia/100)*100) / 100
	}

	idReservaProveedor := fmt.Sprintf("%v", respAerolinea["reservacionId"])
	err = s.repo.InsertarDetalle(
		req.ReservacionID,
		req.ProveedorID,
		TipoDetalleVuelo,
		idReservaProveedor,
		totalConGanancia,
		respAerolinea,
	)
	if err != nil {
		return nil, errors.New("error guardando detalle de reservación")
	}

	err = s.repo.RecalcularTotalReservacion(req.ReservacionID)
	if err != nil {
		return nil, errors.New("error recalculando total de reservación")
	}

	return map[string]interface{}{
		"mensaje":            "detalle de vuelo agregado exitosamente",
		"reservacion_id":     req.ReservacionID,
		"total_base":         totalBase,
		"total_con_ganancia": totalConGanancia,
		"detalle":            respAerolinea,
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
func (s *DetalleReservacionService) AgregarDetalleHotel(usuarioID int, req dto.AgregarDetalleHotelRequest) (interface{}, error) {

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

	urlAPI, tokenEntrada, porcentajeGanancia, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleHotel)
	if err != nil {
		return nil, err
	}

	respHotel, err := s.llamarReservacionHotel(urlAPI, tokenEntrada, req.Habitaciones)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en hotelera: %w", err)
	}

	totalBase := respHotel["total"].(float64)
	var totalConGanancia float64

	if habitaciones, ok := respHotel["habitaciones"].([]interface{}); ok && len(habitaciones) > 0 {
		for _, h := range habitaciones {
			if hab, ok := h.(map[string]interface{}); ok {
				precio, okP := hab["precioPorNoche"].(float64)
				noches, okN := hab["noches"].(float64)
				precioPorPersona, _ := hab["precioPorPersona"].(float64)
				personasExtra, _ := hab["personasExtra"].(float64)
				if okP && okN && noches > 0 {
					precioNocheMarkup := math.Round(precio*(1+porcentajeGanancia/100)*100) / 100
					precioPersonaMarkup := math.Round(precioPorPersona*(1+porcentajeGanancia/100)*100) / 100
					totalConGanancia += (precioNocheMarkup + personasExtra*precioPersonaMarkup) * noches
				}
			}
		}
		totalConGanancia = math.Round(totalConGanancia*100) / 100
	} else {
		var totalNoches int
		for _, hab := range req.Habitaciones {
			checkIn, errCI := time.Parse("2006-01-02", hab.FechaCheckIn)
			checkOut, errCO := time.Parse("2006-01-02", hab.FechaCheckOut)
			if errCI == nil && errCO == nil {
				n := int(checkOut.Sub(checkIn).Hours() / 24)
				if n > 0 {
					totalNoches += n
				}
			}
		}
		if totalNoches > 0 {
			precioBaseNoche := totalBase / float64(totalNoches)
			precioNocheConGanancia := math.Round(precioBaseNoche*(1+porcentajeGanancia/100)*100) / 100
			totalConGanancia = math.Round(precioNocheConGanancia*float64(totalNoches)*100) / 100
		} else {
			totalConGanancia = math.Round(totalBase*(1+porcentajeGanancia/100)*100) / 100
		}
	}

	idReservaProveedor := fmt.Sprintf("%v", respHotel["id"])
	err = s.repo.InsertarDetalle(
		req.ReservacionID,
		req.ProveedorID,
		TipoDetalleHotel,
		idReservaProveedor,
		totalConGanancia,
		respHotel,
	)
	if err != nil {
		return nil, errors.New("error guardando detalle de reservación")
	}

	err = s.repo.RecalcularTotalReservacion(req.ReservacionID)
	if err != nil {
		return nil, errors.New("error recalculando total de reservación")
	}

	return map[string]interface{}{
		"mensaje":            "detalle de hotel agregado exitosamente",
		"reservacion_id":     req.ReservacionID,
		"total_base":         totalBase,
		"total_con_ganancia": totalConGanancia,
		"detalle":            respHotel,
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
func (s *DetalleReservacionService) llamarReservacionAerolinea(urlAPI, token string, vuelos []dto.SeleccionVuelo) (map[string]interface{}, error) {
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
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		return nil, fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	var resultado map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return nil, errors.New("respuesta inválida de la aerolínea")
	}

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
func (s *DetalleReservacionService) llamarReservacionHotel(urlAPI, token string, habitaciones []dto.SeleccionHabitacion) (map[string]interface{}, error) {
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
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		return nil, fmt.Errorf("hotelera respondió con status %d", resp.StatusCode)
	}

	var resultado map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return nil, errors.New("respuesta inválida de la hotelera")
	}

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

	return s.llamarPasajerosAerolinea(urlAPI, token, reservacionAerolineaID, req.Pasajeros)
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
	urlAPI, token string,
	reservacionID int,
	pasajeros []dto.PasajeroVueloDTO,
) error {

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
		return fmt.Errorf("error contactando aerolínea: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		var errResp map[string]interface{}
		json.NewDecoder(resp.Body).Decode(&errResp)
		if msg, ok := errResp["message"].(string); ok {
			return fmt.Errorf("aerolínea rechazó la solicitud: %s", msg)
		}
		return fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	return nil
}
