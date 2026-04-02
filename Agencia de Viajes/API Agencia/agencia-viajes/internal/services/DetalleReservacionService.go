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

type DetalleReservacionService struct {
	repo *repositories.DetalleReservacionRepository
}

func NewDetalleReservacionService(db *sql.DB) *DetalleReservacionService {
	return &DetalleReservacionService{
		repo: repositories.NewDetalleReservacionRepository(db),
	}
}

func (s *DetalleReservacionService) AgregarDetalleVuelo(usuarioID int, req dto.AgregarDetalleVueloRequest) (interface{}, error) {

	// 1. Validar reservacion
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

	// 2. Obtener datos del proveedor validando tipo
	urlAPI, tokenEntrada, porcentajeGanancia, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleVuelo)
	if err != nil {
		return nil, err
	}

	// 3. Llamar a la aerolínea
	respAerolinea, err := s.llamarReservacionAerolinea(urlAPI, tokenEntrada, req.Vuelos)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en aerolínea: %w", err)
	}

	// 4. Calcular total con ganancia — aplicar markup por boleto (igual que la búsqueda)
	totalBase := respAerolinea["total"].(float64)

	// Contar pasajeros totales del request para aplicar markup por boleto
	var totalPasajeros int
	for _, v := range req.Vuelos {
		if v.CantidadPasajeros > 0 {
			totalPasajeros += v.CantidadPasajeros
		}
	}

	var totalConGanancia float64
	if totalPasajeros > 0 {
		// Precio base por boleto → aplicar markup redondeado → multiplicar por pasajeros
		// Garantiza que coincida con lo que muestra la búsqueda
		precioBaseBoleto := totalBase / float64(totalPasajeros)
		precioBoletoConGanancia := math.Round(precioBaseBoleto*(1+porcentajeGanancia/100)*100) / 100
		totalConGanancia = math.Round(precioBoletoConGanancia*float64(totalPasajeros)*100) / 100
	} else {
		// Fallback si no hay pasajeros en el request
		totalConGanancia = math.Round(totalBase*(1+porcentajeGanancia/100)*100) / 100
	}

	// 5. Guardar detalle
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

	// 6. Recalcular total de la reservación principal
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

func (s *DetalleReservacionService) AgregarDetalleHotel(usuarioID int, req dto.AgregarDetalleHotelRequest) (interface{}, error) {

	// 1. Validar reservacion
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

	// 2. Obtener datos del proveedor validando tipo
	urlAPI, tokenEntrada, porcentajeGanancia, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleHotel)
	if err != nil {
		return nil, err
	}

	// 3. Llamar a la hotelera
	respHotel, err := s.llamarReservacionHotel(urlAPI, tokenEntrada, req.Habitaciones)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en hotelera: %w", err)
	}

	// 4. Calcular total con ganancia — aplicar markup por noche (igual que la búsqueda)
	totalBase := respHotel["total"].(float64)

	// Contar noches totales del request para aplicar markup por noche
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

	var totalConGanancia float64
	if totalNoches > 0 {
		// Precio base por noche → aplicar markup redondeado → multiplicar por noches
		// Garantiza que coincida con lo que muestra la búsqueda ($83.07 × 10 = $830.70)
		precioBaseNoche := totalBase / float64(totalNoches)
		precioNocheConGanancia := math.Round(precioBaseNoche*(1+porcentajeGanancia/100)*100) / 100
		totalConGanancia = math.Round(precioNocheConGanancia*float64(totalNoches)*100) / 100
	} else {
		// Fallback si no se pueden parsear las fechas
		totalConGanancia = math.Round(totalBase*(1+porcentajeGanancia/100)*100) / 100
	}

	// 5. Guardar detalle
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

	// 6. Recalcular total de la reservación principal
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

func (s *DetalleReservacionService) AgregarPasajerosVuelo(
	usuarioID int,
	req dto.AgregarPasajerosVueloRequest,
) error {

	// 1. Validar pasaportes
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

	// 2. Obtener datos del detalle
	idReservaAerolinea, urlAPI, token, err := s.repo.ObtenerDetalleAerolineaPorProveedor(
		req.ReservacionID, usuarioID, req.ProveedorID,
	)
	if err != nil {
		return err
	}

	// 3. Convertir ID string → int
	reservacionAerolineaID, err := strconv.Atoi(idReservaAerolinea)
	if err != nil {
		return fmt.Errorf("id de reservación de aerolínea inválido: %w", err)
	}

	// 4. Llamar a la aerolínea
	return s.llamarPasajerosAerolinea(urlAPI, token, reservacionAerolineaID, req.Pasajeros)
}

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
