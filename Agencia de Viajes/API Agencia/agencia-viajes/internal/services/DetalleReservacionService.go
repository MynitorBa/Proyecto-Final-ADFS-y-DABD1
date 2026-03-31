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

	// 2. Obtener datos del proveedor
	urlAPI, tokenEntrada, porcentajeGanancia, err := s.repo.ObtenerDatosProveedorPorTipo(req.ProveedorID, TipoDetalleVuelo)
	if err != nil {
		return nil, err
	}

	// 3. Llamar a la aerolínea
	respAerolinea, err := s.llamarReservacionAerolinea(urlAPI, tokenEntrada, req.Vuelos)
	if err != nil {
		return nil, fmt.Errorf("error al reservar en aerolínea: %w", err)
	}

	// 4. Calcular total con ganancia
	totalBase := respAerolinea["total"].(float64)
	totalConGanancia := math.Round(totalBase*(1+porcentajeGanancia/100)*100) / 100

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

	// 6. Actualizar total de la reservación principal
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

func (s *DetalleReservacionService) llamarReservacionAerolinea(urlAPI, token string, vuelos []dto.SeleccionVuelo) (map[string]interface{}, error) {
	body, _ := json.Marshal(map[string]interface{}{
		"vuelos": vuelos,
	})

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

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("aerolínea respondió con status %d", resp.StatusCode)
	}

	var resultado map[string]interface{}
	if err := json.NewDecoder(resp.Body).Decode(&resultado); err != nil {
		return nil, errors.New("respuesta inválida de la aerolínea")
	}

	return resultado, nil
}
