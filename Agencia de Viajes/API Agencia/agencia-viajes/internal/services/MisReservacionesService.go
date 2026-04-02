package services

import (
	"agencia-viajes/internal/dto"
	"agencia-viajes/internal/repositories"
	"crypto/tls"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
)

type MisReservacionesService struct {
	repo *repositories.MisReservacionesRepository
}

func NewMisReservacionesService(repo *repositories.MisReservacionesRepository) *MisReservacionesService {
	return &MisReservacionesService{repo: repo}
}

// ─── Ruta 1: Listar todas las reservaciones (solo datos locales) ──────────────

func (s *MisReservacionesService) ListarReservaciones(usuarioID int) ([]dto.ReservacionResumenResponse, error) {
	filas, err := s.repo.ObtenerReservacionesDeUsuario(usuarioID)
	if err != nil {
		return nil, err
	}

	// Agrupar filas por reservacion_id
	orden := []int{}
	mapa := map[int]*dto.ReservacionResumenResponse{}

	for _, f := range filas {
		if _, existe := mapa[f.ReservacionID]; !existe {
			orden = append(orden, f.ReservacionID)
			res := &dto.ReservacionResumenResponse{
				ID:              f.ReservacionID,
				NoReservacion:   f.NoReservacion,
				TipoReserva:     f.TipoReservaID,
				EstadoID:        f.EstadoID,
				Total:           f.Total,
				FechaCreacion:   f.FechaCreacion,
				FechaExpiracion: f.FechaExpiracion,
				Detalles:        []dto.DetalleResumenResponse{},
			}
			mapa[f.ReservacionID] = res
		}

		var params interface{}
		_ = json.Unmarshal([]byte(f.ParametrosJson), &params)

		mapa[f.ReservacionID].Detalles = append(mapa[f.ReservacionID].Detalles, dto.DetalleResumenResponse{
			ID:                 f.DetalleID,
			TipoDetalleID:      f.TipoDetalleID,
			IDReservaProveedor: f.IDReservaProveedor,
			Total:              f.DetalleTotal,
			EstadoDetalleID:    f.EstadoDetalleID,
			ParametrosJson:     params,
		})
	}

	resultado := make([]dto.ReservacionResumenResponse, 0, len(orden))
	for _, id := range orden {
		resultado = append(resultado, *mapa[id])
	}
	return resultado, nil
}

// ─── Ruta 2: Detalle completo llamando a proveedores ─────────────────────────

func (s *MisReservacionesService) ObtenerDetalle(reservacionID, usuarioID int) (*dto.ReservacionDetalladaResponse, error) {
	filas, err := s.repo.ObtenerReservacionPorID(reservacionID, usuarioID)
	if err != nil {
		return nil, err
	}
	if len(filas) == 0 {
		return nil, errors.New("reservación no encontrada o no pertenece al usuario")
	}

	primera := filas[0]
	resp := &dto.ReservacionDetalladaResponse{
		ID:              primera.ReservacionID,
		NoReservacion:   primera.NoReservacion,
		TipoReserva:     primera.TipoReservaID,
		EstadoID:        primera.EstadoID,
		Total:           primera.Total,
		FechaCreacion:   primera.FechaCreacion,
		FechaExpiracion: primera.FechaExpiracion,
		Detalles:        []dto.DetalleCompletoResponse{},
	}

	for _, f := range filas {
		var params interface{}
		_ = json.Unmarshal([]byte(f.ParametrosJson), &params)

		dataProveedor, err := s.consultarProveedor(f.TipoDetalleID, f.IDReservaProveedor, f.URLAPI, f.TokenEntrada)
		if err != nil {
			dataProveedor = map[string]string{"error": err.Error()}
		}

		resp.Detalles = append(resp.Detalles, dto.DetalleCompletoResponse{
			ID:                 f.DetalleID,
			TipoDetalleID:      f.TipoDetalleID,
			IDReservaProveedor: f.IDReservaProveedor,
			Total:              f.DetalleTotal,
			EstadoDetalleID:    f.EstadoDetalleID,
			ParametrosJson:     params,
			DataProveedor:      dataProveedor,
		})
	}

	return resp, nil
}

// consultarProveedor — llama al endpoint correcto según tipo
func (s *MisReservacionesService) consultarProveedor(tipoDetalleID int, idReservaProveedor, urlAPI, token string) (interface{}, error) {
	var url string
	switch tipoDetalleID {
	case 1: // Aerolínea
		url = fmt.Sprintf("%s/api/reservaciones-agencia/gestion/%s", urlAPI, idReservaProveedor)
	case 2: // Hotel
		url = fmt.Sprintf("%s/agencia/reservaciones/%s", urlAPI, idReservaProveedor)
	default:
		return nil, fmt.Errorf("tipo de detalle desconocido: %d", tipoDetalleID)
	}

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("X-Agencia-Token", token)
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{
		Transport: &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		},
	}

	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("error contactando proveedor: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		return nil, fmt.Errorf("proveedor respondió con status %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var resultado interface{}
	if err := json.Unmarshal(body, &resultado); err != nil {
		return nil, err
	}
	return resultado, nil
}
